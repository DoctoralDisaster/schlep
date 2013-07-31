package com.netflix.schlep.sqs.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * Serialize entities to/from JSON strings.  
 * 
 * Meant to be used with very simple POJOs and only uses the fields.
 * 
 * @author elandau
 *
 * @param <T>
 */
public class JacksonSerializer<T> implements Serializer<T> {

    private final ObjectMapper mapper;
    private final Class<T>  	 clazz;
    
    public JacksonSerializer(Class<T> clazz) {
    	this.clazz = clazz;
    	
    	try {
            clazz.getConstructor();
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Missing default constructor for " + clazz.getCanonicalName());
        }
        mapper = new ObjectMapper();
        
        mapper.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.setSerializationInclusion(Inclusion.NON_EMPTY);
        
        mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enableDefaultTyping();
        
        mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(Visibility.ANY)
                .withGetterVisibility(Visibility.NONE)
                .withSetterVisibility(Visibility.NONE)
                .withCreatorVisibility(Visibility.NONE)
                .withIsGetterVisibility(Visibility.NONE));
    }
    
    @Override
    public String serialize(T entity) throws Exception {
        if (entity == null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
 	    mapper.writeValue(baos, entity);
 	    baos.flush();
 	    return baos.toString();
    }

    @Override
    public T deserialize(String str) throws Exception {
        if (str == null)
            return null;
        return (T) mapper.readValue(
                new ByteArrayInputStream(str.getBytes()),
	           clazz);
    }
}
