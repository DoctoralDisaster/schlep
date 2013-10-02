package com.netflix.schlep.consumer;

import com.google.common.base.Function;
import com.google.inject.Singleton;
import com.netflix.schlep.exception.ConsumerException;

/**
 * The SimpleConsumer provides a very simple API to consuming messages.
 * 
 * <pre>
 *  public class Service {
 *      private MessageConsumer consumer;
 *      
 *      @Inject
 *      public Service(SimpleConsumerManager manager) {
 *          this.consumer = manager.subscribe("consumername", new Function<Void, IncomingMessage>() {
 *              
 *          });
 *      }
 *  }
 * </pre>
 * @author elandau
 *
 */
@Singleton
public class SimpleConsumerManager {
    private MessageConsumerProvider provider;
    
    public SimpleConsumerManager() {
        
    }
    
    /**
     * Subscribe for message notification
     * @param foo
     */
    public MessageConsumer subscribe(String id, Function<Void, IncomingMessage> callback) throws ConsumerException {
        MessageConsumer consumer = provider.get(id);
        return consumer;
    }
}
