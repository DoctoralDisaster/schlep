package com.netflix.schlep.sim;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.BooleanSubscription;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

import com.google.common.base.Supplier;
import com.netflix.schlep.processor.MessageProcessor;
import com.netflix.schlep.processor.MessageProcessors;
import com.netflix.schlep.reader.IncomingMessage;
import com.netflix.schlep.reader.MessageReader;
import com.netflix.schlep.reader.MessageReaderManager;
import com.netflix.schlep.router.MessageRouter;
import com.netflix.schlep.sim.SimMessageWriter.Holder;
import com.netflix.schlep.writer.MessageWriterManager;
import com.netflix.schlep.writer.MessageWriter;
import com.netflix.schlep.writer.Completion;
import com.netflix.schlep.writer.OutgoingMessage;

public class SimConsumerTest {
    private static final Logger LOG = LoggerFactory.getLogger(SimConsumerTest.class);
    
    @Test
    public void test() throws Exception {
        
        final MessageReader reader = SimMessageReader.builder()
                .withId("test")
                .withBatchSize(1)
                .withMaxCount(10)
                .withInterval(500, TimeUnit.MILLISECONDS)
                .build();
                
        final MessageRouter  dispatcher = new MessageRouter(Observable.create(reader));
        
        // Async, one thread per reply, 
        dispatcher.addProcessor(MessageProcessors.async(new Action1<IncomingMessage>() {
            @Override
            public void call(IncomingMessage message) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                LOG.info("Async : " + message);
            }
        }, Schedulers.newThread()));
        
        // Sync, reply immediately
        dispatcher.addProcessor(new MessageProcessor() {
            @Override
            public Observable<Completion<IncomingMessage>> process(IncomingMessage message) {
                LOG.info("Sync : " + message);
                return Observable.just(Completion.from(message));
            }
        });
        
        // Funnel reply to writer
        final MessageWriter writer = SimMessageWriter.builder().build();

        dispatcher.addProcessor(MessageProcessors.toWriter(writer));
        
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
    }
    
    @Test
    public void testBuffer() throws Exception {
        PublishSubject<Integer> subject = PublishSubject.create();
        subject.buffer(3, TimeUnit.SECONDS, 3)
            .observeOn(Schedulers.newThread())
            .subscribe(new Action1<List<Integer>>() {
                @Override
                public void call(List<Integer> t1) {
                    LOG.info("Sending batch");
                    for (Integer w : t1) {
                        LOG.info(w.toString());
                    }
                }
            });
        
        for (int i = 0; i < 20; i++) {
            Thread.sleep(10);
            subject.onNext(i);
        }
        
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));

    }
}
