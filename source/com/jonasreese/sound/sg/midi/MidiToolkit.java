/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 30.12.2003
 */
package com.jonasreese.sound.sg.midi;

import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.vstcontainer.VstMidiEvent;

/**
 * <p>
 * This class provides some utility functionality concerning MIDI.
 * </p>
 * @author jreese
 */
public class MidiToolkit
{
    private static MidiDeviceList inputDeviceList = null;
    private static MidiDeviceList outputDeviceList = null;
    
    private MidiToolkit() {}
    
    /**
     * Converts the given MIDI command value into a human-readable string
     * representation.
     * @param command The MIDI message command.
     * @return A non-<code>null</code> string. If the command is not valid,
     *         the string <code>INVALID</code> is returned.
     */
    public static String getCommandString( int command ) {
        switch (command) {
            case ShortMessage.CHANNEL_PRESSURE: return "CHANNEL_PRESSURE";
            case ShortMessage.CONTROL_CHANGE: return "CONTROL_CHANGE";
            case ShortMessage.NOTE_OFF: return "NOTE_OFF";
            case ShortMessage.NOTE_ON: return "NOTE_ON";
            case ShortMessage.PITCH_BEND: return "PITCH_BEND";
            case ShortMessage.POLY_PRESSURE: return "POLY_PRESSURE";
            case ShortMessage.PROGRAM_CHANGE: return "PROGRAM_CHANGE";
        }
        return "INVALID";
    }
    
    /**
     * Converts the given MIDI status value into a human-readable string
     * representation.
     * @param status The MIDI message status.
     * @return A non-<code>null</code> string. If the status is not valid,
     *         the string <code>INVALID</code> is returned.
     */
    public static String getStatusString( int status ) {
        switch (status) {
            case ShortMessage.ACTIVE_SENSING: return "ACTIVE_SENSING";
            case ShortMessage.CONTINUE: return "CONTINUE";
            case ShortMessage.END_OF_EXCLUSIVE: return "END_OF_EXCLUSIVE";
            case ShortMessage.MIDI_TIME_CODE: return "MIDI_TIME_CODE";
            case ShortMessage.SONG_POSITION_POINTER: return "SONG_POSITION_POINTER";
            case ShortMessage.SONG_SELECT: return "SONG_SELECT";
            case ShortMessage.START: return "START";
            case ShortMessage.STOP: return "STOP";
            case ShortMessage.SYSTEM_RESET: return "SYSTEM_RESET";
            case ShortMessage.TIMING_CLOCK: return "TIMING_CLOCK";
            case ShortMessage.TUNE_REQUEST: return "TUNE_REQUEST";
        }
        return "INVALID";
    }
    
    /**
     * 
     * @param message
     * @return
     */
    public static boolean isChannelMessage( MidiMessage message ) {
        if (message instanceof ShortMessage) {
            return isChannelMessageStatusByte( ((ShortMessage) message).getStatus() );
        }
        return false;
    }
    
    /**
     * Returns <code>true</code> if the given status byte belongs to a MIDI
     * channel message.
     * @return <code>true</code> if and only if the given status byte is a
     * channel message status byte.
     */
    public static boolean isChannelMessageStatusByte( int status ) {
        switch (status & 0xf0) {
            case ShortMessage.CHANNEL_PRESSURE:
            case ShortMessage.CONTROL_CHANGE:
            case ShortMessage.NOTE_OFF:
            case ShortMessage.NOTE_ON:
            case ShortMessage.PITCH_BEND:
            case ShortMessage.POLY_PRESSURE:
            case ShortMessage.PROGRAM_CHANGE:
                return true;
        }
        return false;
    }
    
    /**
     * Translates MIDI events for one MIDI resoultion into another resolution.
     * @param oldResolution The current MIDI events resolution value.
     * @param newResolution The resoultion value the MIDI events shall be translated into.
     * @param events The MIDI events as an array. This method may change each array element
     *        by calling the <code>MidiEvent.setTick(long)</code> method.
     */
    public static void translateResolutions( int oldResolution, int newResolution, MidiEvent[] events ) {
        if (events == null || oldResolution == newResolution) { return; }
        for (int i = 0; i < events.length; i++) {
            long newTick = (long)
                (((double) events[i].getTick() / (double) oldResolution) * (double) newResolution);
            events[i].setTick( newTick );
        }
    }
    
    /**
     * Converts MPQ into BPM.
     * @param mpq The mpq value.
     * @return The according BPM value.
     */
    public static float mpqToBPM( float mpq ) {
        float bpm = 6.0E7F / mpq;
        return bpm;
    }
    
    /**
     * Converts BPM into MPQ. 
     * @param bpm The bpm value.
     * @return The according MPQ value.
     */
    public static float bpmToMPQ( float bpm ) {
        float mpq = 6.0E7F / bpm;
        return mpq;
    }
    
    /**
     * Calculates the nano seconds per quarter note by a BPM value.
     * @param bpm The BPM (Beats Per Minute) value.
     * @return A double precision nanosecond value.
     */
    public static double bpmToNanosPerQuarternote( float bpm ) {
        return 6.0E10D / (double) bpm;
    }
    
    /**
     * Creates a three-element byte array from the given MPQ tempo value.
     * @param mpq The tempo in microseconds per quarternote.
     * @return The encoded three-element byte array.
     */
    public static byte[] createTempoBytes( float mpq ) {
        byte[] tempoBytes = new byte[3];
        int tempo = (int) mpq;//Float.floatToRawIntBits( mpq );
        tempoBytes[2] = toByte( tempo & 0xff );
        tempo >>= 8;
        tempoBytes[1] = toByte( tempo & 0xff );
        tempo >>= 8;
        tempoBytes[0] = toByte( tempo & 0xff );

        return tempoBytes;
    }
    
    /**
     * Gets the tempo (in microseconds per quarternote) from the given meta message.
     * @param metaMessage The tempo meta message (type 0x51).
     * @return The tempo in MPQ.
     * @throws IllegalArgumentException if the given meta message is not of type 0x51.
     */
    public static float getTempoInMPQ( MetaMessage metaMessage ) {
        if (metaMessage.getType() != 0x51) {
            throw new IllegalArgumentException(
                "Tempo information can only be extracted from a meta message of type 0x51" );
        }
        byte[] data = metaMessage.getData();
        return (toInt( data[0] ) << 16) | (toInt( data[1] ) << 8) | toInt( data[2] );
    }
    
    /**
     * Returns <code>true</code> if and only if the given <code>MidiEvent</code>
     * carries track name information.
     * @param event The event to be checked.
     * @return <code>true</code> if the given event carries track name information.
     *         If <code>true</code> is returned, the <code>MidiMessage</code> carried
     *         by the event is of type <code>MetaMessage</code>.
     */
    public static boolean isTrackNameEvent( MidiEvent event ) {
        if (event.getMessage() instanceof MetaMessage) {
            byte[] data = event.getMessage().getMessage();
            if (data != null &&
                data.length > 2 &&
                (((int) data[0]) & 0xff) == 0xff &&
                data[1] == 0x03) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns <code>true</code> if and only if the given <code>MidiEvent</code>
     * carries tempo information.
     * @param event The event to be checked.
     * @return <code>true</code> if the given event carries tempo information.
     *         If <code>true</code> is returned, the <code>MidiMessage</code> carried
     *         by the event is of type <code>MetaMessage</code>.
     */
    public static boolean isTempoEvent( MidiEvent e ) {
        MidiMessage message = e.getMessage();
        if (message instanceof MetaMessage) {
            MetaMessage metaMessage = (MetaMessage) message;
            if (metaMessage.getType() == 0x51) { // set tempo
                return true;
            }
        }
        return false;
    }
    
    /**
     * Creates a new <code>MidiEvent</code> that contains tempo information.
     * @param tempoInMpq The tempo (in microseconds per quarternote) to set in the newly created event.
     * @return A newly created, non-attached <code>MidiEvent</code> object.
     */
    public static MidiEvent createTempoEvent( float tempoInMpq ) {
        byte[] data = createTempoBytes( tempoInMpq );
        
        MetaMessage message = new MetaMessage();
        try {
            message.setMessage( 0x51, data, data.length );
        } catch (InvalidMidiDataException e) { // should not occur
            System.err.println( "FATAL BUG IN CLASS " + MidiToolkit.class );
            e.printStackTrace();
        }
        MidiEvent event = new MidiEvent( message, 0 );
        System.out.println( "event tempo check: mpq = " + getTempoInMPQ( message ) + ", bpm = " + mpqToBPM(getTempoInMPQ( message ) ) );
        return event;
    }
    
    /**
     * Gets the track name from a track name <code>MetaMessage</code>.
     * @param metaMessage The <code>MetaMessage</code> containing the track name.
     *        Sould be of type 0x3.
     * @return The track name, or <code>null</code> if the given <code>MetaMessage</code>
     *         does not contain a track name (e.g., wrong type, ...).
     */
    public static String getTrackName( MetaMessage metaMessage ) {
        byte[] data = metaMessage.getMessage();
        if (data != null &&
            data.length > 2 &&
            toInt( data[0] ) == 0xff &&
            toInt( data[1] ) == 0x03) {
            int len = toInt( data[2] );
            return new String( data, 3, Math.min( len, data.length - 3 ) );
        }
        return null;
    }
    
    /**
     * Gets the default human-readable names for MIDI channels. The names are extracted
     * from the resource bundle, so they depend on the current language.
     * @param includeNumbers If set to <code>true</code>, the name will include the
     *        MIDI channel number.
     * @return An array of 16 elements, wher element 0 is the name of MIDI channel 1
     *         and element 15 is the name of MIDI channel 16.
     */
    public static String[] getDefaultMidiChannelNames( boolean includeNumbers ) {
        String[] defaultNames = new String[16];
        ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        for (int i = 0; i < defaultNames.length; i++) {
            defaultNames[i] =
                rb.getString( "midi.channel." + i + ".name" );
            if (includeNumbers) {
                String pref = "" + (i + 1);
                if (pref.length() < 2) { pref = "0" + pref; }
                defaultNames[i] = pref + " " + defaultNames[i];
            }
        }
        return defaultNames;
    }
    
    private static int toInt( byte val ) {
        return ((val < 0) ? 256 + val : val);
    }
    
    private static byte toByte( int val ) {
        byte result = 0;
        // check if return value must be negative
        if (val > Byte.MAX_VALUE) {
            result = (byte) (val - 256);
        } else {
            result = (byte) val;
        }

        return result;
    }
    
    /**
     * Gets a <code>MidiDevice</code> for the given descriptor.
     * @param descriptor The <code>MidiDeviceDescriptor</code>
     * @return A <code>MidiDevice</code>. If the given device descriptor does not map to a valid
     * existing MIDI device, a dummy device is returned.
     * @throws MidiUnavailableException
     */
    public static MidiDevice getMidiDevice( MidiDeviceDescriptor descriptor ) throws MidiUnavailableException {
        if (descriptor == null || descriptor.getDeviceInfo() == null) {
            return new DummyDevice();
        }
        return MidiSystem.getMidiDevice( descriptor.getDeviceInfo() );
    }
    
    /**
     * Gets the list of all MIDI input devices that are available.
     * @return A <code>MidiDeviceList</code> containing all available MIDI input devices.
     */
    public static MidiDeviceList getMidiInputDeviceList() {
        if (inputDeviceList != null) {
            return inputDeviceList;
        }
        MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
        ArrayList<MidiDeviceDescriptor> descriptors = new ArrayList<MidiDeviceDescriptor>();
        // add devices that exist in the system
        for (int i = 0; i < info.length; i++) {
            MidiDevice dev = null;
            try {
                dev = MidiSystem.getMidiDevice( info[i] );
            } catch (MidiUnavailableException e) {}
            if (dev != null &&
                (dev.getMaxTransmitters() != 0) &&
                !(dev instanceof Sequencer)) {
                descriptors.add( new MidiDeviceDescriptor( info[i], null ) );
            }
        }
        // add devices that do not exist in the system, but in the device list
        // given by the application properties
        MidiDeviceList list = SgEngine.getInstance().getProperties().getMidiInputDeviceList();
        for (int i = 0; i < list.getCount(); i++) {
            MidiDeviceDescriptor desc = list.getMidiDeviceDescriptor( i );
            if (desc.getDeviceInfo() == null) {
                descriptors.add( desc );
            }
        }
        
        MidiDeviceDescriptor[] array = new MidiDeviceDescriptor[descriptors.size()];
        descriptors.toArray( array );
        inputDeviceList = new MidiDeviceList( array );
        return inputDeviceList;
    }
    
    /**
     * Gets the list of all MIDI output devices that are available.
     * @return A <code>MidiDeviceList</code> containing all available MIDI output devices.
     */
    public static MidiDeviceList getMidiOutputDeviceList() {
        if (outputDeviceList != null) {
            return outputDeviceList;
        }
        MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
        ArrayList<MidiDeviceDescriptor> descriptors = new ArrayList<MidiDeviceDescriptor>();
        // add devices that exist in the system
        for (int i = 0; i < info.length; i++) {
            MidiDevice dev = null;
            try {
                dev = MidiSystem.getMidiDevice( info[i] );
            } catch (MidiUnavailableException e) {}
            if (dev != null &&
                (dev.getMaxReceivers() != 0) &&
                !(dev instanceof Sequencer)) {
                descriptors.add( new MidiDeviceDescriptor( info[i], null ) );
            }
        }
        // add devices that do not exist in the system, but in the device list
        // given by the application properties
        MidiDeviceList list = SgEngine.getInstance().getProperties().getMidiOutputDeviceList();
        for (int i = 0; i < list.getCount(); i++) {
            MidiDeviceDescriptor desc = list.getMidiDeviceDescriptor( i );
            if (desc.getDeviceInfo() == null) {
                descriptors.add( desc );
            }
        }
        
        MidiDeviceDescriptor[] array = new MidiDeviceDescriptor[descriptors.size()];
        descriptors.toArray( array );
        outputDeviceList = new MidiDeviceList( array );
        return outputDeviceList;
    }
    
    /**
     * Converts a java MIDI message to a VST midi message. This method stores
     * the result in the <code>VstEvent</code> parameter (call-by-reference).
     * @param m The <code>MidiMessage</code> to be converted to a VST message.
     * @param vstEvent The VST event.
     */
    public static void javaMidiToVstMidi( MidiMessage m, VstMidiEvent vstEvent ) {
        // TODO: implement this properly!
        byte[] message = m.getMessage();
        vstEvent.setMidiData0( message[0] );
        if (message.length > 1) {
            vstEvent.setMidiData1( message[1] );
            if (message.length > 2) {
                vstEvent.setMidiData2( message[2] );
            }
        }
    }
    
    /**
     * Converts a java MIDI message to a VST midi message. This method stores
     * the result in the <code>VstEvent</code> parameter (call-by-reference).
     * @param m The <code>MidiMessage</code> to be converted to a VST message.
     * @param vstEvent The VST event.
     */
    public static void vstMidiToJavaMidi( VstMidiEvent vstEvent, MidiMessage m ) {
        // TODO: implement this properly!
        m.getMessage()[0] = vstEvent.getMidiData0();
        m.getMessage()[1] = vstEvent.getMidiData1();
        m.getMessage()[2] = vstEvent.getMidiData2(); 
    }

    /**
     * Convenience method that repeats the given <code>MidiEvent</code>s
     * <code>countPerTact</code> times per tact and <code>tactCount</code> tacts.
     * @param events The original events. They are not included in the returned array.
     * The MIDI events must contain short messages. Neither the array object nor any
     * of it's elements may be <code>null</code>.
     * @param seqeunce The MIDI sequence. It will not be changed by this method and is only
     * required for information about the MIDI resulution.
     * @param countPerTact The number of repeatings per tact. Must be &gt;0
     * @param tactCount The number of repeat tacts.
     * @return A <code>MidiEvent</code> array containing the repeated events.
     */
    public static MidiEvent[] repeatEvents(
            SgMidiSequence sequence,
            MidiEvent[] events,
            int countPerTact,
            int tactCount ) {
        
        if (countPerTact <= 0) {
            return new MidiEvent[0];
        }
        
        for (int i = 0; i < events.length; i++) {
            if (!(events[i].getMessage() instanceof ShortMessage)) {
                throw new IllegalArgumentException( "Event at index " + i + " contains no ShortMessage" );
            }
        }

        MidiEvent[] result = new MidiEvent[events.length * countPerTact * tactCount - events.length];
        
        int ppq;
        if (sequence.getDivisionType() == SgMidiSequence.PPQ) {
            ppq = sequence.getResolution();
        } else {
            throw new UnsupportedOperationException( "SMPTE division types not yet supported" );
        }

        int ppn = ppq * 4;
        
        // pre-calculate tact offsets for better performance
        int[] tactOffsets = new int[countPerTact];
        tactOffsets[0] = 0;
        for (int i = 1; i < tactOffsets.length; i++) {
            tactOffsets[i] = ((i * ppn) / countPerTact);
        }
        
        int c = 0;
        try {
            for (int i = 0; i < countPerTact; i++) {
                for (int j = 0; j < tactCount; j++) {
                    for (int k = 0; k < events.length; k++) {
                        if (i > 0 || j > 0) {
                            long tick = events[k].getTick();
                            tick += (j * ppn) + tactOffsets[i];
                            ShortMessage sm = (ShortMessage) events[k].getMessage();
                            ShortMessage newMessage = new ShortMessage();
                            newMessage.setMessage( sm.getCommand(), sm.getChannel(), sm.getData1(), sm.getData2() );
                            result[c++] = new MidiEvent( newMessage, tick );
                        }
                    }
                }
            }
        } catch (InvalidMidiDataException imdex) {
            imdex.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Copies the given <code>EventDescriptor</code> and sets the given <code>EventMap</code>
     * as parent.
     * @param ed The <code>EventDescriptor</code> to be copied.
     * @param eventMap The parent <code>EventMap</code>.
     * @return The resulting copy.
     */
    public static EventDescriptor copyEventDescriptor( EventDescriptor ed, EventMap eventMap ) {
        if (ed instanceof MetaMessageDescriptor) {
            return new MetaMessageDescriptor( ed.getDescription(), eventMap );
        } else if (ed instanceof NoteDescriptor) {
            NoteDescriptor nd = (NoteDescriptor) ed;
            return new NoteDescriptor( nd.getNote(), eventMap, nd.getDescription() );
        } else if (ed instanceof ShortMessageEventDescriptor) {
            ShortMessageEventDescriptor sd = (ShortMessageEventDescriptor) ed;
            return new ShortMessageEventDescriptor( sd.getDescription(), eventMap, sd.getStatus() );
        } else if (ed instanceof SysexDescriptor) {
            return new SysexDescriptor( ed.getDescription(), eventMap );
        }
        return null;
    }
}