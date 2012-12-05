/*
 * Created on 14.05.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

/**
 * <p>
 * This interface maps a <code>MidiDevice</code> object to a number of
 * MIDI channels.
 * </p>
 * @author jonas.reese
 */
public interface MidiChannelMap {
    /**
     * Gets the <code>MidiDeviceDescriptor</code> mapped to a number of channels.
     * @return The <code>MidiDeviceDescriptor</code> (not <code>null</code>).
     */
    public MidiDeviceDescriptor getMidiDeviceDescriptor();
    
    /**
     * Adds a channel.
     * @param channel The channel number to add, or <code>-1</code> to add
     * all channels.
     * @return <code>true</code> if the channel is successfully added,
     * <code>false</code> if the channel has already been added.
     */
    public boolean addChannel( int channel );
    
    /**
     * Removes a channel.
     * @param channel The MIDI channel number.
     * @return <code>true</code> if channel is successfully removed,
     * <code>false</code> if channel did not exist in the mapping.
     */
    public boolean removeChannel( int channel );
    
    /**
     * Removes all channels.
     */
    public void removeAllChannels();
    
    /**
     * Gets an array of all MIDI channels that are enabled for the MIDI
     * device returned by <code>getMidiDeviceDescriptor()</code>.
     * @return An array of channel numbers, or <code>[]</code> if no channel
     * is mapped.
     */
    public int[] getChannels();
    
    /**
     * Returns <code>true</code> if and only if this is an empty mapping,
     * meaning that <code>getChannels()</code> returns <code>[]</code>.
     * @return <code>true</code> if and only if the channel mapping is empty.
     */
    public boolean isEmpty();
}
