package com.netflix.schlep.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Consumer {
    /**
     * name or id in the configuration associated with the endpoint
     */
    public String name();
    
    /**
     * Sync lifecycle of the queue to this class.  This will auto-start the consumer
     * on postConstruct and shut it down in preDestroy
     */
    public boolean syncLifecycle() default false;
    
    /**
     * If true the message will be automatically ack'd when the method returns
     */
    public boolean autoAck() default true;
    
    public boolean autoStart() default false;
}
