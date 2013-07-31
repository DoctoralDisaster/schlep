package com.netflix.schlep.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.netflix.schlep.MessageConsumerProvider;
import com.netflix.schlep.MessageProducerProvider;

public abstract class SchlepPluginModule extends AbstractModule {
    private final String schema;
    private final Class<? extends MessageConsumerProvider> consumerProvider;
    private final Class<? extends MessageProducerProvider> producerProvider;
    
    public SchlepPluginModule(String name, Class<? extends MessageConsumerProvider> consumerProvider, Class<? extends MessageProducerProvider> producerProvider) {
        this.schema = name;
        this.consumerProvider = consumerProvider;
        this.producerProvider = producerProvider;
    }
    
    @Override
    final protected void configure() {
        MapBinder<String, MessageConsumerProvider> consumers = MapBinder.newMapBinder(binder(), String.class, MessageConsumerProvider.class);
        consumers.addBinding(schema).to(consumerProvider);
        
        MapBinder<String, MessageProducerProvider> producers = MapBinder.newMapBinder(binder(), String.class, MessageProducerProvider.class);
        producers.addBinding(schema).to(producerProvider);
        
        internalConfigure();
    }

    public abstract void internalConfigure();
}
