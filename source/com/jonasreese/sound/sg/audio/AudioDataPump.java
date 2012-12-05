/*
 * Created on 20.06.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio;

import javax.sound.sampled.AudioFormat;

/**
 * <p>
 * This interface shall be implemented by classes that can asynchronously pump
 * audio data frames to a receiver class. It can be used if no input device
 * is available or desired as a data pump.
 * </p>
 * @author jonas.reese
 */
public interface AudioDataPump {
    /**
     * Gets the <code>AudioFormat</code> this <code>AudioDataPump</code>
     * generates as output.
     * @return The audio format.
     */
    public AudioFormat getAudioFormat();
}
