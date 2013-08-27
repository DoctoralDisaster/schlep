package com.netflix.schlep.mapper.schema.types;

public class BooleanType extends SimpleType {
    public BooleanType() {
        super(SchemaType.BOOLEAN);
    }

    @Override
    public String toString() {
        return "BooleanSchema []";
    }
}
