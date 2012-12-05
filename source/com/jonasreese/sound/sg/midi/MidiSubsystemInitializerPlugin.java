/*
 * Created on 05.01.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;

/**
 * @author jonas.reese
 */
public class MidiSubsystemInitializerPlugin implements Plugin {

    public MidiSubsystemInitializerPlugin() {
        super();
    }

    public String getName() {
        return "MIDI subsystem initializer";
    }

    public String getShortDescription() {
        return "Initializes the MIDI subsystem";
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

    // all MIDI-specific initialization stuff here...
    public void init() {
        System.out.println( "MIDI subsystem init()..." );
        SgEngine.getInstance().addFileHandler( new MidiFileHandler() );
        SgEngine.getInstance().addSessionElementCreationHandler(
                new MidiSessionElementCreationHandler() );
    }

    public void exit() {
    }

    public PluginConfigurator getPluginConfigurator() {
        return null;
    }
}
