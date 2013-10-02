package com.netflix.schlep.consumer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.schlep.component.SimpleComponentManager;

/**
 * Registry of available MessageConsumer instances.  Uses a MessageConsumerProvider to
 * create a new MessageConsumer if one does not exist.  The MessageConsumerProvider is
 * normally tied to a specific configuration mechanism, such as properties, or JSON.
 * 
 * @author elandau
 *
 */
@Singleton
public class MessageConsumerRegistry extends SimpleComponentManager<MessageConsumer>{
    private final MessageConsumerProvider provider;
    
    @Inject
    public MessageConsumerRegistry(MessageConsumerProvider provider) {
        this.provider = provider;
    }
    
    protected MessageConsumer create(String id) throws Exception {
        return provider.get(id);
    }
}
