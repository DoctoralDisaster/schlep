package com.netflix.schlep.consumer;

/**
 * Part of the bootstrapping phase, MessageConsumerContext captures the current
 * context of an entity's consumer.
 * 
 * @author elandau
 *
 * @param <T>
 */
public class MessageConsumerContext<T> {
    private final Class<T> type;
    
    private final MessageConsumer<T> consumer;
    
    public MessageConsumerContext(Class<T> type, MessageConsumer<T> consumer) {
        this.type     = type;
        this.consumer = consumer;
    }
    
    public Class<T> getType() {
        return this.type;
    }
    
    public MessageConsumer<T> getConsumer() {
        return this.consumer;
    }
}
