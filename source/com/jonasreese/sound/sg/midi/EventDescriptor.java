/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 16.12.2003
 */
package com.jonasreese.sound.sg.midi;

/**
 * <p>
 * A class used for describing a MIDI event.
 * </p>
 * @author jreese
 */
public abstract class EventDescriptor {
    private String description;
    private EventMap eventMap;
    
    /**
     * Constructs a new <code>EventDescriptor</code>.
     * @param description A human-readable description.
     * @param eventMap The parent <code>EventMap</code>.
     */
    public EventDescriptor( String description, EventMap eventMap ) {
        this.description = description;
        this.eventMap = eventMap;
    }

    /**
     * Gets a human-readable event description.
     * @return The event description in a human-readable format.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets a human-readable event description.
     * @param description The event description in a human-readable format.
     */
    public void setDescription( String description ) {
        if (this.description == description ||
                (this.description != null && this.description.equals( description ))) {
            return;
        }
        this.description = description;
        if (eventMap != null) {
            eventMap.eventMapChanged();
        }
    }
    
    /**
     * Gets the parent <code>EventMap</code>.
     * @return The parent <code>EventMap</code>.
     */
    public EventMap getEventMap() {
        return eventMap;
    }
    
    public String toString() {
        return description;
    }
}
