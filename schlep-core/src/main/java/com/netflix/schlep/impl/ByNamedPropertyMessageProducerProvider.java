package com.netflix.schlep.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.config.ConfigurationReader;
import com.netflix.schlep.exception.ProducerException;
import com.netflix.schlep.producer.MessageProducer;
import com.netflix.schlep.producer.MessageProducerFactory;

public class ByNamedPropertyMessageProducerProvider implements MessageProducerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ByNamedPropertyMessageProducerProvider.class);
    
    public static final String PROP_CONSUMER_TYPE = "%s.netflix.messaging.cloud.type";

    private final Map<String, MessageProducerFactory> providers;
    
    @Inject
    public ByNamedPropertyMessageProducerProvider(Map<String, MessageProducerFactory> providers) {
        this.providers = providers;
    }

    public <T> MessageProducer<T> createProducer(EndpointKey<T> key, ConfigurationReader mapper) throws ProducerException {
        LOG.info("Connecting producer for " + key);
        
        String propName = String.format(PROP_CONSUMER_TYPE, key.getName());
        String type = "sim"; // mapper.getObjectType();
        if (type == null) {
            throw new ProducerException(
                    "Producer type not specific for " + key.getName() 
                    + ". Expecting one of " + providers.keySet() + " in property " + propName);
        }
        MessageProducerFactory provider = providers.get(type);
        if (provider == null)
            throw new ProducerException(
                    "Producer type not found for " + key.getName() 
                    + ". Expecting one of " + providers.keySet() + " in property " + propName);
        
        return provider.createProducer(key, mapper);
    }
}
