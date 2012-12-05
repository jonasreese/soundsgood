/*
 * Created on 16.12.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import com.jonasreese.sound.sg.audio.AudioDescriptor;

/**
 * This interface shall be implemented by nodes that can sample audio clips.
 * 
 * @author Jonas Reese
 */
public interface AudioSamplerNode extends SbNode {

    /**
     * Sets the <code>AudioDescriptor</code> that shall be sampled.
     * @param audioDescriptor The <code>AudioDescriptor</code> containing the
     * audio file to be sampled.
     */
    public void setAudioDescriptor( AudioDescriptor audioDescriptor );
    
    /**
     * Gets the <code>AudioDescriptor</code> that shall be sampled.
     * @return The <code>AudioDescriptor</code> to sample, or <code>null</code>
     * if none is set.
     */
    public AudioDescriptor getAudioDescriptor();

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
     * Gets the current sample mode. Default is <code>START_TO_END</code>.
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
        START_TO_END,
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
