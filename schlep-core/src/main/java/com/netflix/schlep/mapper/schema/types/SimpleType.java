package com.netflix.schlep.mapper.schema.types;

public abstract class SimpleType extends Schema {
    private final SchemaType type;
    
    public SimpleType(SchemaType type) {
        this.type = type;
    }
    
    @Override
    public final SchemaType getType() {
        return type;
    }

}
