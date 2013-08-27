package com.netflix.schlep.sqs;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.netflix.schlep.admin.QueueAdmin;
import com.netflix.schlep.admin.QueueAttributes;

public class SqsQueueAdmin implements QueueAdmin {
    
    private final int    DEFAULT_MAX_RETRIES   = 3;
    private final int    DEFAULT_TIMEOUT       = 5000;
    private final String DEFAULT_REGION        = "us-east";
    
    
    private final AmazonSQSClient client;

    @Inject
    public SqsQueueAdmin(Provider<AWSCredentials> awsCredentials) {
        // Construct the client
        this.client = new AmazonSQSClient(awsCredentials.get(), new ClientConfiguration()
                .withConnectionTimeout(DEFAULT_TIMEOUT)
                .withSocketTimeout    (DEFAULT_TIMEOUT)
                .withMaxConnections   (DEFAULT_TIMEOUT)
                .withMaxErrorRetry    (DEFAULT_MAX_RETRIES)
                );
        
        // Modify the region endpoint
        client.setEndpoint(DEFAULT_REGION);
    }
    
    @Override
    public ListenableFuture<Boolean> createQueue(final String uri,final  QueueAttributes attributes) {
        return submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                CreateQueueRequest createRequest = new CreateQueueRequest()
                    .withQueueName(uri)
                    .withAttributes(attributes.getAttributes());
                client.createQueue(createRequest);
                return true;
            }
        });
    }

    @Override
    public ListenableFuture<Boolean> deleteQueue(final String uri) {
        return submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                DeleteQueueRequest request = new DeleteQueueRequest()
                    .withQueueUrl(uri);
                client.deleteQueue(request);
                return true;
            }
        });
    }

    @Override
    public ListenableFuture<QueueAttributes> getQueueAttributes(final String uri, final Collection<String> attributes) {
        return submit(new Callable<QueueAttributes>() {
            @Override
            public QueueAttributes call() throws Exception {
                GetQueueAttributesResult result = client.getQueueAttributes(new GetQueueAttributesRequest()
                    .withQueueUrl(uri)
                    .withAttributeNames(attributes));
            
                return new QueueAttributes().withAttributes(result.getAttributes());
            }
        });
    }
    
    @Override
    public ListenableFuture<Void> updateQueueAttributes(final String uri, final QueueAttributes attributes) {
        return submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // TODO: Validate attribute list or filter out unsupported attributes
                client.setQueueAttributes(new SetQueueAttributesRequest()
                    .withQueueUrl(uri)
                    .withAttributes(attributes.getAttributes()));
                return null;
            }
        });
    }
    
    @Override
    public ListenableFuture<List<String>> listQueues() {
        return submit(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                ListQueuesResult result = client.listQueues();
                return result.getQueueUrls();
            }
        });
    }

    @Override
    public ListenableFuture<QueueAttributes> getQueueAttributes(String queueName) {
        return getQueueAttributes(queueName, null);
    }

    @Override
    public ListenableFuture<Long> getQueueMessageCount(final String uri) {
        return submit(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                GetQueueAttributesResult result = client.getQueueAttributes(new GetQueueAttributesRequest()
                    .withQueueUrl(uri)
                    .withAttributeNames(SqsQueueAttribute.ApproximateNumberOfMessages.name()));
        
                try {
                    return Long.valueOf(result.getAttributes().get(SqsQueueAttribute.ApproximateNumberOfMessages.name()));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to get queue depth for queue: " + uri, e);
                }
            }
        });
    }
    
    private <V> ListenableFuture<V> submit(Callable<V> callable) {
        SettableFuture<V> future = SettableFuture.create();
        try {
            future.set(callable.call());
        } catch (Exception e) {
            future.setException(e);
        }
        return future;
    }
}
