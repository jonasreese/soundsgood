/*
 * Created on 10.07.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.Metronome;
import com.jonasreese.sound.sg.midi.MidiDeviceDescriptor;
import com.jonasreese.sound.sg.midi.MidiDeviceList;
import com.jonasreese.sound.sg.midi.MidiToolkit;

/**
 * @author jonas.reese
 */
public class MetronomeImpl implements Metronome, PropertyChangeListener {
    
    private MetronomeReceiver metronomeReceiver;
    private MidiEvent midiClockEvent;
    private MidiEvent onEvent;
    private MidiEvent offEvent;
    private MidiEvent firstOnEvent;
    private MidiEvent firstOffEvent;
    private int beatsPerTact;
    private int tactCounter;
    
    private boolean sendMidiClockEnabled;
    
    private boolean stress;
    
    private boolean running;
    private float tempoInBpm;
    
    private boolean updateOnPropertyChange;
    private boolean defaultDeviceOutputEnabled;
    
    private MetronomeThread metronomeThread;
    
    private ArrayList<Receiver> midiOutputReceivers;
    
    /**
     * Constructs new <code>MetronomeImpl</code> object.
     * @param updateOnPropertyChange If <code>true</code>, a <code>PropertyChangeListener</code>
     * is installed that updates the settings for this metronome using those from the
     * SoundsGood application properties.
     */
    public MetronomeImpl( boolean updateOnPropertyChange ) {
        running = false;
        tactCounter = 0;
        midiOutputReceivers = new ArrayList<Receiver>();
        tempoInBpm = 120;
        this.updateOnPropertyChange = updateOnPropertyChange;
        initMetronome();
    }
    
    private void initMetronome() {
        ShortMessage clockMsg = new ShortMessage();
        ShortMessage onMsg = new ShortMessage();
        ShortMessage offMsg = new ShortMessage();
        stress = SgEngine.getInstance().getProperties().isStressOnClickOne();
        ShortMessage firstOnMsg = (stress ? new ShortMessage() : onMsg);
        ShortMessage firstOffMsg = (stress ? new ShortMessage() : offMsg);
        short[] c = SgEngine.getInstance().getProperties().getNoteClick();
        short[] c0 = (stress ? SgEngine.getInstance().getProperties().getNoteClickOne() : c);
        try {
            clockMsg.setMessage( ShortMessage.TIMING_CLOCK );
            onMsg.setMessage( ShortMessage.NOTE_ON, c[1], c[0], c[2] );
            offMsg.setMessage( ShortMessage.NOTE_ON, c[1], c[0], 0 );
            if (stress) {
                firstOnMsg.setMessage( ShortMessage.NOTE_ON, c0[1], c0[0], c0[2] );
                firstOffMsg.setMessage( ShortMessage.NOTE_ON, c0[1], c0[0], 0 );
            }
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
        midiClockEvent = new MidiEvent( clockMsg, 0 );
        firstOnEvent = new MidiEvent( firstOnMsg, c0[3] );
        firstOffEvent = new MidiEvent( firstOffMsg, c0[3] );
        onEvent = new MidiEvent( onMsg, c[3] );
        offEvent = new MidiEvent( offMsg, c[3] );
        beatsPerTact = SgEngine.getInstance().getProperties().getClicksPerTact();
    }
    
    public void addMidiOutputReceiver( Receiver midiOutputReceiver ) {
        synchronized (midiOutputReceivers) {
            if (!midiOutputReceivers.contains( midiOutputReceiver )) {
                midiOutputReceivers.add( midiOutputReceiver );
            }
        }
    }

    public void removeMidiOutputReceiver( Receiver midiOutputReceiver ) {
        synchronized (midiOutputReceivers) {
            midiOutputReceivers.remove( midiOutputReceiver );
        }
    }

    public void setSendMidiClockEnabled(boolean sendMidiClockEnabled) {
        this.sendMidiClockEnabled = sendMidiClockEnabled;
    }
    
    public boolean isSendMidiClockEnabled() {
        return sendMidiClockEnabled;
    }

    
    public synchronized void start() {
        if (running) {
            return;
        }
        if (this.updateOnPropertyChange) {
            if (stress != SgEngine.getInstance().getProperties().isStressOnClickOne()) {
                initMetronome();
            } else if (beatsPerTact != SgEngine.getInstance().getProperties().getClicksPerTact()) {
                beatsPerTact = SgEngine.getInstance().getProperties().getClicksPerTact();
            }
        }
        
        metronomeThread = new MetronomeThread();
        metronomeThread.setPriority( Thread.MAX_PRIORITY );
        metronomeReceiver = new MetronomeReceiver( defaultDeviceOutputEnabled );
        SgEngine.getInstance().getProperties().addPropertyChangeListener( this );
        running = true;
        metronomeThread.start();
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        if (metronomeThread != null) {
            metronomeThread.interrupt();
        }
        SgEngine.getInstance().getProperties().removePropertyChangeListener( this );
        try {
            wait( 5000 ); // wait for the thread to stop
        } catch (InterruptedException iex) {
        }
    }
    
    public synchronized void sync() {
        if (metronomeThread != null) {
            metronomeThread.interrupt();
        }
    }
    
    public boolean isRunning() {
        return running;
    }

    public synchronized void setTempoInBpm( float tempoInBpm ) {
        this.tempoInBpm = tempoInBpm;
        if (metronomeThread != null) {
            metronomeThread.nanosPerClick = MidiToolkit.bpmToNanosPerQuarternote( tempoInBpm );
            metronomeThread.nanosPerMidiClock = metronomeThread.nanosPerClick / 24.0;
        }
    }

    public float getTempoInBpm() {
        return tempoInBpm;
    }

    public float getTempoInMpq() {
        return MidiToolkit.bpmToMPQ( tempoInBpm );
    }

    public void setTempoInMpq( float tempoInMpq ) {
        setTempoInBpm( MidiToolkit.mpqToBPM( tempoInMpq ) );
    }
    
    public int getBeatsPerTact() {
        return beatsPerTact;
    }

    public void setBeatsPerTact( int beatsPerTact ) {
        this.beatsPerTact = beatsPerTact;
    }
    
    public int getTactCounter() {
        return tactCounter;
    }

    public void propertyChange( PropertyChangeEvent e ) {
        if (!updateOnPropertyChange && defaultDeviceOutputEnabled &&
                "midiClickDeviceList".equals( e.getPropertyName() )) {
            stop();
            start();
        } else if (updateOnPropertyChange) {
            if ("midiClickDeviceList".equals( e.getPropertyName() ) ||
                    "stressOnClickOne".equals( e.getPropertyName() ) ||
                    "clicksPerTact".equals( e.getPropertyName() ) ||
                    "noteClickOne".equals( e.getPropertyName() ) ||
                    "noteClick".equals( e.getPropertyName() )) {
                if (running) {
                    stop();
                    initMetronome();
                    start();
                }
            }
        }
    }
    
    class MetronomeReceiver implements Receiver {

        Receiver[] receivers;
        MidiDevice[] devices;

        MetronomeReceiver( boolean defaultDeviceOutputEnabled ) {
            if (defaultDeviceOutputEnabled) {
                MidiDeviceList deviceList = SgEngine.getInstance().getProperties().getMidiClickDeviceList();
                devices = new MidiDevice[deviceList.getCount()];
                receivers = new Receiver[devices.length];
                for (int i = 0; i < devices.length; i++) {
                    MidiDeviceDescriptor devInfo = (MidiDeviceDescriptor) deviceList.getMidiDeviceDescriptor( i );
                    try {
                        devices[i] = MidiToolkit.getMidiDevice( devInfo );
                        devices[i].open();
                        receivers[i] = devices[i].getReceiver();
                    } catch (MidiUnavailableException ignored) {
                        devices[i].close();
                    }
                }
            } else {
                receivers = null;
                devices = null;
            }
        }
        
        public void send( MidiMessage m, long tick ) {
            if (receivers != null) {
                for (int i = 0; i < receivers.length; i++) {
                    if (receivers[i] != null) {
                        receivers[i].send( m, tick );
                    }
                }
            }
            if (!midiOutputReceivers.isEmpty()) {
                synchronized (midiOutputReceivers) {
                    for (int i = 0; i < midiOutputReceivers.size(); i++) {
                        Receiver r = midiOutputReceivers.get( i );
                        r.send( m, tick );
                    }
                }
            }
        }

        public void close() {
            if (receivers != null) {
                for (int i = 0; i < receivers.length; i++) {
                    if (receivers[i] != null) {
                        receivers[i].close();
                        devices[i].close();
                    }
                }
            }
            if (midiOutputReceivers != null && !midiOutputReceivers.isEmpty()) {
                synchronized (midiOutputReceivers) {
                    for (int i = 0; i < midiOutputReceivers.size(); i++) {
                        Receiver r = midiOutputReceivers.get( i );
                        r.close();
                    }
                }
            }
        }
    }



    public boolean isDefaultDeviceOutputEnabled() {
        return defaultDeviceOutputEnabled;
    }

    public void setDefaultDeviceOutputEnabled(boolean defaultDeviceOutputEnabled) {
        this.defaultDeviceOutputEnabled = defaultDeviceOutputEnabled;
    }
    
    
    class MetronomeThread extends Thread {
        double nanosPerClick;
        double nanosPerMidiClock;
        long nextClickFireTime;
        long nextMidiClockFireTime;
        long nextClickOffFireTime;
        
        public MetronomeThread() {
            super( "Metronome Thread" );
        }
        public void run() {
            boolean on = true;
            nanosPerClick = MidiToolkit.bpmToNanosPerQuarternote( tempoInBpm );
            nanosPerMidiClock = nanosPerClick / 24.0;
            nextClickFireTime = System.nanoTime();
            nextMidiClockFireTime = nextClickFireTime;
            nextClickOffFireTime = Long.MAX_VALUE;
            
            int c = 0;
            int click = 0;
            boolean clock = false;
            while (running) {
                try {
                    if (clock) {
                        metronomeReceiver.send( midiClockEvent.getMessage(), 0 );
                        nextMidiClockFireTime += (long) nanosPerMidiClock;
                    } else {
                        click = c % beatsPerTact;
                        if (click == 0) {
                            tactCounter++;
                        }
                        MidiEvent event = (click == 0 ? (on ? firstOnEvent : firstOffEvent) : (on ? onEvent : offEvent));
                        metronomeReceiver.send( event.getMessage(), 0 );
                        if (on) {
                            if (sendMidiClockEnabled) { // sync to tact
                                nextMidiClockFireTime = nextClickFireTime + (long) nanosPerMidiClock;
                            }
                            c++;
                            nextClickOffFireTime = nextClickFireTime + (long)(event.getTick() * 1000000L);
                            nextClickFireTime += (long) nanosPerClick;
                        } else {
                            nextClickOffFireTime = Long.MAX_VALUE;
                        }
                    }
                    long nextFireTime;
                    if (sendMidiClockEnabled) {
                        if (nextClickOffFireTime < nextMidiClockFireTime) {
                            nextFireTime = nextClickOffFireTime;
                            //System.out.println("scheduling note off   " + (nextFireTime / 1000000));
                            on = false;
                            clock = false;
                            nextClickOffFireTime = Long.MAX_VALUE;
                        } else if (nextMidiClockFireTime <= nextClickFireTime) {
                            nextFireTime = nextMidiClockFireTime;
                            //System.out.println("scheduling midi clock " + (nextFireTime / 1000000));
                            clock = true;
                        } else {
                            nextFireTime = nextClickFireTime;
                            //System.out.println("scheduling note on    " + (nextFireTime / 1000000));
                            on = true;
                            clock = false;
                        }
                    } else {
                        if (nextClickOffFireTime < nextClickFireTime) {
                            nextFireTime = nextClickOffFireTime;
                            on = false;
                            nextClickOffFireTime = Long.MAX_VALUE;
                        } else {
                            nextFireTime = nextClickFireTime;
                            on = true;
                        }
                    }
                    waitUntil( nextFireTime );
                } catch (InterruptedException e) {
                    boolean wasOn = false;
                    if (on) {
                        wasOn = true;
                        MidiEvent event = (click == 0 ? firstOffEvent : offEvent);
                        metronomeReceiver.send( event.getMessage(), click );
                        on = true;
                    }
                    if (running) { // sync
                        long newNanoTime = System.nanoTime();
                        if (wasOn) {
                            c--;
                        } else {
                            long lastDistance = Math.abs( newNanoTime - nextClickFireTime );
                            long nextDistance = Math.abs( newNanoTime - nextClickFireTime - (long) nanosPerClick );
                            if (nextDistance < lastDistance) {
                                c--;
                            }
                        }
                        on = true;
                        nextClickFireTime = newNanoTime;
                    }
                }
            }
            
            synchronized (MetronomeImpl.this) {
                MetronomeImpl.this.notify();
            }
            metronomeReceiver.close();
        }
        private void waitUntil( long nanoTime ) throws InterruptedException {
            long timeDiff = nanoTime - System.nanoTime();
            if (timeDiff <= 0) {
                //System.out.println("timeDiff <= 0 (" + timeDiff + ")");
                return;
            }
            /*if (nanoTime == Long.MAX_VALUE) {
                System.out.println("nanoTime = Long.MAX_VALUE");
            }*/
            int nanos = (int) (timeDiff % 1000000);
            sleep( timeDiff / 1000000, nanos );
        }
    }
}
