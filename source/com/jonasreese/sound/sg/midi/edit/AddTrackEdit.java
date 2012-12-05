/*
 * Created on 17.04.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi.edit;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.SgMidiSequence;
import com.jonasreese.sound.sg.midi.TrackProxy;

/**
 * <p>
 * This class is the undoable edit class for a MIDI track add operation.
 * </p>
 * @author jreese
 */
public class AddTrackEdit extends SgUndoableEdit {

    private static final long serialVersionUID = 1;
    
    
    private boolean changed;
    private MidiDescriptor midiDescriptor;

    private TrackProxy newTrack;
    private SgMidiSequence sequence;
    protected Object changeObj;
    
    /**
     * Constructs a new <code>AddTrackEdit</code>.
     * @param sequence The MIDI <code>SgMidiSequence</code>.
     * @param midiDescriptor The <code>MidiDescriptor</code> containing the MIDI
     *        sequence. If the sequence passed to this constructor is not assigned to
     *        a <code>MidiDescriptor</code>, <code>midiDescriptor</code> may be
     *        <code>null</code>.
     * @param changeObj The object that adds the track.
     */
    public AddTrackEdit( SgMidiSequence sequence, MidiDescriptor midiDescriptor, Object changeObj )
    {
        this.sequence = sequence;
        this.changeObj = changeObj;
        newTrack = null;
        this.midiDescriptor = midiDescriptor;
        changed = (midiDescriptor != null ? midiDescriptor.isChanged() : true);
    }

    
    public String getRedoPresentationName()
    {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.redo" ) + " " + getPresentationName();
    }

    public String getUndoPresentationName()
    {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.undo" ) + " " + getPresentationName();
    }
    
    public String getPresentationName()
    {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.addTrackEdit" );
    }

    public void die()
    {
        super.die();
        sequence = null;
        newTrack = null;
    }

    public void undo()
    {
        super.undo();
        sequence.deleteTrackProxy( newTrack );
        if (midiDescriptor != null)
        {
            changed = changed || !midiDescriptor.isChanged();
            midiDescriptor.setChanged( changed );
        }
    }
    
    public void redo()
    {
        super.redo();
        perform();
    }
    
    /**
     * Gets the new <code>TrackProxy</code> created by the
     * <code>perform()</code> method.
     * @return The new <code>TrackProxy</code>, or <code>null</code>
     *         if <code>perform()</code> has not yet been called.
     */
    public TrackProxy getNewTrackProxy()
    {
        return newTrack;
    }

    public void perform()
    {
        newTrack = sequence.addTrackProxy( newTrack, changeObj );
        if (midiDescriptor != null)
        {
            midiDescriptor.setChanged( true );
        }
    }
}
