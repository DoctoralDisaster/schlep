package com.netflix.schlep.sqs;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class TestSqsModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
            .implement(SqsClient.class, FakeSqsClient.class)
            .build(SqsClientFactory.class));        
    }
}
