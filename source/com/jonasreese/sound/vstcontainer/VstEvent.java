/*
 * Created on 20.10.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.vstcontainer;

/**
 * @author jonas.reese
 */
public abstract class VstEvent {

    public static final int VST_MIDI_TYPE = 1;
    public static final int VST_AUDIO_TYPE = 2;
    public static final int VST_VIDEO_TYPE = 3;
    public static final int VST_PARAMETER_TYPE = 4;
    public static final int VST_TRIGGER_TYPE = 5;
    
    
    private int deltaFrames;
    private int flags;
    
    public VstEvent() {
        this( 0, 0 );
    }
    
    public VstEvent( int deltaFrames, int flags ) {
        this.deltaFrames = deltaFrames;
        this.flags = flags;
    }

    public int getDeltaFrames() {
        return deltaFrames;
    }

    public void setDeltaFrames(int deltaFrames) {
        this.deltaFrames = deltaFrames;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * Gets the <code>VstEvent</code> type.
     * @return The <code>VstEvent</code> type value.
     */
    public abstract int getType();
}
