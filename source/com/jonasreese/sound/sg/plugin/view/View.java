/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 19.09.2003
 */
package com.jonasreese.sound.sg.plugin.view;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.plugin.Plugin;

/**
 * <b>
 * This interface must be implemented by classes that are capable of
 * providing sessions (or parts of sessions) for display or modification.
 * </b>
 * @author jreese
 */
public interface View extends Plugin
{
    /**
     * Gets the <code>autostart</code> property for this <code>View</code>.
     * For an autostart view, a <code>ViewInstance</code> is automatically
     * created and displayed when a workspace has been created.
     * @return
     */
    public boolean isAutostartView();

    /**
     * Gets the <code>multipleInstancePerSessionAllowed</code> property for this
     * <code>View</code>.
     * @return <code>true</code> to indicate that multiple instances
     *         of this view's <code>ViewInstance</code> can be created
     *         within one <code>Session</code>, <code>false</code> if
     *         there should be only one visible instance of this
     *         <code>View</code> for a session.
     */
    public boolean isMultipleInstancePerSessionAllowed();
    
    /**
     * Gets the <code>multipleInstancePerSessionElementAllowed</code> property for this
     * <code>View</code>.
     * @return <code>true</code> to indicate that multiple instances
     *         of this view's <code>ViewInstance</code> can be created
     *         within one <code>SessionElement</code>, <code>false</code> if
     *         there should be only one visible instance of this
     *         <code>View</code> for a session.
     */
    public boolean isMultipleInstancePerSessionElementAllowed();
    
    /**
     * Gets the information if this <code>View</code> can
     * handle the given <code>SessionElementDescriptor</code> object.
     * @param sessionElement The object to be checked. If set to <code>null</code>,
     *        this has a special meaning. Returning <code>true</code>
     *        for a <code>null</code> data means that this <code>View</code>
     *        can always be activated with any kind of session element.
     * @return <code>true</code>, if <code>sessionElement</code> can be handled
     *         by this view, <code>false</code> otherwise.
     */
    public boolean canHandle( SessionElementDescriptor sessionElement );

    /**
     * Creates a new <code>ViewInstance</code> for this <code>View</code>.
     * @param session The <code>ViewInstance</code>s parent <code>Session</code>.
     * @param sessionElement The sessionElement to be viewed. If this
     *        <code>View</code> cannot handle the given data, this method shall
     *        throw an <code>IllegalArgumentException</code>.
     * @return A newly created <code>ViewInstance</code>.
     */    
    public ViewInstance createViewInstance(
        Session session, SessionElementDescriptor sessionElementDescriptor )
    throws ViewInstanceCreationFailedException;
}
