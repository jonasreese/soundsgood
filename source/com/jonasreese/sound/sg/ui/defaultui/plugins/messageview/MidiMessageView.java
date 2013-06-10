/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 20.09.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.messageview;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <b>
 * A simple <code>View</code> that displays the MIDI messages.
 * </b>
 * @author jreese
 */
public class MidiMessageView implements View, Icon
{
    private ResourceBundle rb;
    private Icon icon;

    /**
     * Default constructor (invoked by plugin subsystem).
     */
    public MidiMessageView()
    {
        rb = SgEngine.getInstance().getResourceBundle();
        icon = new ResourceLoader(
            getClass(), "resource/midimessageview.gif" ).getAsIcon();
    }

	/* (non-Javadoc)
	 */
	public boolean canHandle( SessionElementDescriptor d )
	{
		return (d instanceof MidiDescriptor);
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.Plugin#getName()
	 */
	public String getName()
	{
		return rb.getString( "plugin.midiMessageView.name" );
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.Plugin#getShortDescription()
	 */
	public String getShortDescription()
	{
		return rb.getString( "plugin.midiMessageView.shortDescription" );
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.Plugin#getDescription()
	 */
	public String getDescription()
	{
		return rb.getString( "plugin.midiMessageView.description" );
	}

    public String getPluginName()
    {
        return "SoundsGood (c) MIDI Message View Plugin";
    }
    
    public String getPluginVersion()
    {
        return "1.0";
    }

    public String getPluginVendor()
    {
        return "Jonas Reese";
    }

    public void init() {}
    public void exit() {}

    /* (non-Javadoc)
     */
    public ViewInstance createViewInstance( Session session, SessionElementDescriptor d )
    {
        if (!canHandle( d ))
        {
            throw new IllegalArgumentException();
        }
        return new ViewInstanceImpl( (MidiDescriptor) d );
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
        return true;
    }
    public boolean isMultipleInstancePerSessionElementAllowed() {
        return false;
    }
    
    class MidiMessagePanel extends JScrollPane
    {
        private static final long serialVersionUID = 1;
        MidiDescriptor descriptor;

		public MidiMessagePanel( MidiDescriptor descriptor )
		{
            this.descriptor = descriptor;
            new Thread()
            {
                public void run()
                {
                    MidiDescriptor descriptor = MidiMessageView.MidiMessagePanel.this.descriptor;
                    Session session = SgEngine.getInstance().getActiveSession();
                    JTextArea ta = new JTextArea();
                    if (session != null)
                    {
                        if (descriptor != null)
                        {
                            try
        					{
                                Sequence seq = descriptor.getSequence();
                                if (descriptor.getFile() != null)
                                {
                                    ta.append( "File name: " +
                                        descriptor.getFile().getName() + "\n\n" );
                                }
                                ta.append( "Length (micros): " + seq.getMicrosecondLength() + "\n\n" );
                                Track[] tracks = seq.getTracks();
                                ta.append( "Tracks: " + tracks.length + "\n" );
                                for (int i = 0; i < tracks.length; i++)
                                {
                                    ta.append( "    Track " + (i + 1) +":\n" );
                                    ta.append( "    Length (ticks) " + tracks[i].ticks() +"\n" );
                                    ta.append( "    Size (events) " + tracks[i].size() +"\n" );
                                    ta.append( "\n" );
                                }
        					}
        					catch (InvalidMidiDataException e)
        					{
        						e.printStackTrace();
        					}
        					catch (IOException e)
        					{
        						e.printStackTrace();
        					}
                        }
                    }
                    setViewportView( ta );
                    Dimension d = getPreferredSize();
                    Component c = UiToolkit.getViewContainer(
                        MidiMessageView.MidiMessagePanel.this );
                    c.setBounds( 100, 50, d.width + 50, d.height + 50 );
                }
            }.start();
		}
    }
    
    class ViewInstanceImpl implements ViewInstance
    {
        MidiDescriptor descriptor;
        ViewInstanceImpl( MidiDescriptor descriptor )
        {
            this.descriptor = descriptor;
        }
        
        public Object getUiObject( ViewContainer parentUiObject )
        {
            return new MidiMessagePanel( descriptor );
        }

		public void close()
		{
		}

		public View getView()
		{
			return MidiMessageView.this;
		}

        public void open()
        {
        }

        public void activate()
        {
        }
        
        public void deactivate()
        {
        }

		public boolean isSetBoundsAllowed()
		{
			return false;
		}
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.View#createViewConfigurator()
     */
    public PluginConfigurator getPluginConfigurator()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
