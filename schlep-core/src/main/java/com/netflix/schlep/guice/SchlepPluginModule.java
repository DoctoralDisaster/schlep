package com.netflix.schlep.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.netflix.schlep.MessageConsumerProvider;
import com.netflix.schlep.MessageProducerProvider;
import com.netflix.schlep.ScheduledMessageProducerProvider;

public abstract class SchlepPluginModule extends AbstractModule {
    private final String schema;
    
    public SchlepPluginModule(String name) {
        this.schema = name;
    }
    
    @Override
    final protected void configure() {
        internalConfigure();
    }

    protected void registerMessageConsumerProvider(Class<? extends MessageConsumerProvider> consumerProvider) {
        MapBinder<String, MessageConsumerProvider> consumers = MapBinder.newMapBinder(binder(), String.class, MessageConsumerProvider.class);
        consumers.addBinding(schema).to(consumerProvider);
    }
    
    protected void registerMessageProducerProvider(Class<? extends MessageProducerProvider> producerProvider) {
        MapBinder<String, MessageProducerProvider> producers = MapBinder.newMapBinder(binder(), String.class, MessageProducerProvider.class);
        producers.addBinding(schema).to(producerProvider);
    }
    
    protected void registerScheduledMessageProducerProvider(Class<? extends ScheduledMessageProducerProvider> consumerProvider) {
        MapBinder<String, ScheduledMessageProducerProvider> producers = MapBinder.newMapBinder(binder(), String.class, ScheduledMessageProducerProvider.class);
        producers.addBinding(schema).to(consumerProvider);
    }
    
    protected abstract void internalConfigure();
}
