package com.netflix.schlep.reader;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

import com.google.common.base.Suppliers;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.netflix.schlep.guice.SchlepModule;
import com.netflix.schlep.sim.SimMessageReader;

public class ReaderTest {
    private static final Logger LOG = LoggerFactory.getLogger(ReaderTest.class);

    public static final String READER_ID = "ReaderTest";

    /**
     * @author elandau
     */
    @Singleton
    public static class MyService {
        private MessageReader reader;

        @Inject
        public MyService(MessageReaderManager manager) {
            this.reader = manager.acquire(READER_ID);

            Observable.create(this.reader)
                    .map(new Func1<IncomingMessage, IncomingMessage>() {
                        @Override
                        public IncomingMessage call(IncomingMessage message) {
                            LOG.info(message.getContents(String.class));
                            return message;
                        }
                    })
                    .subscribe(new Action1<IncomingMessage>() {
                        @Override
                        public void call(IncomingMessage message) {
                            message.ack();
                        }
                    });
        }
    }

    @Test
    public void test() throws Exception {
        Injector injector = Guice.createInjector(new SchlepModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(MyService.class);
                    }
                });

        MessageReaderManager readerManager = injector.getInstance(MessageReaderManager.class);
        readerManager.add(
                READER_ID,
                Suppliers.<MessageReader> ofInstance(SimMessageReader.builder()
                        .withId(READER_ID)
                        .withBatchSize(3)
                        .withInterval(3, TimeUnit.SECONDS)
                        .withMaxCount(100)
                        .build()));

        injector.getInstance(MyService.class);

        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
    }
}
