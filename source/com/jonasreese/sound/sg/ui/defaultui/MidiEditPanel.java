/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 08.12.2003
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.midi.TrackProxy;

/**
 * <p>
 * </p>
 * @author jreese
 */
public class MidiEditPanel extends JPanel {
    
    private static final long serialVersionUID = 1;
    
    private static final Object[] SHORT_MESSAGE_COMMAND_DATA =
    {
        new CommandWrapper( ShortMessage.ACTIVE_SENSING, "ACTIVE_SENSING" ),
        new CommandWrapper( ShortMessage.CHANNEL_PRESSURE, "CHANNEL_PRESSURE" ),
        new CommandWrapper( ShortMessage.CONTINUE, "CONTINUE" ),
        new CommandWrapper( ShortMessage.CONTROL_CHANGE, "CONTROL_CHANGE" ),
        new CommandWrapper( ShortMessage.END_OF_EXCLUSIVE, "END_OF_EXCLUSIVE" ),
        new CommandWrapper( ShortMessage.MIDI_TIME_CODE, "MIDI_TIME_CODE" ),
        //new CommandWrapper( ShortMessage.NOTE_OFF, "NOTE_OFF" ),
        //new CommandWrapper( ShortMessage.NOTE_ON, "NOTE_ON" ),
        new CommandWrapper( ShortMessage.PITCH_BEND, "PITCH_BEND" ),
        new CommandWrapper( ShortMessage.POLY_PRESSURE, "POLY_PRESSURE" ),
        new CommandWrapper( ShortMessage.PROGRAM_CHANGE, "PROGRAM_CHANGE" ),
        new CommandWrapper( ShortMessage.SONG_POSITION_POINTER, "SONG_POSITION_POINTER" ),
        new CommandWrapper( ShortMessage.SONG_SELECT, "SONG_SELECT" ),
        new CommandWrapper( ShortMessage.START, "START" ),
        new CommandWrapper( ShortMessage.STOP, "STOP" ),
        new CommandWrapper( ShortMessage.SYSTEM_RESET, "SYSTEM_RESET" ),
        new CommandWrapper( ShortMessage.TIMING_CLOCK, "TIMING_CLOCK" ),
        new CommandWrapper( ShortMessage.TUNE_REQUEST, "TUNE_REQUEST" ),
    };
    
    private JSpinner tickSpinner;
    private JComboBox commandComboBox;
    private JSpinner channelSpinner;
    private JSpinner data1Spinner;
    private JSpinner data2Spinner;
    private MidiEvent event;
    private TrackProxy track;
    private JLabel data1Label;
    private JLabel data2Label;
    private JLabel channelLabel;
    
    /**
     * Constructs a new <code>MidiEditPanel</code>.
     * @param track The MIDI event track.
     * @param event The <code>MidiEvent</code>.
     */
    public MidiEditPanel( TrackProxy track, MidiEvent event, String title )
    {
        super( new GridLayout( 5, 1 ) );

        this.track = track;
        this.event = event;

        JPanel tickPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 0 ) );
        JLabel tickLabel = new JLabel(
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.event.edit.tick" ) );
        tickSpinner = new JSpinner(
            new SpinnerNumberModel(
                new Long( event.getTick() ),
                new Long( 0 ),
                new Long( 0xFFFFFFFFl ), // 32-bit value
                new Long( 1 ) ) );
        tickLabel.setLabelFor( tickSpinner );
        tickPanel.add( tickLabel );
        tickPanel.add( tickSpinner );
        add( tickPanel );
        
        JPanel commandPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 0 ) );
        commandComboBox = new JComboBox( SHORT_MESSAGE_COMMAND_DATA );
        int command;
        ShortMessage sm = (ShortMessage) event.getMessage();
        if (MidiToolkit.isChannelMessageStatusByte( sm.getStatus() )) {
            command = sm.getCommand();
        } else {
            command = sm.getStatus();
        }
        int selIndex = 0;
        for (int i = 0; i < SHORT_MESSAGE_COMMAND_DATA.length; i++)
        {
            if (((CommandWrapper) SHORT_MESSAGE_COMMAND_DATA[i]).command == command)
            {
                selIndex = i;
                break;
            } 
        }
        commandComboBox.setSelectedIndex( selIndex );
        JLabel commandLabel = new JLabel(
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.event.edit.command" ) );
        commandLabel.setLabelFor( commandComboBox );
        commandPanel.add( commandLabel );
        commandPanel.add( commandComboBox );
        add( commandPanel );

        JPanel channelPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 0 ) );
        channelLabel = new JLabel(
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.event.edit.channel" ) );
        channelSpinner = new JSpinner( new SpinnerNumberModel(
                new Integer( ((ShortMessage) event.getMessage()).getChannel() ),
                new Integer( 0 ),
                new Integer( 15 ), // 4-bit value
                new Integer( 1 ) ) );
        channelSpinner.setPreferredSize( commandComboBox.getPreferredSize() );
        channelLabel.setLabelFor( channelSpinner );
        channelPanel.add( channelLabel );
        channelPanel.add( channelSpinner );
        add( channelPanel );

        JPanel data1Panel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 0 ) );
        data1Label = new JLabel(
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.event.edit.data1" ) );
        data1Spinner = new JSpinner( new SpinnerNumberModel(
                new Integer( ((ShortMessage) event.getMessage()).getData1() ),
                new Integer( 0 ),
                new Integer( 127 ), // 7-bit value
                new Integer( 1 ) ) );
        data1Label.setLabelFor( data1Spinner );
        data1Panel.add( data1Label );
        data1Panel.add( data1Spinner );
        add( data1Panel );

        JPanel data2Panel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 0 ) );
        data2Label = new JLabel(
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.event.edit.data2" ) );
        data2Spinner = new JSpinner( new SpinnerNumberModel(
                new Integer( ((ShortMessage) event.getMessage()).getData2() ),
                new Integer( 0 ),
                new Integer( 127 ), // 7-bit value
                new Integer( 1 ) ) );
        data2Label.setLabelFor( data2Spinner );
        data2Panel.add( data2Label );
        data2Panel.add( data2Spinner );
        add( data2Panel );
        
        // align some components
        tickSpinner.setPreferredSize( commandComboBox.getPreferredSize() );
        channelSpinner.setPreferredSize( data1Spinner.getPreferredSize() );
        
        setBorder( new TitledBorder( title ) );

        commandComboBox.getModel().addListDataListener( new ListDataListener()
        {
            public void contentsChanged(ListDataEvent arg0)
            {
                updateEnabledState();
            }
            public void intervalAdded(ListDataEvent arg0)
            {
                updateEnabledState();
            }
            public void intervalRemoved(ListDataEvent arg0)
            {
                updateEnabledState();
            }
        } );
        updateEnabledState();
    }

    private void updateEnabledState() {
        int c = getStatus();
        int l = getLengthForCommand( c );
        data1Spinner.setEnabled( (l >= 2) );
        data1Label.setEnabled( (l >= 2) );
        data2Spinner.setEnabled( (l >= 3) );
        data2Label.setEnabled( (l >= 3) );
        boolean b = MidiToolkit.isChannelMessageStatusByte( c );
        channelSpinner.setEnabled( b );
        channelLabel.setEnabled( b );
    }

    private int getLengthForCommand( int command )
    {
        ShortMessage sm = new ShortMessage();
        try
        {
            sm.setMessage( command, 0, 0, 0 );
            return sm.getLength();
        }
        catch (InvalidMidiDataException ignored)
        {
        }
        return -1;
    }

    /**
     * Gets the <code>changed</code> flag.
     * @return <code>true</code> if the user has changed at least one MIDI event
     *         value, <code>false</code> if the event remained unchanged.
     */
    public boolean hasChanged()
    {
        return (getTick() != event.getTick() ||
                getStatus() != ((ShortMessage) event.getMessage()).getCommand() ||
                getChannel() != ((ShortMessage) event.getMessage()).getChannel() ||
                getData1() != ((ShortMessage) event.getMessage()).getData1() ||
                getData2() != ((ShortMessage) event.getMessage()).getData2());
    }
    
    /**
     * Gets the <code>MIDI status</code> value entered (or changed) by the user.
     * @return The status value.
     */
    public int getStatus()
    {
        return ((CommandWrapper) commandComboBox.getSelectedItem()).command;
    }
    
    /**
     * Gets the <code>tick</code> value entered (or changed) by the user.
     * @return The tick value.
     */
    public long getTick()
    {
        return ((Long) tickSpinner.getValue()).longValue();
    }
    /**
     * Gets the <code>channel</code> value entered (or changed) by the user.
     * @return The channel value.
     */
    public int getChannel()
    {
        return ((Integer) channelSpinner.getValue()).intValue();
    }
    /**
     * Gets the <code>data1</code> value entered (or changed) by the user.
     * @return The data1 value.
     */
    public int getData1()
    {
        return ((Integer) data1Spinner.getValue()).intValue();
    }
    /**
     * Gets the <code>data1</code> value entered (or changed) by the user.
     * @return The data1 value.
     */
    public int getData2()
    {
        return ((Integer) data2Spinner.getValue()).intValue();
    }

    /**
     * Applies the changes to the event passed to the constructor.
     * Note that this method might have side effects, since it uses the event reference
     * to change its state.
     * @param changeObj The object responsible for the change.
     */
    public void applyChanges( Object changeObj ) throws InvalidMidiDataException
    {
        track.remove( event, changeObj );
        event.setTick( getTick() );
        ShortMessage msg = (ShortMessage) event.getMessage();
        int status = getStatus();
        if (MidiToolkit.isChannelMessageStatusByte( status )) {
            msg.setMessage( status, getChannel(), getData1(), getData2() );
        } else {
            msg.setMessage( status, getData1(), getData2() );
        }
        track.add( event, changeObj );
    }
    

    
    
    static class CommandWrapper
    {
        int command;
        String description;
        CommandWrapper( int command, String description )
        {
            this.command = command;
            this.description = description;
        }
        public String toString() { return description; }
    }
}
