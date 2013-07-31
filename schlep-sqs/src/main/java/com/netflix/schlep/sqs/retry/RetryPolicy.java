package com.netflix.schlep.sqs.retry;

import java.util.concurrent.Callable;

/**
 * Abstraction for a *blocking* retry policy.  
 * 
 * @author elandau
 */
public interface RetryPolicy {
    /**
     * Wrap a callable with retry logic.  Calling call() on the provided callable
     * will execute the original callable with retries.  The retry policy will
     * attempt to retry on any exception, except for NotRetryableException which 
     * should be thrown by the original callback to force exiting the retry loop.
     * 
     * @param callable
     * @return
     */
    public <R> Callable<R> wrap(Callable<R> callble);
}
