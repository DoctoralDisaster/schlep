package com.netflix.schlep.sqs;

import com.google.inject.Inject;
import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.SchlepModules;
import com.netflix.schlep.exception.ProducerException;
import com.netflix.schlep.serializer.Mapper;
import com.netflix.schlep.writer.MessageProducer;
import com.netflix.schlep.writer.MessageWriterFactory;

/**
 * Implementation of a message producer that sends messages to SQS
 * 
 * @author elandau
 */
public class SqsMessageProducerProvider implements MessageWriterFactory {
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
    public <T> MessageProducer<T> createProducer(EndpointKey<T> key, Mapper reader) throws ProducerException {
        try {
            SqsClientConfiguration config = reader.create(SqsClientConfiguration.class);
            SqsClient              client = clientFactory.create(config);

            return new SqsMessageProducer<T>(key, client, config, modules);
        } catch (Exception e) {
            throw new ProducerException(e);
        }
    }
}
