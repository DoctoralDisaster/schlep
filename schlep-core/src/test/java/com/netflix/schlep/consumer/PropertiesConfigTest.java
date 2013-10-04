package com.netflix.schlep.consumer;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.Observable.OnSubscribeFunc;
import rx.subscriptions.BooleanSubscription;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.netflix.governator.configuration.ConfigurationProvider;
import com.netflix.governator.configuration.PropertiesConfigurationProvider;
import com.netflix.governator.guice.BootstrapBinder;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.schlep.Completion;
import com.netflix.schlep.governator.GovernatorConfigurationMessageConsumerFactory;
import com.netflix.schlep.guice.SchlepModule;
import com.netflix.schlep.sim.SimSchlepPlugin;

public class PropertiesConfigTest {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesConfigTest.class);

    public static final String CONSUMER_ID = "consumer1";

    /**
     * @author elandau
     */
    @Singleton
    public static class MyService {
        private MessageConsumer consumer;

        @Inject
        public MyService(MessageConsumerManager manager) {
            try {
                this.consumer = manager.get(CONSUMER_ID);
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }

            this.consumer.subscribe(new MessageHandler() {
                @Override
                public Observable<Completion<IncomingMessage>> call(final IncomingMessage message) {
                    LOG.info(message.getContents(String.class));
                    return Observable.create(new OnSubscribeFunc<Completion<IncomingMessage>>() {

                        @Override
                        public Subscription onSubscribe(final Observer<? super Completion<IncomingMessage>> observer) {
                            LOG.info("Observe: " + message);
                            Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                                @Override
                                public void run() {
                                    LOG.info("Process: " + message);
                                    observer.onNext(Completion.from(message));
                                    observer.onCompleted();
                                }
                            }, 3, TimeUnit.SECONDS);
                          
                            return new BooleanSubscription();
                        }
                    });
                }
            });
        }
        
        @PostConstruct
        public void init() {
            try {
                consumer.start();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }

    @Test
    public void test() throws Exception {
        final Properties properties = new Properties();
        properties.setProperty("com.netflix.schlep.consumer1.type",        SimSchlepPlugin.TYPE);
        properties.setProperty("com.netflix.schlep.consumer1.threadCount", "4");
        properties.setProperty("com.netflix.schlep.consumer1.batchSize",   "10");
        properties.setProperty("com.netflix.schlep.consumer1.maxCount",    "100");
        properties.setProperty("com.netflix.schlep.consumer1.maxBacklog",  "10");

        Injector injector = LifecycleInjector.builder()
            .withBootstrapModule(new BootstrapModule() {
                @Override
                public void configure(BootstrapBinder binder) {
                    binder.bind(ConfigurationProvider.class).toInstance(new PropertiesConfigurationProvider(properties));
                }
            })
            .withModules(
                new SchlepModule(),
                new SimSchlepPlugin(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(DefaultMessageConsumerFactory.class).to(GovernatorConfigurationMessageConsumerFactory.class);
                        bind(MyService.class);
                    }
            })
            .createInjector();                
                
        injector.getInstance(MyService.class);

        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
    }
}
