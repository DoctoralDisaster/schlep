package com.netflix.schlep.apq;

import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.MessageProducer;
import com.netflix.schlep.MessageProducerProvider;
import com.netflix.schlep.exception.ProducerException;

public class ApqMessageProducerProvider implements MessageProducerProvider {

    @Override
    public <T> MessageProducer<T> getProducer(EndpointKey<T> key) throws ProducerException {
        // TODO Auto-generated method stub
        return null;
    }

}
