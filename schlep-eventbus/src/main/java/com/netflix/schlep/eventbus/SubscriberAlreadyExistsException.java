package com.netflix.schlep.eventbus;

public class SubscriberAlreadyExistsException extends Exception {
    public SubscriberAlreadyExistsException(Exception e) {
        super(e);
    }
    public SubscriberAlreadyExistsException(String msg) {
        super(msg);
    }
    public SubscriberAlreadyExistsException(String msg, Exception e) {
        super(msg, e);
    }
}
