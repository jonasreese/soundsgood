/*
 * Created on 14.05.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

/**
 * <p>
 * This interface shall be implemented by classes that map a
 * <code>TrackProxy</code> to a number of <code>ChannelMap</code>s.
 * A <code>ChannelMap</code> maps a MIDI device to a number of MIDI
 * channels that device is activated for.
 * </p>
 * @author jonas.reese
 */
public interface MidiDeviceMap {

    /**
     * Gets the <code>TrackProxy</code> with which the <code>ChannelMap</code>s
     * returned by <code>getChannelMaps()</code> is associated.
     * @return A <code>TrackProxy</code> object, or <code>null</code> if this
     * <code>MidiDeviceMap</code> is associated with any track.
     */
    public TrackProxy getTrack();
    
    /**
     * Gets info about all <code>MidiDevice</code>s which are activated for the MIDI track
     * returned by <code>getTrack()</code>.
     * @return A list of all MIDI devices information activate for the track returned by
     * <code>getTrack()</code>.
     */
    public MidiDeviceList getMidiDeviceList();
    
    /**
     * Adds a MIDI device to this <code>MidiDeviceMap</code>. If that device
     * has already been added, this method does nothing but return the
     * <code>MidiChannelMap</code> which has already been created for the given
     * device. If the given device has not yet been added, a newly created
     * <code>MidiChannelMap</code> is returned and the given device is included
     * into the array returned by <code>getMidiDevices()</code>.
     * @param device The MIDI device to add.
     * @return The (newly created) <code>MidiChannelMap</code> that will be
     * returned by <code>getChannelMapFor(device)</code>.
     */
    public MidiChannelMap addMidiDevice( MidiDeviceDescriptor device );
    
    /**
     * Removes a MIDI device from this <code>MidiDeviceMap</code>.
     * @param device The <code>MidiDevice</code> to be removed from this mapping.
     * @return <code>true</code> if the given <code>MidiDevice</code> was present
     * in the mapping and has now been removed, or <code>false</code> if the
     * <code>MidiDevice</code> did not exist in the mapping and this method
     * call did nothing.
     */
    public boolean removeMidiDevice( MidiDeviceDescriptor device );
    
    /**
     * Gets all <code>MidiChannelMap</code>s contained by this <code>MidiDeviceMap</code>.
     * @return An array of all contained <code>MidiChannelMap</code> objects, one per
     * <code>MidiDevice</code> (@see MidiDeviceMap#getMidiDevices()).
     */
    public MidiChannelMap[] getChannelMaps();
    
    /**
     * Gets a specific <code>MidiChannelMap</code>.
     * @param device The MIDI device to get a <code>MidiChannelMap</code> for.
     * @return The <code>MidiChannelMap</code> for the given MIDI device, or
     * <code>null</code> if none exists for the given MIDI device.
     */
    public MidiChannelMap getChannelMapFor( MidiDeviceDescriptor device );
    
    /**
     * Returns <code>true</code> if no MIDI devices are associated with this
     * <code>MidiDeviceMap</code>, or if only <code>ChannelMap</code>s are contained
     * which are empty.
     * @return <code>true</code> if empty, <code>false</code> otherwise.
     */
    public boolean isEmpty();
}
