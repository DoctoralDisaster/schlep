package com.netflix.schlep.processor;

import com.netflix.schlep.reader.IncomingMessage;
import com.netflix.schlep.writer.Completion;

import rx.Observable;

/**
 * Abstraction for a message processor.  The message processor receives an incoming
 * message and returns an Observable on which a sync or async Completion notification
 * may be emitted. 
 * 
 * @author elandau
 *
 */
public interface MessageProcessor {
    /**
     * Abstraction for a message processor.  The message processor receives an incoming
     * message and returns an Observable on which a sync or async Completion notification
     * may be emitted. 
     * 
     * @param message
     * @return
     */
    Observable<Completion<IncomingMessage>> process(IncomingMessage message);
}
