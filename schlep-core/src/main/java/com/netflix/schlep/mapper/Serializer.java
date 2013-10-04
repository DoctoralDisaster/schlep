package com.netflix.schlep.mapper;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstraction for a serializer used to convert entities to/from input streams
 * 
 * @author elandau
 *
 * @param <T>
 */
public interface Serializer {
    /**
     * Serialize an entity to a string.
     * @param entity
     * @return
     * @throws Exception
     */
    public <T> void serialize(T entity, OutputStream os) throws Exception;
    
    /**
     * Deserialize an entity from a string
     * @param str
     * @return
     * @throws Exception
     */
    public <T> T deserialize(InputStream is, Class<T> clazz) throws Exception;
}
