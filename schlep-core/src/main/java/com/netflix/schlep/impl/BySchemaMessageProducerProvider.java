package com.netflix.schlep.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.netflix.schlep.MessageProducer;
import com.netflix.schlep.MessageProducerProvider;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.exception.ProducerException;

public class BySchemaMessageProducerProvider implements MessageProducerProvider {
    private static final Logger LOG = LoggerFactory.getLogger(BySchemaMessageConsumerProvider.class);
    
    private final Map<String, MessageProducerProvider> providers;
    
    @Inject
    public BySchemaMessageProducerProvider(Map<String, MessageProducerProvider> providers) {
        this.providers = providers;
    }

    public <T> MessageProducer<T> getProducer(EndpointKey<T> key) throws ProducerException {
        LOG.info("Connecting producer for " + key);

        URI uri;
        try {
            uri = new URI(key.getName());
        } catch (URISyntaxException e) {
            throw new ProducerException("Invalid endpoint URI: " + key.getName(), e);
        }
        MessageProducerProvider provider = providers.get(uri.getScheme());
        return provider.getProducer(key);
    }
}
