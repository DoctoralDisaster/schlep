package com.netflix.schlep.impl;

import java.util.Map;

import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.MessageCallback;
import com.netflix.schlep.MessageConsumer;
import com.netflix.schlep.MessageConsumerProvider;
import com.netflix.schlep.exception.ConsumerException;

public class ByNamedPropertyMessageConsumerProvider implements MessageConsumerProvider {
    private static final Logger LOG = LoggerFactory.getLogger(BySchemaMessageConsumerProvider.class);
            
    public static final String PROP_CONSUMER_TYPE = "%s.netflix.messaging.cloud.type";
    
    private final Map<String, MessageConsumerProvider> providers;
    private final AbstractConfiguration                config;
    
    @Inject
    public ByNamedPropertyMessageConsumerProvider(Map<String, MessageConsumerProvider> providers, AbstractConfiguration config) {
        this.providers = providers;
        this.config    = config;
    }
    
    public <T> MessageConsumer<T> subscribe(EndpointKey<T> key, MessageCallback<T> callback) throws ConsumerException {
        LOG.info("Connecting consumer for " + key);
        
        String propName = String.format(PROP_CONSUMER_TYPE, key.getName());
        String type = config.getString(propName);
        if (type == null) {
            throw new ConsumerException(
                    "Consumer type not specific for " + key.getName() 
                    + ". Expecting one of " + providers.keySet() + " in property " + propName);
        }
        MessageConsumerProvider provider = providers.get(type);
        if (provider == null)
            throw new ConsumerException(
                    "Consumer prtype not found for " + key.getName() 
                    + ". Expecting one of " + providers.keySet() + " in property " + propName);
        return provider.subscribe(key, callback);
    }
    
}
