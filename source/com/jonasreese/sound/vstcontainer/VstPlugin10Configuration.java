/*
 * Created on 02.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.vstcontainer;

/**
 * @author jonas.reese
 */
public interface VstPlugin10Configuration {
    public abstract int getBlockSize();

    public abstract String getLogBasePath();
    
    public abstract String getLogFileName();
    
    public abstract int getMasterVersion();
    
    public abstract float getSampleRate();
    
    public abstract void setParameterAutomated( int index, float value );
    
    public abstract String getVendor();
    
    public abstract String getProductName();
    
    public abstract int getProductVersion();
}
