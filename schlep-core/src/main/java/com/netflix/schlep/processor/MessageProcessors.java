package com.netflix.schlep.processor;

import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.writer.MessageWriter;

import rx.Scheduler;
import rx.util.functions.Action1;

public class MessageProcessors {
    public static MessageHandler async(Action1<IncomingMessage> action, Scheduler scheduler) {
        return new AsyncMessageProcessor(action, scheduler);
    }
    
    public static MessageHandler toWriter(MessageWriter writer) {
        return new ToWriterMessageProcessor(writer);
    }
}
