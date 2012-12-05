/*
 * Created on 19.01.2008
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.aucontainer;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiMessage;
import javax.sound.sampled.AudioFormat;

/**
 * This class wraps an Apple Audio Unit.
 * 
 * @author Jonas Reese
 */
public class AudioUnit {

    private AudioUnitDescriptor audioUnitDescriptor;
    private AudioFormat audioFormat;
    private int blockSize;
    private List<AudioUnitListener> listeners;
    private long handle;
    
    public AudioUnit( AudioUnitDescriptor audioUnitDescriptor ) throws AudioUnitNotAvailableException {
        this.audioUnitDescriptor = audioUnitDescriptor;
        listeners = new ArrayList<AudioUnitListener>();
        handle = createAudioUnit();
        if (handle == 0) {
            throw new AudioUnitNotAvailableException( "Could not create native peer for AudioUnit" );
        }
    }
    private native long createAudioUnit();
    
    
    public void addAudioUnitListener( AudioUnitListener listener ) {
        listeners.add( listener );
    }
    
    public void removeAudioUnitListener( AudioUnitListener listener ) {
        listeners.remove( listener );
    }

    /**
     * Gets the audio format.
     * @return The audio format.
     */
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    /**
     * Sets the desired audio format
     * @param audioFormat The audio format to set.
     */
    public void setAudioFormat(AudioFormat audioFormat) {
        this.audioFormat = audioFormat;
    }

    /**
     * Gets the processing block size in frames.
     * @return The block size.
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * Sets the processing block size.
     * @param blockSize The block size (in frames).
     */
    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }
    
    /**
     * Gets the number of parameters that can be set for this <code>AudioUnit</code>.
     * @return The parameter count.
     */
    public int getNumParams() {
        return 0;
    }
    
    /**
     * Returns the parameter value for the specified index.
     * @param index The index.
     * @return The parameter value, or 0 if index is out of range.
     */
    public float getParameter( int index ) {
        return 0;
    }
    
    /**
     * Sets the parameter at the specified index to the specified value.
     * @param index The index.
     * @param value The value.
     */
    public void setParameter( int index, float value ) {
        
    }
    
    /**
     * Checks if the parameter at the specified index can be automated.
     * @param index The parameter index.
     * @return <code>true</code> if the parameter can be automated, <code>false</code>
     * otherwise.
     */
    public boolean canParameterBeAutomated( int index ) {
        return true;
    }
    
    /**
     * Gets the parameter name for the parameter at the specified index.
     * @param index The index.
     * @return The parameter name, or <code>null</code> if the index is out of range.
     */
    public String getParameterName( int index ) {
        return "";
    }


    /**
     * Gets the <code>AudioUnitDescriptor</code> that is associated with this
     * <code>AudioUnit</code>.
     * @return the <code>AudioUnitDescriptor</code>, not <code>null</code>.
     */
    public AudioUnitDescriptor getDescriptor() {
        return audioUnitDescriptor;
    }
    
    /**
     * Gets the audio unit name.
     * @return the name.
     */
    public String getName() {
        return audioUnitDescriptor.getName();
    }
    
    /**
     * Opens this audio unit for processing.
     */
    public void open() {
        
    }
    
    /**
     * Closes this audio unit.
     */
    public void close() {
        
    }
    
    /**
     * Checks if this audio unit can receive MIDI events.
     * @return <code>true</code> if the audio unit has a MIDI input, <code>false</code>
     * otherwise.
     */
    public boolean hasMidiInput() {
        return true;
    }
    
    /**
     * Checks if this audio unit can send MIDI events.
     * @return <code>true</code> if the audio unit has a MIDI output, <code>false</code>
     * otherwise.
     */
    public boolean hasMidiOutput() {
        return true;
    }
    
    /**
     * Returns the audio input count.
     * @return The number of audio inputs, e.g. 2 for "stereo".
     */
    public int getNumInputs() {
        return 2;
    }
    
    /**
     * Returns the audio output count.
     * @return The number of audio outputs, e.g. 2 for "stereo".
     */
    public int getNumOutputs() {
        return 2;
    }

    /**
     * Set the tempo for this <code>AudioUnit</code>.
     * @param bpm The tempo in beats per minute.
     */
    public void setTempo( float bpm ) {
        // TODO: implement this
    }
    
    /**
     * Gets the tempo for this <code>AudioUnit</code>.
     * @return The tempo in beats per minute.
     */
    public float getTempo() {
        // TODO: implement this
        return 120;
    }
    
    /**
     * Sends a MIDI event to this audio unit.
     * @param m The MIDI message.
     */
    public void processMidi( MidiMessage m ) {
        
    }

    /**
     * Process byte data.
     * @param data The data array.
     * @param numInputs The number of inputs.
     * @param numOutputs The number of outputs.
     * @param frameSize The frame size.
     * @param bigEndian <code>true</code> if and only if bigEndian is used.
     * @param encoding The audio encoding type.
     */
    public void process(
            byte[] data,
            int numInputs,
            int numOutputs,
            int frameSize,
            boolean bigEndian,
            AudioFormat.Encoding encoding ) {
        System.out.println( "AudioUnit.process()" );
        process( handle, data, numInputs, numOutputs, frameSize );
    }
    
    private native void process(
            long handle,
            byte[] data,
            int numInputs,
            int numOutputs,
            int frameSize );
    
    public void openEditWindow() {
        
    }

    
    public void finalize() throws Throwable {
        try {
            destroyAudioUnit( handle );
        } finally {
            super.finalize();
        }
    }
    public void destroyAudioUnit() {
        destroyAudioUnit( handle );
    }
    private native void destroyAudioUnit( long handle );
}
