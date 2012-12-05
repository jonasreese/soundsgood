/*
 * Created on 28.11.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi.edit;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.midi.TrackProxy;

/**
 * <p>
 * An <code>UndoableEdit</code> implementation that allows to undo
 * a change operation of of one or more MIDI events on a MIDI track.
 * Please perform changes to the <code>MidiEvent</code> objects passed
 * to the constructor <b>after</b> the constructor call, but <code>before</code>
 * the <code>perform()</code> method call. Please do not forget to call
 * <code>perform()</code>! <code>ChangeEventsEdit</code> will automatically
 * remove and re-add events whose tick values have changed. This is required
 * by the JAVA MIDI API.
 * </p>
 * @author jreese
 */
public class ChangeEventsEdit extends SgUndoableEdit {

    private static final long serialVersionUID = 1;
    
    private boolean changed;
    private MidiDescriptor midiDescriptor;
    private String presentationName;

    // the original messages (copied)
    protected TrackProxy track;
    protected MidiEvent[] originalEvents;
    protected MidiEvent[] events;
    protected Object changeObj;

    /**
     * Constructs a new <code>ChangeEventsEdit</code>.
     * @param track The <code>TrackProxy</code> that contains the given MIDI events.
     * @param events An array of the events that shall be
     *        changed (state <b>before</b> change).
     *        Shall not be <code>null</code>.
     * @param midiDescriptor The <code>MidiDescriptor</code> containing the
     *        MIDI sequence that contains the given MIDI events. If the events
     *        passed to this constructor are not assigned to a <code>MidiDescriptor</code>,
     *        <code>midiDescriptor</code> may be <code>null</code>.
     * @param presentationName An alternate presentation name. If set to <code>null</code>,
     *        the default undo presentation name is used.
     * @param changeObj The <code>Object</code> that changes the events.
     */
    public ChangeEventsEdit(
            TrackProxy track,
            MidiEvent[] events,
            MidiDescriptor midiDescriptor,
            String presentationName,
            Object changeObj ) {
        this.track = track;
        // keep state (copy events)
        this.events = events;
        originalEvents = copyEvents( events );
        this.changeObj = changeObj;
        this.midiDescriptor = midiDescriptor;
        changed = (midiDescriptor != null ? midiDescriptor.isChanged() : true);
        this.presentationName = presentationName;
    }
    
    /**
     * Gets the events that are being manipulated.
     * @return The events, as a <code>MidiEvent</code> array.
     */
    public MidiEvent[] getEvents() {
        return events;
    }
    
    /**
     * Copies the given events and returns them.
     * @param events The events to be copied.
     * @return A deep copy of the given events.
     */
    protected MidiEvent[] copyEvents( MidiEvent[] events ) {
        MidiEvent[] result = new MidiEvent[events.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new MidiEvent(
                (MidiMessage) events[i].getMessage().clone(), events[i].getTick() );
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.undo.UndoableEdit#die()
     */
    public void die() {
        super.die();
        originalEvents = null;
        events = null;
    }
    /**
     * Performs the redo.
     */
    public void redo() throws CannotRedoException {
        super.redo();
        swap();
        if (midiDescriptor != null) {
            midiDescriptor.setChanged( true );
        }
    }
    /**
     * Performs the undo. Overwrite this method with a call to
     * <code>super.undo()</code> in order to add task-specific
     * undo operations (like updating the UI, ...)
     */
    public void undo() throws CannotUndoException {
        super.undo();
        swap();
        if (midiDescriptor != null) {
            changed = changed || !midiDescriptor.isChanged();
            midiDescriptor.setChanged( changed );
        }
    }
    /* (non-Javadoc)
     * @see javax.swing.undo.UndoableEdit#getRedoPresentationName()
     */
    public String getRedoPresentationName() {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.redo" ) + " " + getPresentationName();
    }
    /* (non-Javadoc)
     * @see javax.swing.undo.UndoableEdit#getUndoPresentationName()
     */
    public String getUndoPresentationName() {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.undo" ) + " " + getPresentationName();
    }
    
    public String getPresentationName() {
        if (presentationName != null) { return presentationName; }
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.changeEventsEdit" );
    }
    
    /**
     * Swaps the copy with the original. This method can be used for undo AND
     * for redo.
     */
    protected void swap() {
        MidiEvent[] result = copyEvents( events ); // for next swap
        
        // all events whose tick has changed have to be removed
        // from the track and later re-added
        track.removeAll( events, changeObj );
        for (int i = 0; i < events.length; i++) {
            events[i].setTick( originalEvents[i].getTick() );
            if (events[i].getMessage() instanceof ShortMessage &&
                originalEvents[i].getMessage() instanceof ShortMessage) {
                ShortMessage msg = (ShortMessage) events[i].getMessage();
                ShortMessage origMsg = (ShortMessage) originalEvents[i].getMessage();
                
                try {
                    if (MidiToolkit.isChannelMessage( origMsg )) {
                        msg.setMessage(
                                origMsg.getCommand(),
                                origMsg.getChannel(),
                                origMsg.getData1(),
                                origMsg.getData2() );
                    } else {
                        msg.setMessage( origMsg.getStatus(), origMsg.getData1(), origMsg.getData2() );
                    }
                } catch (InvalidMidiDataException imdex) {
                    imdex.printStackTrace();
                    throw new CannotUndoException();
                }
            } else {
                throw new UnsupportedOperationException(
                    "MIDI messages other than ShortMessage cannot be changed currently" );
            }
        }
        // re-add events whose tick had changed
        track.addAll( events, changeObj );
        
        originalEvents = result; // for next swap
    }
    
    /**
     * This method does nothing. You can overwrite it in order to
     * manipulate the original <code>events</code> array.
     */
    public void perform() {
        track.removeAll( events, changeObj );
        track.addAll( events, changeObj );
        track.fireEventsChanged( events, changeObj );
        if (midiDescriptor != null) {
            midiDescriptor.setChanged( true );
        }
    }
}