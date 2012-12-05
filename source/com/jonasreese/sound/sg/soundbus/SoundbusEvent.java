/*
 * Created on 11.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import java.util.EventObject;

/**
 * <p>
 * This event is generated when an update notification for a soundbus
 * is sent to the registered listeners.
 * </p>
 * @author jonas.reese
 */
public class SoundbusEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private SbNode node;

    /**
     * Constructs a new <code>SoundbusEvent</code>.
     * @param source The source <code>Soundbus</code>.
     * @param node The soundbus node that is affected by this
     * <code>SoundbusEvent</code>. May be <code>null</code>.
     */
    public SoundbusEvent( Soundbus source, SbNode node ) {
        super( source );
        this.node = node;
    }

    /**
     * Gets the soundbus that is the source to this event.
     * @return The <code>Soundbus</code>.
     */
    public Soundbus getSoundbus() {
        return (Soundbus) getSource();
    }
    
    /**
     * Gets the <code>SbNode</code> that is affected by this
     * event. 
     * @return The node. Might be <code>null</code>.
     */
    public SbNode getNode() {
        return node;
    }
}
