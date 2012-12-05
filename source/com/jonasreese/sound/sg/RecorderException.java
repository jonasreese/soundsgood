/*
 * Created on 18.12.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg;

/**
 * This exception shall be thrown if a <code>Recorder</code> came into an
 * exceptional state.
 * 
 * @author Jonas Reese
 */
public class RecorderException extends Exception {
    private static final long serialVersionUID = 1L;

    public RecorderException() {
        super();
    }

    public RecorderException( String message, Throwable cause ) {
        super( message, cause );
    }

    public RecorderException( String message ) {
        super( message );
    }

    public RecorderException( Throwable cause ) {
        super( cause );
    }
}
