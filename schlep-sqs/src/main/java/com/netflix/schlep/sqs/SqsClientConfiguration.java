package com.netflix.schlep.sqs;

import com.netflix.schlep.mapper.SerializerProvider;
import com.netflix.util.batch.BatchingPolicy;
import com.netflix.util.retry.RetryPolicy;

/**
 * Wraps all aspects of the client configuration
 * 
 * @author elandau
 */
public interface SqsClientConfiguration {

    /**
     * @return Socket read timeout in milliseconds
     */
    int getReadTimeoutInMillis();

    /**
     * @return Connect timeout in milliseconds
     */
    int getConnectTimeoutInMillis();

    /**
     * @return Sets the maximum number of allowed open HTTP connections
     */
    int getMaxConnections();
 
    /**
     * Simple or fully qualified queue name.  
     * 1: /queuename
     * 2: /accountid/queuename
     * @see QueueName
     */
    String getQueueName();

    /**
     * @return Maximum number of messages in a read 
     */
    int getMaxReadBatchSize();
    
    /**
     * Maximum number of retries on the SQS client.  Note that this is a
     * configuration of the Amazon SQS client and is not part of the retry policy
     * @return
     */
    int getMaxRetries();

    /**
     * @return Overrides the default endpoint for this client. Callers can use this
     * method to control which AWS region they want to work with.
     */
    String getEndpoint();

    /**
     * @return  This is the message consume timeout between reading the message
     * and issuing an ack. 
     */
    int getVisibilityTimeoutSeconds();

    /**
     * @return  If true then all messages will be base64 endcoded
     * 
     * TODO: Do we really need this for JSON serialized messages or just for binary messages
     */
    boolean getEnable64Encoding();

    /**
     * @return  Number of threads that read from SQS and process the messages
     * 
     * TODO: Split this into 'read' or 'process' threads
     */
    int getWorkerThreadCount();

    /**
     * @return  Time to wait for worker threads to terminate
     */
    long getTerminateTimeout();
    
    /**
     * Backoff and retry policy on top of whatever retry policy is available in the underlying
     * client (i.e. Amazon SQS client).
     * 
     * @return  Schlep backoff and retry policy for all operations.
     */
    RetryPolicy getRetryPolicy();

    /**
     * @return  Policy for constructing any kind of batch: consume, ack, renew, produce
     */
    BatchingPolicy getBatchPolicy();
    
    /**
     * @return Serializer factory to use to create a serializer that will be used to encode messages
     */
    SerializerProvider getSerializerFactory();
}
