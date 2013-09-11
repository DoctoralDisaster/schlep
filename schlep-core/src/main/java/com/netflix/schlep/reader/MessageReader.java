package com.netflix.schlep.reader;

import rx.Observer;
import rx.Subscription;
import rx.util.functions.Func1;

import com.netflix.schlep.component.Component;

/**
 * Abstraction for a MessageConsumer that emits IncomingMessage.  The
 * MessageReader implements an Rx function that accepts subscriptions and
 * begin emitting IncommingMessages once subscribed to.
 * 
 * @author elandau
 *
 * @param <T>
 */
public interface MessageReader extends Component, Func1<Observer<IncomingMessage>, Subscription> { 
    
    /**
     * Return the unique id for this message consumer
     * @return
     */
    public abstract String getId();
}
