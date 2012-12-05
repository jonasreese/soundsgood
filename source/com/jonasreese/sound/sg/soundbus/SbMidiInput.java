/*
 * Created on 28.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import javax.sound.midi.MidiMessage;

/**
 * <p>
 * This interface shall be implemented by entities that represent
 * a MIDI input plug of a soundbus. It can receive MIDI events from
 * an <code>SbMidiOutput</code>.
 * </p>
 * @author jonas.reese
 */
public interface SbMidiInput extends SbInput {
    /**
     * Called by an <code>SbOutput</code> instance to receive a
     * <code>MidiEvent</code>.
     * @param m The <code>MidiMessage</code> to be received.
     * @param output The sending output, typically (but not necessarily)
     * a <code>SbMidiOutput</code> instance.
     */
    public void receive( MidiMessage m, SbOutput output );
}