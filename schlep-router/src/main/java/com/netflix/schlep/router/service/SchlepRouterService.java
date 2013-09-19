package com.netflix.schlep.router.service;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.netflix.schlep.processor.MessageProcessor;
import com.netflix.schlep.reader.IncomingMessage;
import com.netflix.schlep.reader.MessageReader;
import com.netflix.schlep.router.MessageRouter;
import com.netflix.schlep.writer.Completion;
import com.netflix.schlep.writer.MessageWriter;

/**
 * Reader -> Dispatcher -> Writer
 * 
 * The routing service has an independent collection of sources and sinks that
 * are joined by a collection of route, which include filters.  A source may 
 * provide data to multiple sync and a writer may receive input from multiple
 * sources.
 * 
 * @author elandau
 *
 */
public class SchlepRouterService {
    private static Logger LOG = LoggerFactory.getLogger(SchlepRouterService.class);
        
    private final Map<String, MessageWriter> sinks   = Maps.newHashMap();
    private final Map<String, SourceContext> sources = Maps.newHashMap();

    public static class SourceContext {
        private MessageRouter router;
        private MessageReader reader;
        
        public SourceContext(MessageReader reader) {
            this.reader = reader;
            this.router = new MessageRouter(Observable.create(reader));
            this.router.addProcessor("$", new MessageProcessor() {
                @Override
                public Observable<Completion<IncomingMessage>> process(IncomingMessage message) {
                    return Observable.just(Completion.from(message));
                }
            });
        }

        public void addProcessor(String routeId, MessageProcessor processor) {
            router.addProcessor(routeId, processor);
        }
        
        public void removeProcessor(String routeId) {
            router.removeProcessor(routeId);
        }
    }
    
    public SchlepRouterService() {
    }
    
    @PostConstruct
    public void init() {
    }
    
    @PreDestroy 
    public void shutdown() {
        
    }
    
    /**
     * Add a sink into the system.  If a previous sink of the same name already exists then 
     * replace it with the current sink.
     * 
     * @param id
     * @param sink
     */
    public synchronized void addSink(MessageWriter sink) {
        MessageWriter prev = sinks.get(sink.getId());
        if (prev != null) {
            // TODO: Remove reader from all routers/processors and hook up the new reader
        }
        sinks.put(sink.getId(), sink);
    }
    
    /**
     * Add a source to the router service.  Note that data will not be read from the source until
     * someone subscribes to it.
     * @param source
     */
    public synchronized void addSource(MessageReader source) {
        LOG.info("Adding source : " + source.getId());
        SourceContext prev = sources.get(source.getId());
        sources.put(source.getId(), new SourceContext(source));
        
        if (prev != null) {
            LOG.error("Need to remove old source : " + source.getId());
        }
    }
    
    public synchronized void addRoute(String fromSource, String toSink, String routeId, Predicate<IncomingMessage> predicate) {
        LOG.info(String.format("Adding route '%s' -> '%s' using filter '%s'", fromSource, toSink, routeId));
        SourceContext source = sources.get(fromSource);
        if (source == null) {
            throw new RuntimeException(String.format("Source '%s' not found", fromSource));
        }
        MessageWriter sink = sinks.get(toSink);
        if (sink == null) {
            throw new RuntimeException(String.format("Sink '%s' not found", fromSource));
        }
        source.addProcessor(routeId, new PredicateMessageProcessor(sink, predicate));
    }

    public synchronized void removeRoute(String fromSource, String routeId) {
        LOG.info(String.format("Removing route '%s' from source '%s'", routeId, fromSource));
        SourceContext source = sources.get(fromSource);
        if (source == null) {
            throw new RuntimeException(String.format("Source '%s' not found", fromSource));
        }
        source.removeProcessor(routeId);    
    }
}
