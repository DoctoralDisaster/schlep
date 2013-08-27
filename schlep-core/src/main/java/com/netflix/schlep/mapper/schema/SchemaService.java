package com.netflix.schlep.mapper.schema;

/**
 * Abstraction for a schema service that keeps track of schema's for entities
 * 
 * @author elandau
 */
public interface SchemaService {
    public void addSchemaForEntity(Class<?> entityClass);
}
