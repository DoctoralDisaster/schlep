package com.netflix.schlep.eventbus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;
import com.netflix.eventbus.spi.DynamicSubscriber;
import com.netflix.eventbus.spi.EventBus;
import com.netflix.eventbus.spi.Subscribe;
import com.netflix.schlep.exception.ProducerException;
import com.netflix.schlep.producer.MessageProducer;

/**
 * Bridge between an EventBus consumer and a Schlep Producer
 * @author elandau
 *
 */
public class EventBusSchlepBridge<T> implements DynamicSubscriber {
    private final static Logger LOG = LoggerFactory.getLogger(EventBusSchlepBridge.class);
    
    private final EventBus           eventBus;
    private final MessageProducer<T> producer;
    private volatile boolean         paused     = false;    // If true then drop events
    
    public EventBusSchlepBridge(
                      EventBus eventBus, 
            @Assisted MessageProducer<T> producer) {
        this.eventBus = eventBus;
        this.producer = producer;
    }
    
    public void start() throws Exception {
        LOG.info("Start");
        eventBus.registerSubscriber(this);
    }
    
    public void stop() {
        LOG.info("Stop");
        eventBus.unregisterSubscriber(this);
    }
    
    public void pause() {
        LOG.info("Pause");
        paused = true;
    }
    
    public void resume() {
        LOG.info("Resume");
        paused = false;
    }
    
    @Subscribe
    private void consume(T event) {
        LOG.info("Consume " + event);
        if (paused == true) 
            return;
        try {
            producer.produce(event);
        } catch (ProducerException e) {
            LOG.error("Error briding message", e);
            // TODO: Count or report or whatever
        }
    }

    @Override
    public Class<?> getEventType() {
        LOG.info("Type: " + producer.getMessageType());
        return producer.getMessageType();
    }
}
