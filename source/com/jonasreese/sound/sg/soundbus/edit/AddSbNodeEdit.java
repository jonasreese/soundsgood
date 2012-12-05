/*
 * Created on 11.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.edit;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.SoundbusException;

/**
 * <p>
 * An <code>UndoableEdit</code> implementation that allows to undo
 * an add operation of one Soundbus Node to a Soundbus.
 * </p>
 * <p>
 * Please note that in most cases, this class has to be overwritten
 * in order to provide special additional behaviour, like UI update
 * or updates with other dependencies. Overwritten methods should
 * always call their super implementations.
 * </p>
 * <p>
 * This edit implementation tries to avoid <code>IllegalStateException</code>s
 * thrown due to an opened <code>Soundbus</code> by closing the soundbus if
 * it is open.
 * </p>
 * @author jonas.reese
 */
public abstract class AddSbNodeEdit extends SgUndoableEdit {

    private static final long serialVersionUID = 1L;

    private boolean changed;
    protected SoundbusDescriptor soundbusDescriptor;
    private String presentationName;
    private SbNode node;
    private Soundbus soundbus;

    /**
     * Constructs a new <code>AddSbNodeEdit</code> object.
     * @param soundbusDescriptor The <code>SoundbusDescriptor</code> containing
     * the <code>Soundbus</code> this edit is performed on.
     * @param presentationName A custom presentation name for this undoable
     * edit, or <code>null</code> if the default shall be used.
     */
    public AddSbNodeEdit( SoundbusDescriptor soundbusDescriptor, String presentationName ) {
        this.soundbusDescriptor = soundbusDescriptor;
        this.presentationName = presentationName;
        changed = (soundbusDescriptor != null ? soundbusDescriptor.isChanged() : true);
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
            "edit.addSbNode" );
    }
    
    /**
     * Override this method to create an <code>SbNode</code> of a certain type,
     * add it to the parent <code>Soundbus</code> and return it. This is the only
     * method that has to be implemented by subclasses.
     * @return A newly created <code>SbNode</code>.
     */
    protected abstract SbNode addNode();
    
    public void undo() {
        super.undo();
        changed = changed || !soundbusDescriptor.isChanged();
        soundbus = node.getSoundbus();
        if (soundbus.isOpen()) {
            try {
                soundbus.close();
            } catch (SoundbusException e) {
                e.printStackTrace();
            }
        }
        soundbus.removeNode( node );
        soundbusDescriptor.setChanged( changed );
    }
    
    public void redo() {
        super.redo();
        changed = (soundbusDescriptor != null ? soundbusDescriptor.isChanged() : true);
        if (soundbus.isOpen()) {
            try {
                soundbus.close();
            } catch (SoundbusException e) {
                e.printStackTrace();
            }
        }
        soundbus.addNode( node );
        soundbus = null;
    }
    
    public void die() {
        super.die();
        node = null;
        soundbus = null;
    }
    
    /**
     * It is not required to override this method. Just implement the
     * <code>createNode()</code> method.
     */
    public void perform() {
        changed = (soundbusDescriptor != null ? soundbusDescriptor.isChanged() : true);
        node = addNode();
    }
}
