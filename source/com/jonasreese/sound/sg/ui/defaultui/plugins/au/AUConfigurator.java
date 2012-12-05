/*
 * Created on 22.10.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.au;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
public class AUConfigurator extends JTabbedPane implements PluginConfigurator {
    private static final long serialVersionUID = 1L;
    
    private Plugin parent;
    
    private JTextField nativeLibPathTextField;
    private JButton selectNativeLibPathButton;
    private JComboBox nativeLibPathSelectionComboBox;
    private JCheckBox enabledCheckBox;
    
    
    public AUConfigurator( Plugin parent ) {
        this.parent = parent;
        ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        addTab( rb.getString( "plugin.au.config.default" ), createDefaultTab() );
        addTab( rb.getString( "plugin.au.config.advanced" ), createAdvancedTab() );
        cancel(); // initialize with defaults
    }
    
    private JPanel createDefaultTab() {
        final ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        JPanel tab = new JPanel( new BorderLayout() );
        JPanel enabledPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        enabledCheckBox = new JCheckBox( rb.getString( "plugin.au.config.default.enabled" ) );
        enabledPanel.add( enabledCheckBox );
        JPanel panel0 = new JPanel( new BorderLayout() );
        panel0.add( enabledPanel, BorderLayout.NORTH );
        panel0.setBorder( new TitledBorder( rb.getString( "plugin.au.config.default.border" ) ) );
        tab.add( panel0, BorderLayout.NORTH );
        return tab;
    }
    
    private JPanel createAdvancedTab() {
        final ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        JPanel tab = new JPanel( new BorderLayout() );
        JPanel pathPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        tab.setBorder( new TitledBorder( rb.getString( "plugin.au.config.advanced.nativeLibPath" ) ) );
        nativeLibPathTextField = new JTextField( 40 );
        selectNativeLibPathButton = new JButton( rb.getString( "plugin.au.config.select" ) );
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
                        rb.getString( "plugin.au.config.advanced.nativeLibPath.default" ),
                        rb.getString( "plugin.au.config.advanced.nativeLibPath.userDefined" ) } );
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
                "plugin.au.config.title" );
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
        p.setPluginProperty( parent, AUInitializerPlugin.AU_ENABLED, enabledCheckBox.isSelected() );
        p.setPluginProperty(
                parent,
                AUInitializerPlugin.USER_DEFINED_LIB_PATH_ENABLED,
                (nativeLibPathSelectionComboBox.getSelectedIndex() > 0) );
        p.setPluginProperty( parent, AUInitializerPlugin.USER_DEFINED_LIB_PATH, nativeLibPathTextField.getText() );
    }

    public void cancel() {
        SgProperties p = SgEngine.getInstance().getProperties();
        enabledCheckBox.setSelected( p.getPluginProperty( parent, AUInitializerPlugin.AU_ENABLED, true ) );
        nativeLibPathSelectionComboBox.setSelectedIndex(
                p.getPluginProperty( parent, AUInitializerPlugin.USER_DEFINED_LIB_PATH_ENABLED, false ) ? 1 : 0 );
        nativeLibPathTextField.setText(
                p.getPluginProperty( parent, AUInitializerPlugin.USER_DEFINED_LIB_PATH, (String) null ) );
        updateNativeLibPathEnabledState();
    }

}
