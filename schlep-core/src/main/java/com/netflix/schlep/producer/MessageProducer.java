package com.netflix.schlep.producer;

import java.util.List;
import java.util.Map;

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
    
    /**
     * Produce a batch of messages.  The producer should attempt to send the batch as a 
     * single unit and avoid any internal batching.  Call this API when you, the caller, 
     * are doing your own batching.
     * @param messages
     */
    public Map<OutgoingMessage<T>, ListenableFuture<Boolean>> produceBatch(List<OutgoingMessage<T>> messages);
    
    /**
     * @return Return a unique producer name.  The producer name has to do more 
     *         with naming the configuration rather than the message target specifics.
     */
    public String getId();
    
    /**
     * @return Return a URI identifying the message target. 
     */
    public String getUri();
    
    /**
     * @return Type of message sent through this producer
     */
    public Class<T> getMessageType();
}
