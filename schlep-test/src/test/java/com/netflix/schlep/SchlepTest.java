package com.netflix.schlep;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.schlep.exception.ConsumerException;
import com.netflix.schlep.guice.SchlepModule;
import com.netflix.schlep.reader.IncomingMessage;
import com.netflix.schlep.reader.MessageCallback;
import com.netflix.schlep.reader.MessageReader;
import com.netflix.schlep.reader.MessageReaderFactory;
import com.netflix.schlep.sim.SimSchlepModule;
import com.netflix.schlep.sqs.SqsSchlepModule;
import com.netflix.schlep.writer.MessageProducer;
import com.netflix.schlep.writer.MessageWriterFactory;

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
    
    public static class MyService {
        private final MessageReaderFactory consumerProvider;
        
        private final MessageWriterFactory producerProvider;
        
        private MessageReader<Message1> message1Consumer;
        
        private MessageReader<Message2> message2Consumer;
        
        private MessageProducer<Message1> message1Producer;
        
        @Inject
        public MyService(MessageReaderFactory consumerProvider, MessageWriterFactory producerProvider) {
            this.consumerProvider = consumerProvider;
            this.producerProvider = producerProvider;
        }
        
        @PostConstruct 
        public void init() throws Exception {
            message1Consumer = this.consumerProvider.createConsumer(EndpointKey.of("foo", Message1.class), null, new MessageCallback<Message1>() {
                @Override
                public void consume(IncomingMessage<Message1> message) throws ConsumerException {
                    LOG.info("Consume: " + message.getEntity());
                }
            });
            
            message2Consumer = this.consumerProvider.createConsumer(EndpointKey.of("bar", Message2.class), null, new MessageCallback<Message2>() {
                @Override
                public void consume(IncomingMessage<Message2> message) throws ConsumerException {
                    LOG.info("Consume: " + message.getEntity());
                }
            });
            
            message1Producer = this.producerProvider.createProducer(EndpointKey.of("foo", Message1.class), null);
        }
        
        @PreDestroy 
        public void shutdown() throws Exception {
            message1Consumer.stop();
            message2Consumer.stop();
        }
        
        public void produce(Message1 message) throws Exception {
            message1Producer.produce(message);
        }
    }
    
    @Test
    public void test() throws Exception {
        // Boostrap
        Injector injector = LifecycleInjector.builder()
            .withModules(
                new SimSchlepModule(),
                new SqsSchlepModule(),
                new SchlepModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        PropertiesConfiguration config = new PropertiesConfiguration();
                        config.addProperty("com.netflix.schlep.consumer.foo.type", "sim");
                        config.addProperty("com.netflix.schlep.consumer.bar.type", "sim");
                        config.addProperty("com.netflix.schlep.producer.foo.type", "sim");
                        bind(AbstractConfiguration.class).toInstance(config);
                        bind(MyService.class).in(Scopes.SINGLETON);
                    }
                }
            )
            .createInjector();

        LifecycleManager    manager = injector.getInstance(LifecycleManager.class);
        manager.start();
        
        try {
            MyService service = injector.getInstance(MyService.class);
            service.produce(new Message1("Hello world"));
        }
        finally {
            manager.close();
        }
    }
}
