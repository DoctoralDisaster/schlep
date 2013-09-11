package com.netflix.schlep.sqs;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.sqs.model.Message;
import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.SchlepModules;
import com.netflix.schlep.exception.ProducerException;
import com.netflix.schlep.mapper.Serializer;
import com.netflix.schlep.sqs.transform.NoOpTransform;
import com.netflix.schlep.sqs.transform.ToBase64Transform;
import com.netflix.schlep.writer.MessageProducer;
import com.netflix.schlep.writer.MessageProducerContext;
import com.netflix.schlep.writer.OutgoingMessage;
import com.netflix.util.batch.Batcher;
import com.netflix.util.retry.RetryPolicy;

/**
 * MessageProducer that sends messages to an SQS queue.
 * 
 * @author elandau
 *
 * @param <T>
 */
public class SqsMessageProducer<T> implements MessageProducer<T> {
    private final SqsClient              client;
    private final EndpointKey<T>         key;
    private final SqsClientConfiguration clientConfig;
    private final RetryPolicy            retryPolicy;
    private final Serializer<T>          serializer;
    private final MessageDigest          digest;
    private final Function<String, String> transform;
    private final Batcher<MessageFuture<Boolean>>   sendBatcher;
  
    public SqsMessageProducer(
            EndpointKey<T>          key, 
            SqsClient               client,
            SqsClientConfiguration  config, 
            SchlepModules           modules)
                    throws ProducerException {
        
        try {
            this.digest       = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new ProducerException(e.getMessage(), e);
        }
        this.key          = key;
        
        MessageProducerContext<T> context = new MessageProducerContext<T>(this, key.getMessageType());
        
        this.serializer   = 
                modules.changeSerializer(
                        context,
                        config.getSerializerFactory() == null ?
                        modules.getDefaultSerializer(context) :
                        config.getSerializerFactory().findSerializer(key.getMessageType())); 
        
        this.clientConfig = config;
        this.client       = client;
        this.retryPolicy  = clientConfig.getRetryPolicy();
        
        if (clientConfig.getEnable64Encoding()) {
            transform = new ToBase64Transform();
        }
        else {
            transform = new NoOpTransform();
        }
        
        this.sendBatcher     = null;
//                clientConfig.getBatchPolicy().create(new Function<List<MessageFuture<Boolean>>, Boolean>() {
//            public Boolean apply(List<MessageFuture<Boolean>> messages) {
//                SqsMessageProducer.this.client.sendMessages(messages);
//                return true;
//            }
//        });

    }

    @Override
    public ListenableFuture<Boolean> produce(T message) throws ProducerException {
        return produce(OutgoingMessage.<T>builder().withMessage(message).build());
    }
    
    @Override
    public ListenableFuture<Boolean> produce(OutgoingMessage<T> message) throws ProducerException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            this.serializer.serialize(message.getMessage(), baos);
        } catch (Exception e) {
            throw new ProducerException("Failed to serialize message", e);
        }
        
        Message sqsMessage = new Message()
            .withBody(this.transform.apply(baos.toString()));
        
        MessageFuture<Boolean> future = new MessageFuture<Boolean>(sqsMessage);
        sendBatcher.add(future);
        return future;
    }

    @Override
    public Map<OutgoingMessage<T>, ListenableFuture<Boolean>> produceBatch(List<OutgoingMessage<T>> messages) {
//        sendBatcher.add(messages);
        // TODO: 
        return null;
    }

    @Override
    public String getId() {
        return key.getName();
    }

    @Override
    public String getUri() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<T> getMessageType() {
        return key.getMessageType();
    }
}
