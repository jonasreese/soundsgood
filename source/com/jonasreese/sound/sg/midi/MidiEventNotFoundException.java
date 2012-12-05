/*
 * Created on 05.04.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;


/**
 * <p>
 * This exception shall be thrown when a (certain type of) MIDI event
 * could not be located but is required (e.g., for a certain functionality).
 * </p>
 * @author jreese
 */
public class MidiEventNotFoundException extends MidiException {

    private static final long serialVersionUID = 1;
    
    /**
     * Constructs a new empty <code>MidiEventNotFoundException</code>.
     */
    public MidiEventNotFoundException() {
        super();
    }
    
    /**
     * Constructs a new <code>MidiEventNotFoundException</code> with an error message.
     * @param message The error message.
     */
    public MidiEventNotFoundException( String message ) {
        super( message );
    }

    /**
     * Constructs a new <code>MidiEventNotFoundException</code> with an error message
     * and a cause <code>Throwable</code>.
     * @param message The error message.
     * @param cause The <code>Throwable</code> that caused this exception.
     */
    public MidiEventNotFoundException( String message, Throwable cause ) {
        super( message, cause );
    }

    /**
     * Constructs a new <code>MidiEventNotFoundException</code> with a cause
     * <code>Throwable</code>.
     * @param cause The <code>Throwable</code> that caused this exception.
     */
    public MidiEventNotFoundException( Throwable cause ) {
        super( cause );
    }
}
