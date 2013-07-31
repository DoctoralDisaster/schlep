package com.netflix.schlep.sqs.ack;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.netflix.schlep.sqs.SqsClient;

// Acking strategy that batches requests
public class SqsBatchAckStrategy {
//    private static final Logger LOG = LoggerFactory.getLogger(SqsBatchAckStrategy.class);
//            
//    private static final int  DEFAULT_BATCH_SIZE      = 50;
//    private static final long DEFAULT_MAX_DELAY       = TimeUnit.SECONDS.toMillis(5);
//    private static final int  DEFAULT_BATCH_THREADS   = 1;
//
//    private final int  batchSize      = DEFAULT_BATCH_SIZE;
//    private final long batchDelay     = DEFAULT_MAX_DELAY;
//    private final int  batchThreads   = DEFAULT_BATCH_THREADS;
//    
//    private List<DeleteMessageBatchRequestEntry> entries = Lists.newArrayList();
//    
//    private final ScheduledExecutorService executor;
//    private final SqsClient                client;
//    
//    @Inject
//    public SqsBatchAckStrategy(
//                      AbstractConfiguration config, 
//            @Assisted SqsClient client) {
//        this.client    = client;
//        
//        this.executor = Executors.newScheduledThreadPool(
//                batchThreads, 
//                new ThreadFactoryBuilder()
//                    .setDaemon(true)
//                    .setNameFormat("SqsBatchDelete-" + client.getQueueName() + "-%d")
//                    .build());
//    }
//    
//    @Override
//    public synchronized void ack(Message message) {
//        entries.add(
//                new DeleteMessageBatchRequestEntry()
//                    .withId(message.getMessageId())
//                    .withReceiptHandle(message.getReceiptHandle()));
//        
//        // First item in the batch.  Start the timer.
//        if (entries.size() == 1) {
//            deleteAfterDelay(entries);
//        }
//        else if (entries.size() == batchSize) {
//            enqueDeleteBatch(entries);
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
//    private void deleteAfterDelay(final List<DeleteMessageBatchRequestEntry> entries) {
//        executor.schedule(new Callable<Boolean>() {
//            @Override
//            public Boolean call() throws Exception {
//                return deleteIfCurrentBatch(entries);
//            }
//        },  batchDelay, TimeUnit.MILLISECONDS);
//    }
//    
//    /**
//     * Delete only if this is the current batch
//     * @param entries
//     */
//    private synchronized Boolean deleteIfCurrentBatch(final List<DeleteMessageBatchRequestEntry> entries) {
//        if (entries == this.entries) {
//            enqueDeleteBatch(entries);
//            this.entries = Lists.newArrayList();
//            return true;
//        }
//        return false;
//    }
//    
//    /**
//     * Enqueue the current batch for delete
//     * @param entries
//     */
//    private void enqueDeleteBatch(final List<DeleteMessageBatchRequestEntry> entries) {
//        executor.submit(new Callable<Boolean>() {
//            @Override
//            public Boolean call() throws Exception {
//                try {
////                    List<BatchResultErrorEntry> failed = client.deleteBatch(entries);
//                    // TODO: Notify someone of failed sends
//                    return true;
//                } catch(Exception e) {
//                    return false;
//                }
//            }
//        });
//    }
}
