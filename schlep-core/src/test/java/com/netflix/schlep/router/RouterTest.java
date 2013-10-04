package com.netflix.schlep.router;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.concurrency.Schedulers;
import rx.util.functions.Action1;

import com.google.common.base.Suppliers;
import com.netflix.schlep.Completion;
import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.consumer.MessageHandler;
import com.netflix.schlep.processor.MessageProcessors;
import com.netflix.schlep.producer.MessageProducerManager;
import com.netflix.schlep.producer.MessageProducer;
import com.netflix.schlep.sim.SimMessageConsumer;
import com.netflix.schlep.sim.SimMessageProducer;

public class RouterTest {
    private static final Logger LOG = LoggerFactory.getLogger(RouterTest.class);
    
    public static final String WRITER_ID = "WriterTest";
    public static final String READER_ID = "ReaderTest";
    
    @Test
    @Ignore
    public void test() throws Exception {
//        MessageProducerRegistry writerManager = new MessageProducerRegistry();
//        writerManager.add(
//                WRITER_ID, 
//                Suppliers.<MessageWriter>ofInstance(SimMessageWriter.builder()
//                      .withId(WRITER_ID)
//                      .withBatchSize(3)
//                      .withWriterCount(3)
//                      .build()));
//                
//        MessageConsumerRegistry readerManager = new MessageConsumerRegistry(null);
//        readerManager.add(
//                SimMessageConsumer.builder()
//                      .withId(READER_ID)
//                      .withBatchSize(10)
//                      .withThrottle(1, TimeUnit.SECONDS)
//                      .withMaxCount(100)
//                      .build());
//        
//        final MessageRouter  dispatcher = new MessageRouter(Observable.create(readerManager.get(READER_ID)));
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

}
