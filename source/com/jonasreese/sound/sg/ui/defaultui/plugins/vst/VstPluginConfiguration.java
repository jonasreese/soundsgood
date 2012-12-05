/*
 * Created on 02.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.vst;

import javax.sound.sampled.AudioFormat;

import com.jonasreese.sound.sg.audio.AudioToolkit;
import com.jonasreese.sound.vstcontainer.VstPlugin23Configuration;
import com.jonasreese.sound.vstcontainer.VstUiHandler;

/**
 * @author jonas.reese
 */
public class VstPluginConfiguration implements VstPlugin23Configuration {
    
    private int blockSize;
    private AudioFormat format;
    
    public VstPluginConfiguration() {
        format = AudioToolkit.getDefaultAudioFormat();
        blockSize = AudioToolkit.getBufferSize( format ) / format.getFrameSize();
    }

    public int getBlockSize() {
        System.out.println( "getBlockSize()" );
        return blockSize;
    }

    public String getLogBasePath() {
        // TODO Auto-generated method stub
        System.out.println( "getLogBasePath()" );
        return null;
    }

    public String getLogFileName() {
        // TODO Auto-generated method stub
        System.out.println( "getLogFileName()" );
        return null;
    }

    public int getMasterVersion() {
        System.out.println( "getMasterVersion()" );
        return 2300;
    }

    public float getSampleRate() {
        System.out.println( "getSampleRate() " + format.getSampleRate() );
        return format.getSampleRate();
    }

    public void setParameterAutomated( int index, float value ) {
        System.out.println( "setParameterAutomated()" );
    }

    public int canHostDo( String feature ) {
        System.out.println( "canHostDo( " + feature + " )" );
        return 0;
    }

    public VstUiHandler getUiHandler() {
        System.out.println( "getUiHandler()" );
        return null;
    }

    public String getVendor() {
        System.out.println( "getVendor()" );
        return "Jonas Reese";
    }

    public String getProductName() {
        System.out.println( "getProductName()" );
        return "SoundsGood";
    }

    public int getProductVersion() {
        System.out.println( "getProductVersion()" );
        return 1;
    }

}
