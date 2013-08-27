package com.netflix.schlep.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.netflix.schlep.consumer.MessageConsumerFactory;
import com.netflix.schlep.impl.ByNamedPropertyMessageConsumerProvider;
import com.netflix.schlep.impl.ByNamedPropertyMessageProducerProvider;
import com.netflix.schlep.producer.MessageProducerFactory;

public class SchlepModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MessageConsumerFactory.class).to(ByNamedPropertyMessageConsumerProvider.class).in(Scopes.SINGLETON);
        bind(MessageProducerFactory.class).to(ByNamedPropertyMessageProducerProvider.class).in(Scopes.SINGLETON);
    }
}
