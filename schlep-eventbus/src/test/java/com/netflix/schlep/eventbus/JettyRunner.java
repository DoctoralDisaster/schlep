package com.netflix.schlep.eventbus;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.schlep.eventbus.jersey.BridgeAdminResource;
import com.netflix.schlep.eventbus.jersey.JsonMessageBodyReader;
import com.netflix.schlep.eventbus.jersey.JsonMessageBodyWriter;
import com.netflix.schlep.guice.SchlepModule;
import com.netflix.schlep.sim.SimSchlepModule;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class JettyRunner {
    private static final Logger LOG = LoggerFactory.getLogger(JettyRunner.class);
    
    public static void main(String[] args) throws Exception {
        final List<AbstractModule> modules = Lists.newArrayList(
                new SimSchlepModule(),
                new SchlepModule(),
                new EventBusModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                    }
                },
                new JerseyServletModule() {
                    @Override
                    protected void configureServlets() {
                        LOG.info("Binding jersey servlets");
                        bind(BridgeAdminResource.class).asEagerSingleton();
                        bind(JsonMessageBodyReader.class).asEagerSingleton();
                        bind(JsonMessageBodyWriter.class).asEagerSingleton();
                        
                        // Route all requests through GuiceContainer
                        bind(GuiceContainer.class).asEagerSingleton();
                        serve("/*").with(GuiceContainer.class);
                    }
                }
            );
            
        LOG.info("Starting application");
        
        // Create the server.
        Server server = new Server(8080);
        ServletContextHandler sch = new ServletContextHandler(server, "/");
         
        final Injector injector = LifecycleInjector.builder()
                .withModules(modules)
                .createInjector();
        
        // Add our Guice listener that includes our bindings
        sch.addEventListener(new GuiceServletContextListener() {
                @Override
                protected Injector getInjector() {
                    return injector;
                }
        });
        
        sch.addFilter(GuiceFilter.class,     "/*", null);
        sch.addServlet(DefaultServlet.class, "/");
         
        LifecycleManager manager = injector.getInstance(LifecycleManager.class);
        try {
            manager.start();
            
            // Start the server
            server.start();
            server.join();
        }
        finally {
            LOG.info("Stopping application");
            Closeables.close(manager, true);
        }
    }
}
