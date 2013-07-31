package com.netflix.schlep;

import java.nio.ByteBuffer;

/**
 * Codec to convert a message to either a specific type
 * 
 * @author elandau
 *
 * @param <T>
 */
public interface MessageCodec<T> {
    /**
     * @param bb
     * @return Decode a message from a byte buffer.
     */
    public T decode(ByteBuffer bb);
    
    /**
     * @param object
     * @return Encode a message into a byte buffer for transmission of the wire
     */
    public ByteBuffer encode(T object);
}
