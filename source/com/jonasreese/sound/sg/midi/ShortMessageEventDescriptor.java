/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 22.12.2003
 */
package com.jonasreese.sound.sg.midi;

/**
 * <p>
 * A class used for describing a MIDI event that carries a <code>ShortMessage</code>,
 * <b>except</b> for <code>NOTE_ON</code> and <code>NOTE_OFF</code> commands.
 * </p>
 * @author jreese
 */
public class ShortMessageEventDescriptor extends EventDescriptor {
    private int status;
    
    /**
     * Constructs a new <code>ShortMessageEventDescriptor</code>.
     * @param description A human-readable description.
     * @param eventMap The parent <code>EventMap</code>.
     * @param status The MIDI command carried by the short message.
     */
    public ShortMessageEventDescriptor( String description, EventMap eventMap, int status ) {
        super( description, eventMap );
        this.status = status;
    }
    
    /**
     * Gets the MIDI status value.
     * @return The MIDI status value.
     */
    public int getStatus() { return status; }
}
