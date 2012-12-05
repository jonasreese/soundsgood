/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 08.12.2003
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.EventDescriptor;
import com.jonasreese.sound.sg.midi.EventMap;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.midi.NoteDescriptor;
import com.jonasreese.sound.sg.midi.TrackProxy;

/**
 * <p>
 * A panel that allows the user to edit a MIDI note (ON and OFF).
 * </p>
 * @author jreese
 */
public class NoteEditPanel extends JPanel
{
    
    private static final long serialVersionUID = 1;
    
    private JSpinner tickSpinner;
    private JSpinner lengthSpinner;
    private JComboBox data1ComboBox;
    private JTextField data2TextField;
    private JSlider data2Slider;
    
    private JLabel lengthLabel;
    private JLabel channelLabel;
    private JLabel data2Label;
    
    private JComboBox channelComboBox;
    
    private JCheckBox setNoteLengthCheckBox;
    private JCheckBox setChannelCheckBox;
    private JCheckBox setData2CheckBox;

    private MidiEvent[] events;
    private EventMap eventMap;
    
    /**
     * Constructs a new <code>MidiEditPanel</code>.
     * @param track The MIDI event track.
     * @param events The <code>MidiEvent</code>s.
     * @param title The title.
     * @param eventMap The event map to be used for displaying the MIDI notes. Can be <code>null</code>.
     */
    public NoteEditPanel( TrackProxy track, MidiEvent[] events, String title, EventMap eventMap )
    {
        super( new GridLayout( 5, 1 ) );

        this.events = events;
        this.eventMap = eventMap;
        MidiEvent on = events[0];
        MidiEvent off = events[1];
        boolean multipleNotes = (events.length > 2);

        JPanel tickPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 0 ) );
        JLabel tickLabel = new JLabel(
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.event.edit.tick" ) );
        tickSpinner = new JSpinner(
            new SpinnerNumberModel(
                new Long( on.getTick() ),
                new Long( 0 ),
                new Long( 0xFFFFFFFFl ), // 32-bit value
                new Long( 1 ) ) );
        tickLabel.setLabelFor( tickSpinner );
        tickPanel.add( tickLabel );
        tickPanel.add( tickSpinner );
        if (multipleNotes) {
            JCheckBox cb = new JCheckBox();
            cb.setEnabled( false );
            tickPanel.add( cb );
            tickSpinner.setEnabled( false );
            tickLabel.setEnabled( false );
        }
        add( tickPanel );

        JPanel lengthPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 0 ) );
        lengthLabel = new JLabel(
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.note.edit.length" ) );
        lengthSpinner = new JSpinner( new SpinnerNumberModel(
                new Long( off.getTick() - on.getTick() ),
                new Long( 0 ),
                new Long( 0xFFFFFFFFl - on.getTick() ), // 7-bit value
                new Long( 1 ) ) );
        lengthLabel.setLabelFor( lengthSpinner );
        lengthPanel.add( lengthLabel );
        lengthPanel.add( lengthSpinner );
        
        if (multipleNotes) {
            setNoteLengthCheckBox = new JCheckBox();
            lengthPanel.add( setNoteLengthCheckBox );
            setNoteLengthCheckBox.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    lengthLabel.setEnabled( setNoteLengthCheckBox.isSelected() );
                    lengthSpinner.setEnabled( setNoteLengthCheckBox.isSelected() );
                }
            } );
            lengthLabel.setEnabled( false );
            lengthSpinner.setEnabled( false );
        }
        
        add( lengthPanel );
        
        JPanel channelPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 0 ) );
        channelLabel = new JLabel(
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.note.edit.channel" ) );
        channelComboBox = new JComboBox( MidiToolkit.getDefaultMidiChannelNames( true ) );
        channelComboBox.setSelectedIndex( ((ShortMessage) on.getMessage()).getChannel() );
        if (((ShortMessage) on.getMessage()).getChannel() !=
            ((ShortMessage) off.getMessage()).getChannel())
        {
            channelComboBox.setEnabled( false );
        }
        channelLabel.setLabelFor( channelComboBox );
        channelPanel.add( channelLabel );
        channelPanel.add( channelComboBox );
        if (multipleNotes) {
            setChannelCheckBox = new JCheckBox();
            channelPanel.add( setChannelCheckBox );
            setChannelCheckBox.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    channelLabel.setEnabled( setChannelCheckBox.isSelected() );
                    channelComboBox.setEnabled( setChannelCheckBox.isSelected() );
                }
            } );
            channelLabel.setEnabled( false );
            channelComboBox.setEnabled( false );
        }
        add( channelPanel );

        JPanel data1Panel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 0 ) );
        JLabel data1Label = new JLabel(
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.note.edit.data1" ) );
        NoteComboBoxModel model = new NoteComboBoxModel();
        data1ComboBox = new JComboBox( model );
        data1ComboBox.setSelectedIndex( model.selIndex );
        
        data1Label.setLabelFor( data1ComboBox );
        data1Panel.add( data1Label );
        data1Panel.add( data1ComboBox );
        if (multipleNotes) {
            JCheckBox cb = new JCheckBox();
            cb.setEnabled( false );
            data1Panel.add( cb );
            data1Label.setEnabled( false );
            data1ComboBox.setEnabled( false );
        }
        add( data1Panel );

        JPanel data2Panel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 0 ) );
        data2Label = new JLabel(
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.note.edit.data2" ) );
        data2TextField = new JTextField(
                Integer.toString( ((ShortMessage) on.getMessage()).getData2() ), 3 );
        data2TextField.getDocument().addDocumentListener( new DocumentListener() {
            public void changedUpdate( DocumentEvent e ) {
                try {
                    int value = Integer.parseInt( data2TextField.getText() );
                    if (data2Slider.getValue() != value) {
                        if (value <= data2Slider.getMaximum() && value >= data2Slider.getMinimum()) {
                            data2Slider.setValue( value );
                        } else {
                            SwingUtilities.invokeLater( new Runnable() {
                                public void run() {
                                    data2TextField.setText( Integer.toString( data2Slider.getValue() ) );
                                }
                            } );
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            public void insertUpdate( DocumentEvent e ) {
                changedUpdate( e );
            }
            public void removeUpdate( DocumentEvent e ) {
                changedUpdate( e );
            }
        });
        data2Label.setLabelFor( data2TextField );
        data2Slider = new JSlider( JSlider.HORIZONTAL, 0, 127, ((ShortMessage) on.getMessage()).getData2() );
        data2Slider.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                String value = Integer.toString( data2Slider.getValue() );
                if (!value.equals( data2TextField.getText() )) {
                    data2TextField.setText( value );
                }
            }
        } );
        data2Panel.add( data2Label );
        data2Panel.add( data2Slider );
        data2Panel.add( data2TextField );
        if (multipleNotes) {
            setData2CheckBox = new JCheckBox();
            data2Panel.add( setData2CheckBox );
            setData2CheckBox.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    data2Label.setEnabled( setData2CheckBox.isSelected() );
                    data2TextField.setEnabled( setData2CheckBox.isSelected() );
                    data2Slider.setEnabled( setData2CheckBox.isSelected() );
                }
            } );
            data2Label.setEnabled( false );
            data2TextField.setEnabled( false );
            data2Slider.setEnabled( false );
        }
        add( data2Panel );

        // align some components
        channelComboBox.setPreferredSize( tickSpinner.getPreferredSize() );
        data1ComboBox.setPreferredSize(
                new Dimension(
                        data1ComboBox.getPreferredSize().width,
                        tickSpinner.getPreferredSize().height ) );
        
        setBorder( new TitledBorder( title ) );
    }
    
    /**
     * Gets the <code>changed</code> flag.
     * @return <code>true</code> if the user has changed at least one MIDI event
     *         value, <code>false</code> if the event remained unchanged.
     */
    public boolean hasChanged()
    {
        if (events.length == 2) {
            MidiEvent on = events[0];
            MidiEvent off = events[1];
            return (getTick() != on.getTick() ||
                    getLength() != off.getTick() - on.getTick() ||
                    getChannel() != ((ShortMessage) on.getMessage()).getChannel() ||
                    getData1() != ((ShortMessage) on.getMessage()).getData1() ||
                    getData2() != ((ShortMessage) on.getMessage()).getData2());
        } else {
            return (setNoteLengthCheckBox.isSelected() ||
                    setChannelCheckBox.isSelected() ||
                    setData2CheckBox.isSelected());

        }
    }
    
    /**
     * Applies the changes to the on and off events passed to the constructor.
     * Note that this method might have side effects, since it uses the event references
     * to change their state.
     * @param changeObj The object responsible for the change.
     */
    public void applyChanges( Object changeObj ) throws InvalidMidiDataException
    {
        for (int i = 0; i < events.length; i += 2) {
            MidiEvent on = events[i];
            MidiEvent off = events[i + 1];

            if (events.length <= 2) {
                on.setTick( getTick() );
                off.setTick( getTick() + getLength() );
            } else {
                if (setNoteLengthCheckBox.isSelected()) {
                    off.setTick( on.getTick() + getLength() );
                }
            }
            ShortMessage onMsg = (ShortMessage) on.getMessage();
            ShortMessage offMsg = (ShortMessage) off.getMessage();
            if (events.length <= 2) {
                onMsg.setMessage( onMsg.getCommand(), getChannel(), getData1(), getData2() );
                offMsg.setMessage( offMsg.getCommand(), getChannel(), getData1(), offMsg.getData2() );
            } else {
                onMsg.setMessage(
                        onMsg.getCommand(),
                        (setChannelCheckBox.isSelected() ? getChannel() : onMsg.getChannel()),
                        onMsg.getData1(),
                        (setData2CheckBox.isSelected() ? getData2() : onMsg.getData2() ) );
                offMsg.setMessage(
                        offMsg.getCommand(),
                        (setChannelCheckBox.isSelected() ? getChannel() : offMsg.getChannel()),
                        offMsg.getData1(),
                        offMsg.getData2() );
            }
        }
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
     * Gets the <code>length</code> value entered (or changed) by the user.
     * @return The length value.
     */
    public long getLength()
    {
        return ((Long) lengthSpinner.getValue()).longValue();
    }
    /**
     * Gets the <code>channel</code> value entered (or changed) by the user.
     * @return The channel value.
     */
    public int getChannel()
    {
        return channelComboBox.getSelectedIndex();
    }
    /**
     * Gets the <code>data1</code> value entered (or changed) by the user.
     * @return The data1 value.
     */
    public int getData1()
    {
        return ((NoteDescriptor) data1ComboBox.getSelectedItem()).getNote();
    }
    /**
     * Gets the <code>data1</code> value entered (or changed) by the user.
     * @return The data1 value.
     */
    public int getData2()
    {
        return data2Slider.getValue();
    }
    
    class NoteComboBoxModel extends DefaultComboBoxModel {
        private static final long serialVersionUID = 1;
        
        ArrayList<EventDescriptor> l;
        int selIndex = -1;
        NoteComboBoxModel() {
            l = new ArrayList<EventDescriptor>();
            int size = NoteEditPanel.this.eventMap != null ? NoteEditPanel.this.eventMap.getSize() : 128;
            for (int i = 0; i < size; i++) {
                EventDescriptor d = null;
                if (NoteEditPanel.this.eventMap != null) {
                    d = NoteEditPanel.this.eventMap.getEventAt( i );
                }
                if (d instanceof NoteDescriptor) {
                    if (((NoteDescriptor) d).getNote() == ((ShortMessage) events[0].getMessage()).getData1()) {
                        selIndex = l.size();
                    }
                    l.add( d );
                } else if (d == null) {
                    if (i == ((ShortMessage) events[0].getMessage()).getData1()) {
                        selIndex = l.size();
                    }
                    l.add( new NoteDescriptor( i, null, Integer.toString( i ) ) );
                }
            }
        }
        public int getSize() {
            return l.size();
        }

        public Object getElementAt( int index ) {
            return l.get( index );
        }
    }
}
