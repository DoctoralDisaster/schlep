package com.netflix.schlep.producer;

/**
 * Encapsulate an outgoing message context
 * 
 * @author elandau
 *
 */
public class OutgoingMessage<T> {
    public static class Builder<T> {
        private T message;
        private String uniqueId;
        
        /**
         * Assign an optional uniqueId.  If supported the messaging solution will
         * fail sending the message if uniquness constraint fails.
         * @param id
         * @return
         */
        public Builder<T> withUniqueId(String id) {
            this.uniqueId = id;
            return this;
        }
        
        /**
         * Set the message content for the output message
         * @param message
         * @return
         */
        public Builder<T> withMessage(T message) {
            this.message = message;
            return this;
        }
        
        public OutgoingMessage<T> build() {
            return new OutgoingMessage<T>(this);
        }
    }
    
    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }
    
    private OutgoingMessage(Builder<T> builder) {
        this.message  = builder.message;
        this.uniqueId = builder.uniqueId;
    }
    
    /**
     * Entity to be persisted as the message body
     */
    private final T message;
    
    /**
     * Optional unique id to prevent duplicate messages from being produced
     */
    private final String uniqueId;
    
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
