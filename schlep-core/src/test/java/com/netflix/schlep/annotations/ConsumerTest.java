package com.netflix.schlep.annotations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.consumer.MessageConsumer;
import com.netflix.schlep.consumer.MessageConsumerManager;
import com.netflix.schlep.consumer.OneMessageConsumer;
import com.netflix.schlep.guice.SchlepModule;

public class ConsumerTest {
    private static final Logger LOG = LoggerFactory.getLogger(ConsumerTest.class);
    
    private static final String CONSUMER_ID = "foo";
    
    @Singleton
    public static class MyService {
        private MessageConsumer consumer;
        private CountDownLatch latch = new CountDownLatch(1);
        
        @Inject
        public MyService() {
        }
        
        @Consumer(name = CONSUMER_ID, autoStart=true)
        public void fooConsume(IncomingMessage message) {
            LOG.info(message.getContents(String.class));
            latch.countDown();
        }
        
        public void waitForCompletion(long timeout, TimeUnit units) throws InterruptedException {
            latch.await(timeout, units);
        }
    }

    @Test
    @Ignore
    public void test() throws Exception {
        Injector injector = Guice.createInjector(
            new SchlepModule(),
            new AbstractModule() {
                @Override
                protected void configure() {
                    bind(MyService.class);
                }
            });

        MessageConsumerManager readerManager = injector.getInstance(MessageConsumerManager.class);
        OneMessageConsumer consumer = new OneMessageConsumer(CONSUMER_ID);
        readerManager.add(consumer);

        MyService service = injector.getInstance(MyService.class);
        service.waitForCompletion(1, TimeUnit.SECONDS);
        
        Assert.assertEquals(1, consumer.getAckCount());
        Assert.assertEquals(1, consumer.getOnNextCount());
    }
}
