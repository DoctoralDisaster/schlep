package com.netflix.schlep.sqs.retry;

import org.apache.commons.configuration.Configuration;

/**
 * Factory for creating the appropriate retry policy from a configuration
 * 
 * @author elandau
 *
 */
public interface RetryPolicyFactory {
    public RetryPolicy create(Configuration configuration);
}
