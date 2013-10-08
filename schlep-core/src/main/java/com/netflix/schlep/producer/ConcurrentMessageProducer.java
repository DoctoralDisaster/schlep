package com.netflix.schlep.producer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action1;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.schlep.Completion;

public abstract class ConcurrentMessageProducer implements MessageProducer {
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentMessageProducer.class);
    
    // Defaults
    private static final int DEFAULT_THREAD_COUNT     = 1;
    private static final int DEFAULT_BATCH_SIZE       = 10;
    private static final int DEFAULT_BUFFER_DELAY     = 1000;
    
    // Configuration
    private final String id;
    private final int    threadCount;
    private final int    batchSize;
    private final long   bufferDelay;
    
    // State
    private final AtomicLong                busyCount = new AtomicLong(0);
    
    private PublishSubject<ObservableCompletion>          subject = PublishSubject.create();
    private Subscription                    subscription;
    private ExecutorService                 executor;

    public static class ObservableCompletion extends Completion<OutgoingMessage> {
        private Observer<? super Completion<OutgoingMessage>> observer;
        
        public ObservableCompletion(OutgoingMessage message, Observer<? super Completion<OutgoingMessage>> observer) {
            super(message);
            this.observer = observer;
        }
        
        public void done() {
            this.observer.onNext(this);
            this.observer.onCompleted();
        }
    }
    
    /**
     * Builder
     * @param <T>
     */
    public static abstract class Builder<T extends Builder<T>> {
        private String   id;
        private int      threadCount        = DEFAULT_THREAD_COUNT;
        private int      batchSize          = DEFAULT_BATCH_SIZE;
        private long     bufferDelay        = DEFAULT_BUFFER_DELAY;
        
        protected abstract T self();
        
        public T withId(String id) {
            this.id = id;
            return self();
        }
        
        public T withThreadCount(int threadCount) {
            this.threadCount = threadCount;
            return self();
        }
        
        public T withBufferDelay(long amount) {
            this.bufferDelay = amount;
            return self();
        }
        
        public T withBatchSize(int size) {
            this.batchSize = size;
            return self();
        }
    }
    
    private final AtomicLong    sendAttempt = new AtomicLong();

    protected ConcurrentMessageProducer(Builder<?> init) {
        this.batchSize   = init.batchSize;
        this.threadCount = init.threadCount;
        this.id          = init.id;
        this.bufferDelay = init.bufferDelay;
    }
    
    @Override
    public synchronized void start() throws Exception {
        if (isStarted()) {
            LOG.warn(String.format("Producer '%s' already started", getId()));
            return;
        }
        
        LOG.info(String.format("Starting producer '%s'", getId()));
        
        subject = PublishSubject.create();

        // Consider making this a cache thread pool so we can adjust the number of threads at runtime
        executor = Executors.newScheduledThreadPool(threadCount,
                new ThreadFactoryBuilder()
                .setNameFormat("Producer-" + getId() + "-%d")
                .setDaemon(true)
                .build());

        subscription = subject
            .buffer(bufferDelay, TimeUnit.MILLISECONDS, batchSize)
            .observeOn(Schedulers.executor(executor))
            .subscribe(new Action1<List<ObservableCompletion>>() {
                @Override
                public void call(List<ObservableCompletion> messages) {
                    if (messages.size() > 0) 
                        sendMessages(messages);
                }
            });
    }

    @Override
    public synchronized void stop() throws Exception {
        LOG.info(String.format("Stopping producer '%s'", getId()));
        
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void pause() throws Exception {
        // NOOP
    }

    @Override
    public void resume() throws Exception {
        // NOOP
    }
    
    @Override
    public String getId() {
        return id;
    }

    /**
     * Write a message and notify completion on the provided observer
     * 
     * @param message
     * @param observer
     */
    @Override
    public void send(OutgoingMessage message, Observer<Completion<OutgoingMessage>> observer) {
        sendAttempt.incrementAndGet();
        subject.onNext(new ObservableCompletion(message, observer));
    }

    /**
     * Write a message and return an observable on which a Completion will be emitted
     * once the message is written successfully
     * @param message
     * @return
     */
    @Override
    public Observable<Completion<OutgoingMessage>> send(final OutgoingMessage message) {
        sendAttempt.incrementAndGet();
        return Observable.create(new OnSubscribeFunc<Completion<OutgoingMessage>>() {
            @Override
            public Subscription onSubscribe(Observer<? super Completion<OutgoingMessage>> observer) {
                subject.onNext(new ObservableCompletion(message, observer));
                return Subscriptions.empty();
            }
        });
    }

    protected abstract void sendMessages(List<ObservableCompletion> messages);

    @Override
    public boolean isStarted() {
        return executor != null;
    }
    

}
