package com.netflix.schlep.mapper.schema.dynamic;

/**
 * Very simple interface to a Map like data structure containing
 * key value pairs.  Typed methods are provided (instead of generic
 * Map.put(String, Object)), so that additional type checks and
 * schema generation can be done by an implementation.
 * 
 * @author elandau
 *
 * TODO:  Nested DataMap
 */
public interface DataMap {

    /**
     * Clear all data in the map
     */
    public void clear();

    /**
     * @return true if there is no data in the map
     */
    public boolean isEmpty();

    /**
     * Remove an element by key
     * @param key
     * @return
     */
    public Object  remove(String key);

    /**
     * Return number of elements in the map
     * 
     * TODO: What about nested DataMap.  Do we want to count those too?
     * @return
     */
    public int size();

    public Integer getInt(String key, Integer defaultValue);
    
    public Short   getShort(String key, Short defaultValue);
    
    public String  getString(String key, String defaultValue);
    
    public Boolean getBoolean(String key, Boolean defaultValue);
    
    public Long    getLong(String key, Long defaultValue);
    
    public Double  getDouble(String key, Double defaultValue);
    
    public Float   getFloat(String key, Float defaultValue);

//    public DataMap getDataMap(String key);
    
    public Integer putInt(String key, Integer value);
    
    public Short   putShort(String key, Short value);
    
    public String  putString(String key, String value);
    
    public Boolean putBoolean(String key, Boolean value);
    
    public Long    putLong(String key, Long value);
    
    public Double  putDouble(String key, Double value);
    
    public Float   putFloat(String key, Float value);

//    public DataMap putDataMap(String key, DataMap value);
}
