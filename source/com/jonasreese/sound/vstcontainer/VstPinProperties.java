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
public class VstPinProperties {
    private static final int ACTIVE_MASK = 1;
    private static final int STEREO_MASK = 2;
    private static final int USE_SPEAKER_MASK = 4;
    
    
    private int arrangementType;
    private int flags;
    private String label;
    private String shortLabel;
    
    public VstPinProperties() {
        arrangementType = 0;
        flags = 0;
        label = null;
        shortLabel = null;
    }
    
    public VstPinProperties( String label, int flags, int arrangementType, String shortLabel ) {
        this.arrangementType = arrangementType;
        this.flags = flags;
        this.label = label;
        this.shortLabel = shortLabel;
    }

    public int getArrangementType() {
        return arrangementType;
    }

    public int getFlags() {
        return flags;
    }

    public String getLabel() {
        return label;
    }

    public String getShortLabel() {
        return shortLabel;
    }
    
    public boolean isActive() {
        return (flags & ACTIVE_MASK) != 0;
    }
    
    public boolean isStereo() {
        return (flags & STEREO_MASK) != 0;
    }
    
    public boolean isUsingSpeaker() {
        return (flags & USE_SPEAKER_MASK) != 0;
    }
}
