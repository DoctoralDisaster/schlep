package com.netflix.schlep;

import com.netflix.schlep.exception.ConsumerException;

public interface MessageConsumerProvider {
    public <T> MessageConsumer<T> subscribe(EndpointKey<T> key, MessageCallback<T> callback) throws ConsumerException;
}
