package com.netflix.schlep.router.service;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.BooleanSubscription;
import rx.util.functions.Func1;

import com.google.common.base.Predicate;
import com.netflix.schlep.processor.MessageProcessor;
import com.netflix.schlep.reader.IncomingMessage;
import com.netflix.schlep.writer.Completion;
import com.netflix.schlep.writer.MessageWriter;
import com.netflix.schlep.writer.OutgoingMessage;

/**
 * Message processor using a predicate to determine whether to send a message to the
 * writer.
 * 
 * @author elandau
 *
 */
public class PredicateMessageProcessor implements MessageProcessor {
    private final MessageWriter writer;
    private final Predicate<IncomingMessage>   predicate;
    
    public PredicateMessageProcessor(MessageWriter writer, Predicate<IncomingMessage> predicate) {
        this.writer    = writer;
        this.predicate = predicate;
    }
    
    @Override
    public Observable<Completion<IncomingMessage>> process(final IncomingMessage message) {
        if (predicate.apply(message)) {
            return Observable.create(new Func1<Observer<Completion<IncomingMessage>>, Subscription>() {
                @Override
                public Subscription call(final Observer<Completion<IncomingMessage>> observer) {
                    final BooleanSubscription sub = new BooleanSubscription();
                    
                    final OutgoingMessage msg = OutgoingMessage.builder()
                            .withMessage(message.getContents(String.class))
                            .build();
                    
                    writer.write(msg, new Observer<Completion<OutgoingMessage>>() {
                        @Override
                        public void onCompleted() {
                            if (!sub.isUnsubscribed())
                                observer.onCompleted();
                        }
    
                        @Override
                        public void onError(Throwable e) {
                            if (!sub.isUnsubscribed())
                                observer.onError(e);
                        }
    
                        @Override
                        public void onNext(Completion<OutgoingMessage> arg) {
                            if (!sub.isUnsubscribed())
                                observer.onNext(Completion.from(message));
                        }
                    });
                    return sub;
                }
            });
        }
        else {
            // Didn't pass the predicate to return a Completion immediately
            return Observable.just(Completion.from(message));
        }
    }
}