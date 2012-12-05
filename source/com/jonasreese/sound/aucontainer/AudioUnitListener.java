/*
 * Created on 22.01.2008
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.aucontainer;

import java.util.EventListener;

/**
 * Event listener interface for audio units.
 * @author Jonas Reese
 */
public interface AudioUnitListener extends EventListener {
    

    /**
     * Called when one or more <code>AUEvent</code>s arrived at the AU host.
     * @param events The events that can be processed.
     * @param audioUnit The <code>VstPlugin</code> that sent those events.
     */
    public void process( AudioUnitEvent[] events, AudioUnit audioUnit );
}
