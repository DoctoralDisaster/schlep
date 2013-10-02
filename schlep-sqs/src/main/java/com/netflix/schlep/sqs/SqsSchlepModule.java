package com.netflix.schlep.sqs;

import com.netflix.schlep.guice.SchlepPlugin;

public class SqsSchlepModule extends SchlepPlugin {
    public static final String NAME = "sqs";
    
    public SqsSchlepModule() {
        super(NAME);
    }

    @Override
    public void internalConfigure() {
        this.registerConsumerType(SqsMessageConsumerProvider.class);
        this.registerMessageProducerProvider(SqsMessageProducerProvider.class);
    }
}
