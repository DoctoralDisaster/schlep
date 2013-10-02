package com.netflix.schlep.writer;

import rx.util.functions.Func2;

import com.netflix.schlep.consumer.IncomingMessage;

public class Completion<T> {
    private final T value;

    public static class SelectFirst implements Func2<Completion<IncomingMessage>, Completion<IncomingMessage>, Completion<IncomingMessage>> {
        @Override
        public Completion<IncomingMessage> call(Completion<IncomingMessage> reply1, Completion<IncomingMessage> reply2) {
            return reply1;
        }
        
        public static SelectFirst instance = new SelectFirst();
        public static SelectFirst get() {
            return instance;
        }
    }


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
