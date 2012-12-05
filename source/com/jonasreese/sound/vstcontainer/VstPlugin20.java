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
public interface VstPlugin20 extends VstPlugin10 {
    
    public static final String CANDO_HOST_ACCEPT_IO_CHANGES   = "acceptIOChanges";
    public static final String CANDO_HOST_ASYNC_PROCESSING    = "asyncProcessing";
    public static final String CANDO_HOST_CLOSE_FILE_SELECTOR = "closeFileSelector";
    public static final String CANDO_HOST_EDIT_FILE   = "editFile";
    public static final String CANDO_HOST_OFFLINE = "offline";
    public static final String CANDO_HOST_OPEN_FILE_SELECTOR  = "openFileSelector";
    public static final String CANDO_HOST_RECEIVE_VST_EVENTS  = "receiveVstEvents";
    public static final String CANDO_HOST_RECEIVE_VST_MIDI_EVENT  = "receiveVstMidiEvent";
    public static final String CANDO_HOST_RECEIVE_VST_TIME_INFO   = "receiveVstTimeInfo";
    public static final String CANDO_HOST_REPORT_CONNECTION_CHANGES   = "reportConnectionChanges";
    public static final String CANDO_HOST_SEND_VST_EVENTS = "sendVstEvents";
    public static final String CANDO_HOST_SEND_VST_MIDI_EVENT = "sendVstMidiEvent";
    public static final String CANDO_HOST_SEND_VST_TIME_INFO  = "sendVstTimeInfo";
    public static final String CANDO_HOST_SIZE_WINDOW = "sizeWindow";
    public static final String CANDO_HOST_START_STOP_PROCESS  = "startStopProcess";
    public static final String CANDO_HOST_SUPPLY_IDLE = "supplyIdle";
    public static final String CANDO_HOST_SUPPORT_SHELL   = "supportShell";
    
    public static final String CANDO_PLUG_SEND_VST_EVENTS = "sendVstEvents";
    public static final String CANDO_PLUG_SEND_VST_MIDI_EVENT = "sendVstMidiEvent";
    public static final String CANDO_PLUG_SEND_VST_TIME_INFO = "sendVstTimeInfo";
    public static final String CANDO_PLUG_RECEIVE_VST_EVENTS = "receiveVstEvents";
    public static final String CANDO_PLUG_RECEIVE_VST_MIDI_EVENT = "receiveVstMidiEvent";
    public static final String CANDO_PLUG_RECEIVE_VST_TIME_INFO = "receiveVstTimeInfo";
    public static final String CANDO_PLUG_OFFLINE = "offline";
    public static final String CANDO_PLUG_PLUG_AS_CHANNEL_INSERT = "plugAsChannelInsert";
    public static final String CANDO_PLUG_PLUG_AS_SEND = "plugAsSend";
    public static final String CANDO_PLUG_MIX_DRY_WET = "mixDryWet";
    public static final String CANDO_PLUG_NO_REAL_TIME = "noRealTime";
    public static final String CANDO_PLUG_MULTIPASS = "multipass";
    public static final String CANDO_PLUG_METAPASS = "metapass";
    public static final String CANDO_PLUG_1_IN_1_OUT = "1in1out";
    public static final String CANDO_PLUG_1_IN_2_OUT = "1in2out";
    public static final String CANDO_PLUG_2_IN_1_OUT = "2in1out";
    public static final String CANDO_PLUG_2_IN_2_OUT = "2in2out";
    public static final String CANDO_PLUG_2_IN_4_OUT = "2in4out";
    public static final String CANDO_PLUG_4_IN_2_OUT = "4in2out";
    public static final String CANDO_PLUG_4_IN_4_OUT = "4in4out";
    public static final String CANDO_PLUG_4_IN_8_OUT ="4in8out";  // 4:2 matrix to surround bus
    public static final String CANDO_PLUG_8_IN_4_OUT = "8in4out"; // surround bus to 4:2 matrix
    public static final String CANDO_PLUG_8_IN_8_OUT = "8in8out";
    
    public static final int PLUGIN_CATEGORY_UNKNOWN = 0;
    public static final int PLUGIN_CATEGORY_EFFECT = 1;
    public static final int PLUGIN_CATEGORY_SYNTH = 2;
    public static final int PLUGIN_CATEGORY_ANALYSIS = 3;
    public static final int PLUGIN_CATEGORY_MASTERING = 4;
    public static final int PLUGIN_CATEGORY_SPACIALIZER = 5;   // like surround panners
    public static final int PLUGIN_CATEGORY_ROOM_FX = 6;       //like delays and reverbs.
    public static final int PLUGIN_CATEGORY_SURROUND_FX = 7;   //Dedicated surround processor.
    public static final int PLUGIN_CATEGORY_RESTORATION = 8;
    public static final int PLUGIN_CATEGORY_OFFLINE_PROCESS = 9;
    public static final int PLUGIN_CATEGORY_SHELL = 10;       // plugin which is only a container of plugins
    public static final int PLUGIN_CATEGORY_GENERATOR = 11;
    
    
    /**
    
    List of methods to be provided by a VST host version 2.0 (Steinberg VST Plug-Ins SDK) 
   
   canDo *
   canParameterBeAutomated
   copyProgram
   fxIdle
   getChannelParameter
   getNumCategories
   getProgramNameIndexed *
   getInputProperties
   getOutputProperties
   getIcon
   getEffectName
   getErrorText
   getGetTailSize
   getParameterProperties
   getPlugCategory *
   getProductString *
   getVendorString *
   getVendorVersion
   getVstVersion
   inputConnected
   outputConnected
   keysRequired
   offlineNotify
   offlineGetNumPasses
   offlineGetNumMetaPasses
   offlinePrepare
   offlineRun
   processEvents
   processVariableIo
   reportCurrentPosition
   reportDestinationBuffer
   setBlockSizeAndSampleRate
   setBypass *
   setSpeakerArrangement
   setViewPosition
   string2parameter *
   vendorSpecific
   
   */

    public int canDo( String feature );
    public boolean canParameterBeAutomated( int index );
    public boolean copyProgram( int destination );
    public int fxIdle();
    public void editIdle();
    public float getChannelParameter( int channel, int index );
    public int getNumCategories();
    public String getProgramNameIndexed( int category, int index );
    public VstPinProperties getInputProperties( int index );
    public VstPinProperties getOutputProperties( int index );
    public byte[] getIcon();
    public String getEffectName();
    public String getErrorText();
    public int getTailSize();
    public VstParameterProperties getParameterProperties( int index );
    public int getPlugCategory();
    public String getProductString();
    public String getVendorString();
    public int getVendorVersion();
    public int getVstVersion();
    public void inputConnected( int index, boolean state );
    public void outputConnected( int index, boolean state );
    public boolean keysRequired();
    public int processEvents( VstEvent[] e );
    public boolean processVariableIo( VstVariableIo varIo );
    public int getCurrentPosition();
    public float[] getDestinationBuffer();
    public void setBlockSizeAndSampleRate( int blockSize, float sampleRate );
    public boolean setBypass( boolean bypass );
    public boolean setSpeakerArrangement( VstSpeakerArrangement pluginInput, VstSpeakerArrangement pluginOutput );
    public boolean setViewPosition( int x, int y );
    public boolean stringToParameter( int index, String value );
}
