package com.netflix.schlep.eventbus.events;

public class SimpleEvent {
    private final int id ;
    
    public SimpleEvent(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "SimpleEvent [id=" + id + "]";
    }
    
}
