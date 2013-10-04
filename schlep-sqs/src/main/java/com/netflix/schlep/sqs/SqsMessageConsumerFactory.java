package com.netflix.schlep.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.netflix.schlep.consumer.MessageConsumer;
import com.netflix.schlep.consumer.MessageConsumerFactory;
import com.netflix.schlep.exception.ConsumerException;
import com.netflix.schlep.serializer.Mapper;

/**
 * Implementation of a consumer that reads messages from an SQS queue
 * 
 * @author elandau
 */
public class SqsMessageConsumerFactory implements MessageConsumerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SqsMessageConsumerFactory.class);
    
    private Provider<AWSCredentials> credentialProvider;
    
    @Inject
    public SqsMessageConsumerFactory(
            Provider<AWSCredentials> credentialProvider
        ) {
        this.credentialProvider = credentialProvider;
    }

    @Override
    public MessageConsumer createConsumer(String id, Mapper mapper) throws ConsumerException {
        try {
            SqsMessageConsumer.Builder<?> builder = SqsMessageConsumer.builder();
            
            builder.withId(id);
            builder.withCredentials(credentialProvider.get());
            
            LOG.info("Building " + builder.toString());
            
            // builder.withRegion();
            // TODO: Bind region
            
            mapper.apply(builder);
            return builder.build();
        } catch (Exception e) {
            
            throw new ConsumerException("Failed to create instance of SimMessageConsumer", e);
        }
    }
}
