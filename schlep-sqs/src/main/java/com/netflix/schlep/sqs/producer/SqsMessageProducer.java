package com.netflix.schlep.sqs.producer;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageBatchResultEntry;
import com.google.common.collect.Lists;
import com.netflix.schlep.exception.ProducerException;
import com.netflix.schlep.mapper.Serializer;
import com.netflix.schlep.producer.ConcurrentMessageProducer;
import com.netflix.schlep.sqs.AmazonSqsClient;
import com.netflix.schlep.sqs.AmazonSqsClient.Builder;
import com.netflix.schlep.sqs.serializer.Base64Serializer;

/**
 * MessageProducer that sends messages to an SQS queue.
 * 
 * @author elandau
 *
 * @param <T>
 */
public class SqsMessageProducer extends ConcurrentMessageProducer {
    private static final Logger LOG = LoggerFactory.getLogger(SqsMessageProducer.class);
    
    public static final Serializer DEFAULT_SERIALIZER = new Base64Serializer();
    public static final long   DEFAULT_VISIBILITY_TIMEOUT = 5;
    
    /**
     * Builder
     * 
     * @param <T>
     */
    public static abstract class Builder<T extends Builder<T>> extends ConcurrentMessageProducer.Builder<T> {
        private Serializer               serializer    = DEFAULT_SERIALIZER;
        private AmazonSqsClient.Builder  clientBuilder = AmazonSqsClient.builder();
        
        public T withCredentials(AWSCredentials credentials) {
            this.clientBuilder.withCredentials(credentials);
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
        
        public SqsMessageProducer build() throws Exception {
            return new SqsMessageProducer(this);
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
    private final AmazonSqsClient          client;
    private final MessageDigest            digest;
    
    // stats
    private final AtomicLong    sendSuccess = new AtomicLong();
    private final AtomicLong    sendInvalid = new AtomicLong();
    private final AtomicLong    sendFailure = new AtomicLong();

    protected SqsMessageProducer(Builder<?> init) throws Exception {
        super(init);
        this.serializer        = init.serializer;
        this.client            = init.clientBuilder.build();
        
        try {
            this.digest       = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new ProducerException(e.getMessage(), e);
        }
    }

    @Override
    protected void sendMessages(final List<ObservableCompletion> messages) {
        List<ObservableCompletion> toSend = Lists.newArrayList(messages);
        
        while (!toSend.isEmpty()) {
            // Construct a send message request and assign each message an ID equivalent to its position
            // in the original list for fast lookup on the response
            final List<SendMessageBatchRequestEntry> batchReqEntries = new ArrayList<SendMessageBatchRequestEntry>(toSend.size());
            int id = 0;
            for (ObservableCompletion completion : toSend) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    serializer.serialize(completion.getValue().getMessage(), baos);
                    // TODO: Add delay
                    batchReqEntries.add(
                            new SendMessageBatchRequestEntry()
                                .withId(Integer.toString(id))
                                .withMessageBody(baos.toString()));
                } catch (Exception e) {
                    LOG.warn("Error sending message", e);
                    completion.setError(new ProducerException("Error building send request", e));
                    completion.done();
                }
                
                id++;
            }
            
            // Send the request
            SendMessageBatchResult result = client.sendMessageBatch(batchReqEntries);
            
            // Update the future for successful sends
            if (result.getSuccessful() != null) {
                for (SendMessageBatchResultEntry entry : result.getSuccessful()) {
                    toSend.get(Integer.parseInt(entry.getId()));
                    sendSuccess.incrementAndGet();
                }
            }
            
            // Handle failed sends
            if (result.getFailed() != null && !result.getFailed().isEmpty()) {
                List<ObservableCompletion> retryableMessages = Lists.newArrayListWithCapacity(result.getFailed().size());
                for (BatchResultErrorEntry entry : result.getFailed()) {
                    // There cannot be resent and are probably the result of something like message exceeding
                    // the max size or certificate errors
                    if (entry.isSenderFault()) {
                        sendInvalid.incrementAndGet();
                        ObservableCompletion completion = toSend.get(Integer.parseInt(entry.getId()));
                        completion.setError(new ProducerException(entry.getCode()));
                        completion.done();
                    }
                    // These messages can probably be resent and may be due to issues on the amazon side, 
                    // such as service timeout
                    else {
                        sendFailure.incrementAndGet();
                        retryableMessages.add(toSend.get(Integer.parseInt(entry.getId())));
                    }
                }
                toSend = retryableMessages;
                
                try { // Implement a retry policy
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            // All sent OK
            else {
                return;
            }      
        }
    }
}
