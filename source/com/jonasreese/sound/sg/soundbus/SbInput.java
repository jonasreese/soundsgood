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
 * an input plug of a soundbus. Input plugs can receive input data
 * (e.g., MIDI or audio data) from <code>SbOutput</code> implementations.
 * </p>
 * @author jonas.reese
 */
public interface SbInput {
    /**
     * Returns a human-readable name for this input.
     * @return A human-readable name.
     */
    public String getName();
    
    /**
     * Returns a human-readable description for this input.
     * @return A human-readable description.
     */
    public String getDescription();
    
    /**
     * Gets the parent soundbus node.
     * @return The <code>SbNode</code> this <code>SbInput</code> belongs to.
     */
    public SbNode getSbNode();
    
    /**
     * This method is called when this <code>SbInput</code> shall be connected to the
     * given output.
     * @param output The output to connect to.
     * @throws CannotConnectException if this input cannot connect to the given output.
     * @throws IllegalStateException if the parent soundbus is open.
     */
    public void connect( SbOutput output ) throws CannotConnectException, IllegalStateException;
    
    /**
     * Called when a connected <code>SbOutput</code> shall be disconnected.
     * @throws IllegalStateException if the parent soundbus is open.
     */
    public void disconnect() throws IllegalStateException;
    
    /**
     * Gets the output that is currently connected to this <code>SbInput</code>.
     * @return The connected output, or <code>null</code> if no output is currently
     * connected to this <code>SbInput</code>.
     */
    public SbOutput getConnectedOutput();
}
