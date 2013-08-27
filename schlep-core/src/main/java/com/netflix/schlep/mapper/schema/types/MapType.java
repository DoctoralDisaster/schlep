package com.netflix.schlep.mapper.schema.types;

public class MapType extends ContainerType {
    private Schema keyType;
    private Schema valueType;
    
    public Schema getKeyType() {
        return keyType;
    }
    
    public void setKeyType(Schema keyType) {
        this.keyType = keyType;
    }
    
    public void setValueType(Schema valueType) {
        this.valueType = valueType;
    }
    
    public Schema getValueType() {
        return valueType;
    }
    
    @Override
    public SchemaType getType() {
        return SchemaType.MAP;
    }

    @Override
    public String toString() {
        return "MapSchema [keyType=" + keyType + ", valueType=" + valueType
                + "]";
    }
}
