package com.netflix.schlep.sim;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netflix.schlep.writer.MessageWriter;
import com.netflix.schlep.writer.Completion;
import com.netflix.schlep.writer.OutgoingMessage;

/**
 * Simulated message writer
 * 
 * @author elandau
 *
 */
public class SimMessageWriter implements MessageWriter {
    private static final Logger LOG = LoggerFactory.getLogger(SimMessageWriter.class);
    
    public static class Holder {
        OutgoingMessage                        message;
        Observer<Completion<OutgoingMessage>>  observer;
        
        public Holder(OutgoingMessage message, Observer<Completion<OutgoingMessage>> observer) {
            this.message  = message;
            this.observer = observer;
        }
    }
    
    public static class Builder {
        private int      bufferSize         = 3;
        private long     bufferDelay        = 3;
        private TimeUnit bufferDelayUnits   = TimeUnit.SECONDS;
        private long     writeDelay         = 100;
        private String   id                 = "SimWriter";
        private int      writerThreads      = 1;
        
        public Builder withBatchSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }
        
        public Builder withBufferDelay(long bufferDelay, TimeUnit units) {
            this.bufferDelay        = bufferDelay;
            this.bufferDelayUnits   = units;
            return this;
        }
        
        public Builder withWriteDelay(long writeDelay, TimeUnit units) {
            this.writeDelay = units.toMillis(writeDelay);
            return this;
        }
        
        public Builder withId(String id) {
            this.id = id;
            return this;
        }
        
        public Builder withWriterCount(int count) {
            this.writerThreads = count;
            return this;
        }
        
        public SimMessageWriter build() {
            return new SimMessageWriter(this);
        }

    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    private PublishSubject<Holder> subject;
    private String id;
    
    private SimMessageWriter(final Builder builder) {
        id = builder.id;
        subject = PublishSubject.create();
        
        subject
            .buffer(builder.bufferDelay, builder.bufferDelayUnits, builder.bufferSize)
            .observeOn(Schedulers.executor(
                    Executors.newFixedThreadPool(
                            builder.writerThreads, 
                            new ThreadFactoryBuilder()
                                .setNameFormat("SimeMessageWriter-" + builder.id + "-%d")
                                .build()
                            )
                        )
                    )
            .subscribe(new Action1<List<Holder>>() {
                @Override
                public void call(List<Holder> messages) {
                    // Introduce a delay
                    try {
                        Thread.sleep(builder.writeDelay);
                    } catch (InterruptedException e) {
                        LOG.error(e.getMessage(), e);
                    }
                    
                    // Ack each message
                    for (Holder holder : messages) {
                        try {
                            LOG.info("Write: " + holder.message.getMessage());
                            holder.observer.onNext(Completion.<OutgoingMessage>from(holder.message));
                            holder.observer.onCompleted();
                        }
                        catch(Throwable t) {
                            LOG.error(t.getMessage(), t);
                            holder.observer.onError(t);
                        }
                    }
                }
            });
    }
    
    @Override
    public Observable<Completion<OutgoingMessage>> write(final OutgoingMessage message) {
        return Observable.create(new Func1<Observer<Completion<OutgoingMessage>>, Subscription>() {
            @Override
            public Subscription call(final Observer<Completion<OutgoingMessage>> observer) {
                LOG.info("Add : " + message.getMessage());
                subject.onNext(new Holder(message, observer));
                return Subscriptions.empty();
            }
        });
    }

    @Override
    public void write(OutgoingMessage message, Observer<Completion<OutgoingMessage>> observer) {
        LOG.info("Add : " + message.getMessage());
        subject.onNext(new Holder(message, observer));
    }

    @Override
    public String getId() {
        return this.id;
    }
}
