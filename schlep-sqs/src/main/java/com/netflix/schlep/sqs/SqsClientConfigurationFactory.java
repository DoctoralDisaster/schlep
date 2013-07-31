package com.netflix.schlep.sqs;

public interface SqsClientConfigurationFactory {
    public SqsClientConfiguration get(String queueName);
}
