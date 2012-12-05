/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 11.12.2003
 */
package com.jonasreese.sound.sg.midi;

import java.util.EventObject;

import javax.sound.midi.MidiEvent;

/**
 * <p>
 * This event shall be sent to <code>MidiEventSelectionListener</code> implementations.
 * </p>
 * @author jreese
 */
public class MidiEventSelectionEvent extends EventObject {

    private static final long serialVersionUID = 1;
    
    protected TrackProxy track;
    protected int updateHint;
    protected MidiEvent[] midiEvents;
    
    public static final int UPDATE_HINT_SELECTION_EMPTY = 1;
    public static final int UPDATE_HINT_SELECTION_ADDED = 2;
    public static final int UPDATE_HINT_SELECTION_FULL = 4;
    public static final int UPDATE_HINT_SELECTION_REMOVED = 8;
    
    /**
     * Constructs a new <code>MidiEventSelectionEvent</code>.
     * @param source The source to the event.
     * @param track The <code>TrackProxy</code> on which the event selection
     *        changed.
     * @param updateHint An ORed int value providing information on how the
     *        selection changed.
     */
    public MidiEventSelectionEvent(
            Object source, TrackProxy track, int updateHint, MidiEvent[] midiEvents ) {
        super( source );
        this.track = track;
        this.updateHint = updateHint;
        this.midiEvents = midiEvents;
    }
    
    /**
     * Retuns the MIDI events that have been removed or added.
     * @return The MIDI events that have been removed or added as a <code>MidiEvent</code>
     * array, or <code>null</code> if either the selection has been cleared or all events
     * have been selected.
     */
    public MidiEvent[] getMidiEvents() {
        return midiEvents;
    }
    
    /**
     * Gets the parent <code>TrackProxy</code>.
     * @return The parent MIDI track.
     */
    public TrackProxy getTrack() {
        return track;
    }
    
    /**
     * Returns <code>true</code>, if the selection was cleared.
     * @return <code>true</code> if the selection is/was cleared, <code>false</code>
     *         otherwise.
     */
    public boolean isSelectionCleared() {
        return (updateHint & UPDATE_HINT_SELECTION_EMPTY) != 0;
    }
    
    /**
     * Returns <code>true</code> if events have been removed from the selection.
     * @return <code>true</code> if events have been removed from the selection,
     *         <code>false</code> otherwise.
     */
    public boolean isRemovedFromSelection() {
        return (updateHint & UPDATE_HINT_SELECTION_REMOVED) != 0;
    }
    
    /**
     * Returns <code>true</code> if events have been added to the selection.
     * @return <code>true</code> if events have been added to the selection,
     * <code>false</code> otherwise.
     */
    public boolean isAddedToSelection() {
        return (updateHint & UPDATE_HINT_SELECTION_ADDED) != 0;
    }
    
    /**
     * Returns <code>true</code> if all events were selected.
     * @return <code>true</code> if all events were selected, <code>false</code>
     * otherwise.
     */
    public boolean isAllSelected() {
        return (updateHint & UPDATE_HINT_SELECTION_FULL) != 0;
    }
}
