package com.netflix.schlep.batch;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

@Deprecated
public class BlockingQueueBatcher<T> {
//    private final int  batchSize;
//    private final long batchDelayInMillis;
//    private final BlockingQueue<Collection<T>> queue;
//    
//    private List<T> entries = Lists.newArrayList();
//    
//    private final static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
//            new ThreadFactoryBuilder()
//            .setDaemon(true)
//            .setNameFormat("Batcher-%d")
//            .build());
//    
//    public BlockingQueueBatcher(int batchSize, long batchDelay, TimeUnit units, BlockingQueue<Collection<T>> queue) {
//        this.batchDelayInMillis = TimeUnit.MILLISECONDS.convert(batchDelay, units);
//        this.batchSize  = batchSize;
//        this.queue      = queue;
//    }
//    
//    public synchronized void add(T entry) throws InterruptedException {
//        entries.add(entry);
//        
//        // First item in the batch.  Start the timer.
//        if (entries.size() == 1) {
//            deleteAfterDelay(entries);
//        }
//        // Reached the batch so dispatch it
//        else if (entries.size() == batchSize) {
//            queue.put(entries);
//            this.entries = Lists.newArrayList();
//        }
//        else {
//            // Continue building the batch
//        }
//    }
//    
//    /**
//     * Start a time that will delete the current batch only if the max delay
//     * time has elapsed and the events haven't been acked yet.
//     * @param entries
//     */
//    private void deleteAfterDelay(final List<T> entries) {
//        executor.schedule(new Callable<Boolean>() {
//            @Override
//            public Boolean call() throws Exception {
//                // TODO: What about the exception?
//                return deleteIfCurrentBatch(entries);
//            }
//        },  batchDelayInMillis, TimeUnit.MILLISECONDS);
//    }
//    
//    /**
//     * Delete only if this is the current batch
//     * @param entries
//     * @throws InterruptedException 
//     */
//    private synchronized Boolean deleteIfCurrentBatch(final List<T> entries) throws InterruptedException {
//        if (entries == this.entries) {
//            queue.put(entries);
//            this.entries = Lists.newArrayList();
//            return true;
//        }
//        return false;
//    }
}
