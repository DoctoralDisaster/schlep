package com.netflix.schlep;

import com.google.common.util.concurrent.ListenableFuture;
import com.netflix.schlep.exception.ProducerException;

/**
 * Abstraction for a message producer that feeds a 'queue' or 'topic' like system.  
 * A concrete message producer with all the proper configuration and protocol 
 * implementation is created at bootstrap and this interface is then passed to the 
 * producer client code.
 * 
 * @author elandau
 */
public interface MessageProducer<T> {
    /**
     * Produce a single message.  Let the implementation wrap it in an OutgoingMessage
     * 
     * @param message
     * @throws ProducerException
     */
    public ListenableFuture<Boolean> produce(T message) throws ProducerException;
    
    /**
     * Produce a single outgoing message on the producer.
     * @param message
     */
    public ListenableFuture<Boolean> produce(OutgoingMessage<T> message) throws ProducerException;
}
