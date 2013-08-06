package com.netflix.schlep.sqs;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.amazonaws.services.sqs.model.Message;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.MessageProducer;
import com.netflix.schlep.MessageProducerProvider;
import com.netflix.schlep.OutgoingMessage;
import com.netflix.schlep.batch.Batcher;
import com.netflix.schlep.exception.ProducerException;
import com.netflix.schlep.sqs.retry.RetryPolicy;
import com.netflix.schlep.sqs.serializer.JacksonSerializer;
import com.netflix.schlep.sqs.serializer.Serializer;
import com.netflix.schlep.sqs.transform.NoOpTransform;
import com.netflix.schlep.sqs.transform.ToBase64Transform;

/**
 * Implementation of a message producer that sends messages to SQS
 * @author elandau
 */
public class SqsMessageProducerProvider implements MessageProducerProvider {
    private final SqsClientFactory              clientFactory;
    private final SqsClientConfigurationFactory configurationFactory;

    @Inject
    public SqsMessageProducerProvider(
            SqsClientFactory              clientFactory,
            SqsClientConfigurationFactory configurationFactory
            ) {
        this.clientFactory        = clientFactory;
        this.configurationFactory = configurationFactory;
    }
    
    @Override
    public <T> MessageProducer<T> getProducer(EndpointKey<T> key) throws ProducerException {
        return new SqsMessageProducer<T>(key);
    }
    
    /**
     * 
     * TODO:   Batching logic (by count && by time)
     * 
     * @author elandau
     *
     * @param <T>
     */
    class SqsMessageProducer<T> implements MessageProducer<T> {
        private final SqsClient            client;
        private final EndpointKey<T>       key;
        private final String               queueName;
        private final SqsClientConfiguration clientConfig;
        private final RetryPolicy          retryPolicy;
        private final Serializer<T>		   serializer;
        private final MessageDigest        digest;
        private final Function<String, String> transform;
        private final Batcher<MessageFuture<Boolean>>   sendBatcher;
      
        public SqsMessageProducer(EndpointKey<T> key) throws ProducerException {
            try {
                this.digest       = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new ProducerException(e.getMessage(), e);
            }
            this.key          = key;
            this.queueName    = key.getName();
            this.serializer   = new JacksonSerializer<T>(key.getMessageType()); // TODO: Codec?
            this.clientConfig = configurationFactory.get(queueName);
            this.client       = clientFactory.create(clientConfig);
            this.retryPolicy  = clientConfig.getRetryPolicy();
            
            if (clientConfig.getEnable64Encoding()) {
                transform = new ToBase64Transform();
            }
            else {
                transform = new NoOpTransform();
            }
            
            this.sendBatcher     = clientConfig.getBatchStrategy().create(new Function<List<MessageFuture<Boolean>>, Boolean>() {
                public Boolean apply(List<MessageFuture<Boolean>> messages) {
                    client.sendMessages(messages);
                    return true;
                }
            });

        }

        @Override
        public ListenableFuture<Boolean> produce(T message) throws ProducerException {
            return produce(new OutgoingMessage<T>().withMessage(message));
        }
        
        @Override
        public ListenableFuture<Boolean> produce(OutgoingMessage<T> message) throws ProducerException {
            String msgBody;
			try {
				msgBody = this.transform.apply(this.serializer.serialize(message.getMessage()));
			} catch (Exception e) {
				throw new ProducerException("Failed to serialize message", e);
			}
			
			Message sqsMessage = new Message()
			    .withBody(msgBody);
            
            MessageFuture<Boolean> future = new MessageFuture<Boolean>(sqsMessage);
            sendBatcher.add(future);
            return future;
        }
    }
}
