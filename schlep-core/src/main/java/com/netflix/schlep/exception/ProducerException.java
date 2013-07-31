package com.netflix.schlep.exception;

public class ProducerException extends Exception {
    public ProducerException(String msg, Exception e) {
        super(msg, e);
    }

    public ProducerException(String msg) {
        super(msg);
    }
}
