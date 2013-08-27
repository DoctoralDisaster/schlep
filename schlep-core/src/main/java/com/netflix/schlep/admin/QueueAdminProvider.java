package com.netflix.schlep.admin;

public interface QueueAdminProvider {
    public QueueAdmin get(String type);
}
