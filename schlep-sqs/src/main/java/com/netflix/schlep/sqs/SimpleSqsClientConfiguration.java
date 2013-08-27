package com.netflix.schlep.sqs;

import java.util.concurrent.TimeUnit;

import com.netflix.schlep.mapper.SerializerProvider;
import com.netflix.schlep.mapper.jackson.JacksonSerializerProvider;
import com.netflix.util.batch.BatchingPolicy;
import com.netflix.util.batch.InlineNoBatchPolicy;
import com.netflix.util.retry.NoRetryPolicy;
import com.netflix.util.retry.RetryPolicy;

public class SimpleSqsClientConfiguration implements SqsClientConfiguration{

    public static final int DEFAULT_READ_TIMEOUT_MILLIS        = (int)TimeUnit.SECONDS     .convert(1,  TimeUnit.MINUTES);
    public static final int DEFAULT_WAIT_TIMEOUT_MILLIS        = (int)TimeUnit.SECONDS     .convert(1,  TimeUnit.MINUTES);
    public static final int DEFAULT_TERMINATE_TIMEOUT          = (int)TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS);
    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS     = (int)TimeUnit.SECONDS     .convert(1,  TimeUnit.MINUTES);
    public static final int DEFAULT_MAX_HTTP_CONNECTIONS       = 10;
    public static final int DEFAULT_MAX_READ_BATCH_SIZE        = 10;
    public static final int DEFAULT_MAX_HTTP_RETRIES           = 5;
    public static final int DEFAULT_VISIBILITY_TIMEOUT         = (int)TimeUnit.SECONDS     .convert(5,  TimeUnit.MINUTES);
    public static final String DEFAULT_REGION                  = "us-east-1";
    public static final int DEFAULT_MAX_SQS_RETRIES            = 3;
    public static final int DEFAULT_SEND_BATCH_MAX_TOTAL_BYTES = 65536;
    public static final int DEFAULT_THREAD_COUNT               = 1;
    public static final boolean DEFAULT_ENABLE_BASE64          = true;
    public static final BatchingPolicy DEFAULT_BATCH_STRATEGY  = new InlineNoBatchPolicy();
    public static final RetryPolicy DEFAULT_RETRY_POLICY       = new NoRetryPolicy();
    public static final SerializerProvider DEFAULT_SERIALIZER   = new JacksonSerializerProvider();
    private String queueName;
    
    private int            readTimeoutInMillis                = DEFAULT_READ_TIMEOUT_MILLIS;
    private int            connectTimeoutInMillis             = DEFAULT_CONNECT_TIMEOUT_MILLIS;
    private int            terminateTimeoutInMillis           = DEFAULT_TERMINATE_TIMEOUT;
    private int            maxConnections                     = DEFAULT_MAX_HTTP_CONNECTIONS;
    private int            maxRetries                         = DEFAULT_MAX_HTTP_RETRIES;
    private int            visibilityTimeout                  = DEFAULT_VISIBILITY_TIMEOUT;
    private String         endpoint                           ;
    private boolean        enableBase64                       = DEFAULT_ENABLE_BASE64;
    private int            workerThreadCount                  = DEFAULT_THREAD_COUNT;
    private RetryPolicy    retryPolicy                        = DEFAULT_RETRY_POLICY;
    private BatchingPolicy batchPolicy                        = DEFAULT_BATCH_STRATEGY;
    private SerializerProvider serializerFactory               = DEFAULT_SERIALIZER;
    private int            maxReadBatchSize                   = DEFAULT_MAX_READ_BATCH_SIZE;
            
    @Override
    public int getReadTimeoutInMillis() {
        return this.readTimeoutInMillis;
    }
    
    public void setReadTimeoutInMillis(int readTimeoutInMillis) {
        this.readTimeoutInMillis = readTimeoutInMillis;
    }

    @Override
    public int getConnectTimeoutInMillis() {
        return this.connectTimeoutInMillis;
    }

    public void setConnectTimeoutInMillis(int connectTimeoutInMillis) {
        this.connectTimeoutInMillis = connectTimeoutInMillis;
    }

    @Override
    public int getMaxConnections() {
        return this.maxConnections;
    }

    public void setMaxHttpConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    @Override
    public String getQueueName() {
        return this.queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public int getMaxRetries() {
        return this.maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public String getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public int getVisibilityTimeoutSeconds() {
        return this.visibilityTimeout;
    }

    public void setVisibilityTimeoutSeconds(int visibilityTimeout) {
        this.visibilityTimeout = visibilityTimeout;
    }

    @Override
    public boolean getEnable64Encoding() {
        return this.enableBase64;
    }
    
    public void setEnable64Encoding(boolean enableBase64) {
        this.enableBase64 = enableBase64;
    }

    @Override
    public int getWorkerThreadCount() {
        return this.workerThreadCount;
    }

    public void setWorkerThreadCount(int workerThreadCount) {
        this.workerThreadCount = workerThreadCount;
    }

    @Override
    public long getTerminateTimeout() {
        return this.terminateTimeoutInMillis;
    }

    public void setTerminateTimeout(int terminateTimeout) {
        this.terminateTimeoutInMillis = terminateTimeout;
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        return this.retryPolicy;
    }
    
    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    @Override
    public BatchingPolicy getBatchPolicy() {
        return this.batchPolicy;
    }

    public void setBatchPolicy(BatchingPolicy batchPolicy) {
        this.batchPolicy = batchPolicy;
    }

    @Override
    public int getMaxReadBatchSize() {
        return this.maxReadBatchSize;
    }

    public void setMaxReadBatchSize(int maxReadBatchSize) {
        this.maxReadBatchSize = maxReadBatchSize;
    }

    @Override
    public SerializerProvider getSerializerFactory() {
        return this.serializerFactory;
    }
    
    public void setSerializerFactory(SerializerProvider serializerFactory) 
    {
        this.serializerFactory = serializerFactory;
    }
}
