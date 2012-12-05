/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 23.09.2003
 */
package com.jonasreese.sound.sg;

import java.util.EventObject;

/**
 * <b>
 * This class denotes events being posted when an element as a
 * part of a session has been added or removed.
 * </b>
 * @author jreese
 */
public class SessionElementEvent extends EventObject {
    
    private static final long serialVersionUID = 1;
    
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_MIDI = 1;

    private SessionElementDescriptor element;
    private int type;
   
    /**
     * Constructs a new <code>SessionElementEvent</code>.
     * @param source The source of the event, usually a
     *        <code>Session</code> object.
     * @param element The element having changed.
     * @param type The type of file (e.g., TYPE_MIDI).
     */
    public SessionElementEvent(
        Object source, SessionElementDescriptor element, int type )
    {
        super( source );
        this.element = element;
        this.type = type;
    }

	/**
     * Gets the element that has been modified.
	 * @return The element, described by a
     *         <code>SessionElementDescriptor</code>.
	 */
	public SessionElementDescriptor getSessionElement()
	{
		return element;
	}

	/**
     * Gets the type of file, e.g. <code>TYPE_MIDI</code>.
	 * @return
	 */
	public int getType()
	{
		return type;
	}

}
