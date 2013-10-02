package com.netflix.schlep.consumer;

import com.netflix.schlep.exception.ConsumerException;

/**
 * Provider to get a message consumer.  The consumer is assumed to have already
 * been configured or will be extract from the default MessageConsumerProvider
 * 
 * Each consumer has a unique ID regardless of the messaging technology and
 * configuration used.
 * 
 * @author elandau
 *
 */
public interface MessageConsumerProvider {
    public MessageConsumer get(String id) throws ConsumerException;
}
