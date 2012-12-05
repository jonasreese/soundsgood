/*
 * Created on 12.07.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

/**
 * This exception shall be raised when a soundbus node type name
 * could not be resolved.
 * 
 * @author jonas.reese
 */
public class UnknownNodeTypeException extends SoundbusException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an empty <code>UnknownNodeTypeException</code>.
     */
    public UnknownNodeTypeException() {
        super();
    }

    /**
     * Constructs an <code>UnknownNodeTypeException</code> with a message
     * an a cause <code>Throwable</code>.
     * @param msg The message.
     * @param cause The cause.
     */
    public UnknownNodeTypeException( String msg, Throwable cause ) {
        super( msg, cause );
    }

    /**
     * Constructs an <code>UnknownNodeTypeException</code> with a message.
     * @param msg The message.
     */
    public UnknownNodeTypeException( String msg ) {
        super(msg);
    }

    /**
     * Constructs an <code>UnknownNodeTypeException</code> with a cause
     * <code>Throwable</code>.
     * @param cause
     */
    public UnknownNodeTypeException( Throwable cause ) {
        super( cause );
    }
}
