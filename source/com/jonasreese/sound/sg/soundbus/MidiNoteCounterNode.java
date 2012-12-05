/*
 * Created on 02.12.2009
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;



/**
 * <p>
 * This interface defines methods provided by a MIDI note counter node.
 * A note counter is a programmable counter that can output MIDI notes
 * depending on the count.
 * </p>
 * 
 * @author Jonas Reese
 */
public interface MidiNoteCounterNode extends SbNode {

    /**
     * Gets the current note counter.
     * @return The current counter.
     */
    public int getCounter();
    
    /**
     * Gets the last counter that has fired.
     * @return The last counter fired.
     */
    public int getLastCounterFired();
    
    /**
     * Gets an copy of counter elements.
     * @return A copy of all counter elements.
     */
    public MidiNoteCounterElement[] getCounterElements();
    
    /**
     * Sets all counter elements from the given array.
     * @param elements The array. Must not be <code>null</code> or contain <code>null</code>
     * elements.
     */
    public void setCounterElements( MidiNoteCounterElement[] elements );

    /**
     * Gets the count of counter elements.
     * @return The element count.
     */
    public int getCounterElementCount();
    
    /**
     * Adds a <code>MidiNoteCounterElement</code> object to this <code>MidiNoteCounterNode</code>.
     * @param element The element to add. Must not be <code>null</code>.
     */
    public void addCounterElement( MidiNoteCounterElement element );

    /**
     * Adds a <code>MidiNoteCounterElement</code> object to this <code>MidiNoteCounterNode</code>.
     * @param index The index.
     * @return The element at the given index.
     * @throws ArrayIndexOutOfBoundsException if index is out of bounds.
     */
    public MidiNoteCounterElement getCounterElementAt( int index );
    
    /**
     * Removes a counter element from the list.
     * @param element The element to be removed.
     * @return <code>true</code> if and only if the removal was successful.
     */
    public boolean removeCounterElement( MidiNoteCounterElement element );
    
    /**
     * Gets the counter element at the given index.
     * @param index The index.
     * @throws ArrayIndexOutOfBoundsException if index is out of bounds.
     */
    public void removeCounterElementAt( int index );
}
