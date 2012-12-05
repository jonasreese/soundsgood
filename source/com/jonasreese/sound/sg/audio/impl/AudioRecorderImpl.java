/*
 * Created on 19.12.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.jonasreese.sound.sg.RecorderException;
import com.jonasreese.sound.sg.RecorderListener;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.audio.AudioDataPump;
import com.jonasreese.sound.sg.audio.AudioDataReceiver;
import com.jonasreese.sound.sg.audio.AudioDescriptor;
import com.jonasreese.sound.sg.audio.AudioDeviceDescriptor;
import com.jonasreese.sound.sg.audio.AudioFormatConverter;
import com.jonasreese.sound.sg.audio.AudioRecorder;
import com.jonasreese.sound.sg.audio.AudioToolkit;

/**
 * @author jr
 */
public class AudioRecorderImpl implements AudioRecorder {

    public static final int BUFFER_SIZE_IN_FRAMES = 512;
    
    private AudioDescriptor descriptor;
    private List<Mixer> mixers;
    private List<SourceDataLine> sourceDataLines;
    private AudioDataPumpThread[] pumpThreads;
    private boolean nextPlaybackMuted;
    private List<AudioDataReceiver> audioOutputReceivers;
    private AudioFormat sourceFormat;
    
    public AudioRecorderImpl( AudioDescriptor descriptor ) {
        this.descriptor = descriptor;
        nextPlaybackMuted = false;
        audioOutputReceivers = new ArrayList<AudioDataReceiver>();
        sourceDataLines = new ArrayList<SourceDataLine>();
        mixers = new ArrayList<Mixer>();
    }
    
    public AudioFormat getAudioFormat() throws RecorderException {
        AudioFormat defaultFormat = AudioToolkit.getDefaultAudioFormat();
        if (AudioSystem.isConversionSupported( defaultFormat, getSourceFormat() )) {
            return defaultFormat;
        }
        return sourceFormat;
    }
    
    private AudioFormat getSourceFormat() throws RecorderException {
        if (sourceFormat == null) {
            try {
                sourceFormat = AudioSystem.getAudioFileFormat( descriptor.getFile() ).getFormat();
            } catch (UnsupportedAudioFileException e) {
                throw new RecorderException( e );
            } catch (IOException e) {
                sourceFormat = AudioToolkit.getDefaultAudioFormat();
            }
        }
        return sourceFormat;
    }
    
    public void addAudioOutputReceiver( AudioDataReceiver audioDataReceiver ) {
        synchronized (audioOutputReceivers) {
            if (!audioOutputReceivers.contains( audioOutputReceivers )) {
                audioOutputReceivers.add( audioDataReceiver );
            }
        }
    }

    public void removeAudioOutputReceiver( AudioDataReceiver audioDataReceiver ) {
        synchronized (audioOutputReceivers) {
            audioOutputReceivers.remove( audioDataReceiver );
        }
    }
    
    public List<AudioDataReceiver> getAudioOutputReceivers() {
        return Collections.unmodifiableList( audioOutputReceivers );
    }
    
    public void clickAndRecord() throws RecorderException {
        // TODO Auto-generated method stub
        
    }

    public long getMicrosecondLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getMicrosecondPosition() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isInFastBackwardMode() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isInFastForwardMode() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isLoopbackEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isLooping() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isLoopingFromLeftToRightMarker() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPlaying() {
        if (sourceDataLines != null) {
            for (SourceDataLine l : sourceDataLines) {
                if (l.isActive()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isPlayingFromLeftMarker() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPlayingFromLeftToRightMarker() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPlayingToRightMarker() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isRecordLoopbackEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isRecording() {
        // TODO Auto-generated method stub
        return false;
    }

    public void jumpToEnd() {
        // TODO Auto-generated method stub
        
    }

    public void loop() throws RecorderException {
        // TODO Auto-generated method stub
        
    }

    public void loopFromLeftToRightMarker() throws RecorderException {
        // TODO Auto-generated method stub
        
    }

    public void persistPlaybackState() {
        // TODO Auto-generated method stub
        
    }

    public void persistRecordingState() {
        // TODO Auto-generated method stub
        
    }

    private AudioDataPumpThread[] createAudioDataPumps()
    throws RecorderException, UnsupportedAudioFileException, IOException {
                
        AudioFormat audioFormat = getAudioFormat();
        AudioInputStream sourceStream = AudioSystem.getAudioInputStream( descriptor.getFile() );
        
        Map<AudioFormatWrapper, List<AudioDataReceiver>> formats = new HashMap<AudioFormatWrapper, List<AudioDataReceiver>>();
        AudioFormat sourceFormat = getSourceFormat();
        for (AudioDataReceiver rec : audioOutputReceivers) {
            AudioFormat format = rec.getAudioFormat();
            if (format == null && rec.canReceive( sourceFormat )) {
                format = sourceFormat;
                rec.setAudioFormat( format );
            }
            AudioFormatConverter converter = null;
            if (format != null) {
                boolean canConvert = (format == sourceFormat) || // just to make sure
                    AudioSystem.isConversionSupported( format, sourceFormat );
                if (!canConvert) {
                    // if cannot convert, try to create an AudioFormatConverter
                    converter = new AudioFormatConverter( sourceFormat, format, sourceFormat.getFrameSize() * BUFFER_SIZE_IN_FRAMES );
                    if (!converter.isSupportedConversion()) {
                        // if still cannot convert to desired format, check if receiver can do source format
                        if (rec.canReceive( sourceFormat )) {
                            format = sourceFormat;
                        } else {
                            throw new RecorderException(
                                    SgEngine.getInstance().getResourceBundle().getString(
                                            "error.unsupportedAudioFormat", sourceFormat ) );
                        }
                    }
                }
                boolean alreadyIn = false;
                for (AudioFormatWrapper f : formats.keySet()) {
                    List<AudioDataReceiver> l = formats.get( f );
                    if (l != null) {
                        if (f.getAudioFormat().matches( format )) {
                            l.add( rec );
                            alreadyIn = true;
                            break;
                        }
                    }
                }
                if (!alreadyIn) {
                    List<AudioDataReceiver> l = new ArrayList<AudioDataReceiver>();
                    l.add( rec );
                    formats.put( new AudioFormatWrapper( format, converter ), l );
                }
            }
        }
        
        boolean nextPlaybackMuted = this.nextPlaybackMuted;
        AudioDataPumpThread[] pumpThreads = new AudioDataPumpThread[formats.size() + (nextPlaybackMuted ? 0 : 1)];
        
        int i = 0;
        if (!nextPlaybackMuted) {
            pumpThreads[i++] = new AudioDataPumpThread( audioFormat, sourceStream, null ) {
                public void run() {
                    byte[] buffer = new byte[in.getFormat().getFrameSize() * BUFFER_SIZE_IN_FRAMES];
                    try {
                        int numBytesRead = 0;
                        // Try to read numBytes bytes from the file.
                        while (running && (numBytesRead = in.read( buffer )) != -1) {
                            synchronized (sourceDataLines) {
                                for (SourceDataLine line : sourceDataLines) {
                                    line.write( buffer, 0, numBytesRead );
                                }
                            }
                        }
                        in.close();
                    } catch (Exception ex) {
                    }
                    synchronized (this) {
                        notifyAll();
                        running = false;
                    }
                }
                public boolean isDefault() {
                    return true;
                }
            };
        }

        for (AudioFormatWrapper formatWrapper : formats.keySet()) {
            final List<AudioDataReceiver> l = formats.get( formatWrapper );
            // check if one AudioDataReceiver requires realtime synchronization
            boolean b = false;
            for (AudioDataReceiver r : l) {
                if (r.isRealtimeOnly() && !r.isRealtimeSynchonous()) {
                    b = true;
                    break;
                }
            }
            final boolean needsSync = b;
            
            pumpThreads[i++] = new AudioDataPumpThread( formatWrapper.getAudioFormat(), sourceStream, formatWrapper.getConverter() ) {
                public void run() {
                    byte[] buffer = new byte[in.getFormat().getFrameSize() * BUFFER_SIZE_IN_FRAMES];
                    try {
                        int numBytesRead = 0;
                        long nextTime = System.nanoTime();
                        // Try to read numBytes bytes from the file.
                        while (running && (numBytesRead = in.read( buffer )) != -1) {
                            if (needsSync) {
                                waitUntil( nextTime - 1000000 ); // subtract a millisecond to avoid buffer underflow
                            }
                            if (numBytesRead < buffer.length) {
                                System.out.println( "read less bytes than buffer size" );
                            }
                            AudioFormat sourceFormat;
                            if (converter != null) {
                                // with converter
                                byte[] convertedBuffer = converter.convert( buffer, 0, numBytesRead );
                                for (int i = 0; i < l.size(); i++) {
                                    AudioDataReceiver r = l.get( i );
                                    r.receive( convertedBuffer, converter.getOffset(), converter.getLength(), this );
                                }
                                sourceFormat = converter.getSourceFormat();
                            } else {
                                // without converter
                                for (int i = 0; i < l.size(); i++) {
                                    AudioDataReceiver r = l.get( i );
                                    r.receive( buffer, 0, numBytesRead, this );
                                }
                                sourceFormat = format;
                            }
                            if (needsSync) {
                                nextTime += AudioToolkit.getAudioBufferLengthInNanos(
                                        sourceFormat.getSampleRate(), sourceFormat.getFrameSize(), numBytesRead );
                            }
                        }
                        in.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    synchronized (this) {
                        notifyAll();
                        running = false;
                    }
                }
                // leave this private so it can be inlined
                private void waitUntil( long nanoTime ) throws InterruptedException {
                    long timeDiff = nanoTime - System.nanoTime();
                    if (timeDiff <= 0) {
                        return;
                    }
                    int nanos = (int) (timeDiff % 1000000);
                    sleep( timeDiff / 1000000, nanos );
                }
                public boolean isDefault() {
                    return false;
                }
            };
        }
        return pumpThreads;
    }
    
    public void play() throws RecorderException {
        try {
            stop();
            
            try {
                pumpThreads = createAudioDataPumps();
            } catch (IllegalArgumentException iaex) {
                throw new RecorderException( iaex );
            }
            if (pumpThreads == null || pumpThreads.length == 0) {
                return; // nothing to play back to
            }
            com.jonasreese.sound.sg.audio.AudioDeviceList list = AudioToolkit.getAudioOutputDeviceList();

            synchronized (sourceDataLines) {
                sourceDataLines.clear();
            }
            synchronized (mixers) {
                mixers.clear();
            }

            nextPlaybackMuted = false;
            if (pumpThreads[0].isDefault()) {
                for (int i = 0; i < list.getCount(); i++) {
                    AudioDeviceDescriptor desc = list.getAudioDeviceDescriptor( i );
                    System.out.println( "Activated audio device : " + desc );
                    Mixer mixer = AudioToolkit.getAudioDevice( desc );
                    Line l = mixer.getLine( new DataLine.Info( SourceDataLine.class, pumpThreads[0].getAudioFormat() ) );
                    synchronized (mixers) {
                        mixers.add( mixer );
                    }
                    if (l != null && l instanceof SourceDataLine) {
                        synchronized (sourceDataLines) {
                            sourceDataLines.add( (SourceDataLine) l );
                        }
                    }
                }
                synchronized (mixers) {
                    for (Mixer mixer : mixers) {
                        System.out.println( "opening mixer " + mixer );
                        mixer.open();
                    }
                }
                synchronized (sourceDataLines) {
                    for (SourceDataLine line : sourceDataLines) {
                        System.out.println( "opening/starting line " + line );
                        line.open();
                        line.start();
                    }
                }
            }
            System.out.println( "AudioRecorder.play(), format: " + pumpThreads[0].getAudioFormat() );
            for (int i = 0; i < pumpThreads.length; i++) {
                pumpThreads[i].start();
            }
        } catch (FileNotFoundException e) {
            throw new RecorderException( e );
        } catch (LineUnavailableException e) {
            throw new RecorderException( e );
        } catch (IOException e) {
            throw new RecorderException( e );
        } catch (UnsupportedAudioFileException e) {
            throw new RecorderException( e );
        }
    }

    public void preparePlayback() {
        // TODO Auto-generated method stub
        
    }

    public void record() throws RecorderException {
        // TODO Auto-generated method stub
        
    }

    public void restorePlaybackState() {
        // TODO Auto-generated method stub
        
    }

    public void restoreRecordingState() {
        // TODO Auto-generated method stub
        
    }

    public void setLoopbackEnabled(boolean permanentLoopbackEnabled)
            throws RecorderException {
        // TODO Auto-generated method stub
        
    }

    public void setMicrosecondPosition(long pos) {
        // TODO Auto-generated method stub
        
    }

    public void setNextPlaybackMuted( boolean muteNextPlayback ) {
        this.nextPlaybackMuted = muteNextPlayback;
    }

    public void setRecordLoopbackEnabled(boolean recordLoopbackEnabled)
            throws RecorderException {
        // TODO Auto-generated method stub
        
    }

    public void startFastBackward() {
        // TODO Auto-generated method stub
        
    }

    public void startFastForward() {
        // TODO Auto-generated method stub
        
    }

    public void stop() {
        if (pumpThreads == null) {
            return;
        }
        for (AudioDataPumpThread t : pumpThreads) {
            synchronized (t) {
                if (t.isAlive() && t.running) {
                    try {
                        t.running = false;
                        try {
                            t.in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        t.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        synchronized (sourceDataLines) {
            for (SourceDataLine line : sourceDataLines) {
                line.stop();
                line.close();
            }
        }
        synchronized (mixers) {
            for (Mixer mixer : mixers) {
                mixer.close();
            }
        }
    }

    public void stopFastBackward() {
        // TODO Auto-generated method stub
        
    }

    public void stopFastForward() {
        // TODO Auto-generated method stub
        
    }

    public void addRecorderListener(RecorderListener l) {
        // TODO Auto-generated method stub
        
    }

    public void jumpToStart() {
        // TODO Auto-generated method stub
        
    }

    public void removeRecorderListener(RecorderListener l) {
        // TODO Auto-generated method stub
        
    }
    
    abstract class AudioDataPumpThread extends Thread implements AudioDataPump {
        AudioInputStream in;
        AudioFormat format;
        boolean started;
        boolean running;
        AudioFormatConverter converter;
        
        AudioDataPumpThread( AudioFormat targetFormat, AudioInputStream sourceStream, AudioFormatConverter converter ) {
            super( "AudioRecorder Data Pump Thread" );
            setPriority( MAX_PRIORITY );
            // we need to get an AudioInputStream for converter source format, the rest is done by the converter
            if (converter != null) {
                this.in = AudioSystem.getAudioInputStream( converter.getSourceFormat(), sourceStream );
                format = converter.getTargetFormat();
            } else {
                this.in = AudioSystem.getAudioInputStream( targetFormat, sourceStream );
                format = in.getFormat(); // should be same as targetFormat
            }
            this.converter = converter;
            started = false;
            running = false;
        }
        
        public void start() {
            synchronized (this) {
                running = true;
                started = true;
                super.start();
            }
        }
        
        public abstract boolean isDefault();
        
        public AudioFormat getAudioFormat() {
            return format;
        }
    }
    
    /**
     * Helper class that wraps an <code>AudioFormat</code> to be able to use it in a
     * <code>HashMap</code>.
     */
    static class AudioFormatWrapper {
        AudioFormat audioFormat;
        AudioFormatConverter converter;
        AudioFormatWrapper( AudioFormat audioFormat, AudioFormatConverter converter ) {
            this.audioFormat = audioFormat;
            this.converter = converter;
        }
        public AudioFormat getAudioFormat() {
            return audioFormat;
        }
        public AudioFormatConverter getConverter() {
            return converter;
        }
        public int hashCode() {
            return audioFormat.toString().hashCode();
        }
        public boolean equals( Object another ) {
            if (another instanceof AudioFormat) {
                return audioFormat.matches( (AudioFormat) another );
            }
            return false;
        }
    }
}
