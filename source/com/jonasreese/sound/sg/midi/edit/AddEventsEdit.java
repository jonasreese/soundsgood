/*
 * Created on 12.12.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi.edit;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.TrackProxy;

/**
 * <p>
 * An <code>UndoableEdit</code> implementation that allows to undo
 * an add operation of of one or more MIDI events to a MIDI track.
 * Please note that in most cases, this class has to be overwritten
 * in order to provide special additional behaviour, like UI update
 * or updates with other dependencies. Overwritten methods should
 * always call their super implementations.
 * </p>
 * @author jreese
 */
public class AddEventsEdit extends SgUndoableEdit {
    private static final long serialVersionUID = 1;
    
    private boolean changed;
    private MidiDescriptor midiDescriptor;
    private String presentationName;
    
    protected TrackProxy track;
    protected MidiEvent[] events;
    protected Object changeObj;
    
    /**
     * Constructs a new <code>AddEventsEdit</code>.
     * @param track The track to which events are added.
     * @param events An array of the events that shall be
     *        added.
     * @param midiDescriptor The <code>MidiDescriptor</code> containing the
     *        MIDI sequence that contains the given MIDI events. If the events
     *        passed to this constructor are not assigned to a <code>MidiDescriptor</code>,
     *        <code>midiDescriptor</code> may be <code>null</code>.
     * @param presentationName The presentation name. If <code>null</code>, the default presentation
     *        name will be used.
     * @param changeObj The <code>Object</code> that adds the events.
     *        Used to identify the source to a change on a MIDI track.
     */
    public AddEventsEdit(
        TrackProxy track, MidiEvent[] events, MidiDescriptor midiDescriptor, String presentationName, Object changeObj )
    {
        this.track = track;
        this.events = events;
        this.changeObj = changeObj;
        this.midiDescriptor = midiDescriptor;
        this.presentationName = presentationName;
        changed = (midiDescriptor != null ? midiDescriptor.isChanged() : true);
    }
    
    /**
     * Constructs a new <code>AddEventsEdit</code> with the default presentation name.
     * @param track The track to which events are added.
     * @param events An array of the events that shall be
     *        added.
     * @param midiDescriptor The <code>MidiDescriptor</code> containing the
     *        MIDI sequence that contains the given MIDI events. If the events
     *        passed to this constructor are not assigned to a <code>MidiDescriptor</code>,
     *        <code>midiDescriptor</code> may be <code>null</code>.
     * @param changeObj The <code>Object</code> that adds the events.
     *        Used to identify the source to a change on a MIDI track.
     */
    public AddEventsEdit(
        TrackProxy track, MidiEvent[] events, MidiDescriptor midiDescriptor, Object changeObj )
    {
        this( track, events, midiDescriptor, null, changeObj );
    }

   /**
     * Copies the given events and returns them.
     * @param events The events to be copied.
     * @return A deep copy of the given events.
     */
    protected MidiEvent[] copyEvents( MidiEvent[] events )
    {
        MidiEvent[] result = new MidiEvent[events.length];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = new MidiEvent(
                (MidiMessage) events[i].getMessage().clone(), events[i].getTick() );
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.undo.UndoableEdit#die()
     */
    public void die()
    {
        super.die();
        events = null;
    }
    /**
     * Calls <code>super.redo()</code> and then the <code>perform()</code>
     * method.
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
        track.removeAll( events, changeObj );
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
        if (presentationName != null) { return presentationName; }
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.addEventsEdit" );
    }
    
    /**
     * This method adds the events passed to the constructor to the
     * track passed to the constructor.
     */
    public void perform()
    {
        track.addAll( events, changeObj );
        if (midiDescriptor != null)
        {
            midiDescriptor.setChanged( true );
        }
    }
}