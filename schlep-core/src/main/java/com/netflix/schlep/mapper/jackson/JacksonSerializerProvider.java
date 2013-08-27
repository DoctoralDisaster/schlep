package com.netflix.schlep.mapper.jackson;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.google.inject.TypeLiteral;
import com.netflix.schlep.mapper.Serializer;
import com.netflix.schlep.mapper.SerializerProvider;

import java.io.*;

/**
 * Serialize entities to/from JSON strings.  
 * 
 * Meant to be used with very simple POJOs and only uses the fields.
 * 
 * @author elandau
 *
 * @param <T>
 */
public class JacksonSerializerProvider implements SerializerProvider {
    private static final ObjectMapper mapper;

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
    }
    
    @Override
    public <T> Serializer<T> findSerializer(final Class<T> clazz) {
        return new Serializer<T>() {
            {
            	try {
                    clazz.getConstructor();
                } catch (SecurityException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Missing default constructor for " + clazz.getCanonicalName());
                }
            }
            
            @Override
            public void serialize(T entity, OutputStream os) throws Exception {
                if (entity != null) {
                    os.write(mapper.writeValueAsString(entity).getBytes());
                }
            }
        
            @Override
            public T deserialize(InputStream is) throws Exception {
                return (T) mapper.readValue(is, clazz);
            }

            @Override
            public Class<T> getType() {
                return clazz;
            }
        };
    }

    @Override
    public <T> Serializer<T> findSerializer(TypeLiteral<T> type) {
        return (Serializer<T>) findSerializer(type.getRawType());
    }
}
