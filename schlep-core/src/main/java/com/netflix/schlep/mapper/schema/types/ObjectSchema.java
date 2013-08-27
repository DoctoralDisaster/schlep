package com.netflix.schlep.mapper.schema.types;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Schema for an object with properties
 * 
 * @author elandau
 *
 */
public class ObjectSchema extends Schema {
    /**
     * ID of the schema.  
     */
    private String id;
    
    /**
     * Generated version string to uniquely identify the current configuration of 'properties'
     */
    private String version;
    
    /**
     * Map of all top level properties
     */
    private Map<String, Schema> properties;

    public Map<String, Schema> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Schema> properties) {
        this.properties = properties;
    }
    
    public void addProperty(String name, Schema property) {
        if (this.properties == null)
            this.properties = Maps.newHashMap();
        this.properties.put(name, property);
    }
    
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public SchemaType getType() {
        return SchemaType.OBJECT;
    }

    @Override
    public String toString() {
        return "ObjectSchema [id=" + id + ", version=" + version
                + ", properties=" + properties + "]";
    }
}
