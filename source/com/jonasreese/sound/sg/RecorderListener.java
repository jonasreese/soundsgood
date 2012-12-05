/*
 * Created on 09.04.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg;

import java.util.EventListener;

/**
 * <p>
 * This interface shall be implemented by classes that wish to receive
 * update events from a <code>MidiRecorder</code>.
 * </p>
 * @author jonas.reese
 */
public interface RecorderListener extends EventListener {
    /**
     * Invoked when a <code>MidiRecorder</code> has started to operate.
     * @param e The event.
     */
    public void recorderStarted( RecorderEvent e );
    
    /**
     * Invoked when a <code>MidiRecorder</code> has ceased operating.
     * @param e The event.
     */
    public void recorderStopped( RecorderEvent e );
    
    /**
     * Invoked when a <code>MidiRecorder</code>'s <code>Metronome</code> has
     * been started.
     * @param e The event.
     */
    public void metronomeStarted( RecorderEvent e );

    /**
     * Invoked when a <code>MidiRecorder</code>'s <code>Metronome</code> has
     * been stopped.
     * @param e The event.
     */
    public void metronomeStopped( RecorderEvent e );
}
