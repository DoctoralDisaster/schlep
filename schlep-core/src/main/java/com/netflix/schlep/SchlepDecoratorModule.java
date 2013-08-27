package com.netflix.schlep;

import com.netflix.schlep.consumer.MessageConsumer;
import com.netflix.schlep.consumer.MessageConsumerContext;
import com.netflix.schlep.producer.MessageProducer;
import com.netflix.schlep.producer.MessageProducerContext;

/**
 * Interface for extending and decorating schlep
 * 
 * @author elandau
 *
 */
public interface SchlepDecoratorModule {
    public <T> MessageConsumer<T> decorateConsumer(MessageConsumerContext<T> context);
    
    public <T> MessageProducer<T> decorateProducer(MessageProducerContext<T> context);
}
