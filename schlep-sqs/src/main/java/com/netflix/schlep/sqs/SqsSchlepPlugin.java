package com.netflix.schlep.sqs;

import com.netflix.schlep.guice.SchlepPlugin;

public class SqsSchlepPlugin extends SchlepPlugin {
    public static final String TYPE = "sqs";
    
    public SqsSchlepPlugin() {
    }

    @Override
    protected void configure() {
        this.registerConsumerType(TYPE,  SqsMessageConsumerFactory.class);
        this.registerProducerType(TYPE,  SqsMessageProducerFactory.class);
    }

}
