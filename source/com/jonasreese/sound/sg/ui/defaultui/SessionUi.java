/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 25.09.2003
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

import javax.swing.BoundedRangeModel;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.plugin.view.ViewInstanceCreationFailedException;
import com.jonasreese.sound.sg.ui.ViewInstanceCreator;
import com.jonasreese.sound.sg.ui.ViewInstanceEvent;
import com.jonasreese.sound.sg.ui.ViewInstanceListener;
import com.jonasreese.sound.sg.ui.defaultui.action.ViewAction;
import com.jonasreese.util.AbstractEventRedirector;

/**
 * <b>
 * This is the base class for all classes that keep UI-specific information related to a
 * given <code>Session</code>. A <code>SessionUi</code> implementation is responsible
 * for how a <code>Session</code> is <i>displayed on the screen</i>.
 * </b>
 * @author jreese
 */
public abstract class SessionUi implements ViewInstanceCreator {
    private JMenuBar menuBar;
    private SgToolBar toolBar;
    private List<ViewInstanceListener> viListeners;
    private SessionActionPool actionPool;
    private List<String> restoringViewIds;

    /**
     * Constructs a new <code>SessionUi</code>.
     * @param desktop
     * @param menuBar
     * @param toolBar A <code>SgToolBar</code> representing the tool bar.
     * @param actionPool
     */
    public SessionUi(
        JMenuBar menuBar, SgToolBar toolBar, SessionActionPool actionPool ) {
        this.menuBar = menuBar;
        this.toolBar = toolBar;
        this.actionPool = actionPool;
        viListeners = Collections.synchronizedList( new ArrayList<ViewInstanceListener>() );
    }
    
    /**
     * Gets the <code>SessionActionPool</code> from this <code>SessionUi</code>.
     * @return The <code>SessionActionPool</code>.
     */
    public SessionActionPool getActionPool() {
        return actionPool;
    }
    
    /**
     * Gets the <code>Session</code> object that is associated with this <code>SessionUi</code>.
     * @return A non-<code>null</code> <code>Session</code> object.
     */
    public Session getSession() {
        return actionPool.getSession();
    }
    
    /**
     * Gets this <code>SessionUi</code>s <code>JMenuBar</code>.
     * @return a <code>JMenuBar</code>.
     */
    public JMenuBar getMenuBar() {
        return menuBar;
    }

    /**
     * Gets this <code>SessionUi</code>s <code>SgToolBar</code>.
     * @return a <code>SgToolBar</code>.
     */
    public SgToolBar getToolBar() {
        return toolBar;
    }
    
    /**
     * Gets the currently active <code>ViewInstance</code>.
     * @return The current active <code>ViewInstance</code>,
     *         or <code>null</code> if none is currently active.
     */
    public abstract ViewInstance getActiveViewInstance();

    /**
     * This method is invoked when this <code>SessionUi</code> is about to
     * be removed, e.g. when the parent session is removed or the application
     * is terminated.
     * @param session The <code>Session</code> this <code>SessionUi</code> is
     * removed from.
     */
    public void sessionUiRemoved( Session session ) {}
    
    /**
     * This method is invoked when this <code>SessionUi</code> is about to be
     * added, e.g. when a new <code>Session</code> has ben created
     * (new session, open session, ...)
     * @param session The <code>Session</code> this <code>SessionUi</code> will
     * be associated with.
     */
    public void sessionUiAdded( Session session ) {}
    
    /**
     * Adds a <code>ViewInstanceListener</code> to this <code>SessionUi</code>.
     * @param l The listener to add.
     */
    public void addViewInstanceListener( ViewInstanceListener l ) {
        viListeners.add( l );
    }
    
    /**
     * Removes a <code>ViewInstanceListener</code> from this <code>SessionUi</code>.
     * @param l The listener to remove.
     */
    public void removeViewInstanceListener( ViewInstanceListener l ) {
        viListeners.add( l );
    }
    
    /**
     * Creates a <code>ViewInstance</code>, using the primary selected data object
     * (which is a <code>SessionElementDescriptor</code>).
     * @param viewAction The <code>ViewAction</code> that has triggered
     *        this creation (the caller).
     * @return The created <code>ViewInstance</code>, or <code>null</code>
     *         if no <code>ViewInstance</code> could be created.
     * @throws ViewInstanceCreationFailedException if the <code>ViewInstance</code>
     *         could not be created because the corresponding <code>View</code>
     *         failed to create it.
     */
    public ViewInstance createViewInstance(
        ViewAction viewAction ) throws ViewInstanceCreationFailedException {
        //Session session = SgEngine.getInstance().getActiveSession();
        SessionElementDescriptor[] selectedObjects = getSession().getSelectedElements();
        SessionElementDescriptor d = null;
        if (selectedObjects == null ||
            selectedObjects.length == 0) {
            d = null;
        } else {
            d = selectedObjects[0];
        }
        return createViewInstance( viewAction, getSession(), d );
    }
    

    /**
     * Restores all view instances from the session's and all
     * SessionElement's <code>openViews</code> properties. Additionally, all autostart
     * views are created.
     * @param brm The <code>BoundedRangeModel</code> to be used
     * @param newSession Shall be <code>true</code> if this restores a brand new
     * session (meaning user clicked on "New Session").
     * @param swtichingView Shall be <code>true</code> if the session view is switched.
     * @param showErrorsEnabled Shall be <code>true</code> if errors shall be shown.
     */
    public void restoreViewInstances(
            BoundedRangeModel brm, boolean newSession, boolean switchingView, boolean showErrorsEnabled ) {
                //  execute views that were activated last time (if this option is enabled)

        final boolean _showErrorsEnabled = showErrorsEnabled;
        int initialVal = 0;
        if (brm != null) {
            initialVal = brm.getValue();
        }
        ViewAction[] vas = getActionPool().getViewActions();
        if (!newSession &&
            (SgEngine.getInstance().getProperties().getRestoreViewsFromSession() || switchingView)) {
            // first, we create all session-scoped views
            String[] sessionViewIds = getActionPool().getSession().getPersistentClientPropertyArray(
                "openViews", ";" );
            if (sessionViewIds != null) {
                for (int i = 0; i < sessionViewIds.length; i++) {
                    if (brm != null) { brm.setValue( initialVal + (i * 50 / sessionViewIds.length) ); }
                    // search for id (class name)
                    for (int j = 0; j < vas.length; j++) {
                        if (vas[j].getView().getClass().getName().equals( sessionViewIds[i] )) {
                            final ViewAction va = vas[j];
                            SwingUtilities.invokeLater( new Runnable() {
                                public void run() {
                                    va.actionPerformed( _showErrorsEnabled );
                                }
                            } );
                            break;
                        }
                    }
                }
            }
            if (brm != null) { brm.setValue( initialVal + 50 ); }
            // secondly, we create all sessionElement-scoped views
            SessionElementDescriptor[] seds = getActionPool().getSession().getAllElements();
            restoringViewIds = new ArrayList<String>();
            for (int l = 0; l < seds.length; l++) {
                String[] seViewIds = seds[l].getPersistentClientPropertyArray(
                    "openViews", ";" );
                for (int i = 0; i < sessionViewIds.length; i++) {
                }
                if (seViewIds != null) {
                    for (int i = 0; i < seViewIds.length; i++) {
                        if (brm != null) { brm.setValue( initialVal + 50 + (i * 50 / seViewIds.length) ); }
                        // search for id (class name)
                        for (int j = 0; j < vas.length; j++) {
                            if (vas[j].getView().getClass().getName().equals( seViewIds[i] )) {
                                restoringViewIds.add( seViewIds[i] );
                                System.out.println( "adding " + seViewIds[i] + " to restoringViewIds" );
                                final ViewAction va = vas[j];
                                final SessionElementDescriptor sed = seds[l];
                                SwingUtilities.invokeLater( new Runnable() {
                                    public void run() {
                                        va.actionPerformed( sed, _showErrorsEnabled );
                                    }
                                } );
                                break;
                            }
                        }
                    }
                }
            }
        }
        // execute autostart views
        else {
            for (int i = 0; i < vas.length; i++) {
                if (brm != null) { brm.setValue( initialVal + (i * 100 / vas.length) ); }
                if (vas[i].getView().isAutostartView()) {
                    vas[i].actionPerformed();
                }
            }
        }
    }

    /**
     * <p>
     * Convenience method that can be called when a <code>ViewInstance</code>
     * has been created by a <code>SessionUi</code> impelementation. It
     * registers the given <code>ViewInstance</code> to the given
     * <code>SessionElementDescriptor</code>s and adds the <code>ViewInstance</code>'s
     * view ID to the session property called <code>openViews</code> if the view allows
     * a single instance per session or to the <code>SessionElementDescriptor</code>'s
     * <code>openViews</code> property if the view allows multiple instances per session.
     * </p>
     * <p>
     * This method does <b>not</b> fire an event.
     * </p>
     * @param viewInstance The <code>ViewInstance</code>.
     * @param descriptors The <code>SessionElementDescriptor</code> associated with
     * <code>viewInstance</code>. May be <code>null</code> if <code>viewInstance</code> is
     * not directly associated with a <code>SessionElementDescriptor</code>.
     */
    protected void viewInstanceCreated( ViewInstance viewInstance, SessionElementDescriptor descriptor ) {
        if (descriptor != null) {
            if (!viewInstance.getView().canHandle( null )) {
                descriptor.registerViewInstance( viewInstance );
            }

            if (viewInstance.getView().isMultipleInstancePerSessionAllowed()) {
                String id = viewInstance.getView().getClass().getName();
                // store in session element
                if (restoringViewIds == null || !restoringViewIds.remove( id )) {
                    String[] values = descriptor.getPersistentClientPropertyArray(
                            "openViews", ";" );
                    String[] openViews = addToArray(
                        values,
                        id,
                        viewInstance.getView().isMultipleInstancePerSessionElementAllowed() );
                    if (values == null ||
                        values.length != openViews.length) {
                        descriptor.putPersistentClientPropertyArray(
                                "openViews", openViews, ";" );
                    }
                } else {
                    System.out.println( "restored " + id );
                }
            }
        }
        // store in session
        if (!viewInstance.getView().isMultipleInstancePerSessionAllowed()) {
            Session session = getActionPool().getSession();
            String[] values = session.getPersistentClientPropertyArray( "openViews", ";" );
            String[] openViews = addToArray(
                values,
                viewInstance.getView().getClass().getName(),
                false );
            if (values == null ||
                values.length != openViews.length) {
                session.putPersistentClientPropertyArray( "openViews", openViews, ";" );
            }
        }
    }
    
    public void viewInstanceRemoved( ViewInstance viewInstance ) {
        SessionElementDescriptor[] descriptors = getActionPool().getSession().getAllElements();
        boolean removedOne = false;
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].unregisterViewInstance( viewInstance )) {
                // remove from session element
                String[] values = descriptors[i].getPersistentClientPropertyArray(
                        "openViews", ";" );
                descriptors[i].putPersistentClientPropertyArray(
                    "openViews",
                    removeFromArray( values,  viewInstance.getView().getClass().getName() ), ";" );
                descriptors[i].unregisterViewInstance( viewInstance );
                removedOne = true;
            }
        }

        // it's not possible that the view is stored in a session element AND in the session
        if (!removedOne) {
            Session session = getActionPool().getSession();
            String[] values = session.getPersistentClientPropertyArray( "openViews", ";" );
            session.putPersistentClientPropertyArray(
                "openViews",
                removeFromArray( values, viewInstance.getView().getClass().getName() ), ";" );
        }
    }
    
    // helper method
    private String[] addToArray( String[] array, String value, boolean allowMultiple ) {
        if (array == null) { return new String[] { value }; }
        if (!allowMultiple) {
            for (int i = 0; i < array.length; i++) {
                if (array[i].equals( value )) {
                    return array;
                }
            }
        }
        String[] result = new String[array.length + 1];
        System.arraycopy( array, 0, result, 0, array.length );
        result[array.length] = value;
        return result;
    }
    
    
    // helper method
    private String[] removeFromArray( String[] array, String value ) {
        if (array == null || array.length <= 1) { return new String[0]; }
        int index = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals( value )) {
                index = i;
            }
        }
        if (index < 0) { return array; }
        
        String[] result = new String[array.length - 1];
        
        int c = 0;
        for (int i = 0; i < array.length; i++) {
            if (i != index) {
                result[c++] = array[i];
            }
        }

        return result;
    }



    /**
     * Fires a <code>ViewInstanceEvent</code> to all registered listeners.
     * @param e The event to be fired.
     */
    protected void fireViewInstanceAddedEvent( ViewInstanceEvent e ) {
        for (int i = 0; i < viListeners.size(); i++) {
            ViewInstanceListener l = viListeners.get( i );
            SgEngine.getInstance().getEventQueue().addQueueEntry(
                new AbstractEventRedirector( l ) {
                    public void redirectEvent( EventObject e ) {
                        ((ViewInstanceListener) getListener()).viewInstanceAdded(
                            (ViewInstanceEvent) e );
                    }
                },  e );
        }
        SgEngine.getInstance().getEventQueue().processEvents();
    }
    
    /**
     * Fires a <code>ViewInstanceEvent</code> to all registered listeners.
     * @param e The event to be fired.
     */
    protected void fireViewInstanceRemovedEvent( ViewInstanceEvent e ) {
        for (int i = 0; i < viListeners.size(); i++) {
            ViewInstanceListener l = viListeners.get( i );
            SgEngine.getInstance().getEventQueue().addQueueEntry(
                new AbstractEventRedirector( l ) {
                    public void redirectEvent( EventObject e ) {
                        ((ViewInstanceListener) getListener()).viewInstanceRemoved(
                            (ViewInstanceEvent) e );
                    }
                },  e );
        }
        SgEngine.getInstance().getEventQueue().processEvents();
    }
    
    /**
     * Fires a <code>ViewInstanceEvent</code> to all registered listeners.
     * @param e The event to be fired.
     */
    protected void fireViewInstanceActivatedEvent( ViewInstanceEvent e ) {
        for (int i = 0; i < viListeners.size(); i++) {
            ViewInstanceListener l = viListeners.get( i );
            SgEngine.getInstance().getEventQueue().addQueueEntry(
                new AbstractEventRedirector( l ) {
                    public void redirectEvent( EventObject e ) {
                        ((ViewInstanceListener) getListener()).viewInstanceActivated(
                            (ViewInstanceEvent) e );
                    }
                },  e );
        }
        SgEngine.getInstance().getEventQueue().processEvents();
    }
}
