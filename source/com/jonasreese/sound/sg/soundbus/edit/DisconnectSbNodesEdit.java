/*
 * Created on 17.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.edit;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.SoundbusException;

/**
 * <p>
 * This undoable edit is used to disconnect two <code>SbNode</code>s.
 * </p>
 * <p>
 * This edit implementation tries to avoid <code>IllegalStateException</code>s
 * thrown due to an opened <code>Soundbus</code> by closing the soundbus if
 * it is open.
 * </p>
 * @author jonas.reese
 */
public class DisconnectSbNodesEdit extends SgUndoableEdit {

    private static final long serialVersionUID = 1L;

    private boolean changed;
    private SoundbusDescriptor soundbusDescriptor;
    private SbInput input;
    private SbOutput output;
    
    /**
     * Constructs a new <code>DisconnectSbNodesEdit</code>.
     * @param soundbusDescriptor The <code>SoundbusDescriptor</code> containing
     * the <code>Soundbus</code> this edit is performed on.
     * @param input The input to be disconnected from the output.
     * @param output The output to be disconnected from the input.
     */
    public DisconnectSbNodesEdit(
            SoundbusDescriptor soundbusDescriptor, SbInput input, SbOutput output ) {
        this.soundbusDescriptor = soundbusDescriptor;
        this.input = input;
        this.output = output;
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
            "edit.disconnectSbNodes" );
    }
    
    public void undo() {
        super.undo();
        changed = changed || !soundbusDescriptor.isChanged();
        Soundbus soundbus = input.getSbNode().getSoundbus();
        if (soundbus.isOpen()) {
            try {
                soundbus.close();
            } catch (SoundbusException e) {
                e.printStackTrace();
            }
        }
        try {
            input.connect( output );
            output.connect( input );
        } catch (CannotConnectException e) {
            e.printStackTrace();
            input.disconnect();
            output.disconnect();
        }
        soundbusDescriptor.setChanged( changed );
    }
    
    public void redo() {
        super.redo();
        perform();
    }

    @Override
    public void perform() {
        changed = (soundbusDescriptor != null ? soundbusDescriptor.isChanged() : true);
        Soundbus soundbus = input.getSbNode().getSoundbus();
        if (soundbus.isOpen()) {
            try {
                soundbus.close();
            } catch (SoundbusException e) {
                e.printStackTrace();
            }
        }
        input.disconnect();
        output.disconnect();
    }
}
