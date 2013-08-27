package com.netflix.schlep.eventbus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerFactory;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.JavaType;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.netflix.eventbus.impl.EventBusImpl;
import com.netflix.eventbus.spi.EventBus;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.schlep.config.PolymorphicModule;
import com.netflix.schlep.eventbus.events.SimpleEvent;
import com.netflix.schlep.eventbus.jersey.BridgeAdminResource;
import com.netflix.schlep.eventbus.jersey.BridgeEntity;
import com.netflix.schlep.guice.SchlepModule;
import com.netflix.schlep.log.LoggingSchlepModule;
import com.netflix.schlep.sim.SimSchlepModule;
import com.netflix.schlep.sqs.FakeSqsClient;
import com.netflix.schlep.sqs.SimpleSqsClientConfiguration;
import com.netflix.schlep.sqs.SqsClient;
import com.netflix.schlep.sqs.SqsClientFactory;
import com.netflix.schlep.sqs.SqsSchlepModule;
import com.netflix.util.batch.BatchingPolicy;
import com.netflix.util.retry.CountingRetryPolicy;
import com.netflix.util.retry.RetryPolicy;

public class EventBusManagerTest {
    @Test
    public void test1() throws Exception {
        // Boostrap
        Injector injector = LifecycleInjector.builder()
            .withModules(
                new SimSchlepModule(),
                new SqsSchlepModule(),
                new LoggingSchlepModule(),
                new SchlepModule(),
                new EventBusModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        install(new FactoryModuleBuilder()
                            .implement(SqsClient.class, FakeSqsClient.class)
                            .build(SqsClientFactory.class));        
                        
                        bind(EventBus.class).to(EventBusImpl.class).asEagerSingleton();
                        bind(BridgeAdminResource.class);
                    }
                }
            )
            .createInjector();

        LifecycleManager    manager = injector.getInstance(LifecycleManager.class);
        manager.start();
        
        try {
            // Dummy config object
            SimpleSqsClientConfiguration config = new SimpleSqsClientConfiguration();
            config.setRetryPolicy(new CountingRetryPolicy(5));
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
            mapper.registerModule(new PolymorphicModule());
            
//            mapper.setSerializerFactory(new CustomSerializerFactory() {
//                public JsonSerializer<Object> createSerializer(SerializationConfig config, JavaType type,
//                        BeanProperty property) throws JsonMappingException {
//                    if (!type.isPrimitive()) {
//                        System.out.println(type.getRawClass());
//                        
//                        Class<?> clazz = type.getRawClass();
//                        for (Type i : clazz.getGenericInterfaces()) {
//                            System.out.println(" " + i);
//                        }
//                    }
//                    return super.createSerializer(config, type, property);
//                }
//            });
            
            String str = mapper.writeValueAsString(config);
            System.out.println(str);
            SimpleSqsClientConfiguration config2 = mapper.readValue(str.getBytes(), SimpleSqsClientConfiguration.class);
            
            String str2 = mapper.writeValueAsString(config2);
            System.out.println(str2);
            
            BridgeAdminResource admin = injector.getInstance(BridgeAdminResource.class);
            admin.addBridge(new BridgeEntity()
                    .withId("foo")
                    .withAutoStart(true)
                    .withEventType(SimpleEvent.class.getCanonicalName())
                    .withProducerType("sqs")
                    .withConfiguration(null));
            
            System.out.println(admin.listBridges());
            
            EventBus eventBus = injector.getInstance(EventBus.class);
            eventBus.publish(new SimpleEvent(0));
            
            Thread.sleep(TimeUnit.MINUTES.toMillis(2));
        }
        finally {
            manager.close();
        }
    }
}
