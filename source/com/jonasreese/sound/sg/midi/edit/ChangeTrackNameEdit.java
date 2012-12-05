/*
 * Created on 05.04.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi.edit;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.TrackProxy;

/**
 * <p>
 * This class is the undoable edit class for a track name change
 * operation.
 * </p>
 * @author jreese
 */
public class ChangeTrackNameEdit extends SgUndoableEdit {
    
    private static final long serialVersionUID = 1;
    
    private boolean changed;
    private MidiDescriptor midiDescriptor;

    private TrackProxy track;
    private String oldName;
    private String newName;
    protected Object changeObj;
    
    /**
     * Constructs a new <code>ChangeTrackNameEdit</code>.
     * @param track The MIDI <code>TrackProxy</code> whose
     *        name is to be changed.
     * @param newName The new name for the track.
     * @param midiDescriptor The <code>MidiDescriptor</code> containing the
     *        MIDI sequence that contains the given MIDI track. If the track
     *        passed to this constructor are not assigned to a <code>MidiDescriptor</code>,
     *        <code>midiDescriptor</code> may be <code>null</code>.
     * @param changeObj The object that changed the track name.
     */
    public ChangeTrackNameEdit(
        TrackProxy track,
        String newName,
        MidiDescriptor midiDescriptor,
        Object changeObj )
    {
        this.track = track;
        this.newName = newName;
        this.changeObj = changeObj;
        
        oldName = track.getTrackName();

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
            "edit.changeTrackNameEdit" );
    }

    public void die()
    {
        super.die();
        track = null;
        newName = null;
        oldName = null;
    }

    public void undo()
    {
        super.undo();
        track.setTrackName( oldName, changeObj );

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
        track.setTrackName( newName, changeObj );
        if (midiDescriptor != null)
        {
            midiDescriptor.setChanged( true );
        }
    }
}
