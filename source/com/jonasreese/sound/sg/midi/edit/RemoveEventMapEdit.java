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
 * a "remove event map" action (where one EventDescriptor is removed moved from
 * the mapping.
 * </p>
 * @author jreese
 */
public class RemoveEventMapEdit extends SgUndoableEdit {
    private static final long serialVersionUID = 1L;

    private EventMap eventMap;
    private int index;
    private EventDescriptor eventDescriptor;

    /**
     * Constructs a new <code>RemoveEventMapEdit</code>.
     * @param eventMap The <code>EventMap</code> to be edited.
     * @param index The index of the <code>EventDescriptor</code> to be removed
     * from the <code>EventMap</code>.
     */
    public RemoveEventMapEdit( EventMap eventMap, int index ) {
        this.eventMap = eventMap;
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
            "edit.removeEventMapEdit" );
    }
    
    @Override
    public void redo() throws CannotUndoException {
        super.redo();
        perform();
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        eventMap.insertEventAt( index, eventDescriptor );
        eventDescriptor = null;
    }
    
    @Override
    public void perform() {
        eventDescriptor = eventMap.getEventAt( index );
        eventMap.removeEventAt( index );
    }
}
