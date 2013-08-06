package com.netflix.schlep.sqs;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.model.Message;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class FakeSqsClient implements SqsClient {
    private static final Logger LOG = LoggerFactory.getLogger(FakeSqsClient.class);
    
    private final SqsClientConfiguration clientConfig;
    
    private static final ConcurrentMap<String, BlockingQueue<Message>> queues = Maps.newConcurrentMap(); 
    
    private final BlockingQueue<Message> queue;
    
    @Inject
    public FakeSqsClient(@Assisted SqsClientConfiguration clientConfig) {
        this.clientConfig = clientConfig;
        
        BlockingQueue<Message> temp = Queues.<Message>newLinkedBlockingDeque();
        BlockingQueue<Message> prev = queues.putIfAbsent(clientConfig.getQueueName(), temp);
        if (prev == null) {
            LOG.info("Connecting to new Queue : " + clientConfig.getQueueName());
            queue = temp;
        }
        else {
            LOG.info("Connecting to existing Queue : " + clientConfig.getQueueName());
            queue = prev;
        }
    }
    
    @Override
    public List<Message> receiveMessages(int maxMessageCount, long visibilityTimeout) {
        return receiveMessages(maxMessageCount, visibilityTimeout, null);
    }

    @Override
    public List<Message> receiveMessages(int maxMessageCount, long visibilityTimeout, List<String> attributes) {
        List<Message> messages = Lists.newArrayListWithCapacity(maxMessageCount);
        for (int i = 0; i < maxMessageCount; i++) {
            Message message = queue.poll();
            if (message == null)
                break;
            messages.add(message);
        }
        return messages;
    }

	@Override
	public List<MessageFuture<Boolean>> sendMessages(List<MessageFuture<Boolean>> messages) {
	    for (MessageFuture<Boolean> entry : messages) {
	        LOG.info("Send message: " + entry.getMessage());
	        
	        queue.add(entry.getMessage());
	        entry.set(true);
	    }
	    
	    return ImmutableList.of();
	}

	@Override
	public String getQueueName() {
        return clientConfig.getQueueName();
	}

    @Override
    public List<MessageFuture<Boolean>> renewMessages(
            List<MessageFuture<Boolean>> messages) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MessageFuture<Boolean>> deleteMessages(
            List<MessageFuture<Boolean>> messages) {
        // TODO Auto-generated method stub
        return null;
    }


}
