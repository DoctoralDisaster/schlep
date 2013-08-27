package com.netflix.schlep.mapper.schema.types;

/**
 * All possible main types for schema properties.  Top level objects
 * are of type OBJECT and may contain any of these types.  For collection
 * typs such as OBJECT, MAP, LIST, SET additional schema attributes
 * will describe the key and value type where applicable.
 * 
 * @author elandau
 *
 */
public enum SchemaType {
    BOOLEAN,
    SHORT,
    INTEGER,
    LONG,
    STRING,
    FLOAT,
    DOUBLE,
    OBJECT, // Structured object
    MAP,    // key-value pairs
    LIST,   // List or array of elements
    SET,    // List of unique elements
}
