package com.netflix.schlep.processor;

import com.netflix.schlep.reader.IncomingMessage;
import com.netflix.schlep.writer.Completion;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.subscriptions.BooleanSubscription;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

public class AsyncMessageProcessor implements MessageProcessor {
    
    private final Action1<IncomingMessage> action;
    private final Scheduler scheduler;
    
    public AsyncMessageProcessor(Action1<IncomingMessage> action, Scheduler scheduler) {
        this.action    = action;
        this.scheduler = scheduler;
    }
    
    @Override
    public Observable<Completion<IncomingMessage>> process(final IncomingMessage message) {
        return Observable.create(new Func1<Observer<Completion<IncomingMessage>>, Subscription>() {
            @Override
            public Subscription call(final Observer<Completion<IncomingMessage>> observer) {
                final BooleanSubscription sub = new BooleanSubscription();
                try {
                    if (!sub.isUnsubscribed()) {
                        action.call(message);
                        observer.onNext(Completion.from(message));
                        observer.onCompleted();
                    }
                }
                catch (Throwable t) {
                    observer.onError(t);
                }
                return sub;
            }
        }).subscribeOn(scheduler);
    }
}
