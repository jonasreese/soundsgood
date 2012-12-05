/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 20.11.2003
 */
package com.jonasreese.sound.sg.midi;

/**
 * <b>
 * An interface that shall be implemented by classes that wish to
 * receive update calls on <code>MidiRecorder</code>s.
 * </b>
 * @author jreese
 */
public interface MidiUpdatable
{
    /// An update hint constant that indicates that the midi device tick position has changed.
    public static final int TICK = 1;
    /// An update hint constant that indicates that the midi device left marker tick position has changed.
    public static final int LEFT_MARKER_TICK = 2;
    /// An update hint constant that indicates that the midi device right marker tick position has changed.
    public static final int RIGHT_MARKER_TICK = 4;
    /// An update hint constant that indicates that the record enabled state for at least one of the tracks has changed.
    public static final int RECORD_ENABLE_STATE = 8;
    /// An update hint constant that indicates that the solo state for one of the tracks has changed.
    public static final int SOLO_STATE = 16;
    /// An update hint constant that indicates that the mute state for at least one of the tracks has changed.
    public static final int MUTE_STATE = 32;
    /// An update hint constant that indicates that the permanent loopback state has changed.
    public static final int LOOPBACK_STATE = 64;
    /// An update hint constant that indicates that the record loopback state has changed.
    public static final int RECORD_LOOPBACK_STATE = 128;
    /// An update hint constant that indicates that all state attributes of the midi device have
    /// (or might have) changed.
    public static final int ALL = Integer.MAX_VALUE;
    
    /**
     * Invoked for <code>MidiRecorder</code> update.
     * @param recorder The <code>MidiRecorder</code> that updates.
     * @param midiDescriptor The <code>MidiDescriptor</code> belonging
     *        to the sequence currently played by <code>recorder</code>.
     * @param updateHint A bitmask carrying information about what has
     *        changed since last <code>deviceUpdate</code> call. See
     *        constant declarations.
     */
    public void deviceUpdate( MidiRecorder recorder, MidiDescriptor midiDescriptor, int updateHint );
}
