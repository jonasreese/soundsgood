/*
 * Created on 28.05.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import com.jonasreese.sound.sg.audio.AudioDeviceDescriptor;

/**
 * <p>
 * This class implements the <code>SbNode</code> interface for an audio output node.
 * </p>
 * @author jonas.reese
 */
public interface AudioOutputNode extends SbNode {
    /**
     * Gets the device that is associated with this <code>SbNode</code>.
     * @return The audio device represented by an <code>AudioDeviceDescriptor</code>,
     * or <code>null</code> if no device is set yet.
     */
    public AudioDeviceDescriptor getAudioDevice();

    /**
     * Associates an audio device with this <code>AudioOutputNode</code>.
     * @param audioDevice The audio device to be set for this <code>AudioOutputNode</code>,
     * or <code>null</code> if no device shall be associated.
     * @throws IllegalStateException if the parent soundbus is already open.
     */
    public void setAudioDevice( AudioDeviceDescriptor audioDevice ) throws IllegalStateException;
}
