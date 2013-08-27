package com.netflix.schlep.consumer;

import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Encapsulate the context of an incoming message on a consumer callback.  
 * 
 * @author elandau
 */
public interface IncomingMessage<T> {
    /**
     * Return the time since the message was received on this consumer
     * @return
     */
    public long getTimeSinceReceived(TimeUnit units);
    
    /**
     * @return Return the message contents
     */
    public T getEntity();
    
    /**
     * Acknowledge the incoming message to reflect that the message was consumed
     * @return 
     */
    public ListenableFuture<Boolean> ack();
    
    /**
     * Negative acknowledgment of a message to reflect that the message was not consumed
     * TODO:  What next?  Should it be put on a poison queue
     */
    public ListenableFuture<Boolean> nak();
    
    /**
     * Delay sending the ack or 'renew lease' to prevent timeout for messages with 
     * long processing time
     * @param duration
     * @param units
     * @return 
     */
    public ListenableFuture<Boolean> renew(long duration, TimeUnit units);
    
    /**
     * Reply directly to the sender of this message.  This may be used when a two-way path
     * exists between the consumer and producer, such as an RPC response. 
     * 
     * Note that for now the same message format is used for the response.
     * @param message
     */
    public ListenableFuture<Boolean> reply(T message);
}
