package com.netflix.schlep.sim;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Singleton;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.config.ConfigurationReader;
import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.consumer.MessageCallback;
import com.netflix.schlep.consumer.MessageConsumer;
import com.netflix.schlep.consumer.MessageConsumerFactory;
import com.netflix.schlep.exception.ConsumerException;
import com.netflix.schlep.producer.OutgoingMessage;

@Singleton
public class SimMessageConsumerProvider implements MessageConsumerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SimMessageConsumerProvider.class);
    
    private Map<String, CallbackHolder<?>> callbacks = Maps.newHashMap();
    
    private static class CallbackHolder<T> implements MessageConsumer<T> {
        private final MessageCallback<T> callback;
        
        public CallbackHolder(MessageCallback<T> callback) {
            this.callback = callback;
        }
        
        public void send(final OutgoingMessage<T> message) throws ConsumerException {
            callback.consume(new IncomingMessage<T>() {
                @Override
                public T getEntity() {
                    return message.getMessage();
                }

                @Override
                public ListenableFuture<Boolean> ack() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public ListenableFuture<Boolean> nak() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public ListenableFuture<Boolean> renew(long duration,
                        TimeUnit units) {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public ListenableFuture<Boolean> reply(T message) {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public long getTimeSinceReceived(TimeUnit units) {
                    // TODO Auto-generated method stub
                    return 0;
                }
            });
        }

        @Override
        public void pause() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void resume() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void start() throws Exception {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void stop() throws Exception {
            // TODO Auto-generated method stub
            
        }
    }
    
    public <T> void send(EndpointKey<T> key, OutgoingMessage<T> message) throws ConsumerException {
        CallbackHolder consumer = (CallbackHolder) callbacks.get(key.getName());
        if (consumer != null) {
            consumer.send(message);
        }
        else {
            LOG.info("Can't find holder for " + key);
        }
    }
    
    @Override
    public <T> MessageConsumer<T> createSubscriber(EndpointKey<T> key, ConfigurationReader mapper, MessageCallback<T> callback) {
        LOG.info("Subscribing to " + key);
        CallbackHolder consumer = callbacks.get(key.getName());
        if (consumer == null) {
            consumer = new CallbackHolder<T>(callback);
            callbacks.put(key.getName(), consumer);
        }
        return consumer;
    }
}
