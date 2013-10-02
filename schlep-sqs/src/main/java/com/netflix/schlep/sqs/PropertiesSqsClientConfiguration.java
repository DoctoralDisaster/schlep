package com.netflix.schlep.sqs;

import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.AbstractConfiguration;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.netflix.schlep.mapper.SerializerProvider;

/**
 * Implementation of SQS configuration from an AbstractConfiguration
 * 
 * @author elandau
 */
public class PropertiesSqsClientConfiguration implements SqsClientConfiguration {
    public static final String PROP_READ_TIMEOUT               = "%s.netflix.messaging.sqs.readTimeoutInMillis";
    public static final String PROP_WAIT_TIMEOUT               = "%s.netflix.messaging.sqs.waitTimeoutInMillis";
    public static final String PROP_CONNECT_TIMEOUT            = "%s.netflix.messaging.sqs.connectTimeoutInMillis";
    public static final String PROP_NO_HTTPCONNECTIONS         = "%s.netflix.messaging.sqs.maxHttpConnections";
    public static final String PROP_MAX_HTTP_RETRIES           = "%s.netflix.messaging.sqs.maxRetries";
    public static final String PROP_QUEUE_REGION               = "%s.netflix.messaging.sqs.region";
    public static final String PROP_VISIBILITY_TIMEOUT         = "%s.netflix.messaging.sqs.visibilityTimeoutInSeconds";
    public static final String PROP_RETRY_TYPE                 = "%s.netflix.messaging.sqs.retry.type";
    public static final String PROP_RETRY_PROPERTIES           = "%s.netflix.messaging.sqs.retry";
    public static final String PROP_MAX_SQS_RETRIES            = "%s.netflix.messaging.sqs.retry.maxRetries";
    public static final String PROP_SEND_BATCH_MAX_TOTAL_BYTES = "%s.netflix.messaging.sqs.sendBatchMaxTotalBytes";
    public static final String PROP_ENABLE_BASE64_ENCODING     = "%s.netflix.messaging.sqs.enableBase64Encoding";
    public static final String PROP_QUEUE_NAME                 = "%s.netflix.messaging.sqs.name";
    public static final String PROP_THREAD_COUNT               = "%s.netflix.messaging.sqs.threadCount";
    public static final String PROP_TERMINATE_TIMEOUT          = "%s.netflix.messaging.sqs.terminateTimeoutInMillis";

    public static final int DEFAULT_MAX_HTTP_CONNECTIONS       = 10000;
    public static final int DEFAULT_MAX_HTTP_RETRIES           = 5;
    public static final int DEFAULT_READ_TIMEOUT_MILLIS        = 1000;
    public static final int DEFAULT_WAIT_TIMEOUT_MILLIS        = 1000;
    public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS     = 1000;
    public static final int DEFAULT_VISIBILITY_TIMEOUT         = (int)TimeUnit.SECONDS     .convert(5,  TimeUnit.MINUTES);
    public static final int DEFAULT_TERMINATE_TIMEOUT          = (int)TimeUnit.MILLISECONDS.convert(10, TimeUnit.SECONDS);
    public static final String DEFAULT_REGION                  = "us-east-1";
    public static final String DEFAULT_RETRY_TYPE              = "none";
    public static final int DEFAULT_MAX_SQS_RETRIES            = 3;
	public static final int DEFAULT_MAX_BATCH_SIZE             = 10;
    public static final int DEFAULT_SEND_BATCH_MAX_TOTAL_BYTES = 65536;
    public static final int DEFAULT_THREAD_COUNT               = 1;
	public static final boolean DEFAULT_ENABLE_BASE64          = true;
//	public static final BatchingPolicy DEFAULT_BATCH_STRATEGY  = new InlineNoBatchPolicy();
//    public static final RetryPolicy DEFAULT_RETRY_POLICY       = new NoRetryPolicy();
            
    private final String queueName;
    
    private final int readTimeoutInMillis   ;
    private final int connectTimeoutInMillis;
    private final int terminateTimeoutInMillis;
    private final int sqsMaxHttpConnections ;
    private final int maxRetries            ;
    private final int visibilityTimeout     ;
    private final String endPoint;
//    private final RetryPolicy retryPolicy = DEFAULT_RETRY_POLICY;
    private final boolean enableBase64;
    private final int workerThreadCount;
//    private final BatchingPolicy batchPolicy;
    
    @Inject
    public PropertiesSqsClientConfiguration(
            AbstractConfiguration           config, 
            @Assisted String                configName) {
        // Load configuration properties
        this.readTimeoutInMillis      = config.getInt    (String.format(PROP_READ_TIMEOUT,           configName),  DEFAULT_READ_TIMEOUT_MILLIS);
        this.connectTimeoutInMillis   = config.getInt    (String.format(PROP_CONNECT_TIMEOUT,        configName),  DEFAULT_CONNECT_TIMEOUT_MILLIS);
        this.sqsMaxHttpConnections    = config.getInt    (String.format(PROP_NO_HTTPCONNECTIONS,     configName),  DEFAULT_MAX_HTTP_CONNECTIONS);
        this.maxRetries               = config.getInt    (String.format(PROP_MAX_HTTP_RETRIES,       configName),  DEFAULT_MAX_HTTP_RETRIES);
        this.visibilityTimeout        = config.getInt    (String.format(PROP_VISIBILITY_TIMEOUT,     configName),  DEFAULT_VISIBILITY_TIMEOUT);
        String region                 = config.getString (String.format(PROP_QUEUE_REGION,           configName),  DEFAULT_REGION);
        this.queueName                = config.getString (String.format(PROP_QUEUE_NAME,             configName),  null);
        this.enableBase64             = config.getBoolean(String.format(PROP_ENABLE_BASE64_ENCODING, configName),  DEFAULT_ENABLE_BASE64);
        this.workerThreadCount        = config.getInt    (String.format(PROP_THREAD_COUNT,           configName),  DEFAULT_THREAD_COUNT);
        this.terminateTimeoutInMillis = config.getInt    (String.format(PROP_TERMINATE_TIMEOUT,      configName),  DEFAULT_TERMINATE_TIMEOUT);
        
        // Modify the region endpoint
        this.endPoint = "sqs." + region + ".amazonaws.com";
        
//        Preconditions.checkArgument(retryType != null && retryPolicyFactories.containsKey(retryType), 
//                String.format("Missing or unknown retry policy type [%s].  Expecting %s for property '%s'", 
//                              retryType,
//                              retryPolicyFactories.keySet(), 
//                              String.format(PROP_RETRY_TYPE, configName)));
//        
        Preconditions.checkArgument(queueName != null,
                String.format("No queue name specified in property property '%s'", 
                        String.format(PROP_QUEUE_NAME, configName)));
                
//        this.retryPolicy   = retryPolicyFactories.get(retryType).create(config.subset(String.format(PROP_RETRY_PROPERTIES, configName)));
//        this.batchPolicy = this.DEFAULT_BATCH_STRATEGY;
    }
    
    @Override
    public String getQueueName() {
        return this.queueName;
    }
    
    @Override
    public int getReadTimeoutInMillis() {
        return readTimeoutInMillis;
    }
    
    @Override
    public int getConnectTimeoutInMillis() {
        return connectTimeoutInMillis;
    }
    
    @Override
    public int getMaxConnections() {
        return sqsMaxHttpConnections;
    }
    
    @Override
    public int getMaxRetries() {
        return maxRetries;
    }
    
    @Override
    public String getEndpoint() {
        return endPoint;
    }
    
    @Override
    public int getVisibilityTimeoutSeconds() {
        return this.visibilityTimeout;
    }

//    @Override
//    public RetryPolicy getRetryPolicy() {
//        return this.retryPolicy;
//    }
    
    @Override
    public boolean getEnable64Encoding() {
        return this.enableBase64;
    }

    @Override
    public int getWorkerThreadCount() {
        return this.workerThreadCount;
    }

    @Override
    public long getTerminateTimeout() {
        return this.terminateTimeoutInMillis;
    }

//    @Override
//    public BatchingPolicy getBatchPolicy() {
//        return this.batchPolicy;
//    }

    @Override
    public int getMaxReadBatchSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public SerializerProvider getSerializerFactory() {
        return null;
    }
}
