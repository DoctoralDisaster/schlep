package com.netflix.schlep.mapper.jackson;

import java.io.InputStream;
import java.io.OutputStream;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.netflix.schlep.mapper.Serializer;

public class JacksonSerializer implements Serializer {
    private static final ObjectMapper mapper;
    private static final JacksonSerializer instance = new JacksonSerializer();
    
    /**
     * Set up a Jackson ObjectMapper with some defaults that we have deemed to 
     * be ideal for a simple Pojo where only fields are considered.
     */
    static {
        mapper = new ObjectMapper();
        
        mapper.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.setSerializationInclusion(Inclusion.NON_EMPTY);
        
        mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enableDefaultTyping();
        
        mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(Visibility.NONE)
                .withGetterVisibility(Visibility.ANY)
                .withSetterVisibility(Visibility.NONE)
                .withCreatorVisibility(Visibility.NONE)
                .withIsGetterVisibility(Visibility.NONE));
        
        mapper.withModule(new PolymorphicModule());
    }
    
    public static JacksonSerializer get() {
        return instance;
    }
    
    @Override
    public <T> void serialize(T entity, OutputStream os) throws Exception {
        if (entity != null) {
            os.write(mapper.writeValueAsString(entity).getBytes());
        }
    }

    @Override
    public <T> T deserialize(InputStream is, Class<T> clazz2) throws Exception {
        return (T) mapper.readValue(is, clazz2);
    }

}
