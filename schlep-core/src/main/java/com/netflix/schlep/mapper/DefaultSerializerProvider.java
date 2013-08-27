package com.netflix.schlep.mapper;

import com.netflix.schlep.mapper.jackson.JacksonSerializerProvider;

public class DefaultSerializerProvider extends BaseSerializerProvider {
    public DefaultSerializerProvider() {
        this.setDefaultSerializerProvider(new JacksonSerializerProvider());
        
//        TypeLiteral<Map<String, Object>> mapType = new TypeLiteral<Map<String, Object>>() {};
        
    }
}
