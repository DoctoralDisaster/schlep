package com.netflix.schlep.mapper.schema.dynamic;

/**
 * A dynamic schema is a meant to be used with unstructured data such
 * as a DataMap and auto-identifies fields in the schema as they are 
 * accessed or written.  As the schema is slowly constructed all calls 
 * will be no ops.  However, as the schema is discovered updates will 
 * likely be sent to a schema registry service.
 * 
 * @author elandau
 *
 */
public interface DynamicSchema {

    /**
     * Notify the dynamic schema of write access. 
     * 
     * @param name
     * @param type
     */
    public <T> void writeAccess(String name, Class<T> type);

    /**
     * Notify the dynamic schema of read access. 
     * 
     * @param name
     * @param type
     */
    public <T> void readAccess(String name, Class<T> type);

}
