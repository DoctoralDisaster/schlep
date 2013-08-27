package com.netflix.schlep.consumer;

import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.config.ConfigurationReader;
import com.netflix.schlep.exception.ConsumerException;

/**
 * Factory for instantiating a subscriber.  
 * 
 * @author elandau
 */
public interface MessageConsumerFactory {
    /**
     * Create the underlying protocol layer to start consuming events from 
     * the specified endpoint and passing them one by one to the provided callback.
     * 
     * Must call .start() on the returned MessageConsumer to begin dispatching
     * events to the callback.
     * 
     * @param key       Identify source of events and message type
     * @param config    Initial configuration
     * @param callback  Callback to invoke for each message.
     * @return
     * @throws ConsumerException
     */
    public <T> MessageConsumer<T> createSubscriber(
            EndpointKey<T>          key, 
            ConfigurationReader     mapper, 
            MessageCallback<T>      callback) 
                    throws ConsumerException;
}
