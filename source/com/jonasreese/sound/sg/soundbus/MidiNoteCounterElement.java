/*
 * Created on 02.12.2009
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import java.util.Map;

/**
 * <p>
 * This interface represents a single element for a <code>MidiNoteCounterNode</code>.
 * </p>
 * <p>
 * Implementations should implement the <code>toString()</code> method properly so
 * clients can call it to display it to the user.
 * </p>
 * 
 * @author Jonas Reese
 */
public interface MidiNoteCounterElement {

    /**
     * Initializes this element with the given counter node.
     * @param counterNode The counter node, not <code>null</code>.
     */
    public void init( MidiNoteCounterNode counterNode );
    
    /**
     * Notifies this <code>MidiNoteCounterElement</code> of a change to the given counter.
     * @param counter The current counter.
     * @param lastCounterFired The current counter that has fired. If the counter hasn't yet
     * fired, this parameter is <code>0</code>.
     * @return <code>true</code> if this element has fired, <code>false</code> otherwise.
     */
    public boolean notifyCounterChanged( int counter, int lastCounterFired );
    
    /**
     * Gets a <code>Map</code> of parameters that are required for this
     * <code>MidiNoteCounterElement</code> at creation time.
     * @return A <code>Map</code> of parameter name and value pairs, or
     * <code>null</code> if no parameters are required for this <code>MidiNoteCounterElement</code>.
     */
    public Map<String,String> getParameters();
    
    /**
     * Sets a <code>Map</code> of parameters that are required for this
     * <code>MidiNoteCounterElement</code> at creation time. This method is called (if it is called)
     * directly after a constructor has been called.
     * @param parameters A <code>Map</code> of parameter name and value pairs, or
     * an empty map if no parameters are required for this <code>MidiNoteCounterElement</code>.
     */
    public void setParameters( Map<String,String> parameters );
}
