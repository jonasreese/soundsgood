/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 16.12.2003
 */
package com.jonasreese.sound.sg.midi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

/**
 * <p>
 * This class provides functionalities that are required to map a MIDI event
 * to a description (e.g., a note or an instrument part description like 'snare drum',
 * 'hi-hat', 'bass drum' for drums).
 * Additionally, a <code>EventMap</code> allows to specifiy a logical index for each event
 * type (so MIDI events can be permutated logically), or to disable certain event types or
 * notes (e.g., if they shall not be displayed in a visual environment).
 * </p>
 * @author jreese
 */
public abstract class EventMap {

    protected static Map<String,Integer> typeMap;
    public static final Map<Integer,String> EVENT_NAME_MAP;
    
    static {
        typeMap = new LinkedHashMap<String,Integer>();
        typeMap.put( "activeSensing", new Integer( ShortMessage.ACTIVE_SENSING ) );
        typeMap.put( "channelPressure", new Integer( ShortMessage.CHANNEL_PRESSURE ) );
        typeMap.put( "controlChange", new Integer( ShortMessage.CONTROL_CHANGE ) );
        typeMap.put( "endOfExclusive", new Integer( ShortMessage.END_OF_EXCLUSIVE ) );
        typeMap.put( "midiTimeCode", new Integer( ShortMessage.MIDI_TIME_CODE ) );
        typeMap.put( "pitchBend", new Integer( ShortMessage.PITCH_BEND ) );
        typeMap.put( "pitchBend", new Integer( ShortMessage.PITCH_BEND ) );
        typeMap.put( "polyPressure", new Integer( ShortMessage.POLY_PRESSURE ) );
        typeMap.put( "programChange", new Integer( ShortMessage.PROGRAM_CHANGE ) );
        typeMap.put( "songPositionPointer", new Integer( ShortMessage.SONG_POSITION_POINTER ) );
        typeMap.put( "songSelect", new Integer( ShortMessage.SONG_SELECT ) );
        typeMap.put( "songSelect", new Integer( ShortMessage.SONG_SELECT ) );
        typeMap.put( "startStopContinue", new Integer( ShortMessage.START ) );
        typeMap.put( "systemReset", new Integer( ShortMessage.SYSTEM_RESET ) );
        typeMap.put( "timingClock", new Integer( ShortMessage.TIMING_CLOCK ) );
        typeMap.put( "tuneRequest", new Integer( ShortMessage.TUNE_REQUEST ) );

        EVENT_NAME_MAP = new LinkedHashMap<Integer,String>();
        EVENT_NAME_MAP.put( ShortMessage.ACTIVE_SENSING, "ACTIVE_SENSING" );
        EVENT_NAME_MAP.put( ShortMessage.CHANNEL_PRESSURE, "CHANNEL_PRESSURE" );
        EVENT_NAME_MAP.put( ShortMessage.CONTROL_CHANGE, "CONTROL_CHANGE" );
        EVENT_NAME_MAP.put( ShortMessage.END_OF_EXCLUSIVE, "END_OF_EXCLUSIVE" );
        EVENT_NAME_MAP.put( ShortMessage.MIDI_TIME_CODE, "MIDI_TIME_CODE" );
        EVENT_NAME_MAP.put( ShortMessage.NOTE_ON, "NOTE_ON/NOTE_OFF" );
        EVENT_NAME_MAP.put( ShortMessage.PITCH_BEND, "PITCH_BEND" );
        EVENT_NAME_MAP.put( ShortMessage.POLY_PRESSURE, "POLY_PRESSURE" );
        EVENT_NAME_MAP.put( ShortMessage.PROGRAM_CHANGE, "PROGRAM_CHANGE" );
        EVENT_NAME_MAP.put( ShortMessage.SONG_POSITION_POINTER, "SONG_POSITION_POINTER" );
        EVENT_NAME_MAP.put( ShortMessage.SONG_SELECT, "SONG_SELECT" );
        EVENT_NAME_MAP.put( ShortMessage.START, "START/STOP/CONTINUE" );
        EVENT_NAME_MAP.put( ShortMessage.SYSTEM_RESET, "SYSTEM_RESET" );
        EVENT_NAME_MAP.put( ShortMessage.TIMING_CLOCK, "TIMING_CLOCK" );
        EVENT_NAME_MAP.put( ShortMessage.TUNE_REQUEST, "TUNE_REQUEST" );
    }
    
    
    
    protected String name;
    protected String description;
    
    
    /**
     * Gets a <code>URL</code> that points to the resource that is the source
     * for this <code>EventMap</code>.
     * @return A <code>URL</code>, or <code>null</code> if no such URL can be
     *         returned.
     */
    public abstract URL getURL();
    
    /**
     * Gets the logical index for the given MIDI event descriptor.
     * @param event The MIDI event descriptor to get the index for.
     * @return The index, or <code>-1</code> if the given event descriptor is
     *         not contained in this <code>EventMap</code>.
     */
    public abstract int getIndexFor( EventDescriptor event );

    /**
     * Gets the logical index for the given MIDI event.
     * @param event The MIDI event to get the index for.
     * @return The index, or <code>-1</code> if the given event is not contained
     *         in this <code>EventMap</code>.
     */
    public abstract int getIndexFor( MidiEvent event );

    /**
     * Gets the logical index for the given MIDI message.
     * @param message The MIDI message to get the index for.
     * @return The index, or <code>-1</code> if the given message type is not contained
     *         in this <code>EventMap</code>.
     */
    public abstract int getIndexFor( MidiMessage message );

    /**
     * Gets the <code>EventDescriptor</code> for the given MIDI message.
     * @param message The MIDI message to get the descriptor for.
     * @return The <code>EventDescriptor</code>, or <code>null</code> if
     *         not contained in the mapping.
     */
    public EventDescriptor getEventDescriptorFor( MidiMessage message ) {
        int index = getIndexFor( message );
        if (index < 0) { return null; }
        return getEventAt( index );
    }
    
    /**
     * Gets the <code>EventDescriptor</code> for the given MIDI event.
     * @param event The MIDI event to get the descriptor for.
     * @return The <code>EventDescriptor</code>, or <code>null</code> if
     *         not contained in the mapping.
     */
    public EventDescriptor getEventDescriptorFor( MidiEvent event ) {
        int index = getIndexFor( event );
        if (index < 0) { return null; }
        return getEventAt( index );
    }
    
    /**
     * Gets a binary representation of this <code>EventMap</code>.
     * @return A <code>byte</code> array containing this <code>EventMap</code>.
     */
    public abstract byte[] toBinary();
    
    /**
     * Gets an XML representation of this <code>EventMap</code>.
     * @return A <code>String</code> containing this <code>EventMap</code> in XML format.
     */
    public abstract String toXml();
    
    /**
     * Inserts a MIDI <code>EventDescriptor</code> at the given index.
     * @param index The index where to insert. A value of <code>0</code> sets at
     *        the first possible position, any value that is <code>&lt;= getSize()</code>
     *        appends to the end of the mapping.
     * @param eventDescriptor The event descriptor to insert or append.
     * @return The logical index where the <code>NoteDescriptor</code> has been placed, or
     * <code>-1</code> if it has not been inserted because the event specified by
     * <code>eventDescriptor</code> is already present in the mapping.
     */
    public abstract int insertEventAt( int index, EventDescriptor eventDescriptor );
    
    /**
     * Appends a MIDI EventDescriptor to the mapping.
     * @param eventDescriptor The <code>EventDescriptor</code> to append.
     * @return The logical index where the <code>EventDescriptor</code> has been placed.
     */
    public abstract int appendEvent( EventDescriptor eventDescriptor );
    
    /**
     * Removes the given MIDI event from the mapping.
     * @param event The MIDI event to be removed.
     * @return <code>true</code> if the given event has been removed successfully,
     *         <code>false</code> if it could not be removed because it was not
     *         contained in the mapping.
     */
    public abstract boolean removeEvent( MidiEvent event );
    
    /**
     * Removes the MIDI event at the specified index from the mapping.
     * @param index The index of the event to be removed.
     */
    public abstract void removeEventAt( int index );
    
    /**
     * Moves an event from a given mapping index to another mapping index.
     * @param fromIndex The from index.
     * @param toIndex The to index.
     */
    public abstract void moveEvent( int fromIndex, int toIndex );
    
    /**
     * Gets the size of this <code>EventMap</code>.
     * @return The size in elements (number of contained index-EventDescriptor pairs).
     */
    public abstract int getSize();
    
    /**
     * Gets the event descriptor at the specified index.
     * @param index The index to get the <code>EventDescriptor</code> for.
     * @return The <code>EventDescriptor</code>.
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is out of bounds.
     */
    public abstract EventDescriptor getEventAt( int index );

    /**
     * Sets all <code>EventDescriptor</code>s that shall be mapped by this
     * <code>EventMap</code>. If this <code>EventMap</code> already contains event
     * descriptors, it will be cleared first.
     * @param eventDescriptors The event descriptors to set.
     */
    public abstract void setEventDescriptors( EventDescriptor[] eventDescriptors );
    
    /**
     * Gets an array of all <code>EventDescriptor</code>s in this <code>EventMap</code>.
     * @return A newly copied array containing all <code>EventDescriptor</code> objects.
     */
    public EventDescriptor[] getEventDescriptors() {
        EventDescriptor[] ed = new EventDescriptor[getSize()];
        for (int i = 0; i < ed.length; i++) {
            ed[i] = getEventAt( i );
        }
        return ed;
    }
    
    /**
     * Gets an array of all <code>NoteDescriptor</code>s in this <code>EventMap</code>.
     * @return A newly copied array containing all <code>NoteDescriptor</code> objects.
     */
    public NoteDescriptor[] getNoteDescriptors() {
        int count = 0;
        for (int i = 0; i < getSize(); i++) {
            if (getEventAt( i ) instanceof NoteDescriptor) {
                count++;
            }
        }
        NoteDescriptor[] ed = new NoteDescriptor[count];
        int j = 0;
        for (int i = 0; i < getSize(); i++) {
            EventDescriptor e = getEventAt( i );
            if (e instanceof NoteDescriptor) {
                ed[j++] = (NoteDescriptor) e;
            }
        }
        return ed;
    }
    
    /**
     * Gets the <code>contains</code> flag for the given MIDI note.
     * @param event The MIDI event value to check.
     * @return <code>true</code> if the given event value is contained, <code>false</code>
     *         otherwise.
     */
    public abstract boolean contains( MidiEvent event );

    /**
     * Gets this event map's name.
     * @return The name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets this event map's name.
     * @param name The name to set.
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * Gets this event map's description.
     * @return The description.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets this event map's description.
     * @param description The description to set.
     */
    public void setDescription( String description ) {
        this.description = description;
    }

    /**
     * Loads this <code>EventMap</code> from an XML description provided through
     * a <code>URL</code>.
     * @param url The URL to read the XML description from.
     * @return A newly created <code>EventMap</code>.
     * @throws IOException if the reading failed.
     * @throws ParserConfigurationException if SAXParser could not be configured.
     * @throws SAXException if the input stream does not provide a correct format.
     * @throws TransformerException if the parsing failed.
     */
    public void loadFromXmlDescription( URL url )
        throws ParserConfigurationException, SAXException, IOException, TransformerException {

        InputStream is = url.openStream();
        loadFromXmlDescription( is );
    }
    
    /**
     * Loads this <code>EventMap</code> from an XML description provided through
     * an <code>InputStream</code>. The input stream will not be closed by this
     * method.
     * @param is The input stream to read the XML description from.
     * @return A newly created <code>EventMap</code>.
     * @throws IOException if the reading failed.
     * @throws ParserConfigurationException if SAXParser could not be configured.
     * @throws SAXException if the input stream does not provide a correct format.
     * @throws TransformerException if the parsing failed.
     */
    public abstract void loadFromXmlDescription( InputStream is )
        throws ParserConfigurationException, SAXException, IOException, TransformerException;

    /**
     * Loads this <code>EventMap</code> from a binary description provided through
     * a <code>byte</code> array.
     * @param data The the binary description.
     * @return A newly created <code>EventMap</code>.
     * @throws IOException if the reading failed.
     * @throws IllegalArgumentException if the binary data is not a correct
     * <code>EventMap</code> description
     */
    public abstract void loadFromBinary( byte[] data ) throws IOException, IllegalArgumentException;
    
    /**
     * Gets the MIDI message command value for the given XML type.
     * @param type The XML type. That is the MIDI command name in camel case notation,
     *        starting with a lower-case letter, e.g. <code>"activeSensing"</code> or
     *        <code>"midiTimeCode"</code>.
     * @return The according MIDI command value.
     * @throws IllegalArgumentException If the type is not a valid one.
     */
    protected static int getMidiCommandFor( String type ) {
        Object o = typeMap.get( type );
        if (o instanceof Integer) { return ((Integer) o).intValue(); }
        throw new IllegalArgumentException( "Illegal ShortMessage identifier: " + type );
    }

    /**
     * Resets this <code>EventMap</code> to the initial (default) state.
     *
     */
    public abstract void resetToDefault();
    
    /**
     * This method is notified when this <code>EventMap</code> has been changed.
     * It shall only be called by <code>NoteDescriptor</code>
     * or it's subclasses.
     */
    abstract void eventMapChanged();
}