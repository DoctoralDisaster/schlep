package com.netflix.schlep.log;

import com.netflix.schlep.guice.SchlepPlugin;

public class LoggingSchlepModule extends SchlepPlugin {
    public static final String TYPE = "log";
    
    public LoggingSchlepModule() {
    }

    @Override
    public void configure() {
//        this.registerMessageProducerProvider(LoggingMessageProducerProvider.class);
    }
}
