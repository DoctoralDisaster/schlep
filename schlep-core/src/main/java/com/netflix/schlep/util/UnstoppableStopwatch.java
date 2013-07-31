package com.netflix.schlep.util;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

public class UnstoppableStopwatch {
    private final Stopwatch sw;
    
    public UnstoppableStopwatch() {
        sw = new Stopwatch().start();
    }
    
    public long elapsed(TimeUnit units) {
        return sw.elapsed(units);
    }
}
