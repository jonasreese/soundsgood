/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 08.10.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.grid;

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
 * <b>
 * This class implements a <code>View</code> that produces <code>ViewInstance</code>
 * objects capable of presenting the user a grid with MIDI events for editing.
 * </b>
 * @author jreese
 */
public class GridView implements View, Icon {
    private ResourceBundle rb;
    private Icon icon;
    private GridViewConfigurator gridViewConfigurator;
    
    /**
     * Default constructor.
     */
    public GridView() {
        rb = SgEngine.getInstance().getResourceBundle();
        icon = new ResourceLoader( getClass(), "resource/grid.gif" ).getAsIcon();
    }
    
	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.View#isAutostartView()
	 */
	public boolean isAutostartView() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.View#isMultipleInstanceView()
	 */
	public boolean isMultipleInstancePerSessionAllowed() {
		return true;
	}
	
    public boolean isMultipleInstancePerSessionElementAllowed() {
        return true;
    }
	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.View#canHandle(SessionElementDescriptor)
	 */
	public boolean canHandle( SessionElementDescriptor sessionElement ) {
		return (sessionElement instanceof MidiDescriptor);
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.View#createViewInstance(SessionElementDescriptor)
	 */
	public ViewInstance createViewInstance(
        Session session, SessionElementDescriptor sessionElement ) throws ViewInstanceCreationFailedException {
        if (!canHandle( sessionElement )) {
            throw new IllegalArgumentException();
        }
		try {
			return new GridVi( this, (MidiDescriptor) sessionElement );
		} catch (Throwable t) {
            t.printStackTrace();
            throw new ViewInstanceCreationFailedException( t );
		}
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.Plugin#getName()
	 */
	public String getName() {
		return rb.getString( "plugin.gridView.name" );
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.Plugin#getShortDescription()
	 */
	public String getShortDescription() {
		return rb.getString( "plugin.gridView.shortDescription" );
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.Plugin#getDescription()
	 */
	public String getDescription() {
		return rb.getString( "plugin.gridView.description" );
	}
    
    public String getPluginName() {
        return "SoundsGood (c) Grid Edit Plugin";
    }
    
    public String getPluginVersion() {
        return "1.0";
    }

    public String getPluginVendor() {
        return "Jonas Reese";
    }

    public void init() {}
    public void exit() {}

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

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.View#createViewConfigurator()
     */
    public PluginConfigurator getPluginConfigurator() {
        if (gridViewConfigurator == null) {
            gridViewConfigurator = new GridViewConfigurator( this );
        }
        return gridViewConfigurator;
    }
}
