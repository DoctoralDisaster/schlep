package com.netflix.schlep.sqs.consumer;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.google.common.collect.Lists;
import com.netflix.schlep.Completion;
import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.consumer.PollingMessageConsumer;
import com.netflix.schlep.exception.ConsumerException;
import com.netflix.schlep.mapper.Base64Serializer;
import com.netflix.schlep.mapper.Serializer;
import com.netflix.schlep.sqs.AmazonSqsClient;
import com.netflix.schlep.sqs.SqsMessage;
import com.netflix.schlep.util.UnstoppableStopwatch;

class SqsMessageConsumer extends PollingMessageConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(SqsMessageConsumer.class);
    
    public static final long        DEFAULT_VISIBILITY_TIMEOUT = TimeUnit.MINUTES.toSeconds(5);
    public static final Serializer  DEFAULT_SERIALIZER         = new Base64Serializer();
    
    /**
     * Builder
     * 
     * @param <T>
     */
    public static abstract class Builder<T extends Builder<T>> extends PollingMessageConsumer.Builder<T> {
        private long            visibilityTimeout     = DEFAULT_VISIBILITY_TIMEOUT;
        private Serializer      serializer            = DEFAULT_SERIALIZER;
        private AmazonSqsClient.Builder clientBuilder = AmazonSqsClient.builder();
        
        public T withCredentials(AWSCredentials credentials) {
            this.clientBuilder.withCredentials(credentials);
            return self();
        }
        
        public T withVisibilityTimeout(long timeout) {
            this.visibilityTimeout = timeout;
            return self();
        }
        
        public T withSerializer(Serializer serializer) {
            this.serializer = serializer;
            return self();
        }
        
        public T withConnectionTimeout(int connectTimeout) {
            clientBuilder.withConnectionTimeout(connectTimeout);
            return self();
        }
        
        public T withReadTimeout(int readTimeout) {
            clientBuilder.withReadTimeout(readTimeout);
            return self();
        }
        
        public T withMaxConnections(int maxConnections) {
            clientBuilder.withMaxConnections(maxConnections);
            return self();
        }
        
        public T withMaxRetries(int retries) {
            clientBuilder.withMaxRetries(retries);
            return self();
        }

        public T withQueueName(String queueName) {
            clientBuilder.withQueueName(queueName);
            return self();
        }
        
        public T withRegion(String region) {
            clientBuilder.withRegion(region);
            return self();
        }
        
        public SqsMessageConsumer build() throws Exception {
            return new SqsMessageConsumer(this);
        }

        @Override
        public String toString() {
            return "Builder [visibilityTimeout="
                    + visibilityTimeout + ", serializer=" + serializer
                    + ", clientBuilder=" + clientBuilder + "]";
        }

    }
    
    /**
     * BuilderWrapper to link with subclass Builder
     * @author elandau
     *
     */
    private static class BuilderWrapper extends Builder<BuilderWrapper> {
        @Override
        protected BuilderWrapper self() {
            return this;
        }
    }
    
    public static Builder<?> builder() {
        return new BuilderWrapper();
    }
    
    private final Serializer               serializer;
    private final long                     visibilityTimeout;
    private final AmazonSqsClient          client;

    private final AtomicLong    ackInvalid = new AtomicLong(0);
    private final AtomicLong    ackFailure = new AtomicLong(0);
    private final AtomicLong    ackSuccess = new AtomicLong(0);
    
    protected SqsMessageConsumer(Builder<?> init) throws Exception {
        super(init);
        
        this.visibilityTimeout = init.visibilityTimeout;
        this.serializer        = init.serializer;
        this.client            = init.clientBuilder.build();
    }
    
    /**
     * Read a batch of messages and dispatch them serially, one by one
     * @param maxMessageCount
     * @param attributes
     * @return
     * @throws ConsumerException
     */
    @Override
    protected List<IncomingMessage> readBatch(int batchSize) throws ConsumerException {
        try {
            long timeout = visibilityTimeout;
            
            // Execute the request
            Collection<SqsMessage> result = client.receiveMessages(batchSize, timeout, null);
            UnstoppableStopwatch   sw     = new UnstoppableStopwatch();
            
            // Transform to internal response
            List<IncomingMessage> messages = Lists.newArrayList();
            for (final SqsMessage message : result) {
                messages.add(new SqsIncomingMessage(message, sw, visibilityTimeout) {
                    @Override
                    public <T> T getContents(Class<T> clazz) {
                        ByteArrayInputStream bais = new ByteArrayInputStream(message.getMessage().getBody().getBytes()); 
                        try {
                            return (T)serializer.deserialize(bais, clazz);
                        } catch (Exception e) {
                            LOG.error("Failed to deserialize message", e);
                            throw new RuntimeException("Bad data format", e);
                        }
                    }
                });
            }
            
            return messages;
        } catch (Exception e) {
            throw new ConsumerException("Error consuming messages " + getId(), e);
        }
    }

    @Override
    protected void sendAckBatch(List<Completion<IncomingMessage>> messages) {
        List<Completion<IncomingMessage>> toAck = Lists.newArrayList(messages);
        while (!toAck.isEmpty()) {
            try {
                // Construct a delete message request and assign each message an ID equivalent to it's position
                // in the original list for fast lookup on the response
                final List<DeleteMessageBatchRequestEntry> batchReqEntries = new ArrayList<DeleteMessageBatchRequestEntry>(messages.size());
                int id = 0;
                for (Completion<IncomingMessage> message : messages) {
                    SqsIncomingMessage sqsMessage = (SqsIncomingMessage)(message.getValue());
                    batchReqEntries.add(new DeleteMessageBatchRequestEntry(
                            Integer.toString(id), 
                            sqsMessage.getMessage().getMessage().getReceiptHandle()));
                    ++id;
                }
                
                // Send the request
                DeleteMessageBatchResult result = client.deleteMessageBatch(batchReqEntries);
                if (result.getSuccessful() != null) {
                    ackSuccess.addAndGet(result.getSuccessful().size());
                }

                // Handle failed sends
                if (result.getFailed() != null && !result.getFailed().isEmpty()) {
                    toAck = Lists.newArrayListWithCapacity(result.getFailed().size());
                    for (BatchResultErrorEntry entry : result.getFailed()) {
                        // There cannot be resent and are probably the result of something like message exceeding
                        // the max size or certificate errors
                        if (entry.isSenderFault()) {
                            ackInvalid.incrementAndGet();
                            // TODO: messages.get(Integer.parseInt(entry.getId())).setException(new ProducerException(entry.getCode()));
                        }
                        // These messages can probably be resent and may be due to issues on the amazon side, 
                        // such as service timeout
                        else {
                            ackFailure.incrementAndGet();
                            toAck.add(messages.get(Integer.parseInt(entry.getId())));
                        }
                    }
                }
                else {
                    return;
                }
            } catch (Exception e) {
                LOG.error("Error acking messages " + getId(), e);
            }
        }
    }
}