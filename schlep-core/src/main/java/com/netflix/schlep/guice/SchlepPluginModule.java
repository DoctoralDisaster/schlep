package com.netflix.schlep.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.netflix.schlep.reader.MessageReaderFactory;
import com.netflix.schlep.writer.MessageWriterFactory;
import com.netflix.schlep.writer.ScheduledMessageProducerProvider;

public abstract class SchlepPluginModule extends AbstractModule {
    private final String schema;
    
    public SchlepPluginModule(String name) {
        this.schema = name;
    }
    
    @Override
    final protected void configure() {
        internalConfigure();
    }

    protected void registerMessageConsumerProvider(Class<? extends MessageReaderFactory> consumerProvider) {
        MapBinder<String, MessageReaderFactory> consumers = MapBinder.newMapBinder(binder(), String.class, MessageReaderFactory.class);
        consumers.addBinding(schema).to(consumerProvider);
    }
    
    protected void registerMessageProducerProvider(Class<? extends MessageWriterFactory> producerProvider) {
        MapBinder<String, MessageWriterFactory> producers = MapBinder.newMapBinder(binder(), String.class, MessageWriterFactory.class);
        producers.addBinding(schema).to(producerProvider);
    }
    
    protected void registerScheduledMessageProducerProvider(Class<? extends ScheduledMessageProducerProvider> consumerProvider) {
        MapBinder<String, ScheduledMessageProducerProvider> producers = MapBinder.newMapBinder(binder(), String.class, ScheduledMessageProducerProvider.class);
        producers.addBinding(schema).to(consumerProvider);
    }
    
    protected abstract void internalConfigure();
}
