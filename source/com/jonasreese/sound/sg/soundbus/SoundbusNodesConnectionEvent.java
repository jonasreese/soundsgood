/*
 * Created on 12.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

/**
 * @author jonas.reese
 */
public class SoundbusNodesConnectionEvent extends SoundbusEvent {
    private static final long serialVersionUID = 1L;

    private SbInput input;
    private SbOutput output;
    
    /**
     * Constructs a new <code>SoundbusNodesConnectionEvent</code>.
     * @param source The source <code>Soundbus</code>.
     * @param input The input that has been connected to an output.
     * @param output The output that has been connected to an input.
     */
    public SoundbusNodesConnectionEvent( Soundbus source, SbInput input, SbOutput output ) {
        super( source, null );
        this.input = input;
        this.output = output;
    }

    /**
     * Returns <code>null</code>.
     * @return <code>null</code>.
     */
    public SbNode getNode() {
        return null;
    }

    /**
     * Gets the input that has been connected to an output.
     * @return The <code>SbInput</code>.
     */
    public SbInput getInput() {
        return input;
    }

    /**
     * Gets the output that has been connected to an input.
     * @return The <code>SbOutput</code>.
     */
    public SbOutput getOutput() {
        return output;
    }
    
    
}