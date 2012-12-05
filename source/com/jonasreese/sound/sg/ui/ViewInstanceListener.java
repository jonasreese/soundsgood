/*
 * Created on 06.10.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui;

import java.util.EventListener;

/**
 * @author jreese
 */
public interface ViewInstanceListener extends EventListener
{
    /**
     * Notified when a <code>ViewInstance</code> has been added
     * (opened).
     * @param e The event.
     */
    public void viewInstanceAdded( ViewInstanceEvent e );

    /**
     * Notified when a <code>ViewInstance</code> has been removed
     * (closed).
     * @param e The event.
     */
    public void viewInstanceRemoved( ViewInstanceEvent e );

    /**
     * Notified when a <code>ViewInstance</code> has been activated
     * (focused).
     * @param e The event.
     */
    public void viewInstanceActivated( ViewInstanceEvent e );
}