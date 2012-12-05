/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 21.12.2003
 */
package com.jonasreese.sound.sg.midi;

import javax.sound.midi.ShortMessage;

/**
 * <p>
 * A class used for describing a MIDI note.
 * </p>
 * @author jreese
 */
public class NoteDescriptor extends ShortMessageEventDescriptor {
    private int note;
    
    /**
     * Constructs a new <code>NoteDescriptor</code>.
     * @param note The MIDI note value.
     * @param eventMap The parent <code>EventMap</code>.
     * @param description
     */
    public NoteDescriptor( int note, EventMap eventMap, String description ) {
        super( description, eventMap, ShortMessage.NOTE_ON );
        this.note = note;
    }
    
    /**
     * Gets the note value.
     * @return the MIDI note value.
     */
    public int getNote() { return note; }
}
