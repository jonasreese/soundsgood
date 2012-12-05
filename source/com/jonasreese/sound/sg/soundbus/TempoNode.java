/*
 * Created on 02.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import com.jonasreese.sound.sg.midi.Metronome;

/**
 * <p>
 * This interface defines methods provided by a tempo node.
 * A tempo node controls the tempo (BPM) for the whole soundbus.
 * </p>
 * @author jonas.reese
 */
public interface TempoNode extends SbNode {
    
    /**
     * Sets the <code>play click</code> property. This will enable
     * the metronome playback through the devices configured in the SoundsGood
     * application properties.
     * @param defaultClick If <code>true</code>, enables the default click playback.
     */
    public void setPlayDefaultClick( boolean defaultClick );
    
    /**
     * Gets the current default click property.
     * @return <code>true</code> if the default click playback is enabled.
     */
    public boolean getPlayDefaultClick();
    
    /**
     * Enables/disables the metronome.
     * @param clickEnabled If <code>true</code>, the click will be enabled.
     */
    public void setClickEnabled( boolean clickEnabled );
    
    /**
     * Gets the metronome enabled state.
     * @return <code>true</code> if the metronome click is enabled, <code>false</code>
     * otherwise.
     */
    public boolean isClickEnabled();
    
    /**
     * Sets the number of beats per tact for the metronome.
     * @param beatsPerTact The beats per tact.
     */
    public void setBeatsPerTact( int beatsPerTact );
    
    /**
     * Gets the number of beats per tact.
     * @return The number of beats per tact.
     */
    public int getBeatsPerTact();
    
    /**
     * Enables/disables sending of tempo control events.
     * @param sendTempoControlEventsEnabled <code>true</code> if sending of tempo control events
     * shall be enabled, <code>false</code> otherwise.
     */
    public void setSendTempoControlEventsEnabled( boolean sendTempoControlEventsEnabled );
    
    /**
     * Gets the tempo control event enabled state.
     * @return <code>true</code> if and only if sending tempo control events is enabled. 
     */
    public boolean isSendTempoControlEventsEnabled();
    
    /**
     * Returns the metronome, or <code>null</code> if metronome is not enabled.
     * @return The <code>Metronome</code>.
     */
    public Metronome getMetronome();
}
