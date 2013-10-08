package com.netflix.schlep.consumer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Function;
import com.netflix.schlep.Completion;

import rx.Observable;
import rx.Subscription;

public class OneMessageConsumer implements MessageConsumer {
    private AtomicReference<MessageHandler> observer = new AtomicReference<MessageHandler>();
    private final AtomicInteger nextMessageCount = new AtomicInteger(0);
    private final AtomicInteger ackMessageCount = new AtomicInteger(0);
    private final AtomicInteger nakMessageCount = new AtomicInteger(0);
    
    private final String id;
    
    public OneMessageConsumer(String id) {
        this.id = id;
    }
    
    @Override
    public void start() throws Exception {
        MessageHandler observer = this.observer.get();
        if (observer == null) {
            throw new RuntimeException("No observer found");
        }
        
        observer.call(new IncomingMessage() {
            {
                nextMessageCount.incrementAndGet();
            }
            
            @Override
            public long getTimeSinceReceived(TimeUnit units) {
                return 0;
            }

            @Override
            public <T> T getContents(Class<T> clazz) {
                if (clazz != String.class)
                    throw new RuntimeException("Only String messages allowed here");
                return (T)"Message";
            }
        });
    }
    
    public int getAckCount() {
        return ackMessageCount.get();
    }
    
    public int getNakCount() {
        return nakMessageCount.get();
    }
    
    public int getOnNextCount() {
        return nextMessageCount.get();
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public void pause() throws Exception {
    }

    @Override
    public void resume() throws Exception {
    }

    @Override
    public boolean isStarted() {
        return false;
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public Subscription subscribe(MessageHandler handler) {
        if (this.observer.compareAndSet(null, handler)) {
            return new Subscription() {
                @Override
                public void unsubscribe() {
                    observer.set(null);
                }
            };
        }
        else {
            throw new RuntimeException("Only one observer may be registered");
        }
    }

    @Override
    public Subscription subscribe(final Function<IncomingMessage, Boolean> handler) {
        return subscribe(new MessageHandler() {
            @Override
            public Observable<Completion<IncomingMessage>> call(IncomingMessage message) {
                handler.apply(message);
                return Observable.from(Completion.from(message));
            }
        });
    }

}
