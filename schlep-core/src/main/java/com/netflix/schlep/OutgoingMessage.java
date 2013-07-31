package com.netflix.schlep;

/**
 * Encapsulate an outgoing message context
 * 
 * @author elandau
 *
 */
public class OutgoingMessage<T> {
    private T message;
    
    /**
     * Set the message content for the output message
     * @param message
     * @return
     */
    public OutgoingMessage<T> withMessage(T message) {
        this.message = message;
        return this;
    }
    
    /**
     * Get the message contained in the outgoing message
     * @return
     */
    public T getMessage() {
        return this.message;
    }
}
