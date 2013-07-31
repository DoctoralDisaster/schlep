package com.netflix.schlep.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.netflix.schlep.MessageCallback;
import com.netflix.schlep.MessageConsumer;
import com.netflix.schlep.MessageConsumerProvider;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.exception.ConsumerException;

public class BySchemaMessageConsumerProvider implements MessageConsumerProvider {
    private static final Logger LOG = LoggerFactory.getLogger(BySchemaMessageConsumerProvider.class);
            
    private final Map<String, MessageConsumerProvider> providers;
    
    @Inject
    public BySchemaMessageConsumerProvider(Map<String, MessageConsumerProvider> providers) {
        this.providers = providers;
    }
    
    public <T> MessageConsumer<T> subscribe(EndpointKey<T> key, MessageCallback<T> callback) throws ConsumerException {
        LOG.info("Creating consumer for " + key);
        URI uri;
        try {
            uri = new URI(key.getName());
        } catch (URISyntaxException e) {
            throw new ConsumerException("Invalid endoint URI " + key.getName(), e);
        }
        MessageConsumerProvider provider = providers.get(uri.getScheme());
        if (provider == null)
            throw new ConsumerException("Scheme not found for " + key.getName());
        return provider.subscribe(key, callback);
    }
    
}
