package com.netflix.schlep.batch;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.netflix.schlep.batch.TimeAndSizeBatchStrategy;

public class TimeAndSizeBatcherTest {
    private static final Logger LOG = LoggerFactory.getLogger(TimeAndSizeBatcherTest.class);
    
    @Test
    @Ignore
    public void testBySize() throws Exception {
        final List<String> result = Lists.newArrayList();
        
        BatchStrategy strategy = new TimeAndSizeBatchStrategy(2, 1, TimeUnit.SECONDS);
        Batcher<String> batcher = strategy.create(new Function<List<String>, Boolean>() {
            public Boolean apply(List<String> list) {
                result.addAll(list);
                return true;
            }
        });
        
        batcher.add("A");
        batcher.add("B");
        Thread.sleep(100);
        batcher.shutdown();
     
        Assert.assertEquals(2, result.size());
    }
    
    @Test
    public void testByTime() throws Exception {
        final List<String> result = Lists.newArrayList();
        
        BatchStrategy strategy = new TimeAndSizeBatchStrategy(10, 1, TimeUnit.SECONDS);
        Batcher<String> batcher = strategy.create(new Function<List<String>, Boolean>() {
            public Boolean apply(List<String> list) {
                result.addAll(list);
                return true;
            }
        });
        
        batcher.add("A");
        batcher.add("B");
        
        Assert.assertEquals(0, result.size());
        
        Thread.sleep(2010);
        
        Assert.assertEquals(2, result.size());
    }
}
