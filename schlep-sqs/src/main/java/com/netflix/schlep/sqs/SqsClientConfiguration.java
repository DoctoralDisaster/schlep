package com.netflix.schlep.sqs;

import com.netflix.schlep.batch.BatchStrategy;
import com.netflix.schlep.sqs.retry.RetryPolicy;

/**
 * Wraps all aspects of the client configuration
 * 
 * @author elandau
 */
public interface SqsClientConfiguration {

    int getReadTimeoutInMillis();

    int getConnectTimeoutInMillis();

    int getWaitTimeoutInMillis();

    int getMaxHttpConnections();

    String getQueueName();

    int getMaxRetries();

    String getEndpoint();

    int getVisibilityTimeout();

    boolean getEnable64Encoding();

    int getWorkerThreadCount();

    int getBatchSize();

    /**
     * Time to wait for worker threads to terminate
     * @return
     */
    long getTerminateTimeout();
    
    RetryPolicy getRetryPolicy();

    BatchStrategy getBatchStrategy();
}
