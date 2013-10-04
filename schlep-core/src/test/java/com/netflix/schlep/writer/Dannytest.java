package com.netflix.schlep.writer;

import java.util.List;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Scheduler;
import rx.concurrency.Schedulers;
import rx.util.functions.Action0;
import rx.util.functions.Action1;

import com.google.common.collect.Lists;

public class Dannytest {
    private static final Logger LOG = LoggerFactory.getLogger(Dannytest.class);
    
    @Test
    public void test() throws InterruptedException {
        List<Integer> list = Lists.newArrayList(1, 2, 3, 4);
        
        Observable.from(list)
            .observeOn(Schedulers.executor(Executors.newFixedThreadPool(4)))
            .subscribe(new Action1<Integer>() {
                @Override
                public void call(Integer t1) {
                    LOG.info(t1.toString());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        
        
        Thread.sleep(10000);
    }
    
    
    @Test
    public void testExecutor() {
        Scheduler s = Schedulers.executor(Executors.newFixedThreadPool(4));
        for (int i = 0; i < 4; i++) {
            final String str = "str_" + i;
            s.schedule(new Action0() {
                @Override
                public void call() {
                    LOG.info(str);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
