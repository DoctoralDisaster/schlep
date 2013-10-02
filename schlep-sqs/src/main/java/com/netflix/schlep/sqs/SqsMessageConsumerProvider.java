package com.netflix.schlep.sqs;

import com.google.inject.Inject;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.consumer.MessageCallback;
import com.netflix.schlep.consumer.MessageConsumer;
import com.netflix.schlep.consumer.MessageConsumerFactory;
import com.netflix.schlep.exception.ConsumerException;
import com.netflix.schlep.serializer.Mapper;

/**
 * Implementation of a consumer that reads messages from an SQS queue
 * 
 * @author elandau
 */
public class SqsMessageConsumerProvider implements MessageConsumerFactory {
    private SqsClientFactory clientFactory;
    
    @Inject
    public SqsMessageConsumerProvider(
            SqsClientFactory              clientFactory
            ) {
        this.clientFactory        = clientFactory;
    }
    
    @Override
    public <T> MessageConsumer<T> createConsumer(EndpointKey<T> key, Mapper reader, MessageCallback<T> callback) throws ConsumerException {
        try {
            SqsClientConfiguration config = reader.create(SqsClientConfiguration.class);
            SqsClient              client = clientFactory.create(config);

            return new SqsMessageConsumer<T>(key, client, config, callback);
        } catch (Exception e) {
            throw new ConsumerException(e);
        }
    }
}
