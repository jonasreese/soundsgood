/*
 * Created on 07.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import javax.sound.midi.MidiMessage;

/**
 * A generic interface that shall be implemented if MIDI data input
 * shall be monitored by clients.
 * @see SbMonitorableMidiInput
 * 
 * @author jonas.reese
 */
public interface MidiInputMonitor {
    /**
     * Invoked when a message is received on a <code>SbMonitorableMidiInput</code>.
     * Invokation of this method may be omitted if the <code>messageProcessed()</code>
     * method is called instead and the message itself has not been modified since
     * it was received.
     * @param m The message that has been received.
     * @param output The output from which the message has been received.
     */
    public void messageReceived( MidiMessage m, SbOutput output );

    /**
     * Invoked when a message is processed by a <code>SbMonitorableMidiInput</code>.
     * @param m The message that has been received.
     * @param output The output from which the message has been received.
     * @param result The processing result. Value and type of <code>result</code>
     * are determined by the node type and thus have to be documented seperately.
     */
    public void messageProcessed( MidiMessage m, SbOutput output, Object result );
}
