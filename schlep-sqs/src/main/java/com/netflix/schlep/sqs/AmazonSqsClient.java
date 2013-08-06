package com.netflix.schlep.sqs;

import java.util.ArrayList;
import java.util.List;

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
import com.amazonaws.services.sqs.model.DeleteMessageBatchResultEntry;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.ListQueuesRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageBatchResultEntry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.netflix.schlep.exception.ProducerException;

/**
 * Concrete implementation of the SQS client using the Amazon provided client
 * Note that this client is attached to a specific queue
 * 
 * @author elandau
 */
public class AmazonSqsClient implements SqsClient {
    private final AmazonSQSClient           client;
    private final String                    queueUrl;
    private final SqsClientConfiguration    clientConfig;
    
    public AmazonSqsClient(
                      Provider<AWSCredentials> awsCredentials, 
            @Assisted SqsClientConfiguration clientConfig) throws Exception {

        this.clientConfig = clientConfig;
        
        // Construct the client
        this.client = new AmazonSQSClient(awsCredentials.get(), new ClientConfiguration()
                .withConnectionTimeout(clientConfig.getConnectTimeoutInMillis() + clientConfig.getWaitTimeoutInMillis())
                .withSocketTimeout    (clientConfig.getReadTimeoutInMillis())
                .withMaxConnections   (clientConfig.getMaxHttpConnections())
                .withMaxErrorRetry    (clientConfig.getMaxRetries()));
        
        // Modify the region endpoint
        client.setEndpoint(clientConfig.getEndpoint());
        
        // Determine the queue URL
        GetQueueUrlRequest request = new GetQueueUrlRequest();
        QueueName queueName = new QueueName(clientConfig.getQueueName());
        
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
                throw new Exception("SQS queue not found. " + clientConfig.getQueueName());
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


    @Override
    public List<Message> receiveMessages(int maxMessageCount, long visibilityTimeout) {
        return receiveMessages(maxMessageCount, visibilityTimeout, null);
    }

    @Override
    public List<Message> receiveMessages(int maxMessageCount, long visibilityTimeout, List<String> attributes) {
        // Prepare the request
        ReceiveMessageRequest request = new ReceiveMessageRequest()
            .withQueueUrl           (queueUrl)
            .withVisibilityTimeout  ((int)visibilityTimeout) 
            .withMaxNumberOfMessages(maxMessageCount);
        
        if (attributes != null)
            request = request.withAttributeNames(attributes);
            
        return client.receiveMessage(request).getMessages();
    }
    
    @Override
    public List<MessageFuture<Boolean>> deleteMessages(List<MessageFuture<Boolean>> messages) {
        // Construct a send message request and assign each message an ID equivalent to it's position
        // in the original list for fast lookup on the response
        final List<DeleteMessageBatchRequestEntry> batchReqEntries = new ArrayList<DeleteMessageBatchRequestEntry>(messages.size());
        int id = 0;
        for (MessageFuture<Boolean> message : messages) {
            batchReqEntries.add(new DeleteMessageBatchRequestEntry(
                    Integer.toString(id), 
                    message.getMessage().getReceiptHandle()));
            ++id;
        }
        
        DeleteMessageBatchRequest request = new DeleteMessageBatchRequest()
            .withQueueUrl(queueUrl)
            .withEntries(batchReqEntries);
    
        // Send the request
        DeleteMessageBatchResult result = null;
        result = client.deleteMessageBatch(request);

        // Update the future for successful sends
        for (DeleteMessageBatchResultEntry entry : result.getSuccessful()) {
            messages.get(Integer.parseInt(entry.getId())).set(true);
        }

        // Handle failed sends
        if (result.getFailed() != null && !result.getFailed().isEmpty()) {
            List<MessageFuture<Boolean>> retryableMessages = Lists.newArrayListWithCapacity(result.getFailed().size());
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
    
    @Override
    public List<MessageFuture<Boolean>> renewMessages(List<MessageFuture<Boolean>> messages) {
        // Construct a send message request and assign each message an ID equivalent to it's position
        // in the original list for fast lookup on the response
        final List<ChangeMessageVisibilityBatchRequestEntry> batchReqEntries = new ArrayList<ChangeMessageVisibilityBatchRequestEntry>(messages.size());
        int id = 0;
        for (MessageFuture<Boolean> messageRenew : messages) {
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
            messages.get(Integer.parseInt(entry.getId())).set(true);
        }
        
        // Handle failed sends
        if (result.getFailed() != null && !result.getFailed().isEmpty()) {
            List<MessageFuture<Boolean>> retryableMessages = Lists.newArrayListWithCapacity(result.getFailed().size());
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

    @Override
    public List<MessageFuture<Boolean>> sendMessages(final List<MessageFuture<Boolean>> messages) {
        // Construct a send message request and assign each message an ID equivalent to it's position
        // in the original list for fast lookup on the response
        final List<SendMessageBatchRequestEntry> batchReqEntries = new ArrayList<SendMessageBatchRequestEntry>(messages.size());
        int id = 0;
        for (MessageFuture<Boolean> message : messages) {
            // TODO: Add delay
            batchReqEntries.add(new SendMessageBatchRequestEntry(Integer.toString(id), message.getMessage().getBody()));
            ++id;
        }
        
        SendMessageBatchRequest request = new SendMessageBatchRequest()
            .withQueueUrl(queueUrl)
            .withEntries(batchReqEntries);
    
        // Send the request
        SendMessageBatchResult result = client.sendMessageBatch(request);
        
        // Update the future for successful sends
        for (SendMessageBatchResultEntry entry : result.getSuccessful()) {
            messages.get(Integer.parseInt(entry.getId())).set(true);
        }
        
        // Handle failed sends
        if (result.getFailed() != null && !result.getFailed().isEmpty()) {
            List<MessageFuture<Boolean>> retryableMessages = Lists.newArrayListWithCapacity(result.getFailed().size());
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

    @Override
    public String getQueueName() {
        return clientConfig.getQueueName();
    }
}
