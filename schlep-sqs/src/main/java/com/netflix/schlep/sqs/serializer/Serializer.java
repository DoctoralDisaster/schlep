package com.netflix.schlep.sqs.serializer;

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
    public String serialize(T entity) throws Exception;
    
    /**
     * Deserialize an entity from a string
     * @param str
     * @return
     * @throws Exception
     */
    public T deserialize(String str) throws Exception;
}
