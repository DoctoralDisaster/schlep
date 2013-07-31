package com.netflix.schlep;

import com.netflix.schlep.exception.ProducerException;

/**
 * Main entrypoint for attaining a producer used to send messages to 
 * an endpoint.  
 * 
 * @author elandau
 *
 */
public interface MessageProducerProvider {
    /**
     * Return the producer implementation for sending events specified by the key
     * @param key   Type of messages being produced
     * @return
     * @throws Exception 
     */
    <T> MessageProducer<T> getProducer(EndpointKey<T> key) throws ProducerException;
}
