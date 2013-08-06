package com.netflix.schlep;

/**
 * Encapsulate an outgoing message context
 * 
 * @author elandau
 *
 */
public class OutgoingMessage<T> {
    /**
     * Entity to be persisted as the message body
     */
    private T message;
    
    /**
     * Optional unique id to prevent duplicate messages from being produced
     */
    private String uniqueId;
    
    /**
     * Assign an optional uniqueId.  If supported the messaging solution will
     * fail sending the message if uniquness constraint fails.
     * @param id
     * @return
     */
    public OutgoingMessage<T> withUniqueId(String id) {
        this.uniqueId = id;
        return this;
    }
    
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
    
    /**
     * @return  True if the message has a unique id
     */
    public boolean hasUniqueId() {
        return this.uniqueId != null;
    }
    
    /**
     * @return Optional uniqueId if one was specified
     */
    public String getUniqueId() {
        return this.uniqueId;
    }
}