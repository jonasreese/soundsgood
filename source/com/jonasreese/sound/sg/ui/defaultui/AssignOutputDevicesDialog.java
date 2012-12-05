/*
 * Created on 17.06.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.jonasreese.sound.sg.RecorderException;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.ui.swing.JrDialog;

/**
 * @author jonas.reese
 */
public class AssignOutputDevicesDialog extends JrDialog {
    
    private static final long serialVersionUID = 1;
    
    private MidiDescriptor midiDescriptor;
    
    public AssignOutputDevicesDialog(
            Frame parent, MidiDescriptor midiDescriptor ) {
        super( parent,
                SgEngine.getInstance().getResourceBundle().getString(
                "midi.track.assignOutputDevices.title" ),
                true );
        
        this.midiDescriptor = midiDescriptor;
        
        AssignDevicesPanel panel = new AssignDevicesPanel( midiDescriptor, null, false );
        getContentPane().add( panel );
        JButton closeButton = new JButton( SgEngine.getInstance().getResourceBundle().getString( "close" ) );
        closeButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                dispose();
                done();
            }
        });
        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                done();
            }
        } );
        JPanel closePanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        closePanel.add( closeButton );
        getContentPane().add( closePanel, BorderLayout.SOUTH );
        getRootPane().setDefaultButton( closeButton );
    }

    private void done() {
        if (midiDescriptor.getMidiRecorder().isLoopbackEnabled()) {
            try {
                midiDescriptor.getMidiRecorder().setLoopbackEnabled( false );
                midiDescriptor.getMidiRecorder().setLoopbackEnabled( true );
            } catch (RecorderException rex) {
                rex.printStackTrace();
            }
        }
        midiDescriptor.getMidiRecorder().persistPlaybackState();
    }
}
