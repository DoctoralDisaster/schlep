package com.netflix.schlep.mapper.schema.types;

public class ListType extends ContainerType {
    private Schema valueType;
    
    public void setValueType(Schema valueType) {
        this.valueType = valueType;
    }
    
    public Schema getValueType() {
        return valueType;
    }
    
    @Override
    public SchemaType getType() {
        return SchemaType.LIST;
    }

    @Override
    public String toString() {
        return "ListSchema [valueType=" + valueType + "]";
    }
}
