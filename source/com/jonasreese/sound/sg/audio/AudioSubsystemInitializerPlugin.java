/*
 * Created on 18.12.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;

/**
 * @author jonas.reese
 */
public class AudioSubsystemInitializerPlugin implements Plugin {

    public AudioSubsystemInitializerPlugin() {
        super();
    }

    public String getName() {
        return "Audio subsystem initializer";
    }

    public String getShortDescription() {
        return "Initializes the audio subsystem";
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

    // all audio-specific initialization stuff here...
    public void init() {
        System.out.println( "Audio subsystem init()..." );
        SgEngine.getInstance().addFileHandler( new AudioFileHandler() );
        SgEngine.getInstance().addSessionElementCreationHandler(
                new AudioSessionElementCreationHandler() );
    }

    public void exit() {
    }

    public PluginConfigurator getPluginConfigurator() {
        return null;
    }
}
