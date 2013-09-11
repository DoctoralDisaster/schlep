package com.netflix.schlep.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.netflix.schlep.config.ConfigurationReader;
import com.netflix.schlep.exception.ProducerException;
import com.netflix.schlep.writer.MessageWriterFactory;
import com.netflix.schlep.writer.MessageWriter;

public class ByNamedPropertyMessageWriterProvider implements MessageWriterFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ByNamedPropertyMessageWriterProvider.class);
    
    public static final String PROP_CONSUMER_TYPE = "%s.netflix.messaging.cloud.type";

    private final Map<String, MessageWriterFactory> providers;
    
    @Inject
    public ByNamedPropertyMessageWriterProvider(Map<String, MessageWriterFactory> providers) {
        this.providers = providers;
    }

    public MessageWriter createProducer(String id, ConfigurationReader mapper) throws ProducerException {
        LOG.info("Connecting producer for " + id);
        
        String propName = String.format(PROP_CONSUMER_TYPE, id);
        String type = "sim"; // mapper.getObjectType();
        if (type == null) {
            throw new ProducerException(
                    "Producer type not specific for " + id 
                    + ". Expecting one of " + providers.keySet() + " in property " + propName);
        }
        MessageWriterFactory provider = providers.get(type);
        if (provider == null)
            throw new ProducerException(
                    "Producer type not found for " + id 
                    + ". Expecting one of " + providers.keySet() + " in property " + propName);
        
        return provider.createProducer(id, mapper);
    }
}
