package com.netflix.schlep;

import com.netflix.schlep.exception.ConsumerException;

/**
 * Callback for incoming messages from an endpoint.  The MessageCallback interface decouples the 
 * concrete message consumer and dispatcher such that the callback implementation need not know
 * anything about threading models, timeouts and other implementation details.
 * 
 * @author elandau
 *
 */
public interface MessageCallback<T> {
    /**
     * @param message
     */
    public void consume(IncomingMessage<T> message) throws ConsumerException;
}
