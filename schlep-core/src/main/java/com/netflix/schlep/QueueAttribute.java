package com.netflix.schlep;

public interface QueueAttribute {
    public boolean isMutable();
    
    public boolean validate(String value);
}
