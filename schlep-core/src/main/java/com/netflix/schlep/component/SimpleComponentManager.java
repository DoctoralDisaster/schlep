package com.netflix.schlep.component;

import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

public class SimpleComponentManager<T extends Component> {
    private final ConcurrentMap<String, T> components = Maps.newConcurrentMap();
    
    public void add(T component) throws Exception {
        if (null != components.putIfAbsent(component.getId(), component)) {
            throw new Exception("Already exists");
        }
    }
    
    public T remove(String id) {
        return components.remove(id);
    }
    
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
    
    protected T create(String id) throws Exception {
        throw new Exception("Component not found");
    }
}
