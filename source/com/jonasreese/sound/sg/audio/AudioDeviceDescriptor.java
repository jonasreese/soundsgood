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
    private String idString;
    
    /**
     * Constructs a new <code>MixerDescriptor</code>.
     * @param deviceInfo The audio device info.
     * @param idString A <code>String</code> that identifies the audio device
     *        described by this <code>MixerDescriptor</code>.
     */
    public AudioDeviceDescriptor( Mixer.Info deviceInfo, String idString ) {
        this.deviceInfo = deviceInfo;
        if (deviceInfo != null && idString == null) {
            idString = deviceInfo.getVendor() + deviceInfo.getName() +
                deviceInfo.getVersion() + deviceInfo.getDescription();
        }
        this.idString = stripWhitespaces( idString );
    }

    private String stripWhitespaces( String s ) {
        if (s == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!Character.isWhitespace( chars[i] )) {
                sb.append( chars[i] );
            }
        }
        return sb.toString();
    }
    
    /**
     * Gets the audio device info.
     * @return The audio device info.
     */
    public Mixer.Info getDeviceInfo() {
        return deviceInfo;
    }
    
    /**
     * Returns the ID string.
     * @return The ID string.
     */
    public String getIdString() {
        return idString;
    }
    
    /**
     * Sets the ID string.
     * @param idString The ID string to set.
     */
    public void setIdString( String idString ) {
        this.idString = stripWhitespaces(idString);
    }
    
    public String toString() {
        if (deviceInfo == null) {
            return SgEngine.getInstance().getResourceBundle().getString(
                "audio.device.unavailable.label" );
        }
        return deviceInfo.getName();
    }
    
    public boolean equals( AudioDeviceDescriptor another ) {
        if (deviceInfo != null && another.deviceInfo != null) {
            return (deviceInfo == another.deviceInfo);
        } else if (idString != null && another.idString != null) {
            return (idString.equals( another.idString ));
        }
        return false;
    }
}
