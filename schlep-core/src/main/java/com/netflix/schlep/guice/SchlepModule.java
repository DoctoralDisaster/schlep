package com.netflix.schlep.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.netflix.schlep.reader.MessageReaderManager;
import com.netflix.schlep.writer.MessageWriterManager;

public class SchlepModule extends AbstractModule {
    @Override
    protected void configure() {
//        bind(MessageReaderFactory.class).to(ByNamedPropertyMessageReaderProvider.class).in(Scopes.SINGLETON);
//        bind(MessageWriterFactory.class).to(ByNamedPropertyMessageWriterProvider.class).in(Scopes.SINGLETON);
        
        bind(MessageReaderManager.class).in(Scopes.SINGLETON);
        bind(MessageWriterManager.class).in(Scopes.SINGLETON);
        
    }
}
