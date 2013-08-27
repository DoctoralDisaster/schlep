package com.netflix.schlep.mapper.jackson;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;

public class SchlepJacksonModule extends SimpleModule {

    public SchlepJacksonModule() {
        super("SchlepJacksonModule", Version.unknownVersion());
    }
    
    public void setupModule(SetupContext context) {
        super.setupModule(context);
    }

}
