package com.netflix.schlep.governator;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.netflix.governator.configuration.ConfigurationKey;
import com.netflix.governator.configuration.ConfigurationProvider;
import com.netflix.governator.configuration.KeyParser;
import com.netflix.schlep.serializer.Mapper;

/**
 * Quick and dirty implementation that looks at all of the withXXX methods of a
 * builder and populates with a property.
 * 
 * @author elandau
 *
 */
public class GovernatorBuilderMapper implements Mapper {
    private static final Logger LOG = LoggerFactory.getLogger(GovernatorBuilderMapper.class);
    
    private final ConfigurationProvider configurationProvider;
    private final String                prefix;
    
    public GovernatorBuilderMapper(ConfigurationProvider configurationProvider, String prefix) {
        this.configurationProvider = configurationProvider;
        this.prefix                = prefix;
    }
    
    @Override
    public <T> T apply(T obj) throws Exception {
        @SuppressWarnings("unchecked")
        Class<T> type = (Class<T>) obj.getClass();
        
        LOG.info("Creating instance of " + type);
        
        for (Method method : type.getMethods()) {
            if (!method.getName().startsWith("with")) {
                continue;
            }
            
            String propertyName = prefix + "." +
                    CaseFormat.UPPER_CAMEL.to(
                        CaseFormat.LOWER_CAMEL, 
                            StringUtils.substringAfter(method.getName(), "with"));
            
            LOG.info("Getting property: " + propertyName);
            
            Class<?>[] types = method.getParameterTypes();
            if (types.length != 1) {
                LOG.warn("Skipping method with not exactly one argument : " + method.getName());
                continue;
            }
            
            ConfigurationKey key = new ConfigurationKey(propertyName, KeyParser.parse(propertyName));
            Supplier<?> supplier = getPropertySupplier(key, types[0]);
            try {
                Object value = supplier.get();
                if (value != null) {
                    method.invoke(obj, value);
                }
            }
            catch (Exception e) {
                LOG.info("No value for : " + propertyName);
            }
        }
        
        return obj;
    }
    
    private Supplier<?> getPropertySupplier(final ConfigurationKey key, final Class<?> type)
    {
        if ( String.class.isAssignableFrom(type) )
        {
            return configurationProvider.getStringSupplier(key, null);
        }
        else if ( Boolean.class.isAssignableFrom(type) || Boolean.TYPE.isAssignableFrom(type) )
        {
            return configurationProvider.getBooleanSupplier(key, null);
        }
        else if ( Integer.class.isAssignableFrom(type) || Integer.TYPE.isAssignableFrom(type) )
        {
            return configurationProvider.getIntegerSupplier(key, null);
        }
        else if ( Long.class.isAssignableFrom(type) || Long.TYPE.isAssignableFrom(type) )
        {
            return configurationProvider.getLongSupplier(key, null);
        }
        else if ( Double.class.isAssignableFrom(type) || Double.TYPE.isAssignableFrom(type) )
        {
            return configurationProvider.getDoubleSupplier(key, null);
        }
        else
        {
            LOG.error("Method argument type not supported: " + type + " (" + type + ")");
            return Suppliers.ofInstance(null);
        }
    }
}
