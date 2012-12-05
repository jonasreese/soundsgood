/*
 * Created on 23.06.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio;

import javax.sound.sampled.AudioFormat;

/**
 * This interface shall be implemented by classes that can receive (and process)
 * audio data.
 * 
 * @author jonas.reese
 */
public interface AudioDataReceiver {
    
    /**
     * Check if the given <code>AudioFormat</code> can be received by this
     * <code>AudioDataReceiver</code>.
     * @param format The format to check.
     * @return <code>true</code> if the given format can be received,
     * <code>false</code> otherwise.
     */
    public boolean canReceive( AudioFormat format );

    /**
     * Gets the desired audio format.
     * @return The audio format.
     */
    public AudioFormat getAudioFormat();

    /**
     * Sets the audio format to the given format. This method
     * shall only be called  
     * @param format The audio format to set.
     * @throws IllegalArgumentException if <code>canReceive()</code> method
     * returns <code>false</code> for the given <code>AudioFormat</code>.
     */
    public void setAudioFormat( AudioFormat format );

    /**
     * Gets the <code>realtime</code> synchronization capability flag.
     * @return <code>true</code> if this input is a realtime-synchronous input meaning
     * it can handle flow control for real-time synchronization, <code>false</code> otherwise.
     */
    boolean isRealtimeSynchonous();
    
    /**
     * Gets the <code>realtime only</code> flag.
     * @return <code>true</code> if this input can only process realtime input (like
     * an audio device), <code>false</code> if it can process non-realtime input also
     * (like an audio disk writer).
     */
    boolean isRealtimeOnly();

    /**
     * Called by an audio data generating or forwarding entity to delegate binary
     * audio data.
     * @param audioData The audio data to be received.
     * @param offset The byte offset where to start receiving from the
     * <code>audioData</code> array.
     * @param length The number of bytes to be received.
     * @param pump The sending <code>AudioDataPump</code>.
     */
    public void receive( byte[] audioData, int offset, int length, AudioDataPump pump );

}
