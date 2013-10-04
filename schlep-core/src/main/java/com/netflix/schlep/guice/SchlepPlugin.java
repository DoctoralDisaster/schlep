package com.netflix.schlep.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.netflix.schlep.consumer.MessageConsumerFactory;
import com.netflix.schlep.producer.MessageProducerFactory;

/**
 * Utility class with convenience methods for registering configurable components
 * 
 * @author elandau
 *
 */
public abstract class SchlepPlugin extends AbstractModule {
    /**
     * Register a consumer type.  The 'type' string is the value used in the configuration field
     * that differentiates the type.
     * 
     * @param type
     * @param consumerProvider
     */
    protected void registerConsumerType(String type, Class<? extends MessageConsumerFactory> factory) {
        MapBinder<String, MessageConsumerFactory> consumers = MapBinder.newMapBinder(binder(), String.class, MessageConsumerFactory.class);
        consumers.addBinding(type).to(factory);
    }
    
    /**
     * Register a producer type.  The 'type' string is the value used in the configuration field
     * that differentiates the type.
     * 
     * @param type
     * @param consumerProvider
     */
    protected void registerProducerType(String type, Class<? extends MessageProducerFactory> producerProvider) {
        MapBinder<String, MessageProducerFactory> producers = MapBinder.newMapBinder(binder(), String.class, MessageProducerFactory.class);
        producers.addBinding(type).to(producerProvider);
    }
}
