/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 20.11.2003
 */
package com.jonasreese.sound.sg.midi.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import com.jonasreese.sound.sg.RecorderException;
import com.jonasreese.sound.sg.RecorderListener;
import com.jonasreese.sound.sg.SessionElementEvent;
import com.jonasreese.sound.sg.SessionElementListener;
import com.jonasreese.sound.sg.SessionEvent;
import com.jonasreese.sound.sg.SessionListener;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.SgProperties;
import com.jonasreese.sound.sg.midi.Metronome;
import com.jonasreese.sound.sg.midi.MidiChannelMap;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.MidiDeviceDescriptor;
import com.jonasreese.sound.sg.midi.MidiDeviceList;
import com.jonasreese.sound.sg.midi.MidiDeviceMap;
import com.jonasreese.sound.sg.midi.MidiRecorder;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.midi.MidiUpdatable;
import com.jonasreese.sound.sg.midi.SgMidiSequence;
import com.jonasreese.sound.sg.midi.TrackProxy;
import com.jonasreese.sound.sg.midi.edit.RecordEdit;
import com.jonasreese.util.ParamRunnable;
import com.jonasreese.util.Updatable;

/**
 * <b>
 * This class is a generic implementation of the <code>MidiRecorder</code> interface.
 * </b>
 * @author jreese
 */
public class MidiRecorderImpl implements MidiRecorder {
    
    static final String PLAYBACK_MUTE_PROPERTY = "playback.mute";
    static final String PLAYBACK_SOLO_PROPERTY = "playback.solo";
    static final String PLAYBACK_OUTPUT_MAP_PROPERTY = "playback.outputmap";
    static final String RECORD_ENABLED_PROPERTY = "record.enabled";
    static final String RECORD_INPUT_MAP_PROPERTY = "record.inputmap";
    static final String LEFT_MARKER_TICK_PROPERTY = "marker.left.tick";
    static final String RIGHT_MARKER_TICK_PROPERTY = "marker.right.tick";
    static final String PLAY_MARKER_TICK_PROPERTY = "marker.play.tick";
    
    private static final int DEFAULT_MODE = 0;
    private static final int FAST_FORWARD_MODE = 1;
    private static final int FAST_FORWARD_RUNNING_MODE = 2;
    private static final int FAST_BACKWARD_MODE = 3;
    private static final int FAST_BACKWARD_RUNNING_MODE = 4;
    private static final int PLAY_FROM_LEFT_TO_RIGHT_MARKER_MODE = 5;
    private static final int PLAY_FROM_LEFT_MARKER_MODE = 6;
    private static final int PLAY_TO_RIGHT_MARKER_MODE = 7;
    
    private Sequencer sequencer;
    private RecordConnector recordingConnector;
    private PlaybackReceiver playbackReceiver;
    private RecordConnector loopbackConnector;
    private Transmitter sequencerTransmitter;

    private boolean recordLoopbackEnabled;
    private boolean permanentLoopbackEnabled;
    
    private boolean needsInputMapUpdate;
    private boolean needsOutputMapUpdate;
    
    private boolean stopClickOnStop;
    
    private Thread shutdownHook;
    
    private DeviceThread deviceThread;
    private Object threadSync;
    
    private ArrayList<RecorderListener> listeners;
    private ArrayList<MidiUpdatable> midiUpdatables;
    private ArrayList<Receiver> midiOutputReceivers;
    private MidiDescriptor descriptor;
    
    private long leftMarkerTick;
    private long rightMarkerTick;
    private long currentTick;

    private boolean muteNextPlayback;
    private boolean looping;
    private int mode;
    private Runnable fastRunnable;

    private SessionListener sessionListener;
    private SessionElementListener sessionElementListener;
    private PropertyChangeListener deviceChangeListener;
    
    private HashMap<TrackProxy,int[]> recordTracks;
    private HashSet<TrackProxy> mutedTracks;
    private HashSet<TrackProxy> soloTracks;
    
    private ArrayList<Object> devicesToClose;
    
    private HashMap<TrackProxy,MidiDeviceMap> inputMap;
    private MidiDeviceMap midiOutputMap;
    
    private boolean inputStateInitialized;
    private boolean outputStateInitialized;
    
    private MetronomeImpl metronome;
    private CountInReceiver countInReceiver;
    
    private Updatable propertyHook;

    /**
     * Constructs a new <code>MidiRecorderImpl</code>.
     * @param 
     */
    public MidiRecorderImpl( MidiDescriptor descriptor ) {
        this.descriptor = descriptor;

        leftMarkerTick = -1;
        rightMarkerTick = -1;
        currentTick = -1;
        
        listeners = new ArrayList<RecorderListener>();
        midiUpdatables = new ArrayList<MidiUpdatable>();
        midiOutputReceivers = new ArrayList<Receiver>();
        devicesToClose = new ArrayList<Object>();
        
        shutdownHook = null;
        deviceThread = null;
        threadSync = new Object();
        
        metronome = null;

        recordLoopbackEnabled = SgEngine.getInstance().getProperties().getDefaultRecordLoopbackEnabled();
        permanentLoopbackEnabled = false; //SgEngine.getInstance().getProperties().getDefaultPermanentLoopbackEnabled();
        if (permanentLoopbackEnabled) {
            permanentLoopbackEnabled = false;
            try {
                setLoopbackEnabled( true );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        muteNextPlayback = false;
        
        needsInputMapUpdate = false;
        needsOutputMapUpdate = false;
        
        stopClickOnStop = false;

        sessionListener = new SessionListener() {
            public void sessionAdded( SessionEvent e ) {
            }
            public void sessionRemoved( SessionEvent e ) {
                if (e.getSession() == MidiRecorderImpl.this.descriptor.getSession()) {
                    System.out.println( "MidiRecorderImpl : parent session removed" );
                    SgEngine.getInstance().removeSessionListener( sessionListener );
                    SgEngine.getInstance().getProperties().removePropertyChangeListener( deviceChangeListener );
                    MidiRecorderImpl.this.descriptor.getSession().removeSessionElementListener(
                        sessionElementListener );
                    closeAll();
                }
            }
            public void sessionActivated( SessionEvent e ) {
            }
            public void sessionDeactivated( SessionEvent e ) {
            }
        };
        deviceChangeListener = new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent e ) {
                try {
                    //System.out.println( "MidiRecorderImpl.deviceChangeListener.propertyChange(" + e.getPropertyName() +
                    //        ") : " + e.getNewValue() + " - " + e.getSource() );
                    if (e.getSource() instanceof SgProperties) {
                        if ("midiInputDeviceList".equals( e.getPropertyName() )) {
                            boolean loopbackConnected = MidiRecorderImpl.this.permanentLoopbackEnabled;
                            if (isRecording()) {
                                stop();
                            }
                            closeAll();
                            if (loopbackConnected) {
                                connectLoopback();
                            }
                        } else if ("midiOutputDeviceList".equals( e.getPropertyName() )) {
                            boolean loopbackConnected = MidiRecorderImpl.this.permanentLoopbackEnabled;
                            stop();
                            closeAll();
                            if (loopbackConnected) {
                                connectLoopback();
                            }
                        } else if ("midiSequencerInfo".equals( e.getPropertyName() )) {
                            stop();
                            closeAll();
                        }
                    }
                } catch (Throwable ignored) {
                    ignored.printStackTrace();
                    sequencer = null;
                }
            }
        };
        
        sessionElementListener = new SessionElementListener() {
            public void elementAdded( SessionElementEvent e ) {
            }
            public void elementRemoved( SessionElementEvent e ) {
                if (e.getSessionElement() == MidiRecorderImpl.this.descriptor) {
                    System.out.println( "MidiRecorderImpl : parent session element removed" );
                    SgEngine.getInstance().removeSessionListener( sessionListener );
                    SgEngine.getInstance().getProperties().removePropertyChangeListener( deviceChangeListener );
                    MidiRecorderImpl.this.descriptor.getSession().removeSessionElementListener(
                        sessionElementListener );
                    closeAll();
                    if (metronome != null) {
                        metronome.stop();
                    }
                }
            }
        };
        
        recordTracks = new HashMap<TrackProxy, int[]>();
        mutedTracks = new HashSet<TrackProxy>();
        soloTracks = new HashSet<TrackProxy>();
        
        fastRunnable = new Runnable() {
            // fast forward/backward thread
            public void run() {
                boolean back = isInFastBackwardMode();
                int[] speeds = new int[] { 1, 20, 30, 40, 60, 85 };
                int accel = 0;
                long max = 0;
                int resolution = 50;
                try {
                    if (MidiRecorderImpl.this.descriptor != null && MidiRecorderImpl.this.descriptor.getSequence() != null) {
                        max = MidiRecorderImpl.this.descriptor.getSequence().getActualLength();
                        if (MidiRecorderImpl.this.descriptor.getSequence().getDivisionType() == SgMidiSequence.PPQ) {
                            int r = MidiRecorderImpl.this.descriptor.getSequence().getResolution();
                            if (r > 0) {
                                resolution = r;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (int count = 0; (back ? isInFastBackwardMode() : isInFastForwardMode()); count++) {
                    long time = System.currentTimeMillis();
                    if (count < 1) { accel = 0; }
                    else if (count <= 10) { accel = 1; }
                    else if (count <= 20) { accel = 2; }
                    else if (count <= 30) { accel = 3; }
                    else if (count <= 50) { accel = 4; }
                    else { accel = 5; }
                    int val = (int) (speeds[accel] * (resolution / 50.0));
                    if (back) { val = -val; }
                    long tick = getTickPosition() + val;
                    if (tick < 0) {
                        tick = 0;
                    }
                    if (tick > max) {
                        tick = max;
                    }
                    setTickPosition( tick );
                    try {
                        long t = (accel == 0 ? 200 : 70) - System.currentTimeMillis() + time;
                        if (t <= 0) { t = 1; }
                        Thread.sleep( t );
                    } catch (InterruptedException e) {}
                    //System.out.println( (back ? "back" : "forward") + count );
                }
            }
        };

        propertyHook = new Updatable() {
            public void update( Object o ) {
                System.out.println( "MidiRecorderImpl.propertyHook: persisting ticks" );
                MidiRecorderImpl.this.descriptor.putPersistentClientProperty(
                        LEFT_MARKER_TICK_PROPERTY, Long.toString( leftMarkerTick ) );
                MidiRecorderImpl.this.descriptor.putPersistentClientProperty(
                        RIGHT_MARKER_TICK_PROPERTY, Long.toString( rightMarkerTick ) );
                MidiRecorderImpl.this.descriptor.putPersistentClientProperty(
                        PLAY_MARKER_TICK_PROPERTY, Long.toString( getTickPosition() ) );
            }
        };
        descriptor.addPropertyHook( propertyHook );

        inputMap = new HashMap<TrackProxy,MidiDeviceMap>();
        inputStateInitialized = false;
        outputStateInitialized = false;
    }

    /**
     * This method is called after the <code>MidiDescriptor</code> is completely
     * initialized. Then, this instance can be completely initialized, too...
     */
    public void init() {
        // add required listeners
        SgEngine.getInstance().addSessionListener( sessionListener );
        SgEngine.getInstance().getProperties().addPropertyChangeListener( deviceChangeListener );
        descriptor.getSession().addSessionElementListener( sessionElementListener );

        setLeftMarkerTick( descriptor.getPersistentClientProperty( -1l, LEFT_MARKER_TICK_PROPERTY ) );
        setRightMarkerTick( descriptor.getPersistentClientProperty( -1l, RIGHT_MARKER_TICK_PROPERTY ) );
        setTickPosition( descriptor.getPersistentClientProperty( 0l, PLAY_MARKER_TICK_PROPERTY ) );
    }
    
    public void addRecorderListener( RecorderListener l ) {
        synchronized (listeners) {
            if (!listeners.contains( l )) {
                listeners.add( l );
            }
        }
    }

    public void removeRecorderListener( RecorderListener l ) {
        synchronized (listeners) {
            listeners.remove( l );
        }
    }

    /**
     * This method is invoked by the <code>MidiDescriptor</code> which registers a
     * <code>MidiChangeMonitor</code>.
     */
    public void midiTrackAdded( SgMidiSequence sequence, TrackProxy track, Object changeObj ) {
    }

    /**
     * This method is invoked by the <code>MidiDescriptor</code> which registers a
     * <code>MidiChangeMonitor</code>.
     */
    public void midiTrackRemoved( SgMidiSequence sequence, TrackProxy track, Object changeObj ) {
        if (isRecordEnabled( track )) {
            disableRecord( track );
        }
        inputMap.remove( track );
        
        // TODO: update other states (solo, mute), too!
        mutedTracks.remove( track );
        soloTracks.remove( track );
    }

    public void midiTrackLengthChanged( SgMidiSequence sequence, TrackProxy track, Object changeObj ) {
    }
    
    private Sequencer getSeq() throws RecorderException {
        if (sequencer != null) { return sequencer; }
        MidiDevice.Info sequencerInfo =
            SgEngine.getInstance().getProperties().getMidiSequencerInfo();
        MidiDevice seqDev;
        try {
            if (sequencerInfo == null ||
                (!((seqDev = MidiSystem.getMidiDevice( sequencerInfo )) instanceof Sequencer))) {
                throw new RecorderException(
                    SgEngine.getInstance().getResourceBundle().getString(
                        "system.noMidiSequencerSelected" ) );
            }
        } catch (MidiUnavailableException muaex) {
            throw new RecorderException( muaex );
        }

        sequencer = (Sequencer) seqDev;
        System.out.println( "Using sequencer: " + sequencer.getClass().getName() );
        return sequencer;
    }

    private boolean isUpdatingRequired() {
        return (sequencer != null && sequencer.isRunning());
    }
    
    private synchronized void openSequencer() throws RecorderException {
        if (sequencer != null && sequencer.isOpen()) { return; }
        if (shutdownHook == null) {
            shutdownHook = new Thread() {
                public void run() {
                    shutdownHook = null;
                    closeAll();
                }
            };
            Runtime.getRuntime().addShutdownHook( shutdownHook );
        }
        try {
            Sequencer sequencer = getSeq();
            if (sequencer != null && !sequencer.isOpen()) {
                sequencer.open();
    
                // initialize underlying sequencer and midi device
                Sequence seq = descriptor.getSequence();
                sequencer.setSequence( seq );
                if (currentTick >= 0) {
                    sequencer.setTickPosition( currentTick );
                }
                currentTick = -1;

                for (TrackProxy track : mutedTracks) {
                    sequencer.setTrackMute( getSequence().getIndexOf( track ), true );
                }
                for (TrackProxy track : soloTracks) {
                    sequencer.setTrackSolo( getSequence().getIndexOf( track ), true );
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RecorderException( e.getMessage() );
        }
    }
    
    /**
     * Connects the playback.
     * @throws RecorderException
     */
    protected synchronized void connectPlayback() throws RecorderException {
        System.out.println("MidiRecorderImpl.connectPlayback()");
        if (playbackReceiver == null) {
            playbackReceiver = new PlaybackReceiver( null );
            devicesToClose.add( playbackReceiver );
        }
        if (sequencerTransmitter == null) {
            try {
                sequencerTransmitter = getSeq().getTransmitter();
            } catch (MidiUnavailableException e) {
                throw new RecorderException( e );
            }
            devicesToClose.add( sequencerTransmitter );
        }
        sequencerTransmitter.setReceiver( playbackReceiver );
        openSequencer();
    }
    
    protected synchronized void disconnectPlayback() {
        System.out.println("MidiRecorderImpl.disconnectPlayback()");
        if (sequencerTransmitter != null) {
            sequencerTransmitter.setReceiver( null );
        }
    }
    
    protected void connectLoopback() throws RecorderException {
        System.out.println( "MidiRecorder.connectLoopback()" );
        openSequencer();
        // set rec device transmitter --> output device receiver
        if (needsInputMapUpdate || needsOutputMapUpdate) {
            needsInputMapUpdate = false;
            needsOutputMapUpdate = false;
            closeAll();
        }
        if (loopbackConnector == null) {
            loopbackConnector = new RecordConnector( true );
            loopbackConnector.open();
            devicesToClose.add( loopbackConnector );
        }
        loopbackConnector.connect();
    }
    
    protected void disconnectLoopback() {
        System.out.println( "MidiRecorder.disconnectLoopback()" );
        // disconnect/close loopback
        if (loopbackConnector != null) {
            loopbackConnector.disconnect();
        }
    }
    
    /**
     * Gets the tick where the left marker is located.
     * @return Returns the left marker tick position.
     */
    public long getLeftMarkerTick() {
        if (leftMarkerTick < 0) {
            return 0;
        }
        return leftMarkerTick;
    }
    /**
     * Sets the tick where the left marker is located.
     * @param leftMarkerTick The leftMarkerTick to set.
     */
    public void setLeftMarkerTick( long leftMarkerTick ) {
        if (leftMarkerTick != this.leftMarkerTick && !isPlaying()) {
            this.leftMarkerTick = leftMarkerTick;
            descriptor.getSession().setChanged( true );
            fireDeviceUpdate( MidiUpdatable.LEFT_MARKER_TICK );
        }
    }
    /**
     * Removes the left marker (sets it's tick position to -1).
     */
    public void removeLeftMarker() {
        setLeftMarkerTick( -1 );
    }
    /**
     * Gets the tick where the right marker is located.
     * @return Returns the right marker tick position.
     */
    public long getRightMarkerTick() {
        if (rightMarkerTick < 0) {
            SgMidiSequence sequence = null;
            if (descriptor != null) {
                try {
                    sequence = descriptor.getSequence();
                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sequence != null) {
                return sequence.getLength();
            } else {
                return 0;
            }
        }
        return rightMarkerTick;
    }
    /**
     * Sets the tick where the right marker is located.
     * @param rightMarkerTick The rightMarkerTick to set.
     */
    public void setRightMarkerTick( long rightMarkerTick ) {
        if (rightMarkerTick != this.rightMarkerTick && !isPlaying()) {
            this.rightMarkerTick = rightMarkerTick;
            descriptor.getSession().setChanged( true );
            fireDeviceUpdate( MidiUpdatable.RIGHT_MARKER_TICK );
        }
    }
    /**
     * Removes the right marker (sets it's tick position to -1).
     */
    public void removeRightMarker() {
        setRightMarkerTick( -1 );
    }
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#setSequence(javax.sound.midi.Sequence)
     */
    public void setSequence( Sequence s ) throws InvalidMidiDataException {
        //System.out.println( "MidiRecorderImpl.setSequence()" );
        try {
            getSeq().setSequence( s );
        }
        catch (RecorderException e) {}
    }
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#getSequence()
     */
    private SgMidiSequence getSequence() {
        try {
            return descriptor.getSequence();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void start() {
        boolean muteNextPlayback = this.muteNextPlayback;
        this.muteNextPlayback = false;
        if (isInFastForwardMode()) { stopFastForward(); }
        if (isInFastBackwardMode()) { stopFastBackward(); }
        initOutputState();
        //System.out.println( "MidiRecorderImpl.start()" );
        try {
            if (needsInputMapUpdate || needsOutputMapUpdate) {
                needsInputMapUpdate = false;
                needsOutputMapUpdate = false;
                closeAll();
                if (permanentLoopbackEnabled) {
                    connectLoopback();
                }
            }

            connectPlayback();
            playbackReceiver.mute = muteNextPlayback;
            
            if (mode == PLAY_FROM_LEFT_TO_RIGHT_MARKER_MODE || mode == PLAY_FROM_LEFT_MARKER_MODE) {
                setTickPosition( getLeftMarkerTick() );
            }
            if (sequencer != null) {
                // set loop parameters
                if (looping) {
                    // check if sequence is long enough to play to right marker, stretch if necessary
                    if (descriptor.getSequence().getLength() < getRightMarkerTick()) {
                        try {
                            TrackProxy[] trackProxies = descriptor.getSequence().getTrackProxies();
                            for (int i = 0; i < trackProxies.length; i++) {
                                trackProxies[i].setLength( getRightMarkerTick(), this );
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    sequencer.setLoopStartPoint( getTickPosition() );
                    sequencer.setLoopEndPoint( getRightMarkerTick() );
                    sequencer.setLoopCount( Sequencer.LOOP_CONTINUOUSLY );
                } else {
                    sequencer.setLoopStartPoint( 0 );
                    sequencer.setLoopEndPoint( -1 );
                    sequencer.setLoopCount( 0 );
                }
                sequencer.start();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            looping = false;
        }
        startUpdaterThread();
    }
    
    /**
     * Gets the playing mode status info for <i>from left to right marker</i>.
     * @return <code>true</code> if this <code>MidiRecorderImpl</code> is playing
     *         a sequence from the left to the right marker position.
     */
    public boolean isPlayingFromLeftToRightMarker() {
        return (mode == PLAY_FROM_LEFT_TO_RIGHT_MARKER_MODE);
    }
    
    /**
     * Starts playing from the left marker position to the right marker position.
     * @throws RecorderException if the midi output device could not
     *         be prepared.
     */
    public void playFromLeftToRightMarker() throws RecorderException {
        mode = PLAY_FROM_LEFT_TO_RIGHT_MARKER_MODE;
        start();
    }
    
    /**
     * Starts playing from the left marker position to the end of the associated sequence.
     * @throws RecorderException if the midi output device could not
     *         be prepared.
     */
    public void playFromLeftMarker() throws RecorderException {
        mode = PLAY_FROM_LEFT_MARKER_MODE;
        start();
    }

    /**
     * Gets the playing mode status info for <i>from left marker</i>.
     * @return <code>true</code> if this <code>MidiRecorder</code> is playing
     *         a sequence from the left marker position to the end.
     */
    public boolean isPlayingFromLeftMarker() {
        return (mode == PLAY_FROM_LEFT_MARKER_MODE);
    }

    /**
     * Starts playing from the current position to the right marker position
     * or to the end if no right marker position is set..
     * @throws RecorderException if the midi output device could not
     *         be prepared.
     */
    public void playToRightMarker() throws RecorderException {
        mode = PLAY_TO_RIGHT_MARKER_MODE;
        start();
    }

    /**
     * Gets the playing mode status info for <i>to right marker</i>.
     * @return <code>true</code> if this <code>MidiRecorder</code> is playing
     *         a sequence from the starting position to the right marker position.
     */
    public boolean isPlayingToRightMarker() {
        return (mode == PLAY_TO_RIGHT_MARKER_MODE);
    }
    
    public synchronized void preparePlayback() {
        if (isPlaying() || isRecording()) {
            return;
        }
        try {
            if (needsInputMapUpdate || needsOutputMapUpdate) {
                needsInputMapUpdate = false;
                needsOutputMapUpdate = false;
                closeAll();
                if (permanentLoopbackEnabled) {
                    connectLoopback();
                }
            }
            connectPlayback();
            if (sequencer != null && sequencer.isOpen()) {
                sequencer.stop();
            }
        } catch (RecorderException e) {
            e.printStackTrace();
        }
    }

    /**
     * Has the same effect as the <code>start()</code> method.
     * @throws RecorderException if the midi output device could not
     *         be prepared.
     */
    public void play() throws RecorderException {
        start();
    }
    
    public synchronized void loop() throws RecorderException {
        looping = true;
        start();
    }

    public boolean isLooping() {
        return looping;
    }

    public synchronized void loopFromLeftToRightMarker() throws RecorderException {
        looping = true;
        playFromLeftToRightMarker();
    }

    public boolean isLoopingFromLeftToRightMarker() {
        return (looping && mode == PLAY_FROM_LEFT_TO_RIGHT_MARKER_MODE);
    }

    public void playSingleNote( MidiEvent on, MidiEvent off ) throws RecorderException {
        if (isRecording()) {
            return;
        }
        connectPlayback();
        //System.out.println( "playMidiNote()" );
        playbackReceiver.send( on.getMessage(), 0 );
        if (off != null) {
            long ticks = off.getTick() - on.getTick();
            double d = (double) getMicrosecondLength() / (double) getTickLength();
            long microseconds = (long) (ticks * d);
            new Thread( new ParamRunnable( new Object[] { new Long( microseconds ), off } ) {
                public void run() {
                    try {
                        Thread.sleep( ((Long) ((Object[]) getParameter())[0]).longValue() / 1000 );
                    } catch (InterruptedException ignored) {}
                    try {
                        playSingleNote( (MidiEvent) ((Object[]) getParameter())[1], null );
                    } catch (RecorderException e) {
                        e.printStackTrace();
                    }
                }
            } ).start();
        }
    }

    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#stop()
     */
    public synchronized void stop() {
        System.out.println( "MidiRecorder.stop() at " + getTickPosition() );
        if (stopClickOnStop) {
            getMetronome().stop();
            stopClickOnStop = false;
        }
        getMetronome().removeMidiOutputReceiver( countInReceiver );
        countInReceiver.count = -1;
        if (isInFastForwardMode()) { stopFastForward(); }
        if (isInFastBackwardMode()) { stopFastBackward(); }
        mode = DEFAULT_MODE;
        looping = false;
        //System.out.println( "MidiRecorderImpl.stop()" );

        if (isRecording()) {
            System.out.println( "currently recording, stopping..." );
            stopRecording();
        }
        Sequencer sequencer = this.sequencer;
        if (sequencer != null && sequencer.isOpen()) {
            sequencer.stop();
        }
        //closeAll();
    }
    /*
     * (non-Javadoc)
     * @see com.jonasreese.sound.sg.midi.MidiRecorder#isPlaying()
     */
    public boolean isPlaying() {
        try {
            return getSeq().isRunning();
        } catch (RecorderException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Starts the recording after click has been started for one tact.
     * @throws RecorderException if the recording MIDI device could not
     *         be prepared.
     */
    public void clickAndRecord() throws RecorderException {
        if (!isRecordEnabledForAnyTrack()) {
            throw new RecorderException(
                SgEngine.getInstance().getResourceBundle().getString( "system.noTrackSelectedForRecord" ) );
        }

        // prepare these things so that record() method is called faster
        initInputState();
        initOutputState();
        
        if (needsInputMapUpdate || needsOutputMapUpdate) {
            needsInputMapUpdate = false;
            needsOutputMapUpdate = false;
            closeAll();
            if (permanentLoopbackEnabled) {
                connectLoopback();
            }
        }
        openSequencer();
        connectPlayback();

        if (metronome == null || !metronome.isRunning()) {
            stopClickOnStop = true;
        }
        getMetronome();
        metronome.addMidiOutputReceiver( countInReceiver );
        metronome.start();
    }
    
    /**
     * Starts the recording.
     * @throws RecorderException if the recording MIDI device could not
     *         be prepared.
     */
    public void record() throws RecorderException {
        if (!isRecordEnabledForAnyTrack()) {
            throw new RecorderException(
                SgEngine.getInstance().getResourceBundle().getString( "system.noTrackSelectedForRecord" ) );
        }
        
        initInputState();
        initOutputState();

        if (isInFastForwardMode()) { stopFastForward(); }
        if (isInFastBackwardMode()) { stopFastBackward(); }
        
        if (needsInputMapUpdate || needsOutputMapUpdate) {
            needsInputMapUpdate = false;
            needsOutputMapUpdate = false;
            closeAll();
            if (permanentLoopbackEnabled) {
                connectLoopback();
            }
        }
        openSequencer();
        connectPlayback();
        //disconnectPlayback(); // sequencer shall not trigger MIDI output when recording
        
        RecordTrack[] recTracks = getRecordTracks();
        for (int i = 0; i < recTracks.length; i++) {
            recTracks[i].track.startAsynchronousEditMode();
        }
        int a = SgEngine.getInstance().getProperties().getMinimumRecordSafetyTime();
        int b = SgEngine.getInstance().getProperties().getRecordIncrementalTime();
        long safetyTicks = microsToTicks( a * 1000000 );
        long ticksToAdd = microsToTicks( b * 1000000 );
        stretchTrack( getTickPosition(), recTracks, safetyTicks, ticksToAdd );

        if (recordingConnector == null) {
            recordingConnector = new RecordConnector( false );
            recordingConnector.open();
            devicesToClose.add( recordingConnector );
        }
        recordingConnector.connect();

        if (recordLoopbackEnabled && !permanentLoopbackEnabled) {
            connectLoopback();
        }
        
        getMetronomeImpl().sync();

        try {
            descriptor.getSequence();
            sequencer.startRecording();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RecorderException( ex.getMessage() );
        }
        startUpdaterThread();
    }

    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#stopRecording()
     */
    private synchronized void stopRecording() {
        if (isInFastForwardMode()) { stopFastForward(); }
        if (isInFastBackwardMode()) { stopFastBackward(); }
        
        Sequencer sequencer = this.sequencer;
        sequencer.stopRecording();
        
        try {
            RecordConnector recordingConnector = this.recordingConnector;
            if (recordingConnector != null) {
                RecordEdit edit = new RecordEdit( descriptor, this );
                for (Receiver recObj : recordingConnector.receiverList) {
                    RecordReceiver receiver = (RecordReceiver) recObj;
                    ArrayList<Object> eventList = receiver.recordedData;
                    MidiEvent[] events = new MidiEvent[eventList.size() - 1];
                    long originalLength = ((Long) eventList.get( 0 )).longValue();
                    for (int i = 1; i <= events.length; i++) {
                        events[i - 1] = (MidiEvent) eventList.get( i );
                    }
                    receiver.recordTrack.track.stopAsynchronousEditMode();
                    edit.addEditTrack( receiver.recordTrack.track, events, originalLength );
                }
                System.out.println("MidiRecorderImpl: Adding RecordEdit");
                edit.perform();
                descriptor.getUndoManager().addEdit( edit );
                recordingConnector.disconnect();
            } else {
                System.out.println( "MidiRecorderImpl: Cannot stop recording - recordingConnector is null" );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        closeAll();
        if (permanentLoopbackEnabled) {
            try {
                connectLoopback();
            } catch (RecorderException e) {
                e.printStackTrace();
            }
        }
        //getRecDev().close();
    }
    
    /*
     * (non-Javadoc)
     * @see com.jonasreese.sound.sg.midi.MidiRecorder#isRecording()
     */
    public boolean isRecording() {
        try {
            return getSeq().isRecording();
        } catch (RecorderException e) {
        }
        return false;
    }

    /* (non-Javadoc)
     */
    public MidiDeviceMap getMidiOutputMap() {
        if (midiOutputMap == null) {
            midiOutputMap = new MidiDeviceMapImpl( null, false );
            initOutputState();
        }
        return midiOutputMap;
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.midi.MidiRecorder#getMidiInputMap()
     */
    public MidiDeviceMap getMidiInputMap( TrackProxy track ) {
        MidiDeviceMap im = (MidiDeviceMap) inputMap.get( track );
        if (im == null) {
            im = new MidiDeviceMapImpl( track, true );
            inputMap.put( track, im );
            initInputState();
        }
        return im;
    }
    
    public void setRecordEnabled( TrackProxy trackProxy, boolean enabled ) {
        if (enabled) {
            enableRecord( trackProxy );
        } else {
            disableRecord( trackProxy );
        }
    }

    private void enableRecord( TrackProxy trackProxy ) throws IllegalStateException {
        initInputState();
        int channel = -1; //
        if (isRecording()) {
            fireDeviceUpdate( MidiUpdatable.RECORD_ENABLE_STATE );
            throw new IllegalStateException();
        }
        int[] o = recordTracks.get( trackProxy );

        if (o != null) {
            int[] old = o;
            boolean b = true;
            for (int i = 0; i < old.length; i++) {
                if (old[i] == channel || old[i] == -1) {
                    b = false;
                }
            }
            if (b) {
                int[] value = new int[old.length + 1];
                System.arraycopy(old, 0, value, 0, old.length);
                value[old.length] = channel;
                recordTracks.put( trackProxy, value );
                descriptor.putPersistentClientProperty( trackProxy, RECORD_ENABLED_PROPERTY, Boolean.toString( true ) );
                fireDeviceUpdate( MidiUpdatable.RECORD_ENABLE_STATE );
            }
        } else {
            recordTracks.put( trackProxy, new int[]{ channel } );
            descriptor.putPersistentClientProperty( trackProxy, RECORD_ENABLED_PROPERTY, Boolean.toString( true ) );
            fireDeviceUpdate( MidiUpdatable.RECORD_ENABLE_STATE );
        }

        if (isLoopbackEnabled()) {
            synchronized (this) {
                try {
                    needsInputMapUpdate = true;
                    disconnectLoopback();
                    connectLoopback();
                } catch (RecorderException mex) {
                    mex.printStackTrace();
                }
            }
        }
    }

    private void disableRecord( TrackProxy trackProxy ) throws IllegalStateException {
        initInputState();
        if (isRecording()) {
            fireDeviceUpdate( MidiUpdatable.RECORD_ENABLE_STATE );
            throw new IllegalStateException();
        }
        recordTracks.remove( trackProxy );
        try {
            descriptor.removePersistentClientProperty( trackProxy, RECORD_ENABLED_PROPERTY );
        } catch (Exception ignored) {
        }
        fireDeviceUpdate( MidiUpdatable.RECORD_ENABLE_STATE );
        if (isLoopbackEnabled()) {
            synchronized (this) {
                try {
                    needsInputMapUpdate = true;
                    disconnectLoopback();
                    connectLoopback();
                } catch (RecorderException mex) {
                    mex.printStackTrace();
                }
            }
        }
    }
    
    private void initOutputState() {
        if (!outputStateInitialized) {
            System.out.println( "initOutputState()" );
            outputStateInitialized = true;
            restorePlaybackState();
        }
    }
    
    private void initInputState() {
        if (!inputStateInitialized) {
            System.out.println( "initInputState()" );
            inputStateInitialized = true;
            restoreRecordingState();
        }
    }
    
    /**
     * Gets the record enabled for any track status.
     * @return <code>true</code> if one of the tracks is in record mode for
     *         any channel, <code>false</code> otherwise.
     */
    public boolean isRecordEnabledForAnyTrack() {
        initInputState();
        return !recordTracks.isEmpty();
    }

    /*
     * (non-Javadoc)
     * @see com.jonasreese.sound.sg.midi.MidiRecorder#isRecordEnabled(com.jonasreese.sound.sg.midi.TrackProxy)
     */
    public boolean isRecordEnabled( TrackProxy trackProxy ) {
        initInputState();
        return recordTracks.containsKey( trackProxy );
    }

    /*
     */
    private int[] getRecordEnabledChannelsForTrack( TrackProxy trackProxy ) {
        Object o = recordTracks.get( trackProxy );
        if (o == null) {
            return new int[0];
        }
        int[] val = (int[]) o;
        int[] copy = new int[val.length];
        System.arraycopy( val, 0, copy, 0, val.length );
        return copy;
    }
    
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#getTempoInBPM()
     */
    public float getTempoInBPM() {
        try {
            return getSeq().getTempoInBPM();
        } catch (RecorderException e) {
        }
        return 0;
    }
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#setTempoInBPM(float)
     */
    public void setTempoInBPM( float bpm ) {
        try {
            getSeq().setTempoInBPM( bpm );
            getMetronome().setTempoInBpm( bpm );
        } catch (RecorderException e) {
        }
    }
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#getTempoInMPQ()
     */
    public float getTempoInMPQ() {
        try {
            return getSeq().getTempoInBPM();
        } catch (RecorderException e) {
        }
        return 0;
    }
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#setTempoInMPQ(float)
     */
    public void setTempoInMPQ( float mpq ) {
        try {
            getSeq().setTempoInMPQ( mpq );
            getMetronome().setTempoInMpq( mpq );
        } catch (RecorderException e) {
        }
    }
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#getTickLength()
     */
    public long getTickLength() {
        try {
            return getSeq().getTickLength();
        } catch (RecorderException e) {
        }
        return 0;
    }
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#getTickPosition()
     */
    public long getTickPosition() {
        Sequencer sequencer = this.sequencer;
        if (sequencer == null || !sequencer.isOpen()) {
            return (currentTick >= 0 ? currentTick : 0);
        }
        return sequencer.getTickPosition();
    }
    
    /**
     * Sets the tick position to the left marker tick position.
     */
    public void jumpToLeftMarker() {
        setTickPosition( getLeftMarkerTick() );
    }
    
    /**
     * Sets the tick position to the right marker tick position.
     */
    public void jumpToRightMarker() {
        setTickPosition( getRightMarkerTick() );
    }

    public void jumpToStart() {
        setTickPosition( 0 );
    }
    
    /**
     * Sets the tick position to the end of the associated sequence.
     */
    public void jumpToEnd() {
        try {
            setTickPosition( descriptor.getSequence().getActualLength() );
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#setTickPosition(long)
     */
    public void setTickPosition( long tick ) {
        if (tick == getTickPosition()) {
            return;
        }
        Sequencer sequencer = this.sequencer;
        if (sequencer != null && sequencer.isOpen()) {
            //System.out.println( "setTickPosition() on sequencer device" );
            sequencer.setTickPosition( tick );
        } else {
            //System.out.println( "setTickPosition() on MidiRecorderImpl" );
            currentTick = tick;
        }
        descriptor.getSession().setChanged( true );
        if (sequencer == null || !sequencer.isRunning()) {
            fireDeviceUpdate( MidiUpdatable.TICK );
        }
    }
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#getMicrosecondLength()
     */
    public long getMicrosecondLength() {
        try {
            return getSeq().getMicrosecondLength();
        } catch (RecorderException e) {
        }
        return 0;
    }
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#setMicrosecondPosition(long)
     */
    public void setMicrosecondPosition( long pos ) {
        //System.out.println( "setMicroSecondPosition()" );
        Sequencer sequencer = this.sequencer;
        if (sequencer == null || !sequencer.isOpen()) {
            double d = pos / getSequence().getMicrosecondLength();
            setTickPosition( (long) (d * getTickLength()) );
        } else {
            sequencer.setMicrosecondPosition( pos );
        }
    }
    
    public void setNextPlaybackMuted( boolean muteNextPlayback ) {
        this.muteNextPlayback = muteNextPlayback;
    }
    
    /* (non-Javadoc)
     */
    public void setTrackMuted( TrackProxy track, boolean mute ) {
        initOutputState();
        Sequencer sequencer = this.sequencer;
        if (sequencer != null && sequencer.isOpen()) {
            sequencer.setTrackMute( getSequence().getIndexOf( track ), mute );
        }
        boolean b;
        if (mute) {
            b = mutedTracks.add( track );
            descriptor.putPersistentClientProperty( track, PLAYBACK_MUTE_PROPERTY, Boolean.toString( true ) );
        } else {
            b = mutedTracks.remove( track );
            descriptor.removePersistentClientProperty( track, PLAYBACK_MUTE_PROPERTY );
        }
        if (b) {
            fireDeviceUpdate( MidiUpdatable.MUTE_STATE );
        }
    }
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#getTrackMute(int)
     */
    public boolean isTrackMuted( TrackProxy track ) {
        initOutputState();
        Sequencer sequencer = this.sequencer;
        if (sequencer != null && sequencer.isOpen()) {
            return sequencer.getTrackMute( getSequence().getIndexOf( track ) );
        }
        return mutedTracks.contains( track );
    }
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#setTrackSolo(int, boolean)
     */
    public void setTrackSolo( TrackProxy track, boolean solo ) {
        initOutputState();
        Sequencer sequencer = this.sequencer;
        if (sequencer != null && sequencer.isOpen()) {
            sequencer.setTrackSolo( getSequence().getIndexOf( track ), solo );
        }
        boolean b;
        if (solo) {
            b = soloTracks.add( track );
            descriptor.putPersistentClientProperty( track, PLAYBACK_SOLO_PROPERTY, Boolean.toString( true ) );
        } else {
            b = soloTracks.remove( track );
            descriptor.removePersistentClientProperty( track, PLAYBACK_SOLO_PROPERTY );
        }
        if (b) {
            fireDeviceUpdate( MidiUpdatable.SOLO_STATE );
        }
    }
    /* (non-Javadoc)
     */
    public boolean isTrackSolo( TrackProxy track ) {
        initOutputState();
        Sequencer sequencer = this.sequencer;
        if (sequencer != null && sequencer.isOpen()) {
            return sequencer.getTrackSolo( getSequence().getIndexOf( track ) );
        }
        return soloTracks.contains( track );
    }

    public Metronome getMetronome() {
        return getMetronomeImpl();
    }

    private MetronomeImpl getMetronomeImpl() {
        if (metronome == null) {
            metronome = new MetronomeImpl( true );
            metronome.setDefaultDeviceOutputEnabled( true );
            metronome.setTempoInMpq( getSequence().getTempoInMpq() );
            countInReceiver = new CountInReceiver();
        }
        return metronome;
    }
    
    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.midi.MidiRecorder#setRecordLoopbackEnabled(boolean)
     */
    public synchronized void setRecordLoopbackEnabled( boolean recordLoopbackEnabled ) throws RecorderException {
        initInputState();
        initOutputState();
        if (this.recordLoopbackEnabled == recordLoopbackEnabled) {
            return;
        }
        if (isRecording()) {
            if (recordLoopbackEnabled) {
                connectLoopback();
            } else {
                if (!permanentLoopbackEnabled) {
                    disconnectLoopback();
                }
            }
        }
        this.recordLoopbackEnabled = recordLoopbackEnabled;
        fireDeviceUpdate( MidiUpdatable.RECORD_LOOPBACK_STATE );
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.midi.MidiRecorder#isRecordLoopbackEnabled()
     */
    public boolean isRecordLoopbackEnabled() {
        return recordLoopbackEnabled;
    }

    public synchronized void setLoopbackEnabled( boolean permanentLoopbackEnabled ) throws RecorderException {
        initInputState();
        initOutputState();
        if (this.permanentLoopbackEnabled == permanentLoopbackEnabled) {
            return;
        }
        if (permanentLoopbackEnabled) {
            if (!(isRecording() && isRecordLoopbackEnabled())) {
                connectLoopback();
            }
        } else {
            if (!(isRecording() && isRecordLoopbackEnabled())) {
                disconnectLoopback();
            }
        }
        this.permanentLoopbackEnabled = permanentLoopbackEnabled;
        fireDeviceUpdate( MidiUpdatable.LOOPBACK_STATE );
    }
    
    public boolean isLoopbackEnabled() {
        return permanentLoopbackEnabled;
    }
    
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#addMetaEventListener(javax.sound.midi.MetaEventListener)
     */
    public boolean addMetaEventListener( MetaEventListener l ) {
        try {
            return getSeq().addMetaEventListener( l );
        } catch (RecorderException e) {
        }
        return false;
    }
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#removeMetaEventListener(javax.sound.midi.MetaEventListener)
     */
    public void removeMetaEventListener( MetaEventListener l ) {
        try {
            getSeq().removeMetaEventListener( l );
        } catch (RecorderException e) {
        }
    }
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#addControllerEventListener(javax.sound.midi.ControllerEventListener, int[])
     */
    public int[] addControllerEventListener(
        ControllerEventListener l,
        int[] controllers) {
        try {
            return getSeq().addControllerEventListener( l, controllers );
        } catch (RecorderException e) {
        }
        return null;
    }
    /* (non-Javadoc)
     * @see javax.sound.midi.Sequencer#removeControllerEventListener(javax.sound.midi.ControllerEventListener, int[])
     */
    public int[] removeControllerEventListener(
        ControllerEventListener l,
        int[] controllers) {

        try {
            return getSeq().removeControllerEventListener( l, controllers );
        } catch (RecorderException e) {
        }
        return null;
    }


    
    private void startUpdaterThread() {
        synchronized (threadSync) {
            if (deviceThread == null || !deviceThread.isAlive()) {
                deviceThread = new DeviceThread();
                deviceThread.start();
            }
        }
    }

    /**
     * Adds a <code>MidiUpdatable</code> to this <code>MidiDeviceProxy</code>.
     * @param updatable The <code>MidiUpdatable</code> to add.
     */
    public void addMidiUpdatable( MidiUpdatable updatable ) {
        synchronized (midiUpdatables) {
            if (!midiUpdatables.contains( updatable )) {
                midiUpdatables.add( updatable );
            }
        }
    }
    
    /**
     * Removes a <code>MidiUpdatable</code> from this <code>MidiDeviceProxy</code>.
     * @param updatable The <code>MidiUpdatable</code> to be removed.
     */
    public void removeMidiUpdatable( MidiUpdatable updatable ) {
        synchronized (midiUpdatables) {
            midiUpdatables.remove( updatable );
        }
    }
    
    /**
     * Adds a MIDI output receiver to this <code>MidiRecorder</code>. An output
     * MIDI receiver receives all MIDI events that are sent to any output device.
     * @param midiOutputReceiver The <code>Receiver</code> that shall receive MIDI
     * events after being sent to an output device. If the given
     * <code>MidiOutputReceiver</code> has already been added, this method does nothing.
     */
    public void addMidiOutputReceiver( Receiver midiOutputReceiver ) {
        synchronized (midiOutputReceivers) {
            if (!midiOutputReceivers.contains( midiOutputReceiver )) {
                midiOutputReceivers.add( midiOutputReceiver );
            }
        }
    }

    /**
     * Removes the given MIDI output receiver from this <code>MidiRecorder</code>.
     * @param midiOutputReceiver The MIDI output receiver that shall no longer
     * receive any MIDI events after being sent to an output device. If the given
     * <code>Receiver</code> is not registered as MIDI output receiver, this method
     * does nothing.
     */
    public void removeMidiOutputReceiver( Receiver midiOutputReceiver ) {
        synchronized (midiOutputReceivers) {
            midiOutputReceivers.remove( midiOutputReceiver );
        }
    }
    
    /* (non-Javadoc)
     * @see javax.sound.midi.MidiDevice#close()
     */
    private synchronized void closeAll() {
        if (sequencer != null && sequencer.isOpen()) {
            currentTick = sequencer.getTickPosition();
            sequencer.close();
        }
        if (devicesToClose.isEmpty()) { return; }
        System.out.println( "MidiRecorderImpl.closeAll()" );
        for (Object o : devicesToClose) {
            if (o instanceof Transmitter) {
                ((Transmitter) o).close();
            }
            if (o instanceof Receiver) {
                ((Receiver) o).close();
            }
            if (o instanceof MidiDevice) {
                ((MidiDevice) o).close();
            }
            if (o instanceof RecordConnector) {
                ((RecordConnector) o).close();
            }
        }
        devicesToClose.clear();
        sequencerTransmitter = null;
        sequencer = null;
        needsOutputMapUpdate = false;
        needsInputMapUpdate = false;
        playbackReceiver = null;
        loopbackConnector = null;
        recordingConnector = null;
        
        if (shutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook( shutdownHook );
            } catch (IllegalStateException ex) { // if shutdown is in progress
                System.out.println( "MidiRecorderImpl.close() : Did not remove shutdown hook - " + ex.getMessage() );
            }
            shutdownHook = null;
        }
        //fireDeviceClosed();
    }

    public long getMicrosecondPosition() {
        try {
            double d = getSequence().getMicrosecondLength() / getSequence().getTickLength();
            return (long) (getTickPosition() * d);
        } catch (Throwable t) {
            return 0;
        }
    }

    private void fireDeviceUpdate( int updateHint ) {
        synchronized (midiUpdatables) {
            for (int i = 0; i < midiUpdatables.size(); i++) {
                MidiUpdatable du = (MidiUpdatable) midiUpdatables.get( i );
                du.deviceUpdate( this, descriptor, updateHint );
            }
        }
    }

//    protected void fireDeviceClosed() {
//        synchronized (midiUpdatables) {
//            for (int i = 0; i < midiUpdatables.size(); i++) {
//                MidiUpdatable mu = (MidiUpdatable) midiUpdatables.get( i );
//                mu.deviceClosed( this, descriptor );
//            }
//        }
//    }
    
    /**
     * Starts the <code>fastForward</code> mode. During the time this mode
     * is active, the tick position advances quickly. Call the
     * <code>stopFastForward()</code> method to stop this mode.
     */
    public synchronized void startFastForward() {
        if (isInFastForwardMode()) { return; }
        if (isInFastBackwardMode()) { stopFastBackward(); }
        Thread t = new Thread( fastRunnable );
        if (isPlaying()) {
            stop();
            mode = FAST_FORWARD_RUNNING_MODE;
        } else {
            mode = FAST_FORWARD_MODE;
        }
        t.start();
    }
    
    /**
     * Stops the <code>fastForward</code> mode. This method has only an effect
     * if <code>isInFastForwardMode()</code> returns <code>true</code>.
     */
    public synchronized void stopFastForward() {
        if (mode == FAST_FORWARD_MODE) {
            mode = DEFAULT_MODE;
        } else if (mode == FAST_FORWARD_RUNNING_MODE) {
            mode = DEFAULT_MODE;
            start();
        }
    }
    
    /**
     * Returns <code>true</code> if the <code>startFastForward()</code> method has
     * been called recently and the <code>stopFastForward</code> method has not
     * yet been called inbetween. Please note that the <code>stopFastForward()</code>
     * method may be called implicitly by some other methods within this class. 
     * @return <code>true</code> if in fastForward mode, <code>false</code> otherwise. 
     */
    public boolean isInFastForwardMode() {
        return (mode == FAST_FORWARD_MODE ||
            mode == FAST_FORWARD_RUNNING_MODE);
    }

    /**
     * Starts the <code>fastBackward</code> mode. During the time this mode
     * is active, the tick position advances quickly backwards. Call the
     * <code>stopFastBackward()</code> method to stop this mode.
     */
    public synchronized void startFastBackward() {
        if (isInFastBackwardMode()) { return; }
        if (isInFastForwardMode()) { stopFastForward(); }
        Thread t = new Thread( fastRunnable );
        if (isPlaying()) {
            stop();
            mode = FAST_BACKWARD_RUNNING_MODE;
        } else {
            mode = FAST_BACKWARD_MODE;
        }
        t.start();
    }
    
    /**
     * Stops the <code>fastBackward</code> mode. This method has only an effect
     * if <code>isInFastBackwardMode()</code> returns <code>true</code>.
     */
    public synchronized void stopFastBackward() {
        if (mode == FAST_BACKWARD_MODE) {
            mode = DEFAULT_MODE;
        } else if (mode == FAST_BACKWARD_RUNNING_MODE) {
            mode = DEFAULT_MODE;
            start();
        }
    }

    /**
     * Returns <code>true</code> if the <code>startFastBackward()</code> method has
     * been called recently and the <code>stopFastBackward</code> method has not
     * yet been called inbetween. Please note that the <code>stopFastBackward()</code>
     * method may be called implicitly by some other methods within this class. 
     * @return <code>true</code> if in fastBackward mode, <code>false</code> otherwise. 
     */
    public boolean isInFastBackwardMode() {
        return (mode == FAST_BACKWARD_MODE ||
            mode == FAST_BACKWARD_RUNNING_MODE);
    }
    
    private long microsToTicks( long micros ) {
        double d = getSequence().getMicrosecondLength() / getSequence().getTickLength();
        return (long) (micros / d);
    }

    public void persistRecordingState() {
        SgMidiSequence sequence = getSequence();
        if (sequence == null) {
            return;
        }
        TrackProxy[] tracks = sequence.getTrackProxies();
        for (int i = 0; i < tracks.length; i++) {
            if (recordTracks.containsKey( tracks[i] )) {
                descriptor.putPersistentClientProperty( tracks[i], RECORD_ENABLED_PROPERTY, Boolean.toString( true ) );
            } else {
                descriptor.removePersistentClientProperty( tracks[i], RECORD_ENABLED_PROPERTY );
            }
        }
        
        // persist mapping
        for (int k = 0; k < tracks.length; k++) {
            MidiDeviceMap inputMap = getMidiInputMap( tracks[k] );
            MidiChannelMap[] channelMaps = inputMap.getChannelMaps();
            MidiDeviceList inputList = SgEngine.getInstance().getProperties().getMidiInputDeviceList();
            StringBuffer property = new StringBuffer();
            for (int i = 0; i < channelMaps.length; i++) {
                if (!channelMaps[i].isEmpty()) {
                    MidiDeviceDescriptor desc = channelMaps[i].getMidiDeviceDescriptor();
                    int[] channels = channelMaps[i].getChannels();
                    // first array element is device number
                    property.append( "{" + inputList.getDeviceIndex( desc ) );
                    for (int j = 0; j < channels.length; j++) {
                        property.append( "," + channels[j] );
                    }
                    property.append( "}" );
                }
            }
            descriptor.putPersistentClientProperty( tracks[k], RECORD_INPUT_MAP_PROPERTY, property.toString() );
        }
    }

    public void restoreRecordingState() {
        try {
            SgMidiSequence sequence = getSequence();
            TrackProxy[] tracks = sequence.getTrackProxies();
            for (int i = 0; i < tracks.length; i++) {
                try {
                    String p = descriptor.getPersistentClientProperty( tracks[i], RECORD_ENABLED_PROPERTY );
                    if (Boolean.toString( true ).equals( p )) {
                        enableRecord( tracks[i] );
                    } else {
                        disableRecord( tracks[i] );
                    }
                } catch (Exception ignored) {
                }
            }

            // restore mapping
            MidiDeviceList inputList = SgEngine.getInstance().getProperties().getMidiInputDeviceList();
            IOException cannotException = new IOException( "Cannot restore recording state: Illegal property format" );
            for (int k = 0; k < tracks.length; k++) {
                MidiDeviceMap inputMap = getMidiInputMap( tracks[k] );
                if (inputMap != null) {
                    String property = descriptor.getPersistentClientProperty( tracks[k], RECORD_INPUT_MAP_PROPERTY );
                    if (property != null) {
                        for (int index = property.indexOf( '{' ); index >= 0; index = property.indexOf( '{' )) {
                            int toIndex = property.indexOf( '}' );
                            if (toIndex < 0) {
                                throw cannotException;
                            }
                            String s = property.substring( index + 1, toIndex );
                            StringTokenizer st = new StringTokenizer( s, "," );
                            if (!st.hasMoreTokens()) {
                                throw cannotException;
                            }
                            int devIndex = Integer.parseInt( st.nextToken() );
                            MidiChannelMap channelMap = inputMap.addMidiDevice(
                                    inputList.getMidiDeviceDescriptor( devIndex ) );
                            while (st.hasMoreTokens()) {
                                channelMap.addChannel( Integer.parseInt( st.nextToken() ) );
                            }
                            property = property.substring( toIndex + 1 );
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void persistPlaybackState() {
        try {
            TrackProxy[] tracks = descriptor.getSequence().getTrackProxies();
            for (int i = 0; i < tracks.length; i++) {
                if (soloTracks.contains( tracks[i] )) {
                    descriptor.putPersistentClientProperty( tracks[i], PLAYBACK_SOLO_PROPERTY, Boolean.toString( true ) );
                } else {
                    descriptor.removePersistentClientProperty( tracks[i], PLAYBACK_SOLO_PROPERTY );
                }
                if (mutedTracks.contains( tracks[i] )) {
                    descriptor.putPersistentClientProperty( tracks[i], PLAYBACK_MUTE_PROPERTY, Boolean.toString( true ) );
                } else {
                    descriptor.removePersistentClientProperty( tracks[i], PLAYBACK_SOLO_PROPERTY );
                }
            }
            
            // persist mapping
            MidiDeviceMap outputMap = getMidiOutputMap();
            MidiChannelMap[] channelMaps = outputMap.getChannelMaps();
            MidiDeviceList outputList = SgEngine.getInstance().getProperties().getMidiOutputDeviceList();
            StringBuffer property = new StringBuffer();
            for (int i = 0; i < channelMaps.length; i++) {
                if (!channelMaps[i].isEmpty()) {
                    MidiDeviceDescriptor desc = channelMaps[i].getMidiDeviceDescriptor();
                    int[] channels = channelMaps[i].getChannels();
                    // first array element is device number
                    property.append( "{" + outputList.getDeviceIndex( desc ) );
                    for (int j = 0; j < channels.length; j++) {
                        property.append( "," + channels[j] );
                    }
                    property.append( "}" );
                }
            }
            descriptor.putPersistentClientProperty( PLAYBACK_OUTPUT_MAP_PROPERTY, property.toString() );
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restorePlaybackState() {
        try {
            SgMidiSequence sequence = getSequence();
            if (sequence == null) {
                return;
            }
            TrackProxy[] tracks = sequence.getTrackProxies();
            for (int i = 0; i < tracks.length; i++) {
                String p = descriptor.getPersistentClientProperty( tracks[i], PLAYBACK_SOLO_PROPERTY );
                setTrackSolo( tracks[i], Boolean.toString( true ).equals( p ) );
                p = descriptor.getPersistentClientProperty( tracks[i], PLAYBACK_MUTE_PROPERTY );
                setTrackMuted( tracks[i], Boolean.toString( true ).equals( p ) );
            }
            
            // restore mapping
            MidiDeviceList outputList = SgEngine.getInstance().getProperties().getMidiOutputDeviceList();
            MidiDeviceMap outputMap = getMidiOutputMap();
            String property = descriptor.getPersistentClientProperty( PLAYBACK_OUTPUT_MAP_PROPERTY );
            IOException cannotException = new IOException( "Cannot restore playback state: Illegal property format" );
            if (outputMap != null) {
                if (property != null) {
                    for (int index = property.indexOf( '{' ); index >= 0; index = property.indexOf( '{' )) {
                        int toIndex = property.indexOf( '}' );
                        if (toIndex < 0) {
                            throw cannotException;
                        }
                        String s = property.substring( index + 1, toIndex );
                        StringTokenizer st = new StringTokenizer( s, "," );
                        if (!st.hasMoreTokens()) {
                            throw cannotException;
                        }
                        int devIndex = Integer.parseInt( st.nextToken() );
                        MidiChannelMap channelMap = outputMap.addMidiDevice(
                                outputList.getMidiDeviceDescriptor( devIndex ) );
                        while (st.hasMoreTokens()) {
                            channelMap.addChannel( Integer.parseInt( st.nextToken() ) );
                        }
                        property = property.substring( toIndex + 1 );
                    }
                } else {
                    // add first device for all channels per default (if output device available)
                    if (outputList != null && outputList.getCount() > 0) {
                        midiOutputMap.addMidiDevice( outputList.getMidiDeviceDescriptor( 0 ) ).addChannel( -1 );
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void stretchTrack( long currentTick, RecordTrack[] recTracks, long safetyTicks, long ticksToAdd ) {
        for (int i = 0; i < recTracks.length; i++) {
            //System.out.println( System.currentTimeMillis() + ": " + recTracks[i].track.ticks() + " - " + safetyTicks + " <= " + currentTick );
            if (recTracks[i].track.ticks() - safetyTicks <= currentTick) {
                try {
                    recTracks[i].track.setLength(
                            currentTick + safetyTicks + ticksToAdd, MidiRecorderImpl.this );
                    //long oldLength = recTracks[i].track.ticks();
                    //System.out.println( "stretched track to length " + recTracks[i].track.ticks() + " current tick is " + currentTick + " old length was " + oldLength );
                } catch (InvalidMidiDataException imdex) {
                    //imdex.printStackTrace();
                }
            }
        }
    }
    
    private RecordTrack[] getRecordTracks() {
        Set<TrackProxy> keys = recordTracks.keySet();
        RecordTrack[] recordTracks = new RecordTrack[keys.size()];
        Iterator<TrackProxy> iter = keys.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            TrackProxy tp = (TrackProxy) iter.next();
            recordTracks[i] = new RecordTrack( tp, getRecordEnabledChannelsForTrack( tp ) );
        }
        return recordTracks;
    }
    
    class RecordTrack {
        TrackProxy track;
        int[] channels;
        RecordTrack( TrackProxy track, int[] channels ) {
            this.track = track;
            this.channels = channels;
        }
        ShortMessage getShortMessage( MidiMessage m ) {
            if (m instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) m;
                if (channels.length == 1 && channels[0] < 0) { return sm; }
                int channel = ((ShortMessage) m).getChannel();
                for (int i = 0; i < channels.length; i++) {
                    if (channels[i] == channel) { return sm; }
                }
            }
            return null;
        }
    }

    class DeviceThread extends Thread implements PropertyChangeListener {
        int deviceUpdateTime;
        DeviceThread() {
            super( "MidiDevice updater thread" );
            setPriority( 2 ); // low priority
        }
        
        public void start() {
            super.start();
        }
        
        public void run() {
            synchronized (threadSync) {
                long maxTick = 0;
                long actualLength = getSequence() != null ? getSequence().getActualLength() : 0;
                boolean rec = isRecording();
                long safetyTicks = 0;
                long ticksToAdd = 0;
                RecordTrack[] recTracks = null;
                Sequencer sequencer = MidiRecorderImpl.this.sequencer;
                if (descriptor != null && getSequence() != null) {
                    if (mode == PLAY_FROM_LEFT_TO_RIGHT_MARKER_MODE || mode == PLAY_TO_RIGHT_MARKER_MODE) {
                        maxTick = Math.min( actualLength, getRightMarkerTick() );
                    } else if (!rec) {
                        maxTick = actualLength;
                    } else {
                        recTracks = getRecordTracks();
                        int a = SgEngine.getInstance().getProperties().getMinimumRecordSafetyTime();
                        int b = SgEngine.getInstance().getProperties().getRecordIncrementalTime();
                        safetyTicks = microsToTicks( a * 1000000 );
                        ticksToAdd = microsToTicks( b * 1000000 );
                        maxTick = Integer.MAX_VALUE;
                    }
                }
                // if in loop mode, do not control maximum length of playback,
                // because loop is always infinite
                if (looping) {
                    maxTick = Integer.MAX_VALUE;
                }
                deviceUpdateTime = SgEngine.getInstance().getProperties().getMidiUpdateTime();
                SgEngine.getInstance().getProperties().addPropertyChangeListener( "midiUpdateTime", this );
                if (deviceUpdateTime <= 0) { deviceUpdateTime = 1; }
                while (isUpdatingRequired()) {
                    //System.out.println( System.currentTimeMillis() );
                    try {
                        threadSync.wait( deviceUpdateTime );
                    } catch (InterruptedException e){}
                    
                    // stretch recorded tracks if neccessary
                    if (rec) {
                        long currentTick = sequencer.getTickPosition();
                        stretchTrack( currentTick, recTracks, safetyTicks, ticksToAdd );
                    } else {
                        if (getTickPosition() >= maxTick) {
                            System.out.println( "MidiRecorderImpl: End of sequence reached" );
                            MidiRecorderImpl.this.stop();
                            setTickPosition( maxTick );
                        }
                    }
                    fireDeviceUpdate( MidiUpdatable.TICK );
                }
            }
            SgEngine.getInstance().getProperties().removePropertyChangeListener(
                    "midiUpdateTime", this );
            //MidiRecorderImpl.this.stop();
            System.out.println( "DeviceThread.die()" );    
        }

        public void propertyChange( PropertyChangeEvent e ) {
            deviceUpdateTime = ((Integer) e.getNewValue()).intValue();
            if (deviceUpdateTime <= 0) {
                deviceUpdateTime = 1;
            }
        }
    }
    
    // connects pool of RecordReceiver objects to the input devices Transmitters
    class RecordConnector {
        HashMap<MidiDeviceDescriptor,MidiDevice> deviceMap;
        ArrayList<Receiver> receiverList;
        ArrayList<Transmitter> transmitterList;
        RecordConnector( boolean loopback ) throws RecorderException {
            System.out.println( "MidiRecorderImpl.RecordConnector()" );
            RecordTrack[] tracks = getRecordTracks();
            deviceMap = new HashMap<MidiDeviceDescriptor,MidiDevice>();
            receiverList = new ArrayList<Receiver>();
            transmitterList = new ArrayList<Transmitter>();
            for (int i = 0; i < tracks.length; i++) {
                MidiDeviceMap inputMap = getMidiInputMap( tracks[i].track );
                if (inputMap != null && !inputMap.isEmpty()) {
                    MidiDeviceList deviceList = inputMap.getMidiDeviceList();
                    for (int j = 0; j < deviceList.getCount(); j++) {
                        Receiver receiver;
                        if (loopback) {
                            boolean[] channelMask = new boolean[16];
                            int[] channels = inputMap.getChannelMapFor( deviceList.getMidiDeviceDescriptor( j ) ).getChannels();
                            for (int k = 0; k < channels.length; k++) {
                                channelMask[channels[k]] = true;
                            }
                            receiver = new PlaybackReceiver( channelMask );
                        } else {
                            receiver = new RecordReceiver(
                                    tracks[i],
                                    inputMap.getChannelMapFor(
                                            deviceList.getMidiDeviceDescriptor( j ) ).getChannels() );
                        }
                        MidiDevice device = (MidiDevice) deviceMap.get( deviceList.getMidiDeviceDescriptor( j ) );
                        if (device == null) {
                            try {
                                device = MidiToolkit.getMidiDevice( deviceList.getMidiDeviceDescriptor( j ) );
                            } catch (MidiUnavailableException muaex) {
                                throw new RecorderException( muaex );
                            }
                            deviceMap.put( deviceList.getMidiDeviceDescriptor( j ), device );
                        }
                        receiverList.add( receiver );
                        Transmitter transmitter;
                        try {
                            transmitter = device.getTransmitter();
                        } catch (MidiUnavailableException muaex) {
                            throw new RecorderException( muaex );
                        }
                        //System.out.println( "setting receiver for device " + device.getDeviceInfo() + " transmitter" );
                        transmitterList.add( transmitter );
                    }
                }
            }
        }
        void open() throws RecorderException {
            for (MidiDevice dev : deviceMap.values()) {
                try {
                    dev.open();
                } catch (MidiUnavailableException muaex) {
                    dev.close();
                    throw new RecorderException( muaex );
                }
            }
        }
        void connect() {
            for (int i = 0; i < transmitterList.size(); i++) {
                Transmitter transmitter = (Transmitter) transmitterList.get( i );
                Receiver receiver = (Receiver) receiverList.get( i );
                transmitter.setReceiver( receiver );
            }
        }
        void disconnect() {
            for (int i = 0; i < receiverList.size(); i++) {
                Object o = receiverList.get( i );
                if (o instanceof PlaybackReceiver) {
                    ((PlaybackReceiver) o).disconnect();
                }
            }
            for (int i = 0; i < transmitterList.size(); i++) {
                Transmitter transmitter = (Transmitter) transmitterList.get( i );
                transmitter.setReceiver( null );
            }
        }
        void close() {
            for (Receiver rec : receiverList) {
                rec.close();
            }
            for (Transmitter trans : transmitterList) {
                trans.close();
            }
            for (MidiDevice dev : deviceMap.values()) {
                dev.close();
            }
        }
    }
    
    class RecordReceiver implements Receiver {
        RecordTrack recordTrack;
        ShortMessage[] noteOn;
        long[] noteOnTick;
        ArrayList<Object> recordedData;
        boolean closed;
        boolean first;
        boolean[] sendOnChannel;
        RecordReceiver( RecordTrack recordTrack, int[] channels ) {
            this.recordTrack = recordTrack;
            closed = true;
            noteOn = new ShortMessage[255];
            noteOnTick = new long[255];
            sendOnChannel = new boolean[16];
            for (int i = 0; i < channels.length; i++) {
                sendOnChannel[channels[i]] = true;
            }
            init();
        }
        void init() {
            closed = false;
            first = true;
            noteOn = new ShortMessage[255];
            recordedData = new ArrayList<Object>();
            recordedData.add( new Long( recordTrack.track.getLength() ) );
        }

        public void send( MidiMessage m, long t ) {
            if (closed) { return; }
            if (first) {
                Thread.currentThread().setPriority( Thread.MAX_PRIORITY );
                first = false;
            }
            if (m instanceof ShortMessage && !sendOnChannel[((ShortMessage) m).getChannel()]) {
                return;
            }
            long tick = sequencer.getTickPosition();
            ShortMessage sm = recordTrack.getShortMessage( m );
            if (sm != null && sm.getStatus() != ShortMessage.TIMING_CLOCK) {
                //System.out.println("data1 = " + sm.getData1());
                //System.out.println("data2 = " + sm.getData2());
                MidiMessage m2 = null;
                long tick0 = tick;
                int index = sm.getData1();
                if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                    ShortMessage originalNoteOn = noteOn[index];
                    if (originalNoteOn != null) {
                        m = originalNoteOn;
                        tick = noteOnTick[index];
                    } else {
                        m = null;
                    }
                    noteOn[index] = sm;
                    noteOnTick[index] = tick;
                } else {
                    if (sm.getCommand() == ShortMessage.NOTE_OFF ||
                        (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() == 0)) {

                        ShortMessage originalNoteOn = noteOn[index];
                        if (originalNoteOn != null) {
                            m = originalNoteOn;
                            tick0 = noteOnTick[index];
                            m2 = sm;
                        }
                    }
                    noteOn[index] = null;
                }
                if (m != null) {
                    //long time = System.currentTimeMillis();
                    MidiEvent e1 = null;
                    MidiEvent e2 = null;
                    if (m2 != null) {
                        e1 = new MidiEvent( m, tick0 );
                        e2 = new MidiEvent( m2, tick );
                        recordTrack.track.addAll(
                            new MidiEvent[] { e1, e2 }, MidiRecorderImpl.this );
                    } else {
                        e1 = new MidiEvent( m, tick );
                        recordTrack.track.add( e1, MidiRecorderImpl.this );
                    }
                    recordedData.add( e1 );
                    if (e2 != null) {
                        recordedData.add( e2 );
                    }
                    //long diff = System.currentTimeMillis() - time;
                    //if (diff > 0) {
                    //    System.out.println( "Took more than one millisecond: " + diff + ", " +
                    //          (m2 != null ? "addAll() called" : "add() called") );
                    //}
                }
            }
        }

        public void close() {
            closed = true;
        }
    }
    
    class PlaybackReceiver implements Receiver {
        MidiDevice[] devices;
        Receiver[] receivers;
        boolean[][] channelActivated;
        boolean[][] noteOn;
        boolean loopback;
        boolean mute;
        PlaybackReceiver( boolean[] channelMask ) throws RecorderException {
            loopback = (channelMask != null);
            MidiDeviceList outputDeviceList =
                SgEngine.getInstance().getProperties().getMidiOutputDeviceList();
            if (outputDeviceList == null || outputDeviceList.getCount() == 0) {
                throw new RecorderException(
                    SgEngine.getInstance().getResourceBundle().getString(
                        "system.noMidiOutputDeviceSelected" ) );
            }
            ArrayList<MidiDeviceDescriptor> deviceList = new ArrayList<MidiDeviceDescriptor>();
            MidiDeviceMap midiOutputMap = getMidiOutputMap();
            for (int i = 0; i < outputDeviceList.getCount(); i++) {
                MidiChannelMap mcm = midiOutputMap.getChannelMapFor( outputDeviceList.getMidiDeviceDescriptor( i ) );
                if (mcm != null && !mcm.isEmpty()) {
                    deviceList.add( mcm.getMidiDeviceDescriptor() );
                }
            }
            devices = new MidiDevice[deviceList.size()];
            receivers = new Receiver[devices.length];
            channelActivated = new boolean[devices.length][16];
            for (int i = 0; i < devices.length; i++) {
                MidiDeviceDescriptor devInfo = (MidiDeviceDescriptor) deviceList.get( i );
                MidiChannelMap mcm = midiOutputMap.getChannelMapFor( devInfo );
                int[] channels = mcm.getChannels();
                for (int j = 0; j < channels.length; j++) {
                    if (channelMask == null || channelMask[channels[j]]) {
                        channelActivated[i][channels[j]] = true;
                    }
                }
                boolean deviceWasOpen = false;
                try {
                    devices[i] = MidiToolkit.getMidiDevice( devInfo );
                    deviceWasOpen = devices[i].isOpen();
                    if (!deviceWasOpen) {
                        devices[i].open();
                    }
                    receivers[i] = devices[i].getReceiver();
                } catch (MidiUnavailableException muaex) {
                    if (!deviceWasOpen) {
                        devices[i].close();
                    }
                    throw new RecorderException( muaex );
                }
            }
            noteOn = new boolean[255][16];
            //System.out.println( "# of devices: " + devices.length );
            
            mute = false;
        }
        public void send( MidiMessage m, long t ) {
            try {
                if (!mute) {
                    if (m instanceof ShortMessage) {
                        ShortMessage sm = (ShortMessage) m;
                        if (sm.getCommand() == ShortMessage.NOTE_OFF ||
                                (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() == 0)) {
                            noteOn[sm.getData1()][sm.getChannel()] = false;
                        } else {
                            noteOn[sm.getData1()][sm.getChannel()] = true;
                        }
                        for (int i = 0; i < receivers.length; i++) {
                            if (channelActivated[i][sm.getChannel()]) {
                                receivers[i].send( m, t );
                            }
                        }
                    } else {
                        for (int i = 0; i < receivers.length; i++) {
                            receivers[i].send( m, t );
                        }
                    }
                }
                if (!midiOutputReceivers.isEmpty()) {
                    synchronized (midiOutputReceivers) {
                        for (int i = 0; i < midiOutputReceivers.size(); i++) {
                            Receiver r = midiOutputReceivers.get( i );
                            r.send( m, t );
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void close() {
            for (int i = 0; i < devices.length; i++) {
                receivers[i].close();
                if (devices[i].isOpen()) {
                    devices[i].close();
                }
            }
        }
        /// sends remaining NOTE_OFFs to output devices
        void disconnect() {
            for (int i = 0; i < noteOn.length; i++) {
                for (int j = 0; j < noteOn[j].length; j++) {
                    if (noteOn[i][j]) {
                        ShortMessage msg = new ShortMessage();
                        try {
                            msg.setMessage( ShortMessage.NOTE_ON, j, i, 0 );
                            send( msg, 0 );
                            noteOn[i][j] = false;
                        } catch (InvalidMidiDataException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
    synchronized void checkIfUpdateNeeded( boolean input ) {
        if (input && recordingConnector != null) {
            needsInputMapUpdate = true;
        }
        if (!input && playbackReceiver != null) {
            needsOutputMapUpdate = true;
        }
        if (loopbackConnector != null) {
            needsInputMapUpdate = true;
            needsOutputMapUpdate = true;
        }
        if (needsInputMapUpdate && isRecording() || needsOutputMapUpdate && isPlaying()) {
            stop();
        }
        //System.out.println(
        //        "MidiRecorderImpl.PlaybackReceiver.checkIfUpdateNeeded( inputmap=" +
        //        needsInputMapUpdate + ", outputmap=" + needsOutputMapUpdate );
    }
    
    // implementation of interface MidiDeviceMap
    class MidiDeviceMapImpl implements MidiDeviceMap {
        TrackProxy track;
        HashMap<MidiDeviceDescriptor,MidiChannelMap> midiChannelMap;
        boolean input;
        MidiDeviceMapImpl( TrackProxy track, boolean input ) {
            this.track = track;
            this.input = input;
            midiChannelMap = new HashMap<MidiDeviceDescriptor,MidiChannelMap>();
        }
        public TrackProxy getTrack() {
            return track;
        }
        public MidiDeviceList getMidiDeviceList() {
            MidiDeviceDescriptor[] descriptors = new MidiDeviceDescriptor[midiChannelMap.size()];
            midiChannelMap.keySet().toArray( descriptors );
            MidiDeviceList result = new MidiDeviceList( descriptors );
            return result;
        }
        public MidiChannelMap addMidiDevice(MidiDeviceDescriptor device) {
            MidiChannelMap mcm = (MidiChannelMap) midiChannelMap.get( device );
            if (mcm == null) {
                mcm = new MidiChannelMapImpl( device, input );
                midiChannelMap.put( device, mcm );
                checkIfUpdateNeeded( input );
            }
            return mcm;
        }
        public boolean removeMidiDevice(MidiDeviceDescriptor device) {
            boolean b = (midiChannelMap.remove( device ) != null);
            if (b) {
                checkIfUpdateNeeded( input );
            }
            return b;
        }
        public MidiChannelMap[] getChannelMaps() {
            MidiChannelMap[] result = new MidiChannelMap[midiChannelMap.size()];
            midiChannelMap.values().toArray( result );
            return result;
        }
        public MidiChannelMap getChannelMapFor(MidiDeviceDescriptor device) {
            return (MidiChannelMap) midiChannelMap.get( device );
        }
        public boolean isEmpty() {
            MidiChannelMap[] cm = getChannelMaps();
            for (int i = 0; i < cm.length; i++) {
                if (!cm[i].isEmpty()) {
                    return false;
                }
            }
            return true;
        }
    }
    
    // implementation of interface MidiChannelMap
    class MidiChannelMapImpl implements MidiChannelMap {
        MidiDeviceDescriptor device;
        int[] channels;
        boolean input;
        MidiChannelMapImpl( MidiDeviceDescriptor device, boolean input ) {
            this.device = device;
            this.input = input;
            channels = new int[0];
        }
        public MidiDeviceDescriptor getMidiDeviceDescriptor() {
            return device;
        }
        public boolean addChannel(int channel) {
            if (channel > 15 || channel < -1) {
                throw new IllegalArgumentException( "Invalid MIDI channel number: " + channel );
            }
            if (channels.length >= 16) {
                return false;
            }
            if (channel == -1) {
                channels = new int[16];
                for (int i = 0; i < channels.length; i++) {
                    channels[i] = i;
                }
                checkIfUpdateNeeded( input );
                return true;
            }
            if (channelIndex( channel ) >= 0) {
                return false;
            }
            int[] newChannels = new int[channels.length + 1];
            System.arraycopy( channels, 0, newChannels, 0, channels.length );
            newChannels[channels.length] = channel;
            channels = newChannels;
            checkIfUpdateNeeded( input );
            return true;
        }
        public boolean removeChannel(int channel) {
            int index = channelIndex( channel );
            if (index < 0) {
                return false;
            }
            int[] newChannels = new int[channels.length - 1];
            for (int i = 0; i < index; i++) {
                newChannels[i] = channels[i];
            }
            for (int i = index + 1; i < channels.length; i++) {
                newChannels[i - 1] = channels[i];
            }
            channels = newChannels;
            checkIfUpdateNeeded( input );
            return true;
        }
        public int[] getChannels() {
            int[] channels = new int[this.channels.length];
            System.arraycopy( this.channels, 0, channels, 0, channels.length );
            return channels;
        }
        public boolean isEmpty() {
            return (channels.length == 0);
        }
        public void removeAllChannels() {
            channels = new int[0];
            checkIfUpdateNeeded( input );
        }
        int channelIndex( int channel ) {
            for (int i = 0; i < channels.length; i++) {
                if (channels[i] == channel) {
                    return i;
                }
            }
            return -1;
        }
    }
    
    private class CountInReceiver implements Receiver {
        int count = -1;
        public void close() {
        }
        public void send( MidiMessage message, long timeStamp ) {
            if (!(message instanceof ShortMessage) || ((ShortMessage) message).getData2() == 0) {
                return;
            }
            if (count < 0) {
                count = metronome.getTactCounter();
            } else if (count != metronome.getTactCounter()) {
                try {
                    record();
                } catch (RecorderException rex) {
                    rex.printStackTrace();
                }
                metronome.removeMidiOutputReceiver( this );
            }
        }
    }
}
