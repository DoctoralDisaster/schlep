package com.netflix.schlep.mapper;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstraction for a serializer used to convert entities to/from strings
 * 
 * @author elandau
 *
 * @param <T>
 */
public interface Serializer<T> {
    /**
     * Serialize an entity to a string.
     * @param entity
     * @return
     * @throws Exception
     */
    public void serialize(T entity, OutputStream os) throws Exception;
    
    /**
     * Deserialize an entity from a string
     * @param str
     * @return
     * @throws Exception
     */
    public T deserialize(InputStream is) throws Exception;
    
    /**
     * Get the type being serialized
     * @return
     */
    Class<T> getType();
}
