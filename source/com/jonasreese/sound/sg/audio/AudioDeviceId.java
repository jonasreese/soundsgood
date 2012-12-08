/*
 * Created on 27.11.2012
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio;

import javax.sound.sampled.Mixer;

/**
 * Helper class that represents a serializable and (pseudo-)unique ID for an
 * audio device.
 * 
 * @author Jonas Reese
 */
public class AudioDeviceId {

    private String id;
    
    public AudioDeviceId(String id) {
        this.id = stripWhitespaces(id);
    }
    
    public AudioDeviceId(Mixer.Info info) {
        this(info.getVendor() + info.getName() + info.getVersion() + info.getDescription());
    }

    public String getIdString() {
        return id;
    }
    
    private String stripWhitespaces(String s) {
        if (s == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!Character.isWhitespace(chars[i])) {
                sb.append(chars[i]);
            }
        }
        return sb.toString();
    }
    
    public boolean equals(Object another) {
        return (another instanceof AudioDeviceId && ((AudioDeviceId) another).id != null && id != null && ((AudioDeviceId) another).id.equals(id));
    }
}
