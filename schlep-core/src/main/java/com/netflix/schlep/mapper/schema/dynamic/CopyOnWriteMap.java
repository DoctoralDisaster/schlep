package com.netflix.schlep.mapper.schema.dynamic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * HashMap implementation for high read throughput maps that makes a copy of
 * the map under a lock whenever there is a change.  Read don't require a lock
 *
 * @param <K>
 * @param <V>
 */
public class CopyOnWriteMap<K, V> implements ConcurrentMap<K, V>, Cloneable {
    private volatile Map<K, V> internalMap;
    
    public static <K, V> CopyOnWriteMap<K,V> create() {
        return new CopyOnWriteMap<K,V>();
    }
    
    /**
     * Creates a new instance of CopyOnWriteMap.
     */
    public CopyOnWriteMap() {
        internalMap = new HashMap<K, V>();
    }
    
    /**
     * Creates a new instance of CopyOnWriteMap with the specified initial size
     *
     * @param initialCapacity
     *  The initial size of the Map.
     */
    public CopyOnWriteMap(int initialCapacity) {
        internalMap = new HashMap<K, V>(initialCapacity);
    }
    /**
     * Creates a new instance of CopyOnWriteMap in which the
     * initial data being held by this map is contained in
     * the supplied map.
     *
     * @param data
     *  A Map containing the initial contents to be placed into
     *  this class.
     */
    public CopyOnWriteMap(Map<K, V> data) {
        internalMap = new HashMap<K, V>(data);
    }
    
    /**
     * Adds the provided key and value to this map.
     *
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public V put(K key, V value) {
        synchronized (this) {
            Map<K, V> newMap = new HashMap<K, V>(internalMap);
            V val = newMap.put(key, value);
            internalMap = newMap;
            return val;
        }
    }
    
    @Override
    public V putIfAbsent(K key, V value) {
        synchronized (this) {
            V existing = internalMap.get(key);
            if (existing != null)
                return existing;
            
            Map<K, V> newMap = new HashMap<K, V>(internalMap);
            V val = newMap.put(key, value);
            internalMap = newMap;
            return existing;
        }
    }

    /**
     * Removed the value and key from this map based on the
     * provided key.
     *
     * @see java.util.Map#remove(java.lang.Object)
     */
    public V remove(Object key) {
        synchronized (this) {
            Map<K, V> newMap = new HashMap<K, V>(internalMap);
            V val = newMap.remove(key);
            internalMap = newMap;
            return val;
        }
    }
    
    @Override
    public boolean remove(Object key, Object expected) {
        synchronized (this) {
            V existing = internalMap.get(key);
            if (existing != null && existing.equals(expected)) {
                Map<K, V> newMap = new HashMap<K, V>(internalMap);
                V ret = newMap.remove(key);
                internalMap = newMap;
                return true;
            }
            return false;
        }
    }

    @Override
    public V replace(K key, V value) {
        synchronized (this) {
            V existing = internalMap.get(key);
            if (existing != null) {
                Map<K, V> newMap = new HashMap<K, V>(internalMap);
                V val = newMap.put(key, value);
                internalMap = newMap;
                return val;
            }
            return null;
        }
    }

    @Override
    public boolean replace(K key, V value, V expected) {
        synchronized (this) {
            V existing = internalMap.get(key);
            if (existing != null && existing.equals(expected)) {
                Map<K, V> newMap = new HashMap<K, V>(internalMap);
                V val = newMap.put(key, value);
                internalMap = newMap;
                return true;
            }
            return false;
        }
    }

    /**
     * Inserts all the keys and values contained in the
     * provided map to this map.
     *
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends K, ? extends V> newData) {
        synchronized (this) {
            Map<K, V> newMap = new HashMap<K, V>(internalMap);
            newMap.putAll(newData);
            internalMap = newMap;
        }
    }
    /**
     * Removes all entries in this map.
     *
     * @see java.util.Map#clear()
     */
    public void clear() {
        synchronized (this) {
            internalMap = new HashMap<K, V>();
        }
    }
    // 
    //  Below are methods that do not modify 
    //          the internal Maps            
    /**
     * Returns the number of key/value pairs in this map.
     *
     * @see java.util.Map#size()
     */
    public int size() {
        return internalMap.size();
    }
    /**
     * Returns true if this map is empty, otherwise false.
     *
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return internalMap.isEmpty();
    }
    /**
     * Returns true if this map contains the provided key, otherwise
     * this method return false.
     *
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return internalMap.containsKey(key);
    }
    /**
     * Returns true if this map contains the provided value, otherwise
     * this method returns false.
     *
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return internalMap.containsValue(value);
    }
    /**
     * Returns the value associated with the provided key from this
     * map.
     *
     * @see java.util.Map#get(java.lang.Object)
     */
    public V get(Object key) {
        return internalMap.get(key);
    }
    /**
     * This method will return a read-only {@link Set}.
     */
    public Set<K> keySet() {
        return internalMap.keySet();
    }
    /**
     * This method will return a read-only {@link Collection}.
     */
    public Collection<V> values() {
        return internalMap.values();
    }
    /**
     * This method will return a read-only {@link Set}.
     */
    public Set<Entry<K, V>> entrySet() {
        return internalMap.entrySet();
    }
    
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}