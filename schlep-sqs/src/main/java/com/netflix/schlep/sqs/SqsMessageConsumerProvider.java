package com.netflix.schlep.sqs;

import com.google.inject.Inject;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.config.ConfigurationReader;
import com.netflix.schlep.exception.ConsumerException;
import com.netflix.schlep.reader.MessageCallback;
import com.netflix.schlep.reader.MessageReader;
import com.netflix.schlep.reader.MessageReaderFactory;

/**
 * Implementation of a consumer that reads messages from an SQS queue
 * 
 * @author elandau
 */
public class SqsMessageConsumerProvider implements MessageReaderFactory {
    private SqsClientFactory clientFactory;
    
    @Inject
    public SqsMessageConsumerProvider(
            SqsClientFactory              clientFactory
            ) {
        this.clientFactory        = clientFactory;
    }
    
    @Override
    public <T> MessageReader<T> createConsumer(EndpointKey<T> key, ConfigurationReader reader, MessageCallback<T> callback) throws ConsumerException {
        try {
            SqsClientConfiguration config = reader.create(SqsClientConfiguration.class);
            SqsClient              client = clientFactory.create(config);

            return new SqsMessageConsumer<T>(key, client, config, callback);
        } catch (Exception e) {
            throw new ConsumerException(e);
        }
    }
}
