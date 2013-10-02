package com.netflix.schlep.writer;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.util.functions.Action1;

import com.google.common.base.Suppliers;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.netflix.schlep.guice.SchlepModule;
import com.netflix.schlep.sim.SimMessageWriter;

public class WriterTest {
    private static final Logger LOG = LoggerFactory.getLogger(WriterTest.class);
    
    public static final String WRITER_ID = "WriterTest";
    
    /**
     * 
     * @author elandau
     */
    @Singleton
    public static class MyService {
        private MessageWriter   writer;
        
        @Inject
        public MyService(MessageProducerRegistry manager) {
            this.writer = manager.acquire(WRITER_ID);
            
            for (int i = 0; i < 10; i++) {
                this.writer
                    .write(OutgoingMessage.builder()
                        .withMessage("test-" + i)
                        .build())
                    .subscribe(new Action1<Completion<OutgoingMessage>>() {
                        @Override
                        public void call(Completion<OutgoingMessage> message) {
                            LOG.info("       Ack: " + message.getValue().getMessage());
                        }
                    });
            }
        }
    }
    
    @Test
    @Ignore
    public void test() throws Exception {
        LOG.info("Starting test");
        Injector injector = Guice.createInjector(
                new SchlepModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(MyService.class);
                    }
                }
            );
        
        LOG.info("Started LifecycleManager");
        MessageProducerRegistry writerManager = injector.getInstance(MessageProducerRegistry.class);
        writerManager.add(
                WRITER_ID, 
                Suppliers.<MessageWriter>ofInstance(SimMessageWriter.builder()
                     .withId(WRITER_ID)
                     .withBatchSize(3)
                     .withWriterCount(3)
                     .build()));
                
        injector.getInstance(MyService.class);
        
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
    }
}
