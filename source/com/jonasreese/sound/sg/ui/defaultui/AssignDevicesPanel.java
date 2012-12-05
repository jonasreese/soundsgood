/*
 * Created on 14.05.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiChannelMap;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.MidiDeviceDescriptor;
import com.jonasreese.sound.sg.midi.MidiDeviceList;
import com.jonasreese.sound.sg.midi.MidiDeviceMap;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.midi.TrackProxy;
import com.jonasreese.ui.swing.SubListSelectionPanel;

/**
 * <p>
 * This class implements a UI that allows the user to assign MIDI in- or output
 * devices to MIDI channel numbers for a given <code>TrackProxy</code>.
 * </p>
 * @author jonas.reese
 */
public class AssignDevicesPanel extends JPanel {

    private static final long serialVersionUID = 1;
    
    private ResourceBundle rb;
    private JList deviceList;
    private SubListSelectionPanel channelList;
    private ArrayList<ChannelWrapper> channels;
    private ArrayList<?> empty;
    private MidiDescriptor midiDescriptor;
    private TrackProxy track;
    private boolean input;
    
    /**
     * Creates a new <code>AssignDevicesPanel</code> object.
     * @param midiDescriptor The <code>MidiDescriptor</code> containing the
     * <code>SgSequence</code> that contains the given <code>TrackProxy</code>.
     * @param track The MIDI track to assign in- or output devices to.
     * @param input If set to <code>true</code>, input devices are assigned,
     * otherwise output devices are assigned.
     */
    public AssignDevicesPanel(
            MidiDescriptor midiDescriptor, TrackProxy track, boolean input ) {
        super( new BorderLayout() );
        this.midiDescriptor = midiDescriptor;
        this.track = track;
        this.input = input;
        rb = SgEngine.getInstance().getResourceBundle();
        //add( new JLabel( rb.getString( "midi.track.name" ) + ": " + track.getTrackName() ), BorderLayout.NORTH );
        MidiDeviceList list =
            (input ?
                    SgEngine.getInstance().getProperties().getMidiInputDeviceList() :
                    SgEngine.getInstance().getProperties().getMidiOutputDeviceList());
        deviceList = new JList( list.getDeviceDescriptors() );
        deviceList.setCellRenderer( new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1;
            public Component getListCellRendererComponent(
                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
                MidiDeviceDescriptor desc = (MidiDeviceDescriptor) value;
                MidiDeviceMap devMap = (AssignDevicesPanel.this.input ?
                        AssignDevicesPanel.this.midiDescriptor.getMidiRecorder().getMidiInputMap( AssignDevicesPanel.this.track ) :
                        AssignDevicesPanel.this.midiDescriptor.getMidiRecorder().getMidiOutputMap());
                MidiChannelMap cm = devMap.getChannelMapFor( desc );
                String notEmpty = "";
                if (cm != null && !cm.isEmpty()) {
                    notEmpty = " (*)";
                }
                String s = "[" + (index + 1) + "] " + value + notEmpty;
                return super.getListCellRendererComponent( list, s, index, isSelected, cellHasFocus );
            }
        });
        
        JPanel trackPanel = null;
        JComboBox trackComboBox = null;
        if (input) {
            trackPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
            String s = rb.getString( "track" );
            trackPanel.setBorder( new TitledBorder( s ) );
            try {
                trackComboBox = new JComboBox( midiDescriptor.getSequence().getTrackProxies() );
                if (track == null) {
                    trackComboBox.insertItemAt( rb.getString( "midi.track.assignInputDevices.selectTrack" ), 0 );
                }
                Dimension d = new Dimension( trackComboBox.getPreferredSize() );
                d.width = 200;
                trackComboBox.setPreferredSize( d );
                JLabel trackLabel = new JLabel( s + ": " );
                trackLabel.setLabelFor( trackComboBox );
                trackPanel.add( trackLabel );
                trackPanel.add( trackComboBox );
                if (track != null) {
                    trackComboBox.setSelectedItem( track );
                } else {
                    trackComboBox.setSelectedIndex( 0 );
                }
                trackComboBox.addItemListener( new ItemListener() {
                    public void itemStateChanged( ItemEvent e )  {
                        if (((JComboBox) e.getSource()).getItemAt( 0 ) instanceof String) {
                            ((JComboBox) e.getSource()).removeItemAt( 0 );
                        }
                        if (e.getItem() instanceof TrackProxy) {
                            AssignDevicesPanel.this.track = (TrackProxy) e.getItem();
                            channelList.getLeftList().setEnabled( true );
                            channelList.getRightList().setEnabled( true );
                            deviceList.setEnabled( true );
                        } else {
                            AssignDevicesPanel.this.track = null;
                        }
                        updateSelectedDevice();
                        updateChannels();
                        deviceList.repaint();
                    }
                });
            } catch (InvalidMidiDataException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        
        JPanel innerPanel = new JPanel( new BorderLayout() );
        
        JPanel devicePanel = new JPanel( new BorderLayout() );
        devicePanel.add( new JScrollPane( deviceList ) );
        devicePanel.setBorder( new TitledBorder( rb.getString(
                (input ? "midi.track.assignInputDevices.devices" : "midi.track.assignOutputDevices.devices" ) ) ) );
        
        innerPanel.add( devicePanel, BorderLayout.NORTH );
        deviceList.addListSelectionListener( new ListSelectionListener() {
            public void valueChanged( ListSelectionEvent e ) {
                updateSelectedDevice();
            }
        } );
        
        String[] channelNames = MidiToolkit.getDefaultMidiChannelNames( false );
        channels = new ArrayList<ChannelWrapper>( channelNames.length );
        for (int i = 0; i < channelNames.length; i++) {
            channels.add( new ChannelWrapper( channelNames[i], i ) );
        }
        Action toLeftAction = new AbstractAction( rb.getString( "midi.track.assignDevices.removeChannel" ) ) {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e ) {
                updateChannels();
            }
        };
        Action toRightAction = new AbstractAction( rb.getString( "midi.track.assignDevices.addChannel" ) ) {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e ) {
                updateChannels();
            }
        };
        Action allToLeftAction = new AbstractAction( rb.getString( "midi.track.assignDevices.removeAllChannels" ) ) {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e ) {
                updateChannels();
            }
        };
        Action allToRightAction = new AbstractAction( rb.getString( "midi.track.assignDevices.addAllChannels" ) ) {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e ) {
                updateChannels();
            }
        };
        channelList = new SubListSelectionPanel( new Object[0], new Object[0], toLeftAction, toRightAction );
        channelList.setAllToLeftAction( allToLeftAction );
        channelList.setAllToRightAction( allToRightAction );
        channelList.getSplitPane().setBorder( null );
        channelList.setKeepLeftOrder( true );
        channelList.getLeftListContainerPanel().setBorder(
                new TitledBorder( rb.getString( "midi.track.assignDevices.availableChannels" ) ) );
        channelList.getRightListContainerPanel().setBorder(
                new TitledBorder( rb.getString( "midi.track.assignDevices.activeChannels" ) ) );
        innerPanel.add( channelList );
        empty = new ArrayList<Object>( 1 );

        if (trackPanel != null) {
            add( trackPanel, BorderLayout.NORTH );
        }
        add( innerPanel );

        if (track == null && input) {
            channelList.getLeftList().setEnabled( false );
            channelList.getRightList().setEnabled( false );
            deviceList.setEnabled( false );
        }
    }
    
    private void updateChannels() {
        MidiDeviceDescriptor descriptor = (MidiDeviceDescriptor) deviceList.getSelectedValue();
        if (descriptor == null) { return; }
        MidiDeviceMap deviceMap =
            (input ?
                    midiDescriptor.getMidiRecorder().getMidiInputMap( track ) :
                    midiDescriptor.getMidiRecorder().getMidiOutputMap() );
        MidiChannelMap channelMap = deviceMap.addMidiDevice( descriptor );
        //System.out.println( "channelMap = " + channelMap + " (" + devInfo + ")" );
        channelMap.removeAllChannels();
        ListModel lm = channelList.getRightList().getModel();
        for (int i = 0; i < lm.getSize(); i++) {
            ChannelWrapper cw = (ChannelWrapper) lm.getElementAt( i );
            channelMap.addChannel( cw.channelNumber );
            //System.out.println( "channelMap.addChannel( " + cw.channelNumber + " );" );
        }
        //System.out.println( "channelmap.getChannels() = " + channelMap.getChannels().length );
        deviceList.repaint();
    }
    
    private void updateSelectedDevice() {
        //System.out.println( "updateSelectedDevice()" );
        MidiDeviceDescriptor descriptor = (MidiDeviceDescriptor) deviceList.getSelectedValue();
        channelList.setRightObjects( empty );
        if (descriptor == null) {
            channelList.setLeftObjects( empty );
        } else {
            channelList.setLeftObjects( channels );
            MidiDeviceMap deviceMap =
                (input ?
                        midiDescriptor.getMidiRecorder().getMidiInputMap( track ) :
                        midiDescriptor.getMidiRecorder().getMidiOutputMap() );
            MidiChannelMap channelMap = deviceMap.addMidiDevice( descriptor );
            //System.out.println( "channelMap = " + channelMap + " (" + devInfo + ")" );
            int[] channelIndices = channelMap.getChannels();
            for (int i = 0; i < channelIndices.length; i++) {
                //System.out.println( "move from left to right: " + channels.get( channelIndices[i] ) );
                channelList.moveFromLeftToRight( channels.get( channelIndices[i] ) );
            }
        }
    }
    
    
    class ChannelWrapper {
        String name;
        int channelNumber;
        ChannelWrapper( String name, int channelNumber ) {
            this.name = name;
            this.channelNumber = channelNumber;
        }
        public String toString() {
            return name;
        }
    }
}