package com.netflix.schlep;

public class ScheduledOutgoingMessage<T> {
    /**
     * Entity to be persisted as the message body
     */
    private T message;
    
    /**
     * Trigger representing the scheduling logic
     */
    private Trigger trigger;
    
    /**
     * Optional unique id to prevent duplicate messages from being produced
     */
    private String uniqueId;
    
    public ScheduledOutgoingMessage<T> withUniqueId(String id) {
        this.uniqueId = id;
        return this;
    }
    
    /**
     * Set the message content for the output message
     * @param message
     * @return
     */
    public ScheduledOutgoingMessage<T> withMessage(T message) {
        this.message = message;
        return this;
    }
    
    /**
     * Set the schedule logic associated with this message
     * @param trigger
     * @return
     */
    public ScheduledOutgoingMessage<T> withTrigger(Trigger trigger) {
        this.trigger = trigger;
        return this;
    }
    
    public boolean hasUniqueId() {
        return this.uniqueId != null;
    }
    
    public String getUniqueId() {
        return this.uniqueId;
    }
    
    /**
     * Get the message contained in the outgoing message
     * @return
     */
    public T getMessage() {
        return this.message;
    }
    
    public Trigger getTrigger() {
        return this.trigger;
    }
}
