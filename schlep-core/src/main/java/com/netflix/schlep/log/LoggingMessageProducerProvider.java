package com.netflix.schlep.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observer;

import com.google.inject.Inject;
import com.netflix.schlep.serializer.Mapper;
import com.netflix.schlep.writer.Completion;
import com.netflix.schlep.writer.MessageWriter;
import com.netflix.schlep.writer.MessageWriterFactory;
import com.netflix.schlep.writer.OutgoingMessage;

public class LoggingMessageProducerProvider implements MessageWriterFactory {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingMessageProducerProvider.class);

    @Inject
    public LoggingMessageProducerProvider() {
    }
    
    @Override
    public MessageWriter createProducer(final String id, final Mapper mapper) {
        return new MessageWriter() {
            @Override
            public void write(OutgoingMessage message,
                    Observer<Completion<OutgoingMessage>> observer) {
            }

            @Override
            public Observable<Completion<OutgoingMessage>> write(
                    OutgoingMessage message) {
                return null;
            }

            @Override
            public String getId() {
                return "logging";
            }
        };
    }
}