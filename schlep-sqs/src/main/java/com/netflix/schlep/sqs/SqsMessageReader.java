package com.netflix.schlep.sqs;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observer;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.subjects.PublishSubject;
import rx.util.functions.Action1;

import com.amazonaws.services.sqs.model.Message;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.consumer.MessageConsumer;
import com.netflix.schlep.sim.SimMessageConsumer;
import com.netflix.schlep.util.UnstoppableStopwatch;

/**
 * MessageReader that reads messages from SQS.  On each subscription the reader will 
 * spawn a new thread so that every observer receives a different set of threads.
 * However, acks and renew from all threads will go over the same ack and batch 
 * thread.
 * @author elandau
 *
 */
public class SqsMessageReader implements MessageConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(SimMessageConsumer.class);
    
    private final String                 id;
    private final SqsClient              client;
    private final SqsClientConfiguration clientConfig;
    
    private final AtomicLong    poolId  = new AtomicLong();
    private final AtomicBoolean paused  = new AtomicBoolean(false);
    
    private PublishSubject<IncomingMessage> ackSubject;
    
    private PublishSubject<IncomingMessage> renewSubject;

    public SqsMessageReader(String id, SqsClient client, SqsClientConfiguration config) {
        this.clientConfig   = config;
        this.client         = client;
        this.id             = id;
        
        ackSubject = PublishSubject.create();
        ackSubject
            .buffer(10, TimeUnit.SECONDS, 1)
            .observeOn(Schedulers.newThread())  // TODO: Policy
            .subscribe(new Action1<List<IncomingMessage>>() {
                @Override
                public void call(List<IncomingMessage> message) {
                }
            });
        
        renewSubject = PublishSubject.create();
        renewSubject
            .buffer(10, TimeUnit.SECONDS, 1)
            .observeOn(Schedulers.newThread())  // TODO: Policy
            .subscribe(new Action1<List<IncomingMessage>>() {
                @Override
                public void call(List<IncomingMessage> message) {
                }
            });
    }
    
    @Override
    public Subscription call(final Observer<IncomingMessage> observer) {
        final AtomicBoolean done = new AtomicBoolean(false);
        final ExecutorService executor  = Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder()
                .setNameFormat("SqsMessageReader-" + poolId.incrementAndGet() + "-" + getId() + "-%d")
                .build());

        executor.submit(new Runnable() {
            @Override
            public void run() {
                while (!done.get()) {
                    if (paused.get()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
                    
                    try {
                        long timeout = clientConfig.getVisibilityTimeoutSeconds();
                        Collection<Message> result = client.receiveMessages(clientConfig.getMaxReadBatchSize(), timeout, null);
                        final UnstoppableStopwatch sw = new UnstoppableStopwatch();
                        for (final Message message : result) {
                            observer.onNext(new IncomingMessage() {
                                @Override
                                public void ack() {
                                    ackSubject.onNext(this);
                                }
                                @Override
                                public void nak() {
                                    // Drop on the floor until SQS supports NAK
                                }
                                @Override
                                public <T> T getContents(Class<T> clazz) {
                                    // TODO: Plug into serializer
                                    return null;
                                }
                                @Override
                                public long getTimeSinceReceived(TimeUnit units) {
                                    return sw.elapsed(units);
                                }
                                public String toString() {
                                    return "SQS[" + StringUtils.abbreviate(message.getBody(), 32) + "]";
                                }
                            });
                        }
                    } catch (Exception e) {
                        LOG.error("Interrupted", e);
                        observer.onError(e);
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
                observer.onCompleted();
                executor.shutdownNow();
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
    }

}
