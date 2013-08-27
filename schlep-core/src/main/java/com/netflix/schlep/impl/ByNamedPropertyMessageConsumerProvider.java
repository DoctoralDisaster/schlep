package com.netflix.schlep.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.config.ConfigurationReader;
import com.netflix.schlep.consumer.MessageCallback;
import com.netflix.schlep.consumer.MessageConsumer;
import com.netflix.schlep.consumer.MessageConsumerFactory;
import com.netflix.schlep.exception.ConsumerException;

public class ByNamedPropertyMessageConsumerProvider implements MessageConsumerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ByNamedPropertyMessageConsumerProvider.class);
            
    private final Map<String, MessageConsumerFactory> providers;
    
    @Inject
    public ByNamedPropertyMessageConsumerProvider(Map<String, MessageConsumerFactory> providers) {
        this.providers = providers;
    }
    
    public <T> MessageConsumer<T> createSubscriber(EndpointKey<T> key, ConfigurationReader mapper, MessageCallback<T> callback) throws ConsumerException {
        LOG.info("Connecting consumer for " + key);
        
        String type = "sim"; // mapper.getObjectType();
        if (type == null) {
            throw new ConsumerException(
                    "Consumer type not specific for " + key.getName()
                    + ". Expecting one of " + providers.keySet());
        }
        MessageConsumerFactory provider = providers.get(type);
        if (provider == null) {
            throw new ConsumerException(
                    "Consumer prtype not found for " + key.getName() 
                    + ". Expecting one of " + providers.keySet());
        }
        return provider.createSubscriber(key, mapper, callback);
    }
    
}
