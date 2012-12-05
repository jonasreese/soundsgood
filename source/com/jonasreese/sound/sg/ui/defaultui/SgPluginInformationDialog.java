/*
 * Copyright (c) 2004 Jonas Reese
 * Created on 05.10.2004
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.ui.swing.JrDialog;

/**
 * <b>
 * </b>
 * @author jreese
 */
public class SgPluginInformationDialog extends JrDialog
{
    private static final long serialVersionUID = 1;
    
    
    private ResourceBundle rb;
    
    public SgPluginInformationDialog( JDialog parent, String title )
    {
        super( parent, title );
        
        rb = SgEngine.getInstance().getResourceBundle();
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );

        Plugin[] plugins = SgEngine.getInstance().getPlugins();

        CustomTableModel model = new CustomTableModel();
        model.setColumnCount( 4 );
        model.setRowCount( plugins.length );
        JTable table = new JTable( model );
        table.setDragEnabled( true );

        table.getColumnModel().getColumn( 0 ).setWidth( 80 );
        table.getColumnModel().getColumn( 1 ).setWidth( 60 );
        table.getColumnModel().getColumn( 2 ).setWidth( 30 );
        table.getColumnModel().getColumn( 3 ).setWidth( 120 );

        table.setDefaultRenderer( Object.class, new DetailsCellRenderer() );
        table.setDefaultEditor( Object.class, null );

        
        
        for (int i = 0; i < plugins.length; i++)
        {
            table.setValueAt( plugins[i].getPluginName(), i, 0 );
            table.setValueAt( plugins[i].getPluginVendor(), i, 1 );
            table.setValueAt( plugins[i].getPluginVersion(), i, 2 );
            table.setValueAt( plugins[i].getDescription(), i, 3 );
        }
        JScrollPane sp = new JScrollPane( table );
        getContentPane().add( sp );
        JPanel jp = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        JButton closeButton = new JButton( new AbstractAction( rb.getString( "close" ) )
        {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e )
            {
                dispose();
            }
        } );
        jp.add( closeButton );
        getContentPane().add( jp, BorderLayout.SOUTH );

        getRootPane().setDefaultButton( closeButton );
        Action cancelKeyAction = new AbstractAction() {
            private static final long serialVersionUID = 1;
            public void actionPerformed(ActionEvent e) {
                ((AbstractButton)e.getSource()).doClick();
            }
        }; 
        KeyStroke cancelKeyStroke = KeyStroke.getKeyStroke( (char) KeyEvent.VK_ESCAPE );
        InputMap inputMap = closeButton.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
        ActionMap actionMap = closeButton.getActionMap();
        if (inputMap != null && actionMap != null) {
            inputMap.put(cancelKeyStroke, "cancel");
            actionMap.put("cancel", cancelKeyAction);
        }
    }

    /**
     * The default renderer for DSM-CC files in the "details" view
     */
    private class DetailsCellRenderer extends DefaultTableCellRenderer
    {
        private static final long serialVersionUID = 1;
        public Component getTableCellRendererComponent( JTable table, 
                                                        Object value,
                                                        boolean isSelected,
                                                        boolean hasFocus,
                                                        int row,
                                                        int column)
        {
            // avoid that a focus is painted on an object that is not a file
            setHorizontalAlignment( LEFT );
            isSelected = false;
            hasFocus = false;
            setIcon( null );
            
            if (value instanceof JComponent)
            {
                return (JComponent) value;
            }
            return super.getTableCellRendererComponent( table, value, isSelected, false, row, column );
        }
    }
    
    private class CustomTableModel extends DefaultTableModel
    {
        private static final long serialVersionUID = 1;
        String[] columnNames =
        {
            rb.getString( "midiInfoFrame.name" ),
            rb.getString( "midiInfoFrame.vendor" ),
            rb.getString( "midiInfoFrame.version" ),
            rb.getString( "midiInfoFrame.description" )
        };
        
        public String getColumnName( int column )
        {
            return columnNames[column];
        }
    }
}
