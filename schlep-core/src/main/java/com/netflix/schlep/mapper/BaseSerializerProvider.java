package com.netflix.schlep.mapper;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.inject.TypeLiteral;

public class BaseSerializerProvider implements SerializerProvider {

    private Map<TypeLiteral<?>, Serializer<?>> serializers = Maps.newHashMap();
    private SerializerProvider defaultProvider;
    
    public BaseSerializerProvider() {
        
    }

    public synchronized void setDefaultSerializerProvider(SerializerProvider serializerProvider) {
        this.defaultProvider = serializerProvider;
    }
    
    public synchronized <T> void addSerializer(Class<T> clazz, Serializer<T> serializer) {
        serializers.put(TypeLiteral.get(clazz), serializer);
    }
    
    public synchronized <T> void addSerializer(TypeLiteral<?> type, Serializer<T> serializer) {
        serializers.put(type, serializer);
    }
    
    @Override
    public synchronized <T> Serializer<T> findSerializer(Class<T> clazz) {
        return findSerializer(TypeLiteral.get(clazz));
    }

    @Override
    public synchronized <T> Serializer<T> findSerializer(TypeLiteral<T> type) {
        Serializer<?> serializer = serializers.get(type);
        if (serializer == null)
            return defaultProvider.findSerializer(type);
        return (Serializer<T>)serializer;
    }

}
