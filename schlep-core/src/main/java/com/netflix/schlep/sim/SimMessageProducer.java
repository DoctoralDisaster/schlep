package com.netflix.schlep.sim;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.schlep.producer.ConcurrentMessageProducer;

/**
 * Simulated message writer
 * 
 * @author elandau
 *
 */
public class SimMessageProducer extends ConcurrentMessageProducer {
    private static final Logger LOG = LoggerFactory.getLogger(SimMessageProducer.class);
    
    public static abstract class Builder<T extends Builder<T>> extends ConcurrentMessageProducer.Builder<T> {
        private long     writeDelay         = 100;
        
        public T withWriteDelay(long writeDelay, TimeUnit units) {
            this.writeDelay = units.toMillis(writeDelay);
            return self();
        }
        
        public SimMessageProducer build() {
            return new SimMessageProducer(this);
        }

    }
    
    /**
     * BuilderWrapper to link with subclass Builder
     * @author elandau
     *
     */
    private static class BuilderWrapper extends Builder<BuilderWrapper> {
        @Override
        protected BuilderWrapper self() {
            return this;
        }
    }
    
    public static Builder<?> builder() {
        return new BuilderWrapper();
    }
        
    private final long writeDelay;
    
    protected SimMessageProducer(final Builder<?> init) {
        super(init);
        
        this.writeDelay = init.writeDelay;
    }
    
    @Override
    protected void sendMessages(List<ObservableCompletion> messages) {
        // Introduce a delay
        try {
            Thread.sleep(writeDelay);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        
        // Ack each message
        for (ObservableCompletion completion : messages) {
            try {
                LOG.info("Write: " + completion.getValue());
            }
            catch(Throwable t) {
                LOG.error(t.getMessage(), t);
                completion.setError(t);
            }
            completion.done();
        }
    }
}
