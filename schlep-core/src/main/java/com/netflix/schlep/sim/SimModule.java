package com.netflix.schlep.sim;

import com.netflix.schlep.guice.SchlepPluginModule;

public class SimModule extends SchlepPluginModule {
    public static final String SCHEME = "sim";
    
    public SimModule() {
        super(SCHEME);
    }

    @Override
    public void internalConfigure() {
        this.registerMessageConsumerProvider(SimMessageConsumerProvider.class);
        this.registerMessageProducerProvider(SimMessageProducerProvider.class);
    }
}
