package com.netflix.schlep.mapper.schema.dynamic;

import com.netflix.schlep.mapper.schema.types.Schema;
import com.netflix.schlep.mapper.schema.types.SchemaType;

/**
 * Encapsulate a Schema and provided a mechanism to track
 * usage count.
 * 
 * @author elandau
 *
 * @param <T>
 */
public abstract class CountingSchemaEntry<T> extends Schema {
    private final Schema orig;
    
    public CountingSchemaEntry(Schema orig) {
        this.orig = orig;
    }

    @Override
    public SchemaType getType() {
        return orig.getType();
    }

    /**
     * Increment the number of writes to the field represented
     * by this schema
     */
    public abstract void incWriteCount();
    
    /**
     * Increment the number of reads to the field represented
     * by this schema
     */
    public abstract void incReadCount();
}
