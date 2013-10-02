package com.netflix.schlep.sim;

import com.netflix.schlep.guice.SchlepPlugin;

public class SimSchlepPlugin extends SchlepPlugin {
    public static final String TYPE = "sim";
    
    public SimSchlepPlugin() {
    }

    @Override
    public void configure() {
        this.registerConsumerType(TYPE, SimMessageConsumerFactory.class);
    }
}
