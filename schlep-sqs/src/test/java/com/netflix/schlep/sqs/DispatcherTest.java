package com.netflix.schlep.sqs;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.consumer.MessageCallback;
import com.netflix.schlep.consumer.MessageConsumer;
import com.netflix.schlep.consumer.MessageConsumerFactory;
import com.netflix.schlep.exception.ConsumerException;
import com.netflix.schlep.guice.SchlepModule;
import com.netflix.schlep.sqs.SqsSchlepModule;
import com.netflix.schlep.writer.MessageProducer;
import com.netflix.schlep.writer.MessageWriterFactory;

public class DispatcherTest {
    private static final Logger LOG = LoggerFactory.getLogger(DispatcherTest.class);
    
    /**
     * 
     * @author elandau
     */
    public static class MyMessage {
        public MyMessage() {
            
        }
        
        public MyMessage(String firstName, String lastName, int age) {
            super();
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
        }
        
        @Override
        public String toString() {
            return "MyMessage [firstName=" + firstName + ", lastName="
                    + lastName + ", age=" + age + "]";
        }
        private String firstName;
        private String lastName;
        private int age;
        
        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        public int getAge() {
            return age;
        }
        public void setAge(int age) {
            this.age = age;
        }
        
    }
    
    /**
     * 
     * @author elandau
     */
    public static class MyService {
        private final MessageConsumerFactory consumerProvider;
        private final MessageWriterFactory producerProvider;
        private MessageConsumer<MyMessage> message1Consumer;
        private MessageProducer<MyMessage> message1Producer;
        
        @Inject
        public MyService(MessageConsumerFactory consumerProvider, MessageWriterFactory producerProvider) {
            this.consumerProvider = consumerProvider;
            this.producerProvider = producerProvider;
        }
        
        @PostConstruct 
        public void init() throws Exception {
            message1Consumer = this.consumerProvider.createConsumer(EndpointKey.of("consumer1", MyMessage.class), null, new MessageCallback<MyMessage>() {
                @Override
                public void consume(IncomingMessage<MyMessage> message) throws ConsumerException {
                    LOG.info("Consume: " + message.getEntity());
                    message.ack();
                }
            });
            
            message1Producer = this.producerProvider.createProducer(EndpointKey.of("producer1", MyMessage.class), null);
            message1Consumer.start();
        }
        
        @PreDestroy 
        public void shutdown() throws Exception {
            message1Consumer.stop();
        }
        
        public void produce(MyMessage message) throws Exception {
            ListenableFuture<Boolean> response = message1Producer.produce(message);
        }
    }
    
    @Test
    public void test() throws Exception {
        // Boostrap
        Injector injector = LifecycleInjector.builder()
            .withModules(
                new SqsSchlepModule(),
                new TestSqsModule(),
                new SchlepModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        PropertiesConfiguration config = new PropertiesConfiguration();
                        
                        config.addProperty("consumer1.netflix.messaging.cloud.type", "sqs");
                        config.addProperty("consumer1.netflix.messaging.sqs.name",   "foo");
                        config.addProperty("producer1.netflix.messaging.cloud.type", "sqs");
                        config.addProperty("producer1.netflix.messaging.sqs.name",   "foo");
                        
                        bind(AbstractConfiguration.class).toInstance(config);
                        bind(MyService.class).in(Scopes.SINGLETON);
                        
//                        install(new FactoryModuleBuilder()
//                            .implement(SqsClientConfiguration.class, PropertiesSqsClientConfiguration.class)
//                            .build(SqsClientConfigurationFactory.class));        
                    }
                }
            )
            .createInjector();

        LifecycleManager    manager = injector.getInstance(LifecycleManager.class);
        manager.start();
        
        try {
            for (int i = 0; i < 10000; i++) {
                MyService service = injector.getInstance(MyService.class);
                service.produce(new MyMessage("Eric", "Cartman", i));
            }
            Thread.sleep(10000);
        }
        finally {
            manager.close();
        }
        
    }
}
