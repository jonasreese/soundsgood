/*
 * Created on 12.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.edit;

import java.util.Collections;
import java.util.List;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.SoundbusException;

/**
 * <p>
 * This class is the base class for all undoable edits that change the
 * inner state of an <code>SbNode</code>.
 * </p>
 * @author jonas.reese
 */
public abstract class SbNodeStateChangeEdit extends SgUndoableEdit {

    private static final long serialVersionUID = 1L;

    private boolean changed;
    private SoundbusDescriptor soundbusDescriptor;

    private List<SbNode> nodes;
    private String presentationName;
    private boolean closeSoundbusOnEdit;
    
    /**
     * Constructs a new <code>SbNodeStateChangeEdit</code> object.
     * @param soundbusDescriptor The <code>SoundbusDescriptor</code> containing
     * the <code>Soundbus</code> this edit is performed on.
     * @param node The node whose state is to be changed.
     * @param presentationName The presentation name, or <code>null</code> if
     * the default presentation name shall be used.
     * @param closeSoundbusOnEdit If this is <code>true</code>, the soundbus will be
     * closed before any edit is performed. This avoids <code>IllegalStateException</code>s
     * when trying to modify a <code>Soundbus</code> that is opened.
     */
    public SbNodeStateChangeEdit(
            SoundbusDescriptor soundbusDescriptor,
            SbNode node,
            String presentationName,
            boolean closeSoundbusOnEdit ) {
        this.soundbusDescriptor = soundbusDescriptor;
        this.presentationName = presentationName;
        this.nodes = Collections.singletonList( node );
        this.closeSoundbusOnEdit = closeSoundbusOnEdit;
    }
    
    /**
     * Constructs a new <code>SbNodeStateChangeEdit</code> object with a list of
     * <code>SbNode</code>s that have been changed.
     * @param soundbusDescriptor The <code>SoundbusDescriptor</code> containing
     * the <code>Soundbus</code> this edit is performed on.
     * @param nodes The nodes whose states are to be changed.
     * @param presentationName The presentation name, or <code>null</code> if
     * the default presentation name shall be used.
     */
    public SbNodeStateChangeEdit(
            SoundbusDescriptor soundbusDescriptor, List<SbNode> nodes, String presentationName ) {
        this.soundbusDescriptor = soundbusDescriptor;
        this.presentationName = presentationName;
        this.nodes = nodes;
    }
    
    /**
     * Gets the node that has been set by the constructor, or the first element from the list.
     * @return The <code>SbNode</code>, or <code>null</code> if an empty or <code>null</code>
     * list was passed to the constructor.
     */
    protected SbNode getNode() {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }
        return nodes.get( 0 );
    }
    
    /**
     * Gets the nodes list.
     * @return A <code>List</code> of nodes.
     */
    protected List<SbNode> getNodes() {
        return nodes;
    }
    
    /**
     * Implement this method to undo.
     */
    protected abstract void undoImpl();
    
    /**
     * Please implement <code>undoImpl()</code> method.
     */
    public final void undo() {
        super.undo();
        changed = changed || !soundbusDescriptor.isChanged();
        if (closeSoundbusOnEdit && nodes != null && !nodes.isEmpty()) {
            Soundbus soundbus = nodes.get( 0 ).getSoundbus();
            if (soundbus.isOpen()) {
                try {
                    soundbus.close();
                } catch (SoundbusException e) {
                    e.printStackTrace();
                }
            }
        }
        undoImpl();
        soundbusDescriptor.setChanged( changed );
    }
    
    /**
     * Implement this method to redo.
     */
    protected abstract void redoImpl();
    
    /**
     * Please implement <code>redoImpl()</code> method.
     */
    public final void redo() {
        changed = (soundbusDescriptor != null ? soundbusDescriptor.isChanged() : true);
        if (closeSoundbusOnEdit && nodes != null && !nodes.isEmpty()) {
            Soundbus soundbus = nodes.get( 0 ).getSoundbus();
            if (soundbus.isOpen()) {
                try {
                    soundbus.close();
                } catch (SoundbusException e) {
                    e.printStackTrace();
                }
            }
        }
        redoImpl();
        super.redo();
    }
    
    /**
     * Implement this method to perform edit for the first time.
     */
    protected abstract void performImpl();
    
    public final void perform() {
        changed = (soundbusDescriptor != null ? soundbusDescriptor.isChanged() : true);
        if (closeSoundbusOnEdit && nodes != null && !nodes.isEmpty()) {
            Soundbus soundbus = nodes.get( 0 ).getSoundbus();
            if (soundbus.isOpen()) {
                try {
                    soundbus.close();
                } catch (SoundbusException e) {
                    e.printStackTrace();
                }
            }
        }
        performImpl();
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
            "edit.changeSbNode" );
    }
}
