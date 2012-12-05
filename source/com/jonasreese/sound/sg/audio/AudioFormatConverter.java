/*
 * Created on 21.12.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Control.Type;

/**
 * This class can convert PCM audio data from one format into another.
 * @author Jonas Reese
 */
public class AudioFormatConverter {
    
    private AudioFormat sourceFormat;
    private AudioFormat targetFormat;
    private double offset;
    private double length;
    private byte[] buffer;
    private Conversion conversion;
    
    public AudioFormatConverter( AudioFormat sourceFormat, AudioFormat targetFormat, int maxBufferSize ) {
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
        if (sourceFormat.matches( targetFormat )) {
            buffer = null;
        } else {
            buffer = new byte[getTargetBufferSize( maxBufferSize )];
        }
        conversion = createConversion();
    }
    
    private int getTargetBufferSize( int originalBufferLength ) {
        int numFrames = originalBufferLength / sourceFormat.getFrameSize();
        int numFramesPerChannel = numFrames / sourceFormat.getChannels();
        
        return numFramesPerChannel * targetFormat.getFrameSize() * targetFormat.getChannels();
    }
    
    private Conversion createConversion() {
        // mono to stereo, same sample size and rate
        if (sourceFormat.getChannels() == 1
                && targetFormat.getChannels() == 2
                && sourceFormat.getFrameRate() == targetFormat.getFrameRate()
                && sourceFormat.getFrameSize() == targetFormat.getFrameSize()) {
            return new Conversion() {
                int fs = sourceFormat.getFrameSize();
                @Override
                void convert( byte[] data, int offset, int length ) {
                    System.out.println( "mono -> stereo" );
                    for (int i = offset; i < offset + length; i += fs) {
                        // duplicate output data
                        for (int j = 0; j < 2; j++) {
                            for (int k = 0; k < fs; k++) {
                                buffer[2 * i + k + (j * fs)] = data[i + k];
                            }
                        }
                    }
                    AudioFormatConverter.this.offset = 2 * offset;
                    AudioFormatConverter.this.length = 2 * length;
                }
            };
        }

        // 8-bit mono to 16-bit mono, same sample size and rate
        if (sourceFormat.getChannels() == 1
                && targetFormat.getChannels() == 1
                && sourceFormat.getSampleSizeInBits() == 8
                && targetFormat.getSampleSizeInBits() == 16
                && sourceFormat.getFrameRate() == targetFormat.getFrameRate()
                && sourceFormat.getFrameSize() == 1
                && targetFormat.getFrameSize() == 2) {
            final int j = targetFormat.isBigEndian() ? 0 : 1;
            final int k = targetFormat.isBigEndian() ? 1 : 0;
            return new Conversion() {
                @Override
                void convert( byte[] data, int offset, int length ) {
                    System.out.println( "8-bit mono -> 16-bit mono" );
                    for (int i = offset; i < offset + length; i++) {
                        buffer[2 * i + j] = data[i];
                        buffer[2 * i + k] = 0;
                    }
                    AudioFormatConverter.this.offset = 2 * offset;
                    AudioFormatConverter.this.length = 2 * length;
                }
            };
        }
        
        // try system conversion
        if (AudioSystem.isConversionSupported( targetFormat, sourceFormat )) {
            final TargetDataLineImpl l = new TargetDataLineImpl( sourceFormat );
            final AudioInputStream in = AudioSystem.getAudioInputStream( targetFormat, new AudioInputStream( l ) );
            return new Conversion() {
                @Override
                void convert( byte[] data, int offset, int length ) {
                    l.prepare( data, offset );
                    length = AudioToolkit.getBufferSize( length, sourceFormat, targetFormat );
                    offset = 0;
                    try {
                        in.read( buffer, offset, length );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
        }

        return null;
    }
    
    /**
     * Asks this <code>AudioFormatConverter</code> if it's underlying conversion
     * is supported. If a conversion is not supported, the convert method still works but
     * will not convert the audio data.
     * @return
     */
    public boolean isSupportedConversion() {
        return conversion != null;
    }
    
    public AudioFormat getSourceFormat() {
        return sourceFormat;
    }
    
    public AudioFormat getTargetFormat() {
        return targetFormat;
    }
    
    public byte[] convert( byte[] data, int offset, int length ) {
        if (buffer == null || conversion == null) {
            this.offset = offset;
            this.length = length;
            return data;
        } else if (conversion != null) {
            conversion.convert( data, offset, length );
        }
        return buffer;
    }

    public int getOffset() {
        return (int) offset;
    }

    public int getLength() {
        return (int) length;
    }
    
    abstract class Conversion {
        abstract void convert( byte[] data, int offset, int length );
    }
    
    
    class TargetDataLineImpl implements TargetDataLine {
        AudioFormat format;
        Info info;
        boolean open;
        Control[] controls;
        byte[] data;
        int offset;
        public TargetDataLineImpl( AudioFormat format ) {
            this.format = format;
            info = new Info( this.getClass(), format );
            open = false;
            controls = new Control[0];
            data = null;
        }
        
        public void open( AudioFormat f )
                throws LineUnavailableException {
        }
        public void open(AudioFormat f, int arg1)
                throws LineUnavailableException {
            open = true;
        }
        public void prepare( byte[] data, int offset ) {
            this.data = data;
            this.offset = offset;
        }
        public int read( byte[] data, int offset, int length ) {
            System.arraycopy( this.data, this.offset, data, offset, length );
            return length;
        }
        public int available() {
            return 0;
        }
        public void drain() {
        }
        public void flush() {
        }
        public int getBufferSize() {
            return 0;
        }
        public AudioFormat getFormat() {
            return null;
        }
        public int getFramePosition() {
            return 0;
        }
        public float getLevel() {
            return 0;
        }
        public long getLongFramePosition() {
            return 0;
        }
        public long getMicrosecondPosition() {
            return 0;
        }
        public boolean isActive() {
            return open;
        }
        public boolean isRunning() {
            return open;
        }
        public void start() {
        }
        public void stop() {
        }
        public void addLineListener(LineListener arg0) {
        }
        public void close() {
            open = false;
        }
        public Control getControl(Type type) {
            return null;
        }
        public Control[] getControls() {
            return controls;
        }
        public javax.sound.sampled.Line.Info getLineInfo() {
            return info;
        }
        public boolean isControlSupported(Type type) {
            return false;
        }
        public boolean isOpen() {
            return open;
        }
        public void open() throws LineUnavailableException {
            open = true;
        }
        public void removeLineListener(LineListener l) {
        }
    }
}
