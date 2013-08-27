package com.netflix.schlep.eventbus;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.netflix.schlep.eventbus.jersey.JacksonReaderFactory;

public class EventBusModule extends AbstractModule {
    @Override
    public void configure() {
        bind(EventBusSchlepBridgeManager.class).in(Scopes.SINGLETON);
        bind(JacksonReaderFactory.class).in(Scopes.SINGLETON);
    }
}
