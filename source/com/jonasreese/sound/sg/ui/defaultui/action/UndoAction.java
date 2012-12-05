/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 25.11.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.undo.UndoManager;

import com.jonasreese.sound.sg.ObjectSelectionChangeListener;
import com.jonasreese.sound.sg.ObjectSelectionChangedEvent;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SessionEvent;
import com.jonasreese.sound.sg.SessionListener;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.UndoableEditUpdateEvent;
import com.jonasreese.sound.sg.edit.UndoableEditUpdateListener;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;

/**
 * <b>
 * This class is a helper class in order to realize undo/redo actions.
 * It manages the automatical update of <code>AbstractButton</code>s when
 * the <code>UndoManager</code> changes it's state.
 * </b>
 * @author jreese
 */
public class UndoAction extends SgAction
    implements SessionListener, ObjectSelectionChangeListener, UndoableEditUpdateListener {
    
    private static final long serialVersionUID = 1;
    
    private boolean redo;
    private List<AbstractButton> buttons;
    private Session session;
    private SessionElementDescriptor currentSessionElement;
    
    private static Map<Session,UndoAction[]> sessionMap = new HashMap<Session,UndoAction[]>();
    
    private UndoAction( boolean redo, Session session ) {
        this.redo = redo;
        this.session = session;
        buttons = new ArrayList<AbstractButton>();
        SgEngine.getInstance().addSessionListener( this );

        Object[] selObjs = session.getSelectedElements();
        if (selObjs == null ||
            selObjs.length == 0 ||
            !(selObjs[0] instanceof SessionElementDescriptor)) {
            currentSessionElement = null;
        } else {
            currentSessionElement = ((SessionElementDescriptor) selObjs[0]);
            currentSessionElement.addUndoableEditUpdateListener( this );
        }
        session.addObjectSelectionChangeListener( this );
    }
    
    /**
     * Gets an <code>UndoAction</code> for undoing.
     * @param icon The icon to be set.
     * @param session The parent <code>Session</code>.
     * @return The undo instance.
     */
    public static UndoAction getUndoInstance( Icon icon, Session session ) {
        UndoAction[] actions = null;
        synchronized (sessionMap) {
            actions = sessionMap.get( session );
        }
        if (actions == null) {
            actions = new UndoAction[2];
            synchronized (sessionMap) {
                sessionMap.put( session, actions );
            }
        }
        if (actions[0] == null) { actions[0] = new UndoAction( false, session ); }
        actions[0].buttons.clear();
        actions[0].putValue( SMALL_ICON, icon );
        actions[0].putValue(
            ACCELERATOR_KEY, UiToolkit.getKeyStroke( "edit.undo.acceleratorKey" ) );
        
        return actions[0];
    }
    
    /**
     * Gets an <code>UndoAction</code> for redoing.
     * @param icon The icon to be set.
     * @param session The parent <code>Session</code>.
     * @return The redo instance.
     */
    public static UndoAction getRedoInstance( Icon icon, Session session ) {
        UndoAction[] actions = null;
        synchronized (sessionMap) {
            actions = sessionMap.get( session );
        }
        if (actions == null) {
            actions = new UndoAction[2];
            synchronized (sessionMap) {
                sessionMap.put( session, actions );
            }
        }
        if (actions[1] == null) { actions[1] = new UndoAction( true, session ); }
        actions[1].buttons.clear();
        actions[1].putValue( SMALL_ICON, icon );
        actions[1].putValue(
            ACCELERATOR_KEY, UiToolkit.getKeyStroke( "edit.redo.acceleratorKey" ) );
        
        return actions[1];
    }
    
    private UndoManager getUndoManager() {
        SessionElementDescriptor sed = currentSessionElement;
        if (sed == null) { return null; }
        return sed.getUndoManager();
    }
    
    public void actionPerformed( ActionEvent e ) {
        UndoManager undoManager = getUndoManager();
        UndoAction[] actions = null;
        synchronized (sessionMap) {
            actions = sessionMap.get( session );
        }
        if (undoManager == null) { return; }
        if (redo && undoManager.canRedo()) {
            undoManager.redo();
        }
        if (!redo && undoManager.canUndo()) {
            undoManager.undo();
        }
        if (actions[0] != null) {
            actions[0].undoableEditUpdate( null );
        }
        if (actions[1] != null) {
            actions[1].undoableEditUpdate( null );
        }
        super.actionPerformed( e );
    }
    public void putValue( String key, Object value ) {
        super.putValue( key, value );
    }
    public Object getValue( String key ) {
        if (NAME.equals( key )) {
            UndoManager undoManager = getUndoManager();
            if (redo) {
                if (undoManager != null && undoManager.canRedo()) {
                    return undoManager.getRedoPresentationName();
                }
                return SgEngine.getInstance().getResourceBundle().getString(
                    "edit.redo" ) + " " +
                    SgEngine.getInstance().getResourceBundle().getString( "edit.cannotRedo" );
            } else {
                if (key.equals( NAME )) {
                    if (undoManager != null && undoManager.canUndo()) {
                        return undoManager.getUndoPresentationName();
                    }
                return SgEngine.getInstance().getResourceBundle().getString(
                    "edit.undo" ) + " " +
                    SgEngine.getInstance().getResourceBundle().getString( "edit.cannotUndo" );
                }
                return super.getValue( key );
            }
        } else if (TOOL_TIP_TEXT.equals( key )) {
            return getValue( NAME );
        }
        return super.getValue( key );
    }
    public boolean isEnabled() {
        UndoManager undoManager = getUndoManager();
        if (redo) {
            return (undoManager != null && undoManager.canRedo());
        } else {
            return (undoManager != null && undoManager.canUndo());
        }
    }
    
    /**
     * Adds an <code>AbstractButton</code> to this <code>UndoAction</code>.
     * Use this method in order to set the button's state depending on
     * changes in the <code>UndoManager</code>.
     * @param b The button to add.
     */
    public void addButton( AbstractButton b ) {
        synchronized (buttons) {
            buttons.add( b );
        }
    }

    /**
     * Removes an <code>AbstractButton</code> from this <code>UndoAction</code>.
     * Please call this method at some point of time when the method
     * <code>addButton(AbstractButton)</code> has been called.
     * @param b The button to add.
     */
    public void removeButton( AbstractButton b ) {
        synchronized (buttons) {
            buttons.remove( b );
        }
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.UndoableEditUpdateListener#undoableEditUpdate(com.jonasreese.sound.sg.UndoableEditUpdateEvent)
     */
    public void undoableEditUpdate( UndoableEditUpdateEvent e ) {
        //System.out.println( "undoableEditUpdate()" );
        synchronized (buttons) {
            for (int i = 0; i < buttons.size(); i++) {
                AbstractButton b = buttons.get( i );
                b.setEnabled( isEnabled() );
                b.setText( (String) getValue( NAME ) );
            }
        }
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.SessionListener#sessionAdded(com.jonasreese.sound.sg.SessionEvent)
     */
    public void sessionAdded( SessionEvent e ) {
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.SessionListener#sessionRemoved(com.jonasreese.sound.sg.SessionEvent)
     */
    public void sessionRemoved( SessionEvent e ) {
        if (e.getSession() == session) {
            synchronized (sessionMap) {
                sessionMap.remove( session );
            }
            session.removeObjectSelectionChangeListener( this );
        }
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.SessionListener#sessionActivated(com.jonasreese.sound.sg.SessionEvent)
     */
    public void sessionActivated( SessionEvent e ) {
    }

    /* (non-Javadoc)
     * 
     */
    public void sessionDeactivated( SessionEvent e ) {
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.ObjectSelectionChangeListener#objectSelectionChanged(com.jonasreese.sound.sg.ObjectSelectionChangedEvent)
     */
    public void objectSelectionChanged( ObjectSelectionChangedEvent e ) {
        if (currentSessionElement != null) {
            currentSessionElement.removeUndoableEditUpdateListener( this );
        }

        Object[] selObjs = session.getSelectedElements();
        if (selObjs == null ||
            selObjs.length == 0 ||
            !(selObjs[0] instanceof SessionElementDescriptor)) {
            currentSessionElement = null;
        } else {
            currentSessionElement = ((SessionElementDescriptor) selObjs[0]);
            currentSessionElement.addUndoableEditUpdateListener( this );
        }
        undoableEditUpdate( null );
    }
}
