/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 23.12.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.grid;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.SgProperties;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.util.swing.ColorSelectionButton;

/**
 * <p>
 * Implements the configuration UI panel for the Grid view and the according
 * <code>ViewConfigurator</code>.
 * </p>
 * @author jreese
 */
public class GridViewConfigurator
    extends JTabbedPane
    implements PluginConfigurator {
    
    private static final long serialVersionUID = 1;
    
    private ResourceBundle rb;
    private View parent;
    private HashMap<String,JButton> colorTableMap;
    
    private JSpinner rowHeightSpinner;
    private JSpinner midiEventTickLengthSpinner;
    private JCheckBox displayTactNumbersCheckBox;
    private JCheckBox toolTipsEnabledCheckBox;
    private JCheckBox saveViewInSessionCheckBox;
    private JCheckBox doubleBufferCheckBox;
    private JCheckBox chaseCursorCheckBox;
    private JRadioButton pagewiseChaseCursorRadioButton;
    private JRadioButton strictlyChaseCursorRadioButton;
    
    /**
     * Constructs a new <code>GridViewConfigurator</code>. 
     * @param parent The parent <code>View</code>.
     */
    public GridViewConfigurator( View parent )
    {
        this.parent = parent;
        rb = SgEngine.getInstance().getResourceBundle();
        colorTableMap = new HashMap<String,JButton>();
        addTab( rb.getString( "plugin.gridView.config.view" ), createViewTab() );
        addTab( rb.getString( "plugin.gridView.config.advanced" ), createAdvancedTab() );
    }
    
    private JPanel createColorPanel( String id, String caption )
    {
        Color defaultColor = SgEngine.getInstance().getProperties().getPluginProperty(
            parent, id, GridComponent.getDefaultColor( id ) );
        JPanel panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        JLabel label = new JLabel( caption );
        JButton button = new ColorSelectionButton(
            defaultColor, rb.getString( "plugin.gridView.config.view.colors.select" ) );
        label.setLabelFor( button );
        panel.add( label );
        panel.add( button );
        colorTableMap.put( id, button );
        return panel;
    }
    
    private JPanel createViewTab()
    {
        JPanel tab = new JPanel( new BorderLayout() );
        
        String[] colorKeys = GridComponent.getDefaultColorKeys();
        int rows = colorKeys.length / 2 + ((colorKeys.length % 2 > 0) ? 1 : 0);
        JPanel colorsPanel = new JPanel( new GridLayout( rows, 2 ) );
        colorsPanel.setBorder( new TitledBorder( rb.getString( "plugin.gridView.config.view.colors" ) ) );
        
        // fill color panel
        for (int i = 0; i < colorKeys.length; i++)
        {
            colorsPanel.add(
                createColorPanel( colorKeys[i],
                    rb.getString( "plugin.gridView.config.view.colors." + colorKeys[i] ) ) );
        }
        
        tab.add( colorsPanel, BorderLayout.NORTH );
        
        JPanel rasterPanel = new JPanel( new GridLayout( 2, 2 ) );
        rasterPanel.setBorder(
            new TitledBorder( rb.getString( "plugin.gridView.config.view.raster" ) ) );
        
        int val = SgEngine.getInstance().getProperties().getPluginProperty(
            parent, "rowHeight", GridComponent.DEFAULT_ROW_HEIGHT );
        JLabel rhLabel = new JLabel(
            rb.getString( "plugin.gridView.config.view.raster.rowHeight" ) );
        rowHeightSpinner = new JSpinner(
            new SpinnerNumberModel( val % 101, 3, 100, 1 ) );
        rhLabel.setLabelFor( rowHeightSpinner );
        JPanel rowHeightPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        rowHeightPanel.add( rhLabel );
        rowHeightPanel.add( rowHeightSpinner );
        rasterPanel.add( rowHeightPanel );
        
        val = SgEngine.getInstance().getProperties().getPluginProperty(
            parent, "midiEventTickLength", GridComponent.DEFAULT_MIDI_EVENT_TICK_LENGTH );
        JLabel metlLabel = new JLabel(
            rb.getString( "plugin.gridView.config.view.raster.midiEventTickLength" ) );
        midiEventTickLengthSpinner = new JSpinner(
            new SpinnerNumberModel( val % 101, 1, 100, 1 ) );
        metlLabel.setLabelFor( midiEventTickLengthSpinner );
        JPanel tickLengthPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        tickLengthPanel.add( metlLabel );
        tickLengthPanel.add( midiEventTickLengthSpinner );
        rasterPanel.add( tickLengthPanel );
        
        displayTactNumbersCheckBox = new JCheckBox(
            rb.getString( "plugin.gridView.config.view.raster.displayTactNumbers" ) );
        displayTactNumbersCheckBox.setSelected(
            SgEngine.getInstance().getProperties().getPluginProperty(
                parent, "displayTactNumbers", true ) );
        JPanel displayTactNumbersCheckBoxPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        displayTactNumbersCheckBoxPanel.add( displayTactNumbersCheckBox );
        rasterPanel.add( displayTactNumbersCheckBoxPanel );
        
        toolTipsEnabledCheckBox = new JCheckBox(
            rb.getString( "plugin.gridView.config.view.raster.enableToolTips" ) );
        toolTipsEnabledCheckBox.setSelected(
            SgEngine.getInstance().getProperties().getPluginProperty(
                parent, "toolTipsEnabled", true ) );
        JPanel toolTipsEnabledCheckBoxPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        toolTipsEnabledCheckBoxPanel.add( toolTipsEnabledCheckBox );
        rasterPanel.add( toolTipsEnabledCheckBoxPanel );
        
        tab.add( rasterPanel );
        return tab;
    }
    
    private JPanel createAdvancedTab()
    {
        JPanel tab = new JPanel( new BorderLayout() );
        
        JPanel cursorPanel = new JPanel( new GridLayout( 2, 2 ) );
        cursorPanel.setBorder(
            new TitledBorder( rb.getString( "plugin.gridView.config.advanced.cursor" ) ) );
        chaseCursorCheckBox = new JCheckBox(
            rb.getString( "plugin.gridView.config.advanced.cursor.chaseCursor" ) );
        chaseCursorCheckBox.setSelected(
            SgEngine.getInstance().getProperties().getPluginProperty(
                parent, "chaseCursor", true ) );
        chaseCursorCheckBox.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                pagewiseChaseCursorRadioButton.setEnabled( chaseCursorCheckBox.isSelected() );
                strictlyChaseCursorRadioButton.setEnabled( chaseCursorCheckBox.isSelected() );
            }
        } );
        cursorPanel.add( chaseCursorCheckBox );
        cursorPanel.add( new JPanel() );
        pagewiseChaseCursorRadioButton = new JRadioButton(
            rb.getString( "plugin.gridView.config.advanced.cursor.chaseCursor.pagewise" ),
            SgEngine.getInstance().getProperties().getPluginProperty(
                parent, "chaseCursor.pagewise", true ) );
        strictlyChaseCursorRadioButton = new JRadioButton(
            rb.getString( "plugin.gridView.config.advanced.cursor.chaseCursor.strict" ),
            !pagewiseChaseCursorRadioButton.isSelected() );
        ButtonGroup bgr = new ButtonGroup();
        pagewiseChaseCursorRadioButton.setEnabled( chaseCursorCheckBox.isSelected() );
        strictlyChaseCursorRadioButton.setEnabled( chaseCursorCheckBox.isSelected() );
        bgr.add( pagewiseChaseCursorRadioButton );
        bgr.add( strictlyChaseCursorRadioButton );
        cursorPanel.add( pagewiseChaseCursorRadioButton );
        cursorPanel.add( strictlyChaseCursorRadioButton );
        
        JPanel sessionPanel = new JPanel( new GridLayout( 1, 1 ) );
        sessionPanel.setBorder(
            new TitledBorder( rb.getString( "plugin.gridView.config.advanced.session" ) ) );
        saveViewInSessionCheckBox = new JCheckBox(
            rb.getString( "plugin.gridView.config.advanced.session.storeViewInSession" ) );
        saveViewInSessionCheckBox.setSelected(
            SgEngine.getInstance().getProperties().getPluginProperty(
                parent, "saveViewInSession", true ) );
        sessionPanel.add( saveViewInSessionCheckBox );
        
        JPanel renderingPanel = new JPanel( new GridLayout( 1, 1 ) );
        renderingPanel.setBorder(
            new TitledBorder( rb.getString( "plugin.gridView.config.advanced.rendering" ) ) );
        doubleBufferCheckBox = new JCheckBox(
            rb.getString( "plugin.gridView.config.advanced.rendering.doublebuffer" ) );
        doubleBufferCheckBox.setSelected(
            SgEngine.getInstance().getProperties().getPluginProperty(
                parent, "doublebuffer", true ) );
        renderingPanel.add( doubleBufferCheckBox );
        
        JPanel northPanel = new JPanel( new BorderLayout() );
        northPanel.add( cursorPanel, BorderLayout.NORTH );
        northPanel.add( sessionPanel );
        northPanel.add( renderingPanel, BorderLayout.SOUTH );
        
        tab.add( northPanel, BorderLayout.NORTH );
        
        return tab;
    }
    
    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.plugin.view.ViewConfigurator#getTitle()
     */
    public String getTitle()
    {
        return rb.getString( "plugin.gridView.config.title" );
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
        return parent;
    }
    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.plugin.view.ViewConfigurator#open()
     */
    public void open()
    {
    }
    
    /**
     * Saves all properties to <code>SgEngine.getInstance().getProperties()</code>.
     */
    public void ok()
    {
        // set colors
        SgProperties p = SgEngine.getInstance().getProperties();
        for (Object key : colorTableMap.keySet())
        {
            p.setPluginProperty(
                parent, key.toString(),
                ((ColorSelectionButton) colorTableMap.get( key )).getColor() );
        }
        
        // set raster properties
        p.setPluginProperty(
            parent,
            "rowHeight",
            ((Integer) rowHeightSpinner.getModel().getValue()).intValue() );
        p.setPluginProperty(
            parent,
            "midiEventTickLength",
            ((Integer) midiEventTickLengthSpinner.getModel().getValue()).intValue() );
        p.setPluginProperty(
            parent,
            "displayTactNumbers",
            displayTactNumbersCheckBox.isSelected() );
        p.setPluginProperty(
            parent,
            "toolTipsEnabled",
            toolTipsEnabledCheckBox.isSelected() );

        // advanced properties: cursor
        p.setPluginProperty( parent, "chaseCursor", chaseCursorCheckBox.isSelected() );
        p.setPluginProperty( parent, "chaseCursor.pagewise", pagewiseChaseCursorRadioButton.isSelected() );

        // advanced properties: session
        p.setPluginProperty(
            parent, "saveViewInSession", saveViewInSessionCheckBox.isSelected() );
        
        p.setPluginProperty(
            parent, "doublebuffer", doubleBufferCheckBox.isSelected() );
    }
    /**
     * Restores all properties in the UI from SgProperties.
     */
    public void cancel()
    {
        // restore colors
        SgProperties p = SgEngine.getInstance().getProperties();
        for (String key : colorTableMap.keySet())
        {
            Color c = p.getPluginProperty( parent, key, GridComponent.getDefaultColor( key ) );
            ((ColorSelectionButton) colorTableMap.get( key )).setColor( c );
        }
        
        // set raster properties
        int val = p.getPluginProperty( parent, "rowHeight", GridComponent.DEFAULT_ROW_HEIGHT );
        rowHeightSpinner.getModel().setValue( new Integer( val ) );
        val = p.getPluginProperty(
            parent, "midiEventTickLength", GridComponent.DEFAULT_MIDI_EVENT_TICK_LENGTH );
        midiEventTickLengthSpinner.getModel().setValue( new Integer( val ) );
        displayTactNumbersCheckBox.setSelected(
            SgEngine.getInstance().getProperties().getPluginProperty(
                parent, "displayTactNumbers", true ) );
        toolTipsEnabledCheckBox.setSelected(
            SgEngine.getInstance().getProperties().getPluginProperty(
                parent, "toolTipsEnabled", true ) );
        
        // set advanced properties: cursor
        chaseCursorCheckBox.setSelected(
            p.getPluginProperty( parent, "chaseCursor", true ) );
        pagewiseChaseCursorRadioButton.setSelected(
            p.getPluginProperty( parent, "chaseCursor.pagewise", true ) );

        // set advanced properties: session
        saveViewInSessionCheckBox.setSelected(
            p.getPluginProperty( parent, "saveViewInSession", true ) );
        
        doubleBufferCheckBox.setSelected(
            p.getPluginProperty( parent, "doublebuffer", true ) );

    }
}
