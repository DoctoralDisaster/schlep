package com.netflix.schlep.writer;

import com.netflix.schlep.exception.ProducerException;
import com.netflix.schlep.serializer.Mapper;

/**
 * Main entrypoint for attaining a producer used to send messages to 
 * an endpoint.  
 * 
 * @author elandau
 *
 */
public interface MessageWriterFactory {
    /**
     * Return the producer implementation for sending events specified by the key
     * @param key    Type of messages being produced
     * @param config Initial configuration for the Producer
     * @return
     * @throws Exception 
     */
    MessageWriter createProducer(String id, Mapper mapper) throws ProducerException;
}
