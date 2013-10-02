package com.netflix.schlep.sim;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.netflix.schlep.consumer.AbstractIncomingMessage;
import com.netflix.schlep.consumer.PollingMessageConsumer;
import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.writer.Completion;

/**
 * Simulates a message consumer by generating messages on a given interval
 * 
 * @author elandau
 *
 */
public class SimMessageConsumer extends PollingMessageConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(SimMessageConsumer.class);
    
    private final AtomicLong    counter    = new AtomicLong();
    private final AtomicLong    ackCounter = new AtomicLong();
    
    private final int itemsToSend;
    
    public static abstract class Builder<T extends Builder<T>> extends PollingMessageConsumer.Builder<T> {
        private int       itemsToSend         = 100;
        
        public T withMaxCount(int count) {
            this.itemsToSend = count;
            return self();
        }
        
        public SimMessageConsumer build() {
            return new SimMessageConsumer(this);
        }
        
    }

    private static class BuilderWrapper extends Builder<BuilderWrapper> {
        @Override
        protected BuilderWrapper self() {
            return this;
        }
    }
    
    public static Builder<?> builder() {
        return new BuilderWrapper();
    }
    
    protected SimMessageConsumer(Builder<?> init) {
        super(init);
        this.itemsToSend          = init.itemsToSend;
    }
    
    @Override
    protected List<IncomingMessage> readBatch(int batchSize) {
        List<IncomingMessage> messages = Lists.newArrayListWithCapacity(batchSize);
        for (int i = 0; i < batchSize; i++) {
            long count = counter.incrementAndGet();
            if (count >= itemsToSend)
                break;
            
            messages.add(new AbstractIncomingMessage<String>(getId() + "-" + count) {
                @Override
                public void ack() {
                    ackCounter.incrementAndGet();
                    LOG.info("Ack: " + getContents(String.class) + " busy=" + (counter.get() - ackCounter.get()));
                }
                @Override
                public void nak() {
                    ackCounter.incrementAndGet();
                    LOG.info("Nak: " + getContents(String.class));
                }
                
                public String toString() {
                    return "Sim[" + StringUtils.abbreviate(this.getContents(String.class), 32) + "]";
                }
                @Override
                public <T> T getContents(Class<T> clazz) {
                    Preconditions.checkArgument(clazz.equals(String.class), "Only string type allowed");
                    return (T) entity;
                }
            });
        }
        return messages;
    }

    @Override
    protected void sendAckBatch(List<Completion<IncomingMessage>> act) {
        LOG.info("Ack: " + act);
    }

}
