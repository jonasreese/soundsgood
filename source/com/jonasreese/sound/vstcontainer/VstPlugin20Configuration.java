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
public interface VstPlugin20Configuration extends VstPlugin10Configuration {
    public int canHostDo( String feature );
    
    public VstUiHandler getUiHandler();
}
