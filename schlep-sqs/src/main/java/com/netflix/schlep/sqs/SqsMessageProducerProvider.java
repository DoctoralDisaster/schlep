package com.netflix.schlep.sqs;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.configuration.AbstractConfiguration;

import com.amazonaws.services.sqs.model.Message;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.inject.Inject;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.MessageProducer;
import com.netflix.schlep.MessageProducerProvider;
import com.netflix.schlep.OutgoingMessage;
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
    private final AbstractConfiguration         config;

    @Inject
    public SqsMessageProducerProvider(
            SqsClientFactory              clientFactory,
            SqsClientConfigurationFactory configurationFactory,
            AbstractConfiguration         config
            ) {
        this.clientFactory        = clientFactory;
        this.configurationFactory = configurationFactory;
        this.config               = config;
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
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public ListenableFuture<Boolean> produce(T message) throws ProducerException {
            return produce(new OutgoingMessage<T>().withMessage(message));
        }
        
        @Override
        public ListenableFuture<Boolean> produce(OutgoingMessage<T> message) throws ProducerException {
            SettableFuture<Boolean> future = SettableFuture.create();
            
            String msgBody;
			try {
				msgBody = this.transform.apply(this.serializer.serialize(message.getMessage()));
			} catch (Exception e) {
				throw new ProducerException("Failed to serialize message", e);
			}
			
			Message sqsMessage = new Message()
			    .withBody(msgBody);
            
            client.sendMessages(ImmutableList.of(new MessageAndFuture<Boolean>(sqsMessage, future)));
            return future;
        }
    }
}
