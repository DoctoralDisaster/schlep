package com.netflix.schlep.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.netflix.schlep.config.ConfigurationReader;
import com.netflix.schlep.exception.ConsumerException;
import com.netflix.schlep.reader.MessageReader;
import com.netflix.schlep.reader.MessageReaderFactory;

public class ByNamedPropertyMessageReaderProvider implements MessageReaderFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ByNamedPropertyMessageReaderProvider.class);
            
    private final Map<String, MessageReaderFactory> providers;
    
    @Inject
    public ByNamedPropertyMessageReaderProvider(Map<String, MessageReaderFactory> providers) {
        this.providers = providers;
    }
    
    public MessageReader createConsumer(String id, ConfigurationReader mapper) throws ConsumerException {
        LOG.info("Connecting consumer for " + id);
        
        String type = "sim"; // mapper.getObjectType();
        if (type == null) {
            throw new ConsumerException(
                    "Consumer type not specific for " + id
                    + ". Expecting one of " + providers.keySet());
        }
        MessageReaderFactory provider = providers.get(type);
        if (provider == null) {
            throw new ConsumerException(
                    "Consumer prtype not found for " + id 
                    + ". Expecting one of " + providers.keySet());
        }
        return provider.createConsumer(id, mapper);
    }
    
}
