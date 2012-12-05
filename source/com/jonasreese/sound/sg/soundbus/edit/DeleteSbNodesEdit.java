/*
 * Created on 17.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.edit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.SAXException;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.IllegalSoundbusDescriptionException;
import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.SoundbusException;

/**
 * <p>
 * This undoable edit is used to delete an <code>SbNode</code>.
 * </p>
 * <p>
 * This edit implementation tries to avoid <code>IllegalStateException</code>s
 * thrown due to an opened <code>Soundbus</code> by closing the soundbus if
 * it is open.
 * </p>
 * @author jonas.reese
 */
public class DeleteSbNodesEdit extends SgUndoableEdit {
    private static final long serialVersionUID = 1L;

    private boolean changed;
    private SoundbusDescriptor soundbusDescriptor;
    private SbNode[] nodes;
    private Soundbus soundbus;
    private List<InOutConnection> connections;
    
    /**
     * Constructs a new <code>DeleteSbNodesEdit</code>.
     * @param soundbusDescriptor The <code>SoundbusDescriptor</code> containing
     * the <code>Soundbus</code> this edit is performed on.
     * @param nodes The nodes to be deleted.
     */
    public DeleteSbNodesEdit( SoundbusDescriptor soundbusDescriptor, SbNode[] nodes ) {
        this.soundbusDescriptor = soundbusDescriptor;
        this.nodes = nodes;
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
            "edit.deleteSbNodes" );
    }
    
    public void redo() {
        super.redo();
        perform();
    }
    
    public void undo() {
        super.undo();
        changed = changed || !soundbusDescriptor.isChanged();
        if (soundbus.isOpen()) {
            try {
                soundbus.close();
            } catch (SoundbusException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < nodes.length; i++) {
            soundbus.addNode( nodes[i] );
        }
        connect();
        soundbusDescriptor.setChanged( changed );
    }
    
    private void connect() {
        for (Iterator<InOutConnection> iter = connections.iterator(); iter.hasNext(); ) {
            InOutConnection conn = iter.next();
            try {
                conn.input.connect( conn.output );
                conn.output.connect( conn.input );
            } catch (CannotConnectException e) {
                conn.input.disconnect();
                conn.output.disconnect();
                e.printStackTrace();
            }
        }
    }
    
    private void disconnect() {
        for (Iterator<InOutConnection> iter = connections.iterator(); iter.hasNext(); ) {
            InOutConnection conn = iter.next();
            conn.input.disconnect();
            conn.output.disconnect();
        }
    }
    
    public void perform() {
        changed = (soundbusDescriptor != null ? soundbusDescriptor.isChanged() : true);
        try {
            soundbus = soundbusDescriptor.getSoundbus();
            if (soundbus.isOpen()) {
                try {
                    soundbus.close();
                } catch (SoundbusException e) {
                    e.printStackTrace();
                }
            }
            connections = new ArrayList<InOutConnection>();
            for (int i = 0; i < nodes.length; i++) {
                SbInput[] inputs = nodes[i].getInputs();
                for (int j = 0; j < inputs.length; j++) {
                    if (inputs[j].getConnectedOutput() != null) {
                        connections.add( new InOutConnection( inputs[j], inputs[j].getConnectedOutput() ) );
                    }
                }
                SbOutput[] outputs = nodes[i].getOutputs();
                for (int j = 0; j < outputs.length; j++) {
                    if (outputs[j].getConnectedInput() != null) {
                        connections.add( new InOutConnection( outputs[j].getConnectedInput(), outputs[j] ) );
                    }
                }
                disconnect();
                soundbus.removeNode( nodes[i] );
            }
        } catch (IllegalSoundbusDescriptionException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (SAXException e1) {
            e1.printStackTrace();
        }
    }
    
    static class InOutConnection {
        SbInput input;
        SbOutput output;
        InOutConnection( SbInput input, SbOutput output ) {
            this.input = input;
            this.output = output;
        }
    }
}
