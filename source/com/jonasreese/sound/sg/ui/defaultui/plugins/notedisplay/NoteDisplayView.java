/*
 * Created on 01.07.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.notedisplay;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.plugin.view.ViewInstanceCreationFailedException;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * @author jonas.reese
 */
public class NoteDisplayView implements View, Icon {
    
    private Icon icon;
    

    public boolean isAutostartView() {
        return false;
    }

    public boolean isMultipleInstancePerSessionAllowed() {
        return true;
    }

    public boolean isMultipleInstancePerSessionElementAllowed() {
        return false;
    }

    public boolean canHandle( SessionElementDescriptor sessionElement ) {
        return (sessionElement instanceof MidiDescriptor);
    }

    public ViewInstance createViewInstance(
            Session session, SessionElementDescriptor sessionElementDescriptor )
    throws ViewInstanceCreationFailedException {
        return new NoteDisplayVi( this, (MidiDescriptor) sessionElementDescriptor );
    }

    public PluginConfigurator getPluginConfigurator() {
        return null;
    }

    public String getName() {
        return "NoteDisplayView";
    }

    public String getShortDescription() {
        return "Displays Notes played by MIDI input devices to the user";
    }

    public String getDescription() {
        return "";
    }

    public String getPluginName() {
        return "NoteDisplayView";
    }

    public String getPluginVersion() {
        return "0.1";
    }

    public String getPluginVendor() {
        return "Jonas Reese";
    }

    public void init() {
        icon = new ResourceLoader( getClass(), "resource/note_display.gif" ).getAsIcon();
    }

    public void exit() {
    }

    public void paintIcon( Component arg0, Graphics arg1, int arg2, int arg3 ) {
        icon.paintIcon( arg0, arg1, arg2, arg3 );
    }

    public int getIconWidth() {
        return icon.getIconWidth();
    }

    public int getIconHeight() {
        return icon.getIconHeight();
    }

}
