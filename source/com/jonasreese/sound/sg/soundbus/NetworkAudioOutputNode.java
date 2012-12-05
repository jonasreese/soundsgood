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
 * This class defines the <code>SbNode</code> interface for a node that retrieves
 * audio data and sends it to a network (UDP).
 * </p>
 * @author jonas.reese
 */
public interface NetworkAudioOutputNode extends SbNode {
    /**
     * Gets the UDP port on which audio packets shall be sent.
     * @return The audio data packet port.
     */
    public int getPort();

    /**
     * Sets the UDP port for sending audio data.
     * @param port The port number.
     * @throws IllegalStateException if the parent soundbus is already open.
     */
    public void setPort( int port ) throws IllegalStateException;
    
    /**
     * Gets the audio data destination host name as a string.
     * @return The destination address.
     */
    public String getDestination();
    
    /**
     * Sets the audio data destination host name as a string.
     * @param destination The destination host.
     * @throws IllegalStateException
     */
    public void setDestination( String destination ) throws IllegalStateException;

    /**
     * Gets the <code>AudioFormat</code> that is sent to the network.
     * @return The target audio format. Not <code>null</code>.
     */
    public AudioFormat getAudioFormat();

    /**
     * Sets the <code>AudioFormat</code> that is sent to the network.
     * @param format The target audio format. <code>null</code> indicates
     * SG default audio format.
     */
    public void setAudioFormat( AudioFormat format ) throws IllegalStateException;
}
