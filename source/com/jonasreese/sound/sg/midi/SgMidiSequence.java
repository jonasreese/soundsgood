/*
 * Created on 19.03.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import java.util.ArrayList;
import java.util.EventObject;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.util.AbstractEventRedirector;
import com.jonasreese.util.EventQueueHandler;
import com.jonasreese.util.EventRedirector;

/**
 * <p>
 * This class is the <code>SoundsGood</code> application's implementation
 * of <code>javax.midi.Sequence</code>.
 * </p>
 * <p>
 * This class offers functionalities that allow objects to be notified of
 * changes to a MIDI sequence. Since the container for changeable objects
 * (the MIDI events) is a MIDI track, these changes must be discovered in
 * the <code>javax.sound.midi.Track</code> implementation. But the problem
 * is that no adapted implementation of the <code>Track</code> class can
 * be provided due to access restrictions. Because of this, implementors
 * of <code>SoundsGood</code> features shall use instances of the
 * <code>TrackProxy</code> class instead if they intend to provide functionality
 * that changes the contents of a MIDI track. <code>TrackProxy</code> allows
 * the same method calls as <code>Track</code>, but provides change support.
 * </p>
 * @author jreese
 */
public class SgMidiSequence extends Sequence {
    private ArrayList<TrackProxy> trackProxies;
    private ArrayList<MidiChangeMonitor> changeMonitors;
    private ArrayList<TrackSelectionListener> trackSelectionListeners;
    private TrackProxy selectedTrackProxy;
    private MidiDescriptor midiDescriptor;
    
    /**
     * Constructs a new MIDI sequence with the specified timing division type,
     * timing resolution, and number of tracks. The division type must be one of
     * the recognized MIDI timing types. For tempo-based timing, <code>divisionType</code>
     * is <code>PPQ</code> (pulses per quarter note) and the resolution is specified
     * in ticks per beat. For SMTPE timing, <code>divisionType</code> specifies the number
     * of frames per second and the resolution is specified in ticks per frame.
     * The sequence will contain no initial tracks.
     * The tracks may be retrieved for editing using the <code>getTracks()</code>
     * method. Additional tracks may be added, or existing tracks removed, using
     * <code>createTrack()</code> and <code>deleteTrack(javax.sound.midi.Track)</code>.
     * @param divisionType the timing division type (PPQ or one of the SMPTE types)
     * @param resolution the timing resolution
     * @param midiDescriptor The parent <code>MidiDescriptor</code>. May be <code>null</code>.
     * @throws InvalidMidiDataException
     */
    public SgMidiSequence(
            float divisionType, int resolution, MidiDescriptor midiDescriptor ) throws InvalidMidiDataException {
        super( divisionType, resolution );
        init( midiDescriptor );
    }

    /**
     * Constructs a new MIDI sequence with the specified timing division type,
     * timing resolution, and number of tracks. The division type must be one of
     * the recognized MIDI timing types. For tempo-based timing, <code>divisionType</code>
     * is <code>PPQ</code> (pulses per quarter note) and the resolution is specified
     * in ticks per beat. For SMTPE timing, <code>divisionType</code> specifies the number
     * of frames per second and the resolution is specified in ticks per frame.
     * The sequence will be initialized with the number of tracks specified by
     * <code>numTracks</code>. These tracks are initially empty (i.e. they contain
     * only the meta-event End of Track).
     * The tracks may be retrieved for editing using the <code>getTracks()</code>
     * method. Additional tracks may be added, or existing tracks removed, using
     * <code>createTrack()</code> and <code>deleteTrack(javax.sound.midi.Track)</code>.
     * @param divisionType the timing division type (PPQ or one of the SMPTE types)
     * @param resolution the timing resolution
     * @param numTracks the initial number of tracks in the sequence.
     * @parem midiDescriptor The parent <code>MidiDescriptor</code>. May be <code>null</code>.
     * @throws InvalidMidiDataException
     */
    public SgMidiSequence( float divisionType, int resolution, int numTracks, MidiDescriptor midiDescriptor )
        throws InvalidMidiDataException {

        super( divisionType, resolution, numTracks );
        init( midiDescriptor );
    }
    
    private void init( MidiDescriptor midiDescriptor ) {
        this.midiDescriptor = midiDescriptor;
        changeMonitors = new ArrayList<MidiChangeMonitor>();
        trackSelectionListeners = new ArrayList<TrackSelectionListener>();
        Track[] tracks = getTracks();
        trackProxies = new ArrayList<TrackProxy>( tracks.length );
        for (int i = 0; i < tracks.length; i++) {
            TrackProxy tp = new TrackProxy( tracks[i], this );
            trackProxies.add( tp );
            tp.addedToParent();
        }
        selectedTrackProxy = null;
    }
    
    /**
     * Returns the parent <code>MidiDescriptor</code>, or <code>null</code>.
     * @return The parent <code>MidiDescriptor</code>, or <code>null</code> if
     * no parent <code>MidiDescriptor</code> is associated with this <code>SgMidiSequence</code>.
     */
    public MidiDescriptor getMidiDescriptor() {
        return midiDescriptor;
    }
    
    /**
     * Gets this sequence's temp in Microseconds Per Quarternote.
     * @return The currently set tempo, or <code>-1</code> if unknown.
     */
    public float getTempoInMpq() {
        TrackProxy[] tracks = getTrackProxies();
        for (int i = 0; i < tracks.length; i++) {
            float tempo = tracks[i].getTempoInMPQ();
            if (tempo >= 0) {
                return tempo;
            }
        }
        return -1;
    }
    
    /**
     * Gets the number of microseconds per tact at the current tempo.
     * @return The number of microseconds per tact, or <code>-1</code> if unknown.
     */
    public float getMicrosecondsPerTact() {
        float tempo = getTempoInMpq();
        if (tempo >= 0) {
            return tempo * 4f;
        } else {
            if (divisionType == PPQ) {
                return resolution * 4f;
            }
        }
        return -1;
    }
    
    
    /**
     * Gets the number of ticks per tact at the current tempo.
     * @return The number of ticks per tact, or <code>-1</code> if unknown.
     */
    public int getTicksPerTact() {
        
        return 100;
    }
    
    /**
     * Returns <code>true</code> if and only if this <code>SgMidiSequence</code>
     * contains the given <code>TrackProxy</code>.
     * @param trackProxy The <code>TrackProxy</code> to be checked.
     * @return <code>true</code> if this <code>SgMidiSequence</code> contains
     * the given <code>TrackProxy</code>, <code>false</code> otherwise.
     */
    public boolean containsTrackProxy( TrackProxy trackProxy ) {
        return (trackProxies.contains( trackProxy ));
    }
    
    /**
     * Gets all contained MIDI tracks as a <code>TrackProxy</code> array.
     * @return A <code>TrackProxy</code> array.
     */
    public TrackProxy[] getTrackProxies() {
        synchronized (trackProxies) {
            TrackProxy[] tps = new TrackProxy[trackProxies.size()];
            for (int i = 0; i < tps.length; i++) {
                tps[i] = (TrackProxy) trackProxies.get( i );
            }
            return tps;
        }
    }
    
    public Track createTrack() {
        return createTrack( null );
    }

    public Track createTrack( Object who ) {
        Track t = super.createTrack();
        TrackProxy tp = new TrackProxy( t, this );
        synchronized (trackProxies) {
            trackProxies.add( tp );
        }
        fireTrackAdded( tp, who );
        tp.addedToParent();
        return t;
    }
    
    public boolean deleteTrackProxy( TrackProxy trackProxy ) {
        return deleteTrack( trackProxy.getTrack(), null );
    }
    
    public boolean deleteTrackProxy( TrackProxy trackProxy, Object who ) {
        return deleteTrack( trackProxy.getTrack(), who );
    }

    public boolean deleteTrack( Track track ) {
        return deleteTrack( track, null );
    }

    private boolean deleteTrack( Track track, Object who ) {
        boolean b = super.deleteTrack( track );
        int removeIndex = -1;
        if (b) {
            TrackProxy removeTp = null;
            synchronized (trackProxies) {
                for (int i = 0; i < trackProxies.size(); i++) {
                    TrackProxy tp = trackProxies.get( i );
                    if (tp.getTrack() == track) {
                        // remove persistent track properties from MidiDescriptor
                        if (midiDescriptor != null) {
                            String[] s = midiDescriptor.getPersistentClientPropertyNames( tp );
                            if (s != null) {
                                for (int j = 0; j < s.length; j++) {
                                    midiDescriptor.removePersistentClientProperty( tp, s[j] );
                                }
                            }
                        }
                        removeIndex = i;
                        removeTp = tp;
                    }
                }
            }
            if (removeIndex > -1) {
                // remove track from this sequence
                synchronized (trackProxies) {
                    trackProxies.remove( removeIndex );
                }
                midiDescriptor.trackRemovedAt( removeIndex );
                fireTrackRemoved( removeTp, who );
                removeTp.removedFromParent();
                
                return true;
            }
            return false;
        }
        return b;
    }
    
    /**
     * Returns a newly created <code>TrackProxy</code> whose embedded
     * <code>Track</code> object is created with the <code>createTrack()</code>
     * method.
     * @param who The <code>Object</code> that caused the change.
     * @return A <code>TrackProxy</code> with a newly created <code>Track</code>.
     */
    public TrackProxy createTrackProxy( Object who ) {
        return createTrackProxy( who, false );
    }
    
    /**
     * Private method that creates a <code>TrackProxy</code> and adds it.
     * @param who The <code>Object</code> that caused the change.
     * @param click If set to <code>true</code>, indicates that a temporary click
     *        track shall be created.
     * @return A <code>TrackProxy</code> with a newly created <code>Track</code>.
     */
    private TrackProxy createTrackProxy( Object who, boolean click ) {
        TrackProxy tp = new TrackProxy( super.createTrack(), this );
        if (!click) {
            synchronized (trackProxies) {
                trackProxies.add( tp );
            }
            fireTrackAdded( tp, who );
            tp.addedToParent();
        }
        return tp;
    }
    
    /**
     * Adds an existing <code>TrackProxy</code> to this <code>SgMidiSequence</code>.
     * @param trackProxy The <code>TrackProxy</code> to be added. if <code>null</code>,
     * a new empty <code>TrackProxy</code> is added.
     * @param who The <code>Object</code> that caused the change.
     * @return The added <code>TrackProxy</code>. If <code>trackProxy</code> is not
     * <code>null</code>, <code>trackProxy</code> is returned.
     */
    public TrackProxy addTrackProxy( TrackProxy trackProxy, Object who ) {
        if (trackProxy == null) {
            return createTrackProxy( who );
        } else {
            Track t = super.createTrack();
            copyEvents( trackProxy.getTrack(), t, false, false );
            trackProxy.setTrack( t );
            synchronized (trackProxies) {
                trackProxies.add( trackProxy );
            }
            fireTrackAdded( trackProxy, who );
            trackProxy.addedToParent();
        }
        return trackProxy;
    }
    
    /**
     * Copies the MIDI events from the source Track to the destination Track.
     * @param from The source track.
     * @param to The destination track
     * @param clearDestination If set to <code>true</code>, the destination track
     *        is cleared (all events are removed) before the copy operation.
     * @param clearSource If set to <code>true</code>, copied events are
     *        removed from the source track.
     */
    static void copyEvents( Track from, Track to, boolean clearDestination, boolean clearSource ) {
        if (clearDestination) {
            clear( to );
        }
        // the end of track event is required by the MIDI API!
        MidiEvent endEvent = from.get( from.size() - 1 );
        to.add( endEvent );
        for (int i = 0; i < from.size() - 1; i++) {
            MidiEvent event = from.get( i );
            to.add( event );
        }
        if (clearSource) {
            clear( from );
        }
    }
    
    /**
     * Clear the given track (removes all events).
     * @param track The track to be cleared.
     */
    static void clear( Track track ) {
        while (track.size() > 0) { track.remove( track.get( track.size() - 1 ) ); }
    }
    
    /**
     * Inserts a new <code>TrackProxy</code> at the specified index into this
     * sequence.<br>
     * <b>Note:</b> This method can be <i>extremely</i> expensive, since the
     * <code>javax.sound.midi</code> API on which SoundsGood MIDI implementations are
     * based does not support MIDI tracks to be swapped on a sequence. Therefor, all
     * physical MIDI track's contents have to be moved from lower index physical tracks
     * to higher index physical tracks, which can be a quite exhaustive operation.
     * Whenever you can avoid a call to this method, do it!
     * @param index The index. If the index is larger than
     * @param who The <code>Object</code> that caused the change.
     */
    public void insertTrackProxyAt( int index, Object who ) {
        insertTrackProxyAt( index, null, who );
    }
    
    /**
     * Inserts a given <code>TrackProxy</code> at the specified index into this
     * sequence.<br>
     * <b>Note:</b> This method can be <i>extremely</i> expensive, since the
     * <code>javax.sound.midi</code> API on which SoundsGood MIDI implementations are
     * based does not support MIDI tracks to be swapped on a sequence. Therefor, all
     * physical MIDI track's contents have to be moved from lower index physical tracks
     * to higher index physical tracks, which can be a quite exhaustive operation.
     * Whenever you can avoid a call to this method, do it!
     * @param index The index. If the index is larger than 
     * @param trackProxy The existing <code>TrackProxy</code> to insert.
     * @param who The <code>Object</code> that caused the change.
     */
    public void insertTrackProxyAt( int index, TrackProxy trackProxy, Object who ) {
        if (index > trackProxies.size()) { index = trackProxies.size(); }

        Track newTrack = super.createTrack();
        MidiEvent endEvent = null;
        if (newTrack.size() > 0) { endEvent = newTrack.get( 0 ); }
        Track[] tracks = super.getTracks();

        // copy from newly created track downwards
        for (int i = tracks.length - 1; i > index; i--) {
            copyEvents( tracks[i - 1], tracks[i], true, false );
        }
        // now, copy the track to insert into the physical track at index
        Track destinationTrack = tracks[index];
        if (trackProxy == null) {
            trackProxy = new TrackProxy( destinationTrack, this );
            clear( destinationTrack ); // empty track (new one)
            if (endEvent != null) {
                destinationTrack.add( endEvent );
            }
        } else {
            trackProxy.setParent( this );
            copyEvents( trackProxy.getTrack(), destinationTrack, true, false );
        }
        synchronized (trackProxies) {
            trackProxies.add( index, trackProxy );
        }
        // map physical tracks to correct TrackProxy objects
        for (int i = index; i < trackProxies.size(); i++) {
            trackProxies.get( i ).setTrack( tracks[i] );
        }
        if (midiDescriptor != null) {
            midiDescriptor.trackInsertedAt( index );
        }
        fireTrackAdded( trackProxy, who );
        trackProxy.addedToParent();
    }
    
    /**
     * Adds a <code>MidiChangeMonitor</code> that will be updated after every
     * change to this <code>SgMidiSequence</code> after this method call.
     * @param monitor The <code>MidiChangeMonitor</code> to be added. If it has
     *        already been added, this method does nothing.
     */
    public void addMidiChangeMonitor( MidiChangeMonitor monitor ) {
        if (!changeMonitors.contains( monitor )) {
            synchronized (changeMonitors) {
                changeMonitors.add( monitor );
            }
        }
    }
    
    /**
     * Removes a <code>MidiChangeMonitor</code> that will no more be updated
     * on changes change to this <code>SgMidiSequence</code> after this method call.
     * @param monitor The <code>MidiChangeMonitor</code> to be removed. If it is not
     *        registered as change monitor, this method does nothing.
     */
    public void removeMidiChangeMonitor( MidiChangeMonitor monitor ) {
        synchronized (changeMonitors) {
            changeMonitors.remove( monitor );
        }
    }
    
    /**
     * Adds a <code>TrackSelectionListener</code> that will be updated after every
     * track selection change to this <code>SgMidiSequence</code> after this method call.
     * @param listener The <code>TrackSelectionListener</code> to be added. If it has
     *        already been added, this method does nothing.
     */
    public void addTrackSelectionListener( TrackSelectionListener listener ) {
        if (!trackSelectionListeners.contains( listener )) {
            synchronized (trackSelectionListeners) {
                trackSelectionListeners.add( listener );
            }
        }
    }
    
    /**
     * Removes a <code>TrackSelectionListener</code> that will no more be updated
     * on track selection changes to this <code>SgMidiSequence</code> after this method call.
     * @param listener The <code>TrackSelectionListener</code> to be removed. If it is not
     *        registered as TrackSelectionListener, this method does nothing.
     */
    public void removeTrackSelectionListener( TrackSelectionListener listener ) {
        synchronized (trackSelectionListeners) {
            trackSelectionListeners.remove( listener );
        }
    }
    
    void fireEventAdded( TrackProxy track, MidiEvent midiEvent, Object who ) {
        fireEventsAdded( track, new MidiEvent[] { midiEvent }, who );
    }
    
    void fireEventsAdded( TrackProxy track, MidiEvent[] midiEvents, Object who ) {
        //System.out.println( "fireEventsAdded()" );
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (changeMonitors) {
            for (int i = 0; i < changeMonitors.size(); i++) {
                EventRedirector erd = new MonitorEventRedirector(
                    true, changeMonitors.get( i ), track, midiEvents, who );
                eqh.addQueueEntry( erd, null );
            }
        }
        eqh.processEvents();
    }
    
    void fireEventRemoved( TrackProxy track, MidiEvent midiEvent, Object who ) {
        fireEventsRemoved( track, new MidiEvent[] { midiEvent }, who );
    }
    
    void fireEventsRemoved( TrackProxy track, MidiEvent[] midiEvents, Object who ) {
        //System.out.println( "fireEventsRemoved()" );
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (changeMonitors) {
            for (int i = 0; i < changeMonitors.size(); i++) {
                EventRedirector erd = new MonitorEventRedirector(
                    false, changeMonitors.get( i ), track, midiEvents, who );
                eqh.addQueueEntry( erd, null );
            }
        }
        eqh.processEvents();
    }
    
    void fireEventsChanged( TrackProxy track, MidiEvent[] midiEvents, Object who ) {
        //System.out.println( "fireEventsChanged()" );
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (changeMonitors) {
            for (int i = 0; i < changeMonitors.size(); i++) {
                EventRedirector erd = new MonitorEventRedirector(
                    true, false, changeMonitors.get( i ), track, midiEvents, who );
                eqh.addQueueEntry( erd, null );
            }
        }
        eqh.processEvents();
    }
    
    void fireTrackAdded( TrackProxy track, Object who ) {
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (changeMonitors) {
            for (int i = 0; i < changeMonitors.size(); i++) {
                EventRedirector erd = new MonitorEventRedirector(
                    true, false, false, changeMonitors.get( i ), track, who );
                eqh.addQueueEntry( erd, null );
            }
        }
        eqh.processEvents();
    }
    
    void fireTrackRemoved( TrackProxy track, Object who ) {
        if (track == selectedTrackProxy) {
            setSelectedTrackProxy( null, this );
        }
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (changeMonitors) {
            for (int i = 0; i < changeMonitors.size(); i++) {
                EventRedirector erd = new MonitorEventRedirector(
                    false, false, false, changeMonitors.get( i ), track, who );
                eqh.addQueueEntry( erd, null );
            }
        }
        eqh.processEvents();
    }
    
    void fireTrackLengthChanged( TrackProxy track, Object who ) {
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (changeMonitors) {
            for (int i = 0; i < changeMonitors.size(); i++) {
                EventRedirector erd = new MonitorEventRedirector(
                    false, true, false, changeMonitors.get( i ), track, who );
                eqh.addQueueEntry( erd, null );
            }
        }
        eqh.processEvents();
    }
    
    void fireTrackEventMapChanged( TrackProxy track, Object who ) {
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (changeMonitors) {
            for (int i = 0; i < changeMonitors.size(); i++) {
                EventRedirector erd = new MonitorEventRedirector(
                    false, false, true, changeMonitors.get( i ), track, who );
                eqh.addQueueEntry( erd, null );
            }
        }
        eqh.processEvents();
    }
    
    /**
     * Gets the given MIDI track's index within this <code>SgMidiSequence</code>.
     * @param track The track to get the index for.
     * @return The index, or -1 if the given track is not part of this sequence.
     */
    public int getIndexOf( TrackProxy track ) {
        synchronized (trackProxies) {
            for (int i = 0; i < trackProxies.size(); i++) {
                if (trackProxies.get( i ) == track) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * This method should not be called (except you know what you're doing).
     */
    public Track[] getTracks() {
        return super.getTracks();
    }
    
    /**
     * Gets the number of tracks contained by this sequence.
     * @return The total number of tracks.
     */
    public int getTrackCount() {
        return trackProxies.size();
    }
    
    
    /**
     * Gets the length in ticks of this <code>SgMidiSequence</code>.
     * @return The tick length, which is calculated as the maximum of all track lengths, as
     *         indicated by the end of track MIDI events.
     */
    public long getLength() {
        TrackProxy[] tps = getTrackProxies();
        if (tps == null) { return 0; }
        long len = 0;
        for (int i = 0; i < tps.length; i++) {
            if (tps[i] != null && tps[i].size() > 0) {
                len = Math.max( len, tps[i].get( tps[i].size() - 1 ).getTick() );
            }
        }
        return len;
    }
    
    /**
     * Gets the actual length in ticks of this <code>SgMidiSequence</code>.
     * @return The tick length, which is calculated as the maximum of all actual track lengths.
     *         Note that not the end of track events are relevant here, but the last MIDI event
     *         <b>before</b> the end of track events.
     */
    public long getActualLength() {
        TrackProxy[] tps = getTrackProxies();
        if (tps == null) { return 0; }
        long len = 0;
        for (int i = 0; i < tps.length; i++) {
            if (tps[i] != null && tps[i].size() > 1) {
                len = Math.max( len, tps[i].get( tps[i].size() - 2 ).getTick() );
            }
        }
        return len;
    }
    
    /**
     * Gets the currently selected <code>TrackProxy</code>.
     * @return The selected <code>TrackProxy</code>, or <code>null</code> if none is selected.
     */
    public TrackProxy getSelectedTrackProxy() {
        return selectedTrackProxy;
    }
    
    /**
     * Sets the selected <code>TrackProxy</code>.
     * @param selectedTrackProxy The <code>TrackProxy</code> that shall be selected.
     * @param who The object that caused the track selection change.
     */
    public void setSelectedTrackProxy( TrackProxy selectedTrackProxy, Object who ) {
        if (this.selectedTrackProxy == selectedTrackProxy) {
            return;
        }
        if (selectedTrackProxy != null && getIndexOf( selectedTrackProxy ) < 0) {
            throw new IllegalArgumentException( "TrackProxy is not part of SgMidiSequence" );
        }
        this.selectedTrackProxy = selectedTrackProxy;
        fireTrackSelectionChanged( new TrackSelectionEvent( who, this, selectedTrackProxy ) );
    }
    
    /**
     * Dispatches a <code>TrackSelectionEvent</code>.
     * @param e The event to be fired.
     */
    protected void fireTrackSelectionChanged( TrackSelectionEvent e ) {
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (trackSelectionListeners) {
            for (int i = 0; i < trackSelectionListeners.size(); i++) {
                EventRedirector erd = new AbstractEventRedirector( trackSelectionListeners.get( i ) ) {
                    public void redirectEvent( EventObject e ) {
                        ((TrackSelectionListener) getListener()).trackSelectionChanged(
                                ((TrackSelectionEvent) e) );
                    }
                };
                eqh.addQueueEntry( erd, e );
            }
        }
        eqh.processEvents();
    }
    
    class MonitorEventRedirector implements EventRedirector {
        boolean changed;
        boolean added;
        boolean lengthChanged;
        boolean eventMapChanged;
        MidiChangeMonitor cm;
        TrackProxy track;
        MidiEvent[] midiEvents;
        Object who;
        /// constructor multiple MIDI events
        MonitorEventRedirector(
            boolean changed,
            boolean added,
            MidiChangeMonitor cm,
            TrackProxy track,
            MidiEvent[] midiEvents,
            Object who ) {

            this.changed = changed;
            this.added = added;
            this.cm = cm;
            this.track = track;
            this.midiEvents = midiEvents;
            this.who = who;
        }
        MonitorEventRedirector(
            boolean added, MidiChangeMonitor cm, TrackProxy track, MidiEvent[] midiEvents, Object who ) {
            this( false, added, cm, track, midiEvents, who );
        }
        /// constructor for MIDI tracks
        MonitorEventRedirector(
            boolean added, boolean lengthChanged,
            boolean eventMapChanged, MidiChangeMonitor cm, TrackProxy track, Object who ) {
            this.changed = false;
            this.added = added;
            this.lengthChanged = lengthChanged;
            this.eventMapChanged = eventMapChanged;
            this.cm = cm;
            this.track = track;
            this.who = who;
            this.midiEvents = null;
        }
        public void redirectEvent( EventObject e ) {
            if (changed) {
                cm.midiEventsChanged( SgMidiSequence.this, track, midiEvents, who );
            } else if (added) {
                if (midiEvents != null) {
                    cm.midiEventsAdded( SgMidiSequence.this, track, midiEvents, who );
                } else {
                    cm.midiTrackAdded( SgMidiSequence.this, track, who );
                }
            } else {
                if (eventMapChanged) {
                    cm.midiTrackEventMapChanged( SgMidiSequence.this, track, who );
                } else if (lengthChanged) {
                    cm.midiTrackLengthChanged( SgMidiSequence.this, track, who );
                } else {
                    if (midiEvents != null) {
                        cm.midiEventsRemoved( SgMidiSequence.this, track, midiEvents, who );
                    } else {
                        cm.midiTrackRemoved( SgMidiSequence.this, track, who );
                    }
                }
            }
        }
        
    }
}