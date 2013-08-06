package com.netflix.schlep;

public interface QueueAdminProvider {
    public QueueAdmin get(String type);
}
