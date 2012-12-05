/*
 * Created on 08.01.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg;

import java.awt.Image;

/**
 * <p>
 * This interface shall be implemented by classes that define a type of a
 * certain session element.
 * </p>
 * @author jonas.reese
 */
public interface SessionElementType {
    
    /**
     * Gets a human-readable type name for this <code>SessionElementDescriptor</code>.
     * @return A type name, e.g. 'MIDI music format'
     */
    public abstract String getName();
    
    /**
     * Gets the description for this
     * <code>SessionElementType</code>.
     * @return The description.
     */
    public abstract String getDescription();

    /**
     * Returns the small icon.
     * @return The small icon (about 15x15 pixels) for this
     * <code>SessionElementType</code>,
     * as an <code>Image</code> object.
     */
    public abstract Image getSmallIcon();
    
    /**
     * Returns the large icon.
     * @return The large icon for this
     * <code>SessionElementType</code>,
     * as an <code>Image</code> object.
     */
    public abstract Image getLargeIcon();
}
