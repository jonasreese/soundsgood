/*
 * Created on 11.11.2012
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.mmj;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;

/**
 * Initializer plugin for MMJ (MIDI for Java on Mac, see http://www.humatic.de/htools/mmj.htm).
 * Checks the environment for Mac OS and loads the native library if necessary.  
 * 
 * @author Jonas Reese
 */
public class MmjInitializerPlugin implements Plugin {

    public MmjInitializerPlugin() {
    }

    public String getName() {
        return "MMJ (MIDI for Java on Mac) initializer plugin";
    }

    public String getShortDescription() {
        return "Loads the MMJ library to work around MIDI issues on Mac OS";
    }

    public String getDescription() {
        return getShortDescription();
    }

    public String getPluginName() {
        return "MmjInitializer";
    }

    public String getPluginVersion() {
        return "1.0";
    }

    public String getPluginVendor() {
        return "jr";
    }

    public void init() {
        String osName = System.getProperty("os.name");
        if (osName != null && osName.toLowerCase().contains("mac os")) {
            System.out.println("Initializing MMJ for " + osName);
            //de.humatic.mmj.MidiSystem.initMidiSystem("mmj src", "mmj dest");
            String libVersion = de.humatic.mmj.MidiSystem.getLibraryVersion();
            if (libVersion != null) {
                System.out.println("MMJ library version is " + libVersion);
                SgEngine.getInstance().getProperties().setUseMmjPatch(true);
            }
        }
    }

    public void exit() {
    }

    public PluginConfigurator getPluginConfigurator() {
        return null;
    }

}
