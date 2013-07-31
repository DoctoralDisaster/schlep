package com.netflix.schlep.sqs.transform;

import com.google.common.base.Function;

public class NoOpTransform implements Function<String, String> {
    @Override
    public String apply(String input) {
        return input;
    }
}
