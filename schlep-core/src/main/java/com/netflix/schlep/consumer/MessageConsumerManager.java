package com.netflix.schlep.consumer;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.netflix.schlep.component.SimpleComponentManager;
import com.netflix.schlep.exception.ConsumerException;

/**
 * Provider to get a message consumer.  The consumer is assumed to have already
 * been configured or will be extract from the default MessageConsumerProvider
 * 
 * Each consumer has a unique ID regardless of the messaging technology and
 * configuration used.
 * 
 * @author elandau
 *
 */
public class MessageConsumerManager extends SimpleComponentManager<MessageConsumer> {
    
    private DefaultMessageConsumerFactory factory;
    
    @Inject
    public MessageConsumerManager(DefaultMessageConsumerFactory factory) {
        this.factory = factory;
    }
    
    /*
     * Simple API for subscribing to incoming messages.  This call will either attach to a previously
     * configuration MessageConsumer or create a new one
     * 
     * <pre>
     *  public class Service {
     *      private MessageConsumer consumer;
     *      
     *      @Inject
     *      public Service(MessageConsumerManager manager) {
     *          this.consumer = manager.subscribe("consumername", new Function<Void, IncomingMessage>() {
     *              
     *          });
     *      }
     *  }
     */
    public MessageConsumer subscribe(String id, Function<Void, IncomingMessage> callback) throws ConsumerException {
        MessageConsumer consumer;
        try {
            consumer = this.get(id);
            return consumer;
        } catch (Exception e) {
            throw new ConsumerException(String.format("Error subscribing to '%s'", id), e);
        }
    }
    
    protected MessageConsumer create(String id) throws Exception {
        return factory.create(id);
    }
}
