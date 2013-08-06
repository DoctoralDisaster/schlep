package com.netflix.schlep;

import java.util.Collection;
import java.util.List;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Abstraction for queue admin operation
 * 
 * @author elandau
 *
 */
public interface QueueAdmin {
    public ListenableFuture<Boolean> createQueue(String uri, QueueAttributes attributes);
    
    public ListenableFuture<Boolean> deleteQueue(String uri);
    
    public ListenableFuture<List<String>> listQueues();

    public ListenableFuture<QueueAttributes> getQueueAttributes(String uri);
    
    public ListenableFuture<QueueAttributes> getQueueAttributes(String uri, Collection<String> attributes);

    public ListenableFuture<Void> updateQueueAttributes(String uri, QueueAttributes attributes);
    
    public ListenableFuture<Long> getQueueMessageCount(String uri);
}
