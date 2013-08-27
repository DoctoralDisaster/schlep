package com.netflix.schlep.eventbus.jersey;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.eventbus.spi.EventBus;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.eventbus.EventBusSchlepBridge;
import com.netflix.schlep.eventbus.EventBusSchlepBridgeManager;
import com.netflix.schlep.exception.ProducerException;
import com.netflix.schlep.producer.MessageProducerFactory;

@Singleton
@Path("/schlep")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BridgeAdminResource {
    private final static Logger LOG = LoggerFactory.getLogger(BridgeAdminResource.class);
    
    private final EventBusSchlepBridgeManager bridges;
    private final Map<String, MessageProducerFactory>     producers;
    private final JacksonReaderFactory                    readerFactory;
    private final EventBus                                eventBus;
    
    @Inject
    public BridgeAdminResource(
            EventBus                            eventBus,
            Map<String, MessageProducerFactory> producers,
            EventBusSchlepBridgeManager         bridges,
            JacksonReaderFactory                readerFactory) {
        this.bridges       = bridges;
        this.producers     = producers;
        this.readerFactory = readerFactory;
        this.eventBus      = eventBus;
    }
    
    /**
     * @return  Return a list of all register consumers by name
     */
    @GET
    public List<String> listBridges() {
        return Lists.newArrayList(bridges.keys());
    }
    
    /**
     * Add a 
     * @param data
     * @throws Exception
     */
    @POST
    public void addBridge(final BridgeEntity entity) throws Exception {
        Preconditions.checkNotNull(entity.getConfiguration(), "Missing configuration");
        
        // Check types and create producer
        final String type = entity.getProducerType();
        if (type == null) {
            throw new ProducerException(
                    String.format("Producer type not specified for '%s'. Expecting one of %s.",
                            entity.getId(), producers.keySet()));
        }
        
        final MessageProducerFactory provider = producers.get(type);
        if (provider == null)
            throw new ProducerException(
                    String.format("Producer type '%s' not found for '%s'. Expecting one of %s.",
                            entity.getProducerType(), entity.getId(), producers.keySet()));
        final Class<?> eventClass;
        try {
            eventClass = Class.forName(entity.getEventType());
        }
        catch (Exception e) {
            throw new RuntimeException(
                    String.format("Event type '%s' not found for '%s'",
                            entity.getEventType(), entity.getId()));
        }
        
        bridges.addComponent(entity.getId(), new Supplier<EventBusSchlepBridge<?>>() {
            @Override
            public EventBusSchlepBridge<?> get() {
                try {
                    EventBusSchlepBridge<?> bridge = new EventBusSchlepBridge(
                            eventBus, 
                            provider.createProducer(
                                    EndpointKey.of(entity.getId(), eventClass), 
                                    readerFactory.wrap(entity.getConfiguration())));
                    if (entity.isAutoStart()) {
                        bridge.start();
                    }
                    return bridge;
                } catch (Exception e) {
                    LOG.info("Failed to load schelp event bus bridge for configuration: " + entity.toString());
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @DELETE
    @Path("{name}")
    public void deleteBridge(@PathParam("name") String name) throws Exception {
        bridges.removeComponent(name, new Function<EventBusSchlepBridge<?>, Void>() {
            public Void apply(@Nullable EventBusSchlepBridge<?> bridge) {
                bridge.stop();
                return null;
            }
        });
    }
    
    @GET
    @Path("{name}/start")
    public void startBridge(@PathParam("name") String name) throws Exception {
        bridges.executeOperation(name, new Function<EventBusSchlepBridge<?>, Void>() {
            public Void apply(@Nullable EventBusSchlepBridge<?> bridge) {
                try {
                    bridge.start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }
    
    @GET
    @Path("{name}/pause")
    public void pauseBridge(@PathParam("name") String name) throws Exception {
        bridges.executeOperation(name, new Function<EventBusSchlepBridge<?>, Void>() {
            public Void apply(@Nullable EventBusSchlepBridge<?> bridge) {
                bridge.pause();
                return null;
            }
        });
    }
    
    @GET
    @Path("{name}/resume")
    public void resumeBridge(@PathParam("name") String name) throws Exception {
        bridges.executeOperation(name, new Function<EventBusSchlepBridge<?>, Void>() {
            public Void apply(@Nullable EventBusSchlepBridge<?> bridge) {
                bridge.resume();
                return null;
            }
        });
    }
}
