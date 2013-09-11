package com.netflix.schlep.config;

/**
 * Utility class to simplify common ConfigurationReader use cases 
 * @author elandau
 *
 */
public final class ConfigurationReaders {
    /**
     * Return a ConfigurationReader that encapsulated an already instantiated
     * and configured object.  The mapper will do the proper error checking
     * to make sure the correct type is returned
     * 
     * @param object
     * @return
     */
    public <T> ConfigurationReader ofInstance(final T object) {
        return new ConfigurationReader() {
            @Override
            public <S> S read(Class<S> type) throws Exception {
                if (type.getClass().equals(object.getClass())) {
                    return (S)object;
                }
                throw new Exception(String.format("Can't convert '%s' to '%s'", 
                        object.getClass().getCanonicalName(),
                        type.getClass().getCanonicalName()));
            }
        };
    }
}   
