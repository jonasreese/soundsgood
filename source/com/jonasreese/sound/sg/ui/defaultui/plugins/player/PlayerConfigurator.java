/*
 * Created on 10.03.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.player;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.SgProperties;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;

/**
 * The <code>ViewConfigurator</code> implementation for the player
 * plugin view.
 * @author jreese
 */
public class PlayerConfigurator extends JTabbedPane implements PluginConfigurator
{
    private static final long serialVersionUID = 1;
    
    private PlayerView view;
    private ResourceBundle rb;

    private JRadioButton defaultDisplayRadioButton;
    private JRadioButton lcdDisplayRadioButton;

	/**
     * Constructs a new <code>PlayerConfigurator</code>.
	 * @param view The parent view.
	 */
	public PlayerConfigurator(PlayerView view)
	{
		this.view = view;

        rb = SgEngine.getInstance().getResourceBundle();
        
        addTab( rb.getString( "plugin.playerView.config.view" ), createViewTab() );
	}

    private JPanel createViewTab()
    {
        JPanel tab = new JPanel( new BorderLayout() );
        
        boolean b = SgEngine.getInstance().getProperties().getPluginProperty(
            view, "lcdDisplay", true );
        defaultDisplayRadioButton = new JRadioButton(
            rb.getString( "plugin.playerView.config.view.display.default" ), !b );
        lcdDisplayRadioButton = new JRadioButton(
            rb.getString( "plugin.playerView.config.view.display.lcd" ), b );
        ButtonGroup bgr = new ButtonGroup();
        bgr.add( defaultDisplayRadioButton );
        bgr.add( lcdDisplayRadioButton );
        JPanel displayTypePanel = new JPanel( new GridLayout( 2, 1 ) );
        displayTypePanel.add( lcdDisplayRadioButton );
        displayTypePanel.add( defaultDisplayRadioButton );
        JPanel displayPanel = new JPanel( new BorderLayout() );
        displayPanel.add( displayTypePanel, BorderLayout.WEST );
        displayPanel.setBorder( new TitledBorder( rb.getString(
            "plugin.playerView.config.view.display" ) ) );
        tab.add( displayPanel, BorderLayout.NORTH );
        
        return tab;
    }

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.plugin.view.ViewConfigurator#getTitle()
	 */
	public String getTitle()
	{
		return rb.getString( "plugin.playerView.config.title" );
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.plugin.view.ViewConfigurator#getUiObject()
	 */
	public Object getUiObject()
	{
		return this;
	}

	public Plugin getPlugin()
	{
		return view;
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.plugin.view.ViewConfigurator#open()
	 */
	public void open()
	{
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.plugin.view.ViewConfigurator#ok()
	 */
	public void ok()
	{
        SgProperties p = SgEngine.getInstance().getProperties();
        
        p.setPluginProperty( view, "lcdDisplay", lcdDisplayRadioButton.isSelected() );
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.plugin.view.ViewConfigurator#cancel()
	 */
	public void cancel()
	{
        SgProperties p = SgEngine.getInstance().getProperties();
        
        boolean b = p.getPluginProperty( view, "lcdDisplay", true );
        lcdDisplayRadioButton.setSelected( b );
        defaultDisplayRadioButton.setSelected( !b );
	}

}
