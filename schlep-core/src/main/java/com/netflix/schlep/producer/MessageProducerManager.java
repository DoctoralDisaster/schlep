package com.netflix.schlep.producer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.schlep.component.SimpleComponentManager;

/**
 * Registry for MessageProducer instances
 * 
 * @author elandau
 *
 */
@Singleton
public class MessageProducerManager extends SimpleComponentManager<MessageProducer> {
    private final DefaultMessageProducerFactory factory;
    
    @Inject
    public MessageProducerManager(DefaultMessageProducerFactory provider) {
        this.factory = provider;
    }
    
    protected MessageProducer create(String id) throws Exception {
        return factory.create(id);
    }
}
