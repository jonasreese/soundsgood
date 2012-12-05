/*
 * Created on 25.12.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi.edit;

import javax.swing.undo.CannotUndoException;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.midi.EventDescriptor;
import com.jonasreese.sound.sg.midi.EventMap;

/**
 * <p>
 * An <code>UndoableEdit</code> implementation that allows to undo
 * an "insert event map" action (where one EventDescriptor inserted into
 * the mapping).
 * </p>
 * @author jreese
 */
public class InsertEventMapEdit extends SgUndoableEdit {
    private static final long serialVersionUID = 1L;

    private EventMap eventMap;
    private EventDescriptor eventDescriptor;
    private int index;

    /**
     * Constructs a new <code>InsertEventMapEdit</code>.
     * @param eventMap The <code>EventMap</code> to be edited.
     * @param index The index of the <code>EventDescriptor</code> to be removed
     * from the <code>EventMap</code>.
     */
    public InsertEventMapEdit( EventMap eventMap, EventDescriptor eventDescriptor, int index ) {
        this.eventMap = eventMap;
        this.eventDescriptor = eventDescriptor;
        this.index = index;
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
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.insertEventMapEdit" );
    }
    
    @Override
    public void redo() throws CannotUndoException {
        super.redo();
        perform();
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        eventMap.removeEventAt( index );
    }
    
    @Override
    public void perform() {
        eventMap.insertEventAt( index, eventDescriptor );
    }
}
