package com.netflix.schlep.mapper.jackson;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.BeanPropertyDefinition;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.deser.BeanDeserializerBuilder;
import org.codehaus.jackson.map.deser.BeanDeserializerModifier;
import org.codehaus.jackson.map.deser.SettableBeanProperty;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.map.ser.BeanSerializerModifier;
import org.codehaus.jackson.map.ser.std.BeanSerializerBase;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

import com.google.common.collect.Sets;

/**
 * I'm not happy about having to write this module but I couldn't find any other
 * way to enable polymorphic serialization of classes without annotations.
 * 
 * @author elandau
 *
 */
public class PolymorphicModule extends SimpleModule {
    private final Set<Class<?>> serializerClasses = Sets.newHashSet();
    
    public static final String  CLASS_NAME_FIELD  = "_class";
    public static final String  TYPE_NAME_FIELD  = "type";
    public static final String  MODULE_NAME       = "PolymorphicModule";
    public static final Version MODULE_VERSION    = new Version(1, 0, 0, null);

    /**
     * Decorator for a serializer that add a concrete class name
     * to the serialized JSON.
     * 
     * @author elandau
     *
     */
    class ClassTypeFieldSerializer extends BeanSerializerBase {
        private BeanSerializerBase src;
        
        private final String typeField;
        private final String typeValue;
        
        protected ClassTypeFieldSerializer(Class<?> iface, BeanSerializerBase src) {
            super(src);
            this.src = src;
            
            if (iface.getPackage().getName().equals(src.handledType().getPackage().getName())) {
                this.typeField = TYPE_NAME_FIELD;
                this.typeValue = src.handledType().getSimpleName();
            }
            else {
                this.typeField = CLASS_NAME_FIELD;
                this.typeValue = src.handledType().getCanonicalName();
            }
        }

        @Override
        public void serialize(Object bean, JsonGenerator jgen,
                SerializerProvider provider) throws IOException,
                JsonGenerationException {
            jgen.writeStartObject();
            jgen.writeStringField(this.typeField, this.typeValue); 
            serializeFields(bean, jgen, provider);
            jgen.writeEndObject();
        }
    }
    
    /**
     * Similar to ClassTypeFieldSerializer except for that there is no base
     * serializer to wrap as the class has no properties
     * @author elandau
     *
     * @param <T>
     */
    class NoPropertiesSerializer<T> extends JsonSerializer<T> {
        private final String typeField;
        private final String typeValue;
        
        public NoPropertiesSerializer(Class<?> iface, Class<?> bean) {
            if (iface.getPackage().getName().equals(bean.getPackage().getName())) {
                this.typeField = TYPE_NAME_FIELD;
                this.typeValue = bean.getSimpleName();
            }
            else {
                this.typeField = CLASS_NAME_FIELD;
                this.typeValue = bean.getCanonicalName();
            }
        }

        @Override
        public void serialize(Object value,
                JsonGenerator jgen,
                SerializerProvider provider)
                throws IOException,
                JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeStringField(this.typeField, this.typeValue); 
            jgen.writeEndObject();
        }
    }

    /**
     * Deserializer that looks for the class name in a polymorphic json tree of a 
     * polymorphic type and deserializes to the correct type.
     * 
     * @author elandau
     *
     * @param <T>
     */
    class ClassTypeFieldDeserializer<T> extends StdDeserializer<T> {
        private final String packageName;
        
        protected ClassTypeFieldDeserializer(Class<T> clazz) {
            super(clazz);
            
            packageName = clazz.getPackage().getName();
        }

        @Override
        public T deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            String   className = "unknown";
            
            // Get the CLASS_NAME_FIELD from json
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();  
            ObjectNode   root   = (ObjectNode  ) mapper.readTree(jp);  
            TextNode     type   = (TextNode  )   root.get(TYPE_NAME_FIELD);
            // Field describes the simple type
            if (type != null) {
                className = StringUtils.join(new String[]{packageName, type.asText()}, ".");
                
                // Discard the CLASS_NAME_FIELD so it doesn't interfere
                root.remove(TYPE_NAME_FIELD);
            }
            // Field describes a full class name
            else {
                type            = (TextNode  )   root.get(CLASS_NAME_FIELD);
                if (type == null)
                    return null;
                
                className = type.asText();
                
                // Discard the CLASS_NAME_FIELD so it doesn't interfere
                root.remove(CLASS_NAME_FIELD);
            }
            
            // Try to determine the actual class
            Class<?> clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new IOException("Failed to get class type for " + className, e);
            }
            
            // Deserialize using the concrete type
            return (T) mapper.readValue(root, clazz);
        }
    }
    
    @SuppressWarnings("unchecked")
    public PolymorphicModule() {
        super(MODULE_NAME, MODULE_VERSION);
    }
    
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        
        context.addBeanSerializerModifier(new BeanSerializerModifier() {
            public JsonSerializer<?> modifySerializer(SerializationConfig config,
                    BasicBeanDescription beanDesc, JsonSerializer<?> serializer) {
                
                // Iterate all the property setters to determine if any are for interfaces.
                // Add polymorphics support for all interface based setters.
                if (serializer != null) {
                    for (BeanPropertyDefinition prop : beanDesc.findProperties()) {
                        AnnotatedMethod setter = prop.getSetter();
                        if (setter != null && setter.getParameterClass(0).isInterface()) {
                            synchronized (serializerClasses) {
                                serializerClasses.add(setter.getParameterClass(0));
                            }
                        }
                    }
                }
                
                // When reaching the polymorphic class added above add the proper
                // serializer wrapper to encode the type information
                Class<?>[] interfaces = beanDesc.getBeanClass().getInterfaces();
                if (interfaces.length == 1) {
                    synchronized (serializerClasses) {
                        if (serializerClasses.contains(interfaces[0])) {
                            if (serializer != null) 
                                return new ClassTypeFieldSerializer(interfaces[0], (BeanSerializerBase) serializer);
                            else
                                return new NoPropertiesSerializer(interfaces[0], beanDesc.getBeanClass());
                        }
                    }
                }
                return serializer;
            }
        });
        
        context.addBeanDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BasicBeanDescription beanDesc, BeanDeserializerBuilder builder) {
                /**
                 * Treat every property that is an interface as policy driven.  Look for implementations
                 * in the same package as the interface
                 */
                Iterator<SettableBeanProperty> beanPropertyIterator = builder.getProperties();
                while (beanPropertyIterator.hasNext()) {
                    SettableBeanProperty settableBeanProperty = beanPropertyIterator.next();
                    // It's an interface!!
                    if (settableBeanProperty.getType().isInterface()) {
                        builder.addOrReplaceProperty(
                            settableBeanProperty.withValueDeserializer(
                                    new ClassTypeFieldDeserializer(settableBeanProperty.getType().getRawClass())),
                            true);
                    }
                }
                return builder;
            }
        });
    }
}
