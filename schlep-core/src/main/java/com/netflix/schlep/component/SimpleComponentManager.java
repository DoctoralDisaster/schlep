package com.netflix.schlep.component;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

public class SimpleComponentManager<T extends Component> {
    private final ConcurrentMap<String, T> components = Maps.newConcurrentMap();
    
    /**
     * Add a component 
     * @param component
     * @throws Exception
     */
    public void add(T component) throws Exception {
        if (null != components.putIfAbsent(component.getId(), component)) {
            throw new Exception("Already exists");
        }
    }
    
    /**
     * Remove a component
     * @param id
     * @return
     */
    public T remove(String id) {
        return components.remove(id);
    }
    
    /**
     * Get a component or create a new one if it doesnt already exist
     * @param id
     * @return
     * @throws Exception
     */
    public T get(String id) throws Exception {
        T c = components.get(id);
        if (c == null) {
            c = create(id);
            T prev = components.putIfAbsent(id, c);
            if (prev != null) {
                return prev;
            }
        }
        return c;
    }
    
    /**
     * Find a component with Id or throw an exception if not found
     * @param id
     * @return
     * @throws Exception
     */
    public T find(String id) throws Exception {
        T component = components.get(id);
        if (component == null)
            throw new Exception("Component not found");
        return component;
    }
    
    /**
     * Template method for creating a Component that has not be registered yet
     * 
     * @param id
     * @return
     * @throws Exception
     */
    protected T create(String id) throws Exception {
        throw new Exception("Component not found");
    }
}
