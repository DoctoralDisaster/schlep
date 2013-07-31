package com.netflix.schlep.batch;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

/**
 * Batching strategy that makes an inline call to the callback for every insert.
 * 
 * @author elandau
 */
public class InlineNoBatchStrategy implements BatchStrategy {
    @Override
    public <T> Batcher<T> create(final Function<List<T>, Boolean> callback) {
        return new Batcher<T>() {
            @Override
            public void add(T object) {
                callback.apply(ImmutableList.of(object));
            }

            @Override
            public void flush() {
                // Nothing to flush here
            }

            @Override
            public void shutdown() {
                // Nothing to do here
            }
        };
    }
}
