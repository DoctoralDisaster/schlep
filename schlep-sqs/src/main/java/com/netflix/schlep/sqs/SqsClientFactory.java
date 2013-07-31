package com.netflix.schlep.sqs;

public interface SqsClientFactory {
    public SqsClient create(SqsClientConfiguration clientConfig);
}
