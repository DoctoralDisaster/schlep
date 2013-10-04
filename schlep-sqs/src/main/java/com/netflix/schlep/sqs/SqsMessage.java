package com.netflix.schlep.sqs;

import com.amazonaws.services.sqs.model.Message;

public class SqsMessage {
    private final Message message;
    private Exception exception;
    private int visibilityTimeout;
    
    public SqsMessage(Message message) {
        this.message = message;
    }
    
    public void setException(Exception e) {
        this.exception =e ;
    }
    
    public Message getMessage() {
        return message;
    }
    
    public Exception getException() {
        return exception;
    }

    public void setVisibilityTimeout(int visibilityTimeout) {
        this.visibilityTimeout = visibilityTimeout;
    }
    
    public int getVisibilityTimeout() {
        return visibilityTimeout;
    }
}
