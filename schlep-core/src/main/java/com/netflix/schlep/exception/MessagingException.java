package com.netflix.schlep.exception;

public class MessagingException extends Exception {
    public MessagingException(String message) {
        super(message);
    }
    
    public MessagingException(String message, Exception e) {
        super(message, e);
    }
}
