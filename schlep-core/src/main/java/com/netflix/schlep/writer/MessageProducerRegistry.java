package com.netflix.schlep.writer;

import com.google.inject.Singleton;
import com.netflix.schlep.component.ConcurrentComponentManager;

/**
 * Registry for MessageWriter instances
 * 
 * @author elandau
 *
 */
@Singleton
public class MessageProducerRegistry extends ConcurrentComponentManager<String, MessageWriter> {
}
