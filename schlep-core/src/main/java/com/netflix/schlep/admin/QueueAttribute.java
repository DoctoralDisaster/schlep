package com.netflix.schlep.admin;

public interface QueueAttribute {
    public boolean isMutable();
    
    public boolean validate(String value);
}
