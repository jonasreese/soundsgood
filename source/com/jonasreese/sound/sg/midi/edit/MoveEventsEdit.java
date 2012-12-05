/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 27.11.2003
 */
package com.jonasreese.sound.sg.midi.edit;

import javax.sound.midi.MidiEvent;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.TrackProxy;

/**
 * <b>
 * An <code>UndoableEdit</code> implementation that allows to undo
 * a move operation of MIDI events on a MIDI track along a certain amount
 * of time and note offset. Please note that in most cases, this class has
 * to be overwritten in order to provide special additional behaviour, like
 * UI update or updates with other dependencies. Overwritten methods should
 * always call their super implementations.
 * </b>
 * @author jreese
 */
public class MoveEventsEdit extends ChangeEventsEdit {

    private static final long serialVersionUID = 1;

    public MoveEventsEdit(
            TrackProxy track,
            MidiEvent[] events,
            MidiDescriptor midiDescriptor,
            Object changeObj) {
        super( track, events, midiDescriptor, null, changeObj );
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
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.moveEventsEdit" );
    }
    
    /**
     * Aborts the moving of events and restores the original state.
     */
    public void abort() {
        swap();
    }
    
    
//    private boolean changed;
//    private MidiDescriptor midiDescriptor;
//    private boolean moveOnPerform;
//
//    protected MidiEvent[] events;
//    protected TrackProxy track;
//    protected long ticks;
//    protected short[] noteOffsets;
//    
//    protected Object changeObj;
//    
//    /**
//     * Constructs a new <code>DeleteEventsEdit</code>.
//     * @param track The track on which to move the events.
//     * @param events An array containing the events to move. Shall not be <code>null</code>.
//     * @param ticks The amount of ticks to move. Use a negative value for left, a positive value
//     *        for right move.
//     * @param noteOffset The amount of notes to move up/down. Use a negative value for up move.
//     * @param midiDescriptor The <code>MidiDescriptor</code> containing the
//     *        MIDI sequence that contains the given MIDI events. If the events
//     *        passed to this constructor are not assigned to a <code>MidiDescriptor</code>,
//     *        <code>midiDescriptor</code> may be <code>null</code>.
//     * @param moveOnPerform If set to <code>true</code>, indicates that a call to the
//     *        <code>perform()</code> method shall move all events. If <code>false</code>,
//     *        a call to <code>perform()</code> will do nothing. You can use <code>false</code>
//     *        if you performed the moving before and use this edit only for undo.
//     * @param changeObj The <code>Object</code> that adds the events.
//     *        Used to identify the source to a change on a MIDI track.
//     */
//    public MoveEventsEdit(
//        TrackProxy track,
//        MidiEvent[] events,
//        long ticks,
//        short[] noteOffsets,
//        MidiDescriptor midiDescriptor,
//        boolean moveOnPerform,
//        Object changeObj )
//    {
//        this.track = track;
//        this.events = events;
//        this.ticks = ticks;
//        this.noteOffsets = noteOffsets;
//        this.changeObj = changeObj;
//        this.midiDescriptor = midiDescriptor;
//        this.moveOnPerform = moveOnPerform;
//        changed = (midiDescriptor != null ? midiDescriptor.isChanged() : true);
//    }
//    
//    /* (non-Javadoc)
//     * @see javax.swing.undo.UndoableEdit#die()
//     */
//    public void die()
//    {
//        super.die();
//        events = null;
//    }
//    /**
//     * Checks if this edit has been undone and calls
//     * <code>perform()</code>.
//     */
//    public void redo() throws CannotRedoException
//    {
//        super.redo();
//        performImpl();
//    }
//    /**
//     * Performs the undo. Overwrite this method with a call to
//     * <code>super.undo()</code> in order to add task-specific
//     * undo operations (like updating the UI, ...)
//     */
//    public void undo() throws CannotUndoException
//    {
//        super.undo();
//
//        MidiEvent lastModifiedEvent = null;
//        track.removeAll( events, changeObj );
//        for (int i = 0; i < events.length; i++)
//        {
//            if (i == 0 ||
//                events[i] != lastModifiedEvent)
//            {
//                events[i].setTick( events[i].getTick() - ticks );
//                int noteOffset = 0;
//                if (noteOffsets != null)
//                {
//                    if (noteOffsets.length > i)
//                    {
//                        noteOffset = noteOffsets[i];
//                    }
//                    else if (noteOffsets.length > 0)
//                    {
//                        noteOffset = noteOffsets[0];
//                    }
//                }
//                if (noteOffset != 0 && events[i].getMessage() instanceof ShortMessage)
//                {
//                    ShortMessage msg = (ShortMessage) events[i].getMessage();
//                    try
//                    {
//                        //System.out.println( "message length: " + msg.getLength() );
//                        int command = msg.getCommand();
//                        int channel = msg.getChannel();
//                        int data1 = msg.getData1();
//                        int data2 = msg.getData2();
//                        msg.setMessage( command, channel, data1 - noteOffset, data2 );
//                        //System.out.println( "message length: " + msg.getLength() );
//                    }
//                    catch (Exception ex)
//                    {
//                        ex.printStackTrace();
//                    }
//                }
//                lastModifiedEvent = events[i];
//            }
//        }
//        track.addAll( events, changeObj );
//        if (midiDescriptor != null)
//        {
//            changed = changed || !midiDescriptor.isChanged();
//            midiDescriptor.setChanged( changed );
//        }
//    }
//    
//    private void performImpl()
//    {
//        MidiEvent lastModifiedEvent = null;
//        track.removeAll( events, changeObj );
//        for (int i = 0; i < events.length; i++)
//        {
//            if (i == 0 ||
//                events[i] != lastModifiedEvent)
//            {
//                events[i].setTick( events[i].getTick() + ticks );
//                int noteOffset = 0;
//                if (noteOffsets != null)
//                {
//                    if (noteOffsets.length > i)
//                    {
//                        noteOffset = noteOffsets[i];
//                    }
//                    else if (noteOffsets.length > 0)
//                    {
//                        noteOffset = noteOffsets[0];
//                    }
//                }
//                if (noteOffset != 0 && events[i].getMessage() instanceof ShortMessage)
//                {
//                    ShortMessage msg = (ShortMessage) events[i].getMessage();
//                    try
//                    {
//                        int status = msg.getStatus();
//                        int data1 = msg.getData1();
//                        int data2 = msg.getData2();
//                        msg.setMessage( status, data1 + noteOffset, data2 );
//                    }
//                    catch (Exception ex)
//                    {
//                        ex.printStackTrace();
//                    }
//                }
//                lastModifiedEvent = events[i];
//            }
//        }
//        track.addAll( events, changeObj );
//        if (midiDescriptor != null)
//        {
//            midiDescriptor.setChanged( true );
//        }
//    }
//    
//    /**
//     * Performs this <code>UndoableEdit</code>, so it can be undone afterwards.
//     * Overwrite this method with a call to
//     * <code>super.perform()</code> in order to add task-specific
//     * edit perform and redo operations (like updating the UI, ...)
//     */
//    public void perform() {
//        if (moveOnPerform) {
//            performImpl();
//        } else {
//            if (midiDescriptor != null) {
//                midiDescriptor.setChanged( true );
//            }
//        }
//    }
}
