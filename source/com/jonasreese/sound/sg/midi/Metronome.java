/*
 * Created on 10.07.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import javax.sound.midi.Receiver;

/**
 * <p>
 * This interface shall be implemented by classes that perform metronome functionality.
 * </p>
 * @author jonas.reese
 * @see com.jonasreese.sound.sg.midi.MidiRecorder
 */
public interface Metronome {

    /**
     * Adds a MIDI output receiver to this <code>MidiRecorder</code>. An output
     * MIDI receiver receives all MIDI events that are sent to any output device.
     * @param midiOutputReceiver The <code>Receiver</code> that shall receive MIDI
     * events after being sent to an output device. If the given
     * <code>MidiOutputReceiver</code> has already been added, this method does nothing.
     */
    public void addMidiOutputReceiver( Receiver midiOutputReceiver );

    /**
     * Removes the given MIDI output receiver from this <code>MidiRecorder</code>.
     * @param midiOutputReceiver The MIDI output receiver that shall no longer
     * receive any MIDI events after being sent to an output device. If the given
     * <code>Receiver</code> is not registered as MIDI output receiver, this method
     * does nothing.
     */
    public void removeMidiOutputReceiver( Receiver midiOutputReceiver );
    
    /**
     * Enables/disables the default MIDI click device output.
     * @param defaultDeviceOutput <code>true</code> if the default device output
     * shall be enabled, <code>false</code> otherwise.
     */
    public void setDefaultDeviceOutputEnabled( boolean defaultDeviceOutputEnabled );
    
    /**
     * Gets the current default device output enabled state.
     * @return <code>true</code> if the click is sent to the default click devices
     * configured, <code>false</code> otherwise. Default value is <code>false</code>.
     */
    public boolean isDefaultDeviceOutputEnabled();

    /**
     * Enables/disables the sending of MIDI clock events (also known as MIDI beat clock,
     * MIDI timing clock).
     * @param sendMidiClockEnabled Enables/disables the sending MIDI clock events. 
     */
    public void setSendMidiClockEnabled(boolean sendMidiClockEnabled);
    
    /**
     * Gets the MIDI clock enabled state.
     * @return <code>true</code> if and only if sending of MIDI clock events is enabled.
     */
    public boolean isSendMidiClockEnabled();
    
    /**
     * Starts the metronome click.
     */
    public void start();
    
    /**
     * Stops the metronome click.
     */
    public void stop();
    
    /**
     * Synchronizes the metronome click.
     */
    public void sync();
    
    /**
     * Sets the number of beats per tact.
     */
    public void setBeatsPerTact( int beatsPerTact );
    
    /**
     * Gets the number of beats per tact.
     * @return The beats per tact.
     */
    public int getBeatsPerTact();
    
    /**
     * Gets the tact counter. The tact counter is incremented with each tact.
     * @return The tact counter.
     */
    public int getTactCounter();
    
    /**
     * Gets the metronome click's running state.
     * @return <code>true</code> if the metronome is running, <code>false</code>
     * otherwise.
     */
    public boolean isRunning();
    
    /**
     * Sets the tempo in beats per minute.
     * @param tempoInBpm The metronome tempo.
     */
    public void setTempoInBpm( float tempoInBpm );
    
    /**
     * Gets the tempo in beats per minute.
     * @return The current tempo.
     */
    public float getTempoInBpm();
    
    /**
     * Sets the tempo in microseconds per quarternote.
     * @param tempoInMpq The metronome tempo.
     */
    public void setTempoInMpq( float tempoInMpq );
    
    /**
     * Gets the tempo in microseconds per quarternote.
     * @return The current tempo.
     */
    public float getTempoInMpq();
}
