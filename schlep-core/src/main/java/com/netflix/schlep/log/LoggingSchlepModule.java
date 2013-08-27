package com.netflix.schlep.log;

import com.netflix.schlep.guice.SchlepPluginModule;

public class LoggingSchlepModule extends SchlepPluginModule {
    public static final String NAME = "log";
    
    public LoggingSchlepModule() {
        super(NAME);
    }

    @Override
    public void internalConfigure() {
        this.registerMessageProducerProvider(LoggingMessageProducerProvider.class);
    }
}
