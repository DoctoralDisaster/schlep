package com.netflix.schlep;

/**
 * Return a reference to a MessageConsumer that has a registerd callback method.
 * 
 * @author elandau
 *
 * @param <T>
 */
public interface MessageConsumer<T> {
    /**
     * Pause the consumer so that the callback is no longer called.
     */
    public void pause() throws Exception;
    
    /**
     * Resume a consumer that has been paused.  No-op if consume is not currently paused
     */
    public void resume() throws Exception;

    /**
     * Start any consumer threads and begin consuming messages
     * 
     * @throws Exception
     */
    public void start() throws Exception;
    
    /**
     * Stop consuming messages.  Empties out any pending messages in the queue.
     * 
     * @throws Exception
     */
    public void stop() throws Exception;
}
