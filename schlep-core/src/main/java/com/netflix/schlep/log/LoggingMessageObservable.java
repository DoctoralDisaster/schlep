package com.netflix.schlep.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.schlep.consumer.IncomingMessage;

import rx.Observer;

public class LoggingMessageObservable {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingMessageObservable.class);
    
    public static Observer<IncomingMessage> create() {
        return new Observer<IncomingMessage>() {
            @Override
            public void onCompleted() {
                LOG.info("Complete");
            }

            @Override
            public void onError(Throwable e) {
                LOG.info("Error");
            }

            @Override
            public void onNext(IncomingMessage arg) {
                LOG.info("Next: " + arg);
            }
        };
    }
}
