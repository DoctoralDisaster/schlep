package com.netflix.schlep.sqs;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.netflix.schlep.consumer.Act;
import com.netflix.schlep.consumer.IncomingMessage;
import com.netflix.schlep.filter.MessageFilter;
import com.netflix.schlep.log.LoggingMessageObservable;
import com.netflix.schlep.sim.SimMessageConsumer;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.subscriptions.BooleanSubscription;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

public class RxTest {
    private static final Logger LOG = LoggerFactory.getLogger(RxTest.class);
    
    public static class Message {
        public static <T> Observable<T> get(final T body) {
            final Observable<T> o = Observable.create(new Func1<Observer<T>, Subscription>() {
                @Override
                public Subscription call(final Observer<T> observer) {
                    final BooleanSubscription subscription = new BooleanSubscription();
                    observer.onNext(body);
                    observer.onCompleted();
                    return subscription;
                }
            }); 
            
            return o;
        }
    }
    
    public static class DummySender<T> implements Observer<List<T>> {
        @Override
        public void onCompleted() {
            // This is a stream shutdown
        }

        @Override
        public void onError(Throwable e) {
            // This is an error reading from the stream.  Not sure it has any real meaning
        }

        @Override
        public void onNext(List<T> args) {
            // Send the next batch
            LOG.info(args.toString());
        }
        
    }
    
    @Test
    public void test() throws Exception {
        
        Observable<IncomingMessage> stream = SimMessageConsumer.get("test", 1, 10, 100, TimeUnit.MILLISECONDS);
        
        Observable.create(MessageFilter.filter(stream, MessageFilter.<String>random(0.5)))
//            (MessageFilter.<String>random(0.5))
//        .observeOn(scheduler)
            .subscribe(LoggingMessageObservable.<String>create())
            ;
        
        // 1.  Reader threads
        // 2.  Read a batch of messages
        // 3.  Break up the batch into msg
        // 4.  Multicast
        // 5.  Filter
        // 6.  Batch for write
        // 7.  Write batch
        // 8.  Ack batch
        
//        Observable<String> source = Observable
//            .just("hi")
//            .subscribeOn(scheduler);
//        
//        for (int i = 0; i < 10; i++) {
//            source.subscribe(new Action1<String>() {
//                @Override
//                public void call(String t1) {
//                    LOG.info(t1);
//                }
//            });
//        }
//        
        
//        Observable
//            .just(Message.wrap(msg))
//            .buffer(10)
//            .subscribe(new DummySender<String>());

        Thread.sleep(10000);
    }
    
    
    enum Response {
        ACK, NACK, DELAY
    }

    interface Router {
        public rx.Observable<Act<String>> doIt(Observable<IncomingMessage> m);
    }

    public Router createRouter(final int routerId) {
        return new Router() {
            @Override
            public Observable<Act<String>> doIt(Observable<IncomingMessage> m) {
                return 
                  m
                   .map(new Func1<IncomingMessage, Act<String>>() {
                       @Override
                       public Act<String> call(IncomingMessage message) {
                           try {
                               if (routerId == 1) {
                                   Thread.sleep(1000);
                               }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                           return new Act<String>(message);
                       }
                   });
            }
        };
    }
    
    @Test
    public void doIt() throws Exception {
        final List<Router> routers = ImmutableList.of(
                createRouter(1),
                createRouter(2),
                createRouter(3),
                createRouter(4),
                createRouter(5)
                );
        
        Observable<IncomingMessage> stream = SimMessageConsumer.get("test", 1, 1000, 10, TimeUnit.MILLISECONDS);

        stream
            .map(new Func1<IncomingMessage, Observable<Act<String>>>() {
                public Observable<Act<String>> call(final IncomingMessage message) {
                    List<Observable<Act<String>>> replies = Lists.newArrayList();
                    for (Router router : routers) {
                        // encapsulate the work needed for each filter
                        Observable<Act<String>> ob = router.doIt(Observable.just(message));
                        
                        // add a default NACK if the filter throws an exception
                        ob = ob.onErrorReturn(new Func1<Throwable, Act<String>>() {
                            @Override
                            public Act<String> call(Throwable arg0) {
                                return Act.from(message);
                            }
                        });
                        
                        // add to the list of work to do
                        replies.add(ob);
                    }
                    
                    // execute all of the filters in parallel
                    return Observable.merge(replies);
                }
            })
            // for each set of filters
            .mapMany(Act.<String>join())
            .subscribe(Act.<String>acker())
            ;
        
        LOG.info("done observing");
        
        Thread.sleep(1000000);
    }
}
