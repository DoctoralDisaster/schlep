package com.netflix.schlep.config;

/**
 * Abstraction on top of a configuration context that is meant to 
 * be passed as a parameter using assisted inject.  The purpose
 * of the mapper is to abstract out the configuration mechanism from
 * the implementation when a configuration must be passed into a 
 * framework and the caller has no knowledge of which type is
 * supposed to be instantiated.
 * 
 * @author elandau
 *
 */
public interface ConfigurationReader {
    /**
     * Construct an object.  If using guice the concrete mapper may
     * choose to using the Injector for object instantiation.
     * 
     * @param type
     * @return
     * @throws Exception
     */
    public <T> T read(Class<T> type) throws Exception ;
}
