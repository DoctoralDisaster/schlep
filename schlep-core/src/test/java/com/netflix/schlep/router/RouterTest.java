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
import com.netflix.schlep.processor.MessageProcessor;
import com.netflix.schlep.processor.MessageProcessors;
import com.netflix.schlep.reader.IncomingMessage;
import com.netflix.schlep.reader.MessageReader;
import com.netflix.schlep.reader.MessageReaderManager;
import com.netflix.schlep.sim.SimMessageReader;
import com.netflix.schlep.sim.SimMessageWriter;
import com.netflix.schlep.writer.Completion;
import com.netflix.schlep.writer.MessageWriter;
import com.netflix.schlep.writer.MessageWriterManager;

public class RouterTest {
    private static final Logger LOG = LoggerFactory.getLogger(RouterTest.class);
    
    public static final String WRITER_ID = "WriterTest";
    public static final String READER_ID = "ReaderTest";
    
    @Test
    @Ignore
    public void test() throws Exception {
        MessageWriterManager writerManager = new MessageWriterManager();
        writerManager.add(
                WRITER_ID, 
                Suppliers.<MessageWriter>ofInstance(SimMessageWriter.builder()
                      .withId(WRITER_ID)
                      .withBatchSize(3)
                      .withWriterCount(3)
                      .build()));
                
        MessageReaderManager readerManager = new MessageReaderManager();
        readerManager.add(
                READER_ID,
                Suppliers.<MessageReader> ofInstance(SimMessageReader.builder()
                      .withId(READER_ID)
                      .withBatchSize(10)
                      .withInterval(1, TimeUnit.SECONDS)
                      .withMaxCount(100)
                      .build()));
        
        final MessageRouter  dispatcher = new MessageRouter(Observable.create(readerManager.acquire(READER_ID)));

        // Async, one thread per reply, 
        dispatcher.addProcessor("id1", MessageProcessors.async(new Action1<IncomingMessage>() {
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
        dispatcher.addProcessor("id2", new MessageProcessor() {
            @Override
            public Observable<Completion<IncomingMessage>> process(IncomingMessage message) {
                LOG.info("Sync : " + message);
                return Observable.just(Completion.from(message));
            }
        });
        
        // Funnel reply to writer
        final MessageWriter writer = SimMessageWriter.builder().build();

        dispatcher.addProcessor("id3", MessageProcessors.toWriter(writer));
        
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
    }

}
