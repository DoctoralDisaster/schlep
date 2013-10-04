package com.netflix.schlep.producer;

import com.netflix.schlep.exception.ProducerException;

/**
 * Interface for a MessagePorducer factory that creates consumers from an underlying configuration
 * mechanism.
 * 
 * @author elandau
 *
 */
public interface DefaultMessageProducerFactory {
    public MessageProducer create(String id) throws ProducerException;
}
