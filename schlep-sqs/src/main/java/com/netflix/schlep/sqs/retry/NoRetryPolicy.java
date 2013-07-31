package com.netflix.schlep.sqs.retry;

import java.util.concurrent.Callable;

import org.apache.commons.configuration.Configuration;

import com.google.inject.Inject;

public class NoRetryPolicy implements RetryPolicyFactory {
    @Inject
    public NoRetryPolicy() {
    }
    
    @Override
    public RetryPolicy create(final Configuration configuration) {
        return new RetryPolicy() {
            @Override
            public <R> Callable<R> wrap(final Callable<R> callable) {
                return new Callable<R>() {
                    @Override
                    public R call() throws Exception {
                        try {
                            return callable.call();
                        }
                        catch (NotRetryableException e) {
                            throw e.getCause();
                        }
                    }
                };
            }
        };
    }
}
