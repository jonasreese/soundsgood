/*
 * Created on 29.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import com.jonasreese.sound.sg.midi.MidiDeviceDescriptor;

/**
 * <p>
 * This class implements the <code>SbNode</code> interface for a MIDI output node.
 * </p>
 * @author jonas.reese
 */
public interface MidiOutputNode extends SbNode {
    /**
     * Gets the device that is associated with this <code>SbNode</code>.
     * @return The MIDI device represented by a <code>MidiDeviceDescriptor</code>,
     * or <code>null</code> if no device is set yet.
     */
    public MidiDeviceDescriptor getMidiDevice();

    /**
     * Associates a MIDI device with this <code>MidiOutputNode</code>.
     * @param midiDevice The MIDI device to be set for this <code>MidiOutputNode</code>,
     * or <code>null</code> if no device shall be associated.
     * @throws IllegalStateException if the parent soundbus is already open.
     */
    public void setMidiDevice( MidiDeviceDescriptor midiDevice ) throws IllegalStateException;
}
