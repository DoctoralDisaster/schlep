package com.netflix.schlep.consumer;

import rx.Subscription;

import com.google.common.base.Function;
import com.netflix.schlep.component.Component;

/**
 * Abstraction for a MessageConsumer that emits IncomingMessage.  The
 * MessageReader implements an Rx function that accepts subscriptions and
 * begin emitting IncommingMessages once subscribed to.
 * 
 * @author elandau
 */
public interface MessageConsumer extends Component {
    Subscription subscribe(MessageHandler handler);

    Subscription subscribe(Function<IncomingMessage, Boolean> handler);
}
