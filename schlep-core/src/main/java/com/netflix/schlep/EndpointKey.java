package com.netflix.schlep;

public class EndpointKey<T> {
    private final Class<T> type;
    private final String   name;
    
    public static <T> EndpointKey<T> of(String name, Class<T> type) {
        return new EndpointKey<T>(name, type);
    }
    
    public EndpointKey(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }
    
    public Class<T> getMessageType() {
        return type;
    }
    
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "EndpointKey [type=" + type + ", name=" + name + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EndpointKey<?> other = (EndpointKey<?>) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
