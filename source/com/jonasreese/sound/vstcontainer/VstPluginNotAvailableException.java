/*
 * Created on 19.10.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.vstcontainer;

/**
 * @author jonas.reese
 */
public class VstPluginNotAvailableException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public VstPluginNotAvailableException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public VstPluginNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public VstPluginNotAvailableException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public VstPluginNotAvailableException(Throwable cause) {
        super(cause);
    }

    
}