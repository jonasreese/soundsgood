/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 11.12.2003
 */
package com.jonasreese.sound.sg.midi;

import java.util.EventListener;

/**
 * <p>
 * This interface shall be implemented by classes that wish to receive
 * <code>MidiEventSelectionEvent</code>s.
 * </p>
 * @author jreese
 */
public interface MidiEventSelectionListener extends EventListener {
    
    /**
     * Called when the MIDI event selection has changed.
     * @param e The <code>MidiEventSelectionEvent</code> that has been fired.
     */
    public void midiEventSelectionUpdate( MidiEventSelectionEvent e );
}
