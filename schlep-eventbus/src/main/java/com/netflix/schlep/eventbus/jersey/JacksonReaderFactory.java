package com.netflix.schlep.eventbus.jersey;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.netflix.schlep.config.ConfigurationReader;

public class JacksonReaderFactory {
    private final ObjectMapper mapper;
    private final Injector     injector;
    
    @Inject
    public JacksonReaderFactory(Injector injector, ObjectMapper mapper) throws Exception {
        this.mapper   = mapper;
        this.injector = injector;
    }
    
    public ConfigurationReader wrap(final JsonNode node) {
        return new ConfigurationReader() {
            @Override
            public <T> T create(final Class<T> type) throws Exception {
                return mapper.readValue(node, type);
            }
        };
    }

}
