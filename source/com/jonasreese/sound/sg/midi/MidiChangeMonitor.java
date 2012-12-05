/*
 * Created on 23.03.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import javax.sound.midi.MidiEvent;

/**
 * <p>
 * This interface shall be implemented by classes that wish to be informed
 * when a certain MIDI sequence has changed.
 * @see SgMidiSequence#addMidiChangeMonitor(MidiChangeMonitor)
 * </p>
 * @author jreese
 */
public interface MidiChangeMonitor {
    /**
     * Invoked when a number of MIDI events have been added to a <code>Track</code>.
     * @param sequence The track's parent <code>SgMidiSequence</code>.
     * @param track The <code>TrackProxy</code> containing the <code>javax.sound.midi.Track</code>.
     * @param events The <code>MidiEvent</code>s that have been added.
     * @param changeObj The object that caused the change.
     */
    public void midiEventsAdded(
        SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj );

    /**
     * Invoked when a number of MIDI events have been removed from a <code>Track</code>.
     * @param sequence The track's parent <code>SgMidiSequence</code>.
     * @param track The <code>TrackProxy</code> containing the <code>javax.sound.midi.Track</code>.
     * @param events The <code>MidiEvent</code>s that have been removed.
     * @param changeObj The object that caused the change.
     */
    public void midiEventsRemoved(
        SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj );

    /**
     * Invoked when a number of MIDI events have been changed without being added
     * or removed to/from the <code>Track</code>.
     * @param sequence The track's parent <code>SgMidiSequence</code>.
     * @param track The <code>TrackProxy</code> containing the <code>javax.sound.midi.Track</code>.
     * @param events The <code>MidiEvent</code>s that have been removed.
     * @param changeObj The object that caused the change.
     */
    public void midiEventsChanged(
        SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj );
    
    /**
     * Invoked when a MIDI track has been added to a MIDI sequence.
     * @param sequence The MIDI sequence the track has been added to.
     * @param track The MIDI track that has been added.
     * @param changeObj The object that caused the change.
     */
    public void midiTrackAdded( SgMidiSequence sequence, TrackProxy track, Object changeObj );

    /**
     * Invoked when a MIDI track has been removed from a MIDI sequence.
     * @param sequence The MIDI sequence the track has been removed from.
     * @param track The MIDI track that has been removed.
     * @param changeObj The object that caused the change.
     */
    public void midiTrackRemoved( SgMidiSequence sequence, TrackProxy track, Object changeObj );
    
    /**
     * Invoked when the length of a MIDI track has changed.
     * @param sequence The MIDI sequence the changed track belongs to.
     * @param track The track whose length has changed.
     * @param changeObj The object that caused the change.
     */
    public void midiTrackLengthChanged( SgMidiSequence sequence, TrackProxy track, Object changeObj );
    
    /**
     * Invoked when the <code>EventMap</code> of a <code>TrackProxy</code> has changed.
     * @param sequence The MIDI sequence the changed track belongs to.
     * @param track The track whose event map has changed.
     * @param changeObj The object that caused the change.
     */
    public void midiTrackEventMapChanged( SgMidiSequence sequence, TrackProxy track, Object changeObj );
}