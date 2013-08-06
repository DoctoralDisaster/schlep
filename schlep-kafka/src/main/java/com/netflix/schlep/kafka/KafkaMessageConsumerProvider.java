package com.netflix.schlep.kafka;

import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.MessageCallback;
import com.netflix.schlep.MessageConsumer;
import com.netflix.schlep.MessageConsumerProvider;
import com.netflix.schlep.exception.ConsumerException;

public class KafkaMessageConsumerProvider implements MessageConsumerProvider {

    @Override
    public <T> MessageConsumer<T> subscribe(EndpointKey<T> key, MessageCallback<T> callback) throws ConsumerException {
        return new KafkaMessageConsumer<T>();
    }

    public static class KafkaMessageConsumer<T> implements MessageConsumer<T> {
        @Override
        public void pause() {
        }

        @Override
        public void resume() {
        }

        @Override
        public void start() throws Exception {
        }

        @Override
        public void stop() throws Exception {
        }
    }
}
