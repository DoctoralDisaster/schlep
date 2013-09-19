package com.netflix.schlep.sim;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.schlep.reader.AbstractIncomingMessage;
import com.netflix.schlep.reader.IncomingMessage;
import com.netflix.schlep.reader.MessageReader;

import rx.Observer;
import rx.Subscription;

/**
 * Simulates a message consumer by generating messages on a given interval
 * 
 * @author elandau
 *
 */
public class SimMessageReader implements MessageReader {
    private static final Logger LOG = LoggerFactory.getLogger(SimMessageReader.class);
    
    private final String    id;
    private final int       batchSize;
    private final int       count;
    private final long      interval;
    private final TimeUnit  intervalUnits;
    
    private final AtomicLong    poolId     = new AtomicLong();
    private final AtomicLong    counter    = new AtomicLong();
    private final AtomicLong    ackCounter = new AtomicLong();
    private final AtomicBoolean paused     = new AtomicBoolean(false);
    
    public static class Builder {
        private String    id            = "sim";
        private int       batchSize     = 10;
        private int       count         = 100;
        private long      interval      = 100;
        private TimeUnit  intervalUnits = TimeUnit.MILLISECONDS;
        
        public Builder withId(String id) {
            this.id = id;
            return this;
        }
        
        public Builder withBatchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }
        
        public Builder withMaxCount(int count) {
            this.count = count;
            return this;
        }
        
        public Builder withInterval(long interval, TimeUnit units) {
            this.interval = interval;
            this.intervalUnits = units;
            return this;
        }
        
        public SimMessageReader build() {
            return new SimMessageReader(this);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public SimMessageReader(Builder builder) {
        this.id             = builder.id;
        this.batchSize      = builder.batchSize;
        this.count          = builder.count;
        this.interval       = builder.interval;
        this.intervalUnits  = builder.intervalUnits;
    }
    
    @Override
    public Subscription call(final Observer<IncomingMessage> observer) {
        final AtomicBoolean done = new AtomicBoolean(false);
        final ExecutorService executor  = Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder()
                .setNameFormat("SimMessageReader-" + poolId.incrementAndGet() + "-" + getId() + "-%d")
                .build());

        executor.submit(new Runnable() {
            @Override
            public void run() {
                while (!done.get()) {
                    try {
                        if (!paused.get()) {
                            for (int i = 0; i < batchSize; i++) {
                                long id = counter.incrementAndGet();
                                if (id > count) {
                                    counter.decrementAndGet();
                                    return;
                                }
                                    
                                observer.onNext(new AbstractIncomingMessage<String>(id + "-" + id) {
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
                        }
                        
                        Thread.sleep(intervalUnits.toMillis(interval));
                    } catch (Exception e) {
                        LOG.error("Interrupted", e);
//                        observer.onError(e);    // Not sure we actually want to do this since it'll stop all consumers.
                        done.set(true);
                        return;
                    }
                }
            }
        });
        
        return new Subscription() {
            @Override
            public void unsubscribe() {
                done.set(true);
            }
        };
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public void pause() throws Exception {
        this.paused.set(true);
    }

    @Override
    public void resume() throws Exception {
        this.paused.set(false);
    }
}
