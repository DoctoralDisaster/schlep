package com.netflix.schlep.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.google.common.base.Charsets;
import com.netflix.schlep.mapper.jackson.JacksonSerializer;

public class StringSerializer implements Serializer {
    private final Serializer next;
    
    public StringSerializer() {
        this(JacksonSerializer.get());
    }
    
    public StringSerializer(Serializer next) {
        this.next = next;
    }
    
    @Override
    public <T> void serialize(T entity, OutputStream os) throws Exception {
        if (entity.getClass().equals(String.class)) {
            byte[] bytes = ((String)entity).getBytes(Charsets.UTF_8);
            os.write(bytes);
        }
        else if (next == null) {
            throw new RuntimeException("Unsupported entity type: " + entity.getClass().getCanonicalName());
        }
        else {
            next.serialize(entity, os);
        }
    }

    @Override
    public <T> T deserialize(InputStream is, Class<T> clazz) throws Exception {
        if (clazz.equals(String.class)) {
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
     
            String line;
            try {
     
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
     
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
     
            return (T) sb.toString();
        }
        else if (next == null) {
            throw new RuntimeException("Unsupported entity type: " + clazz.getCanonicalName());
        }
        else {
            return next.deserialize(is, clazz);
        }
    }
}
