/*
 * Created on 03.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import java.util.EventObject;

/**
 * @author jonas.reese
 */
public class ConnectionEvent extends EventObject {
    private static final long serialVersionUID = 1L;

    private SbInput input;
    private SbOutput output;
    
    /**
     * Constructs a new <code>ConnectionEvent</code>.
     * @param source The event source object.
     * @param input The soundbus input.
     * @param output The soundbus output.
     */
    public ConnectionEvent( Object source, SbInput input, SbOutput output ) {
        super( source );
        this.input = input;
        this.output = output;
    }
    
    /**
     * Gets the soundbus input that has been connected/disconnected.
     * @return The <code>SbInput</code>.
     */
    public SbInput getInput() {
        return input;
    }

    /**
     * Gets the soundbus input that has been connected/disconnected.
     * @return The <code>SbInput</code>.
     */
    public SbOutput getOutput() {
        return output;
    }
}
