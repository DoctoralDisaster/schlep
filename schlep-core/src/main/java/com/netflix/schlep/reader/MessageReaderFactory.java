package com.netflix.schlep.reader;

import com.netflix.schlep.config.ConfigurationReader;
import com.netflix.schlep.exception.ConsumerException;

/**
 * Factory for creating an instance of a consumer from a configuration
 * 
 * @author elandau
 */
public interface MessageReaderFactory {
    /**
     * Create the underlying protocol layer to start consuming messages.
     * 
     * Must call asObservable and observe it to start receiving messages.
     * Note that each call to observe the Observable spawns a separate that
     * and that each thread will receive different events.
     * 
     * @param key       Identify source of events and message type
     * @param config    Initial configuration
     * @param callback  Callback to invoke for each message.
     * @return
     * @throws ConsumerException
     */
    public MessageReader createConsumer(
            String                  id, 
            ConfigurationReader     mapper) 
                    throws ConsumerException;
}
