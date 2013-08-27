package com.netflix.schlep.sqs;

import com.google.inject.Inject;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.SchlepModules;
import com.netflix.schlep.config.ConfigurationReader;
import com.netflix.schlep.exception.ProducerException;
import com.netflix.schlep.producer.MessageProducer;
import com.netflix.schlep.producer.MessageProducerFactory;

/**
 * Implementation of a message producer that sends messages to SQS
 * 
 * @author elandau
 */
public class SqsMessageProducerProvider implements MessageProducerFactory {
    private final SqsClientFactory        clientFactory;
    private final SchlepModules           modules;
    @Inject
    public SqsMessageProducerProvider(
            SqsClientFactory        clientFactory,
            SchlepModules           modules
            ) {
        this.clientFactory  = clientFactory;
        this.modules        = modules;
    }
    
    @Override
    public <T> MessageProducer<T> createProducer(EndpointKey<T> key, ConfigurationReader reader) throws ProducerException {
        try {
            SqsClientConfiguration config = reader.create(SqsClientConfiguration.class);
            SqsClient              client = clientFactory.create(config);

            return new SqsMessageProducer<T>(key, client, config, modules);
        } catch (Exception e) {
            throw new ProducerException(e);
        }
    }
}
