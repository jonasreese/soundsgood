/*
 * Created on 21.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.edit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.soundbus.IllegalSoundbusDescriptionException;
import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.SoundbusException;
import com.jonasreese.sound.sg.soundbus.SoundbusToolkit;
import com.jonasreese.sound.sg.soundbus.datatransfer.SoundbusSerializer;

/**
 * <p>
 * This undoable edit is used to paste soundbus elements from the clipboard
 * </p>
 * 
 * @author jonas.reese
 */
public class PasteSoundbusElementsEdit extends SgUndoableEdit {
    private static final long serialVersionUID = 1L;

    private boolean changed;
    private SoundbusDescriptor soundbusDescriptor;
    private SoundbusSerializer serializer;
    private Soundbus soundbus;
    private List<SbNode> nodes;
    
    /**
     * Constructs a new <code>PasteSoundbusElementsEdit</code>.
     * @param soundbusDescriptor The <code>SoundbusDescriptor</code> containing
     * the <code>Soundbus</code> this edit is performed on.
     * @param serializer The <code>SoundbusSerializer</code> from the clipboard.
     */
    public PasteSoundbusElementsEdit( SoundbusDescriptor soundbusDescriptor, SoundbusSerializer serializer ) {
        this.soundbusDescriptor = soundbusDescriptor;
        this.serializer = serializer;
        nodes = null;
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
            "edit.pasteSbElements" );
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
        if (nodes != null) {
            for (SbNode node : nodes) {
                soundbus.removeNode( node );
            }
        }
        soundbusDescriptor.setChanged( changed );
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
            ByteArrayInputStream in = new ByteArrayInputStream( serializer.getSoundbusDescription().getBytes() );
            nodes = new ArrayList<SbNode>();
            SoundbusToolkit.deserializeSoundbusElements( in, soundbus, nodes );
        } catch (IllegalSoundbusDescriptionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
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
