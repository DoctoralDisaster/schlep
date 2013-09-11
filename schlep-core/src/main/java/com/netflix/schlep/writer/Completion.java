package com.netflix.schlep.writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Completion<T> {
    private final static Logger LOG = LoggerFactory.getLogger(Completion.class);
    
    private final T value;

    public Completion(T value) {
        this.value = value;
    }

    public String toString() {
        return new StringBuilder().append("Completion[").append(value).append("]").toString();
    }
    
    public T getValue() {
        return value;
    }
    
    public static <T> Completion<T> from(T message) {
        return new Completion<T>(message);
    }
}
