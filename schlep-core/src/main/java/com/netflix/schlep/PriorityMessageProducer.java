package com.netflix.schlep;

import com.google.common.util.concurrent.ListenableFuture;
import com.netflix.schlep.exception.ProducerException;

/**
 * Abstraction for a message producer for messages that must be processed in 
 * priority order.  This differs from ScheduleMessageProcuder in that there
 * are no delays in executing the messages.
 *   
 * A concrete message producer with all the proper configuration and protocol 
 * implementation is created at bootstrap and this interface is then passed to the 
 * producer client code.
 * 
 * @author elandau
 */
public interface PriorityMessageProducer<T> {
    /**
     * Produce a single outgoing message on the producer.
     * @param message
     */
    public ListenableFuture<Boolean> produce(PriorityOutgoingMessage<T> message) throws ProducerException;

}
