/*
 * Created on 03.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import java.util.EventListener;

/**
 * <p>
 * This interface shall be implemented by classes that wish to receive connection
 * events (that means, the fact that two soundbus nodes have been connected or disconnected).
 * </p>
 * @author jonas.reese
 */
public interface ConnectionListener extends EventListener {
    /**
     * Called when an output has been connected to an input.
     * @param e The <code>ConnectionEvent</code>.
     */
    public void connected( ConnectionEvent e );
    /**
     * Called when an output has been disconnected from an input.
     * @param e The <code>ConnectionEvent</code>.
     */
    public void disconnected( ConnectionEvent e );
}