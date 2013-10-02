package com.netflix.schlep.processor;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.BooleanSubscription;
import rx.util.functions.Func1;

import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.writer.MessageWriter;
import com.netflix.schlep.writer.Completion;
import com.netflix.schlep.writer.OutgoingMessage;

/**
 * MessageProcessor that forwards a message to a writer and also forwards the completion notification
 * 
 * @author elandau
 *
 */
public class ToWriterMessageProcessor implements MessageHandler {
    private MessageWriter writer;
    
    public ToWriterMessageProcessor(MessageWriter writer) {
        this.writer = writer;
    }
    
    @Override
    public Observable<Completion<IncomingMessage>> call(final IncomingMessage message) {
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
}
