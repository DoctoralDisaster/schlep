package com.netflix.schlep.producer;

import com.netflix.schlep.exception.ProducerException;

public interface ScheduledMessageProducerProvider {
    /**
     * Return the producer implementation for sending events specified by the key
     * @param key   Type of messages being produced
     * @return
     * @throws Exception 
     */
    <T> ScheduledMessageProducer<T> getProducer(String id) throws ProducerException;
}
