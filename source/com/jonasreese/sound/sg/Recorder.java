/*
 * Created on 18.12.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg;

/**
 * <p>
 * This interface defines methods required to play or record media sequences.
 * It shall be implemented by classes that represent the abstraction layer
 * between the low-level media device(s) and high-level functionalities.
 * </p>
 * @author Jonas Reese
 */
public interface Recorder {
    /**
     * Adds a <code>RecorderListener</code> to this <code>Recorder</code>.
     * @param l The listener to add. If it has already been added, this method does
     * nothing.
     */
    public void addRecorderListener( RecorderListener l );
    
    /**
     * Removes a <code>RecorderListener</code> from this <code>Recorder</code>.
     * @param l The listener to be removed. If it is not registered, this method does
     * nothing.
     */
    public void removeRecorderListener( RecorderListener l );
    
    /**
     * Prepares this <code>Recorder</code> so that the next playback can start with
     * very little delay.
     */
    public void preparePlayback();
    
    /**
     * Has the same effect as the <code>start()</code> method.
     * @throws RecorderException if the  output device could not
     *         be prepared.
     */
    public void play() throws RecorderException;
    
    /**
     * Plays from the beginning to the end in an infinite loop, until
     * <code>stop()</code> is called.
     * @throws RecorderException if the  output device could not
     *         be prepared.
     */
    public void loop() throws RecorderException;
    
    /**
     * Returns <code>true</code> if this <code>Recorder</code> is currently
     * playing in a loop, <code>false</code> otherwise.
     * @return <code>true</code> if and only if currently looping.
     */
    public boolean isLooping();
    
    /**
     * Plays from left to right marker in an infinite loop, until <code>stop()</code>
     * is called.
     * @throws RecorderException if the  output device could not
     *         be prepared.
     */
    public void loopFromLeftToRightMarker() throws RecorderException;

    /**
     * Returns <code>true</code> if this <code>Recorder</code> is currently
     * playing in a loop from the left to the right marker, <code>false</code> otherwise.
     * @return <code>true</code> if and only if currently looping from left to right marker.
     */
    public boolean isLoopingFromLeftToRightMarker();
    
    /**
     * Stops the playback/recording.
     */
    public void stop();

    /**
     * Asks this <code>Player</code> if a playback is currently running.
     * @return <code>true</code> if currently playing, <code>false</code> otherwise.
     */
    public boolean isPlaying();

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
     * @throws RecorderException if this <code>Recorder</code> is currently
     * recording and the MIDI output device could not be connected to the input device.
     * If this <code>Recorder</code> is not currently recording, no exception will be
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
     * @return <code>true</code> if this <code>Recorder</code> is currently recording,
     * <code>false</code> otherwise.
     */
    public boolean isRecording();
    
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
     * Sets the position to the start of the associated sequence.
     */
    public void jumpToStart();
    
    /**
     * Sets the position to the end of the associated sequence.
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
     * Persists this <code>Recorder</code>'s recording state
     * (input device map, recording enabled states) to the
     * underlying <code>Descriptor</code> so that it can be restored
     * using the <code>restorePlaybackState()</code> method when the
     * <code>Descriptor</code>'s state is restored from the session.
     */
    public void persistRecordingState();
    
    /**
     * Restores this <code>Recorder</code>'s recording state
     * (input device map, recording enabled states) from the underlying
     * <code>Descriptor</code> if possible.
     */
    public void restoreRecordingState();
    
    /**
     * Persists this <code>Recorder</code>'s playback state
     * (output device map, mute and solo states) to the
     * underlying <code>Descriptor</code> so that it can be restored
     * using the <code>restorePlaybackState()</code> method when the
     * <code>Descriptor</code>'s state is restored from the session.
     */
    public void persistPlaybackState();

    /**
     * Restores this <code>Recorder</code>'s playback state
     * (output device map, mute and solo states) from the underlying
     * <code>Descriptor</code> if possible.
     */
    public void restorePlaybackState();
}
