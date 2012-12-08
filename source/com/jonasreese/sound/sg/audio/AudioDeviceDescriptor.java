/*
 * Created on 23.05.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio;

import javax.sound.sampled.Mixer;

import com.jonasreese.sound.sg.SgEngine;

/**
 * <p>
 * This class is used to describe a <code>Mixer</code> (audio device).
 * </p>
 * @author jonas.reese
 */
public class AudioDeviceDescriptor {
    
    private Mixer.Info deviceInfo;
    private AudioDeviceId id;
    
    /**
     * Constructs a new <code>MixerDescriptor</code>.
     * @param deviceInfo The audio device info.
     * @param id A <code>String</code> that identifies the audio device
     *        described by this <code>MixerDescriptor</code>.
     */
    public AudioDeviceDescriptor( Mixer.Info deviceInfo, AudioDeviceId id ) {
        this.deviceInfo = deviceInfo;
        if (id == null) {
            id = new AudioDeviceId(deviceInfo);
        }
        this.id = id;
    }

    /**
     * Gets the audio device info.
     * @return The audio device info.
     */
    public Mixer.Info getDeviceInfo() {
        return deviceInfo;
    }
    
    /**
     * Returns the ID.
     * @return The ID.
     */
    public AudioDeviceId getId() {
        return id;
    }
    
    /**
     * Sets the ID.
     * @param id The ID to set.
     */
    public void setId( AudioDeviceId id ) {
        this.id = id;
    }
    
    public String toString() {
        if (deviceInfo == null) {
            return SgEngine.getInstance().getResourceBundle().getString(
                "audio.device.unavailable.label" );
        }
        return deviceInfo.getName();
    }
    
    public boolean equals( AudioDeviceDescriptor another ) {
        if (id != null && another.id != null) {
            return id.equals(another.id);
        }
        return false;
    }
}
