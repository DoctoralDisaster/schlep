package com.netflix.schlep.reader;

import com.google.inject.Singleton;
import com.netflix.schlep.component.ConcurrentComponentManager;

/**
 * Registry of available MessageReader instances
 * 
 * @author elandau
 *
 */
@Singleton
public class MessageReaderManager extends ConcurrentComponentManager<String, MessageReader> {
}
