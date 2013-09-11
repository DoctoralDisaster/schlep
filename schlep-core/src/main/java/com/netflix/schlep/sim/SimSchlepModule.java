package com.netflix.schlep.sim;

import com.netflix.schlep.guice.SchlepPluginModule;

public class SimSchlepModule extends SchlepPluginModule {
    public static final String NAME = "sim";
    
    public SimSchlepModule() {
        super(NAME);
    }

    @Override
    public void internalConfigure() {
    }
}
