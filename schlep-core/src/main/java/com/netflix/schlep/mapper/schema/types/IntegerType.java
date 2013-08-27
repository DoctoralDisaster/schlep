package com.netflix.schlep.mapper.schema.types;

public class IntegerType extends SimpleType {
    public IntegerType() {
        super(SchemaType.INTEGER);
    }
    
    @Override
    public String toString() {
        return "IntegerSchema []";
    }
}
