/*
 * Created on 22.10.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.vst;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.SgProperties;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.util.ParametrizedResourceBundle;
import com.jonasreese.util.swing.DirectoryTreePanel;

/**
 * @author jonas.reese
 */
public class VstConfigurator extends JTabbedPane implements PluginConfigurator {
    private static final long serialVersionUID = 1L;
    
    private Plugin parent;
    
    private JTextField pathsTextField;
    private JButton selectPathButton;
    private JTextField nativeLibPathTextField;
    private JButton selectNativeLibPathButton;
    private JComboBox nativeLibPathSelectionComboBox;
    private JCheckBox enabledCheckBox;
    
    
    public VstConfigurator( Plugin parent ) {
        this.parent = parent;
        ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        addTab( rb.getString( "plugin.vst.config.default" ), createDefaultTab() );
        addTab( rb.getString( "plugin.vst.config.advanced" ), createAdvancedTab() );
        cancel(); // initialize with defaults
    }
    
    private JPanel createDefaultTab() {
        final ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        JPanel tab = new JPanel( new BorderLayout() );
        JPanel pathPanel = new JPanel( new GridLayout( 2, 1 ) );
        pathsTextField = new JTextField( 40 );
        selectPathButton = new JButton( rb.getString( "plugin.vst.config.select" ) );
        selectPathButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                String paths = pathsTextField.getText();
                paths = DirectoryTreePanel.showDirectoryTreeDialog(
                        UiToolkit.getMainFrame(),
                        "Select Path", "System", paths, rb.getString( "ok" ), rb.getString( "cancel" ) );
                if (paths != null) {
                    pathsTextField.setText( paths );
                }
            }
        } );
        pathPanel.add( new JLabel( rb.getString( "plugin.vst.config.default.pathLabel", File.pathSeparator ) ) );
        pathPanel.add( pathsTextField );
        //pathPanel.add( selectPathButton );
        JPanel enabledPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        enabledCheckBox = new JCheckBox( rb.getString( "plugin.vst.config.default.enabled" ) );
        enabledCheckBox.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                updateEnabledState();
            }
        } );
        enabledPanel.add( enabledCheckBox );
        JPanel panel0 = new JPanel( new BorderLayout() );
        panel0.add( enabledPanel, BorderLayout.NORTH );
        panel0.add( pathPanel, BorderLayout.SOUTH );
        panel0.setBorder( new TitledBorder( rb.getString( "plugin.vst.config.default.pathBorder" ) ) );
        tab.add( panel0, BorderLayout.NORTH );
        return tab;
    }
    private void updateEnabledState() {
        boolean b = enabledCheckBox.isSelected();
        pathsTextField.setEnabled( b );
        selectPathButton.setEnabled( b );
    }
    
    private JPanel createAdvancedTab() {
        final ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        JPanel tab = new JPanel( new BorderLayout() );
        JPanel pathPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        tab.setBorder( new TitledBorder( rb.getString( "plugin.vst.config.advanced.nativeLibPath" ) ) );
        nativeLibPathTextField = new JTextField( 40 );
        selectNativeLibPathButton = new JButton( rb.getString( "plugin.vst.config.select" ) );
        selectNativeLibPathButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                String paths = nativeLibPathTextField.getText();
                paths = DirectoryTreePanel.showDirectoryTreeDialog(
                        UiToolkit.getMainFrame(),
                        "Select Path", "System", paths, rb.getString( "ok" ), rb.getString( "cancel" ) );
                if (paths != null) {
                    nativeLibPathTextField.setText( paths );
                }
            }
        } );
        nativeLibPathSelectionComboBox = new JComboBox(
                new Object[] {
                        rb.getString( "plugin.vst.config.advanced.nativeLibPath.default" ),
                        rb.getString( "plugin.vst.config.advanced.nativeLibPath.userDefined" ) } );
        nativeLibPathSelectionComboBox.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                updateNativeLibPathEnabledState();
            }
        } );
        pathPanel.add( nativeLibPathTextField );
        //pathPanel.add( selectNativeLibPathButton );
        JPanel northPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        northPanel.add( nativeLibPathSelectionComboBox );
        tab.add( northPanel, BorderLayout.NORTH );
        tab.add( pathPanel );
        updateNativeLibPathEnabledState();
        return tab;
    }
    
    private void updateNativeLibPathEnabledState() {
        boolean b = nativeLibPathSelectionComboBox.getSelectedIndex() != 0;
        nativeLibPathTextField.setEnabled( b );
        selectNativeLibPathButton.setEnabled( b );
    }
    
    public String getTitle() {
        return SgEngine.getInstance().getResourceBundle().getString(
                "plugin.vst.config.title" );
    }

    public Object getUiObject() {
        return this;
    }

    public Plugin getPlugin() {
        return parent;
    }

    public void open() {
    }

    public void ok() {
        SgProperties p = SgEngine.getInstance().getProperties();
        p.setPluginProperty( parent, VstInitializerPlugin.VST_ENABLED, enabledCheckBox.isSelected() );
        p.setPluginProperty( parent, VstInitializerPlugin.VST_PATHS_PROPERTY, pathsTextField.getText() );
        p.setPluginProperty(
                parent,
                VstInitializerPlugin.USER_DEFINED_LIB_PATH_ENABLED,
                (nativeLibPathSelectionComboBox.getSelectedIndex() > 0) );
        p.setPluginProperty( parent, VstInitializerPlugin.USER_DEFINED_LIB_PATH, nativeLibPathTextField.getText() );
    }

    public void cancel() {
        SgProperties p = SgEngine.getInstance().getProperties();
        enabledCheckBox.setSelected( p.getPluginProperty( parent, VstInitializerPlugin.VST_ENABLED, true ) );
        pathsTextField.setText( p.getPluginProperty( parent, VstInitializerPlugin.VST_PATHS_PROPERTY, "" ) );
        nativeLibPathSelectionComboBox.setSelectedIndex(
                p.getPluginProperty( parent, VstInitializerPlugin.USER_DEFINED_LIB_PATH_ENABLED, false ) ? 1 : 0 );
        nativeLibPathTextField.setText(
                p.getPluginProperty( parent, VstInitializerPlugin.USER_DEFINED_LIB_PATH, (String) null ) );
        updateEnabledState();
        updateNativeLibPathEnabledState();
    }

}
