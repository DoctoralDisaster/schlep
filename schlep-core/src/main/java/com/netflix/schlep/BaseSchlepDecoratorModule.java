package com.netflix.schlep;

import com.netflix.schlep.consumer.MessageConsumer;
import com.netflix.schlep.consumer.MessageConsumerContext;
import com.netflix.schlep.producer.MessageProducer;
import com.netflix.schlep.producer.MessageProducerContext;

public class BaseSchlepDecoratorModule implements SchlepDecoratorModule {

    @Override
    public <T> MessageConsumer<T> decorateConsumer(MessageConsumerContext<T> context) {
        return context.getConsumer();
    }

    @Override
    public <T> MessageProducer<T> decorateProducer(MessageProducerContext<T> context) {
        return context.getProducer();
    }

}
