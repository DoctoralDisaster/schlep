package com.netflix.schlep.sqs.consumer;

import java.util.concurrent.TimeUnit;

import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.sqs.SqsMessage;
import com.netflix.schlep.util.UnstoppableStopwatch;

/**
 * Wrapper for an SQS message and associates the message with an ack strategy
 * 
 * @author elandau
 *
 */
public abstract class SqsIncomingMessage implements IncomingMessage {
    private final SqsMessage                message;
    private final UnstoppableStopwatch      sw;
    private long  visibilityTimeoutInSeconds ;
    
    public SqsIncomingMessage(SqsMessage message, UnstoppableStopwatch sw, long visibilityTimeoutInSeconds) {
        this.message     = message;
        this.sw          = sw;
        this.visibilityTimeoutInSeconds = visibilityTimeoutInSeconds;
    }
    
    @Override
    public long getTimeSinceReceived(TimeUnit units) {
        return sw.elapsed(units);
    }
    
    public SqsMessage getMessage() {
        return message;
    }
    
    void extendVisibilityTimeout(long amount, TimeUnit units) {
        this.visibilityTimeoutInSeconds += TimeUnit.SECONDS.convert(amount, units);
    }
    
    long getVisibilityTimeout(TimeUnit units) {
        return units.convert(this.visibilityTimeoutInSeconds, TimeUnit.SECONDS);
    }
}
