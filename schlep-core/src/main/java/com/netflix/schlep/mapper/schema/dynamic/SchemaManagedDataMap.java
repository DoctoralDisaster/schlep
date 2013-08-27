package com.netflix.schlep.mapper.schema.dynamic;

import java.util.Map;

/**
 * Implementation of a DataMap that manages a dynamic schema on the side
 * 
 * @author elandau
 */
public class SchemaManagedDataMap implements DataMap {
    private final Map<String, Object> map;
    private final DynamicSchema schema;
    
    public SchemaManagedDataMap(Map<String, Object> map, DynamicSchema schema) {
        this.map = map;
        this.schema = schema;
    }
    
    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public Integer getInt(String key, Integer defaultValue) {
        schema.readAccess(key, Integer.class);
        Object obj = map.get(key);
        return obj == null ? defaultValue : (Integer)obj;
    }

    @Override
    public Short getShort(String key, Short defaultValue) {
        schema.readAccess(key, Short.class);
        Object obj = map.get(key);
        return obj == null ? defaultValue : (Short)obj;
    }

    @Override
    public String getString(String key, String defaultValue) {
        schema.readAccess(key, String.class);
        Object obj = map.get(key);
        return obj == null ? defaultValue : (String)obj;
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        schema.readAccess(key, Boolean.class);
        Object obj = map.get(key);
        return obj == null ? defaultValue : (Boolean)obj;
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        schema.readAccess(key, Long.class);
        Object obj = map.get(key);
        return obj == null ? defaultValue : (Long)obj;
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        schema.readAccess(key, Double.class);
        Object obj = map.get(key);
        return obj == null ? defaultValue : (Double)obj;
    }

    @Override
    public Float getFloat(String key, Float defaultValue) {
        schema.readAccess(key, Float.class);
        Object obj = map.get(key);
        return obj == null ? defaultValue : (Float)obj;
    }

    @Override
    public Integer putInt(String key, Integer value) {
        schema.writeAccess(key, Integer.class);
        return (Integer) map.put(key, value);
    }

    @Override
    public Short putShort(String key, Short value) {
        schema.writeAccess(key, Short.class);
        return (Short) map.put(key, value);
    }

    @Override
    public String putString(String key, String value) {
        schema.writeAccess(key, String.class);
        return (String) map.put(key, value);
    }

    @Override
    public Boolean putBoolean(String key, Boolean value) {
        schema.writeAccess(key, Boolean.class);
        return (Boolean) map.put(key, value);
    }

    @Override
    public Long putLong(String key, Long value) {
        schema.writeAccess(key, Long.class);
        return (Long) map.put(key, value);
    }

    @Override
    public Double putDouble(String key, Double value) {
        schema.writeAccess(key, Double.class);
        return (Double) map.put(key, value);
    }

    @Override
    public Float putFloat(String key, Float value) {
        schema.writeAccess(key, Float.class);
        return (Float) map.put(key, value);
    }

    @Override
    public Object remove(String key) {
        return map.remove(key);
    }

    @Override
    public int size() {
        return map.size();
    }
}
