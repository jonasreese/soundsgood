/*
 * Created on 05.04.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

/**
 * <p>
 * A <code>MidiException</code> or one of it's subclasses shall be
 * thrown whenever an exception concerning MIDI functionality occurs.
 * </p>
 * @author jreese
 */
public class MidiException extends Exception {
    
    private static final long serialVersionUID = 1;
    
    /**
     * Constructs a new empty <code>MidiException</code>.
     */
    public MidiException() {
        super();
    }
    
    /**
     * Constructs a new <code>MidiException</code> with an error message.
     * @param message The error message.
     */
    public MidiException( String message ) {
        super( message );
    }

    /**
     * Constructs a new <code>MidiException</code> with an error message
     * and a cause <code>Throwable</code>.
     * @param message The error message.
     * @param cause The <code>Throwable</code> that caused this exception.
     */
    public MidiException( String message, Throwable cause ) {
        super( message, cause );
    }

    /**
     * Constructs a new <code>MidiException</code> with a cause
     * <code>Throwable</code>.
     * @param cause The <code>Throwable</code> that caused this exception.
     */
    public MidiException( Throwable cause ) {
        super( cause );
    }
}
