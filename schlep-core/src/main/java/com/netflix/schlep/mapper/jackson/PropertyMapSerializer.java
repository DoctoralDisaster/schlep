package com.netflix.schlep.mapper.jackson;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.MapSerializer;

public class PropertyMapSerializer extends MapSerializer {
//    @Override
//    public void serialize(Map<?,?> value, JsonGenerator jgen, SerializerProvider provider)
//            throws IOException, JsonGenerationException {
//        jgen.writeStartObject();
//        for (Entry<?, ?> entry : value.entrySet()) {
//            jgen.writeObjectField(entry.getKey().toString(), entry.getValue());
//        }
//        jgen.writeEndObject();
//    }

}