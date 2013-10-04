package com.netflix.schlep.governator;

import com.google.inject.AbstractModule;
import com.netflix.schlep.consumer.DefaultMessageConsumerFactory;
import com.netflix.schlep.producer.DefaultMessageProducerFactory;

public class GovernatorSchlepModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DefaultMessageConsumerFactory.class).to(GovernatorConfigurationMessageConsumerFactory.class);
        bind(DefaultMessageProducerFactory.class).to(GovernatorConfigurationMessageProducerFactory.class);
    }
}
