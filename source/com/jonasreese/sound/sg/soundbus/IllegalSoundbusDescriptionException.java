/*
 * Created on 14.07.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

/**
 * This exception shall be raised if a problem was found in a soundbus
 * description (file).
 * 
 * @author jonas.reese
 */
public class IllegalSoundbusDescriptionException extends SoundbusException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an <code>IllegalSoundbusDescriptionException</code>.
     */
    public IllegalSoundbusDescriptionException() {
        super();
    }

    /**
     * Constructs an <code>IllegalSoundbusDescriptionException</code>
     * with an error message and cause <code>Throwable</code>.
     * @param msg The message.
     * @param cause The cause.
     */
    public IllegalSoundbusDescriptionException( String msg, Throwable cause ) {
        super( msg, cause );
    }

    /**
     * Constructs an <code>IllegalSoundbusDescriptionException</code>
     * with an error message.
     * @param msg The error message.
     */
    public IllegalSoundbusDescriptionException( String msg ) {
        super( msg );
    }

    /**
     * Constructs a new <code>IllegalSoundbusDescriptionException</code>
     * with a cause <code>Throwable</code>.
     * @param cause The cause.
     */
    public IllegalSoundbusDescriptionException( Throwable cause ) {
        super( cause );
    }
}
