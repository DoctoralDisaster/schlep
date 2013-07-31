package com.netflix.schlep.sqs;

import com.google.inject.multibindings.MapBinder;
import com.netflix.schlep.guice.SchlepPluginModule;
import com.netflix.schlep.sqs.retry.RetryPolicyFactory;
import com.netflix.schlep.sqs.retry.NoRetryPolicy;

public class SqsModule extends SchlepPluginModule {
    public static final String NAME = "sqs";
    
    public SqsModule() {
        super(NAME,
            SqsMessageConsumerProvider.class,
            SqsMessageProducerProvider.class);
    }

    @Override
    public void internalConfigure() {
        registerRetryPolicy("none", NoRetryPolicy.class);
    }

    private void registerRetryPolicy(String name, Class<? extends RetryPolicyFactory> clazz) {
        MapBinder<String, RetryPolicyFactory> policies = MapBinder.newMapBinder(binder(), String.class, RetryPolicyFactory.class);
        policies.addBinding(name).to(clazz);
    }
}
