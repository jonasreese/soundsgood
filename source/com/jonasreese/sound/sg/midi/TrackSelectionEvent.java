/*
 * Created on 25.06.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import java.util.EventObject;

/**
 * @author jonas.reese
 */
public class TrackSelectionEvent extends EventObject {
    
    private static final long serialVersionUID = 1;
    
    private SgMidiSequence sequence;
    private TrackProxy track;
    
    /**
     * Constructs a new <code>TrackSelectionEvent</code>.
     * @param source The object that caused this event.
     * @param sequence The parent <code>SgMidiSequence</code>.
     * @param track The newly selected <code>TrackProxy</code>. May be <code>null</code>.
     */
    public TrackSelectionEvent( Object source, SgMidiSequence sequence, TrackProxy track ) {
        super( source );
        this.sequence = sequence;
        this.track = track;
    }
    
    /**
     * Gets the <code>SgMidiSequence</code> on which the track selection has changed.
     * @return The <code>SgMidiSequence</code>.
     */
    public SgMidiSequence getSequence() {
        return sequence;
    }
    
    /**
     * Gets the newly selected <code>TrackProxy</code>.
     * @return The newly selected <code>TrackProxy</code>, or <code>null</code>
     * if none is selected.
     */
    public TrackProxy getTrack() {
        return track;
    }
}
