/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 30.09.2003
 */
package com.jonasreese.sound.sg;

import java.util.EventObject;

/**
 * <b>
 * </b>
 * @author jreese
 */
public class ObjectSelectionChangedEvent extends EventObject {
    private static final long serialVersionUID = 1;
    
    private SessionElementDescriptor[] selectedObjects;
    private Object trigger;
    
    /**
     * Constructs a new <code>ObjectSelectionChangedEvent</code>.
     * @param source The source to the event.
     * @param selectedObjects The selected objects.
     */
    public ObjectSelectionChangedEvent( Object e, SessionElementDescriptor[] selectedObjects ) {
        this( e, selectedObjects, null );
    }
    
    /**
     * Constructs a new <code>ObjectSelectionChangedEvent</code> with a
     * trigger object.
     * @param source The source to the event.
     * @param selectedObjects The selected objects.
     * @param trigger An object that triggered this event.
     */
    public ObjectSelectionChangedEvent( Object e, SessionElementDescriptor[] selectedObjects, Object trigger ) {
        super( e );
        this.selectedObjects = selectedObjects;
        this.trigger = trigger;
    }
    
    /**
     * Gets the selected objects.
     * @return The selected objects. An empty array if no objects are selected...
     */
    public SessionElementDescriptor[] getSelectedElements() {
        return selectedObjects;
    }
    
    /**
     * Gets the trigger for this event.
     * @return The trigger. May be <code>null</code>.
     */
    public Object getTrigger() {
        return trigger;
    }
}
