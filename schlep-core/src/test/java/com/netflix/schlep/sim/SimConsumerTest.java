package com.netflix.schlep.sim;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.concurrency.Schedulers;
import rx.subjects.PublishSubject;
import rx.util.functions.Action1;

import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.consumer.MessageConsumer;
import com.netflix.schlep.processor.MessageHandler;
import com.netflix.schlep.processor.MessageProcessors;
import com.netflix.schlep.writer.MessageWriter;
import com.netflix.schlep.writer.Completion;

public class SimConsumerTest {
    private static final Logger LOG = LoggerFactory.getLogger(SimConsumerTest.class);
    
    @Test
    @Ignore
    public void test() throws Exception {
        
//        final MessageConsumer reader = SimMessageConsumer.builder()
//                .withId("test")
//                .withBatchSize(1)
//                .withMaxCount(10)
//                .withThrottle(500, TimeUnit.MILLISECONDS)
//                .build();
//                
//        final MessageRouter  dispatcher = new MessageRouter(Observable.create(reader));
//        
//        // Async, one thread per reply, 
//        dispatcher.addProcessor("id1", MessageProcessors.async(new Action1<IncomingMessage>() {
//            @Override
//            public void call(IncomingMessage message) {
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                }
//                LOG.info("Async : " + message);
//            }
//        }, Schedulers.newThread()));
//        
//        // Sync, reply immediately
//        dispatcher.addProcessor("id2", new MessageHandler() {
//            @Override
//            public Observable<Completion<IncomingMessage>> call(IncomingMessage message) {
//                LOG.info("Sync : " + message);
//                return Observable.just(Completion.from(message));
//            }
//        });
//        
//        // Funnel reply to writer
//        final MessageWriter writer = SimMessageWriter.builder().build();
//
//        dispatcher.addProcessor("id3", MessageProcessors.toWriter(writer));
//        
//        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
    }
    
    @Test
    @Ignore
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
