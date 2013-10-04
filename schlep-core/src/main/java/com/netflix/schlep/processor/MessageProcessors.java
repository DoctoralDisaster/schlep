package com.netflix.schlep.processor;

import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.consumer.MessageHandler;
import com.netflix.schlep.producer.MessageProducer;

import rx.Scheduler;
import rx.util.functions.Action1;

public class MessageProcessors {
    public static MessageHandler async(Action1<IncomingMessage> action, Scheduler scheduler) {
        return new ConcurrentMessageProcessor(action, scheduler);
    }
    
    public static MessageHandler toWriter(MessageProducer writer) {
        return new ToWriterMessageProcessor(writer);
    }
}
