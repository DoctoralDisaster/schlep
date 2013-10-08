package com.netflix.schlep.sqs.serializer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Charsets;
import com.netflix.schlep.mapper.Serializer;

/**
 * @author elandau
 */
public class Base64Serializer implements Serializer {
    private final Serializer component;
    
    public Base64Serializer(Serializer component) {
        this.component = component;
    }
    
    public Base64Serializer() {
        this(new DefaultSerializer());
    }
    
    @Override
    public <T> void serialize(T entity, OutputStream os) throws Exception {
        ByteArrayOutputStream strm = new ByteArrayOutputStream();
        component.serialize(entity, strm);
        byte[] bytes = strm.toString().getBytes(Charsets.UTF_8);
        byte[] base64Bytes = Base64.encodeBase64(bytes);
        os.write(base64Bytes);
    }

    @Override
    public <T> T deserialize(InputStream is, Class<T> clazz) throws Exception {
        // Read into stream
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
 
        // Baset64 decode
        byte[] origBytes = Base64.decodeBase64(sb.toString().getBytes());
        ByteArrayInputStream baos = new ByteArrayInputStream(origBytes);
        return component.deserialize(baos, clazz);
    }
}
