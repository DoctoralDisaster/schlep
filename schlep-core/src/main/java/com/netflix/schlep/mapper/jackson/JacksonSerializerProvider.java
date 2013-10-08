package com.netflix.schlep.mapper.jackson;

import com.google.inject.TypeLiteral;
import com.netflix.schlep.mapper.Serializer;
import com.netflix.schlep.mapper.SerializerProvider;

/**
 * Serialize entities to/from JSON strings.  
 * 
 * Meant to be used with very simple POJOs and only uses the fields.
 * 
 * @author elandau
 *
 * @param <T>
 */
public class JacksonSerializerProvider implements SerializerProvider {
    private JacksonSerializer serializer = new JacksonSerializer();
    
    @Override
    public <T> Serializer findSerializer(final Class<T> clazz) {
        return serializer;
    }

    @Override
    public <T> Serializer findSerializer(TypeLiteral<T> type) {
        return (Serializer) findSerializer(type.getRawType());
    }
}
