package com.netflix.schlep.sim;

import com.netflix.schlep.consumer.MessageConsumer;
import com.netflix.schlep.consumer.MessageConsumerFactory;
import com.netflix.schlep.exception.ConsumerException;
import com.netflix.schlep.serializer.Mapper;
import com.netflix.schlep.sim.SimMessageConsumer.Builder;

public class SimMessageConsumerFactory implements MessageConsumerFactory {
    @Override
    public MessageConsumer createConsumer(String id, Mapper mapper) throws ConsumerException {
        try {
            Builder<?> builder = SimMessageConsumer.builder();
            builder.withId(id);
            mapper.apply(builder);
            return builder.build();
        } catch (Exception e) {
            
            throw new ConsumerException("Failed to create instance of SimMessageConsumer", e);
        }
    }
}
