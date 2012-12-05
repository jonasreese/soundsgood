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
public interface VstPlugin23 extends VstPlugin22 {

    public static final String CANDO_HOST_START_STOP_PROCESS = "startStopProcess";
    
    public static final String CANDO_PLUG_BYPASS = "bypass";

    /**

     List of methods to be provided by a VST host version 2.3 (Steinberg VST Plug-Ins SDK) 

    [2.3]
    setTotalSampleToProcess
    getNextShellPlugin
    startProcess
    stopProcess 

     */
    
    
    public int setTotalSampleToProcess( int value );
    public PluginNameAndId getNextShellPlugin();
    public int startProcess();
    public int stopProcess();
}
