package com.netflix.schlep;

/**
 * Abstraction of the scheduling logic for a scheduled message.  The logic
 * could be as simple as a delay or a complex cron syntax.
 * 
 * @author elandau
 */
public interface Trigger {
    /**
     * Get the consume time specified by this trigger to be used when 
     * producing the associated message.  
     * 
     * @return Consume time as indicated by this trigger.
     */
    public long getConsumeTime();
    
    /**
     * @return Return true if this trigger indicates a repeatable schedule
     */
    public boolean isRepeatable();
    
    /**
     * Calculate the next time the message is to be executed after consuming
     * a message.  
     * 
     * This method is actually optional and may only be used by clients that 
     * manage the schedule themselves by producing the next message.
     * 
     * @return 
     */
    public Trigger generateNextScheduleTrigger();
}
