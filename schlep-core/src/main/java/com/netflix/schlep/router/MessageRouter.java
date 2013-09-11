package com.netflix.schlep.router;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Subscription;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.netflix.schlep.processor.MessageProcessor;
import com.netflix.schlep.reader.IncomingMessage;
import com.netflix.schlep.writer.Completion;

/**
 * The message router observes messages from an Observable<IncomingMessage> and routes them to 
 * multiple message processors, which may process messages asynchronously.  It will then merge
 * the responses and send an ack.
 * 
 * @author elandau
 */
public class MessageRouter {
    private static final Logger LOG = LoggerFactory.getLogger(MessageRouter.class);
    
    private CopyOnWriteArraySet<MessageProcessor> processors = Sets.newCopyOnWriteArraySet();
    private AtomicLong      counter = new AtomicLong();
    private Subscription    subscription = null;
    private ReentrantLock   lock = new ReentrantLock();
    
    public static class SelectFirst implements Func2<Completion<IncomingMessage>, Completion<IncomingMessage>, Completion<IncomingMessage>> {
        @Override
        public Completion<IncomingMessage> call(Completion<IncomingMessage> reply1, Completion<IncomingMessage> reply2) {
            return reply1;
        }
        
        public static SelectFirst instance = new SelectFirst();
        public static SelectFirst get() {
            return instance;
        }
    }
    
    /**
     * Adding the default message processor ensures messages flow through the system
     * even if there are no other processors
     * @author elandau
     *
     */
    public static class DefaultMessageProcessor implements MessageProcessor {
        @Override
        public Observable<Completion<IncomingMessage>> process(IncomingMessage message) {
            return Observable.just(Completion.from(message));
        }
    }

    private Observable<IncomingMessage> stream;
    
    public MessageRouter(Observable<IncomingMessage> stream) {
        this.stream = stream;
    }
    
    /**
     * Add a message processor.  Will register for notifications when the first processor is added.
     * 
     * @param processor
     */
    public void addProcessor(MessageProcessor processor) {
        processors.add(processor);
        if (counter.incrementAndGet() == 1) {
            lock.tryLock();
            try {
                this.subscription = stream
                    .mapMany(new Func1<IncomingMessage, Observable<Completion<IncomingMessage>>>() {
                        @Override
                        public Observable<Completion<IncomingMessage>> call(final IncomingMessage message) {
                            LOG.info(message.toString());
                            List<Observable<Completion<IncomingMessage>>> replies = Lists.newArrayList();
                            for (MessageProcessor processor : processors) {
                                try {
                                    // encapsulate the work needed for each filter
                                    Observable<Completion<IncomingMessage>> ob = processor.process(message);
                                    // add a default ACK if the filter throws an exception
                                    ob = ob.onErrorReturn(new Func1<Throwable, Completion<IncomingMessage>>() {
                                        @Override
                                        public Completion<IncomingMessage> call(Throwable t) {
                                            return Completion.from(message);
                                        }
                                    });
                                    
                                    // add to the list of work to do
                                    replies.add(ob);
                                }
                                catch (RuntimeException e) {
                                    e.printStackTrace();
                                }
                            }
                            
                            if (replies.isEmpty()) {
                                LOG.info("No processors for " + message);
                                return Observable.just(Completion.from(message));
                            }
                            
                            // execute all of the filters in parallel
                            return Observable
                                .merge(replies)
                                .reduce(SelectFirst.get());
                        }                
                    })
                    .subscribe(new Action1<Completion<IncomingMessage>>() {
                        @Override
                        public void call(Completion<IncomingMessage> act) {
                            LOG.info("Ack: " + act);
                            act.getValue().ack();
                        }
                    });
            }
            finally {
                lock.unlock();
            }
        }
    }
    
    /**
     * Remove a message processor.  Unsubscribe from the stream if the number of processors 
     * goes down to zero
     * @param processr
     */
    public void removeProcessor(MessageProcessor processr) {
        processors.remove(processr);
        if (counter.decrementAndGet() == 0) {
            lock.tryLock();
            try {
                if (subscription == null) {
                    subscription.unsubscribe();
                    subscription = null;
                }
            }
            finally {
                lock.unlock();
            }
        }
    }
}

