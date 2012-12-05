/*
 * Created on 23.05.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import javax.sound.midi.MidiDevice;

import com.jonasreese.sound.sg.SgEngine;

/**
 * <p>
 * This class is used to describe a <code>MidiDevice</code>.
 * </p>
 * @author jonas.reese
 */
public class MidiDeviceDescriptor {
    
    private MidiDevice.Info deviceInfo;
    private MidiDeviceId id;
    
    /**
     * Constructs a new <code>MidiDeviceDescriptor</code>.
     * @param deviceInfo The MIDI device info.
     * @param id A <code>MidiDeviceId</code> that identifies the MIDI device
     *        described by this <code>MidiDeviceDescriptor</code>.
     */
    public MidiDeviceDescriptor( MidiDevice.Info deviceInfo, MidiDeviceId id ) {
        this.deviceInfo = deviceInfo;
        if (id == null) {
            id = new MidiDeviceId(deviceInfo);
        }
        this.id = id;
    }
    
    /**
     * Gets the MIDI device info.
     * @return The MIDI device info.
     */
    public MidiDevice.Info getDeviceInfo() {
        return deviceInfo;
    }
    
    /**
     * Returns the ID.
     * @return The ID.
     */
    public MidiDeviceId getId() {
        return id;
    }
    
    /**
     * Sets the ID string.
     * @param id The ID string to set.
     */
    public void setId(MidiDeviceId id) {
        this.id = id;
    }
    
    public String toString() {
        return (deviceInfo == null ?
                SgEngine.getInstance().getResourceBundle().getString(
                        "midi.device.unavailable.label" ) :
                    deviceInfo.toString());
    }
    
    public boolean equals( MidiDeviceDescriptor another ) {
        if (id != null && another.id != null) {
            return id.equals(another.id);
        }
        return false;
    }
}
