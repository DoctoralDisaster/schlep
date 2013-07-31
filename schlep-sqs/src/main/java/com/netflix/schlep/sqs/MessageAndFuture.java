package com.netflix.schlep.sqs;

import com.amazonaws.services.sqs.model.Message;
import com.google.common.util.concurrent.SettableFuture;

public class MessageAndFuture<V> {
    private final Message           message;
    private final SettableFuture<V> future;
    private final long              visibilityTimeout;
    
    public MessageAndFuture(Message message, SettableFuture<V> future) {
        super();
        this.message = message;
        this.future = future;
        this.visibilityTimeout = 0;
    }

    public MessageAndFuture(Message message, SettableFuture<V> future, long visibilityTimeout) {
        super();
        this.message = message;
        this.future = future;
        this.visibilityTimeout = visibilityTimeout;
    }

    public Message getMessage() {
        return this.message;
    }

    public SettableFuture<V> getFuture() {
        return this.future;
    }
    
    public long getVisibilityTimeout() {
        return this.visibilityTimeout;
    }
    
    
}
