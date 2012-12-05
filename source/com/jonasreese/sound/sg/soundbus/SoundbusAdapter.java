/*
 * Created on 11.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;


/**
 * <p>
 * Convenience base class for partial <code>SoundbusListener</code> implementations.
 * </p>
 * @author jonas.reese
 */
public abstract class SoundbusAdapter implements SoundbusListener {

    public void nodeAdded(SoundbusEvent e) {}
    
    public void nodeRemoved(SoundbusEvent e) {}

    public void soundbusOpened(SoundbusEvent e) {}

    public void soundbusClosed(SoundbusEvent e) {}
    
    public void muteStatusChanged(SoundbusEvent e) {}
    
    public void tempoChanged(SoundbusEvent e) {}

    public void nodesConnected(SoundbusNodesConnectionEvent e) {}
    
    public void nodesDisconnected(SoundbusNodesConnectionEvent e) {}
}
