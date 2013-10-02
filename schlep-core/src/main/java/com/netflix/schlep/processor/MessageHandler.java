package com.netflix.schlep.processor;

import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.writer.Completion;

import rx.Observable;
import rx.util.functions.Func1;

/**
 * Abstraction for a message handler.  The message handler receives an incoming
 * message and returns an Observable on which a sync or async Completion notification
 * must be emitted. 
 * 
 * @author elandau
 *
 */
public interface MessageHandler extends Func1<IncomingMessage, Observable<Completion<IncomingMessage>>> {
}
