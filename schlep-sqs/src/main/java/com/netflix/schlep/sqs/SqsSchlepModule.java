package com.netflix.schlep.sqs;

import com.netflix.schlep.guice.SchlepPluginModule;

public class SqsSchlepModule extends SchlepPluginModule {
    public static final String NAME = "sqs";
    
    public SqsSchlepModule() {
        super(NAME);
    }

    @Override
    public void internalConfigure() {
        this.registerMessageConsumerProvider(SqsMessageConsumerProvider.class);
        this.registerMessageProducerProvider(SqsMessageProducerProvider.class);
    }
}
