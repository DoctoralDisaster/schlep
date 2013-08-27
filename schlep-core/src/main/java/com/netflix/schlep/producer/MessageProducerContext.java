package com.netflix.schlep.producer;

/**
 * Part of the bootstrapping phase, MessageConsumerContext captures the current
 * context of an entity's consumer.
 * 
 * @author elandau
 *
 * @param <T>
 */
public class MessageProducerContext<T> {
    private final Class<T> type;
    
    private final MessageProducer<T> producer;
    
    public MessageProducerContext(MessageProducer<T> producer, Class<T> type) {
        this.type     = type;
        this.producer = producer;
    }
    
    public Class<T> getType() {
        return this.type;
    }
    
    public MessageProducer<T> getProducer() {
        return this.producer;
    }
}
