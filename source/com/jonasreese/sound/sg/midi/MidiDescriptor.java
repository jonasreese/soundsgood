/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 01.10.2003
 */
package com.jonasreese.sound.sg.midi;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SessionElementType;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.impl.MidiRecorderImpl;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * This class describes a MIDI session element.
 * 
 * @author jreese
 */
public class MidiDescriptor extends SessionElementDescriptor {
    
    public static final SessionElementType TYPE = new SessionElementType() {
        public String getName() {
            return SgEngine.getInstance().getResourceBundle().getString(
                    "descriptor.type.midi" );
        }
        public String getDescription() {
            return SgEngine.getInstance().getResourceBundle().getString(
                "descriptor.type.midi.description" );
        }

        public Image getSmallIcon() {
            return new ResourceLoader( getClass(), "/resource/midi.gif" ).getAsImage();
        }

        public Image getLargeIcon() {
            return new ResourceLoader( getClass(), "/resource/midi_large.gif" ).getAsImage();
        }
    };
    
    private SgMidiSequence sequence;
    //private SequencerProxy sequencer;
    private MidiRecorderImpl midiRecorder;
    private float mpq;
    private String name;
    private MidiChangeMonitor midiChangeMonitor;

    /**
     * Constructs a <code>MidiDescriptor</code> with all
     * properties set to <code>null</code>.
     */
    public MidiDescriptor() {
        sequence = null;
        //sequencer = new SequencerProxy( this );
        midiRecorder = new MidiRecorderImpl( this );
        mpq = -1;
        name = null;
        midiChangeMonitor = new MidiChangeMonitor() {
            public void midiEventsAdded(
                    SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj ) {
            }
            public void midiEventsRemoved(
                    SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj ) {
            }
            public void midiEventsChanged(
                    SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj ) {
            }
            public void midiTrackAdded(
                    SgMidiSequence sequence, TrackProxy track, Object changeObj ) {
                float tempo = track.getTempoInMPQ();
                if (tempo >= 0) {
                    setTempoInMpq( tempo );
                }
                // invoke midi recorder so it can update it's solo, mute and record states
                midiRecorder.midiTrackAdded( sequence, track, changeObj );
            }
            public void midiTrackRemoved(
                    SgMidiSequence sequence, TrackProxy track, Object changeObj ) {
                float tempo = track.getTempoInMPQ();
                if (tempo >= 0) {
                    setTempoInMpq( -1 );
                }
                // invoke midi recorder so it can update it's solo, mute and record states
                midiRecorder.midiTrackRemoved( sequence, track, changeObj );
            }
            public void midiTrackLengthChanged(
                    SgMidiSequence sequence, TrackProxy track, Object changeObj) {
                midiRecorder.midiTrackLengthChanged( sequence, track, changeObj );
            }
            public void midiTrackEventMapChanged(
                    SgMidiSequence sequence, TrackProxy track, Object changeObj ) {
            }
        };
    }
    
    /**
     * Returns the MIDI type.
     * @return The type, same as <code>MidiDescriptor.TYPE</code>.
     */
    public SessionElementType getType() {
        return TYPE;
    }
    
    /**
     * Sets this <code>MidiDescriptor</code>s name.
     * @param name The name to set. Can be <code>null</code> if the
     *        name shall be set to it's default value.
     */
    public void setName( String name ) {
        this.name = name;
    }
    
    /**
     * Gets this <code>MidiDescriptor</code>s name.
     * @return The name, or <code>null</code> if none is assigned.
     */
    public String getName() {
        if (name != null) {
            return name;
        }
        File f = getFile();
        if (f != null) {
            return f.getName();
        }
        return SgEngine.getInstance().getResourceBundle().getString(
            "midi.new.defaultName" );
    }
    
    // copy description from superclass...
    public void save() throws IOException {
        try {
            saveCopy( getFile() );
            setChanged( false );
        } catch (IOException ioex) {
            throw ioex;
        }
    }
    
    // copy description from superclass...
    public void saveCopy( File copy ) throws IOException {
        SgMidiSequence seq;
        try {
            seq = getSequence();
        } catch (InvalidMidiDataException e) {
            throw new IOException( e.getMessage() );
        }
        if (seq != null) {
            int[] types = MidiSystem.getMidiFileTypes( seq );
            System.out.println( "supported types: " );
            if (types.length == 0) {
                throw new IOException(
                    SgEngine.getInstance().getResourceBundle().getString(
                        "error.midiSaveNotSupported" ) );
            }
            if (seq.getTrackCount() == 0) {
                throw new IOException(
                    SgEngine.getInstance().getResourceBundle().getString(
                        "error.atLeastOneTrackRequiredToSave" ) );
            }
            FileOutputStream fout = new FileOutputStream( copy );
            MidiSystem.write( seq, types[0], fout );
            fout.close();
        }
    }

    /**
     * Gets the MIDI <code>Sequence</code> that is associated
     * with this <code>MidiDescriptor</code>.
     * @return The <code>Sequence</code>, or <code>null</code> if
     *         no <code>Sequence</code> has yet been created for
     *         this <code>MidiDescriptor</code>.
     * @throws IOException
     * @throws InvalidMidiDataException
     */
    public SgMidiSequence getSequence() throws InvalidMidiDataException, IOException {
        if (sequence != null) {
            return sequence;
        }
        return SgEngine.getInstance().loadSequence( this );
    }

    /**
     * Sets a <code>Sequence</code> for this <code>MidiDescriptor</code>.
     * @param sequence The sequence to set.
     */
    public void setSequence( SgMidiSequence sequence ) {
        if (this.sequence != null) {
            this.sequence.removeMidiChangeMonitor( midiChangeMonitor );
        }
        this.sequence = sequence;
        if (sequence != null) {
            sequence.addMidiChangeMonitor( midiChangeMonitor );
            TrackProxy[] trackProxies = sequence.getTrackProxies();
            float tempo = -1;
            for (int i = 0; tempo < 0 && i < trackProxies.length ; i++) {
                tempo = trackProxies[i].getTempoInMPQ();
            }
            if (tempo >= 0) {
                setTempoInMpq( tempo );
            }
        }
    }
    
    public void setSession( Session session ) {
        super.setSession( session );
        midiRecorder.init();
    }
    
    /**
     * Sets the tempo in microseconds per quarternote. This method is
     * automatically called, so the programmer does not need to call this method.
     * @param mpq The tempo. Set to -1 if shall be set to 'unknown'
     */
    public void setTempoInMpq( float mpq ) {
        if (mpq < 0) { mpq = -1; }
        
        if (this.mpq != mpq) {
            PropertyChangeEvent e = new PropertyChangeEvent(
                this, "tempoInMpq", new Float( this.mpq ), new Float( mpq ) );
            this.mpq = mpq;
            propertyChangeSupport.firePropertyChange( e );
        }
    }
    
    /**
     * Gets the MPQ tempo.
     * @return The tempo in microseconds per quarternote, or -1 if tempo is unknown.
     */
    public float getTempoInMpq() { return mpq; }
    
    /**
     * Gets the BPM tempo.
     * @return The tempo in beats per minute, or -1 if tempo is unknown.
     */
    public float getTempoInBpm() {
        if (mpq < 0) { return -1; }
        return MidiToolkit.mpqToBPM( mpq );
    }
    
    /**
     * Gets a <code>MidiRecorder</code> for playing/recording the sequence
     * set in this <code>MidiDescriptor</code>.
     * @return A non-<code>null</code> <code>MidiRecorder</code> instance.
     */
    public MidiRecorder getMidiRecorder() {
        return midiRecorder;
    }
    
	private int getIndexOf( TrackProxy track ) {
        if (sequence == null) {
            throw new IllegalStateException( "Sequence not loaded" );
        }
        
        TrackProxy[] tp = sequence.getTrackProxies();
        int index = -1;
        for (int i = 0; i < tp.length; i++) {
            if (tp[i] == track) {
                index = i;
                break;
            }
        }
        return index;
    }
    
    private String[] getTrackPropertyNames( TrackProxy track, Map<String, String> properties ) {
        ArrayList<String> list = new ArrayList<String>();
        synchronized (properties) {
            int searchIndex = getIndexOf( track );
            String id = "trackProperty.track";
            for (String key : properties.keySet()) {
                if (key.startsWith( id )) {
                    String s = key.substring( id.length() );
                    int index = s.indexOf( '.' );
                    if (index >= 0) {
                        String t = s.substring( 0, index );
                        try {
                            int trackIndex = Integer.parseInt( t );
                            if (trackIndex == searchIndex) {
                                list.add( s.substring( index + 1 ) );
                            }
                        } catch (NumberFormatException ignored) {
                            ignored.printStackTrace();
                        }
                    }
                }
            }
        }
        String[] result = new String[list.size()];
        list.toArray( result );
        return result;

    }
    
    /**
     * Stores a non-persistent client property associated with the given MIDI
     * <code>TrackProxy</code> into this <code>MidiDescriptor</code>.
     * Non-persistent client properties are lost when this session is no longer
     * referenced.<br>
     * This method fires a <code>PropertyChangeEvent</code>, using the string
     * <code>clientProperty.trackProperty.track<i>track_index</i>.</code> as prefix, directly followed by the specified
     * property name.
     * @param track The MIDI track the client property refers to.
     * @param propertyName The property name.
     * @param value The property value.
     * @throws IllegalArgumentException if the given <code>track</code> does not belong
     * to this <code>MidiDescriptor</code>'s <code>SgSequence</code>.
     * @throws IllegalStateException if the MIDI sequence is not yet loaded.
     */
    public void putClientProperty( TrackProxy track, String propertyName, String value ) {
        int index = getIndexOf( track );
        if (index < 0) {
            throw new IllegalArgumentException( "Given track is not part of sequence" );
        }
        putClientProperty( "trackProperty.track" + index + "." + propertyName, value );
    }
    
    /**
     * Gets a non-persistent client property associated with the given MIDI
     * <code>TrackProxy</code>.
     * @param track The MIDI track the client property refers to.
     * @param propertyName The property name.
     * @return The property value, as a <code>String</code>.
     * @throws IllegalArgumentException if the given <code>track</code> does not belong
     * to this <code>MidiDescriptor</code>'s <code>SgSequence</code>.
     * @throws IllegalStateException if the MIDI sequence is not yet loaded.
     */
    public String getClientProperty( TrackProxy track, String propertyName ) {
        int index = getIndexOf( track );
        if (index < 0) {
            throw new IllegalArgumentException( "Given track is not part of sequence" );
        }
        return getClientProperty( "trackProperty.track" + index + "." + propertyName );
    }
    
    /**
     * Gets all client property names for the given <code>TrackProxy</code>.
     * @param track The MIDI track to get all associated property names for.
     * @return All names, as used when storing with the
     * <code>putClientProperty(TrackProxy, String, String)</code> method.
     * @throws IllegalArgumentException if the given <code>track</code> does not belong
     * to this <code>MidiDescriptor</code>'s <code>SgSequence</code>.
     * @throws IllegalStateException if the MIDI sequence is not yet loaded.
     */
    public String[] getClientPropertyNames( TrackProxy track ) {
        return getTrackPropertyNames( track, clientProperties );
    }

    /**
     * Removes a client property associated with a <code>TrackProxy</code>.
     * @param track The MIDI track
     * @param propertyName The property name.
     * @return <code>true</code> if the  client property has been removed,
     * <code>false</code> if it did not exist an thus has not been removed. 
     */
    public boolean removeClientProperty( TrackProxy track, String propertyName ) {
        int index = getIndexOf( track );
        if (index < 0) {
            throw new IllegalArgumentException( "Given track is not part of sequence" );
        }
        return removeClientProperty( "trackProperty.track" + index + "." + propertyName );
    }
    
    /**
     * Stores a persistent client property associated with the given MIDI
     * <code>TrackProxy</code> into this <code>MidiDescriptor</code>.
     * Persistent client properties can be saved and restored.<br>
     * This method fires a <code>PropertyChangeEvent</code>, using the string
     * <code>persistentClientProperty.trackProperty.track<i>track_index</i>.</code> as prefix, directly followed by the specified
     * property name.
     * @param track The MIDI track the client property refers to.
     * @param propertyName The property name.
     * @param value The property value.
     * @throws IllegalArgumentException if the given <code>track</code> does not belong
     * to this <code>MidiDescriptor</code>'s <code>SgSequence</code>.
     * @throws IllegalStateException if the MIDI sequence is not yet loaded.
     */
    public void putPersistentClientProperty( TrackProxy track, String propertyName, String value ) {
        int index = getIndexOf( track );
        if (index < 0) {
            throw new IllegalArgumentException( "Given track is not part of sequence" );
        }
        putPersistentClientProperty( "trackProperty.track" + index + "." + propertyName, value );
    }
    
    /**
     * Stores a persistent client property array associated with the given MIDI
     * <code>TrackProxy</code> into this <code>MidiDescriptor</code>.
     * Persistent client properties can be saved and restored.<br>
     * This method fires a <code>PropertyChangeEvent</code>, using the string
     * <code>persistentClientProperty.trackProperty.track<i>track_index</i>.</code> as prefix, directly followed by the specified
     * property name.
     * @param track The MIDI track the client property refers to.
     * @param propertyName The property name.
     * @param propertyArray The property array.
     * @param delimeter The string that separates two array elements.
     * @throws IllegalArgumentException if the given <code>track</code> does not belong
     * to this <code>MidiDescriptor</code>'s <code>SgSequence</code>.
     * @throws IllegalStateException if the MIDI sequence is not yet loaded.
     */
    public void putPersistentClientPropertyArray(
            TrackProxy track, String propertyName, String[] propertyArray, String delimeter ) {
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < propertyArray.length; i++) {
            sb.append( propertyArray[i] );
            if (i + 1 < propertyArray.length) {
                sb.append( delimeter );
            }
        }
        putPersistentClientProperty( track, propertyName, sb.toString() );
    }

    /**
     * Gets a persistent client property associated with the given MIDI
     * <code>TrackProxy</code>.
     * @param track The MIDI track the client property refers to.
     * @param propertyName The property name.
     * @return The property value, as a <code>String</code>.
     * @throws IllegalArgumentException if the given <code>track</code> does not belong
     * to this <code>MidiDescriptor</code>'s <code>SgSequence</code>.
     * @throws IllegalStateException if the MIDI sequence is not yet loaded.
     */
    public String getPersistentClientProperty( TrackProxy track, String propertyName ) {
        int index = getIndexOf( track );
        if (index < 0) {
            throw new IllegalArgumentException( "Given track is not part of sequence" );
        }
        return getPersistentClientProperty( "trackProperty.track" + index + "." + propertyName );
    }
    
    /**
     * Gets a persistent client property array associated with the given MIDI
     * <code>TrackProxy</code>.
     * @param track The MIDI track the client property refers to.
     * @param propertyName The property name.
     * @param delimeter The string that separates two array elements.
     * @return The property array, as a <code>String[]</code>.
     * @throws IllegalArgumentException if the given <code>track</code> does not belong
     * to this <code>MidiDescriptor</code>'s <code>SgSequence</code>.
     * @throws IllegalStateException if the MIDI sequence is not yet loaded.
     */
    public String[] getPersistentClientPropertyArray(
            TrackProxy track, String propertyName, String delimeter ) {
        String s = getPersistentClientProperty( track, propertyName );
        if (s == null) { return null; }
        StringTokenizer st = new StringTokenizer( s, delimeter );
        String[] result = new String[st.countTokens()];
        for (int i = 0; i < result.length; i++) {
            result[i] = st.nextToken();
        }
        return result;
    }
    
    /**
     * Removes a persistent client property associated with a <code>TrackProxy</code>.
     * @param track The MIDI track
     * @param propertyName The property name.
     * @return <code>true</code> if the persistent client property has been removed,
     * <code>false</code> if it did not exist an thus has not been removed. 
     */
    public boolean removePersistentClientProperty( TrackProxy track, String propertyName ) {
        int index = getIndexOf( track );
        if (index < 0) {
            throw new IllegalArgumentException( "Given track is not part of sequence" );
        }
        return removePersistentClientProperty( "trackProperty.track" + index + "." + propertyName );
    }
    
    /**
     * Gets all persistent client property names for the given <code>TrackProxy</code>.
     * @param track The MIDI track to get all associated property names for.
     * @return All names, as used when storing with the
     * <code>putPersistentClientProperty(TrackProxy, String, String)</code> method.
     * @throws IllegalArgumentException if the given <code>track</code> does not belong
     * to this <code>MidiDescriptor</code>'s <code>SgSequence</code>.
     * @throws IllegalStateException if the MIDI sequence is not yet loaded.
     */
    public String[] getPersistentClientPropertyNames( TrackProxy track ) {
        return getTrackPropertyNames( track, persistentClientProperties );
    }
    
    /**
     * Resets any data cached in memory by this <code>SessionElementDescriptor</code>.
     * This method can be called when data held by this <code>SessionElementDescriptor</code>
     * shall be re-loaded from the file system. This method should not be called while
     * there are <code>ViewInstance</code>s registered, or this
     * <code>SessionElementDescriptor</code> has changed but not yet been saved.
     * However, this method will not check any preconditions and anyway reset it's memory
     * data. So be careful when calling this method.<br>
     */
    public void resetData() {
        setSequence( null );
    }
    
    /**
     * This method can be invoked by MIDI sequence when a track has been removed,
     * so that the track-specific persistent client properties can be updated.
     */
    void trackRemovedAt( int index ) {
        String prefix = "trackProperty.track";
        ArrayList<String> removeKeys = new ArrayList<String>();
        ArrayList<String[]> addProperties = new ArrayList<String[]>();
        synchronized (persistentClientProperties) {
            for (String key : persistentClientProperties.keySet()) {
                if (key.startsWith( prefix )) {
                    String s = key.substring( prefix.length() );
                    int idx = s.indexOf( '.' );
                    if (idx >= 0) {
                        String istr = s.substring( 0, idx );
                        try {
                            int compIndex = Integer.parseInt( istr );
                            if (compIndex > index) {
                                removeKeys.add( key );
                                addProperties.add( 
                                        new String[] {
                                                prefix + (compIndex - 1) + s.substring( istr.length() ),
                                                (String) persistentClientProperties.get( key ) } );
                            }
                        } catch (NumberFormatException nfex) {
                            nfex.printStackTrace();
                        }
                    }
                }
            }
            // remove old...
            for (int i = 0; i < removeKeys.size(); i++) {
                persistentClientProperties.remove( removeKeys.get( i ) );
            }
            // ...and add new properties.
            for (int i = 0; i < addProperties.size(); i++) {
                String[] s = (String[]) addProperties.get( i );
                persistentClientProperties.put( s[0], s[1] );
            }
        }
    }
    /**
     * This method can be invoked by MIDI sequence when a track has been inserted,
     * so that the track-specific persistent client properties can be updated.
     */
    void trackInsertedAt( int index ) {
        String prefix = "trackProperty.track";
        ArrayList<String> removeKeys = new ArrayList<String>();
        ArrayList<String[]> addProperties = new ArrayList<String[]>();
        synchronized (persistentClientProperties) {
            for (String key : persistentClientProperties.keySet()) {
                if (key.startsWith( prefix )) {
                    String s = key.substring( prefix.length() );
                    int idx = s.indexOf( '.' );
                    if (idx >= 0) {
                        String istr = s.substring( 0, idx );
                        try {
                            int compIndex = Integer.parseInt( istr );
                            if (compIndex >= index) {
                                removeKeys.add( key );
                                addProperties.add( 
                                        new String[] {
                                                prefix + (compIndex + 1) + s.substring( istr.length() ),
                                                (String) persistentClientProperties.get( key ) } );
                            }
                        } catch (NumberFormatException nfex) {
                            nfex.printStackTrace();
                        }
                    }
                }
            }
            // remove old...
            for (int i = 0; i < removeKeys.size(); i++) {
                persistentClientProperties.remove( removeKeys.get( i ) );
            }
            // ...and add new properties.
            for (int i = 0; i < addProperties.size(); i++) {
                String[] s = (String[]) addProperties.get( i );
                persistentClientProperties.put( s[0], s[1] );
            }
        }
    }

    @Override
    public void destroy() {
    }
}