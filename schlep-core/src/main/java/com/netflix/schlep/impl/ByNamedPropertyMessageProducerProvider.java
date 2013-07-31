package com.netflix.schlep.impl;

import java.util.Map;

import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.MessageProducer;
import com.netflix.schlep.MessageProducerProvider;
import com.netflix.schlep.exception.ProducerException;

public class ByNamedPropertyMessageProducerProvider implements MessageProducerProvider {
    private static final Logger LOG = LoggerFactory.getLogger(BySchemaMessageConsumerProvider.class);
    
    public static final String PROP_CONSUMER_TYPE = "%s.netflix.messaging.cloud.type";

    private final Map<String, MessageProducerProvider> providers;
    private final AbstractConfiguration                config;
    
    @Inject
    public ByNamedPropertyMessageProducerProvider(Map<String, MessageProducerProvider> providers, AbstractConfiguration config) {
        this.providers = providers;
        this.config    = config;
    }

    public <T> MessageProducer<T> getProducer(EndpointKey<T> key) throws ProducerException {
        LOG.info("Connecting producer for " + key);
        
        String propName = String.format(PROP_CONSUMER_TYPE, key.getName());
        String type = config.getString(propName);
        if (type == null) {
            throw new ProducerException(
                    "Consumer type not specific for " + key.getName() 
                    + ". Expecting one of " + providers.keySet() + " in property " + propName);
        }
        MessageProducerProvider provider = providers.get(type);
        if (provider == null)
            throw new ProducerException(
                    "Consumer prtype not found for " + key.getName() 
                    + ". Expecting one of " + providers.keySet() + " in property " + propName);
        
        return provider.getProducer(key);
    }
}
