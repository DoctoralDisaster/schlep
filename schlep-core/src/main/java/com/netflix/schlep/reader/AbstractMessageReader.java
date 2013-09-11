package com.netflix.schlep.reader;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractMessageReader implements MessageReader {
    private AtomicBoolean paused = new AtomicBoolean();
    
    public AbstractMessageReader() {
    }
    
    @Override
    public synchronized void start() throws Exception {
    }

    @Override
    public synchronized void stop() throws Exception {
    }

    @Override
    public synchronized void pause() throws Exception {
        paused.set(true);
    }

    @Override
    public synchronized void resume() throws Exception {
        paused.set(false);
    }

}
