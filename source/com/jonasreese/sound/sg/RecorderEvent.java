/*
 * Created on 09.04.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg;

import java.util.EventObject;

/**
 * <p>
 * This event is dispatched when a <code>Recorder</code> changes
 * it's state.
 * </p>
 * @author jonas.reese
 */
public class RecorderEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new <code>MidiRecorderEvent</code>.
     * @param source The source <code>MidiRecorder</code>.
     */
    public RecorderEvent( Recorder source ) {
        super( source );
    }

    /**
     * Returns the same object as <code>getSource()</code>, but
     * performs a downcast to <code>MidiRecorder</code> before.
     */
    public Recorder getMidiRecorder() {
        return (Recorder) getSource();
    }
}
