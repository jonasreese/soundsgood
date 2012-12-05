/*
 * Created on 25.06.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import java.util.EventListener;

/**
 * <p>
 * This interface shall be implemented by classes that wish to receive
 * <code>TrackSelectionEvent</code>s.
 * </p>
 * @author jonas.reese
 */
public interface TrackSelectionListener extends EventListener {
    /**
     * Invoked when a track selection has changed.
     * @param e The associated <code>TrackSelectionEvent</code>.
     */
    public void trackSelectionChanged( TrackSelectionEvent e );
}