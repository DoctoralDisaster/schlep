package com.netflix.schlep.sqs;

import com.amazonaws.services.sqs.model.Message;
import com.google.common.util.concurrent.AbstractFuture;
import com.sun.istack.internal.Nullable;

public class MessageFuture<V> extends AbstractFuture<V> {
    private final Message           message;
    private final long              visibilityTimeout;
    
    public MessageFuture(Message message) {
        super();
        this.message = message;
        this.visibilityTimeout = 0;
    }

    public MessageFuture(Message message, long visibilityTimeout) {
        super();
        this.message = message;
        this.visibilityTimeout = visibilityTimeout;
    }

    public Message getMessage() {
        return this.message;
    }

    public long getVisibilityTimeout() {
        return this.visibilityTimeout;
    }
    
    /**
     * Sets the value of this future.  This method will return {@code true} if
     * the value was successfully set, or {@code false} if the future has already
     * been set or cancelled.
     *
     * @param value the value the future should hold.
     * @return true if the value was successfully set.
     */
    @Override
    public boolean set(@Nullable V value) {
      return super.set(value);
    }

    /**
     * Sets the future to having failed with the given exception. This exception
     * will be wrapped in an {@code ExecutionException} and thrown from the {@code
     * get} methods. This method will return {@code true} if the exception was
     * successfully set, or {@code false} if the future has already been set or
     * cancelled.
     *
     * @param throwable the exception the future should hold.
     * @return true if the exception was successfully set.
     */
    @Override
    public boolean setException(Throwable throwable) {
      return super.setException(throwable);
    }
}
