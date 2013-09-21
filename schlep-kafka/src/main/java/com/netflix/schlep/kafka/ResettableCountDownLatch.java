package com.netflix.schlep.kafka;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ResettableCountDownLatch {
    private CountDownLatch latch;

    public void reset(int count) {
        latch = new CountDownLatch(count);
    }

    public long getCount() { return latch.getCount(); }
    public void countDown() { latch.countDown(); }
    public void await() throws InterruptedException { latch.await(); }
    public void await(long timeout, TimeUnit timeUnit) throws InterruptedException { latch.await(timeout, timeUnit); }
}
