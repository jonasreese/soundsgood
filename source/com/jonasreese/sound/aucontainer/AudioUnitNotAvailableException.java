/*
 * Created on 19.01.2008
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.aucontainer;

/**
 * @author Jonas Reese
 */
public class AudioUnitNotAvailableException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public AudioUnitNotAvailableException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public AudioUnitNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public AudioUnitNotAvailableException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public AudioUnitNotAvailableException(Throwable cause) {
        super(cause);
    }

    
}
