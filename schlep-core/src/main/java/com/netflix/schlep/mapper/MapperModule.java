package com.netflix.schlep.mapper;

public interface MapperModule {
    public <T> Serializer<T> decorateSerializer(Serializer<T> serializer);
}
