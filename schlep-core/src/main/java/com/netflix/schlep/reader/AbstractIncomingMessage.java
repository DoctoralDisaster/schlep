package com.netflix.schlep.reader;

import java.util.concurrent.TimeUnit;

import com.netflix.schlep.util.UnstoppableStopwatch;

public abstract class AbstractIncomingMessage<T> implements IncomingMessage  {
    protected final T                     entity;
    private   final UnstoppableStopwatch  sw;
    
    public AbstractIncomingMessage(T entity) {
        this.entity  = entity;
        this.sw      = new UnstoppableStopwatch();
    }
    
    @Override
    public long getTimeSinceReceived(TimeUnit units) {
        return sw.elapsed(units);
    }
}