/*
 * Created on 26.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;


/**
 * <p>
 * This interface shall be implemented by entities that represent
 * an output plug of a soundbus. Output plugs can be connected to input
 * plugs (<code>SbInput</code> instances) and send data (e.g., MIDI or
 * audio data) to them.
 * </p>
 * @author jonas.reese
 */
public interface SbOutput {

    /**
     * Returns a human-readable name for this output.
     * @return A human-readable name.
     */
    public String getName();
    
    /**
     * Returns a human-readable description for this output.
     * @return A human-readable description.
     */
    public String getDescription();
    
    /**
     * Gets the parent soundbus node.
     * @return The <code>SbNode</code> this <code>SbOutput</code> belongs to.
     */
    public SbNode getSbNode();
    
    /**
     * Checks if this <code>SbOutput</code> can be connected to the given
     * input and returns <code>true</code> if that is possible.
     * @param in The input.
     * @return <code>true</code> if a connection is possible, <code>false</code>
     * otherwise.
     */
    public boolean canConnect( SbInput in );
    
    /**
     * Connects this <code>SbOutput</code> with the given input.
     * @param in The input to connect with.
     * @throws CannotConnectException if the given input cannot be connected
     * to this output (e.g., because the types are incompatible).
     * @throws IllegalStateException if the parent soundbus is open.
     */
    public void connect( SbInput in ) throws CannotConnectException, IllegalStateException;
    
    /**
     * Disconnects this <code>SbOutput</code> from the connected input.
     * @throws IllegalStateException if the parent soundbus is open.
     */
    public void disconnect() throws IllegalStateException;
    
    /**
     * Gets the input that is currently connected to this <code>SbOutput</code>.
     * @return The connected input, or <code>null</code> if no input is currently
     * connected to this <code>SbOutput</code>.
     */
    public SbInput getConnectedInput();
}
