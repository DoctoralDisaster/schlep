package com.netflix.schlep.mapper.schema;

import com.netflix.schlep.mapper.schema.types.Schema;

/**
 * Abstraction on top of a schema generator that inspects a type and returns
 * a representation of the object's typed schema.  
 * 
 * @see Schema
 * @author elandau
 *
 */
public interface SchemaGenerator {

    /**
     * Generate a Schema for this type
     * 
     * @param clazz
     * @return
     */
    Schema getSchema(Class<?> clazz);

}
