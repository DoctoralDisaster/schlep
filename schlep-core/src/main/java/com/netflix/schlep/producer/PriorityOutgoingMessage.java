package com.netflix.schlep.producer;

public class PriorityOutgoingMessage<T> {
    /**
     * Entity to be persisted as the message body
     */
    private T message;
    
    /**
     * Optional unique id to prevent duplicate messages from being produced
     */
    private String uniqueId;
    
    /**
     * Priority used for ordering messages.
     * TODO: Consider making this generic using byte[]
     */
    private long priority = 0;
    
    /**
     * Priority value used for sorting messages
     * @param priority
     * @return
     */
    public PriorityOutgoingMessage<T> withPriority(long priority) {
        this.priority = priority;
        return this;
    }
    
    /**
     * Assign an optional uniqueId.  If supported the messaging solution will
     * fail sending the message if uniquness constraint fails.
     * @param id
     * @return
     */
    public PriorityOutgoingMessage<T> withUniqueId(String id) {
        this.uniqueId = id;
        return this;
    }
    
    /**
     * Set the message content for the output message
     * @param message
     * @return
     */
    public PriorityOutgoingMessage<T> withMessage(T message) {
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
    
    public long getPriority() {
        return this.priority;
    }
}
