package com.netflix.schlep.mapper.schema.commons;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.netflix.schlep.mapper.schema.SchemaGenerator;
import com.netflix.schlep.mapper.schema.types.BooleanType;
import com.netflix.schlep.mapper.schema.types.DoubleType;
import com.netflix.schlep.mapper.schema.types.FloatType;
import com.netflix.schlep.mapper.schema.types.IntegerType;
import com.netflix.schlep.mapper.schema.types.ListType;
import com.netflix.schlep.mapper.schema.types.LongType;
import com.netflix.schlep.mapper.schema.types.MapType;
import com.netflix.schlep.mapper.schema.types.ObjectSchema;
import com.netflix.schlep.mapper.schema.types.Schema;
import com.netflix.schlep.mapper.schema.types.SetType;
import com.netflix.schlep.mapper.schema.types.ShortType;
import com.netflix.schlep.mapper.schema.types.StringType;

/**
 * Utility class to generate schema elements using apache commons bean utils to 
 * scan for properties
 * 
 * Note that Apache Commons BeanUtils will only treat fields for which there is a 
 * getter (getXXX) as properties.
 * 
 * @author elandau
 *
 */
public class CommonsSchemaGenerator implements SchemaGenerator {
    
    private Map<Class<?>, Schema> schemaCache = Maps.newHashMap();
    
    public static interface PropertyResolver {
        public Schema resolve(PropertyDescriptor descriptor);
    }    
    
    public ImmutableMultimap<Class<?>, PropertyResolver> resolvers = ImmutableMultimap.<PropertyResolver, Class<?>>builder()
            .putAll(new PropertyResolver() {
                @Override
                public Schema resolve(PropertyDescriptor descriptor) {
                    return new IntegerType();
                }
            }, Integer.class, int.class)
            .putAll(new PropertyResolver() {
                @Override
                public Schema resolve(PropertyDescriptor descriptor) {
                    return new LongType();
                }
            }, Long.class, long.class)
            .putAll(new PropertyResolver() {
                @Override
                public Schema resolve(PropertyDescriptor descriptor) {
                    return new BooleanType();
                }
            }, boolean.class, Boolean.class)
            .putAll(new PropertyResolver() {
                @Override
                public Schema resolve(PropertyDescriptor descriptor) {
                    return new ShortType();
                }
            }, short.class, Short.class)
            .putAll(new PropertyResolver() {
                @Override
                public Schema resolve(PropertyDescriptor descriptor) {
                    return new FloatType();
                }
            }, Float.class, float.class)
            .putAll(new PropertyResolver() {
                @Override
                public Schema resolve(PropertyDescriptor descriptor) {
                    return new DoubleType();
                }
            }, Double.class, double.class)
            .put(new PropertyResolver() {
                @Override
                public Schema resolve(PropertyDescriptor descriptor) {
                    return new StringType();
                }
            }, String.class)
            .put(new PropertyResolver() {
                @Override
                public Schema resolve(PropertyDescriptor descriptor) {
                    MapType schema = new MapType();
                    Method method = descriptor.getReadMethod();
                    Type genericFieldType = method.getGenericReturnType();
                    if (genericFieldType instanceof ParameterizedType){
                        ParameterizedType aType = (ParameterizedType) genericFieldType;
                        Type[] fieldArgTypes = aType.getActualTypeArguments();
                        Type keyType = fieldArgTypes[0];
                        Type valueType = fieldArgTypes[1];
                        schema.setKeyType(getSchema((Class<?>)keyType));
                        schema.setValueType(getSchema((Class<?>)valueType));
                    }
                    
                    return schema;
                }
            }, Map.class)
            .put(new PropertyResolver() {
                @Override
                public Schema resolve(PropertyDescriptor descriptor) {
                    ListType schema = new ListType();
                    Method method = descriptor.getReadMethod();
                    Type genericFieldType = method.getGenericReturnType();
                    if (genericFieldType instanceof ParameterizedType){
                        ParameterizedType aType = (ParameterizedType) genericFieldType;
                        Type[] fieldArgTypes = aType.getActualTypeArguments();
                        Type valueType = fieldArgTypes[0];
                        schema.setValueType(getSchema((Class<?>) valueType));
                    }
                    return schema;
                }
            }, List.class)
            .put(new PropertyResolver() {
                @Override
                public Schema resolve(PropertyDescriptor descriptor) {
                    SetType schema = new SetType();
                    Method method = descriptor.getReadMethod();
                    Type genericFieldType = method.getGenericReturnType();
                    if (genericFieldType instanceof ParameterizedType){
                        ParameterizedType aType = (ParameterizedType) genericFieldType;
                        Type[] fieldArgTypes = aType.getActualTypeArguments();
                        Type valueType = fieldArgTypes[0];
                        schema.setValueType(getSchema((Class<?>)valueType));
                    }
                    return schema;
                }
            }, Set.class)
            .build().inverse();
    
    public Collection<Schema> getCachedSchemas() {
        return schemaCache.values();
    }
    
    /**
     * Generate a schema by inspecting a clazz.  The clazz will be cached so it can be 
     * referenced later when embedded in another entity.
     * 
     * @param clazz
     * @return
     */
    @Override
    public Schema getSchema(Class<?> clazz) {
        if (schemaCache.containsKey(clazz)) {
            return schemaCache.get(clazz);
        }
        
        ImmutableCollection<PropertyResolver> resolver = resolvers.get(clazz);
        if (resolver == null || resolver.isEmpty()) {
            return getDefaultResolver(clazz).resolve(null);
        }
        else {
            return resolver.iterator().next().resolve(null);
        }
    }
    
    public String getPropertyNameFromMethodName(String prefix, Method method) {
        return StringUtils.uncapitalize(StringUtils.center(method.getName(), prefix.length()));
    }
    
    public String getMethodNameFromPropertyName(String prefix, String name) {
        return prefix + StringUtils.capitalize(name);
    }
    
    public PropertyResolver getDefaultResolver(final Class<?> clazz) {
        return new PropertyResolver() {
            @Override
            public Schema resolve(PropertyDescriptor descriptor) {
                ObjectSchema schema = new ObjectSchema();
                schema.setId(clazz.getCanonicalName());
                
                for (PropertyDescriptor prop : PropertyUtils.getPropertyDescriptors(clazz)) {
                    if (prop.getPropertyType() == Class.class) {
                        continue;
                    }
                    ImmutableCollection<PropertyResolver> resolver = resolvers.get(prop.getPropertyType());
                    if (resolver == null || resolver.isEmpty()) {
                        schema.addProperty(prop.getName(), getSchema(prop.getPropertyType()));
                    }
                    else {
                        schema.addProperty(prop.getName(), resolver.iterator().next().resolve(prop));
                    }
                }
                
                schemaCache.put(clazz, schema);
                return schema;
            }
        };
    }
    

}
