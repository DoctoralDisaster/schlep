package com.netflix.schlep.rx;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.util.functions.Action0;
import rx.util.functions.Func1;

public class OperationRetry {
    public static <T> Func1<Observer<T>, Subscription> retry(final Observable<T> source) {
        return new Func1<Observer<T>, Subscription>() {
            @Override
            public Subscription call(final Observer<T> observer) {
                return source.subscribe(new RetryObserver<T>(observer, new TightRetryPolicy()));
            }
        };
    }
    
    public static <T> Func1<Observer<T>, Subscription> retry(final Observable<T> source, RetryPolicy policy) {
        return new Func1<Observer<T>, Subscription>() {
            @Override
            public Subscription call(final Observer<T> observer) {
                return source.subscribe(new RetryObserver<T>(observer, new TightRetryPolicy()));
            }
        };
    }
    
    public static <T> Func1<Observer<T>, Subscription> retry(final Observable<T> source, RetryPolicy policy, Scheduler scheduler) {
        return new Func1<Observer<T>, Subscription>() {
            @Override
            public Subscription call(final Observer<T> observer) {
                return source.subscribe(new RetryObserver<T>(observer, new TightRetryPolicy()));
            }
        };
    }
    
    /**
     * Encapsulate a retry context from the beginning of an operation
     * @author elandau
     *
     */
    public static class Context {
        private long attemptCount = 0;
        private long startTime = System.nanoTime();
        
        /**
         * Return the number of attempts, or number of times +1 nextBackoffDelay was called
         * @return
         */
        public long getAttemptCount() {
            return attemptCount;
        }    
        
        public long incAttemptCount() {
            return ++attemptCount;
        }
        
        /**
         * Return elapsed time since the context was created
         * @return Ellapsed time in msec
         */
        public long getEllapsedTime() {
            return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        }
    }
    
    /**
     * Abstraction for a retry policy
     * @author elandau
     *
     */
    public static interface RetryPolicy {
        /**
         * Return delay in msec or -1 if done
         * 
         * @param context
         * @return
         */
        public long getDelay(Context context);
    }
    
    public static class TightRetryPolicy implements RetryPolicy {
        @Override
        public long getDelay(Context context) {
            return 0;
        }
    }
    
    public static class NoRetryPolicy implements RetryPolicy {
        @Override
        public long getDelay(Context context) {
            return -1;
        }
    }
    
    public static class CountingRetryPolicy implements RetryPolicy {
        final long maxCount;
        
        public CountingRetryPolicy(long maxCount) {
            this.maxCount = maxCount;
        }
        
        @Override
        public long getDelay(Context context) {
            if (context.incAttemptCount() > this.maxCount) 
                return -1;
            return 0;
        }
    }
    
    public static class CountingDelayRetryPolicy extends CountingRetryPolicy {
        final long delay;
        public CountingDelayRetryPolicy(long delay, long maxCount) {
            super(maxCount);
            this.delay = delay;
        }
        
        public long getDelay(Context context) {
            if (super.getDelay(context) == -1) {
                return -1;
            }
            
            return delay;
        }
    }
    
    public static class RetryObserver<T> implements Observer<T> {
        private final Observer<T> observer;
        private final RetryPolicy policy;
        
        public RetryObserver(Observer<T> observer, RetryPolicy policy) {
            this.observer = observer;
            this.policy   = policy;
        }

        @Override
        public void onCompleted() {
            this.observer.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            this.observer.onError(e);
        }

        @Override
        public void onNext(final T args) {
            final Context ctx = new Context();
            while (true) {
                try {
                    observer.onNext(args);
                    return;
                }
                catch (Throwable t) {
                    System.out.println(t.getMessage());
                    long delay = policy.getDelay(ctx);
                    if (delay == -1) {
                        observer.onError(t);
                    }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(t);
                    }
                }
            }
        }
    }
    
    public static class ScheduledRetryObserver<T> implements Observer<T> {
        private final Observer<T> observer;
        private final RetryPolicy policy;
        private final Scheduler   scheduler;
        
        public ScheduledRetryObserver(Observer<T> observer, RetryPolicy policy, Scheduler scheduler) {
            this.observer  = observer;
            this.policy    = policy;
            this.scheduler = scheduler;
        }

        @Override
        public void onCompleted() {
            this.observer.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            this.observer.onError(e);
        }

        @Override
        public void onNext(final T args) {
            final Context ctx = new Context();
            new Action0() {
                @Override
                public void call() {
                    try {
                        observer.onNext(args);
                        return;
                    }
                    catch (Throwable t) {
                        System.out.println(t.getMessage());
                        long delay = policy.getDelay(ctx);
                        if (delay == -1) {
                            observer.onError(t);
                        }
                        
                        scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);
                    }
                }
            }.call();
        }
    }
}
