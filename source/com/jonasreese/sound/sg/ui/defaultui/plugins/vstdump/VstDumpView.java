/*
 * Created on 27.07.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.vstdump;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.plugin.view.ViewInstanceCreationFailedException;

/**
 * @author jonas.reese
 */
public class VstDumpView implements View {

    public boolean canHandle( SessionElementDescriptor sessionElement ) {
        return true;
    }

    public ViewInstance createViewInstance(
            Session session, SessionElementDescriptor sessionElementDescriptor )
    throws ViewInstanceCreationFailedException {
        return new VstDumpVi( session, this );
    }

    public boolean isAutostartView() {
        return false;
    }

    public boolean isMultipleInstancePerSessionAllowed() {
        return false;
    }

    public boolean isMultipleInstancePerSessionElementAllowed() {
        return false;
    }
    
    public void exit() {
    }

    public String getDescription() {
        return getShortDescription();
    }

    public String getName() {
        return "VstDump";
    }

    public PluginConfigurator getPluginConfigurator() {
        return null;
    }

    public String getPluginName() {
        return "VstDumpView";
    }

    public String getPluginVendor() {
        return "Jonas Reese";
    }

    public String getPluginVersion() {
        return "1.0";
    }

    public String getShortDescription() {
        return "Dumps information about installed VST plugins";
    }

    public void init() {
    }
}
