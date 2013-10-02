package com.netflix.schlep.component;

/**
 * Base interface for any component that has a start/stop/pause/resume states
 * 
 *                       stop
 *             +-----------------------+
 *    start    |   pause               v
 * o---------->o---------->o---------->o
 *             ^           |    stop
 *             +-----------+
 *                 resume
 *             
 * @author elandau
 *
 */
public interface Component {
    /**
     * Start the component.  Spins up any thread pool 
     */
    void start() throws Exception;
    
    void stop() throws Exception;
    
    void pause() throws Exception;
    
    void resume() throws Exception;
    
    String getId();
}
