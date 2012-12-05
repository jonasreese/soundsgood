/*
 * Created on 22.10.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.vst;

import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.SgProperties;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.sound.vstcontainer.VstContainer;
import com.jonasreese.sound.vstcontainer.VstNode;
import com.jonasreese.sound.vstcontainer.VstPlugin;
import com.jonasreese.sound.vstcontainer.VstPluginDescriptor;
import com.jonasreese.sound.vstcontainer.VstPluginNotAvailableException;

/**
 * @author jonas.reese
 */
public class VstInitializerPlugin implements Plugin {
    
    private static final HashMap<String,String> PLUG_CATEGORY_MAP = new HashMap<String,String>();
    
    static {
        PLUG_CATEGORY_MAP.put( "" + VstPlugin.PLUGIN_CATEGORY_UNKNOWN, "PLUGIN_CATEGORY_UNKNOWN" );
        PLUG_CATEGORY_MAP.put( "" + VstPlugin.PLUGIN_CATEGORY_EFFECT, "PLUGIN_CATEGORY_EFFECT" );
        PLUG_CATEGORY_MAP.put( "" + VstPlugin.PLUGIN_CATEGORY_SYNTH, "PLUGIN_CATEGORY_SYNTH" );
        PLUG_CATEGORY_MAP.put( "" + VstPlugin.PLUGIN_CATEGORY_ANALYSIS, "PLUGIN_CATEGORY_ANALYSIS" );
        PLUG_CATEGORY_MAP.put( "" + VstPlugin.PLUGIN_CATEGORY_MASTERING, "PLUGIN_CATEGORY_MASTERING" );
        PLUG_CATEGORY_MAP.put( "" + VstPlugin.PLUGIN_CATEGORY_SPACIALIZER, "PLUGIN_CATEGORY_SPACIALIZER" );
        PLUG_CATEGORY_MAP.put( "" + VstPlugin.PLUGIN_CATEGORY_ROOM_FX, "PLUGIN_CATEGORY_ROOM_FX" );
        PLUG_CATEGORY_MAP.put( "" + VstPlugin.PLUGIN_CATEGORY_SURROUND_FX, "PLUGIN_CATEGORY_SURROUND_FX" );
        PLUG_CATEGORY_MAP.put( "" + VstPlugin.PLUGIN_CATEGORY_RESTORATION, "PLUGIN_CATEGORY_RESTORATION" );
        PLUG_CATEGORY_MAP.put( "" + VstPlugin.PLUGIN_CATEGORY_OFFLINE_PROCESS, "PLUGIN_CATEGORY_OFFLINE_PROCESS" );
        PLUG_CATEGORY_MAP.put( "" + VstPlugin.PLUGIN_CATEGORY_SHELL, "PLUGIN_CATEGORY_SHELL" );
        PLUG_CATEGORY_MAP.put( "" + VstPlugin.PLUGIN_CATEGORY_GENERATOR, "PLUGIN_CATEGORY_GENERATOR" );
    }
    

    public static final String VST_ENABLED = "enabled";
    public static final String VST_PATHS_PROPERTY = "vstPaths";
    public static final String USER_DEFINED_LIB_PATH_ENABLED = "userDefinedLibPathEnabled";
    public static final String USER_DEFINED_LIB_PATH = "userDefinedLibPath";


    private VstConfigurator configurator;
    private JMenu vstMenu;
    
    public String getName() {
        return "VST initializer";
    }

    public String getShortDescription() {
        return "Provides VST plugins";
    }

    public String getDescription() {
        return "Initializes the VST subsystem and creates UI elements for VST plugin access";
    }

    public String getPluginName() {
        return "VstInitializer";
    }

    public String getPluginVersion() {
        return "0.1";
    }

    public String getPluginVendor() {
        return "Jonas Reese";
    }
    
//    private JMenu getOptionsMenu( JMenuBar menuBar ) {
//        String s = SgEngine.getInstance().getResourceBundle().getString( "menu.options" );
//        for (int i = 0; i < menuBar.getMenuCount(); i++) {
//            JMenu menu = menuBar.getMenu( i );
//            if (menu != null) {
//                if (s.equals( menu.getText() )) {
//                    return menu;
//                }
//            }
//        }
//        return null;
//    }
    
//    private String getCanDoString( VstPlugin plugin, String canDo ) {
//        return "canDo( " + canDo + " ): " + plugin.canDo( canDo );
//    }
    
//    private void printPlugin( VstPlugin plugin, File file ) {
//        System.out.println( "==========================================" );
//        System.out.println( "File: " + (file == null ? null : file.getName()) );
//        System.out.println( "Vendor string: " + plugin.getVendorString() );
//        System.out.println( "Vendor version: " + plugin.getVendorVersion() );
//        System.out.println( "VST version: " + plugin.getVstVersion() );
//        com.jonasreese.sound.vstcontainer.PluginNameAndId nid = plugin.getNextShellPlugin();
//        System.out.println( "VST next shell plugin name and ID: " + nid.getName() + " - " + nid.getId() );
//        System.out.println( "startProcess() returns " + plugin.startProcess() );
//        MidiProgramName midiProgramName = new MidiProgramName();
//        plugin.getMidiProgramName( 0, midiProgramName );
//        System.out.println( "MidiProgramName: " + midiProgramName );
//        System.out.println( "EffectName; " + plugin.getEffectName() );
//        System.out.println( "errorText: " + plugin.getErrorText() );
//        System.out.println( "tailSize: " + plugin.getTailSize() );
//        System.out.println( "parameterProperties: " + plugin.getParameterProperties( 0 ) );
//        System.out.println( "plugCategory: " + plugin.getPlugCategory() );
//        System.out.println( "productString: " + plugin.getProductString() );
//        
//        System.out.println( "====== CAPABILITIES ======" );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_ACCEPT_IO_CHANGES ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_ASYNC_PROCESSING ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_CLOSE_FILE_SELECTOR ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_EDIT_FILE ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_OFFLINE ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_OPEN_FILE_SELECTOR ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_RECEIVE_VST_EVENTS ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_RECEIVE_VST_TIME_INFO ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_REPORT_CONNECTION_CHANGES ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_SEND_VST_EVENTS ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_SEND_VST_MIDI_EVENT ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_SEND_VST_TIME_INFO ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_SIZE_WINDOW ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_START_STOP_PROCESS ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_SUPPLY_IDLE ) );
//        System.out.println( getCanDoString( plugin, VstPlugin20.CANDO_HOST_SUPPORT_SHELL ) );
//        System.out.println( "==========================================" );
//        plugin.open();
//        System.out.println( "plugin opened" );
////        VstVariableIo io = new VstVariableIo( new float[10][5], new float[7][3], 0, 0 );
////        plugin.processVariableIo( io );
////        printIo( io.getInputs(), io.getOutputs() );
////        float[] buff = plugin.getDestinationBuffer();
////        StringBuffer sb = new StringBuffer();
////        for (int i = 0; i < buff.length; i++) {
////            sb.append( buff[i] );
////            sb.append( " | " );
////        }
////        System.out.println( sb );
////        plugin.setSpeakerArrangement( new VstSpeakerArrangement(), new VstSpeakerArrangement() );
////        float[][] inputs = new float[2][15];
////        inputs[1][3] = 5.345f; 
////        float[][] outputs = new float[2][15];
////        plugin.process( inputs, outputs, 15 );
////        printIo( inputs, outputs );
////        plugin.close();
////        System.out.println( "plugin closed" );
////        System.out.println( "stopProcess() returns " + plugin.stopProcess() );
//        System.out.println( "plugin.getProgram() = " + plugin.getProgram() );
//        plugin.setProgram( 1 );
//        System.out.println( "plugin.getProgram() = " + plugin.getProgram() );
//        System.out.println( "plugin.getProgramName() = " + plugin.getProgramName() );
//        plugin.setProgramName( "testProgram" );
//        System.out.println( "plugin.getProgramName() = " + plugin.getProgramName() );
//        System.out.println( "plugin.getParameterLabel(0) = " + plugin.getParameterLabel( 0 ) );
//        System.out.println( "plugin.getParameterDisplay(0) = " + plugin.getParameterDisplay( 0 ) );
//        System.out.println( "plugin.getParameterName(0) = " + plugin.getParameterName( 0 ) );
//        System.out.println( "plugin.getVu() = " + plugin.getVu() );
//        System.out.println( "plugin.getNumParams() = " + plugin.getNumParams() );
//        System.out.println( "plugin.getNumPrograms() = " + plugin.getNumPrograms() );
//        System.out.println( "plugin.getUniqueId() = " + plugin.getUniqueId() );
//    }

//    private void printIo(float[][] inputs, float[][] outputs) {
//        for (int i = 0; i < inputs.length; i++) {
//            StringBuffer sb = new StringBuffer();
//            for (int j = 0; j < inputs[i].length; j++) {
//                sb.append( inputs[i][j] );
//                sb.append( "|" );
//            }
//            System.out.println( "input[" + i + "] = " + sb );
//        }
//        for (int i = 0; i < outputs.length; i++) {
//            StringBuffer sb = new StringBuffer();
//            for (int j = 0; j < outputs[i].length; j++) {
//                sb.append( outputs[i][j] );
//                sb.append( "|" );
//            }
//            System.out.println( "output[" + i + "] = " + sb );
//        }
//    }

    public void init() {
        SgProperties p = SgEngine.getInstance().getProperties();
        if (!p.getPluginProperty( this, VST_ENABLED, true )) {
            return;
        }
        
        try {
            // initialize VST engine
            VstContainer.getInstance().setVstPaths( p.getPluginProperty( this, VST_PATHS_PROPERTY, "" ) );
            String nativeLibPath = null;
            if (p.getPluginProperty( this, USER_DEFINED_LIB_PATH_ENABLED, false )) {
                nativeLibPath = p.getPluginProperty( this, USER_DEFINED_LIB_PATH, (String) null );
            }
            VstContainer.getInstance().setUserDefinedNativeLibraryPath( nativeLibPath );
            if (!VstContainer.getInstance().isVstContainerAvailable()) {
                String message = p.getResourceBundle().getString(
                        "plugin.vst.errorOnStart.vstSubsystemNotAvailable" );
                if (VstContainer.getInstance().getInitFailedMessage() != null) {
                    message += p.getResourceBundle().getString(
                            "plugin.vst.errorOnStart.vstSubsystemNotAvailable.errorMessage",
                            VstContainer.getInstance().getInitFailedMessage() );
                }
                throw new Exception( message );
            }
            // finally, set configuration for all VSTPlugins
            VstContainer.getInstance().setConfiguration( new VstPluginConfiguration() );
        } catch (Exception ex) {
            //ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    UiToolkit.getMainFrame(),
                    SgEngine.getInstance().getResourceBundle().getString(
                        "plugin.vst.errorOnStart.paths",
                        ex.getMessage() == null ? "Unknown error" : ex.getMessage() ),
                    SgEngine.getInstance().getResourceBundle().getString(
                            "plugin.vst.errorOnStart" ),
                    JOptionPane.ERROR_MESSAGE );
        }

        vstMenu = new JMenu( "VST" );
        // create VST menu from VST plugin tree structure
        VstNode node = VstContainer.getInstance().getVstRoot();
        addToMenu( vstMenu, node );
        
        //UiToolkit.getDefaultMenuBar().add( vstMenu );
    }
    
    private void startVstPlugin( VstPluginDescriptor pluginDescriptor ) throws VstPluginNotAvailableException {
        System.out.println( "File : " + pluginDescriptor.getPluginLibrary() );
        
        VstPlugin plugin = pluginDescriptor.createPlugin();
        System.out.println( "pluginCategory: " + PLUG_CATEGORY_MAP.get( "" + plugin.getPlugCategory() ) );
        plugin.setSampleRate( 44100.0f );
        plugin.setBlockSize( 5512 );
        plugin.open();
        System.out.println( System.getProperty("awt.toolkit" ) );
        System.out.println( "plugCanDo( BYPASS ): " + plugin.canDo( VstPlugin.CANDO_PLUG_BYPASS ) );
        System.out.println( "getProgram: " + plugin.getProgram() );
        System.out.println( "getVstVersion: " + plugin.getVstVersion() );
        System.out.println( "getProgramNameIndexed( -1, 0 ): " + plugin.getProgramNameIndexed( -1, 0 ) );
        System.out.println( "getProgramName: " + plugin.getProgramName() );
        System.out.println( "getProgram: " + plugin.getProgram() );
        System.out.println( "plugCanDo( VstPlugin.CANDO_PLUG_MIDI_PROGRAM_NAMES ): " + plugin.canDo( VstPlugin.CANDO_PLUG_MIDI_PROGRAM_NAMES ) );
        System.out.println( "getVendorString: " + plugin.getVendorString() );
        plugin.openEditWindow();
        //plugin.close();
    }
    
    private void addToMenu( JMenu menu, VstNode node ) {
        VstNode[] children = node.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i].containsPlugins()) {
                JMenu newMenu = new JMenu( children[i].getName() );
                menu.add( newMenu );
                addToMenu( newMenu, children[i] );
            }
        }
        VstPluginDescriptor[] plugins = node.getPluginDescriptors();
        for (int i = 0; i < plugins.length; i++) {
            final VstPluginDescriptor _plugin = plugins[i];
            Action action = new AbstractAction( plugins[i].getName() ) {
                private static final long serialVersionUID = 1L;
                public void actionPerformed( ActionEvent e ) {
                    //printPlugin( _plugin, _plugin.getPluginFile() );
                    try {
                        startVstPlugin( _plugin );
                    } catch (VstPluginNotAvailableException e1) {
                        e1.printStackTrace();
                    }
                }
            };
            menu.add( action );
        }
    }

    public void exit() {
    }

    public PluginConfigurator getPluginConfigurator() {
        if (configurator == null) {
            configurator = new VstConfigurator( this );
        }
        return configurator;
    }
}
