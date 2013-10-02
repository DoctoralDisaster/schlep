package com.netflix.schlep.consumer;

import java.util.concurrent.TimeUnit;

/**
 * Encapsulate the context of an incoming message on a consumer callback.  
 * 
 * @author elandau
 */
public interface IncomingMessage {
    /**
     * Return the time since the message was received on this consumer
     * @return
     */
    public long getTimeSinceReceived(TimeUnit units);
    
    /**
     * @return Return the message contents
     */
    public <T> T getContents(Class<T> clazz);
    
    /**
     * Acknowledge processing of the message
     */
    public void ack();
    
    /**
     * Acknowledge refusal to process the message
     */
    public void nak();

    /**
     * Delay sending the ack or 'renew lease' to prevent timeout for messages with 
     * long processing time
     * @param duration
     * @param units
     * @return 
     */
//    public ListenableFuture<Boolean> renew(long duration, TimeUnit units);
}
