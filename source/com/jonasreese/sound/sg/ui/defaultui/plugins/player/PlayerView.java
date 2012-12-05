/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 11.10.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.player;

import java.awt.Component;
import java.awt.Graphics;
import java.util.ResourceBundle;

import javax.swing.Icon;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.plugin.view.ViewInstanceCreationFailedException;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <b>
 * </b>
 * @author jreese
 */
public class PlayerView implements View, Icon
{
    private ResourceBundle rb;
    private Icon icon;
    private PluginConfigurator viewConfigurator;
    
    /**
     * Default constructor.
     */
    public PlayerView()
    {
        rb = SgEngine.getInstance().getResourceBundle();
        icon = new ResourceLoader( getClass(), "resource/player.gif" ).getAsIcon();
    }
    
    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.View#isAutostartView()
     */
    public boolean isAutostartView()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.View#isMultipleInstanceView()
     */
    public boolean isMultipleInstancePerSessionAllowed()
    {
        return false;
    }

    public boolean isMultipleInstancePerSessionElementAllowed() {
        return false;
    }
    
    /* (non-Javadoc)
     */
    public boolean canHandle( SessionElementDescriptor d )
    {
        return true;
    }

    public void init() {}
    public void exit() {}

    /* (non-Javadoc)
     */
    public ViewInstance createViewInstance(
        Session session, SessionElementDescriptor d ) throws ViewInstanceCreationFailedException
    {
        if (!canHandle( d ))
        {
            throw new IllegalArgumentException();
        }
        try
        {
            return new PlayerVi( this, session );
        }
        catch (Throwable t)
        {
            throw new ViewInstanceCreationFailedException( t );
        }
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.Plugin#getName()
     */
    public String getName()
    {
        return rb.getString( "plugin.playerView.name" );
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.Plugin#getShortDescription()
     */
    public String getShortDescription()
    {
        return rb.getString( "plugin.playerView.shortDescription" );
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.Plugin#getDescription()
     */
    public String getDescription()
    {
        return rb.getString( "plugin.playerView.description" );
    }

    public String getPluginName()
    {
        return "SoundsGood (c) Player/Recorder Plugin";
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
    public void paintIcon( Component c, Graphics g, int x, int y )
    {
        icon.paintIcon( c, g, x, y );
    }

    /* (non-Javadoc)
     * @see javax.swing.Icon#getIconWidth()
     */
    public int getIconWidth()
    {
        return icon.getIconWidth();
    }

    /* (non-Javadoc)
     * @see javax.swing.Icon#getIconHeight()
     */
    public int getIconHeight()
    {
        return icon.getIconHeight();
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.View#createViewConfigurator()
     */
    public PluginConfigurator getPluginConfigurator()
    {
        if (viewConfigurator == null)
        {
            viewConfigurator = new PlayerConfigurator( this ); 
        }
        return viewConfigurator;
    }
}
