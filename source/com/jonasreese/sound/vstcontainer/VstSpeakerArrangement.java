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
public class VstSpeakerArrangement {
    public static final int  SPEAKER_ARRANGEMENT_USER_DEFINED    = -2;
    public static final int  SPEAKER_ARRANGEMENT_EMPTY           = -1;
    public static final int  SPEAKER_ARRANGEMENT_MONO            =  0;
    public static final int  SPEAKER_ARRANGEMENT_STEREO_M        =  1;
    public static final int  SPEAKER_ARRANGEMENT_STEREO_SURROUND =  2;
    public static final int  SPEAKER_ARRANGEMENT_STEREO_CENTER   =  3;
    public static final int  SPEAKER_ARRANGEMENT_STEREO_SIDE     =  4;
    public static final int  SPEAKER_ARRANGEMENT_STEREO_CL_FE    =  5;
    public static final int  SPEAKER_ARRANGEMENT_30CINE          =  6;
    public static final int  SPEAKER_ARRANGEMENT_30MUSIC         =  7;
    public static final int  SPEAKER_ARRANGEMENT_31CINE          =  8;
    public static final int  SPEAKER_ARRANGEMENT_31MUSIC         =  9;
    public static final int  SPEAKER_ARRANGEMENT_40CINE          = 10;
    public static final int  SPEAKER_ARRANGEMENT_40MUSIC         = 11;
    public static final int  SPEAKER_ARRANGEMENT_41CINE          = 12;
    public static final int  SPEAKER_ARRANGEMENT_41MUSIC         = 13;
    public static final int  SPEAKER_ARRANGEMENT_50              = 14;
    public static final int  SPEAKER_ARRANGEMENT_51              = 15;
    public static final int  SPEAKER_ARRANGEMENT_60CINE          = 16;
    public static final int  SPEAKER_ARRANGEMENT_60MUSIC         = 17;
    public static final int  SPEAKER_ARRANGEMENT_61CINE          = 18;
    public static final int  SPEAKER_ARRANGEMENT_61MUSIC         = 19;
    public static final int  SPEAKER_ARRANGEMENT_70CINE          = 20;
    public static final int  SPEAKER_ARRANGEMENT_70MUSIC         = 21;
    public static final int  SPEAKER_ARRANGEMENT_71CINE          = 22;
    public static final int  SPEAKER_ARRANGEMENT_71MUSIC         = 23;
    public static final int  SPEAKER_ARRANGEMENT_80CINE          = 24;
    public static final int  SPEAKER_ARRANGEMENT_80MUSIC         = 25;
    public static final int  SPEAKER_ARRANGEMENT_81CINE          = 26;
    public static final int  SPEAKER_ARRANGEMENT_81MUSIC         = 27;
    public static final int  SPEAKER_ARRANGEMENT_102             = 28;
    public static final int  NUM_SPEAKER_ARRANGMENT              = 29;
    
    
    private int type;
    private VstSpeakerProperties[] speakers;
    
    public VstSpeakerArrangement() {
        this( 0, null );
    }

    public VstSpeakerArrangement( int type, VstSpeakerProperties[] speakers ) {
        this.type = type;
        this.speakers = speakers;
    }

    public int getNumChannels() {
        return speakers == null ? 0 : speakers.length;
    }

    public VstSpeakerProperties[] getSpeakers() {
        return speakers;
    }

    public void setSpeakers(VstSpeakerProperties[] speakers) {
        this.speakers = speakers;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    
}
