/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 21.12.2003
 */
package com.jonasreese.sound.sg.midi;

/**
 * <p>
 * A descriptor for a Meta Event message.
 * </p>
 * @author jreese
 */
public class MetaMessageDescriptor extends EventDescriptor {
    /**
     * Constructs a new <code>MetaMessageDescriptor</code>.
     * @param description The human-readable description.
     * @param eventMap The parent <code>EventMap</code>.
     */
    public MetaMessageDescriptor( String description, EventMap eventMap ) {
        super( description, eventMap );
    }
}
