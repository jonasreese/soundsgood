/*
 * Created on 05.01.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;

/**
 * @author jonas.reese
 */
public class SoundbusSubsystemInitializerPlugin implements Plugin {

    public SoundbusSubsystemInitializerPlugin() {
        super();
    }

    public String getName() {
        return "Soundbus subsystem initializer";
    }

    public String getShortDescription() {
        return "Initializes the Soundbus subsystem";
    }

    public String getDescription() {
        return getShortDescription();
    }

    public String getPluginName() {
        return getName();
    }

    public String getPluginVersion() {
        return "1.0";
    }

    public String getPluginVendor() {
        return "Jonas Reese";
    }

    public void init() {
        System.out.println( "Soundbus subsystem init()..." );
        SgEngine.getInstance().addFileHandler( new SoundbusFileHandler() );
        SgEngine.getInstance().addSessionElementCreationHandler(
                new SoundbusSessionElementCreationHandler() );
    }

    public void exit() {
    }

    public PluginConfigurator getPluginConfigurator() {
        return null;
    }
}
