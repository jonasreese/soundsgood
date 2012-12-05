/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 25.11.2003
 */
package com.jonasreese.sound.sg.midi.edit;

import javax.sound.midi.MidiEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.TrackProxy;

/**
 * <b>
 * An <code>UndoableEdit</code> implementation that allows to undo
 * a delete operation of MIDI events on a MIDI track. Please note that
 * in most cases, this class has to be overwritten in order to provide
 * special additional behaviour, like UI update or updates with other
 * dependencies. Overwritten methods should always call their super
 * implementations.
 * </b>
 * @author jreese
 */
public class DeleteEventsEdit extends SgUndoableEdit {

    private static final long serialVersionUID = 1;
    
    private boolean changed;
    private MidiDescriptor midiDescriptor;

    protected Object changeObj;
    
    protected MidiEvent[] events;
    protected TrackProxy track;
    
    /**
     * Constructs a new <code>DeleteEventsEdit</code>.
     * @param track The track to delete from. Shall not be <code>null</code>.
     * @param events A copy of the events to delete. Shall not be <code>null</code>.
     * @param midiDescriptor The <code>MidiDescriptor</code> containing the
     *        MIDI sequence that contains the given MIDI track. If the track
     *        passed to this constructor are not assigned to a <code>MidiDescriptor</code>,
     *        <code>midiDescriptor</code> may be <code>null</code>.
     * @param changeObj The <code>Object</code> that adds the events.
     *        Used to identify the source to a change on a MIDI track.
     */
    public DeleteEventsEdit(
        TrackProxy track, MidiEvent[] events, MidiDescriptor midiDescriptor, Object changeObj )
    {
        this.track = track;
        this.events = events;
        this.changeObj = changeObj;
        this.midiDescriptor = midiDescriptor;
        changed = (midiDescriptor != null ? midiDescriptor.isChanged() : true);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.undo.UndoableEdit#die()
     */
    public void die()
    {
        super.die();
        events = null;
        track = null;
    }
    /**
     * Checks if this edit has been undone and calls
     * <code>perform()</code>.
     */
    public void redo() throws CannotRedoException
    {
        super.redo();
        perform();
    }
    /**
     * Performs the undo. Overwrite this method with a call to
     * <code>super.undo()</code> in order to add task-specific
     * undo operations (like updating the UI, ...)
     */
    public void undo() throws CannotUndoException
    {
        super.undo();

        track.addAll( events, changeObj );
        if (midiDescriptor != null)
        {
            changed = changed || !midiDescriptor.isChanged();
            midiDescriptor.setChanged( changed );
        }
    }
    /* (non-Javadoc)
     * @see javax.swing.undo.UndoableEdit#getRedoPresentationName()
     */
    public String getRedoPresentationName()
    {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.redo" ) + " " + getPresentationName();
    }
    /* (non-Javadoc)
     * @see javax.swing.undo.UndoableEdit#getUndoPresentationName()
     */
    public String getUndoPresentationName()
    {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.undo" ) + " " + getPresentationName();
    }
    
    public String getPresentationName()
    {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.deleteEventsEdit" );
    }
    
    /**
     * Performs this <code>UndoableEdit</code>, so it can be undone afterwards.
     * Overwrite this method with a call to
     * <code>super.perform()</code> in order to add task-specific
     * edit perform and redo operations (like updating the UI, ...)
     */
    public void perform()
    {
        track.removeAll( events, changeObj );
        if (midiDescriptor != null)
        {
            midiDescriptor.setChanged( true );
        }
    }
}
