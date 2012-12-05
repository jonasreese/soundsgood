/*
 * Created on 02.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.midi.EventDescriptor;
import com.jonasreese.sound.sg.midi.EventMap;
import com.jonasreese.sound.sg.midi.MidiFilter;
import com.jonasreese.sound.sg.midi.MidiFilterElement;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.midi.NoteDescriptor;
import com.jonasreese.sound.sg.midi.TrackProxy;
import com.jonasreese.sound.sg.soundbus.MidiFilterNode;
import com.jonasreese.sound.sg.soundbus.MidiInputMonitor;
import com.jonasreese.sound.sg.soundbus.SbMonitorableMidiInput;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.edit.SbNodeStateChangeEdit;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.ui.swing.JrDialog;
import com.jonasreese.ui.swing.SubListSelectionPanel;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author jonas.reese
 */
public class PMidiFilterNode extends PSbNode implements PropertyChangeListener, MidiInputMonitor {

    private static final long serialVersionUID = 1L;
    
    public static final String DISPLAY_INFO_PROPERTY = "displayInfo";
    
    private static String[] channelNames = MidiToolkit.getDefaultMidiChannelNames( false );
    
    private boolean info;
    private EventMap eventMap;
    private int count;
    
    public PMidiFilterNode(
            SbEditorComponent editor, MidiFilterNode node, SoundbusDescriptor soundbusDescriptor ) {
        super( editor, node, soundbusDescriptor );
        backgroundPaint = new Color( 200, 200, 0, 100 );
        info = false;
        eventMap = TrackProxy.createDefaultNoteEventMap();
        setInfo( Boolean.TRUE.toString().equalsIgnoreCase( getSbNode().getClientProperty( DISPLAY_INFO_PROPERTY ) ) );
    }
    
    public void messageReceived( MidiMessage m, SbOutput output ) {
    }

    public void messageProcessed( MidiMessage m, SbOutput output, Object result ) {
        if (!(m instanceof ShortMessage)) {
            return;
        }
        setTitleTextSecondLine( (ShortMessage) m, result == Boolean.TRUE );
    }
    
    public void setTitleTextSecondLine( ShortMessage m, boolean leftThrough ) {
        if (leftThrough) {
            titleTextSecondLinePaint = Color.BLACK;
        } else {
            titleTextSecondLinePaint = Color.RED;
        }
        if (MidiToolkit.isChannelMessageStatusByte( m.getStatus() )) {
            String text = null;
            if (m.getCommand() == ShortMessage.NOTE_ON || m.getCommand() == ShortMessage.NOTE_OFF) {
                EventDescriptor ed = eventMap.getEventDescriptorFor( m );
                if (ed != null) {
                    text = ed.getDescription();
                }
            }
            if (text == null) {
                String desc = EventMap.EVENT_NAME_MAP.get( m.getCommand() );
                text = desc + " " + m.getData1();
            }
            if (text != null) {
                setTitleTextSecondLine(
                        "(" + (++count) + ")\n" +
                        text +
                        "\n" + SgEngine.getInstance().getResourceBundle().getString(
                                "midi.channel.shortName", (m.getChannel() + 1) ) );
            }
        } else {
            int status = m.getStatus();
            if (status == ShortMessage.STOP || status == ShortMessage.CONTINUE) {
                status = ShortMessage.STOP;
            }
            if (status != ShortMessage.TIMING_CLOCK) {
                String desc = EventMap.EVENT_NAME_MAP.get( status );
                if (desc != null) {
                    setTitleTextSecondLine(
                            "(" + (++count) + ")\n" +
                            desc + " " + m.getData1() +
                            "\n" + SgEngine.getInstance().getResourceBundle().getString(
                                    "midi.channel.shortName", (m.getChannel() + 1) ) );
                }
            }
        }
    }
    
    private void setInfo( boolean info ) {
        if (!this.info && info) {
            ((SbMonitorableMidiInput) getSbNode().getInputs()[0]).addMidiInputMonitor( this );
        } else if (this.info && !info) {
            ((SbMonitorableMidiInput) getSbNode().getInputs()[0]).removeMidiInputMonitor( this );
            setTitleTextSecondLine( null );
        }
        this.info = info;
    }
    
    @Override
    public void nodeAdded() {
        getSbNode().addPropertyChangeListener( this );
    }
    
    @Override
    public void nodeRemoved() {
        getSbNode().removePropertyChangeListener( this );
    }
    
    public void propertyChange( PropertyChangeEvent evt ) {
        if (DISPLAY_INFO_PROPERTY.equals( evt.getPropertyName() )) {
            setInfo( Boolean.TRUE.toString().equalsIgnoreCase( getSbNode().getClientProperty( DISPLAY_INFO_PROPERTY ) ) );
        }
    }

    public void editNode() {
        MidiFilter filter = ((MidiFilterNode) getSbNode()).getMidiFilter();
        Frame m = UiToolkit.getMainFrame();
        if (filter == null) {
            int option = JOptionPane.showConfirmDialog(
                m, SgEngine.getInstance().getResourceBundle().getString( "plugin.sbEditor.node.filter.create.message" ),
                SgEngine.getInstance().getResourceBundle().getString( "plugin.sbEditor.node.filter.create" ),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE );
            if (option == JOptionPane.CANCEL_OPTION) {
                return;
            }
            if (option == JOptionPane.YES_OPTION) {
                filter = new MidiFilter( true, false, new MidiFilterElement[0] );
            } else {
                filter = new MidiFilter( false, false, new MidiFilterElement[0] );
            }
        }
        EditMidiFilterNodeDialog d = new EditMidiFilterNodeDialog( m, filter, info );
        d.pack();
        d.setLocation( m.getX() + m.getWidth() / 2 - d.getWidth() / 2, m.getY() + m.getHeight() / 2 - d.getHeight() / 2 );
        d.setVisible( true );
        boolean b = d.panel.displayInfoCheckBox.isSelected();
        if (b != info || (d.getFilter() != null && (filter == null || !(filter.equals( d.getFilter() ))))) {
            SgUndoableEdit edit = new ChangeMidiFilterEdit(
                    getSoundbusDescriptor(), (MidiFilterNode) getSbNode(), d.getFilter(), b );
            getSoundbusDescriptor().getUndoManager().addEdit( edit );
            edit.perform();
        }
    }
    
    protected void paintInOutNode( InOutNode n, PPaintContext paintContext ) {
        super.paintInOutNode( n, paintContext );
        PBounds b = n.getBoundsReference();
        paintContext.getGraphics().drawImage(
                n.isInputNode() ? PMidiOutputNode.MIDI_IN_ICON :
                    PMidiInputNode.MIDI_OUT_ICON, (int) b.getX(), (int) b.getY(), null );
    }
    
    
    static class EditMidiFilterNodeDialog extends JrDialog {
        
        private static final long serialVersionUID = 1;
        
        private EditMidiFilterPanel panel;
        
        public EditMidiFilterNodeDialog( Frame parent, MidiFilter filter, boolean info ) {
            super( parent,
                    SgEngine.getInstance().getResourceBundle().getString(
                    "midi.filter.editFilter.title" ),
                    true );
            
            ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
            panel = new EditMidiFilterPanel(
                    TrackProxy.createDefaultNoteEventMap(),
                    filter, info );
            getContentPane().add( panel );
            JButton okButton = new JButton( rb.getString( "ok" ) );
            okButton.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    dispose();
                    done();
                }
            });
            JButton cancelButton = new JButton( rb.getString( "cancel" ) );
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

        private void done() {
            panel.done();
            ListModel lm = panel.eventTypeComboBox.getModel();
            ArrayList<MidiFilterElement> list = new ArrayList<MidiFilterElement>();
            for (int i = 0; i < lm.getSize(); i++) {
                EventTypeWrapper etw = (EventTypeWrapper) lm.getElementAt( i );
                MidiFilterElement filterElement = etw.getMidiFilterElement();
                if (filterElement != null) {
                    list.add( filterElement );
                }
            }
            boolean optimistic = panel.filter == null ? true : panel.filter.isOptimistic();
            boolean blockNonShortMessageEvents = panel.blockNonShortMessageEventsRadioButton.isSelected();
            panel.filter = new MidiFilter( optimistic, blockNonShortMessageEvents, list );
        }

        public void pack() {
            super.pack();
        }

        /**
         * Returns the resulting <code>ShortMessageMidiFilter</code>.
         * @return
         */
        public MidiFilter getFilter() {
            return panel.filter;
        }
    }

    static class EditMidiFilterPanel extends JPanel {
    
        private static final long serialVersionUID = 1;
        
        private SubListSelectionPanel eventTypeList;
        private SubListSelectionPanel channelsList;
        private JSlider minVolumeSlider;
        private JSlider maxVolumeSlider;
        private JCheckBox displayInfoCheckBox;
        private ArrayList<ChannelWrapper> channels;
        private ArrayList<EventDescWrapper> noteList;
        private ArrayList<EventDescWrapper> data1List;
        private MidiFilter filter;
        private JComboBox eventTypeComboBox;
        private JRadioButton blockNonShortMessageEventsRadioButton = new JRadioButton();
        private EventTypeWrapper selectedEventTypeWrapper;

        public EditMidiFilterPanel( EventMap eventMap, MidiFilter filter, boolean info ) {
            super( new BorderLayout() );
            this.filter = filter;
            
            JSplitPane innerPanel = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
            innerPanel.setContinuousLayout( true );
            
            ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
            Action toLeftAction = new AbstractAction( rb.getString( "midi.filter.editFilter.unmap" ) ) {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e ) {
                }
            };
            Action toRightAction = new AbstractAction( rb.getString( "midi.filter.editFilter.map" ) ) {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e ) {
                }
            };
            Action allToLeftAction = new AbstractAction( rb.getString( "midi.filter.editFilter.mapNone" ) ) {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e ) {
                }
            };
            Action allToRightAction = new AbstractAction( rb.getString( "midi.filter.editFilter.mapAll" ) ) {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e ) {
                }
            };
            eventTypeList = new SubListSelectionPanel( new Object[0], new Object[0] , toLeftAction, toRightAction );
            eventTypeList.setAllToLeftAction( allToLeftAction );
            eventTypeList.setAllToRightAction( allToRightAction );
            eventTypeList.getSplitPane().setBorder( null );
            eventTypeList.setKeepLeftOrder( true );
    
            toLeftAction = new AbstractAction( rb.getString( "midi.filter.editFilter.unmap" ) ) {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e ) {
                }
            };
            toRightAction = new AbstractAction( rb.getString( "midi.filter.editFilter.map" ) ) {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e ) {
                }
            };
            allToLeftAction = new AbstractAction( rb.getString( "midi.filter.editFilter.mapNone" ) ) {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e ) {
                }
            };
            allToRightAction = new AbstractAction( rb.getString( "midi.filter.editFilter.mapAll" ) ) {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e ) {
                }
            };
            
            channelsList = new SubListSelectionPanel( new Object[0], new Object[0] , toLeftAction, toRightAction );
            channelsList.setAllToLeftAction( allToLeftAction );
            channelsList.setAllToRightAction( allToRightAction );
            channelsList.getSplitPane().setBorder( null );
            channelsList.setKeepLeftOrder( true );
            channels = new ArrayList<ChannelWrapper>( channelNames.length );
            for (int i = 0; i < channelNames.length; i++) {
                channels.add( new ChannelWrapper( channelNames[i], i ) );
            }
            
            EventDescriptor[] eds = eventMap.getEventDescriptors();
            noteList = new ArrayList<EventDescWrapper>();
            for (int i = 0; i < eds.length; i++) {
                noteList.add( new EventDescWrapper(
                        (short) ((NoteDescriptor) eds[i]).getNote(), eds[i].getDescription() ) );
            }
            data1List = new ArrayList<EventDescWrapper>();
            for (short i = 0; i < 128; i++) {
                data1List.add( new EventDescWrapper( i, Short.toString( i ) ) );
            }

            JPanel eventTypeSelectPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
            eventTypeSelectPanel.add( new JLabel( rb.getString( "midi.filter.editFilter.eventType" ) ) );
            EventTypeWrapper[] etw = new EventTypeWrapper[EventMap.EVENT_NAME_MAP.size()];
            int i = 0;
            EventTypeWrapper selectedEventType = null;
            EventTypeWrapper noteOnEventTypeWrapper = null;
            for (Integer command : EventMap.EVENT_NAME_MAP.keySet()) {
                etw[i] = new EventTypeWrapper(
                        command.intValue(), EventMap.EVENT_NAME_MAP.get( command ),
                        (filter == null ? null : filter.getFilterElement( command.shortValue() )) );
                if (command.intValue() == ShortMessage.NOTE_ON) {
                    noteOnEventTypeWrapper = etw[i];
                }
                if (etw[i].getMidiFilterElement() != null && selectedEventType == null) {
                    selectedEventType = etw[i];
                }
                i++;
            }
            if (selectedEventType == null) {
                selectedEventType = noteOnEventTypeWrapper;
            }
            eventTypeComboBox = new JComboBox( etw );
            eventTypeComboBox.setSelectedItem( selectedEventType );
            eventTypeComboBox.addItemListener( new ItemListener() {
                public void itemStateChanged( ItemEvent e ) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        setFilterList();
                    }
                }
            } );
            eventTypeSelectPanel.add( eventTypeComboBox );
            blockNonShortMessageEventsRadioButton = new JRadioButton(
                    rb.getString( "midi.filter.editFilter.blockNonShortMessage" ),
                    (filter == null ? false : filter.isBlockingNonShortMessageEvents()) );
            JRadioButton acceptNonShortMessageEventsRadioButton = new JRadioButton(
                    rb.getString( "midi.filter.editFilter.acceptNonShortMessage" ),
                    !blockNonShortMessageEventsRadioButton.isSelected() );
            ButtonGroup bgr = new ButtonGroup();
            bgr.add( blockNonShortMessageEventsRadioButton );
            bgr.add( acceptNonShortMessageEventsRadioButton );
            eventTypeSelectPanel.add( new JLabel( "    " ) );
            eventTypeSelectPanel.add( acceptNonShortMessageEventsRadioButton );
            eventTypeSelectPanel.add( blockNonShortMessageEventsRadioButton );
    
            JPanel eventTypeListPanel = new JPanel( new BorderLayout() );
            eventTypeListPanel.setBorder( new TitledBorder( rb.getString( "midi.filter.editFilter.events" ) ) );
            eventTypeListPanel.add( eventTypeSelectPanel, BorderLayout.NORTH );
            eventTypeListPanel.add( eventTypeList );
            JPanel channelListPanel = new JPanel( new GridLayout() );
            channelListPanel.setBorder( new TitledBorder( rb.getString( "midi.filter.editFilter.channels" ) ) );
            channelListPanel.add( channelsList );
            innerPanel.setLeftComponent( eventTypeListPanel );
            innerPanel.setRightComponent( channelListPanel );
            innerPanel.setDividerLocation( 0.7 );
            innerPanel.setResizeWeight( 0.7 );
    
            JPanel minVolumePanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
            JLabel minVolumeLabel = new JLabel(
                SgEngine.getInstance().getResourceBundle().getString(
                    "midi.filter.editFilter.minVolume" ) );
            minVolumeSlider =
                new JSlider( JSlider.HORIZONTAL, 0, 127, 0 );
            final JTextField minVolumeTextField = new JTextField( Integer.toString( minVolumeSlider.getValue() ), 3 );
            minVolumeTextField.getDocument().addDocumentListener( new DocumentListener() {
                public void changedUpdate( DocumentEvent e ) {
                    try {
                        int value = Integer.parseInt( minVolumeTextField.getText() );
                        if (minVolumeSlider.getValue() != value) {
                            if (value <= minVolumeSlider.getMaximum() && value >= minVolumeSlider.getMinimum()) {
                                minVolumeSlider.setValue( value );
                            } else {
                                SwingUtilities.invokeLater( new Runnable() {
                                    public void run() {
                                        minVolumeTextField.setText( Integer.toString( minVolumeSlider.getValue() ) );
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
            minVolumeLabel.setLabelFor( minVolumeTextField );
            minVolumeSlider.addChangeListener( new ChangeListener() {
                public void stateChanged( ChangeEvent e ) {
                    String value = Integer.toString( minVolumeSlider.getValue() );
                    if (!value.equals( minVolumeTextField.getText() )) {
                        minVolumeTextField.setText( value );
                    }
                }
            } );
            minVolumePanel.add( minVolumeSlider );
            minVolumePanel.add( minVolumeTextField );
            
            JPanel maxVolumePanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
            JLabel maxVolumeLabel = new JLabel(
                SgEngine.getInstance().getResourceBundle().getString(
                    "midi.filter.editFilter.maxVolume" ) );
            maxVolumeSlider =
                new JSlider( JSlider.HORIZONTAL, 0, 127, 127 );
            final JTextField maxVolumeTextField = new JTextField( Integer.toString( maxVolumeSlider.getValue() ), 3 );
            maxVolumeTextField.getDocument().addDocumentListener( new DocumentListener() {
                public void changedUpdate( DocumentEvent e ) {
                    try {
                        int value = Integer.parseInt( maxVolumeTextField.getText() );
                        if (maxVolumeSlider.getValue() != value) {
                            if (value <= maxVolumeSlider.getMaximum() && value >= maxVolumeSlider.getMinimum()) {
                                maxVolumeSlider.setValue( value );
                            } else {
                                SwingUtilities.invokeLater( new Runnable() {
                                    public void run() {
                                        maxVolumeTextField.setText( Integer.toString( maxVolumeSlider.getValue() ) );
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
            maxVolumeLabel.setLabelFor( maxVolumeTextField );
            maxVolumeSlider.addChangeListener( new ChangeListener() {
                public void stateChanged( ChangeEvent e ) {
                    String value = Integer.toString( maxVolumeSlider.getValue() );
                    if (!value.equals( maxVolumeTextField.getText() )) {
                        maxVolumeTextField.setText( value );
                    }
                }
            } );
            maxVolumePanel.add( maxVolumeSlider );
            maxVolumePanel.add( maxVolumeTextField );
            
            JPanel minVolumeLabelPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
            minVolumeLabelPanel.add( minVolumeLabel );
            JPanel maxVolumeLabelPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
            maxVolumeLabelPanel.add( maxVolumeLabel );
            
            JPanel volumePanel = new JPanel( new GridLayout( 4, 1 ) );
            volumePanel.add( minVolumeLabelPanel );
            volumePanel.add( minVolumePanel );
            volumePanel.add( maxVolumeLabelPanel );
            volumePanel.add( maxVolumePanel );
            
            JButton resetToPessimisticButton = new JButton(
                    rb.getString( "midi.filter.editFilter.resetToPessimistic" ) );
            resetToPessimisticButton.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    ListModel lm = eventTypeComboBox.getModel();
                    EditMidiFilterPanel.this.filter = new MidiFilter(
                            false,
                            EditMidiFilterPanel.this.filter != null ?
                                    EditMidiFilterPanel.this.filter.isBlockingNonShortMessageEvents() : false,
                                    new MidiFilterElement[0] );
                    for (int i = 0; i < lm.getSize(); i++) {
                        EventTypeWrapper etw = (EventTypeWrapper) lm.getElementAt( i );
                        etw.setMidiFilterElement( null );
                    }
                    selectedEventTypeWrapper = null;
                    setFilterList();
                }
            } );
            JButton resetToOptimisticButton = new JButton(
                    rb.getString( "midi.filter.editFilter.resetToOptimistic" ) );
            resetToOptimisticButton.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    ListModel lm = eventTypeComboBox.getModel();
                    EditMidiFilterPanel.this.filter = new MidiFilter(
                            true,
                            EditMidiFilterPanel.this.filter != null ?
                                    EditMidiFilterPanel.this.filter.isBlockingNonShortMessageEvents() : true,
                                    new MidiFilterElement[0] );
                    for (int i = 0; i < lm.getSize(); i++) {
                        EventTypeWrapper etw = (EventTypeWrapper) lm.getElementAt( i );
                        etw.setMidiFilterElement( null );
                    }
                    selectedEventTypeWrapper = null;
                    setFilterList();
                }
            } );
            
            JPanel infoPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
            displayInfoCheckBox = new JCheckBox( rb.getString( "midi.filter.editFilter.displayFilterInfo" ) );
            displayInfoCheckBox.setSelected( info );
            infoPanel.add( displayInfoCheckBox );
            JPanel southEastPanel = new JPanel( new BorderLayout() );
            JPanel resetButtonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
            resetButtonPanel.add( resetToPessimisticButton );
            resetButtonPanel.add( resetToOptimisticButton );
            southEastPanel.add( resetButtonPanel, BorderLayout.NORTH );
            southEastPanel.add( infoPanel, BorderLayout.SOUTH );
            
            JPanel southPanel = new JPanel( new BorderLayout() );
            southPanel.add( volumePanel );
            southPanel.add( southEastPanel, BorderLayout.EAST );
            
            setFilterList();
            
            add( innerPanel );
            add( southPanel, BorderLayout.SOUTH );
        }
        
        void done() {
            if (selectedEventTypeWrapper != null) {
                selectedEventTypeWrapper.setMidiFilterElement( createMidiFilterElement(
                        (short) selectedEventTypeWrapper.getCommand() ) );
            }
        }
        
        private void setFilterList() {
            done();
            
            EventTypeWrapper etw = (EventTypeWrapper) eventTypeComboBox.getSelectedItem();
            MidiFilterElement filterElement = null;
            filterElement = etw.getMidiFilterElement();
            selectedEventTypeWrapper = etw;
            
            channelsList.setRightObjects( Collections.EMPTY_LIST );
            if (MidiToolkit.isChannelMessageStatusByte( etw.getCommand() )) {
                if (!channelsList.isEnabled()) {
                    channelsList.setEnabled( true );
                }
                channelsList.setLeftObjects( channels );
                if ((filterElement == null && (filter == null || filter.isOptimistic())) ||
                        (filterElement != null && filterElement.getChannels() == null)) {
                    channelsList.moveFromLeftToRight( channels );
                } else if (filterElement != null){
                    ArrayList<Object> moveObjects = new ArrayList<Object>();
                    for (int i = 0; i < channelsList.getLeftList().getModel().getSize(); i++) {
                        ChannelWrapper cw = (ChannelWrapper) channelsList.getLeftList().getModel().getElementAt( i );
                        if (filterElement.getChannelMask()[cw.channelNumber]) {
                            moveObjects.add( cw );
                        }
                    }
                    channelsList.moveFromLeftToRight( moveObjects );
                }
            } else {
                channelsList.setLeftObjects( Collections.EMPTY_LIST );
                channelsList.setEnabled( false );
            }
            List<EventDescWrapper> list = etw.getCommand() == ShortMessage.NOTE_ON ? noteList : data1List;
            eventTypeList.setLeftObjects( list );
            eventTypeList.setRightObjects( Collections.EMPTY_LIST );
            if ((filterElement == null && (filter == null || filter.isOptimistic())) ||
                    (filterElement != null && filterElement.getData1Fields() == null)) {
                eventTypeList.moveFromLeftToRight( list );
            } else if (filterElement != null) {
                ArrayList<Object> moveObjects = new ArrayList<Object>();
                for (int i = 0; i < eventTypeList.getLeftList().getModel().getSize(); i++) {
                    EventDescWrapper edw = (EventDescWrapper) eventTypeList.getLeftList().getModel().getElementAt( i );
                    if (filterElement.getData1Mask()[edw.getData1()]) {
                        moveObjects.add( edw );
                    }
                }
                eventTypeList.moveFromLeftToRight( moveObjects );
            }
            if (filterElement == null) {
                minVolumeSlider.setValue( minVolumeSlider.getMinimum() );
                maxVolumeSlider.setValue( minVolumeSlider.getMaximum() );
            } else {
                minVolumeSlider.setValue( filterElement.getMinimumData2() );
                maxVolumeSlider.setValue( filterElement.getMaximumData2() );
            }
        }
        
        private MidiFilterElement createMidiFilterElement( short command ) {
            if (filter != null && filter.isOptimistic() &&
                    getEventTypeList().getLeftList().getModel().getSize() == 0 &&
                    getChannelList().getLeftList().getModel().getSize() == 0 &&
                    minVolumeSlider.getValue() == minVolumeSlider.getMinimum() &&
                    maxVolumeSlider.getValue() == maxVolumeSlider.getMaximum()) {
                return null;
            }
            if (filter != null && !filter.isOptimistic() &&
                    getEventTypeList().getRightList().getModel().getSize() == 0 &&
                    getChannelList().getRightList().getModel().getSize() == 0 &&
                    minVolumeSlider.getValue() == minVolumeSlider.getMinimum() &&
                    maxVolumeSlider.getValue() == maxVolumeSlider.getMaximum()) {
                return null;
            }

            short[] notes;
            short[] channels;
            SubListSelectionPanel list = getEventTypeList();
            if (list.getLeftList().getModel().getSize() == 0) {
                notes = null;
            } else {
                notes = new short[list.getRightList().getModel().getSize()];
                for (int i = 0; i < notes.length; i++) {
                    EventDescWrapper w = (EventDescWrapper) list.getRightList().getModel().getElementAt( i );
                    notes[i] = (short) w.getData1();
                }
            }
            list = getChannelList();
            if (list.getLeftList().getModel().getSize() == 0) {
                channels = null;
            } else {
                channels = new short[list.getRightList().getModel().getSize()];
                for (int i = 0; i < channels.length; i++) {
                    ChannelWrapper w = (ChannelWrapper) list.getRightList().getModel().getElementAt( i );
                    channels[i] = (short) w.getChannel();
                }
            }
            return new MidiFilterElement(
                    command, channels, notes, getMinimumVolume(), getMaximumVolume() );
        }
                
        public SubListSelectionPanel getEventTypeList() {
            return eventTypeList;
        }
    
        public SubListSelectionPanel getChannelList() {
            return channelsList;
        }
        
        public short getMinimumVolume() {
            return (short) minVolumeSlider.getValue();
        }
    
        public short getMaximumVolume() {
            return (short) maxVolumeSlider.getValue();
        }
    }
    static class ChannelWrapper {
        private String name;
        private int channelNumber;
        ChannelWrapper( String name, int channelNumber ) {
            this.name = name;
            this.channelNumber = channelNumber;
        }
        public int getChannel() {
            return channelNumber;
        }
        public String toString() {
            return " " + name;
        }
    }
    public static class EventDescWrapper {
        private short data1;
        private String s;
        EventDescWrapper( short data1, String s ) {
            this.data1 = data1;
            this.s = s;
        }
        public short getData1() {
            return data1;
        }
        public String toString() {
            return " " + s;
        }
    }
    public static class EventTypeWrapper {
        private int command;
        private String name;
        private MidiFilterElement midiFilterElement;
        EventTypeWrapper( int command, String name, MidiFilterElement midiFilterElement ) {
            this.command = command;
            this.name = name;
            this.midiFilterElement = midiFilterElement;
        }
        public MidiFilterElement getMidiFilterElement() {
            return midiFilterElement;
        }
        public void setMidiFilterElement( MidiFilterElement midiFilterElement ) {
            this.midiFilterElement = midiFilterElement;
        }
        public int getCommand() {
            return command;
        }
        public String toString() {
            return name;
        }
    }
    
    static class ChangeMidiFilterEdit extends SbNodeStateChangeEdit {

        private static final long serialVersionUID = 1L;

        MidiFilter filter;
        boolean info;
        
        ChangeMidiFilterEdit(
                SoundbusDescriptor soundbusDescriptor, MidiFilterNode node, MidiFilter filter, boolean info ) {
            super( soundbusDescriptor, node,
                    SgEngine.getInstance().getResourceBundle().getString( "edit.changeMidiFilter" ),
                    false );
            this.filter = filter;
            this.info = info;
        }
        
        
        @Override
        protected void redoImpl() {
            performImpl();
        }

        @Override
        protected void undoImpl() {
            performImpl();
        }

        @Override
        public void performImpl() {
            MidiFilterNode node = (MidiFilterNode) getNode();
            MidiFilter filter = node.getMidiFilter();
            node.setMidiFilter( this.filter );
            this.filter = filter;
            String s = node.getClientProperty( DISPLAY_INFO_PROPERTY );
            if (!Boolean.toString( info ).equals( s )) {
                node.putClientProperty( DISPLAY_INFO_PROPERTY, Boolean.toString( info ) );
                info = !info;
            }
        }
    }
}
