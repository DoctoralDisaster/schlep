package com.netflix.schlep.sqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.netflix.schlep.exception.ProducerException;
import com.netflix.schlep.producer.MessageProducer;
import com.netflix.schlep.producer.MessageProducerFactory;
import com.netflix.schlep.serializer.Mapper;

public class SqsMessageProducerFactory implements MessageProducerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SqsMessageProducerFactory.class);
    
    private Provider<AWSCredentials> credentialProvider;
    
    @Inject
    public SqsMessageProducerFactory(
            Provider<AWSCredentials> credentialProvider
        ) {
        this.credentialProvider = credentialProvider;
    }

    @Override
    public MessageProducer create(String id, Mapper mapper) throws ProducerException {
        try {
            SqsMessageProducer.Builder<?> builder = SqsMessageProducer.builder();
            
            builder.withId(id);
            builder.withCredentials(credentialProvider.get());
            
            LOG.info("Building " + builder.toString());
            
            // builder.withRegion();
            // TODO: Bind region
            
            mapper.apply(builder);
            return builder.build();
        } catch (Exception e) {
            throw new ProducerException("Failed to create instance of SimMessageProducer", e);
        }
    }
}
