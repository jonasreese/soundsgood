/*
 * Created on 31.12.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.au;

import javax.swing.JOptionPane;

import com.jonasreese.sound.aucontainer.AUContainer;
import com.jonasreese.sound.aucontainer.AudioUnitDescriptor;
import com.jonasreese.sound.aucontainer.AudioUnitDescriptor.AudioUnitType;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.SgProperties;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;

/**
 * Initializes the AudioUnit subsystem.
 * @author Jonas Reese
 */
public class AUInitializerPlugin implements Plugin {

    public static final String AU_ENABLED = "enabled";
    public static final String USER_DEFINED_LIB_PATH_ENABLED = "userDefinedLibPathEnabled";
    public static final String USER_DEFINED_LIB_PATH = "userDefinedLibPath";

    
    private AUConfigurator configurator;
    
    public void exit() {
    }

    public String getDescription() {
        return "Initializes the Audio Unit subsystem";
    }

    public String getName() {
        return "AudioUnit initializer";
    }

    public PluginConfigurator getPluginConfigurator() {
        if (configurator == null) {
            configurator = new AUConfigurator( this );
        }
        return configurator;
    }

    public String getPluginName() {
        return "AUInitializer";
    }

    public String getPluginVendor() {
        return "Jonas Reese";
    }

    public String getPluginVersion() {
        return "0.9";
    }

    public String getShortDescription() {
        return "Initializes the Audio Unit subsystem";
    }

    public void init() {
        SgProperties p = SgEngine.getInstance().getProperties();
        if (!p.getPluginProperty( this, AU_ENABLED, true )) {
            return;
        }
        
        try {
            // initialize AU engine
            String nativeLibPath = null;
            if (p.getPluginProperty( this, USER_DEFINED_LIB_PATH_ENABLED, false )) {
                nativeLibPath = p.getPluginProperty( this, USER_DEFINED_LIB_PATH, (String) null );
            }
            AUContainer.getInstance().setUserDefinedNativeLibraryPath( nativeLibPath );
            if (!AUContainer.getInstance().isAUContainerAvailable()) {
                String message = p.getResourceBundle().getString(
                        "plugin.au.errorOnStart.auSubsystemNotAvailable" );
                if (AUContainer.getInstance().getInitFailedMessage() != null) {
                    message += p.getResourceBundle().getString(
                            "plugin.au.errorOnStart.auSubsystemNotAvailable.errorMessage",
                            AUContainer.getInstance().getInitFailedMessage() );
                }
                throw new Exception( message );
            }
            for (AudioUnitType type : AudioUnitType.values()) {
                AudioUnitDescriptor[] descriptors = AUContainer.getInstance().getAllAudioUnitDescriptors( type );
                System.out.println( "type: " + type );
                System.out.println( "============" );
                for (AudioUnitDescriptor audioUnitDescriptor : descriptors) {
                    System.out.println( audioUnitDescriptor );
                }
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    UiToolkit.getMainFrame(),
                    SgEngine.getInstance().getResourceBundle().getString(
                        "plugin.au.errorOnStart.paths",
                        ex.getMessage() == null ? "Unknown error" : ex.getMessage() ),
                    SgEngine.getInstance().getResourceBundle().getString(
                            "plugin.au.errorOnStart" ),
                    JOptionPane.ERROR_MESSAGE );
        }
    }

}
