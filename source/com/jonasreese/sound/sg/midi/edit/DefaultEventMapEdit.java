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
import com.jonasreese.sound.sg.midi.EventDescriptor;
import com.jonasreese.sound.sg.midi.EventMap;

/**
 * <p>
 * An <code>UndoableEdit</code> implementation that allows to undo
 * a "reset to default event map" action.
 * </p>
 * @author jreese
 */
public class DefaultEventMapEdit extends SgUndoableEdit {
    private static final long serialVersionUID = 1L;

    private EventDescriptor[] eventDescriptors;
    private EventMap eventMap;

    /**
     * Constructs a new <code>DefaultEventMapEdit</code>.
     * @param eventMap The <code>EventMap</code> to be edited.
     */
    public DefaultEventMapEdit( EventMap eventMap ) {
        this.eventMap = eventMap;
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
            "edit.defaultEventMapEdit" );
    }
    
    @Override
    public void redo() throws CannotUndoException {
        super.redo();
        perform();
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        eventMap.setEventDescriptors( eventDescriptors );
        eventDescriptors = null;
    }
    
    @Override
    public void perform() {
        eventDescriptors = eventMap.getEventDescriptors();
        eventMap.resetToDefault();
    }
}
