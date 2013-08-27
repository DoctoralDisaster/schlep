package com.netflix.schlep.producer;

import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.config.ConfigurationReader;
import com.netflix.schlep.exception.ProducerException;

/**
 * Main entrypoint for attaining a producer used to send messages to 
 * an endpoint.  
 * 
 * @author elandau
 *
 */
public interface MessageProducerFactory {
    /**
     * Return the producer implementation for sending events specified by the key
     * @param key    Type of messages being produced
     * @param config Initial configuration for the Producer
     * @return
     * @throws Exception 
     */
    <T> MessageProducer<T> createProducer(EndpointKey<T> key, ConfigurationReader mapper) throws ProducerException;
}
