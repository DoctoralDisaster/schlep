package com.netflix.schlep.producer;

import com.google.common.util.concurrent.ListenableFuture;
import com.netflix.schlep.exception.ProducerException;

/**
 * Abstraction for a message producer for messages that have a delay or are
 * executed on a schedule with possible repeats.
 *   
 * A concrete message producer with all the proper configuration and protocol 
 * implementation is created at bootstrap and this interface is then passed to the 
 * producer client code.
 * 
 * @author elandau
 */
public interface ScheduledMessageProducer<T> {
    /**
     * Produce a single outgoing message on the producer.
     * @param message
     */
    public ListenableFuture<Boolean> produce(ScheduledOutgoingMessage<T> message) throws ProducerException;
}
