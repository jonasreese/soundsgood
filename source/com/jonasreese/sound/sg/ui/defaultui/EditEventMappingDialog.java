/*
 * Created on 24.12.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListModel;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.EventDescriptor;
import com.jonasreese.sound.sg.midi.EventMap;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.midi.edit.ChangeEventMapEdit;
import com.jonasreese.ui.swing.JrDialog;

/**
 * @author jonas.reese
 */
public class EditEventMappingDialog extends JrDialog {
    
    private static final long serialVersionUID = 1;
    
    private EventMap eventMap;
    private EditEventMapPanel panel;
    private MidiDescriptor midiDescriptor;
    
    public EditEventMappingDialog(
            Frame parent, MidiDescriptor midiDescriptor, EventMap eventMap ) {
        super( parent,
                SgEngine.getInstance().getResourceBundle().getString(
                "midi.track.editEventMap.title" ),
                true );
        
        this.midiDescriptor = midiDescriptor;
        this.eventMap = eventMap;
        
        ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        panel = new EditEventMapPanel(
                eventMap,
                rb.getString( "midi.track.editEventMap.map" ),
                rb.getString( "midi.track.editEventMap.unmap" ),
                rb.getString( "midi.track.editEventMap.mapAll" ),
                rb.getString( "midi.track.editEventMap.mapNone" ) );
        getContentPane().add( panel );
        JButton okButton = new JButton(
                SgEngine.getInstance().getResourceBundle().getString( "ok" ) );
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                dispose();
                done();
            }
        });
        JButton cancelButton = new JButton(
                SgEngine.getInstance().getResourceBundle().getString( "cancel" ) );
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                dispose();
            }
        });
        cancelButton.setPreferredSize(
                new Dimension(
                        Math.max( okButton.getPreferredSize().width, cancelButton.getPreferredSize().width ),
                        cancelButton.getPreferredSize().height ) );
        okButton.setPreferredSize( cancelButton.getPreferredSize() );
        JPanel closePanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        closePanel.add( okButton );
        closePanel.add( cancelButton );
        getContentPane().add( closePanel, BorderLayout.SOUTH );
        getRootPane().setDefaultButton( okButton );
    }

    public void pack() {
        super.pack();
        setSize( (getWidth() * 3) / 2, getSize().height * 2 );
    }
    
    private void done() {
        ListModel lm = panel.getEventTypeList().getRightList().getModel();
        EventDescriptor[] eds = new EventDescriptor[lm.getSize()];
        for (int i = 0; i < lm.getSize(); i++) {
            EventDescriptor ed =
                ((EditEventMapPanel.EventDescWrapper) lm.getElementAt( i )).getEventDescriptor();
            eds[i] = MidiToolkit.copyEventDescriptor( ed, eventMap );
        }
        ChangeEventMapEdit edit = new ChangeEventMapEdit( eventMap, eds );
        edit.perform();
        midiDescriptor.getUndoManager().addEdit( edit );
    }
}
