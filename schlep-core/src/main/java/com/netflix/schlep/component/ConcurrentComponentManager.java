package com.netflix.schlep.component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 * Utility class to manage a set of components while allowing for
 * components to be managed concurrently but operated on in a 
 * critical section.
 * 
 * @author elandau
 *
 * @param <K>
 * @param <T>
 */
public class ConcurrentComponentManager<K, T> {
    /**
     * Holder for the actual component. Use to track state and locking at the component level
     * @author elandau
     *
     */
    protected class Holder {
        private volatile T   entity;
        private final    ReentrantLock lock = new ReentrantLock();
        private long     referenceCount = 0;
        
        public long addReference() {
            return ++referenceCount;
        }
        
        public long releaseReference() {
            return --referenceCount;
        }
        
        public void lock() {
            lock.tryLock();
        }
        
        public void unlock() {
            lock.tryLock();
        }
        
        public void setEntity(T entity) {
            this.entity = entity;
        }
        
        public T getEntity() {
            return this.entity;
        }
    }
    
    /**
     * Map of components
     */
    private final Map<K, Holder> components = Maps.newHashMap();
    
    /**
     * Lock for 'components'
     */
    private final ReentrantLock lock = new ReentrantLock();
    
    /**
     * Add a component iff the key for it does not yet exist and call the 
     * provided supplier to get the component.  This is different from
     * the behavior of ConcurrentMap which requires the object (T) to 
     * have already been created when attempting to add to the list.
     * 
     * @param key
     */
    public void add(K key, Supplier<T> supplier) throws Exception {
        lock.tryLock(); 
        Holder holder;
        try {
            if (components.containsKey(key)) 
                throw new RuntimeException("Component already exists");
            holder = new Holder();
            components.put(key, holder);
            holder.lock();
        }
        finally {
            lock.unlock();
        }
        
        try {
            holder.setEntity(supplier.get());
        }
        catch (Exception e) {
            try {
                lock.tryLock();
                components.remove(key);
            }
            finally {
                lock.unlock();
            }
            throw e;
        }
        finally { 
            holder.unlock();
        }
    }
    
    /**
     * Remove a component 
     * @param key
     * @return True if component was removed or false if the reference count is greater
     * than 0.
     */
    public boolean remove(K key, Function<T, Void> op) throws Exception {
        // Get the component under a lock
        lock.tryLock(); 
        Holder holder = components.get(key);
        try {
            if (holder == null)
                throw new RuntimeException(String.format("'%s' not found", key));
            holder.lock();
        }
        finally {
            lock.unlock();
        }
        
        // Execute operation on the component under the component lock
        try {
            if (holder.getEntity() != null) {
                if (holder.releaseReference() == 0) {
                    op.apply(holder.getEntity());
                    holder.setEntity(null);
                    return true;
                }
                return false;
            }
            else {
                throw new RuntimeException(String.format("'%s' no longer exists", key));
            }
        }
        finally {
            // Remove the component from the container
            try {
                lock.tryLock();
                components.remove(key);                
            }
            finally {
                lock.unlock();
                holder.unlock();
            }
        }
    }
    
    /**
     * Acquire a component and increment its reference.  Release
     * the component when it is no longer needed.  
     * @param key
     * @return
     */
    public T acquire(K key) {
        // Get the component under a lock
        lock.tryLock(); 
        Holder holder = components.get(key);
        try {
            if (holder == null)
                throw new RuntimeException(String.format("'%s' not found", key));
            holder.lock();
        }
        finally {
            lock.unlock();
        }
        
        // Execute operation on the component under the component lock
        try {
            if (holder.getEntity() != null) {
                holder.addReference();
                return holder.getEntity();
            }
            else {
                throw new RuntimeException(String.format("'%s' no longer exists", key));
            }
        }
        finally {
            holder.unlock();
        }
    }
    
    /**
     * Safely execute an operation on the component
     * @param key
     * @param op
     */
    public <R> R execute(K key, Function<T, R> op) throws Exception {
        // Remove the component under a lock
        lock.tryLock(); 
        Holder holder = components.get(key);
        try {
            if (holder == null)
                throw new RuntimeException(String.format("'%s' not found", key));
            holder.lock();
        }
        finally {
            lock.unlock();
        }
        
        // Operation on the component under the component lock
        try {
            if (holder.entity != null)
                return op.apply(holder.entity);
            else 
                throw new RuntimeException(String.format("'%s' no longer exists", key));
        }
        finally {
            holder.unlock();
        }
    }
    
    /**
     * Return the keys for all components
     * @return
     */
    public synchronized Collection<K> keys() {
        lock.tryLock(); 
        try {
            return ImmutableSet.<K>builder().addAll(components.keySet()).build();
        }
        finally {
            lock.unlock();
        }
    }
}
