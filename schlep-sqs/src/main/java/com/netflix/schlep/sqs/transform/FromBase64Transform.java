package com.netflix.schlep.sqs.transform;

import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Charsets;
import com.google.common.base.Function;

public class FromBase64Transform implements Function<String, String> {
    @Override
    public String apply(String input) {
        byte[] origBytes = Base64.decodeBase64(input.getBytes());
        return new String(origBytes, Charsets.UTF_8);
    }
}
