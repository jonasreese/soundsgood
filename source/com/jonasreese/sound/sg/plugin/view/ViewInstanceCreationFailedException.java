/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 10.10.2003
 */
package com.jonasreese.sound.sg.plugin.view;

/**
 * <b>
 * This exception is thrown when a view instance creation failed.
 * </b>
 * @author jreese
 */
public class ViewInstanceCreationFailedException extends Exception {

    private static final long serialVersionUID = 1;
    
    /**
     * Constructs a new <code>ViewInstanceCreationFailedException</code>.
     */
    public ViewInstanceCreationFailedException() {
        super();
    }

    /**
     * Constructs a new <code>ViewInstanceCreationFailedException</code>.
     * @param message The error message.
     */
    public ViewInstanceCreationFailedException( String message ) {
        super( message );
    }
    
    /**
     * Constructs a new <code>ViewInstanceCreationFailedException</code>.
     * @param message The error message.
     * @param cause The original cause's <code>Throwable</code>.
     */
    public ViewInstanceCreationFailedException( String message, Throwable cause ) {
        super( message, cause );
    }

    /**
     * Constructs a new <code>ViewInstanceCreationFailedException</code>.
     * @param cause The original cause's <code>Throwable</code>.
     */
    public ViewInstanceCreationFailedException( Throwable cause ) {
        super( cause );
    }
}