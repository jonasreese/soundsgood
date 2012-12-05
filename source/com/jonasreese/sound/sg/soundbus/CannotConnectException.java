/*
 * Created on 28.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

/**
 * <p>
 * This exception shall be thrown if a soundbus output could not be connected
 * to an input (e.g., the input and output types do not match).
 * </p>
 * @author jonas.reese
 */
public class CannotConnectException extends SoundbusException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new <code>CannotConnectException</code>.
     */
    public CannotConnectException() {
        super();
    }

    /**
     * Constructs a new <code>CannotConnectException</code> with a message.
     * @param msg The error message.
     */
    public CannotConnectException( String msg ) {
        super( msg );
    }

    /**
     * Constructs a new <code>CannotConnectException</code> with a message and
     * a cause.
     * @param msg The error message.
     * @param cause The cause for this <code>CannotConnectException</code>.
     */
    public CannotConnectException( String msg, Throwable cause ) {
        super( msg, cause );
    }

    /**
     * Constructs a new <code>CannotConnectException</code> with a cause.
     * @param cause The cause for this <code>CannotConnectException</code>.
     */
    public CannotConnectException( Throwable cause ) {
        super( cause );
    }
}
