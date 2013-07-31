package com.netflix.schlep;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.netflix.schlep.impl.ByNamedPropertyMessageConsumerProvider;
import com.netflix.schlep.impl.ByNamedPropertyMessageProducerProvider;

public class SchlepModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(MessageConsumerProvider.class).to(ByNamedPropertyMessageConsumerProvider.class).in(Scopes.SINGLETON);
        bind(MessageProducerProvider.class).to(ByNamedPropertyMessageProducerProvider.class).in(Scopes.SINGLETON);
    }
}
