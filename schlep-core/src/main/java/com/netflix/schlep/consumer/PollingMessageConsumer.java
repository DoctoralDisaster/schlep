package com.netflix.schlep.consumer;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.schlep.processor.MessageHandler;
import com.netflix.schlep.writer.Completion;
import com.netflix.schlep.writer.Completion.SelectFirst;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

/**
 * Base implementation of a PollingMessageConsumer.  Once started this consumer will
 * read messages from the protocol layer and pass on to 0 or more subscribed observers.
 * Each observer will receive all messages.   
 * 
 * TODO: Flag to determine whether to process messages if there are no subscription
 * 
 * @author elandau
 */
public abstract class PollingMessageConsumer implements MessageConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(PollingMessageConsumer.class);
    
    // Defaults
    private static final int DEFAULT_THREAD_COUNT     = 1;
    private static final int DEFAULT_THROTTLE_MSEC    = 1;
    private static final int DEFAULT_BATCH_SIZE       = 10;
    private static final int DEFAULT_ACK_BATCH_MSEC   = 1000;
    private static final int DEFAULT_MAX_BACKLOG      = 100;
    
    // Configuration
    private final String id;
    private final int    threadCount;
    private final long   throttleMsec;
    private final int    batchSize;
    private final long   ackMsec;
    private final int    maxBacklog;
    
    // State
    private final AtomicBoolean             paused   = new AtomicBoolean();
    private ScheduledExecutorService        executor;
    private PublishSubject<IncomingMessage> subject = PublishSubject.create();
    private Subscription                    subscription;
    private final AtomicLong                busyCount = new AtomicLong(0);
    private final AtomicLong                messagesRead = new AtomicLong(0);
    private final AtomicLong                messagesAcked = new AtomicLong(0);

    private final ConcurrentMap<Subscription, MessageHandler> handlers = Maps.newConcurrentMap();
    
    /**
     * Builder
     * @param <T>
     */
    public static abstract class Builder<T extends Builder<T>> {
        private String  id;
        private int     threadCount  = DEFAULT_THREAD_COUNT;
        private long    throttleMsec = DEFAULT_THROTTLE_MSEC;
        private int     batchSize    = DEFAULT_BATCH_SIZE;
        private long    ackMsec      = DEFAULT_ACK_BATCH_MSEC;
        private int     maxBacklog   = DEFAULT_MAX_BACKLOG;
        
        protected abstract T self();
        
        public T withId(String id) {
            this.id = id;
            return self();
        }
        
        public T withThreadCount(int threadCount) {
            this.threadCount = threadCount;
            return self();
        }
        
        public T withThrottle(long amount, TimeUnit units) {
            this.throttleMsec = units.toMillis(amount);
            return self();
        }
        
        public T withThrottle(long msec) {
            return withThrottle(msec, TimeUnit.MILLISECONDS);
        }
        
        public T withAckDelay(long msec) {
            this.ackMsec = msec;
            return self();
        }
        
        public T withBatchSize(int size) {
            this.batchSize = size;
            return self();
        }
        
        public T withMaxBacklog(int size) {
            this.maxBacklog = size;
            return self();
        }
    }
    
    protected PollingMessageConsumer(Builder<?> builder) {
        this.id           = builder.id;
        this.threadCount  = builder.threadCount;
        this.throttleMsec = builder.throttleMsec;
        this.batchSize    = builder.batchSize;
        this.ackMsec      = builder.ackMsec;
        this.maxBacklog   = builder.maxBacklog;
    }
    
    /**
     * Read a single batch from the subclass protocol implementation
     * @param batchSize
     * @return
     */
    protected abstract List<IncomingMessage> readBatch(int batchSize);
    
    /**
     * Send an ACK batch response.  Can be a no-op for non-acking protocols.
     * @param act
     */
    protected abstract void sendAckBatch(List<Completion<IncomingMessage>> act);
    
    @Override
    public synchronized void start() throws Exception {
        if (isStarted()) {
            LOG.warn(String.format("Consumer '%s' already started", getId()));
            return;
        }
        
        executor = Executors.newScheduledThreadPool(threadCount,
                new ThreadFactoryBuilder()
                .setNameFormat("Consumer-" + getId() + "-%d")
                .build());
        
        subscription = subject
            // Fan out to all subscribers and consolidate into a single Completion 
            .mapMany(new Func1<IncomingMessage, Observable<Completion<IncomingMessage>>>() {
                @Override
                public Observable<Completion<IncomingMessage>> call(final IncomingMessage message) {
                    // Track some counts
                    busyCount.incrementAndGet();
                    messagesRead.incrementAndGet();
                    
                    // Construct a list of Completions
                    List<Observable<Completion<IncomingMessage>>> completions = Lists.newArrayList();
                    for (MessageHandler processor : handlers.values()) {
                        try {
                            // encapsulate the work needed for each filter
                            Observable<Completion<IncomingMessage>> ob = processor.call(message);
                            
                            // add a default ACK if the filter throws an exception
                            ob = ob.onErrorReturn(new Func1<Throwable, Completion<IncomingMessage>>() {
                                @Override
                                public Completion<IncomingMessage> call(Throwable t) {
                                    return Completion.from(message);
                                }
                            });
                            
                            // add to the list of work to do
                            completions.add(ob);
                        }
                        catch (RuntimeException e) {
                            LOG.warn(e.getMessage(), e);
                        }
                    }
                    
                    if (completions.isEmpty()) {
                        return Observable.just(Completion.from(message));
                    }
                    
                    // execute all of the filters in parallel
                    return Observable
                        .merge(completions)
                        .reduce(SelectFirst.get());
                }                
            })
            // After all subscribers have finishing processing, send the ack
            .buffer(ackMsec, TimeUnit.MILLISECONDS, batchSize)
            .subscribe(new Action1<List<Completion<IncomingMessage>>>() {
                @Override
                public void call(final List<Completion<IncomingMessage>> act) {
                    if (!act.isEmpty()) {
                        // TODO: Can we use subscribeOn or observeOn instead?
                        executor.submit(new Runnable() {
                            @Override
                            public void run() {
                                sendAckBatch(act);
                            }
                        });
                        
                        busyCount.addAndGet(-act.size());
                        messagesAcked.incrementAndGet();
                    }
                }
            });
        
        for (int i = 0; i < threadCount; i++) {
            executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (paused.get() || busyCount.get() > maxBacklog) {
                        return;
                    }
                    
                    try {
                        List<IncomingMessage> messages = readBatch(batchSize);
                        for (IncomingMessage message : messages) {
                            try {
                                subject.onNext(message);
                            }
                            catch (Exception e) {
                                LOG.error("Failed to process message", e);
                            }
                        }
                    }
                    catch (Exception e) {
                        LOG.error("Error executing consumer", e);
                    }
                    
                }
            }, 0, this.throttleMsec, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Stop all reader and ack threads.
     */
    @Override
    public synchronized void stop() throws Exception {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    /**
     * Pause consuming new messages.  Messages already in process will be processed to 
     * completion.
     */
    @Override
    public void pause() throws Exception {
        paused.set(true);
    }

    /**
     * Resume consuming messages
     */
    @Override
    public void resume() throws Exception {
        paused.set(false);
    }
    
    public boolean isStarted() {
        return this.executor != null;
    }
    
    @Override
    public String getId() {
        return this.id;
    }
    
    /**
     * Add a subscription in the form of a message handler.  All subscriptions 
     * will receive a copy of each message.
     */
    @Override
    public Subscription subscribe(final MessageHandler handler) {
        Subscription sub = new Subscription() {
            @Override
            public void unsubscribe() {
                handlers.remove(this);
            }
        };
        
        handlers.put(sub, handler);
        return sub;
    }
    
    /**
     * Add a subscription in the form of a message handler.  All subscriptions 
     * will receive a copy of each message.
     */
    @Override
    public Subscription subscribe(final Function<IncomingMessage, Boolean> handler) {
        return subscribe(new MessageHandler() {
            @Override
            public Observable<Completion<IncomingMessage>> call(IncomingMessage message) {
                handler.apply(message);
                return Observable.from(Completion.from(message));
            }
        });
    }
}
