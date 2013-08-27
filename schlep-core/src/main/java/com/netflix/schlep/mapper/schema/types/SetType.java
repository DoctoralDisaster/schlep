package com.netflix.schlep.mapper.schema.types;

public class SetType extends ContainerType {
    private Schema valueType;
    
    public void setValueType(Schema valueType) {
        this.valueType = valueType;
    }
    
    public Schema getValueType() {
        return valueType;
    }
    
    @Override
    public SchemaType getType() {
        return SchemaType.SET;
    }

    @Override
    public String toString() {
        return "SetSchema [valueType=" + valueType + "]";
    }
}
