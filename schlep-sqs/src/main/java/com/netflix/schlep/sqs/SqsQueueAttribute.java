package com.netflix.schlep.sqs;

import com.netflix.schlep.QueueAttribute;

public enum SqsQueueAttribute implements QueueAttribute {

    // - returns all values.
    All(false), 
    
    // - returns the approximate number of visible messages in a queue. 
    // For more information, see Resources Required to Process Messages in the Amazon SQS Developer Guide.
    ApproximateNumberOfMessages(false), 
    
    // - returns the approximate number of messages that are not timed-out and not deleted. 
    // For more information, see Resources Required to Process Messages in the Amazon SQS Developer Guide.
    ApproximateNumberOfMessagesNotVisible(false), 
    
    // - returns the visibility timeout for the queue. 
    // For more information about visibility timeout, see Visibility Timeout in the Amazon SQS Developer Guide.
    VisibilityTimeout(true),
    
    // - returns the time when the queue was created (epoch time in seconds).
    CreatedTimestamp(false), 
    
    // - returns the time when the queue was last changed (epoch time in seconds).
    LastModifiedTimestamp(false), 
    
    // - returns the queue's policy.
    Policy(true),
    
    // - returns the limit of how many bytes a message can contain before Amazon SQS rejects it.
    MaximumMessageSize (true),
    
    // - returns the number of seconds Amazon SQS retains a message.
    MessageRetentionPeriod(true),
    
    // - returns the queue's Amazon resource name (ARN).
    QueueArn(false), 
    
    // - returns the approximate number of messages that are pending to be added to the queue.
    ApproximateNumberOfMessagesDelayed(false), 
    
    // - returns the default delay on the queue in seconds.
    DelaySeconds(true)
    
    ; 

    SqsQueueAttribute(boolean mutable) {
        this.mutable = mutable;
    }
    
    private boolean mutable;
    
    @Override
    public boolean isMutable() {
        return mutable;
    }

    @Override
    public boolean validate(String value) {
        return true;
    }

}
