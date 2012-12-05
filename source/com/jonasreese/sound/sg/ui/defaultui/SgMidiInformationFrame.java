/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 11.09.2003
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.jonasreese.sound.sg.SgEngine;

/**
 * <b>
 * </b>
 * @author jreese
 */
public class SgMidiInformationFrame extends JFrame
{
    private static final long serialVersionUID = 1;
    
    private ResourceBundle rb;
    
    public SgMidiInformationFrame( String title )
    {
        super( title );
        
        rb = SgEngine.getInstance().getResourceBundle();
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );

        MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();

        CustomTableModel model = new CustomTableModel();
        model.setColumnCount( 4 );
        model.setRowCount( info.length );
        JTable table = new JTable( model );
        table.setDragEnabled( true );

        table.getColumnModel().getColumn( 0 ).setWidth( 80 );
        table.getColumnModel().getColumn( 1 ).setWidth( 60 );
        table.getColumnModel().getColumn( 2 ).setWidth( 30 );
        table.getColumnModel().getColumn( 3 ).setWidth( 120 );

        table.setDefaultRenderer( Object.class, new DetailsCellRenderer() );
        table.setDefaultEditor( Object.class, null );

        for (int i = 0; i < info.length; i++)
        {
            table.setValueAt( info[i].getName(), i, 0 );
            table.setValueAt( info[i].getVendor(), i, 1 );
            table.setValueAt( info[i].getVersion(), i, 2 );
            table.setValueAt( info[i].getDescription(), i, 3 );
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
