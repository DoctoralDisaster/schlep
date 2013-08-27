package com.netflix.schlep.eventbus;

import com.netflix.schlep.component.ConcurrentComponentManager;

public class EventBusSchlepBridgeManager 
    extends ConcurrentComponentManager<
        String, 
        EventBusSchlepBridge<?>> {

}
