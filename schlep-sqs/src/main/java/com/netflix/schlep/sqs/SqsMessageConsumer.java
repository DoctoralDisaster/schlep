package com.netflix.schlep.sqs;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.model.Message;
import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.consumer.MessageConsumer;
import com.netflix.schlep.exception.ConsumerException;
import com.netflix.schlep.mapper.Serializer;
import com.netflix.schlep.sqs.transform.FromBase64Transform;
import com.netflix.schlep.sqs.transform.NoOpTransform;
import com.netflix.schlep.util.UnstoppableStopwatch;
import com.netflix.util.batch.Batcher;

class SqsMessageConsumer implements MessageConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(SqsMessageConsumer.class);
    
    private static final long THROTTLE_TIMESPAN = 1000;

    private final MessageCallback<T>                callback;
    private final SqsClient                         client;
    private final EndpointKey<T>                    key;
    private final String                            consumerName;
    private Function<String, String>                transform;
    private final Serializer<T>                     serializer;
    private final SqsClientConfiguration            clientConfig;
    private ExecutorService                         executor;
    private final Batcher<MessageFuture<Boolean>>   ackBatcher;
    private final Batcher<MessageFuture<Boolean>>   renewBatcher;
    
    public SqsMessageConsumer(EndpointKey<T> key, SqsClient client, SqsClientConfiguration config, MessageCallback<T> callback) throws ConsumerException {
        this.callback       = callback;
        this.key            = key;
        this.consumerName   = key.getName();
        this.clientConfig   = config;
        this.client         = client;
        this.serializer     = config.getSerializerFactory().findSerializer(key.getMessageType());

        if (this.clientConfig.getEnable64Encoding()) {
            transform = new FromBase64Transform();
        }
        else {
            transform = new NoOpTransform();
        }
        
        ackBatcher = null;
        renewBatcher = null;
        
//        this.ackBatcher     = clientConfig.getBatchPolicy().create(new Function<List<MessageFuture<Boolean>>, Boolean>() {
//            public Boolean apply(List<MessageFuture<Boolean>> messages) {
//                SqsMessageConsumer.this.client.deleteMessages(messages);
//                return true;
//            }
//        });
//        
//        this.renewBatcher     = clientConfig.getBatchPolicy().create(new Function<List<MessageFuture<Boolean>>, Boolean>() {
//            public Boolean apply(List<MessageFuture<Boolean>> messages) {
//                SqsMessageConsumer.this.client.renewMessages(messages);
//                return true;
//            }
//        });
    }
    
    @Override
    public synchronized void start() throws Exception {
        if (executor != null) 
            return;
        
        LOG.info("Starting - " + consumerName);
        
        int threadCount = clientConfig.getWorkerThreadCount();
        executor = Executors.newFixedThreadPool(
                threadCount, 
                new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat(consumerName + "-%d")
                    .build());
        for (int i = 0; i < threadCount; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!Thread.interrupted()) {
                            try {
                                int count = receiveAndDispatchMessages(clientConfig.getMaxReadBatchSize(), null);
                                if (count == 0) {
                                    Thread.sleep(THROTTLE_TIMESPAN);
                                }
                            } catch (ConsumerException e) {
                                Thread.sleep(THROTTLE_TIMESPAN);
                            }
                        }
                    } catch (InterruptedException e) {
                        LOG.info("Terminating thread");
                    }
                }
            });
        }
    }
    
    @Override
    public synchronized void stop() throws Exception {
        LOG.info("Stopping - " + consumerName);
        
        if (executor != null) {
            executor.shutdown();
            if (!executor.awaitTermination(clientConfig.getTerminateTimeout(), TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    @Override
    public void pause() {
        // TODO:
    }

    @Override
    public void resume() {
        // TODO:
    }

    /**
     * Dispatch a single message that was received
     * 
     * @param message
     * @param sw
     */
    private void dispatchMessage(final Message message, final UnstoppableStopwatch sw, long visibilityTimeout) {
        // First, let's deserialize the message from a string to an entity
        T entity;
        try {
            entity = serializer.deserialize(new ByteArrayInputStream(transform.apply(message.getBody()).getBytes()));
        }
        catch (Throwable t) {
            LOG.warn("Failed to deserialize message : " + message.getBody(), t);
            // TODO: Discard the message or put in a poison queue
            return;
        }
        
        try {
            callback.consume(new SqsIncomingMessage<T>(message, entity, sw, visibilityTimeout) {
                @Override
                public ListenableFuture<Boolean> ack() {
                    MessageFuture<Boolean> future = new MessageFuture<Boolean>(message);
                    ackBatcher.add(future);
                    return future;
                }

                @Override
                public ListenableFuture<Boolean> renew(long duration, TimeUnit units) {
                    extendVisibilityTimeout(duration, units);
                    MessageFuture<Boolean> future = new MessageFuture<Boolean>(message, this.getVisibilityTimeout(TimeUnit.SECONDS));
                    renewBatcher.add(future);
                    return future;
                }

                @Override
                public ListenableFuture<Boolean> nak() {
                    throw new UnsupportedOperationException("NAK not supported for SQS");
                }

                @Override
                public ListenableFuture<Boolean> reply(T message) {
                    throw new UnsupportedOperationException("Reply not supported for SQS");
                }
            });
        }
        catch (Throwable t) {
            LOG.warn("Failed to consume message : " + message.getBody(), t);
            // TODO: Discard the message or put in a poison queue
            return;
        }            
    }
    
    /**
     * Read a batch of messages and dispatch them serially, one by one
     * @param maxMessageCount
     * @param attributes
     * @return
     * @throws ConsumerException
     */
    private int receiveAndDispatchMessages(int maxMessageCount, List<String> attributes) throws ConsumerException {
        try {
            long timeout = clientConfig.getVisibilityTimeoutSeconds();
            // Execute the request
            Collection<Message> result = client.receiveMessages(maxMessageCount, timeout, attributes);
            UnstoppableStopwatch sw = new UnstoppableStopwatch();
            // Transform to internal response
            for (Message message : result) {
                dispatchMessage(message, sw, timeout);
            }
            
            return result.size();
        } catch (Exception e) {
            throw new ConsumerException("Error consuming messages " + key, e);
        }
    }
}