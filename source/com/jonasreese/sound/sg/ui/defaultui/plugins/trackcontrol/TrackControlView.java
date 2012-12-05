/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 17.09.2004
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.trackcontrol;

import java.awt.Component;
import java.awt.Graphics;
import java.util.ResourceBundle;

import javax.swing.Icon;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.plugin.view.ViewInstanceCreationFailedException;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * This is the main class of the the <i>TrackControl</i> plugin.
 * The TrackControl plugin allows the user to manage the tracks within a MIDI element,
 * such as muting tracks or selecting them for record or solo play.
 * @author jonas.reese
 */
public class TrackControlView implements View, Icon {
    private ResourceBundle rb;
    private Icon icon;

    public TrackControlView() {
        rb = SgEngine.getInstance().getResourceBundle();
        icon = new ResourceLoader(getClass(), "resource/trackcontrol.gif").getAsIcon();
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.plugin.view.View#isAutostartView()
     */
    public boolean isAutostartView() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.plugin.view.View#isMultipleInstanceView()
     */
    public boolean isMultipleInstancePerSessionAllowed() {
        return false;
    }
    
    public boolean isMultipleInstancePerSessionElementAllowed() {
        return false;
    }

    /* (non-Javadoc)
     */
    public boolean canHandle(SessionElementDescriptor d) {
        return true;
    }

    public void init() {}
    public void exit() {}

    /* (non-Javadoc)
     */
    public ViewInstance createViewInstance(Session session, SessionElementDescriptor d)
            throws ViewInstanceCreationFailedException {
        MidiDescriptor midiDescriptor = null;
        if (d instanceof MidiDescriptor) {
            midiDescriptor = (MidiDescriptor) d;
        }
        return new TrackControlVi(this, session, midiDescriptor);
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.plugin.view.View#createViewConfigurator()
     */
    public PluginConfigurator getPluginConfigurator() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.Plugin#getName()
     */
    public String getName() {
        return rb.getString("plugin.trackControl.name");
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.Plugin#getShortDescription()
     */
    public String getShortDescription() {
        return rb.getString("plugin.trackControl.shortDescription");
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.Plugin#getDescription()
     */
    public String getDescription() {
        return getShortDescription();
    }

    public String getPluginName()
    {
        return "SoundsGood (c) Track Control Plugin";
    }
    
    public String getPluginVersion()
    {
        return "1.0";
    }

    public String getPluginVendor()
    {
        return "Jonas Reese";
    }

    /* (non-Javadoc)
     * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
     */
    public void paintIcon( Component c, Graphics g, int x, int y ) {
        icon.paintIcon( c, g, x, y );
    }

    /* (non-Javadoc)
     * @see javax.swing.Icon#getIconWidth()
     */
    public int getIconWidth() {
        return icon.getIconWidth();
    }

    /* (non-Javadoc)
     * @see javax.swing.Icon#getIconHeight()
     */
    public int getIconHeight() {
        return icon.getIconHeight();
    }
}
