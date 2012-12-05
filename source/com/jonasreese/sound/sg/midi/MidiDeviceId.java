/*
 * Created on 27.11.2012
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import javax.sound.midi.MidiDevice;

/**
 * Helper class that represents a serializable and (pseudo-)unique ID for a
 * MIDI device.
 * 
 * @author Jonas Reese
 */
public class MidiDeviceId {

    private String id;
    
    public MidiDeviceId(String id) {
        this.id = stripWhitespaces(id);
    }
    
    public MidiDeviceId(MidiDevice.Info info) {
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
        return (another instanceof MidiDeviceId && ((MidiDeviceId) another).id != null && id != null && ((MidiDeviceId) another).id.equals(id));
    }
}
