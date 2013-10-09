package com.netflix.schlep.sqs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchRequest;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchRequestEntry;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchResult;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchResultEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.ListQueuesRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.netflix.schlep.exception.ProducerException;

/**
 * Concrete implementation of the SQS client using the Amazon provided client
 * Note that this client is attached to a specific queue
 * 
 * @author elandau
 */
public class AmazonSqsClient {
    public static final int DEFAULT_READ_TIMEOUT        = (int)TimeUnit.SECONDS.toMillis(10);
    public static final int DEFAULT_WAIT_TIMEOUT        = (int)TimeUnit.SECONDS.toMillis(10);
    public static final int DEFAULT_CONNECT_TIMEOUT     = (int)TimeUnit.SECONDS.toMillis(10);
    public static final int DEFAULT_MAX_RETRIES         = (int)TimeUnit.SECONDS.toMillis(10);
    public static final int DEFAULT_MAX_CONNECTIONS     = (int)TimeUnit.SECONDS.toMillis(10);
    public static final String DEFAULT_REGION           = "us-east-1";
    
    public static class Builder {
        private AWSCredentials credentials;
        private int connectTimeout  = DEFAULT_CONNECT_TIMEOUT;
        private int readTimeout     = DEFAULT_READ_TIMEOUT;
        private int maxConnections  = DEFAULT_MAX_CONNECTIONS;
        private int maxRetries      = DEFAULT_MAX_RETRIES;
        private String region       = DEFAULT_REGION;
        private String queueName;
        
        public Builder withQueueName(String queueName) {
            this.queueName = queueName;
            return this;
        }
        
        public Builder withRegion(String region) {
            this.region = region;
            return this;
        }
        
        public Builder withCredentials(AWSCredentials credentials) {
            this.credentials = credentials;
            return this;
        }
        
        public Builder withConnectionTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }
        
        public Builder withReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }
        
        public Builder withMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }
        
        public Builder withMaxRetries(int retries) {
            this.maxRetries = retries;
            return this;
        }
        
        public AmazonSqsClient build() throws Exception {
            return new AmazonSqsClient(this);
        }

        @Override
        public String toString() {
            return "Builder [credentials=" + credentials + ", connectTimeout="
                    + connectTimeout + ", readTimeout=" + readTimeout
                    + ", maxConnections=" + maxConnections + ", maxRetries="
                    + maxRetries + ", region=" + region + ", queueName="
                    + queueName + "]";
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    private final AmazonSQSClient           client;
    private final String                    queueUrl;
    private final String                    queueName;

    protected AmazonSqsClient(Builder builder) throws Exception {
        this.queueName = builder.queueName;
        
        // Construct the client
        this.client = new AmazonSQSClient(builder.credentials, 
                new ClientConfiguration()
                    .withConnectionTimeout(builder.connectTimeout)
                    .withSocketTimeout    (builder.readTimeout)
                    .withMaxConnections   (builder.maxConnections)
                    .withMaxErrorRetry    (builder.maxRetries));
            
        // Modify the region endpoint
        client.setEndpoint("sqs." + builder.region + ".amazonaws.com");
        
        // Determine the queue URL
        GetQueueUrlRequest request   = new GetQueueUrlRequest();
        QueueName          queueName = new QueueName(builder.queueName);
        
        if (!queueName.isFullQualifiedName()) {
            // List all queue names and find the one which has a full url ending with the queue name
            ListQueuesRequest listRequest = new ListQueuesRequest()
                .withQueueNamePrefix(queueName.getName());
            ListQueuesResult listResult = client.listQueues(listRequest);
            
            List<String> queueUrlList = listResult.getQueueUrls();
            boolean found = false;
            for (String queueUrl : queueUrlList) {
                String queueNameFromUrl = queueUrl.substring(queueUrl.lastIndexOf("/") + 1);
                if(queueNameFromUrl.equals(queueName.getName()))
                    found = true;
            }

            // TODO: Should we auto create the queue? I'm guessing no!
            if (!found) {
                throw new Exception("SQS queue not found. " + builder.queueName);
            }
            
            request.setQueueName(queueName.getName());
        }
        else {
            request.setQueueOwnerAWSAccountId(queueName.getAccountId());
            request.setQueueName(queueName.getName());
        }
        
        // get queueUrl string as required for subsequent calls to AmazonSQSClient
        GetQueueUrlResult result = client.getQueueUrl(request);
        this.queueUrl = result.getQueueUrl();
    }
    
    public static boolean isFullQualifiedQueueName(String queueName) {
        if ((queueName.charAt(0) == '/' && queueName.lastIndexOf('/') > 0)) 
            return true;
        else
            return false;
    }
    
    /**
     * @return first = ownerAccountId, second = queueName
     */
    public static String[] splitFullyQualifiedQueueName(String fullyQualifiedQueueName) {
        if(fullyQualifiedQueueName.charAt(0) != '/')
            throw new IllegalArgumentException("invalid fully qualified queue name: " + fullyQualifiedQueueName);

        String[] parts = fullyQualifiedQueueName.split("/");
        if(parts == null || parts.length != 3)
            throw new IllegalArgumentException("invalid fully qualified queue name: " + fullyQualifiedQueueName);

        if(StringUtils.isNotBlank(parts[1]) && StringUtils.isNotBlank(parts[2])) {
            String ownerAccountId = parts[1];
            String queueName = parts[2];
            return new String[]{ownerAccountId, queueName};
        } else {
            throw new IllegalArgumentException("invalid fully qualified queue name: " + fullyQualifiedQueueName);
        }
    }  


    public List<SqsMessage> receiveMessages(int maxMessageCount, long visibilityTimeout) {
        return receiveMessages(maxMessageCount, visibilityTimeout, null);
    }

    public List<SqsMessage> receiveMessages(int maxMessageCount, long visibilityTimeout, List<String> attributes) {
        // Prepare the request
        ReceiveMessageRequest request = new ReceiveMessageRequest()
            .withQueueUrl           (queueUrl)
            .withVisibilityTimeout  ((int)visibilityTimeout) 
            .withMaxNumberOfMessages(maxMessageCount);
        
        if (attributes != null)
            request = request.withAttributeNames(attributes);
            
        List<SqsMessage> response = Lists.newArrayList();
        for (Message message : client.receiveMessage(request).getMessages()) {
            response.add(new SqsMessage(message));
        }
        return response;
    }
   
    public List<SqsMessage> renewMessages(List<SqsMessage> messages) {
        // Construct a send message request and assign each message an ID equivalent to it's position
        // in the original list for fast lookup on the response
        final List<ChangeMessageVisibilityBatchRequestEntry> batchReqEntries = new ArrayList<ChangeMessageVisibilityBatchRequestEntry>(messages.size());
        int id = 0;
        for (SqsMessage messageRenew : messages) {
            // TODO: Add delay
            batchReqEntries.add(new ChangeMessageVisibilityBatchRequestEntry()
                    .withId(Integer.toString(id)) 
                    .withReceiptHandle(messageRenew.getMessage().getReceiptHandle())
                    .withVisibilityTimeout((int)messageRenew.getVisibilityTimeout()));
            ++id;
        }
        
        ChangeMessageVisibilityBatchRequest request = new ChangeMessageVisibilityBatchRequest()
            .withQueueUrl(queueUrl)
            .withEntries(batchReqEntries);
    
        // Send the request
        ChangeMessageVisibilityBatchResult result = client.changeMessageVisibilityBatch(request);
        
        // Update the future for successful sends
        for (ChangeMessageVisibilityBatchResultEntry entry : result.getSuccessful()) {
            messages.get(Integer.parseInt(entry.getId()));
        }
        
        // Handle failed sends
        if (result.getFailed() != null && !result.getFailed().isEmpty()) {
            List<SqsMessage> retryableMessages = Lists.newArrayListWithCapacity(result.getFailed().size());
            for (BatchResultErrorEntry entry : result.getFailed()) {
                // There cannot be resent and are probably the result of something like message exceeding
                // the max size or certificate errors
                if (entry.isSenderFault()) {
                    messages.get(Integer.parseInt(entry.getId())).setException(new ProducerException(entry.getCode()));
                }
                // These messages can probably be resent and may be due to issues on the amazon side, 
                // such as service timeout
                else {
                    retryableMessages.add(messages.get(Integer.parseInt(entry.getId())));
                }
            }
            return retryableMessages;
        }
        // All sent OK
        else {
            return ImmutableList.of();
        }
    }

    public String getQueueName() {
        return queueName;
    }

    public SendMessageBatchResult sendMessageBatch(Collection<SendMessageBatchRequestEntry> entries) {
        SendMessageBatchRequest request = new SendMessageBatchRequest();
        request.withEntries(entries);
        request.withQueueUrl(queueUrl);
        return client.sendMessageBatch(request);
    }
    
    public DeleteMessageBatchResult deleteMessageBatch(Collection<DeleteMessageBatchRequestEntry> entries) {
        DeleteMessageBatchRequest request = new DeleteMessageBatchRequest();
        request.withQueueUrl(queueUrl);
        request.withEntries(entries);
        return client.deleteMessageBatch(request);
    }
}
