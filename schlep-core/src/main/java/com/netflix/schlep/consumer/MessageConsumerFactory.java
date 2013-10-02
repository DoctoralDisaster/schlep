package com.netflix.schlep.consumer;

import com.netflix.schlep.exception.ConsumerException;
import com.netflix.schlep.serializer.Mapper;

/**
 * Factory for creating an instance of a consumer from a configuration
 * 
 * @author elandau
 */
public interface MessageConsumerFactory {
    /**
     * Create the underlying protocol layer.  Messages will not be consumed
     * until 'start' is called.
     * @param id 
     * 
     * @param Mapper    Mapper from which the configuration instance can be 
     *                  Instantiated.
     * @return
     * @throws ConsumerException
     */
    public MessageConsumer createConsumer(String id, Mapper mapper) throws ConsumerException;
}
