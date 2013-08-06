package com.netflix.schlep.kafka;

import java.io.IOException;

import com.google.common.util.concurrent.ListenableFuture;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.MessageProducer;
import com.netflix.schlep.MessageProducerProvider;
import com.netflix.schlep.OutgoingMessage;
import com.netflix.schlep.exception.ProducerException;

public class KafkaMessageProducerProvider implements MessageProducerProvider {

    @Override
    public <T> MessageProducer<T> getProducer(EndpointKey<T> key) throws ProducerException {
        return new ApqMessageProducer<T>();
    }

    public static class ApqMessageProducer<T> implements MessageProducer<T> {
        @Override
        public ListenableFuture<Boolean> produce(T message) throws ProducerException {
            return null;
        }

        @Override
        public ListenableFuture<Boolean> produce(OutgoingMessage<T> message) throws ProducerException {
            return null;
        }
        
    }
}
