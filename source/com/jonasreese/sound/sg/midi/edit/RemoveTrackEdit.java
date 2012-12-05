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
 * This class is the undoable edit class for a MIDI track remove operation.
 * </p>
 * @author jreese
 */
public class RemoveTrackEdit extends SgUndoableEdit {
    
    private static final long serialVersionUID = 1;
    
    private boolean changed;
    private MidiDescriptor midiDescriptor;

    private TrackProxy removedTrack;
    private SgMidiSequence sequence;
    protected Object changeObj;
    private int trackIndex;
    private String[] removedPropertyNames;
    private String[] removedValues;
    
    /**
     * Constructs a new <code>RemoveTrackEdit</code>.
     * @param sequence The MIDI <code>SgMidiSequence</code>.
     * @param removedTrack The MIDI track that is to be removed.
     * @param midiDescriptor The <code>MidiDescriptor</code> containing the
     *        MIDI sequence that contains the given MIDI track. If the track
     *        passed to this constructor are not assigned to a <code>MidiDescriptor</code>,
     *        <code>midiDescriptor</code> may be <code>null</code>.
     * @param changeObj The object that removes the track.
     */
    public RemoveTrackEdit(
        SgMidiSequence sequence,
        TrackProxy removedTrack,
        MidiDescriptor midiDescriptor,
        Object changeObj )
    {
        this.sequence = sequence;
        this.removedTrack = removedTrack;
        this.changeObj = changeObj;
        trackIndex = -1;
        this.midiDescriptor = midiDescriptor;
        changed = (midiDescriptor != null ? midiDescriptor.isChanged() : true);
        removedPropertyNames = null;
        removedValues = null;
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
            "edit.removeTrackEdit" );
    }

    public void die()
    {
        super.die();
        sequence = null;
        removedTrack = null;
    }

    public void undo()
    {
        super.undo();
        if (trackIndex >= 0)
        {
            sequence.insertTrackProxyAt( trackIndex, removedTrack, changeObj );
            
            if (removedPropertyNames != null) {
                for (int i = 0; i < removedPropertyNames.length; i++) {
                    midiDescriptor.putPersistentClientProperty(
                            removedTrack, removedPropertyNames[i], removedValues[i] );
                }
            }
        }
        else
        {
            System.err.println( "RemoveTrackEdit.undo() - Error: trackIndex < 0!" );
        }
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
    
    public void perform()
    {
        // remember track-specific persistent client properties
        removedPropertyNames = midiDescriptor.getPersistentClientPropertyNames( removedTrack );
        if (removedPropertyNames != null) {
            removedValues = new String[removedPropertyNames.length];
            for (int i = 0; i < removedPropertyNames.length; i++) {
                removedValues[i] = midiDescriptor.getPersistentClientProperty( removedTrack, removedPropertyNames[i] );
            }
        }
        
        // remember the track index
        trackIndex = sequence.getIndexOf( removedTrack );
        System.out.println( "trackIndex = " + trackIndex );
        sequence.deleteTrackProxy( removedTrack, changeObj );
        if (midiDescriptor != null)
        {
            midiDescriptor.setChanged( true );
        }
    }
}
