package com.netflix.schlep;

import java.nio.ByteBuffer;

/**
 * Encapsulate a message and provide access to it's body and headers
 * @author elandau
 *
 */
public interface Message {
    /**
     * @return Return the message body as a string
     */
    public String getMessageBodyAsString();

    /**
     * Return the raw bytebuffer backing the message body
     * @return
     */
    public ByteBuffer getRawMessageBody();
    
    /**
     * @param decoder
     * @return Return the message 
     */
    public <T> void getMessageBody(MessageCodec<T> decoder);
    
    /**
     * Specify a header with a boolean value
     * @param name
     * @param value
     * @return
     */
    public Message withBooleanHeader(String name, String value);
    
    /**
     * Specify a header with a double value
     * @param name
     * @param value
     * @return
     */
    public Message withDoubleHeader(String name, String value);
    
    /**
     * Specify a header with a string value
     * @param name
     * @param value
     * @return
     */
    public Message withStringHeader(String name, String value);
    
    /**
     * Specify a header with a integer value
     * @param name
     * @param value
     * @return
     */
    public Message withIntegerHeader(String name, Integer value);
    
}
