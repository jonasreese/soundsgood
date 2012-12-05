/*
 * Created on 21.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio;

import java.util.ArrayList;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.SgProperties;
import com.jonasreese.util.ParametrizedResourceBundle;


/**
 * @author jonas.reese
 */
public class AudioToolkit {
    private static AudioDeviceList inputDeviceList = null;
    private static AudioDeviceList outputDeviceList = null;

    
    /**
     * Determines if two given <code>AudioFormat</code> objects describe the same
     * physical audio format.
     * @param f1 The first audio format.
     * @param f2 The second audio format.
     * @return <code>true</code> if and only if <code>f1</code> and <code>f2</code> are the same objects (or <code>null</code>)
     * or describe the same physical audio format.
     */
    public static boolean isSameAudioFormat( AudioFormat f1, AudioFormat f2 ) {
        if (f1 == f2) {
            return true;
        }
        if (f1 != null && f2 != null) {
            return f1.matches( f2 );
        }
        return false;
    }
    
    /**
     * Gets a <code>Mixer</code> for the given descriptor.
     * @param descriptor The <code>AudioDeviceDescriptor</code>
     * @return A <code>Mixer</code>. If the given device descriptor does not map to a valid
     * existing audio device, a dummy device is returned.
     */
    public static Mixer getAudioDevice( AudioDeviceDescriptor descriptor ) {
        if (descriptor.getDeviceInfo() == null) {
            return new DummyDevice();
        }
        return AudioSystem.getMixer( descriptor.getDeviceInfo() );
    }
    
    /**
     * Gets the list of all audio input devices that are available.
     * @return A <code>AudioDeviceList</code> containing all available audio input devices.
     */
    public static AudioDeviceList getAudioInputDeviceList() {
        if (inputDeviceList != null) {
            return inputDeviceList;
        }
        Mixer.Info[] info = AudioSystem.getMixerInfo();
        ArrayList<AudioDeviceDescriptor> descriptors = new ArrayList<AudioDeviceDescriptor>();
        // add devices that exist in the system
        Line.Info dataLineInfo = new Line.Info( TargetDataLine.class );
        for (int i = 0; i < info.length; i++) {
            Mixer dev = AudioSystem.getMixer( info[i] );
            if (dev != null && dev.getMaxLines( dataLineInfo ) != 0) {
                descriptors.add( new AudioDeviceDescriptor( info[i], null ) );
            }
        }
        // add devices that do not exist in the system, but in the device list
        // given by the application properties
        AudioDeviceList list = SgEngine.getInstance().getProperties().getAudioInputDeviceList();
        for (int i = 0; i < list.getCount(); i++) {
            AudioDeviceDescriptor desc = list.getAudioDeviceDescriptor( i );
            if (desc.getDeviceInfo() == null) {
                descriptors.add( desc );
            }
        }
        
        AudioDeviceDescriptor[] array = new AudioDeviceDescriptor[descriptors.size()];
        descriptors.toArray( array );
        inputDeviceList = new AudioDeviceList( array );
        return inputDeviceList;
    }
    
    /**
     * Gets the list of all audio output devices that are available.
     * @return A <code>AudioDeviceList</code> containing all available audio output devices.
     */
    public static AudioDeviceList getAudioOutputDeviceList() {
        if (outputDeviceList != null) {
            return outputDeviceList;
        }
        Mixer.Info[] info = AudioSystem.getMixerInfo();
        ArrayList<AudioDeviceDescriptor> descriptors = new ArrayList<AudioDeviceDescriptor>();
        // add devices that exist in the system
        Line.Info dataLineInfo = new Line.Info( SourceDataLine.class );
        for (int i = 0; i < info.length; i++) {
            Mixer dev = AudioSystem.getMixer( info[i] );
            if (dev != null && dev.getMaxLines( dataLineInfo ) != 0) {
                descriptors.add( new AudioDeviceDescriptor( info[i], null ) );
            }
        }
        // add devices that do not exist in the system, but in the device list
        // given by the application properties
        AudioDeviceList list = SgEngine.getInstance().getProperties().getAudioOutputDeviceList();
        for (int i = 0; i < list.getCount(); i++) {
            AudioDeviceDescriptor desc = list.getAudioDeviceDescriptor( i );
            if (desc.getDeviceInfo() == null) {
                descriptors.add( desc );
            }
        }
        
        AudioDeviceDescriptor[] array = new AudioDeviceDescriptor[descriptors.size()];
        descriptors.toArray( array );
        outputDeviceList = new AudioDeviceList( array );
        return outputDeviceList;
    }
    
    /**
     * Returns the default <code>AudioFormat</code> that is currently set in the
     * <code>SoundsGood</code> application properties, as an <code>AudioFormat</code> object.
     * The number of channels will be 2 if stereo is enabled or 1 for mono.
     * @return A newly created <code>AudioFormat</code> object representing the format
     * that is currently configured in the application settings.
     */
    public static AudioFormat getDefaultAudioFormat() {
        return getDefaultAudioFormat( -1 );
    }
    
    /**
     * Returns the default <code>AudioFormat</code> that is currently set in the
     * <code>SoundsGood</code> application properties, as a <code>AudioFormat</code> object.
     * The number of channels can be provided to this method.
     * @param channels The number of channels to set on the resulting <code>AudioFormat</code>.
     * If &lt;0, indicates that the default number of channels shall be set.
     * @return A newly created <code>AudioFormat</code> object representing the format
     * that is currently configured in the application settings, whereas the number of channels
     * is equal to the channel count provided to this method.
     */
    public static AudioFormat getDefaultAudioFormat( int channels ) {
        SgProperties p = SgEngine.getInstance().getProperties();
        if (channels < 0) {
            channels = p.isDefaultAudioFormatMono() ? 1 : 2;
        }
        return new AudioFormat(
                p.getDefaultAudioFormatSampleRate(),
                p.getDefaultAudioFormatSampleSize(),
                channels,
                p.isDefaultAudioFormatSigned(),
                p.isDefaultAudioFormatBigEndian() );
    }
    
    /**
     * Calculates the audio buffer length in nanoseconds from the given
     * audio format values.
     * @param sampleRate The sample rate in Hz.
     * @param sampleSize The sample size in bytes.
     * @param bufferSize The buffer size in bytes.
     * @return The buffer length in nanoseconds.
     */
    public static long getAudioBufferLengthInNanos( float sampleRate, int sampleSize, int bufferSize ) {
        double nanos = 1000000000.0 * (((double) bufferSize) / ((double) sampleRate * (double) sampleSize));
        //System.out.println( "Buffer length is " + nanos + " nanoseconds" );
        return (long) nanos;
    }
    
    /**
     * Gets the buffer size (in bytes) for the an audio format with the values
     * provided and the given buffer length in milliseconds.
     * @param sampleRate The sample rate in Hz.
     * @param sampleSize The sampe size in bytes.
     * @param audioBufferLength The audio buffer length in milliseconds.
     * @return An integer greater than 0 describing the required buffer size
     * in bytes.
     */
    public static int getBufferSize( float sampleRate, int sampleSize, int audioBufferLength ) {
        int result = ((int) (sampleRate * sampleSize * audioBufferLength)) / 1000;
        int minBufferSize = 16 * sampleSize;
        if (result < minBufferSize) {
            result = minBufferSize;
        }
        int modulus = result % sampleSize;
        if (modulus != 0) {
            result += (sampleSize - modulus);
        }
        return result;
    }
    
    /**
     * Gets the buffer size (in bytes) for given target audio format when converted from
     * <code>sourceFormat</code>, given the buffer size <code>sourceSize</code> in bytes. 
     * @param sourceSize The source buffer size.
     * @param sourceFormat The source format. Must not be <code>null</code>.
     * @param targetFormat The target format. Must not be <code>null</code>.
     */
    public static int getBufferSize(
            int sourceSize, AudioFormat sourceFormat, AudioFormat targetFormat ) {
        long nanos = getAudioBufferLengthInNanos( sourceFormat.getSampleRate(), sourceFormat.getSampleSizeInBits(), sourceSize );
        
        int sampleSize = targetFormat.getSampleSizeInBits() / 8;
        int result = ((int) (targetFormat.getSampleRate() * sampleSize * nanos)) / 1000000;
        
        int modulus = result % sampleSize;
        if (modulus != 0) {
            result += (sampleSize - modulus);
        }
        return result;

    }
    
    /**
     * Gets the buffer size (in bytes) for the given audio format and the default
     * buffer length configured in milliseconds.
     * @param audioFormat The audio format.
     * @return An integer greater than 0 describing the required buffer size
     * in bytes.
     */
    public static int getBufferSize( AudioFormat audioFormat ) {
        return getBufferSize(
                audioFormat.getSampleRate(),
                audioFormat.getFrameSize(),
                SgEngine.getInstance().getProperties().getAudioBufferLength() );
    }
    
    /**
     * Gets the buffer size (in bytes) for the default audio format and the
     * buffer length configured in milliseconds.
     * @return An integer greater than 0 describing the required buffer size
     * in bytes.
     */
    public static int getBufferSizeForDefaultAudioFormat() {
        SgProperties p = SgEngine.getInstance().getProperties();
        return getBufferSize(
                p.getDefaultAudioFormatSampleRate(),
                p.getDefaultAudioFormatSampleSize(),
                p.getAudioBufferLength() );
    }
    
    /**
     * Gets a human-readable, localized name for an audio output with the given
     * <code>AudioFormat</code>.
     * @param format The audio format.
     * @return The human-readable name.
     */
    public static String getAudioOutputName( AudioFormat format ) {
        ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        String s;
        if (format.getChannels() == 1) {
            s = rb.getString( "audio.output.mono" );
        } else if (format.getChannels() == 2) {
            s = rb.getString( "audio.output.stereo" );
        } else {
            s = rb.getString( "audio.output.multichannel", format.getChannels() );
        }
        return s;
    }
    
    /**
     * Gets a human-readable, localized name for an audio input with the given
     * <code>AudioFormat</code>.
     * @param format The audio format.
     * @return The human-readable name.
     */
    public static String getAudioInputName( AudioFormat format ) {
        ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        String s;
        if (format.getChannels() == 1) {
            s = rb.getString( "audio.input.mono" );
        } else if (format.getChannels() == 2) {
            s = rb.getString( "audio.input.stereo" );
        } else {
            s = rb.getString( "audio.input.multichannel", format.getChannels() );
        }
        return s;
    }
    
    /**
     * Gets a human-readable, localized description for an audio output with the given
     * <code>AudioFormat</code>.
     * @param format The audio format.
     * @return The human-readable description.
     */
    public static String getAudioOutputDescription( AudioFormat format ) {
        ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        return rb.getString(
                "audio.inOut.sampleRateAndSize", format.getSampleRate(), format.getSampleSizeInBits() );
    }

    /**
     * Gets a human-readable, localized description for an audio input with the given
     * <code>AudioFormat</code>.
     * @param format The audio format.
     * @return The human-readable description.
     */
    public static String getAudioInputDescription( AudioFormat format ) {
        ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        return rb.getString(
                "audio.inOut.sampleRateAndSize", format.getSampleRate(), format.getSampleSizeInBits() );
    }
    
    /**
     * Saves the given <code>AudioFormat</code> to a given <code>Map</code>.
     * It can be restored from the map by calling {@link #restoreAudioFormat(Map<String, String>)}
     * @param p The properties map. Must not be <code>null</code>.
     * @param format The format to be saved.
     */
    public static void saveAudioFormat( Map<String, String> p, AudioFormat format ) {
        if (format == null) {
            return;
        }
        p.put( "channels", Integer.toString( format.getChannels() ) );
        p.put( "encoding", format.getEncoding().toString() );
        p.put( "frameRate", Float.toString( format.getFrameRate() ) );
        p.put( "frameSize", Integer.toString( format.getFrameSize() ) );
        p.put( "sampleRate", Float.toString( format.getSampleRate() ) );
        p.put( "sampleSizeInBits", Integer.toString( format.getSampleSizeInBits() ) );
        p.put( "bigEndian", Boolean.toString( format.isBigEndian() ) );
    }
    
    public static javax.sound.sampled.AudioFormat.Encoding getEncoding( String enc ) {
        if (javax.sound.sampled.AudioFormat.Encoding.ALAW.toString().equals( enc )) {
            return javax.sound.sampled.AudioFormat.Encoding.ALAW;
        }
        if (javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED.toString().equals( enc )) {
            return javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
        }
        if (javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED.toString().equals( enc )) {
            return javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED;
        }
        if (javax.sound.sampled.AudioFormat.Encoding.ULAW.toString().equals( enc )) {
            return javax.sound.sampled.AudioFormat.Encoding.ULAW;
        }
        return null;
    }
    
    /**
     * Restores an <code>AudioFormat</code> from a given <code>Map</code>.
     * @param p The properties. Must not be <code>null</code>.
     * @return An <code>AudioFormat</code>, or <code>null</code> if the given
     * properties map did not contain the appropriate properties.
     */
    public static AudioFormat restoreAudioFormat( Map<String, String> p ) {
        if (p == null) {
            return null;
        }
        int channels = 0;
        if (p.containsKey( "channels" )) {
            channels = Integer.parseInt( p.get( "channels" ) );
        } else {
            return null;
        }
        javax.sound.sampled.AudioFormat.Encoding encoding = null;
        if (p.containsKey( "encoding" )) {
            encoding = getEncoding( p.get( "encoding" ) );
        }
        if (encoding == null) {
            return null;
        }
        float frameRate = 0;
        if (p.containsKey( "frameRate" )) {
            frameRate = Float.parseFloat( p.get( "frameRate" ) );
        } else {
            return null;
        }
        int frameSize = 0;
        if (p.containsKey( "frameSize" )) {
            frameSize = Integer.parseInt( p.get( "frameSize" ) );
        } else {
            return null;
        }
        float sampleRate = 0;
        if (p.containsKey( "sampleRate" )) {
            sampleRate = Float.parseFloat( p.get( "sampleRate" ) );
        } else {
            return null;
        }
        int sampleSizeInBits = 0;
        if (p.containsKey( "sampleSizeInBits" )) {
            sampleSizeInBits = Integer.parseInt( p.get( "sampleSizeInBits" ) );
        } else {
            return null;
        }
        boolean bigEndian = false;
        if (p.containsKey( "bigEndian" )) {
            bigEndian = Boolean.parseBoolean( p.get( "bigEndian" ) );
        } else {
            return null;
        }
        
        return new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
    }
}
