package com.netflix.schlep.sqs;

import java.util.concurrent.TimeUnit;

import com.amazonaws.services.sqs.model.Message;
import com.netflix.schlep.IncomingMessage;
import com.netflix.schlep.util.UnstoppableStopwatch;

/**
 * Wrapper for an SQS message and associates the message with an ack strategy
 * 
 * @author elandau
 *
 */
public abstract class SqsIncomingMessage<T> implements IncomingMessage<T> {
    private final Message        message;
    private final T              entity;
    private final UnstoppableStopwatch sw;
    private long  visibilityTimeoutInSeconds;
    
    public SqsIncomingMessage(Message message, T entity, UnstoppableStopwatch sw, long visibilityTimeoutInSeconds) {
        this.message     = message;
        this.entity      = entity;
        this.sw          = sw;
        this.visibilityTimeoutInSeconds = visibilityTimeoutInSeconds;
    }
    
    @Override
    public long getTimeSinceReceived(TimeUnit units) {
        return sw.elapsed(units);
    }
    
    @Override
    public T getEntity() {
        return entity;
    }
    
    public Message getMessage() {
        return message;
    }
    
    void extendVisibilityTimeout(long amount, TimeUnit units) {
        this.visibilityTimeoutInSeconds += TimeUnit.SECONDS.convert(amount, units);
    }
    
    long getVisibilityTimeout(TimeUnit units) {
        return units.convert(this.visibilityTimeoutInSeconds, TimeUnit.SECONDS);
    }
}
