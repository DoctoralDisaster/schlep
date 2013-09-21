package com.netflix.schlep.kafka;

import kafka.serializer.Decoder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.Map;

public class JsonMapDecoder implements Decoder<Map<Object, Object>> {
    private ObjectMapper jsonMapper = new ObjectMapper();
    private TypeReference<Map<Object, Object>> typeRef = new TypeReference<Map<Object, Object>>() {};

    @Override
    public Map<Object, Object> fromBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        } else {
            try {
                return jsonMapper.readValue(bytes, typeRef);
            } catch (IOException e) {
                return null;
            }
        }
    }
}
