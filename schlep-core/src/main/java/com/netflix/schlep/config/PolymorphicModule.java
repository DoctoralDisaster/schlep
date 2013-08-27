package com.netflix.schlep.config;

import java.io.IOException;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.AbstractTypeResolver;
import org.codehaus.jackson.map.BeanPropertyDefinition;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.deser.BeanDeserializerBuilder;
import org.codehaus.jackson.map.deser.BeanDeserializerModifier;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.module.SimpleModule;
import org.codehaus.jackson.map.ser.BeanSerializerModifier;
import org.codehaus.jackson.map.ser.std.BeanSerializerBase;
import org.codehaus.jackson.type.JavaType;

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
    private final Set<Class<?>> deserializerClasses = Sets.newHashSet();
    
    private static final String CLASS_NAME_FIELD   = "_class";
    private static final String BUILDER_CLASS_NAME = "Builder";
    
    /**
     * Decorator for a serializer that add a concrete class name
     * to the serialized JSON.
     * 
     * @author elandau
     *
     */
    class ClassTypeFieldSerializer extends BeanSerializerBase {
        BeanSerializerBase src;
        
        protected ClassTypeFieldSerializer(BeanSerializerBase src) {
            super(src);
            this.src = src;
        }

        @Override
        public void serialize(Object bean, JsonGenerator jgen,
                SerializerProvider provider) throws IOException,
                JsonGenerationException {
            jgen.writeStartObject();
            jgen.writeStringField(CLASS_NAME_FIELD, bean.getClass().getCanonicalName()); 
            serializeFields(bean, jgen, provider);
            jgen.writeEndObject();
        }
    }
    
    /**
     * 
     * @author elandau
     *
     * @param <T>
     */
    class NoFieldSerializer<T> extends JsonSerializer<T> {
        @Override
        public void serialize(Object value,
                JsonGenerator jgen,
                SerializerProvider provider)
                throws IOException,
                JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeStringField(CLASS_NAME_FIELD, value.getClass().getCanonicalName()); 
            jgen.writeEndObject();
        }
    }
    
    @SuppressWarnings("unchecked")
    public PolymorphicModule() {
        super("polymorphic", Version.unknownVersion());
        
//        for (final Class<?> clazz : this.classes) {
//            this.addDeserializer(clazz, new JsonDeserializer() {
//                @Override
//                public Object deserialize(JsonParser jp,
//                        DeserializationContext ctxt) throws IOException,
//                        JsonProcessingException {
//                    // Deserialize all the fields into a tree.  This probably 
//                    // includes nested fields.  That's ok for now.
//                    JsonNode nodes = jp.readValueAsTree();
//                    
//                    // Look for the class name to have been serialized in the JSON
//                    JsonNode classField = nodes.findValue(CLASS_NAME_FIELD);
//                    if (classField != null) {
//                        try {
//                            Class<?> clazz = Class.forName(classField.asText());
//                            if (clazz != null) {
//                                // Look for the Builder inner class
//                                for (Class<?> builderClass : clazz.getClasses()) {
//                                    if (builderClass.getSimpleName().equals(BUILDER_CLASS_NAME)) {
//                                        Object builder = builderClass.newInstance(); 
//                                        // Iterate 'with' methods and set values from JSON
//                                        for (Method method : builderClass.getMethods()) {
//                                            String methodName = method.getName();
//                                            if (methodName.startsWith("with")) {
//                                                methodName = StringUtils.substring(methodName, "with".length());
//                                                methodName = StringUtils.uncapitalize(methodName);
//                                                
//                                                JsonNode node = nodes.get(methodName);
//                                                // Found a value
//                                                if (node != null) {
//                                                    Class<?> paramType = method.getParameterTypes()[0];
//                                                    if (paramType.equals(Integer.class) || paramType.equals(int.class)) {
//                                                        method.invoke(builder, node.asInt());
//                                                    }
//                                                    else if (paramType.equals(Double.class) || paramType.equals(double.class)) {
//                                                        method.invoke(builder, node.asDouble());
//                                                    }
//                                                    else if (paramType.equals(String.class)) {
//                                                        method.invoke(builder, node.asText());
//                                                    }
//                                                    else if (paramType.equals(Long.class) || paramType.equals(long.class)) {
//                                                        method.invoke(builder, node.asLong());
//                                                    }
//                                                    else if (paramType.equals(Boolean.class) || paramType.equals(boolean.class)) {
//                                                        method.invoke(builder, node.asBoolean());
//                                                    }
//                                                    else {
//                                                        System.out.println("With method: " + methodName);
//                                                    }
//                                                }
//                                            }
//                                        }
//                                        
//                                        Method buildMethod = builderClass.getMethod("build");
//                                        return buildMethod.invoke(builder);
//                                    }
//                                }
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    System.out.println("No serializer ");
//                    return null;
//                }
//            });
//        }
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
                
                // When reaching the polimorphic class added above add the proper
                // serializer wrapper to encode the type information
                Class<?>[] interfaces = beanDesc.getBeanClass().getInterfaces();
                if (interfaces.length == 1) {
                    synchronized (serializerClasses) {
                        if (serializerClasses.contains(interfaces[0])) {
                            if (serializer != null) 
                                return new ClassTypeFieldSerializer((BeanSerializerBase) serializer);
                            else
                                return new NoFieldSerializer();
                        }
                    }
                }
                return serializer;
            }
        });
        
        context.addBeanDeserializerModifier(new BeanDeserializerModifier() {
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                    BasicBeanDescription beanDesc, BeanDeserializerBuilder builder) {
                System.out.println("addBeanDeserializerModifier " + beanDesc.getBeanClass());
                return builder;
            }

            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                    BasicBeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                System.out.println("modifyDeserializer: " + beanDesc.getBeanClass());
                return deserializer;
            }
        });
        
        context.addAbstractTypeResolver(new AbstractTypeResolver() {
            public JavaType findTypeMapping(DeserializationConfig config, JavaType type) {
                if (type.isInterface()) {
                    System.out.println("findTypeMapping: " + type);
                }
                return null;
            }
            public JavaType resolveAbstractType(DeserializationConfig config, JavaType type) {
                if (type.isInterface()) {
                    System.out.println("resolveAbstractType: " + type);
                    return config.getTypeFactory().constructFromCanonical(type.getRawClass() + "Impl");
                }
                return null;
            }    
        });
    }
}
