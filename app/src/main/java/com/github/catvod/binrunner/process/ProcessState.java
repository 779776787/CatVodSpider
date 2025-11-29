package com.github.catvod.binrunner.process;

/**
 * Process state enumeration.
 * Represents all possible states of a BinRunner process.
 */
public enum ProcessState {
    /**
     * Process is waiting to be started
     */
    PENDING,
    
    /**
     * Process is currently running
     */
    RUNNING,
    
    /**
     * Process was stopped manually
     */
    STOPPED,
    
    /**
     * Process finished successfully
     */
    FINISHED,
    
    /**
     * Process encountered an error
     */
    ERROR,
    
    /**
     * Process is restarting after failure
     */
    RESTARTING
}
