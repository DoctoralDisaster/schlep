package com.netflix.schlep.sqs;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;

/**
 * Wrapper for queuename providing access to parts of the name.
 * 
 * A queue name may be a simple string or a fully qualified queue name.
 * 1: /accountid/queuename
 * 2: /queuename
 * 
 * 
 * @author elandau
 *
 */
public class QueueName {
    private boolean isFullyQualified;
    private final String  name;
    private final String  accountId;
    
    public QueueName(String uriOrName) {
        Preconditions.checkNotNull(uriOrName, "QueueName cannot be null");
        isFullyQualified = isFullQualifiedQueueName(uriOrName);
        if (!isFullyQualified) {
            name      = uriOrName;
            accountId = null;
        }
        else {
            String[] pair = splitFullyQualifiedQueueName(uriOrName);
            accountId = pair[0];
            name      = pair[1];
        }
    }
    
    public boolean isFullQualifiedName() {
        return isFullyQualified;
    }
    
    public String getName() {
        return name;
    }
    
    public String getAccountId() {
        return accountId;
    }
    
    /**
     * @return first = ownerAccountId, second = queueName
     */
    private static String[] splitFullyQualifiedQueueName(String fullyQualifiedQueueName) {
        if(fullyQualifiedQueueName.charAt(0) != '/')
            throw new IllegalArgumentException("invalid fully qualified queue name: " + fullyQualifiedQueueName);

        String[] parts = fullyQualifiedQueueName.split("/");
        if(parts == null || parts.length != 3)
            throw new IllegalArgumentException("invalid fully qualified queue name: " + fullyQualifiedQueueName);

        if(StringUtils.isNotBlank(parts[1]) && StringUtils.isNotBlank(parts[2])) {
            String ownerAccountId = parts[1];
            String queueName = parts[2];
            return new String[]{ownerAccountId, queueName};
        } else {
            throw new IllegalArgumentException("invalid fully qualified queue name: " + fullyQualifiedQueueName);
        }
    }  

    private static boolean isFullQualifiedQueueName(String queueName) {
        if ((queueName.charAt(0) == '/' && queueName.lastIndexOf('/') > 0)) 
            return true;
        else
            return false;
    }
    


}
