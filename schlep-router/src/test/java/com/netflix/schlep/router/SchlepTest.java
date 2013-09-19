package com.netflix.schlep.router;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Suppliers;
import com.netflix.schlep.reader.IncomingMessage;
import com.netflix.schlep.reader.MessageReader;
import com.netflix.schlep.router.service.SchlepRouterService;
import com.netflix.schlep.sim.SimMessageReader;
import com.netflix.schlep.sim.SimMessageWriter;
import com.netflix.schlep.writer.MessageWriter;

public class SchlepTest {
    private static final Logger LOG = LoggerFactory.getLogger(SchlepTest.class);
    
    public static class Message1 {
        private String str;
        
        public Message1(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return "Message1 [str=" + str + "]";
        }
    }
    
    public static class Message2 {

        @Override
        public String toString() {
            return "Message2 []";
        }
        
    }
    
    @Test
    public void test() throws Exception {
        SchlepRouterService service = new SchlepRouterService();
        service.addSource(SimMessageReader.builder()
                      .withId("in1")
                      .withBatchSize(10)
                      .withInterval(1, TimeUnit.SECONDS)
                      .withMaxCount(100)
                      .build());
        
        service.addSink(SimMessageWriter.builder()
                      .withId("out1")
                      .withBatchSize(3)
                      .withWriterCount(3)
                      .build());

        service.addRoute("in1", "out1", "filter1", new Predicate<IncomingMessage>() {
            public boolean apply(IncomingMessage message) {
                LOG.info("Filter message1 " + message.toString());
                return true;
            }
        });
        
        service.addRoute("in1", "out1", "filter2", new Predicate<IncomingMessage>() {
            public boolean apply(IncomingMessage message) {
                LOG.info("Filter message2 " + message.toString());
                return true;
            }
        });
        
        Thread.sleep(TimeUnit.SECONDS.toMillis(3));
        service.removeRoute("in1", "filter2");
        
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
        
//        // Boostrap
//        Injector injector = LifecycleInjector.builder()
//            .withModules(
//                new SimSchlepModule(),
//                new SqsSchlepModule(),
//                new SchlepModule(),
//                new AbstractModule() {
//                    @Override
//                    protected void configure() {
//                        PropertiesConfiguration config = new PropertiesConfiguration();
//                        config.addProperty("com.netflix.schlep.consumer.foo.type", "sim");
//                        config.addProperty("com.netflix.schlep.consumer.bar.type", "sim");
//                        config.addProperty("com.netflix.schlep.producer.foo.type", "sim");
//                        bind(AbstractConfiguration.class).toInstance(config);
//                        bind(MyService.class).in(Scopes.SINGLETON);
//                    }
//                }
//            )
//            .createInjector();
//
//        LifecycleManager    manager = injector.getInstance(LifecycleManager.class);
//        manager.start();
//        
//        try {
//            MyService service = injector.getInstance(MyService.class);
//            service.produce(new Message1("Hello world"));
//        }
//        finally {
//            manager.close();
//        }
    }
}
