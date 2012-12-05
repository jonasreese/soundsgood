/*
 * Created on 02.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import com.jonasreese.sound.sg.midi.MidiDescriptor;

/**
 * <p>
 * This interface defines methods provided by a MIDI sampler node.
 * A MIDI sampler node can play MIDI samples. A MIDI input allows to control
 * the MIDI sampler node by sending MIDI events.
 * </p>
 * @author jonas.reese
 */
public interface MidiSamplerNode extends SbNode {
    /**
     * Sets the <code>MidiDescriptor</code> that shall be sampled.
     * @param midiDescriptor The <code>MidiDescriptor</code> containing the
     * <code>MIDI</code> file to be sampled.
     */
    public void setMidiDescriptor( MidiDescriptor midiDescriptor );
    
    /**
     * Gets the <code>MidiDescriptor</code> that shall be sampled.
     * @return The <code>MidiDescriptor</code> to sample, or <code>null</code>
     * if none is set.
     */
    public MidiDescriptor getMidiDescriptor();

    /**
     * Sets the retrigger mode. The retrigger mode determines what
     * happens if a trigger event comes in and the sample is still playing.
     * @param retriggerMode The <code>RetriggerMode</code> to set. Must
     * not be <code>null</code>.
     */
    public void setRetriggerMode( RetriggerMode retriggerMode );
    
    /**
     * Gets the current retrigger mode. The retrigger mode determines what
     * happens if a trigger event comes in and the sample is still playing.
     * @return The <code>RetriggerMode</code>.
     */
    public RetriggerMode getRetriggerMode();
    
    /**
     * Sets the sample mode.
     * @param sampleMode The sample mode to set. Must not be <code>null</code>. 
     */
    public void setSampleMode( SampleMode sampleMode );
    
    /**
     * Gets the current sample mode. Default is <code>LEFT_TO_RIGHT_MARKER</code>.
     * @return The current sample mode, never <code>null</code>.
     */
    public SampleMode getSampleMode();
    
    /**
     * Enables/disables the sample's default output device(s). 
     * @param enabled <code>true</code> if sample playback shall
     * send to default output(s), <code>false</code> otherwise.
     */
    public void setDefaultOutputsEnabled( boolean enabled );

    /**
     * Gets the default output enabled flag.
     * @return <code>true</code> if sample playback sends to default output(s),
     * <code>false</code> otherwise.
     */
    public boolean getDefaultOutputsEnabled();
    
    /**
     * This enumeration contains options for the sample playback.
     */
    public static enum SampleMode {
        LEFT_TO_RIGHT_MARKER,
        START_TO_END,
        LEFT_TO_RIGHT_MARKER_LOOP
    }
    
    /**
     * This enumeration contains options that determine what to do
     * on a trigger event if a sample is still running.
     */
    public static enum RetriggerMode {
        CONTINUE,
        STOP,
        RESTART,
        STOP_ON_NOTE_OFF
    }
}
