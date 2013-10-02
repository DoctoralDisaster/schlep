package com.netflix.schlep.consumer;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.subscriptions.BooleanSubscription;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.consumer.MessageConsumer;
import com.netflix.schlep.consumer.MessageConsumerRegistry;
import com.netflix.schlep.guice.SchlepModule;
import com.netflix.schlep.processor.MessageHandler;
import com.netflix.schlep.sim.SimMessageConsumer;
import com.netflix.schlep.writer.Completion;

public class ConsumerTest {
    private static final Logger LOG = LoggerFactory.getLogger(ConsumerTest.class);

    public static final String CONSUMER_ID = "ReaderTest";

    /**
     * @author elandau
     */
    @Singleton
    public static class MyService {
        private MessageConsumer consumer;

        @Inject
        public MyService(MessageConsumerRegistry manager) {
            try {
                this.consumer = manager.get(CONSUMER_ID);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
            
            this.consumer.subscribe(new MessageHandler() {
                @Override
                public Observable<Completion<IncomingMessage>> call(IncomingMessage message) {
                    LOG.info(message.getContents(String.class));
                    return Observable.from(Completion.from(message));
                }
                
            });
        }
    }
    
    @Test
    @Ignore
    public void test2() throws Exception {
        Subscription s = Observable.create(new Func1<Observer<Integer>, Subscription>() {
            @Override
            public Subscription call(Observer<Integer> t1) {
                try {
                    int counter = 0;
                    while (true) {
                        t1.onNext(counter++);
                        Thread.sleep(200);
                    }
                }
                catch (Throwable t) {
                    System.out.println("Done in observable");
                }
                return new BooleanSubscription();
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer t1) {
                LOG.info(t1.toString());
            }
        }, Schedulers.newThread());
        
        Thread.sleep(1000);
        s.unsubscribe();
        
        System.out.println("Done");
        Thread.sleep(10000);
    }
    
    @Test
    @Ignore
    public void test() throws Exception {
        Injector injector = Guice.createInjector(new SchlepModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(MyService.class);
                    }
                });

        MessageConsumerRegistry readerManager = injector.getInstance(MessageConsumerRegistry.class);
        readerManager.add(
                SimMessageConsumer.builder()
                        .withId(CONSUMER_ID)
                        .withBatchSize(3)
                        .withThrottle(3, TimeUnit.SECONDS)
                        .withMaxCount(100)
                        .build());

        injector.getInstance(MyService.class);

        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
    }
}
