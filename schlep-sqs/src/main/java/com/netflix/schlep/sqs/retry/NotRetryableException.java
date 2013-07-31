package com.netflix.schlep.sqs.retry;

/**
 * Exception expected to be thrown by a callable to force a retry policy 
 * to exit the retry loop.
 * @author elandau
 */
public class NotRetryableException extends Exception {
    private Exception cause;
    
    public NotRetryableException(Exception cause) {
        this.cause = cause;
    }
    
    public Exception getCause() {
        return cause;
    }
}
