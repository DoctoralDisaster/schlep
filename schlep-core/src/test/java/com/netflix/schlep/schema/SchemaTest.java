package com.netflix.schlep.schema;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.junit.Test;

import com.netflix.schlep.mapper.jackson.SchlepJacksonModule;
import com.netflix.schlep.mapper.schema.commons.CommonsSchemaGenerator;
import com.netflix.schlep.mapper.schema.types.Schema;
import com.netflix.schlep.serializer.SimpleEntity;

public class SchemaTest {
   
    @Test
    public void test() {
        CommonsSchemaGenerator generator = new CommonsSchemaGenerator();
        generator.getSchema(SimpleEntity.class);
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.setSerializationInclusion(Inclusion.NON_EMPTY);
        
        mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new SchlepJacksonModule());
        
        for (Schema schema : generator.getCachedSchemas()) {
//            System.out.println("=== " + schema);
            try {
                System.out.println(mapper.defaultPrettyPrintingWriter().writeValueAsString(schema));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }
}
