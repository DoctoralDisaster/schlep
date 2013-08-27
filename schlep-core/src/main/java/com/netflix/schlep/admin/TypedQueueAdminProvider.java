package com.netflix.schlep.admin;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TypedQueueAdminProvider implements QueueAdminProvider {

    private final Map<String, Provider<QueueAdmin>> admins;
    
    @Inject
    public TypedQueueAdminProvider(Map<String, Provider<QueueAdmin>> admins) {
        this.admins = admins;
    }
    
    @Override
    public QueueAdmin get(String type) {
        Preconditions.checkArgument(admins.containsKey(type), String.format("Unknown queue type '%s' expected one of '%s'", type, admins.keySet()));
        return admins.get(type).get();
    }

}
