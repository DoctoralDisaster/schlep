package com.netflix.schlep.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.netflix.schlep.consumer.MessageConsumerFactory;
import com.netflix.schlep.producer.MessageProducerFactory;
import com.netflix.schlep.producer.ScheduledMessageProducerProvider;

public abstract class SchlepPluginModule extends AbstractModule {
    private final String schema;
    
    public SchlepPluginModule(String name) {
        this.schema = name;
    }
    
    @Override
    final protected void configure() {
        internalConfigure();
    }

    protected void registerMessageConsumerProvider(Class<? extends MessageConsumerFactory> consumerProvider) {
        MapBinder<String, MessageConsumerFactory> consumers = MapBinder.newMapBinder(binder(), String.class, MessageConsumerFactory.class);
        consumers.addBinding(schema).to(consumerProvider);
    }
    
    protected void registerMessageProducerProvider(Class<? extends MessageProducerFactory> producerProvider) {
        MapBinder<String, MessageProducerFactory> producers = MapBinder.newMapBinder(binder(), String.class, MessageProducerFactory.class);
        producers.addBinding(schema).to(producerProvider);
    }
    
    protected void registerScheduledMessageProducerProvider(Class<? extends ScheduledMessageProducerProvider> consumerProvider) {
        MapBinder<String, ScheduledMessageProducerProvider> producers = MapBinder.newMapBinder(binder(), String.class, ScheduledMessageProducerProvider.class);
        producers.addBinding(schema).to(consumerProvider);
    }
    
    protected abstract void internalConfigure();
}
