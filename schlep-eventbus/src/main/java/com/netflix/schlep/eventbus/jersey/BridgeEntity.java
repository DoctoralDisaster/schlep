package com.netflix.schlep.eventbus.jersey;

import org.codehaus.jackson.JsonNode;

public class BridgeEntity {
    private JsonNode configuration;
    private String eventType;
    private String producerType;
    private String id;
    private boolean autoStart = true;
    
    public JsonNode getConfiguration() {
        return configuration;
    }
    public void setConfiguration(JsonNode configuration) {
        this.configuration = configuration;
    }
    public BridgeEntity withConfiguration(JsonNode configuration) {
        this.configuration = configuration;
        return this;
    }
    
    public String getEventType() {
        return eventType;
    }
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    public BridgeEntity withEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }
    
    public String getProducerType() {
        return producerType;
    }
    public void setProducerType(String producerType) {
        this.producerType = producerType;
    }
    public BridgeEntity withProducerType(String producerType) {
        this.producerType = producerType;
        return this;
    }
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public BridgeEntity withId(String id) {
        this.id = id;
        return this;
    }
    
    public boolean isAutoStart() {
        return autoStart;
    }
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
    public BridgeEntity withAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
        return this;
    }
    @Override
    public String toString() {
        return "BridgeEntity [configuration=" + configuration + ", eventType="
                + eventType + ", producerType=" + producerType + ", id=" + id
                + ", autoStart=" + autoStart + "]";
    }
    
}
