package com.netflix.schlep.apq;

import com.netflix.schlep.EndpointKey;
import com.netflix.schlep.MessageCallback;
import com.netflix.schlep.MessageConsumer;
import com.netflix.schlep.MessageConsumerProvider;
import com.netflix.schlep.exception.ConsumerException;

public class ApqMessageConsumerProvider implements MessageConsumerProvider {

    @Override
    public <T> MessageConsumer<T> subscribe(EndpointKey<T> key, MessageCallback<T> callback) throws ConsumerException {
        // TODO Auto-generated method stub
        return null;
    }

}
