/*
 * Created on 08.01.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg;


/**
 * <p>
 * A <code>SessionElementCreationHandler</code> shall be implemented to provide the
 * functionality of creating a certain type of session element.
 * </p>
 * @author jonas.reese
 */
public interface SessionElementCreationHandler {
    /**
     * Gets the type for the session elements that are created by this
     * <code>SessionElementCreationHandler</code>.
     * @return The type.
     */
    public SessionElementType getType();
    
    /**
     * Gets a human-readable description of what this handler does.
     * @return A description.
     */
    public String getDescription();
    
    /**
     * Overwrite this method to create the session element and add it to
     * the given <code>Session</code>.
     */
    public void createSessionElement( Session session );
}
