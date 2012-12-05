/*
 * Created on 02.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import com.jonasreese.sound.sg.midi.MidiFilter;

/**
 * <p>
 * This interface defines methods provided by a MIDI filter node.
 * A MIDI filter node is a connector with one MIDI input and one MIDI output,
 * where the output only sends MIDI data that matches some filter criteria.
 * </p>
 * @author jonas.reese
 */
public interface MidiFilterNode extends SbNode {
    /**
     * Sets a <code>MidiFilter</code>.
     * @param midiFilter The <code>MidiFilter</code> to be used.
     */
    public void setMidiFilter( MidiFilter midiFilter );

    /**
     * Gets the current <code>MidiFilter</code>.
     * @param midiFilter The <code>MidiFilter</code> that is currently used.
     */
    public MidiFilter getMidiFilter();
}
