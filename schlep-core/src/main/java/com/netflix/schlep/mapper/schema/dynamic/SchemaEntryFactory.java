package com.netflix.schlep.mapper.schema.dynamic;

import com.netflix.schlep.mapper.schema.types.Schema;

public interface SchemaEntryFactory {
    public <T> CountingSchemaEntry<T> create(String name, Schema schema);
}
