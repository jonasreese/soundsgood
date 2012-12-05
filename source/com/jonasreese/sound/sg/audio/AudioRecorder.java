/*
 * Created on 18.12.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio;

import java.util.List;

import javax.sound.sampled.AudioFormat;

import com.jonasreese.sound.sg.Recorder;
import com.jonasreese.sound.sg.RecorderException;

/**
 * <p>
 * This interface defines methods required to play or record audio sequences.
 * It shall be implemented by classes that represent the abstraction layer
 * between the low-level audio device(s) and high-level functionalities.
 * </p>
 * @author jonas.reese
 */
public interface AudioRecorder extends Recorder {

    /**
     * Gets the audio format that will be used for playback.
     * @return The playback audio format. Shall be default audio format if conversion
     * is supported, the source audio format otherwise.
     * @throws RecorderException if the audio format could not be determined.
     */
    public AudioFormat getAudioFormat() throws RecorderException;

    /**
     * Adds an audio data receiver to this <code>AudioRecorder</code>. An output
     * audio receiver receives all audio data that is sent to any output device.
     * @param audioDataReceiver The <code>AudioDataReceiver</code> that shall receive audio
     * data after being sent to an output device. If the given
     * <code>AudioDataReceiver</code> has already been added, this method does nothing.
     */
    public void addAudioOutputReceiver( AudioDataReceiver audioDataReceiver );

    /**
     * Removes the given audio data receiver from this <code>AudioRecorder</code>.
     * @param audioDataReceiver The audio output receiver that shall no longer
     * receive any audio data after being sent to an output device. If the given
     * <code>AudioDataReceiver</code> is not registered as an output receiver,
     * this method does nothing.
     */
    public void removeAudioOutputReceiver( AudioDataReceiver audioDataReceiver );
    
    /**
     * Gets the list of audio data receivers.
     * @return The audio output receiver list.
     */
    public List<AudioDataReceiver> getAudioOutputReceivers();

    /**
     * Gets the playing mode status info for <i>from left to right marker</i>.
     * @return <code>true</code> if this <code>AudioRecorder</code> is playing
     *         a sequence from the left to the right marker position.
     */
    public boolean isPlayingFromLeftToRightMarker();

    /**
     * Prepares this <code>AudioRecorder</code> so that the next playback can start with
     * very little delay.
     */
    public void preparePlayback();
    
    /**
     * Has the same effect as the <code>start()</code> method.
     * @throws RecorderException if the audio output device could not
     *         be prepared.
     */
    public void play() throws RecorderException;
    
    /**
     * Plays from the beginning to the end in an infinite loop, until
     * <code>stop()</code> is called.
     * @throws RecorderException if the audio output device could not
     *         be prepared.
     */
    public void loop() throws RecorderException;
    
    /**
     * Returns <code>true</code> if this <code>AudioRecorder</code> is currently
     * playing in a loop, <code>false</code> otherwise.
     * @return <code>true</code> if and only if currently looping.
     */
    public boolean isLooping();
    
    /**
     * Stops the playback/recording.
     */
    public void stop();

    /**
     * Asks this <code>AudioPlayer</code> if a playback is currently running.
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
     * @throws RecorderException if this <code>AudioRecorder</code> is currently
     * recording and the MIDI output device could not be connected to the input device.
     * If this <code>AudioRecorder</code> is not currently recording, no exception will be
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
     * @return <code>true</code> if this <code>AudioRecorder</code> is currently recording,
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
     * Persists this <code>AudioRecorder</code>'s recording state
     * (input device map, recording enabled states) to the
     * underlying <code>AudioDescriptor</code> so that it can be restored
     * using the <code>restorePlaybackState()</code> method when the
     * <code>AudioDescriptor</code>'s state is restored from the session.
     */
    public void persistRecordingState();
    
    /**
     * Restores this <code>AudioRecorder</code>'s recording state
     * (input device map, recording enabled states) from the underlying
     * <code>AudioDescriptor</code> if possible.
     */
    public void restoreRecordingState();
    
    /**
     * Persists this <code>AudioRecorder</code>'s playback state
     * (output device map, mute and solo states) to the
     * underlying <code>AudioDescriptor</code> so that it can be restored
     * using the <code>restorePlaybackState()</code> method when the
     * <code>AudioDescriptor</code>'s state is restored from the session.
     */
    public void persistPlaybackState();

    /**
     * Restores this <code>AudioRecorder</code>'s playback state
     * (output device map, mute and solo states) from the underlying
     * <code>AudioDescriptor</code> if possible.
     */
    public void restorePlaybackState();
}