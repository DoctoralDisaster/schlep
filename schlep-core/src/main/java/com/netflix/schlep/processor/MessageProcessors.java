package com.netflix.schlep.processor;

import com.netflix.schlep.reader.IncomingMessage;
import com.netflix.schlep.writer.MessageWriter;

import rx.Scheduler;
import rx.util.functions.Action1;

public class MessageProcessors {
    public static MessageProcessor async(Action1<IncomingMessage> action, Scheduler scheduler) {
        return new AsyncMessageProcessor(action, scheduler);
    }
    
    public static MessageProcessor toWriter(MessageWriter writer) {
        return new ToWriterMessageProcessor(writer);
    }
}
