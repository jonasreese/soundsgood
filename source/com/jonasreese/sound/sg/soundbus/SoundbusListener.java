/*
 * Created on 11.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import java.util.EventListener;

/**
 * <p>
 * This listener interface shall be implemented by classes that wish to
 * receive update events from a <code>Soundbus</code>.
 * </p>
 * @author jonas.reese
 */
public interface SoundbusListener extends EventListener {

    /**
     * Invoked when an <code>SbNode</code> has been added to the
     * soundbus.
     * @param e The <code>SoundbusEvent</code>.
     */
    public void nodeAdded( SoundbusEvent e );
    
    /**
     * Invoked when an <code>SbNode</code> has been removed from
     * the soundbus.
     * @param e The <code>SoundbusEvent</code>.
     */
    public void nodeRemoved( SoundbusEvent e );

    /**
     * Invoked when a <code>Soundbus</code> has been opened.
     * @param e The <code>SoundbusEvent</code>.
     */
    public void soundbusOpened( SoundbusEvent e );

    /**
     * Invoked when a <code>Soundbus</code> has been closed.
     * @param e The <code>SoundbusEvent</code>.
     */
    public void soundbusClosed( SoundbusEvent e );
    
    /**
     * Invoked when a <code>Soundbus</code> has been muted/unmuted.
     * @param e The <code>SoundbusEvent</code>.
     */
    public void muteStatusChanged( SoundbusEvent e );
    
    /**
     * Invoked when the tempo for a <code>Soundbus</code> has changed.
     * @param e The <code>SoundbusEvent</code>.
     */
    public void tempoChanged( SoundbusEvent e );

    /**
     * Invoked when two <code>SbNode</code>s have been connected to
     * each oter.
     * @param e The <code>SoundbusNodesConnectedEvent</code> containing
     * all required event-specific information.
     */
    public void nodesConnected( SoundbusNodesConnectionEvent e );
    
    /**
     * Invoked when two <code>SbNode</code>s have been disconnected from
     * each oter.
     * @param e The <code>SoundbusNodesConnectedEvent</code> containing
     * all required event-specific information.
     */
    public void nodesDisconnected( SoundbusNodesConnectionEvent e );
}
