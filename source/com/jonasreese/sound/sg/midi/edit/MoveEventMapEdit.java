/*
 * Created on 24.12.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi.edit;

import javax.swing.undo.CannotUndoException;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.midi.EventMap;

/**
 * <p>
 * An <code>UndoableEdit</code> implementation that allows to undo
 * a "move event map" action (where one EventDescriptor is moved from it's
 * current index to another).
 * </p>
 * @author jreese
 */
public class MoveEventMapEdit extends SgUndoableEdit {
    private static final long serialVersionUID = 1L;

    private EventMap eventMap;
    private int fromIndex;
    private int toIndex;

    /**
     * Constructs a new <code>MoveEventMapEdit</code>.
     * @param eventMap The <code>EventMap</code> to be edited.
     * @param fromIndex The from index.
     * @param toIndex The to index.
     */
    public MoveEventMapEdit( EventMap eventMap, int fromIndex, int toIndex ) {
        this.eventMap = eventMap;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
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
            "edit.moveEventMapEdit" );
    }
    
    @Override
    public void redo() throws CannotUndoException {
        super.redo();
        perform();
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        eventMap.moveEvent( toIndex, fromIndex );
    }
    
    @Override
    public void perform() {
        eventMap.moveEvent( fromIndex, toIndex );
    }
}
