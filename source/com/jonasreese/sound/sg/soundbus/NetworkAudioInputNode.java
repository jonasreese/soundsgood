/*
 * Created on 28.05.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import javax.sound.sampled.AudioFormat;


/**
 * <p>
 * This class defines the <code>SbNode</code> interface for an input node that retrieves
 * audio data from a network (UDP).
 * </p>
 * @author jonas.reese
 */
public interface NetworkAudioInputNode extends SbNode {
    /**
     * Gets the UDP port on which audio packets shall be retrieved.
     * @return The audio data packet port.
     */
    public int getPort();

    /**
     * Sets the UDP port for receiving audio data.
     * @param port The port number.
     * @throws IllegalStateException if the parent soundbus is already open.
     */
    public void setPort( int port ) throws IllegalStateException;
    
    /**
     * Gets the <code>AudioFormat</code> that is expected from the network.
     * @return The expected audio format. Not <code>null</code>.
     */
    public AudioFormat getAudioFormat();

    /**
     * Sets the <code>AudioFormat</code> that is expected from the network.
     * @param format The expected audio format. <code>null</code> indicates
     * SG default audio format.
     */
    public void setAudioFormat( AudioFormat format ) throws IllegalStateException;
}
