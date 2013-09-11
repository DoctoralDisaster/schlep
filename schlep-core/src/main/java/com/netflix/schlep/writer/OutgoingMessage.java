package com.netflix.schlep.writer;

/**
 * Encapsulate an outgoing message context
 * 
 * @author elandau
 *
 */
public class OutgoingMessage {
    public static class Builder {
        private Object message;
        private String uniqueId;
        
        /**
         * Assign an optional uniqueId.  If supported the messaging solution will
         * fail sending the message if uniquness constraint fails.
         * @param id
         * @return
         */
        public Builder withUniqueId(String id) {
            this.uniqueId = id;
            return this;
        }
        
        /**
         * Set the message content for the output message
         * @param message
         * @return
         */
        public Builder withMessage(Object message) {
            this.message = message;
            return this;
        }
        
        public OutgoingMessage build() {
            return new OutgoingMessage(this);
        }
    }
    
    public static  Builder builder() {
        return new Builder();
    }
    
    private OutgoingMessage(Builder builder) {
        this.message  = builder.message;
        this.uniqueId = builder.uniqueId;
    }
    
    /**
     * Entity to be persisted as the message body
     */
    private final Object message;
    
    /**
     * Optional unique id to prevent duplicate messages from being produced
     */
    private final String uniqueId;
    
    /**
     * Get the message contained in the outgoing message
     * @return
     */
    public Object getMessage() {
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
