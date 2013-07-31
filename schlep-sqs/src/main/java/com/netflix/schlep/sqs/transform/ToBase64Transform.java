package com.netflix.schlep.sqs.transform;

import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Charsets;
import com.google.common.base.Function;

public class ToBase64Transform implements Function<String, String> {
    @Override
    public String apply(String msg) {
        byte[] base64Bytes = Base64.encodeBase64(msg.getBytes(Charsets.UTF_8));
        return new String(base64Bytes);
    }
}
