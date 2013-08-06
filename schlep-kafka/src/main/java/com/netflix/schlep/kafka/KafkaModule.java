package com.netflix.schlep.kafka;

import com.netflix.schlep.guice.SchlepPluginModule;

public class KafkaModule extends SchlepPluginModule {
    public static final String NAME = "apq";
    
    public KafkaModule() {
        super(NAME);
    }

    @Override
    public void internalConfigure() {
    }

}
