package com.netflix.schlep.serializer;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.Module.SetupContext;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.map.ser.BeanPropertyWriter;
import org.codehaus.jackson.map.ser.BeanSerializerModifier;
import org.codehaus.jackson.map.ser.std.BeanSerializerBase;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.schema.JsonSchema;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Module;

public class SerializerTest {
    class SchemaDiscoverySerializer extends BeanSerializerBase {
        private BeanSerializerBase src;
        
        protected SchemaDiscoverySerializer(BeanSerializerBase src) {
            super(src);
            
            this.src = src;
        }

        public JsonNode getSchema(SerializerProvider provider, Type typeHint)
                throws JsonMappingException {
            System.out.println("---");
            for (BeanPropertyWriter prop : this._props) {
                System.out.println(prop.getName() + " " + prop.getGenericPropertyType());
            }
            JsonNode node = super.getSchema(provider, typeHint);
            ObjectNode o = (ObjectNode)node;
            o.put("_class", this._handledType.getCanonicalName());
            return node;
        }

        @Override
        public void serialize(Object bean, JsonGenerator jgen,
                SerializerProvider provider) throws IOException,
                JsonGenerationException {
            src.serialize(bean, jgen, provider);
        }
        
    }

    @Test
    public void testJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule("foo", Version.unknownVersion()) {
            public void setupModule(SetupContext context) {
                super.setupModule(context);
                
                context.addBeanSerializerModifier(new BeanSerializerModifier() {
                    public JsonSerializer<?> modifySerializer(SerializationConfig config,
                            BasicBeanDescription beanDesc, JsonSerializer<?> serializer) {
                        return new SchemaDiscoverySerializer((BeanSerializerBase)serializer);
                    }
                });
            }
        });
        
        JsonSchema schema = mapper.generateJsonSchema(SimpleEntity.class);
        System.out.println(mapper.defaultPrettyPrintingWriter().writeValueAsString(schema));
    }
    
    @Test
    public void testApi() throws Exception {
        SimpleEntity entity = new SimpleEntity();
        for (PropertyDescriptor prop : PropertyUtils.getPropertyDescriptors(SimpleEntity.class)) {
            System.out.println("---");
            System.out.println(prop.getDisplayName());
            System.out.println(prop.getName());
            System.out.println(prop.getPropertyType().getCanonicalName());
        }
    }
}
