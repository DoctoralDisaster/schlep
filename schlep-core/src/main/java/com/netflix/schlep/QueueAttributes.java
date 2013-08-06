package com.netflix.schlep;

import java.util.Map;

import com.google.common.collect.Maps;

public class QueueAttributes {
    private Map<String, String> attributes = Maps.newHashMap();
    
    public QueueAttributes withAttributes(Map<String, String> attributes) {
        attributes.putAll(attributes);
        return this;
    }
    
    public QueueAttributes withAttribute(String name, String value) {
        attributes.put(name, value);
        return this;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }
}
