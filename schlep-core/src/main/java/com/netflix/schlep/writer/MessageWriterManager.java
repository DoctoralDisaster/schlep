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
public class MessageWriterManager extends ConcurrentComponentManager<String, MessageWriter> {
}
