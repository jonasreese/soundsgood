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
public class VstSpeakerProperties {

    public static final int     SPEAKER_C  = 3;
    public static final int     SPEAKER_CS = 9;
    public static final int     SPEAKER_L  = 1;
    public static final int     SPEAKER_LC = 7;
    public static final int     SPEAKER_LFE   =  4;
    public static final int     SPEAKER_LFE2   = 19;
    public static final int     SPEAKER_LS = 5;
    public static final int     SPEAKER_M  = 0;
    public static final int     SPEAKER_R  = 2;
    public static final int     SPEAKER_RC = 8;
    public static final int     SPEAKER_RS = 6;
    public static final int     SPEAKER_S  = 9;
    public static final int     SPEAKER_SL = 10;
    public static final int     SPEAKER_SR = 11;
    public static final int     SPEAKER_TFC   =  14;
    public static final int     SPEAKER_TFL    = 13;
    public static final int     SPEAKER_TFR   =  15;
    public static final int     SPEAKER_TM  = 12;
    public static final int     SPEAKER_TRC  =  17;
    public static final int     SPEAKER_TRL   =  16;
    public static final int     SPEAKER_TRR   =  18;
    public static final int     SPEAKER_U1 = -1;
    public static final int     SPEAKER_U10  =   -10;
    public static final int     SPEAKER_U11   =  -11;
    public static final int     SPEAKER_U12   =  -12;
    public static final int     SPEAKER_U13   =  -13;
    public static final int     SPEAKER_U14    = -14;
    public static final int     SPEAKER_U15 =    -15;
    public static final int     SPEAKER_U16  =   -16;
    public static final int     SPEAKER_U17   =  -17;
    public static final int     SPEAKER_U18    = -18;
    public static final int     SPEAKER_U19 =    -19;
    public static final int     SPEAKER_U2 = -2;
    public static final int     SPEAKER_U20  =   -20;
    public static final int     SPEAKER_U21  =   -21;
    public static final int     SPEAKER_U22  =   -22;
    public static final int     SPEAKER_U23  =   -23;
    public static final int     SPEAKER_U24  =   -24;
    public static final int     SPEAKER_U25  =  -25;
    public static final int     SPEAKER_U26  =  -26;
    public static final int     SPEAKER_U27  =  -27;
    public static final int     SPEAKER_U28  =  -28;
    public static final int     SPEAKER_U29  =  -29;
    public static final int     SPEAKER_U3  = -3;
    public static final int     SPEAKER_U30 =   -30;
    public static final int     SPEAKER_U31 =   -31;
    public static final int     SPEAKER_U32 =   -32;
    public static final int     SPEAKER_U4 = -4;
    public static final int     SPEAKER_U5 = -5;
    public static final int     SPEAKER_U6 = -6;
    public static final int     SPEAKER_U7 = -7;
    public static final int     SPEAKER_U8 = -8;
    public static final int     SPEAKER_U9 = -9;
    
    private float azimuth; //     rad     -PI...PI    10.f for LFE channel
    private float elevation; //   rad     -PI/2...PI/2    10.f for LFE channel
    private float radius;   // meter   0.f for LFE channel
    private String name;  // For new setups, new names should be given (L/R/C... won't do)
    private int type;  // speaker type
    
    public VstSpeakerProperties() {
        this( 0, 0, 0, null, 0 );
    }
    
    public VstSpeakerProperties( float azimuth, float elevation, float radius, String name, int type ) {
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.radius = radius;
        this.name = name;
        this.type = type;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    public float getElevation() {
        return elevation;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
}
