package com.netflix.schlep.sqs;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class AmazonSqsModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
            .implement(SqsClient.class, AmazonSqsClient.class)
            .build(SqsClientFactory.class));        
    }
}
