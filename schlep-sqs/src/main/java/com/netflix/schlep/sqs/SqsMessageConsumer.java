package com.netflix.schlep.sqs;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.netflix.schlep.Completion;
import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.consumer.PollingMessageConsumer;
import com.netflix.schlep.exception.ConsumerException;
import com.netflix.schlep.mapper.Serializer;
import com.netflix.schlep.sqs.transform.FromBase64Transform;
import com.netflix.schlep.util.UnstoppableStopwatch;

class SqsMessageConsumer extends PollingMessageConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(SqsMessageConsumer.class);
    
    public static final Function<String, String> DEFAULT_TRANSFORM = new FromBase64Transform();
    public static final long   DEFAULT_VISIBILITY_TIMEOUT = TimeUnit.MINUTES.toSeconds(5);
    
    private final Function<String, String> transform;
    private final Serializer               serializer;
    private final long                     visibilityTimeout;
    private final AmazonSqsClient          client;
    
    /**
     * Builder
     * 
     * @param <T>
     */
    public static abstract class Builder<T extends Builder<T>> extends PollingMessageConsumer.Builder<T> {
        private Function<String, String> transform         = DEFAULT_TRANSFORM;
        private long            visibilityTimeout = DEFAULT_VISIBILITY_TIMEOUT;
        private Serializer      serializer;
        private AmazonSqsClient.Builder clientBuilder = AmazonSqsClient.builder();
        
        public T withCredentials(AWSCredentials credentials) {
            this.clientBuilder.withCredentials(credentials);
            return self();
        }
        
        public T withTransform(Function<String, String> transform) {
            this.transform = transform;
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
            return "Builder [transform=" + transform + ", visibilityTimeout="
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
    
    protected SqsMessageConsumer(Builder<?> init) throws Exception {
        super(init);
        
        this.transform         = init.transform;
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
            for (SqsMessage message : result) {
                messages.add(new SqsIncomingMessage(message, sw, visibilityTimeout) {
                    @Override
                    public void ack() {
                    }

                    @Override
                    public void nak() {
                    }
                });
            }
            
            return messages;
        } catch (Exception e) {
            throw new ConsumerException("Error consuming messages " + getId(), e);
        }
    }

    @Override
    protected void sendAckBatch(List<Completion<IncomingMessage>> act) {
        // TODO Auto-generated method stub
        
    }
}