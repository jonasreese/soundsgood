/*
 * Created on 28.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

/**
 * <p>
 * This class is the base class for all soundbus-related exceptions and can be
 * thrown if a general soundbus exception occurred.
 * </p>
 * @author jonas.reese
 */
public class SoundbusException extends Exception {
    private static final long serialVersionUID = 1L;

    private SbNode node;
    
    /**
     * Constructs a new <code>SoundbusException</code>.
     */
    public SoundbusException() {
        super();
    }

    /**
     * Constructs a new <code>SoundbusException</code> with a message.
     * @param msg The error message.
     */
    public SoundbusException( String msg ) {
        super( msg );
    }

    /**
     * Constructs a new <code>SoundbusException</code> with a message and
     * a cause.
     * @param msg The error message.
     * @param cause The cause for this <code>SoundbusException</code>.
     */
    public SoundbusException( String msg, Throwable cause ) {
        super( msg, cause );
    }

    /**
     * Constructs a new <code>SoundbusException</code> with a cause.
     * @param cause The cause for this <code>SoundbusException</code>.
     */
    public SoundbusException( Throwable cause ) {
        super( cause );
    }
    
    /**
     * Sets the node where a problem has occurred.
     * @param node The node, or <code>null</code> if the exception is not node-specific.
     */
    public void setNode( SbNode node ) {
        this.node = node;
    }
    
    /**
     * Gets the node where a problem has occurred.
     * @return The node, or <code>null</code> if the exception is not node-specific.
     */
    public SbNode getNode() {
        return node;
    }
}
