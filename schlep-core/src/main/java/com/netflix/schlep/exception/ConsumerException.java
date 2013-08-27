package com.netflix.schlep.exception;

/**
 * Base class for all message consume exceptions
 * @author elandau
 *
 */
public class ConsumerException extends MessagingException {

    public ConsumerException(Exception e) {
        super(e);
    }
    
    public ConsumerException(String msg, Exception e) {
        super(msg, e);
    }

    public ConsumerException(String msg) {
        super(msg);
    }

}
