package com.netflix.schlep.mapper.schema.dynamic;

import java.util.concurrent.atomic.AtomicLong;

import com.netflix.schlep.mapper.schema.types.Schema;

public class DynamicSchemaEntryImpl<T> extends CountingSchemaEntry<T> {
    private final String    name;
    private final AtomicLong readCount  = new AtomicLong();
    private final AtomicLong writeCount = new AtomicLong();
    
    public DynamicSchemaEntryImpl(String name, Schema type) {
        super(type);
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public void incWriteCount() {
        readCount.incrementAndGet();
    }

    public void incReadCount() {
        writeCount.incrementAndGet();
    }
    
    public long getReadCount() {
        return readCount.get();
    }
    
    public long getWriteCount() {
        return writeCount.get();
    }
}
