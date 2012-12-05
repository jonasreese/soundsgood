/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 18.09.2003
 */
package com.jonasreese.sound.sg;

import java.util.EventListener;

/**
 * <b>
 * Must be implemented by classes that want to keep track of session events.
 * </b>
 * @author jreese
 */
public interface SessionListener extends EventListener
{
    /**
     * Invoked when a <code>Session</code> has been added to the workspace.
     * @param e The <code>SessionEvent</code> containing further information.
     */
    public void sessionAdded( SessionEvent e );

    /**
     * Invoked when a <code>Session</code> has been removed from the workspace.
     * @param e The <code>SessionEvent</code> containing further information.
     */
    public void sessionRemoved( SessionEvent e );
    
    /**
     * Invoked when a <code>Session</code> has been activated.
     * @param e The <code>SessionEvent</code> containing further information.
     */
    public void sessionActivated( SessionEvent e );

    /**
     * Invoked when a <code>Session</code> has been deactivated.
     * @param e The <code>SessionEvent</code> containing further information.
     */
    public void sessionDeactivated( SessionEvent e );
}
