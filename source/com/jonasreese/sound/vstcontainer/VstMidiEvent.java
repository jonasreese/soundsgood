/*
 * Created on 02.06.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.vstcontainer;

/**
 * @author jonas.reese
 */
public class VstMidiEvent extends VstEvent {

    private int deltaFrames;
    private int noteLength;
    private int noteOffset;
    private byte midiData0;
    private byte midiData1;
    private byte midiData2;
    private byte detune;
    private byte noteOffVelocity;
            
    /**
     * Constructs a new <code>VstMidiEvent</code> with no user-specific fields set.
     */
    public VstMidiEvent() {
        super( 0, 0 );
    }
    
    /**
     * Constructs a new <code>VstMidiEvent</code> from the given parameters.
     * @param deltaFrames
     * @param noteLength
     * @param noteOffset
     * @param midiData0
     * @param midiData1
     * @param midiData2
     * @param detune
     * @param noteOffVelocity
     */
    public VstMidiEvent(
            int deltaFrames,
            int noteLength,
            int noteOffset,
            byte midiData0,
            byte midiData1,
            byte midiData2,
            byte detune,
            byte noteOffVelocity ) {
        super( deltaFrames, 0 );
        setNoteLength( noteLength );
        setNoteOffset( noteOffset );
        setMidiData0( midiData0 );
        setMidiData1( midiData0 );
        setMidiData2( midiData0 );
        setDetune( detune );
        setNoteOffVelocity( noteOffVelocity );
    }
    
    public int getType() {
        return VST_MIDI_TYPE;
    }

    public int getDeltaFrames() {
        return deltaFrames;
    }

    public void setDeltaFrames(int deltaFrames) {
        this.deltaFrames = deltaFrames;
    }

    public byte getDetune() {
        return detune;
    }

    public void setDetune(byte detune) {
        this.detune = detune;
    }

    public byte getMidiData0() {
        return midiData0;
    }

    public void setMidiData0(byte midiData0) {
        this.midiData0 = midiData0;
    }

    public byte getMidiData1() {
        return midiData1;
    }

    public void setMidiData1(byte midiData1) {
        this.midiData1 = midiData1;
    }

    public byte getMidiData2() {
        return midiData2;
    }

    public void setMidiData2(byte midiData2) {
        this.midiData2 = midiData2;
    }

    public int getNoteLength() {
        return noteLength;
    }

    public void setNoteLength(int noteLength) {
        this.noteLength = noteLength;
    }

    public int getNoteOffset() {
        return noteOffset;
    }

    public void setNoteOffset(int noteOffset) {
        this.noteOffset = noteOffset;
    }

    public byte getNoteOffVelocity() {
        return noteOffVelocity;
    }

    public void setNoteOffVelocity(byte noteOffVelocity) {
        this.noteOffVelocity = noteOffVelocity;
    }

    
}
