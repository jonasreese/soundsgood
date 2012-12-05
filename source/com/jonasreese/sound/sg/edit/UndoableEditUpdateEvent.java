/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 25.11.2003
 */
package com.jonasreese.sound.sg.edit;

import java.util.EventObject;

/**
 * <b>
 * </b>
 * @author jreese
 */
public class UndoableEditUpdateEvent extends EventObject {
    static final long serialVersionUID = 0;
    
    /**
     * Constructs a new <code>UndoableEditUpdateEvent</code> object.
     * @param source The source of the event. This should be an <code>UndoManager</code>.
     */
    public UndoableEditUpdateEvent( Object source ) {
        super( source );
    }
}
