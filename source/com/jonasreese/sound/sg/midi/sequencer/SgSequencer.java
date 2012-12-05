/*
 * Created on 04.10.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi.sequencer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;


/** Sequencer implementation in pure Java.
 */
public class SgSequencer implements Sequencer {

    private Sequencer sequencer;
    private MidiDevice.Info info;
    
    /**
     * Constructs a new <code>SgSequencer</code>.
     * @param info
     * @throws MidiUnavailableException
     */
    public SgSequencer(MidiDevice.Info info) throws MidiUnavailableException {
        MidiDevice.Info[] devs = MidiSystem.getMidiDeviceInfo();
        sequencer = null;
        for (int i = 0; i < devs.length && sequencer == null; i++) {
            if (!devs[i].equals(info)) {
                MidiDevice dev = MidiSystem.getMidiDevice(devs[i]);
                if (dev instanceof Sequencer) {
                    this.sequencer = (Sequencer) dev;
                }
            }
        }
        
        this.info = info;
    }
    
    public MidiDevice.Info getDeviceInfo() {
        return info;
    }
    
    /**
     * @return
     */
    public int getMaxReceivers() {
        return sequencer.getMaxReceivers();
        //return -1;
    }
    
    /**
     * @param arg0
     * @param arg1
     * @return
     */
    public int[] addControllerEventListener(ControllerEventListener arg0,
            int[] arg1) {
        return sequencer.addControllerEventListener(arg0, arg1);
    }
    /**
     * @param arg0
     * @return
     */
    public boolean addMetaEventListener(MetaEventListener arg0) {
        return sequencer.addMetaEventListener(arg0);
    }
    /**
     * 
     */
    public void close() {
        sequencer.close();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        return sequencer.equals(arg0);
    }
    /**
     * @return
     */
    public int getLoopCount() {
        return sequencer.getLoopCount();
    }
    /**
     * @return
     */
    public long getLoopEndPoint() {
        return sequencer.getLoopEndPoint();
    }
    /**
     * @return
     */
    public long getLoopStartPoint() {
        return sequencer.getLoopStartPoint();
    }
    /**
     * @return
     */
    public SyncMode getMasterSyncMode() {
        return sequencer.getMasterSyncMode();
    }
    /**
     * @return
     */
    public SyncMode[] getMasterSyncModes() {
        return sequencer.getMasterSyncModes();
    }
    /**
     * @return
     */
    public int getMaxTransmitters() {
        return sequencer.getMaxTransmitters();
    }
    /**
     * @return
     */
    public long getMicrosecondLength() {
        return sequencer.getMicrosecondLength();
    }
    /**
     * @return
     */
    public long getMicrosecondPosition() {
        return sequencer.getMicrosecondPosition();
    }
    /**
     * @return
     * @throws javax.sound.midi.MidiUnavailableException
     */
    public Receiver getReceiver() throws MidiUnavailableException {
        return sequencer.getReceiver();
    }
    
    /**
     * @return
     */
    public List<Receiver> getReceivers() {
    	return sequencer.getReceivers();
    }
    
    /**
     * @return
     */
    public Sequence getSequence() {
        return sequencer.getSequence();
    }
    /**
     * @return
     */
    public SyncMode getSlaveSyncMode() {
        return sequencer.getSlaveSyncMode();
    }
    /**
     * @return
     */
    public SyncMode[] getSlaveSyncModes() {
        return sequencer.getSlaveSyncModes();
    }
    /**
     * @return
     */
    public float getTempoFactor() {
        return sequencer.getTempoFactor();
    }
    /**
     * @return
     */
    public float getTempoInBPM() {
        return sequencer.getTempoInBPM();
    }
    /**
     * @return
     */
    public float getTempoInMPQ() {
        return sequencer.getTempoInMPQ();
    }
    /**
     * @return
     */
    public long getTickLength() {
        return sequencer.getTickLength();
    }
    /**
     * @return
     */
    public long getTickPosition() {
        return sequencer.getTickPosition();
    }
    /**
     * @param arg0
     * @return
     */
    public boolean getTrackMute(int arg0) {
        return sequencer.getTrackMute(arg0);
    }
    /**
     * @param arg0
     * @return
     */
    public boolean getTrackSolo(int arg0) {
        return sequencer.getTrackSolo(arg0);
    }
    /**
     * @return
     * @throws javax.sound.midi.MidiUnavailableException
     */
    public Transmitter getTransmitter() throws MidiUnavailableException {
        return sequencer.getTransmitter();
    }
    /**
     * @return
     */
    public List<Transmitter> getTransmitters() {
    	return sequencer.getTransmitters();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return sequencer.hashCode();
    }
    /**
     * @return
     */
    public boolean isOpen() {
        return sequencer.isOpen();
    }
    /**
     * @return
     */
    public boolean isRecording() {
        return sequencer.isRecording();
    }
    /**
     * @return
     */
    public boolean isRunning() {
        return sequencer.isRunning();
    }
    /**
     * @throws javax.sound.midi.MidiUnavailableException
     */
    public void open() throws MidiUnavailableException {
        sequencer.open();
    }
    /**
     * @param arg0
     */
    public void recordDisable(Track arg0) {
        sequencer.recordDisable(arg0);
    }
    /**
     * @param arg0
     * @param arg1
     */
    public void recordEnable(Track arg0, int arg1) {
        sequencer.recordEnable(arg0, arg1);
    }
    /**
     * @param arg0
     * @param arg1
     * @return
     */
    public int[] removeControllerEventListener(ControllerEventListener arg0,
            int[] arg1) {
        return sequencer.removeControllerEventListener(arg0, arg1);
    }
    /**
     * @param arg0
     */
    public void removeMetaEventListener(MetaEventListener arg0) {
        sequencer.removeMetaEventListener(arg0);
    }
    /**
     * @param arg0
     */
    public void setLoopCount(int arg0) {
        sequencer.setLoopCount(arg0);
    }
    /**
     * @param arg0
     */
    public void setLoopEndPoint(long arg0) {
        sequencer.setLoopEndPoint(arg0);
    }
    /**
     * @param arg0
     */
    public void setLoopStartPoint(long arg0) {
        sequencer.setLoopStartPoint(arg0);
    }
    /**
     * @param arg0
     */
    public void setMasterSyncMode(SyncMode arg0) {
        sequencer.setMasterSyncMode(arg0);
    }
    /**
     * @param arg0
     */
    public void setMicrosecondPosition(long arg0) {
        sequencer.setMicrosecondPosition(arg0);
    }
    /**
     * @param arg0
     * @throws java.io.IOException
     * @throws javax.sound.midi.InvalidMidiDataException
     */
    public void setSequence(InputStream arg0) throws IOException,
            InvalidMidiDataException {
        sequencer.setSequence(arg0);
    }
    /**
     * @param arg0
     * @throws javax.sound.midi.InvalidMidiDataException
     */
    public void setSequence(Sequence arg0) throws InvalidMidiDataException {
        sequencer.setSequence(arg0);
    }
    /**
     * @param arg0
     */
    public void setSlaveSyncMode(SyncMode arg0) {
        sequencer.setSlaveSyncMode(arg0);
    }
    /**
     * @param arg0
     */
    public void setTempoFactor(float arg0) {
        sequencer.setTempoFactor(arg0);
    }
    /**
     * @param arg0
     */
    public void setTempoInBPM(float arg0) {
        sequencer.setTempoInBPM(arg0);
    }
    /**
     * @param arg0
     */
    public void setTempoInMPQ(float arg0) {
        sequencer.setTempoInMPQ(arg0);
    }
    /**
     * @param arg0
     */
    public void setTickPosition(long arg0) {
        sequencer.setTickPosition(arg0);
    }
    /**
     * @param arg0
     * @param arg1
     */
    public void setTrackMute(int arg0, boolean arg1) {
        sequencer.setTrackMute(arg0, arg1);
    }
    /**
     * @param arg0
     * @param arg1
     */
    public void setTrackSolo(int arg0, boolean arg1) {
        sequencer.setTrackSolo(arg0, arg1);
    }
    /**
     * 
     */
    public void start() {
        sequencer.start();
    }
    /**
     * 
     */
    public void startRecording() {
        sequencer.startRecording();
    }
    /**
     * 
     */
    public void stop() {
        System.out.println("Sequencer.stop()");
        sequencer.stop();
    }
    /**
     * 
     */
    public void stopRecording() {
        System.out.println("Sequencer.stopRecording()");
        sequencer.stopRecording();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return sequencer.toString();
    }
    

    /**
     * Inner Info class implementation
     */
    public static class Info extends MidiDevice.Info {
        String name;
        String vendor;
        String description;
        String version;
        public Info(String name, String vendor, String description, String version) {
            super(name, vendor, description, version);
            this.name = name;
            this.vendor = vendor;
            this.description = description;
            this.version = version;
        }
    }
}