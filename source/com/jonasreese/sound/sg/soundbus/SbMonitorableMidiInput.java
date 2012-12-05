/*
 * Created on 07.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

/**
 * This <code>SbMidiInput</code> specialization allows to add
 * monitors so that a clients (e.g., a UI) are informed about what
 * MIDI events are received an how they are processed.
 * @author jonas.reese
 */
public interface SbMonitorableMidiInput extends SbMidiInput {
    /**
     * Adds a <code>MidiInputMonitor</code> to this <code>SbMonitorableMidiInput</code>.
     * <p>You should not forget to remove that monitor after it is used no more.
     * @param monitor The monitor to add. If already added, this method does nothing.
     */
    public void addMidiInputMonitor( MidiInputMonitor monitor );
    /**
     * Removes a <code>MidiInputMonitor</code> from this <code>SbMonitorableMidiInput</code>.
     * @param monitor The monitor to be removed. If not registered, this method does nothing.
     */
    public void removeMidiInputMonitor( MidiInputMonitor monitor );
}
