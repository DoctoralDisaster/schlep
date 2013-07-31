package com.netflix.schlep.sqs;

import java.util.List;

import com.amazonaws.services.sqs.model.Message;

/**
 * Abstraction and simplified API to the SQS client.  Any implementation of 
 * SqsClient only needs to implement the synchronous calls to SQS and does not 
 * need need to implement any batching or failover logic.  All batching and
 * failover logic is expected to be implemented by the caller of this API.  
 * All calls here are synchronous.
 * 
 * @author elandau
 *
 */
public interface SqsClient {
    /**
     * @return Unique queue name
     */
    public String getQueueName();

    /**
     * Receive up to maxMessageCount messages
     * 
     * @param maxMessageCount
     * @return
     */
    public List<Message> receiveMessages(int maxMessageCount, long visibilityTimeout);

    /**
     * Receive up to maxMessageCount messages and provide a list of additional attributes to retrieve
     * @param maxMessageCount
     * @param attributes
     * @return
     */
    public List<Message> receiveMessages(int maxMessageCount, long visibilityTimeout, List<String> attributes);

    /**
     * Send a batch of messages and return only the failed and retryable messages.
     * 
     * Messages that were sent successfully will have their future set to true.
     * Messages that cannot be resent will have their future's exception set.
     * 
     * @return  Collection of failed messages
     */
    public List<MessageAndFuture<Boolean>> sendMessages(List<MessageAndFuture<Boolean>> messages);
    
    /**
     * Delete the messages, which is effectively is acking them.
     * 
     * @param messages
     * @return
     */
    public List<MessageAndFuture<Boolean>> deleteMessages(List<MessageAndFuture<Boolean>> messages);
    
    /**
     * Extend the visibility timeout for these messages
     * @param messages
     * @return
     */
    public List<MessageAndFuture<Boolean>> renewMessages(List<MessageAndFuture<Boolean>> messages);
}
