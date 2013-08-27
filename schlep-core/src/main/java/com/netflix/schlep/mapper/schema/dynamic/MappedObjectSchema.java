package com.netflix.schlep.mapper.schema.dynamic;

import com.google.common.base.Function;
import com.netflix.schlep.mapper.schema.SchemaGenerator;
import com.netflix.schlep.mapper.schema.types.Schema;
import com.netflix.schlep.mapper.schema.types.SchemaType;

/**
 * Schema built dynamically with notification from DyanmicSchema 
 * 
 * @author elandau
 *
 */
public class MappedObjectSchema extends Schema implements DynamicSchema {
    
    private CopyOnWriteMap<String, CountingSchemaEntry<?>> map = CopyOnWriteMap.create();
    private final SchemaEntryFactory factory;
    private final SchemaGenerator    generator;
    private final Function<MappedObjectSchema, Void> notify;
    
    private MappedObjectSchema(SchemaEntryFactory factory, SchemaGenerator generator, Function<MappedObjectSchema, Void> notify) {
        this.factory   = factory;
        this.generator = generator;
        this.notify    = notify;
    }
    
    /**
     * Inform the schema that a field was written to.  The type is provided
     * so that the schema may identify duplicate types for the same field name
     * 
     * @param name
     * @param type
     */
    @Override
    public <T> void writeAccess(String name, Class<T> type) {
        CountingSchemaEntry<?> entry = map.get(name);
        if (entry == null) {
            entry = factory.create(name, generator.getSchema(type));
            CountingSchemaEntry<?> prev = map.putIfAbsent(name + type.getCanonicalName(), entry);
            if (prev != null) {
                entry = prev;
            }
            else {
                notify.apply(this);
            }
        }
        entry.incWriteCount();
    }
    
    /**
     * Inform the schema of access to a field
     * @param name
     * @param type
     */
    @Override
    public <T> void readAccess(String name, Class<T> type) {
        CountingSchemaEntry<?> entry = map.get(name);
        if (entry == null) {
            entry = factory.create(name, generator.getSchema(type));
            CountingSchemaEntry<?> prev = map.putIfAbsent(name + type.getCanonicalName(), entry);
            if (prev != null) {
                entry = prev;
            }
            else {
                notify.apply(this);
            }
        }
        entry.incReadCount();
    }
    
    @Override
    public SchemaType getType() {
        return SchemaType.OBJECT;
    }

}
