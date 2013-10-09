package com.netflix.schlep.governator;

import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.netflix.governator.configuration.ConfigurationKey;
import com.netflix.governator.configuration.ConfigurationProvider;
import com.netflix.governator.configuration.KeyParser;
import com.netflix.schlep.exception.ProducerException;
import com.netflix.schlep.producer.DefaultMessageProducerFactory;
import com.netflix.schlep.producer.MessageProducer;
import com.netflix.schlep.producer.MessageProducerFactory;

/**
 * Message consumer provider using governator exposed properties to create
 * MessageConsumers 
 * 
 * @author elandau
 *
 */
@Singleton
public class GovernatorConfigurationMessageProducerFactory implements DefaultMessageProducerFactory {
    private final String PRODUCER_PROPERTIES_PREFIX  = "com.netflix.schlep.%s";
    private final String PRODUCER_TYPE_FORMAT_STRING = PRODUCER_PROPERTIES_PREFIX + ".type";
    
    private final Injector injector;
    private final ConfigurationProvider configProvider;
    private final Map<String, MessageProducerFactory> factories;
    
    @Inject
    public GovernatorConfigurationMessageProducerFactory(
            Injector                            injector,
            ConfigurationProvider               configProvider, 
            Map<String, MessageProducerFactory> factories) {
        
        this.configProvider = configProvider;
        this.factories      = factories;
        this.injector       = injector;
    }
    
    @Override
    public MessageProducer create(String id) throws ProducerException {
        // Determine the consumer type for this id
        String           configurationName = String.format(PRODUCER_TYPE_FORMAT_STRING, id);
        ConfigurationKey key               = new ConfigurationKey(configurationName, KeyParser.parse(configurationName));
        String           type              = configProvider.getStringSupplier(key, null).get();
        if (type == null) {
            throw new ProducerException(
                    "Consumer type not specific for " + id
                    + ". Expecting one of " + factories.keySet());
        }
        MessageProducerFactory provider = factories.get(type);
        if (provider == null) {
            throw new ProducerException(
                    "Consumer type not found for " + id 
                    + ". Expecting one of " + factories.keySet());
        }
        
        return provider.create(id, 
                new GovernatorBuilderMapper(injector, configProvider, String.format(PRODUCER_PROPERTIES_PREFIX, id)));
    }

}
