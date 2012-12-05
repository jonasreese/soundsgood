/*
 * Created on 03.10.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Receiver;

import com.jonasreese.sound.sg.Recorder;
import com.jonasreese.sound.sg.RecorderException;

/**
 * <p>
 * This interface defines methods required to play or record midi sequences.
 * It shall be implemented by classes that represent the abstraction layer
 * between the low-level MIDI device(s) and high-level functionalities.
 * </p>
 * @author jonas.reese
 */
public interface MidiRecorder extends Recorder {
    /**
     * Starts playing from the left marker position to the right marker position.
     * @throws RecorderException if the midi output device could not
     *         be prepared.
     */
    public void playFromLeftToRightMarker() throws RecorderException;

    /**
     * Gets the playing mode status info for <i>from left to right marker</i>.
     * @return <code>true</code> if this <code>MidiRecorder</code> is playing
     *         a sequence from the left to the right marker position.
     */
    public boolean isPlayingFromLeftToRightMarker();

    /**
     * Starts playing from the left marker position to the end of the associated sequence.
     * @throws RecorderException if the midi output device could not
     *         be prepared.
     */
    public void playFromLeftMarker() throws RecorderException;

    /**
     * Gets the playing mode status info for <i>from left marker</i>.
     * @return <code>true</code> if this <code>MidiRecorder</code> is playing
     *         a sequence from the left marker position to the end.
     */
    public boolean isPlayingFromLeftMarker();

    /**
     * Starts playing from the current position to the right marker position
     * or to the end if no right marker position is set..
     * @throws RecorderException if the midi output device could not
     *         be prepared.
     */
    public void playToRightMarker() throws RecorderException;

    /**
     * Gets the playing mode status info for <i>to right marker</i>.
     * @return <code>true</code> if this <code>MidiRecorder</code> is playing
     *         a sequence from the starting position to the right marker position.
     */
    public boolean isPlayingToRightMarker();
    
    /**
     * Prepares this <code>MidiRecorder</code> so that the next playback can start with
     * very little delay.
     */
    public void preparePlayback();
    
    /**
     * Has the same effect as the <code>start()</code> method.
     * @throws RecorderException if the midi output device could not
     *         be prepared.
     */
    public void play() throws RecorderException;
    
    /**
     * Plays from the beginning to the end in an infinite loop, until
     * <code>stop()</code> is called.
     * @throws RecorderException if the midi output device could not
     *         be prepared.
     */
    public void loop() throws RecorderException;
    
    /**
     * Returns <code>true</code> if this <code>MidiRecorder</code> is currently
     * playing in a loop, <code>false</code> otherwise.
     * @return <code>true</code> if and only if currently looping.
     */
    public boolean isLooping();
    
    /**
     * Plays from left to right marker in an infinite loop, until <code>stop()</code>
     * is called.
     * @throws RecorderException if the midi output device could not
     *         be prepared.
     */
    public void loopFromLeftToRightMarker() throws RecorderException;

    /**
     * Returns <code>true</code> if this <code>MidiRecorder</code> is currently
     * playing in a loop from the left to the right marker, <code>false</code> otherwise.
     * @return <code>true</code> if and only if currently looping from left to right marker.
     */
    public boolean isLoopingFromLeftToRightMarker();
    
    /**
     * Gets this <code>MidiRecorder</code>'s output (playback) <code>MidiDeviceMap</code>.
     * The MIDI output device map is valid for all tracks (that are not muted).
     * @return The MIDI output map (not <code>null</code>).
     */
    public MidiDeviceMap getMidiOutputMap();
    
    /**
     * Plays a single MIDI note. This cannot be stopped.
     * @param noteOn The <code>MidiEvent</code> containing the MIDI note to be played.
     * @param noteOff The <code>MidiEvent</code> that stops the note. May be <code>null</code>.
     * @throws RecorderException if the midi output device could not
     *         be prepared.
     */
    public void playSingleNote( MidiEvent noteOn, MidiEvent noteOff ) throws RecorderException;
    
    /**
     * Stops the playback/recording.
     */
    public void stop();

    /**
     * Asks this <code>MidiPlayer</code> if a playback is currently running.
     * @return <code>true</code> if currently playing, <code>false</code> otherwise.
     */
    public boolean isPlaying();

    /**
     * Gets the tick where the left marker is located.
     * @return Returns the left marker tick position.
     */
    public long getLeftMarkerTick();

    /**
     * Sets the tick where the left marker is located.
     * @param leftMarkerTick The leftMarkerTick to set.
     */
    public void setLeftMarkerTick( long leftMarkerTick );

    /**
     * Removes the left marker (sets it's tick position to -1).
     */
    public void removeLeftMarker();

    /**
     * Gets the tick where the right marker is located.
     * @return Returns the right marker tick position.
     */
    public long getRightMarkerTick();

    /**
     * Sets the tick where the right marker is located.
     * @param rightMarkerTick The rightMarkerTick to set.
     */
    public void setRightMarkerTick( long rightMarkerTick );

    /**
     * Removes the right marker (sets it's tick position to -1).
     */
    public void removeRightMarker();
    
    /**
     * Starts the recording.
     * @throws RecorderException if the recording MIDI device could not
     *         be prepared.
     */
    public void record() throws RecorderException;
    
    /**
     * Starts the recording after click has been started for one tact.
     * @throws RecorderException if the recording MIDI device could not
     *         be prepared.
     */
    public void clickAndRecord() throws RecorderException;
    
    /**
     * Sets the enabled state for the record loopback.
     * @param recordLoopbackEnabled If set to <code>true</code>, MIDI events received
     * from the input device will be forwarded to the current output device during
     * a recording process, otherwise not.
     * @throws RecorderException if this <code>MidiRecorder</code> is currently
     * recording and the MIDI output device could not be connected to the input device.
     * If this <code>MidiRecorder</code> is not currently recording, no exception will be
     * thrown until the next time the <code>record()</code> method is called.
     */
    public void setRecordLoopbackEnabled( boolean recordLoopbackEnabled ) throws RecorderException;
    
    /**
     * Gets the current enabled state for the record loopback.
     * @return <code>true</code> if MIDI events received from the input device are
     * forwarded to the current output device during a recording process,
     * <code>false</code> otherwise.
     */
    public boolean isRecordLoopbackEnabled();
    
    /**
     * Sets the permanent loopback (MIDI thru) mode to enabled/disabled.
     * @param permanentLoopbackEnabled The permanent loopback state to set.
     * @throws RecorderException If the current input device could not be connected to
     * the current output device.
     */
    public void setLoopbackEnabled( boolean permanentLoopbackEnabled ) throws RecorderException;
    
    /**
     * Gets the current permanent loopback (MIDI thru) state.
     * @return <code>true</code> if permanent loopback is currently enabled, <code>false</code> otherwise.
     */
    public boolean isLoopbackEnabled();
    
    /**
     * Gets the <code>recording</code> state.
     * @return <code>true</code> if this <code>MidiRecorder</code> is currently recording,
     * <code>false</code> otherwise.
     */
    public boolean isRecording();
    
    /**
     * Gets this <code>MidiRecorder</code>'s input (recording) <code>MidiDeviceMap</code>.
     * @param track The <code>TrackProxy</code> to get the MIDI input map for.
     * @return The MIDI input map (not <code>null</code>).
     */
    public MidiDeviceMap getMidiInputMap( TrackProxy track );
    
    /**
     * Enables/disables the record mode for the given <code>TrackProxy</code> on the given
     * MIDI channel.
     * @param trackProxy The <code>TrackProxy</code> to enable recording.
     * @param enabled
     * @throws IllegalStateException if this <code>MidiRecorder</code> is currently recording.
     */
    public void setRecordEnabled( TrackProxy trackProxy, boolean enabled ) throws IllegalStateException;
    
    /**
     * Gets the record enabled for any track status.
     * @return <code>true</code> if one of the tracks is in record mode for
     *         any channel, <code>false</code> otherwise.
     */
    public boolean isRecordEnabledForAnyTrack();
    
    /**
     * Gets the <code>record enabled</code> state for the given <code>TrackProxy</code>.
     * @param trackProxy The track to check for.
     * @return <code>true</code> if and only if record is enabled for any channel on
     *         the given track.
     */
    public boolean isRecordEnabled( TrackProxy trackProxy );
    
    /**
     * Gets the total play length in ticks.
     * @return The length in MIDI ticks.
     */
    public long getTickLength();
    
    /**
     * Gets the current tick position.
     * @return The current MIDI tick position.
     */
    public long getTickPosition();

    /**
     * Sets the tick position.
     * @param tick The MIDI tick position to set.
     */
    public void setTickPosition( long tick );

    /**
     * Gets the play length in microseconds.
     * @return The microsecond length.
     */
    public long getMicrosecondLength();

    /**
     * Sets the position to the given microsecond position.
     * @param pos The microsecond position to be set.
     */
    public void setMicrosecondPosition( long pos );

    /**
     * Gets the current microsecond position.
     * @return The current position in microseconds.
     */
    public long getMicrosecondPosition();

    /**
     * Sets the tick position to the left marker tick position.
     */
    public void jumpToLeftMarker();
    
    /**
     * Sets the tick position to the right marker tick position.
     */
    public void jumpToRightMarker();

    /**
     * Sets the tick position to the end of the associated sequence.
     */
    public void jumpToEnd();
    
    /**
     * Sets the next playback to be muted/unmuted.
     * @param muteNextPlayback If <code>true</code>, indicates that for the next
     * call to one of the <code>play...()</code> methods, the configured output
     * devices shall not receive any events. As soon as the playback starts, this
     * flag is reset to <code>false</code>.
     */
    public void setNextPlaybackMuted( boolean muteNextPlayback );
    
    /**
     * Sets the MIDI track at the given index to mute/unmute.
     * @param track The track to be muted/unmuted.
     * @param mute If <code>true</code>, the given track is muted, otherwise unmuted.
     */
    public void setTrackMuted( TrackProxy track, boolean mute );

    /**
     * Gets the <code>mute</code> state for the MIDI track at the given index.
     * @param track The track whose mute state is to be checked.
     */
    public boolean isTrackMuted( TrackProxy track );

    /**
     * Sets the <code>solo</code> state for the MIDI track at the given index.
     * @param track The MIDI track.
     * @param solo The solo state to set.
     */
    public void setTrackSolo( TrackProxy track, boolean solo );

    /**
     * Gets the <code>solo</code> state for the MIDI track at the given index.
     * @param track The MIDI track to get the <code>solo</code> state for.
     */
    public boolean isTrackSolo( TrackProxy track );
    
    /**
     * Gets the <code>Metronome</code> instance associated with this <code>MidiRecorder</code>.
     * @return The <code>Metronome</code>. Is not <code>null</code>.
     */
    public Metronome getMetronome();
    
    /**
     * Adds a <code>MidiUpdatable</code> to this <code>MidiRecorder</code>.
     * @param updatable The <code>MidiUpdatable</code> to add.
     */
    public void addMidiUpdatable( MidiUpdatable updatable );

    /**
     * Removes a <code>DeviceUpdatable</code> from this <code>MidiRecorder</code>.
     * @param updatable The <code>DeviceUpdatable</code> to be removed.
     */
    public void removeMidiUpdatable( MidiUpdatable updatable );
    
    /**
     * Adds a MIDI output receiver to this <code>MidiRecorder</code>. An output
     * MIDI receiver receives all MIDI events that are sent to any output device.
     * @param midiOutputReceiver The <code>Receiver</code> that shall receive MIDI
     * events after being sent to an output device. If the given
     * <code>MidiOutputReceiver</code> has already been added, this method does nothing.
     */
    public void addMidiOutputReceiver( Receiver midiOutputReceiver );

    /**
     * Removes the given MIDI output receiver from this <code>MidiRecorder</code>.
     * @param midiOutputReceiver The MIDI output receiver that shall no longer
     * receive any MIDI events after being sent to an output device. If the given
     * <code>Receiver</code> is not registered as MIDI output receiver, this method
     * does nothing.
     */
    public void removeMidiOutputReceiver( Receiver midiOutputReceiver );
    
    /**
     * Starts the <code>fastForward</code> mode. During the time this mode
     * is active, the tick position advances quickly. Call the
     * <code>stopFastForward()</code> method to stop this mode.
     */
    public void startFastForward();

    /**
     * Stops the <code>fastForward</code> mode. This method has only an effect
     * if <code>isInFastForwardMode()</code> returns <code>true</code>.
     */
    public void stopFastForward();

    /**
     * Returns <code>true</code> if the <code>startFastForward()</code> method has
     * been called recently and the <code>stopFastForward</code> method has not
     * yet been called inbetween. Please note that the <code>stopFastForward()</code>
     * method may be called implicitly by some other methods within this class. 
     * @return <code>true</code> if in fastForward mode, <code>false</code> otherwise. 
     */
    public boolean isInFastForwardMode();

    /**
     * Starts the <code>fastBackward</code> mode. During the time this mode
     * is active, the tick position advances quickly backwards. Call the
     * <code>stopFastBackward()</code> method to stop this mode.
     */
    public void startFastBackward();

    /**
     * Stops the <code>fastBackward</code> mode. This method has only an effect
     * if <code>isInFastBackwardMode()</code> returns <code>true</code>.
     */
    public void stopFastBackward();

    /**
     * Returns <code>true</code> if the <code>startFastBackward()</code> method has
     * been called recently and the <code>stopFastBackward</code> method has not
     * yet been called inbetween. Please note that the <code>stopFastBackward()</code>
     * method may be called implicitly by some other methods within this class. 
     * @return <code>true</code> if in fastBackward mode, <code>false</code> otherwise. 
     */
    public boolean isInFastBackwardMode();

    /**
     * Gets the current tempo in beats per minute.
     * @return The current tempo.
     */
    public float getTempoInBPM();
    
    /**
     * Sets the current tempo in beats per minute.
     * @param bpm The temp to set.
     */
    public void setTempoInBPM( float bpm );
    
    /**
     * Gets the current temp in microseconds per quarternote.
     * @return The current tempo.
     */
    public float getTempoInMPQ();
    
    /**
     * Sets the current temp in microseconds per quarternote.
     * @param mpq The temp to set.
     */
    public void setTempoInMPQ( float mpq );
    
    /**
     * Persists this <code>MidiRecorder</code>'s recording state
     * (input device map, recording enabled states) to the
     * underlying <code>MidiDescriptor</code> so that it can be restored
     * using the <code>restorePlaybackState()</code> method when the
     * <code>MidiDescriptor</code>'s state is restored from the session.
     */
    public void persistRecordingState();
    
    /**
     * Restores this <code>MidiRecorder</code>'s recording state
     * (input device map, recording enabled states) from the underlying
     * <code>MidiDescriptor</code> if possible.
     */
    public void restoreRecordingState();
    
    /**
     * Persists this <code>MidiRecorder</code>'s playback state
     * (output device map, mute and solo states) to the
     * underlying <code>MidiDescriptor</code> so that it can be restored
     * using the <code>restorePlaybackState()</code> method when the
     * <code>MidiDescriptor</code>'s state is restored from the session.
     */
    public void persistPlaybackState();

    /**
     * Restores this <code>MidiRecorder</code>'s playback state
     * (output device map, mute and solo states) from the underlying
     * <code>MidiDescriptor</code> if possible.
     */
    public void restorePlaybackState();
}