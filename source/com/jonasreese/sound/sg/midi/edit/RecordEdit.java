/*
 * Created on 04.10.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi.edit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
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
 * An <code>SgUndoableEdit</code> implementation that allows to undo
 * a record operation of of one or more MIDI events on one or more MIDI tracks.
 * </p>
 * @author jonas.reese
 */
public class RecordEdit extends SgUndoableEdit {
    
    private static final long serialVersionUID = 1;
    
    private boolean changed;
    private MidiDescriptor midiDescriptor;
    private String presentationName;
    
    protected List<Object[]> tracksAndEvents;
    protected Object changeObj;

    /**
     * Constructs a new <code>RecordEdit</code>.
     * @param midiDescriptor The <code>MidiDescriptor</code> containing the
     *        MIDI sequence that contains the given MIDI events. If the events
     *        passed to this constructor are not assigned to a <code>MidiDescriptor</code>,
     *        <code>midiDescriptor</code> may be <code>null</code>.
     * @param presentationName The presentation name. If <code>null</code>, the default presentation
     *        name will be used.
     * @param changeObj The <code>Object</code> that adds the events.
     *        Used to identify the source to a change on a MIDI track.
     */
    public RecordEdit(
        MidiDescriptor midiDescriptor,
        String presentationName,
        Object changeObj) {
        this.changeObj = changeObj;
        this.midiDescriptor = midiDescriptor;
        this.presentationName = presentationName;
        changed = (midiDescriptor != null ? midiDescriptor.isChanged() : true);
        tracksAndEvents = new ArrayList<Object[]>();
    }

    /**
     * Constructs a new <code>RecordEdit</code> with the default presentation name.
     * @param midiDescriptor The <code>MidiDescriptor</code> containing the
     *        MIDI sequence that contains the given MIDI events. If the events
     *        passed to this constructor are not assigned to a <code>MidiDescriptor</code>,
     *        <code>midiDescriptor</code> may be <code>null</code>.
     * @param changeObj The <code>Object</code> that adds the events.
     *        Used to identify the source to a change on a MIDI track.
     */
    public RecordEdit(
        MidiDescriptor midiDescriptor,
        Object changeObj) {
        this( midiDescriptor, null, changeObj );
    }

    /**
     * Adds a <code>TrackProxy</code> to this <code>RecordEdit</code>. This
     * method shall only be called before the <code>perform()</code>, <code>undo()</code>
     * and <code>redo()</code> methods are called, not inbetween.
     * @param track The track to which events have been added.
     * @param events An array of the events that have been added to the given track.
     * @param originalLength The original Track length (in Ticks) before recording.
     */
    public void addEditTrack( TrackProxy track, MidiEvent[] events, long originalLength ) {
        tracksAndEvents.add(
                new Object[] {
                        track, events, new Long( originalLength ), new Long( track.getLength() ) } );
    }
    
    /**
     * Copies the given events and returns them.
     * @param events The events to be copied.
     * @return A deep copy of the given events.
     */
    protected MidiEvent[] copyEvents(MidiEvent[] events) {
        MidiEvent[] result = new MidiEvent[events.length];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = new MidiEvent(
                (MidiMessage) events[i].getMessage().clone(), events[i].getTick());
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.undo.UndoableEdit#die()
     */
    public void die() {
        super.die();
        tracksAndEvents = null;
    }
    /**
     * Calls <code>super.redo()</code> and then the <code>perform()</code>
     * method.
     */
    public void redo() throws CannotRedoException {
        super.redo();
        for (Iterator<Object[]> iter = tracksAndEvents.iterator(); iter.hasNext(); ) {
            Object[] o = iter.next();
            TrackProxy track = (TrackProxy) o[0];
            MidiEvent[] events = (MidiEvent[]) o[1];
            long currentLength = ((Long) o[3]).longValue();
            track.addAll(events, changeObj);
            try {
                track.setLength(currentLength, changeObj);
            } catch (InvalidMidiDataException e) {
                // should not occur
                e.printStackTrace();
            }
            if (midiDescriptor != null) {
                midiDescriptor.setChanged(true);
            }
        }
    }
    /**
     * Performs the undo. Overwrite this method with a call to
     * <code>super.undo()</code> in order to add task-specific
     * undo operations (like updating the UI, ...)
     */
    public void undo() throws CannotUndoException {
        super.undo();
        for (Object[] o : tracksAndEvents) {
            TrackProxy track = (TrackProxy) o[0];
            MidiEvent[] events = (MidiEvent[]) o[1];
            long originalLength = ((Long) o[2]).longValue();
            System.out.println( "Removing " + events.length + " events from track " + track.getTrackName() );
            track.removeAll(events, changeObj);
            try {
                track.setLength(originalLength, changeObj);
            } catch (InvalidMidiDataException e) {
                // should not occur
                e.printStackTrace();
            }
            if (midiDescriptor != null) {
                changed = changed || !midiDescriptor.isChanged();
                midiDescriptor.setChanged(changed);
            }
        }
    }
    /* (non-Javadoc)
     * @see javax.swing.undo.UndoableEdit#getRedoPresentationName()
     */
    public String getRedoPresentationName() {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.redo") + " " + getPresentationName();
    }
    /* (non-Javadoc)
     * @see javax.swing.undo.UndoableEdit#getUndoPresentationName()
     */
    public String getUndoPresentationName() {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.undo") + " " + getPresentationName();
    }
    
    public String getPresentationName() {
        if (presentationName != null) { return presentationName; }
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.recordEdit");
    }

    /**
     * This method does basically nothing but set the <code>MidiDescriptor</code>s
     * <code>changed</code> state, since during a record operation, all MIDI events
     * are added to the according MIDI tracks in real-time (which means, they have
     * already been added when this method is called).
     */
    public void perform() {
        if (midiDescriptor != null) {
            midiDescriptor.setChanged(true);
        }
    }

}
