package com.netflix.schlep.apq;

import com.netflix.schlep.guice.SchlepPluginModule;

public class ApqModule extends SchlepPluginModule {
    public static final String NAME = "apq";
    
    public ApqModule() {
        super(NAME,
            ApqMessageConsumerProvider.class,
            ApqMessageProducerProvider.class);
    }

    @Override
    public void internalConfigure() {
    }

}
