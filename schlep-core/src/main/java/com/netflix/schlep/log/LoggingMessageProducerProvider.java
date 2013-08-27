package com.netflix.schlep.log;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.config.ConfigurationReader;
import com.netflix.schlep.exception.ProducerException;
import com.netflix.schlep.producer.MessageProducer;
import com.netflix.schlep.producer.MessageProducerFactory;
import com.netflix.schlep.producer.OutgoingMessage;

public class LoggingMessageProducerProvider implements MessageProducerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingMessageProducerProvider.class);

    @Inject
    public LoggingMessageProducerProvider() {
    }
    
    @Override
    public <T> MessageProducer<T> createProducer(final EndpointKey<T> key, final ConfigurationReader mapper) {
        return new MessageProducer<T>() {
            @Override
            public ListenableFuture<Boolean> produce(OutgoingMessage<T> message) throws ProducerException {
                LOG.info("Producing message: " + message.getMessage());
                return Futures.immediateFuture(true);
            }

            @Override
            public ListenableFuture<Boolean> produce(T message) throws ProducerException {
                return produce(OutgoingMessage.<T>builder().withMessage(message).build());
            }

            @Override
            public Map<OutgoingMessage<T>, ListenableFuture<Boolean>> produceBatch(List<OutgoingMessage<T>> messages) {
                // TODO
                return null;
            }

            @Override
            public String getId() {
                return key.getName();
            }

            @Override
            public String getUri() {
                return "log://" + key.getName();
            }

            @Override
            public Class<T> getMessageType() {
                return key.getMessageType();
            }
        };
    }
}