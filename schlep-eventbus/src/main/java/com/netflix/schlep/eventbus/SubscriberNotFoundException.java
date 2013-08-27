package com.netflix.schlep.eventbus;

public class SubscriberNotFoundException extends Exception {
    public SubscriberNotFoundException(Exception e) {
        super(e);
    }
    public SubscriberNotFoundException(String msg) {
        super(msg);
    }
    public SubscriberNotFoundException(String msg, Exception e) {
        super(msg, e);
    }
}
