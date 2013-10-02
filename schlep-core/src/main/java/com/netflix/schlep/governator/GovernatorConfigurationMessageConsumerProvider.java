package com.netflix.schlep.governator;

import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.governator.configuration.ConfigurationKey;
import com.netflix.governator.configuration.ConfigurationProvider;
import com.netflix.governator.configuration.KeyParser;
import com.netflix.schlep.consumer.MessageConsumer;
import com.netflix.schlep.consumer.MessageConsumerFactory;
import com.netflix.schlep.consumer.MessageConsumerProvider;
import com.netflix.schlep.exception.ConsumerException;

/**
 * Message consumer provider using governator exposed properties to create
 * MessageConsumers 
 * 
 * @author elandau
 *
 */
@Singleton
public class GovernatorConfigurationMessageConsumerProvider implements MessageConsumerProvider {
    private final String CONSUMER_PROPERTIES_PREFIX  = "com.netflix.schlep.%s";
    private final String CONSUMER_TYPE_FORMAT_STRING = CONSUMER_PROPERTIES_PREFIX + ".type";
    
    private final ConfigurationProvider configProvider;
    private final Map<String, MessageConsumerFactory> factories;
    
    @Inject
    public GovernatorConfigurationMessageConsumerProvider(
            ConfigurationProvider               configProvider, 
            Map<String, MessageConsumerFactory> factories) {
        
        this.configProvider = configProvider;
        this.factories      = factories;
    }
    
    @Override
    public MessageConsumer get(String id) throws ConsumerException {
        // Determine the consumer type for this id
        String           configurationName = String.format(CONSUMER_TYPE_FORMAT_STRING, id);
        ConfigurationKey key               = new ConfigurationKey(configurationName, KeyParser.parse(configurationName));
        String           type              = configProvider.getStringSupplier(key, null).get();
        if (type == null) {
            throw new ConsumerException(
                    "Consumer type not specific for " + id
                    + ". Expecting one of " + factories.keySet());
        }
        MessageConsumerFactory provider = factories.get(type);
        if (provider == null) {
            throw new ConsumerException(
                    "Consumer type not found for " + id 
                    + ". Expecting one of " + factories.keySet());
        }
        
        return provider.createConsumer(id, 
                new GovernatorBuilderMapper(configProvider, String.format(CONSUMER_PROPERTIES_PREFIX, id)));
    }

}
