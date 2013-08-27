package com.netflix.schlep.mapper.schema.types;

public class StringType extends SimpleType {
    public StringType() {
        super(SchemaType.STRING);
    }

    @Override
    public String toString() {
        return "StringSchema []";
    }
}
