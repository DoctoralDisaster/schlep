package com.netflix.schlep.sim;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.netflix.schlep.MessageConsumerProvider;
import com.netflix.schlep.MessageProducer;
import com.netflix.schlep.MessageProducerProvider;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.OutgoingMessage;
import com.netflix.schlep.exception.ConsumerException;
import com.netflix.schlep.exception.ProducerException;

public class SimMessageProducerProvider implements MessageProducerProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SimMessageProducerProvider.class);

    private SimMessageConsumerProvider consumerProvider;
    
    @Inject
    public SimMessageProducerProvider(Map<String, MessageConsumerProvider> consumers) {
        consumerProvider = (SimMessageConsumerProvider)consumers.get("sim");
    }
    
    @Override
    public <T> MessageProducer<T> getProducer(final EndpointKey<T> key) {
        return new MessageProducer<T>() {
            @Override
            public void close() throws IOException {
            }

            @Override
            public ListenableFuture<Boolean> produce(OutgoingMessage<T> message) throws ProducerException {
                LOG.info("Producing message: " + message.getMessage());
                try {
                    consumerProvider.send(key, message);
                } catch (ConsumerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                return Futures.immediateFuture(true);
            }

            @Override
            public ListenableFuture<Boolean> produce(T message) throws ProducerException {
                return produce(new OutgoingMessage<T>().withMessage(message));
            }
        };
    }
}
