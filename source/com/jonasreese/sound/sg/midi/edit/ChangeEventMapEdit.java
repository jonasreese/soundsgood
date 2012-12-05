/*
 * Created on 24.12.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi.edit;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.midi.EventDescriptor;
import com.jonasreese.sound.sg.midi.EventMap;

/**
 * <p>
 * An <code>UndoableEdit</code> implementation that allows to undo
 * a change operation event maps for a MIDI track.
 * </p>
 * @author jreese
 */
public class ChangeEventMapEdit extends SgUndoableEdit {
    private static final long serialVersionUID = 1L;
    
    private EventDescriptor[] eventDescriptors;
    private EventMap eventMap;
    private String presentationName;
    
    /**
     * Constructs a new <code>ChangeEventMapEdit</code>.
     * @param eventMap The <code>EventMap</code> to be edited.
     * @param eventDescriptors The <code>EventDescriptor</code>s the existing
     * ones will be replaced with.
     * @param presentationName An alternative human-readable presentation name for
     * this <code>SgUndoableEdit</code>. May be <code>null</code>.
     */
    public ChangeEventMapEdit(
            EventMap eventMap, EventDescriptor[] eventDescriptors, String presentationName ) {
        this.eventMap = eventMap;
        this.eventDescriptors = eventDescriptors;
        this.presentationName = presentationName;
    }
    
    /**
     * Constructs a new <code>ChangeEventMapEdit</code> with the default presentation name.
     * @param eventMap The <code>EventMap</code> to be edited.
     * @param eventDescriptors The <code>EventDescriptor</code>s the existing
     * ones will be replaced with.
     */
    public ChangeEventMapEdit(
            EventMap eventMap, EventDescriptor[] eventDescriptors ) {
        this( eventMap, eventDescriptors, null );
    }
    
    @Override
    public String getRedoPresentationName() {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.redo" ) + " " + getPresentationName();
    }
    
    @Override
    public String getUndoPresentationName() {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.undo" ) + " " + getPresentationName();
    }
    
    @Override
    public String getPresentationName() {
        if (presentationName != null) { return presentationName; }
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.changeEventMapEdit" );
    }
    
    @Override
    public void die() {
        super.die();
        eventDescriptors = null;
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        perform();
    }
    
    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        perform();
    }
    
    @Override
    public void perform() {
        EventDescriptor[] eventDescriptors = eventMap.getEventDescriptors();
        eventMap.setEventDescriptors( this.eventDescriptors );
        this.eventDescriptors = eventDescriptors;
    }
    
}
