package com.netflix.schlep.mapper;

import com.google.inject.TypeLiteral;

/**
 * Factory for creating a specific serializer instances for a class.
 * Depending on the implementation the same serializer may be reused.
 * A concrete serializer will most likely inspect the type's fields
 * and cache the field serializer.
 * 
 * @author elandau
 *
 */
public interface SerializerProvider {
    /**
     * Return a serializer for the provided type.
     * 
     * @param clazz
     * @return
     */
    public <T> Serializer findSerializer(Class<T> clazz);
    
    public <T> Serializer findSerializer(TypeLiteral<T> type);
}
