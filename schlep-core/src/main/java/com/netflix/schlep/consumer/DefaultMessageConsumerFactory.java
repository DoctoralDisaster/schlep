package com.netflix.schlep.consumer;

import com.netflix.schlep.exception.ConsumerException;

/**
 * Interface for a MessageConsumer that creates consumers from an underlying configuration
 * mechanism.
 * 
 * @author elandau
 *
 */
public interface DefaultMessageConsumerFactory {
    public MessageConsumer create(String id) throws ConsumerException;
}
