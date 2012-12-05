/*
 * Created on 05.12.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.grid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.SgProperties;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.midi.EventDescriptor;
import com.jonasreese.sound.sg.midi.EventMap;
import com.jonasreese.sound.sg.midi.MidiChangeMonitor;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.MidiEventSelectionEvent;
import com.jonasreese.sound.sg.midi.MidiEventSelectionListener;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.midi.NoteDescriptor;
import com.jonasreese.sound.sg.midi.SgMidiSequence;
import com.jonasreese.sound.sg.midi.ShortMessageEventDescriptor;
import com.jonasreese.sound.sg.midi.TrackProxy;
import com.jonasreese.sound.sg.midi.datatransfer.MidiSerializer;
import com.jonasreese.sound.sg.midi.datatransfer.MidiTransferable;
import com.jonasreese.sound.sg.midi.edit.ChangeEventMapEdit;
import com.jonasreese.sound.sg.midi.edit.DefaultEventMapEdit;
import com.jonasreese.sound.sg.midi.edit.ImportEventMapEdit;
import com.jonasreese.sound.sg.midi.edit.InsertEventMapEdit;
import com.jonasreese.sound.sg.midi.edit.RemoveEventMapEdit;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.functionality.Functionality;
import com.jonasreese.sound.sg.ui.defaultui.SessionActionPool;
import com.jonasreese.sound.sg.ui.defaultui.SessionUi;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.sound.sg.ui.defaultui.action.UndoAction;
import com.jonasreese.util.ExtensionFileFilter;
import com.jonasreese.util.FSTools;
import com.jonasreese.util.ParametrizedResourceBundle;
import com.jonasreese.util.Updatable;
import com.jonasreese.util.resource.ResourceLoader;
import com.jonasreese.util.swing.DefaultPopupListener;

/**
 * <p>
 * This class acts as a controller for common grid functionalities.<br>
 * It controls the dependencies between the <code>GridComponent</code>,
 * the <code>GridControlPanel</code> and the <code>ViewInstance</code>
 * (<code>GridVi</code>).
 * </p>
 * @author jreese
 */
public class GridController
    implements PropertyChangeListener, MidiEventSelectionListener, MidiChangeMonitor {
    
    private GridComponent gridComponent;
    private JScrollPane scrollPane;
    private JMenu gridMenu;
    private JMenu tracksSubMenu;
    private JMenu functionsMenu;
    private JComboBox tracksComboBox;
    private ArrayList<JMenuItem> trackMenuItems;
    private AbstractButton gridToggleButton;
    private JCheckBoxMenuItem gridToggleMenuItem;
    private JCheckBoxMenuItem[] snapToGridToggleMenuItems;
    private SessionActionPool sessionActionPool;
    private SgAction insertMidiNoteAction;
    private Action repeatMidiNoteAction;
    private Action playMidiNoteAction;
    private Action applyMinimumVolumeAction;
    private Action applyMaximumVolumeAction;
    private Action increaseVolumeAction;
    private Action decreaseVolumeAction;
    private Action quantizeAction;
    private Action setTempoAction;
    private Action editMidiEventsAction;
    private Action infoAction;
    private Action rectangularSelectionAction;
    private AbstractButton[] rectangularSelectionButtons; 
    private EventTypeAction[] eventTypeActions;
    private JCheckBoxMenuItem[] eventTypeItems;
    private Action editNoteMappingAction;
    private Action extractNoteMappingAction;
    private Action importNoteMappingAction;
    private Action exportNoteMappingAction;
    private Action defaultNoteMappingAction;
    private JPopupMenu contextMenu;
    private AbstractButton undoButton;
    private AbstractButton redoButton;
    private Plugin plugin;
    private Updatable tempoUpdatable;
    
    /**
     * Constructs a new GridController.
     * @param midiDescriptor A <code>MidiDescriptor</code>.
     * @param plugin The <code>Plugin</code> that is used for property retrieval.
     * @throws InvalidMidiDataException If the sequence associated with this
     *         <code>MidiDescriptor</code> is invalid.
     * @throws IOException If the loading of the sequence failed.
     */
    public GridController( final MidiDescriptor midiDescriptor, Plugin plugin )
        throws InvalidMidiDataException, IOException {

        this.plugin = plugin;
        gridComponent = new GridComponent( midiDescriptor );
        gridComponent.setEventMappingComponent( new EventMappingComponent( gridComponent, this ) );
        gridComponent.addPropertyChangeListener( this );
        // set gridComponent properties
        SgProperties p = SgEngine.getInstance().getProperties();
        gridComponent.setBackground(
            p.getPluginProperty( plugin, "backgroundColor", gridComponent.getBackground() ) );
        gridComponent.setGridColor(
            p.getPluginProperty( plugin, "gridColor", gridComponent.getGridColor() ) );
        gridComponent.setNoteColor1(
            p.getPluginProperty( plugin, "noteColor1", gridComponent.getNoteColor1() ) );
        gridComponent.setNoteColor2(
            p.getPluginProperty( plugin, "noteColor2", gridComponent.getNoteColor2() ) );
        gridComponent.setSelectedNoteColor(
            p.getPluginProperty( plugin, "selectedNoteColor", gridComponent.getSelectedNoteColor() ) );
        gridComponent.setEventColor(
            p.getPluginProperty( plugin, "eventColor", gridComponent.getEventColor() ) );
        gridComponent.setSelectedEventColor(
            p.getPluginProperty( plugin, "selectedEventColor", gridComponent.getSelectedEventColor() ) );
        gridComponent.setTactColor(
            p.getPluginProperty( plugin, "tactColor", gridComponent.getTactColor() ) );
        gridComponent.setRowHeight(
            p.getPluginProperty( plugin, "rowHeight", GridComponent.DEFAULT_ROW_HEIGHT ) );
        gridComponent.setMidiEventTickLength(
            p.getPluginProperty( plugin, "midiEventTickLength", GridComponent.DEFAULT_ROW_HEIGHT ) );
        gridComponent.setDisplayTactNumbers(
            p.getPluginProperty( plugin, "displayTactNumbers", true ) );
        gridComponent.setToolTipsEnabled(
            p.getPluginProperty( plugin, "toolTipsEnabled", true ) );
        gridComponent.setChaseCursor(
            p.getPluginProperty( plugin, "chaseCursor", true ) );
        gridComponent.setChaseCursorPagewise(
            p.getPluginProperty( plugin, "chaseCursor.pagewise", true ) );
        gridComponent.setDoubleBuffered(
            p.getPluginProperty( plugin, "doublebuffer", true ) );
        
        scrollPane = new JScrollPane( gridComponent );
        scrollPane.setColumnHeaderView( gridComponent.getRulerComponent() );
        scrollPane.setRowHeaderView( gridComponent.getEventMappingComponent() );
        JSlider control = new JSlider( JSlider.HORIZONTAL );
        control.setMinimum( 40 );
        control.setMaximum( 150 );
        int val = gridComponent.getEventMappingComponent().getComponentWidth();
        if (val < control.getMinimum()) {
            val = control.getMinimum();
            gridComponent.getEventMappingComponent().setComponentWidth( val );
        }
        control.setPreferredSize( new Dimension( control.getMinimum(), control.getPreferredSize().height ) );
        control.setValue( val );
        control.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                gridComponent.getEventMappingComponent().setComponentWidth(
                        ((JSlider) e.getSource()).getValue() );
                gridComponent.getEventMappingComponent().updateLayout();
            }
        } );
        scrollPane.setCorner( JScrollPane.UPPER_LEFT_CORNER, control );

        sessionActionPool =
            UiToolkit.getSessionUi( midiDescriptor.getSession() ).getActionPool();

        final ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        
        // create track submenu
        tracksSubMenu = new JMenu(
            rb.getString(
                "plugin.gridView.gridMenu.tracks" ) );
        tracksSubMenu.setIcon( UiToolkit.SPACER );

        Action showGridAction = new ShowGridAction(
            rb.getString( "plugin.gridView.gridMenu.showGrid" ),
            rb.getString( "plugin.gridView.gridMenu.showGrid.shortDescription" ) );

        gridToggleButton = new JToggleButton(
            rb.getString(
                "plugin.gridView.showGrid" ),
            getGridComponent().isGridPainted() );
        gridToggleButton.addActionListener( showGridAction );

        Action snapToGridAction = new SnapToGridAction(
            rb.getString( "plugin.gridView.gridMenu.snapToGrid" ),
            rb.getString( "plugin.gridView.gridMenu.snapToGrid.shortDescription" ) );

        // create additional menues
        gridMenu = new JMenu(
            rb.getString(
                "plugin.gridView.gridMenu" ) );

        gridMenu.add( tracksSubMenu );
        // create zoom submenu
        JMenu zoomMenu = new JMenu(
            rb.getString(
                "plugin.gridView.gridMenu.zoom" ) );
        zoomMenu.setIcon( UiToolkit.SPACER );
        ButtonGroup bgr = new ButtonGroup();
        for (int i = 10; i <= 100; i+= 10) {
            double zoom = i / 100.0;
            JCheckBoxMenuItem item = new ZoomMenuItem( zoom );
            bgr.add( item );
            zoomMenu.add( item );
        }
        for (int i = 150; i <= 1000; i+= 50) {
            double zoom = i / 100.0;
            JCheckBoxMenuItem item = new ZoomMenuItem( zoom );
            bgr.add( item );
            zoomMenu.add( item );
            if (i >= 500) { i += 50; }
        }
        
        gridMenu.add( zoomMenu );
        gridToggleMenuItem = new JCheckBoxMenuItem( showGridAction );
        gridToggleMenuItem.setSelected( getGridComponent().isGridPainted() );
        gridMenu.add( gridToggleMenuItem );
        snapToGridToggleMenuItems = new JCheckBoxMenuItem[2];
        snapToGridToggleMenuItems[0] = new JCheckBoxMenuItem( snapToGridAction );
        snapToGridToggleMenuItems[1] = new JCheckBoxMenuItem( snapToGridAction );
        for (int i = 0; i < snapToGridToggleMenuItems.length; i++) {
            snapToGridToggleMenuItems[i].setSelected( getGridComponent().isSnapToGridEnabled() );
        }
        gridMenu.add( snapToGridToggleMenuItems[0] );
        gridMenu.addSeparator();
        
        JMenu eventsMenu = new JMenu(
            rb.getString(
                "plugin.gridView.gridMenu.eventTypes" ) );
        eventsMenu.setIcon( UiToolkit.SPACER );
        eventTypeActions = createEventTypeActions();
        eventTypeItems = new JCheckBoxMenuItem[eventTypeActions.length];
        for (int i = 0; i < eventTypeActions.length; i++) {
            eventTypeItems[i] = new JCheckBoxMenuItem( eventTypeActions[i] );
            eventsMenu.add( eventTypeItems[i] );
		}
        updateEventTypeSelectedStates();
        eventsMenu.addSeparator();
        eventsMenu.add( new AbstractAction(
            rb.getString(
                "plugin.gridView.gridMenu.eventTypes.allEvents" ) ) {
                    private static final long serialVersionUID = 1L;
                    public void actionPerformed( ActionEvent e ) {
                        TrackProxy track = gridComponent.getTrack();
                        if (track != null) {
                            for (int i = eventTypeActions.length - 1; i >= 0; i--) {
                                if (track.getEventMap().getIndexFor(
                                        new ShortMessageEventDescriptor( null, null, eventTypeActions[i].type ) ) < 0) {
                                    eventTypeActions[i].actionPerformed( null );
                                }
                            }
                        }
                    }
        } );
        eventsMenu.add(new AbstractAction(
            rb.getString(
                "plugin.gridView.gridMenu.eventTypes.noEvents" ) ) {
                    private static final long serialVersionUID = 1L;
                    public void actionPerformed( ActionEvent e ) {
                        TrackProxy track = gridComponent.getTrack();
                        if (track != null) {
                            for (int i = 0; i < eventTypeActions.length; i++) {
                                if (track.getEventMap().getIndexFor(
                                        new ShortMessageEventDescriptor( null, null, eventTypeActions[i].type ) ) >= 0) {
                                    eventTypeActions[i].actionPerformed( null );
                                }
                            }
                        }
                    }
        } );
        gridMenu.add( eventsMenu );
        gridMenu.addSeparator();
        editNoteMappingAction = new AbstractAction(
                rb.getString( "plugin.gridView.eventmap.editEventMap" ) ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                UiToolkit.showEditEventMappingDialog(
                        gridComponent.getMidiDescriptor(), gridComponent.getEventMap() );
            }
        };
        editNoteMappingAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.gridView.eventmap.editEventMap.description" ) );
        gridMenu.add( editNoteMappingAction );
        extractNoteMappingAction = new AbstractAction(
                rb.getString( "plugin.gridView.eventmap.extractEventMap" ) ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                final JRadioButton cutUpperLowerRadioButton = new JRadioButton( rb.getString(
                        "plugin.gridView.eventmap.extractEventMap.cutUpperLower" ), true );
                final JRadioButton cutAllRadioButton = new JRadioButton( rb.getString(
                        "plugin.gridView.eventmap.extractEventMap.cutAll" ) );
                final JCheckBox removeMapsCheckBox = new JCheckBox( rb.getString(
                        "plugin.gridView.eventmap.extractEventMap.cutUnused" ), true );
                removeMapsCheckBox.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent e ) {
                        boolean b = removeMapsCheckBox.isSelected();
                        cutUpperLowerRadioButton.setEnabled( b );
                        cutAllRadioButton.setEnabled( b );
                    }
                } );
                JCheckBox addNonExistingMapsCheckBox = new JCheckBox( rb.getString(
                        "plugin.gridView.eventmap.extractEventMap.addNonExisting" ) );
                ButtonGroup bgr = new ButtonGroup();
                bgr.add( cutUpperLowerRadioButton );
                bgr.add( cutAllRadioButton );
                GridLayout layout = new GridLayout( 2, 1 );
                JPanel radioPanel = new JPanel( new BorderLayout() );
                JPanel rPanel = new JPanel( layout );
                rPanel.add( cutUpperLowerRadioButton );
                rPanel.add( cutAllRadioButton );
                radioPanel.add( rPanel );
                radioPanel.add( new JPanel(), BorderLayout.WEST );
                Object[] controls = new Object[] {
                        removeMapsCheckBox,
                        radioPanel,
                        addNonExistingMapsCheckBox
                };
                
                int option = JOptionPane.showConfirmDialog(
                        UiToolkit.getMainFrame(),
                        controls,
                        rb.getString( "plugin.gridView.eventmap.extractEventMap.title" ),
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE, null );
                    
                if (option == JOptionPane.OK_OPTION) {
                    try {
                        TrackProxy track = midiDescriptor.getSequence().getSelectedTrackProxy();
                        EventMap eventMap = gridComponent.getEventMap();
                        EventMap newMap = TrackProxy.createEmptyEventMap();
                        newMap.setEventDescriptors( eventMap.getEventDescriptors() );
                        if (removeMapsCheckBox.isSelected()) {
                            boolean[] removeIndices = new boolean[newMap.getSize()];
                            for (int i = 0; i < removeIndices.length; i++) {
                                removeIndices[i] = true;
                            }
                            for (int i = 0; i < track.size(); i++) {
                                MidiEvent midiEvent = track.get( i );
                                int index = newMap.getIndexFor( midiEvent );
                                if (index >= 0 && newMap.getEventAt( index ) instanceof NoteDescriptor) {
                                    removeIndices[index] = false;
                                }
                            }
                            if (cutAllRadioButton.isSelected()) {
                                for (int i = removeIndices.length - 1; i >= 0; i--) {
                                    if (removeIndices[i] && newMap.getEventAt( i ) instanceof NoteDescriptor) {
                                        newMap.removeEventAt( i );
                                    }
                                }
                            } else {
                                // remove events at the bottom
                                boolean fromTop = false;
                                for (int i = removeIndices.length - 1; i >= 0; i--) {
                                    if (removeIndices[i] && newMap.getEventAt( i ) instanceof NoteDescriptor) {
                                        newMap.removeEventAt( i );
                                    } else {
                                        fromTop = true;
                                        i = -1;
                                    }
                                }
                                if (fromTop) {
                                    // remove events at the top, but in reverse order
                                    int count = 0;
                                    for (int i = 0; i < removeIndices.length; i++) {
                                        if (!removeIndices[i]) {
                                            break;
                                        }
                                        count++;
                                    }
                                    for (int i = count - 1; i >= 0; i--) {
                                        if (newMap.getEventAt( i ) instanceof NoteDescriptor) {
                                            newMap.removeEventAt( i );
                                        }
                                    }
                                }
                            }
                        }
                        if (addNonExistingMapsCheckBox.isSelected()) {
                            EventMap defaultMap = TrackProxy.createDefaultEventMap();
                            for (int i = 0; i < track.size(); i++) {
                                MidiEvent midiEvent = track.get( i );
                                if (!newMap.contains( midiEvent )) {
                                    EventDescriptor ed = defaultMap.getEventDescriptorFor( midiEvent );
                                    if (ed != null) {
                                        if (ed instanceof NoteDescriptor) {
                                            newMap.appendEvent( ed );
                                        } else {
                                            newMap.insertEventAt( 0, ed );
                                        }
                                    }
                                }
                            }
                        }
                        EventDescriptor[] descriptors = new EventDescriptor[newMap.getSize()];
                        for (int i = 0; i < descriptors.length; i++) {
                            EventDescriptor ed = newMap.getEventAt( i );
                            if (ed.getEventMap() == eventMap) {
                                descriptors[i] = ed;
                            } else {
                                descriptors[i] = MidiToolkit.copyEventDescriptor( ed, eventMap );
                            }
                        }
                        ChangeEventMapEdit edit = new ChangeEventMapEdit( eventMap, descriptors );
                        edit.perform();
                        midiDescriptor.getUndoManager().addEdit( edit );
                    } catch ( InvalidMidiDataException imdex ) {
                        imdex.printStackTrace();
                    } catch ( IOException ioex ) {
                        ioex.printStackTrace();
                    }
                }
            }
        };
        extractNoteMappingAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.gridView.eventmap.extractEventMap.description" ) );
        gridMenu.add( extractNoteMappingAction );
        importNoteMappingAction = new AbstractAction(
            rb.getString(
                "plugin.gridView.gridMenu.importNoteMapping" ),
            UiToolkit.SPACER ) {

            static final long serialVersionUID = 0;
            public void actionPerformed( ActionEvent e ) {
                importNoteMapping();
            }
        };
        importNoteMappingAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.gridView.gridMenu.importNoteMapping.shortDescription" ) );
        gridMenu.add( importNoteMappingAction );
        exportNoteMappingAction = new AbstractAction(
            rb.getString(
                "plugin.gridView.gridMenu.exportNoteMapping" ),
            UiToolkit.SPACER ) {

            static final long serialVersionUID = 0;
            public void actionPerformed( ActionEvent e ) {
                exportNoteMapping();
            }
        };
        exportNoteMappingAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.gridView.gridMenu.exportNoteMapping.shortDescription" ) );
        gridMenu.add( exportNoteMappingAction );
        defaultNoteMappingAction = new AbstractAction(
            rb.getString(
                "plugin.gridView.gridMenu.defaultNoteMapping" ),
            UiToolkit.SPACER ) {

            static final long serialVersionUID = 0;
            public void actionPerformed( ActionEvent e ) {
                DefaultEventMapEdit edit = new DefaultEventMapEdit(
                        gridComponent.getTrack().getEventMap() );
                edit.perform();
                midiDescriptor.getUndoManager().addEdit( edit );
            }
        };
        defaultNoteMappingAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.gridView.gridMenu.defaultNoteMapping.shortDescription" ) );
        gridMenu.add( defaultNoteMappingAction );

        
        
        
        // create menu for track
        // add MIDI track menu functionality to context menu
        Plugin pl = SgEngine.getInstance().getPlugin(
                "com.jonasreese.sound.sg.ui.defaultui.plugins.midimenu.MidiMenuFunctionality" );

        if (pl instanceof Functionality) {
            Functionality f = (Functionality) pl;
            // create popup menu
            try {
                editMidiEventsAction = (Action) f.getProperty( "editMidiEventsAction" );
                playMidiNoteAction = (Action) f.getProperty( "playMidiNoteAction" );
                insertMidiNoteAction = (SgAction) f.getProperty( "insertMidiNoteAction" );
                repeatMidiNoteAction = (Action) f.getProperty( "repeatMidiNoteAction" );
                setTempoAction = (Action) f.getProperty( "setTempoAction" );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // create menu for functions
        functionsMenu = new JMenu(
            rb.getString(
                "plugin.gridView.functionsMenu" ) );
        // add this action as double click action to the grid component
        gridComponent.addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent e ) {
                if (editMidiEventsAction != null && e.getClickCount() == 2) {
                    System.out.println( "doubleClick, activating editMidiEventsAction" );
                    if (editMidiEventsAction.isEnabled() &&
                        !getGridComponent().isSelectionEmpty()) {
                        editMidiEventsAction.actionPerformed( null );
                    }
                }
            }
        } );
        applyMinimumVolumeAction = new AbstractAction(
            rb.getString(
                "plugin.gridView.functionsMenu.applyMinimumVolume" ),
            new ResourceLoader( getClass(), "resource/min_volume.gif" ).getAsIcon() ) {

            static final long serialVersionUID = 0;
            public void actionPerformed( ActionEvent e ) {
                int defaultValue =
                    SgEngine.getInstance().getProperties().getPluginProperty(
                        GridController.this.plugin, "minVol", 127 );
                int vol = gridComponent.showSetMinimumVolumeDialog( defaultValue );
                if (vol >= 0) {
                    SgEngine.getInstance().getProperties().setPluginProperty(
                        GridController.this.plugin, "minVol", vol );
                }
            }
        };
        applyMinimumVolumeAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.gridView.functionsMenu.applyMinimumVolume.shortDescription" ) );
        functionsMenu.add( applyMinimumVolumeAction );
        applyMaximumVolumeAction = new AbstractAction(
            rb.getString(
                "plugin.gridView.functionsMenu.applyMaximumVolume" ),
            new ResourceLoader( getClass(), "resource/max_volume.gif" ).getAsIcon() ) {
            static final long serialVersionUID = 0;
            public void actionPerformed( ActionEvent e ) {
                int defaultValue =
                    SgEngine.getInstance().getProperties().getPluginProperty(
                        GridController.this.plugin, "maxVol", 127 );
                int vol = gridComponent.showSetMaximumVolumeDialog( defaultValue );
                if (vol >= 0) {
                    SgEngine.getInstance().getProperties().setPluginProperty(
                        GridController.this.plugin, "maxVol", vol );
                }
            }
        };
        applyMaximumVolumeAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.gridView.functionsMenu.applyMaximumVolume.shortDescription" ) );
        functionsMenu.add( applyMaximumVolumeAction );
        functionsMenu.addSeparator();
        increaseVolumeAction = new AbstractAction(
            rb.getString(
                "plugin.gridView.functionsMenu.increaseVolume" ),
            new ResourceLoader( getClass(), "resource/inc_volume.gif" ).getAsIcon() ) {
            static final long serialVersionUID = 0;
            public void actionPerformed( ActionEvent e ) {
                gridComponent.showIncreaseVolumeDialog(
                    true,
                    true,
                    1,
                    5,
                    rb.getString(
                        "plugin.gridView.increaseVolumeDialog" ),
                    rb.getString(
                        "plugin.gridView.increaseVolumeDialog.functionTitle" ),
                    rb.getString(
                        "plugin.gridView.increaseVolumeDialog.factor" ),
                    rb.getString(
                        "plugin.gridView.increaseVolumeDialog.constant" ) );
            }
        };
        increaseVolumeAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.gridView.functionsMenu.increaseVolume.shortDescription" ) );
        functionsMenu.add( increaseVolumeAction );
        decreaseVolumeAction = new AbstractAction(
            rb.getString(
                "plugin.gridView.functionsMenu.decreaseVolume" ),
            new ResourceLoader( getClass(), "resource/dec_volume.gif" ).getAsIcon() ) {
            static final long serialVersionUID = 0;
            public void actionPerformed( ActionEvent e ) {
                gridComponent.showDecreaseVolumeDialog(
                    true,
                    true,
                    1,
                    5,
                    rb.getString(
                        "plugin.gridView.decreaseVolumeDialog" ),
                    rb.getString(
                        "plugin.gridView.decreaseVolumeDialog.functionTitle" ),
                    rb.getString(
                        "plugin.gridView.decreaseVolumeDialog.factor" ),
                    rb.getString(
                        "plugin.gridView.decreaseVolumeDialog.constant" ) );
            }
        };
        decreaseVolumeAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.gridView.functionsMenu.decreaseVolume.shortDescription" ) );
        functionsMenu.add( decreaseVolumeAction );
        functionsMenu.addSeparator();

        quantizeAction = new AbstractAction(
            rb.getString(
                "plugin.gridView.functionsMenu.quantize" ),
            new ResourceLoader( getClass(), "resource/quantize.gif" ).getAsIcon() ) {
            static final long serialVersionUID = 0;
            public void actionPerformed( ActionEvent e ) {
                // TODO: Implement this
            }
        };
        quantizeAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.gridView.functionsMenu.quantize.shortDescription" ) );
        functionsMenu.add( quantizeAction );
        
        if (setTempoAction != null) {
            functionsMenu.add( setTempoAction );
        }
        functionsMenu.addSeparator();
        
        infoAction = new AbstractAction(
            rb.getString(
                "plugin.gridView.functionsMenu.info" ),
            UiToolkit.SPACER ) {
            static final long serialVersionUID = 0;
            public void actionPerformed( ActionEvent e ) {
                getGridComponent().showInformationDialog();
            }
        };
        functionsMenu.add( infoAction );
        
        // DEBUG CODE
//        Action findNextMidiEventAction = new AbstractAction(
//            /*rb.getString(
//                "plugin.gridView.functionsMenu.info" )*/"Noten ausgeben",
//            UiToolkit.SPACER ) {
//            static final long serialVersionUID = 0;
//            public void actionPerformed( ActionEvent e ) {
//                MidiEvent[] events = getGridComponent().getSelectedEvents();
//                for (int i = 0; i < events.length; i++) {
//                    String command = "<none>";
//                    String data1 = "<none>";
//                    String data2 = "<none>";
//                    String channel = "<none>";
//                    if (events[i].getMessage() instanceof ShortMessage) {
//                        data1 = Integer.toString( ((ShortMessage) events[i].getMessage()).getData1() );
//                        data2 = Integer.toString( ((ShortMessage) events[i].getMessage()).getData2() );
//                        channel = Integer.toString( ((ShortMessage) events[i].getMessage()).getChannel() );
//                        int cmd = ((ShortMessage) events[i].getMessage()).getCommand();
//                        if (cmd == ShortMessage.NOTE_ON) {
//                            command = "NOTE_ON";
//                        } else if (cmd == ShortMessage.NOTE_OFF) {
//                            command = "NOTE_OFF";
//                        } else {
//                            command = Integer.toString( cmd );
//                        }
//                    }
//                    System.out.println(
//                            "event[" + i + "] = " + events[i].getClass().getName() + events[i].hashCode() +
//                            ", tick = " + events[i].getTick() + ", command = " + command + " data1 = " +
//                            data1 + ", data2 = " + data2 + ", channel = " + channel );
//                }
//            }
//        };
        
        // add functionality to edit menu
        rectangularSelectionAction = new AbstractAction(
            rb.getString(
                "plugin.gridView.editMenu.selectOverAllEvents" ),
            UiToolkit.SPACER ) {
            static final long serialVersionUID = 0;
            public void actionPerformed( ActionEvent e ) {
                // toggle selection mode
                getGridComponent().setRectangularSelectionMode(
                    !getGridComponent().getRectangularSelectionMode() );
            }
        };
        rectangularSelectionAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString(
                        "plugin.gridView.editMenu.selectOverAllEvents.shortDescription" ) );
        rectangularSelectionButtons = new AbstractButton[2];
        rectangularSelectionButtons[0] = new JCheckBoxMenuItem( rectangularSelectionAction );
        rectangularSelectionButtons[1] = new JCheckBoxMenuItem( rectangularSelectionAction );
        
        // create context menu
        contextMenu = new JPopupMenu();
        if (editMidiEventsAction != null) {
            contextMenu.add( editMidiEventsAction );
        }
        if (playMidiNoteAction != null) {
            contextMenu.add( playMidiNoteAction );
        }
        if (insertMidiNoteAction != null) {
            contextMenu.add( insertMidiNoteAction );
        }
        if (repeatMidiNoteAction != null) {
            contextMenu.add( repeatMidiNoteAction );
            contextMenu.addSeparator();
        }
        contextMenu.add( snapToGridToggleMenuItems[1] );
        contextMenu.addSeparator();
        undoButton = new JMenuItem( sessionActionPool.getAction( SessionActionPool.UNDO ) );
        contextMenu.add( undoButton );
        redoButton = new JMenuItem( sessionActionPool.getAction( SessionActionPool.REDO ) );
        contextMenu.add( redoButton );
        contextMenu.addSeparator();
        contextMenu.add( sessionActionPool.getAction( SessionActionPool.CUT ) );
        contextMenu.add( sessionActionPool.getAction( SessionActionPool.COPY ) );
        contextMenu.add( sessionActionPool.getAction( SessionActionPool.PASTE ) );
        contextMenu.addSeparator();
        contextMenu.add( sessionActionPool.getAction( SessionActionPool.DELETE ) );
        contextMenu.add( sessionActionPool.getAction( SessionActionPool.SELECT_ALL ) );
        contextMenu.add( sessionActionPool.getAction( SessionActionPool.SELECT_NONE ) );
        contextMenu.add( sessionActionPool.getAction( SessionActionPool.INVERT_SELECTION ) );
        contextMenu.add( rectangularSelectionButtons[1] );
        //contextMenu.add( findNextMidiEventAction );
        contextMenu.addSeparator();
        contextMenu.add( sessionActionPool.getAction( SessionActionPool.PROPERTIES ) );
        
        gridComponent.addMouseListener( new DefaultPopupListener( contextMenu ) );
        
        // create some control components
        createTrackControls();


        applySettingsFromMidiDescriptor();
    }
    
    public Action getDefaultNoteMappingAction() {
        return defaultNoteMappingAction;
    }

    public Action getExportNoteMappingAction() {
        return exportNoteMappingAction;
    }

    public Action getImportNoteMappingAction() {
        return importNoteMappingAction;
    }

    public Action getEditNoteMappingAction() {
        return editNoteMappingAction;
    }
    
    public Action getExtractNoteMappingAction() {
        return extractNoteMappingAction;
    }

    private void updateEventTypeSelectedStates() {
        //System.out.println( "updateEventTypeSelectedStates" );
        TrackProxy track = gridComponent.getTrack();
        for (int i = 0; i < eventTypeItems.length; i++) {
            boolean sel = false;
            if (track != null) {
                if (track.getEventMap() != null) {
                    if (track.getEventMap().getIndexFor(
                            new ShortMessageEventDescriptor( null, null, eventTypeActions[i].type ) ) >= 0) {
                        sel = true;
                    }
                } else {
                    sel = true;
                }
            }
            eventTypeItems[i].setSelected( sel );
        }
    }
    
    /**
     * Gets an array of those <code>Track</code>s (contained by the MIDI sequence
     * held by the <code>MidiDescriptor</code> passed to the constructor) that can
     * be displayed and edited within a <code>GridComponent</code>.
     * @return A copied array of all <code>Track</code>s that can be used with
     *         the <code>GridComponent.setTrack(Track)</code> method. Please note that
     *         other tracks from the sequence can also be set, but cannot be displayed
     *         in a way that makes sense for the user.
     */

    /**
     * Creates and adds the UI track control elements.
     */
    private void createTrackControls() {
        TrackProxy[] tracks = getGridComponent().getEditableTracks();
        TrackWrapper[] tw = new TrackWrapper[tracks.length];
        for (int i = 0; i < tracks.length; i++) {
            tw[i] = new TrackWrapper( tracks[i], i );
        }

        if (tracksComboBox == null) {
            tracksComboBox = new JComboBox( tw );
            if (tracksComboBox.getItemCount() > 0) {
                tracksComboBox.setSelectedIndex( 0 );
            }
        } else {
            ActionListener[] al = tracksComboBox.getActionListeners();
            for (int i = 0; i < al.length; i++) {
                tracksComboBox.removeActionListener( al[i] );
            }
            TrackWrapper currSel = (TrackWrapper) tracksComboBox.getSelectedItem();
            tracksComboBox.removeAllItems();
            int selIndex = 0;
            for (int i = 0; i < tw.length; i++) {
                if (currSel != null && tw[i].track == currSel.track) {
                    selIndex = i;
                }
                tracksComboBox.addItem( tw[i] );
            }
            if (selIndex < tracksComboBox.getItemCount()) {
                tracksComboBox.setSelectedIndex( selIndex );
            } else if (tracksComboBox.getItemCount() > 0) {
                tracksComboBox.setSelectedIndex( 0 );
            }
        }
        ActionListener al = new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                try {
                    for (int i = 0; i < trackMenuItems.size(); i++) {
                        ((AbstractButton) trackMenuItems.get( i )).setSelected( false );
                    }
                    int index = ((JComboBox) e.getSource()).getSelectedIndex();
                    if (index >= 0) {
                        getGridComponent().setTrack(
                            ((TrackWrapper) ((JComboBox) e.getSource()).getItemAt( index )).track );
                        getGridComponent().requestFocus();
                        ((AbstractButton) trackMenuItems.get( index )).setSelected( true );
                        updateEventTypeSelectedStates();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog( getGridComponent(),
                        ex.getMessage(),
                        SgEngine.getInstance().getResourceBundle().getString(
                            "error.invalidMidiData" ),
                        JOptionPane.ERROR_MESSAGE );
                }
            }
        };
        tracksComboBox.addActionListener( al );
        Dimension d = tracksComboBox.getPreferredSize();
        tracksComboBox.setPreferredSize(
            new Dimension( 160, d.height ) );
        tracksSubMenu.removeAll();
        trackMenuItems = new ArrayList<JMenuItem>();
        for (int i = 0; i < tw.length; i++) {
            Action a = new SelectTrackAction(
                tw[i].track,
                tw[i].toString() );
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem( a );
            menuItem.setSelected( (i == tracksComboBox.getSelectedIndex()) );
            tracksSubMenu.add( menuItem );
            trackMenuItems.add( menuItem );
        }
        
        // enable/disable track-specific actions, depending on
        // wether a track is selected or not
        boolean sel = (tracksComboBox.getSelectedItem() != null);
        applyMinimumVolumeAction.setEnabled( sel );
        applyMaximumVolumeAction.setEnabled( sel );
        increaseVolumeAction.setEnabled( sel );
        decreaseVolumeAction.setEnabled( sel );
        setTempoAction.setEnabled( sel );
        infoAction.setEnabled( sel );
    }
    
    /**
     * Restores settings that have been saved int the MidiDescriptor.
     */
    private void applySettingsFromMidiDescriptor() {
        // check if we can restore some properties from the sessionElementDescriptor
        if (saveViewInSessionEnabled()) {
            String zoom =
                getGridComponent().getMidiDescriptor().getPersistentClientProperty( "grid.zoom" );
            if (zoom != null) {
                try {
                    getGridComponent().setZoom( Double.parseDouble( zoom ) );
                }
                catch (Exception ignored) {}
            }
            
            String rowHeight =
                getGridComponent().getMidiDescriptor().getPersistentClientProperty( "grid.rowHeight" );
            if (rowHeight != null) {
                try {
                    getGridComponent().setRowHeight( Integer.parseInt( rowHeight ) );
                } catch (Exception ignored) {}
            }
            
            String eventMap =
                getGridComponent().getMidiDescriptor().getPersistentClientProperty( "grid.eventmap" );
            if (eventMap != null) {
                importNoteMapping( eventMap, null );
            }

            String trackNum =
                getGridComponent().getMidiDescriptor().getPersistentClientProperty( "grid.track" );
            if (trackNum != null) {
                try {
                    int tn = Integer.parseInt( trackNum );
                    if (tn >= 0 && tn < tracksComboBox.getItemCount()) {
                        TrackWrapper tw = (TrackWrapper) tracksComboBox.getItemAt( tn );
                        getGridComponent().setTrack( tw.track );
                        tracksComboBox.setSelectedIndex( tn );
                    } else if (tracksComboBox.getItemCount() > 0) {
                        TrackWrapper tw = (TrackWrapper) tracksComboBox.getItemAt( 0 );
                        getGridComponent().setTrack( tw.track );
                        tracksComboBox.setSelectedIndex( 0 );
                    }
                } catch (Exception ignored) {}
            } else if (tracksComboBox != null && tracksComboBox.getItemCount() > 0) {
                TrackWrapper tw = (TrackWrapper) tracksComboBox.getItemAt( 0 );
                getGridComponent().setTrack( tw.track );
                tracksComboBox.setSelectedIndex( 0 );
            }
            
            String gridPainted =
                getGridComponent().getMidiDescriptor().getPersistentClientProperty( "grid.painted" );
            boolean gp = !"false".equals( gridPainted );
            getGridComponent().setGridPainted( gp );
            gridToggleButton.setSelected( gp );
            gridToggleMenuItem.setSelected( gp );

            String tacting =
                getGridComponent().getMidiDescriptor().getPersistentClientProperty( "grid.tacting" );
            if (tacting != null) {
                try {
                    getGridComponent().setGridDivisor( Integer.parseInt( tacting ) );
                } catch (Exception ignored) {}
            }
            
            String snapToGrid =
                getGridComponent().getMidiDescriptor().getPersistentClientProperty( "grid.snapToGrid" );
            boolean stg = "true".equals( snapToGrid );
            getGridComponent().setSnapToGridEnabled( stg );
            for (int i = 0; i < snapToGridToggleMenuItems.length; i++) {
                snapToGridToggleMenuItems[i].setSelected( stg );
            }
        }
    }
    
    
    
    /**
     * Monitors some property changes in the environment and updates the
     * <code>GridComponent</code> of important ones.
     */
    public void propertyChange( PropertyChangeEvent e ) {
        //System.out.println( "PropertyChangeEvent: " + e.getPropertyName() );
        if (e.getSource() instanceof SgProperties) {
            // check if plugin property changed
            String propPrefix = "plugin." + plugin.getClass().getName() + ".";
            if (e.getPropertyName().startsWith( propPrefix )) {
                String propName = e.getPropertyName().substring( propPrefix.length() );
                if ("backgroundColor".equals( propName )) {
                    gridComponent.setBackground( (Color) e.getNewValue() );
                } else if ("gridColor".equals( propName )) {
                    gridComponent.setGridColor( (Color) e.getNewValue() );
                } else if ("noteColor1".equals( propName )) {
                    gridComponent.setNoteColor1( (Color) e.getNewValue() );
                } else if ("noteColor2".equals( propName )) {
                    gridComponent.setNoteColor2( (Color) e.getNewValue() );
                } else if ("selectedNoteColor".equals( propName )) {
                    gridComponent.setSelectedNoteColor( (Color) e.getNewValue() );
                } else if ("eventColor".equals( propName )) {
                    gridComponent.setEventColor( (Color) e.getNewValue() );
                } else if ("selectedEventColor".equals( propName )) {
                    gridComponent.setSelectedEventColor( (Color) e.getNewValue() );
                } else if ("tactColor".equals( propName )) {
                    gridComponent.setTactColor( (Color) e.getNewValue() );
                } else if ("rowHeight".equals( propName )) {
                    int val = GridComponent.DEFAULT_ROW_HEIGHT;
                    if (e.getNewValue() != null) {
                        val = ((Integer) e.getNewValue()).intValue();
                    }
                    gridComponent.setRowHeight( val );
                } else if ("midiEventTickLength".equals( propName )) {
                    int val = GridComponent.DEFAULT_MIDI_EVENT_TICK_LENGTH;
                    if (e.getNewValue() != null) {
                        val = ((Integer) e.getNewValue()).intValue();
                    }
                    gridComponent.setMidiEventTickLength( val );
                } else if ("displayTactNumbers".equals( propName )) {
                    boolean val = true;
                    if (e.getNewValue() != null) {
                        val = ((Boolean) e.getNewValue()).booleanValue();
                    }
                    gridComponent.setDisplayTactNumbers( val );
                } else if ("toolTipsEnabled".equals( propName )) {
                    boolean val = true;
                    if (e.getNewValue() != null) {
                        val = ((Boolean) e.getNewValue()).booleanValue();
                    }
                    gridComponent.setToolTipsEnabled( val );
                } else if ("chaseCursor".equals( propName )) {
                    boolean val = true;
                    if (e.getNewValue() != null) {
                        val = ((Boolean) e.getNewValue()).booleanValue();
                    }
                    gridComponent.setChaseCursor( val );
                } else if ("chaseCursor.pagewise".equals( propName )) {
                    boolean val = true;
                    if (e.getNewValue() != null) {
                        val = ((Boolean) e.getNewValue()).booleanValue();
                    }
                    gridComponent.setChaseCursorPagewise( val );
                } else if ("doublebuffer".equals( propName )) {
                    boolean val = true;
                    if (e.getNewValue() != null) {
                        val = ((Boolean) e.getNewValue()).booleanValue();
                    }
                    gridComponent.setDoubleBuffered( val );
                }
            }
        } else if (e.getSource() instanceof MidiDescriptor) {
            // session change notification
            if ("changed".equals( e.getPropertyName() )) {
                gridComponent.updateTitle();
            } else if ("tempoInMpq".equals( e.getPropertyName() )) {
                updateTempo( getMidiDescriptor().getTempoInBpm(),
                    getMidiDescriptor().getTempoInMpq() );
            }
        } else if (e.getSource() instanceof GridComponent) {
            // store view properties in sessionElementDescriptor if this feature is enabled
            if ("zoom".equals( e.getPropertyName() ) && saveViewInSessionEnabled()) {
                Double zoom = new Double( getGridComponent().getZoom() );
                getGridComponent().getMidiDescriptor().putPersistentClientProperty(
                    "grid.zoom", zoom.toString() );
            } else if ("rowHeight".equals( e.getPropertyName() ) && saveViewInSessionEnabled()) {
                int rowHeight = getGridComponent().getRowHeight();
                getGridComponent().getMidiDescriptor().putPersistentClientProperty(
                     "grid.rowHeight", Integer.toString( rowHeight ) );
            } else if ("track".equals( e.getPropertyName() ) && saveViewInSessionEnabled()) {
                int trackIndex = 0;
                TrackProxy[] tracks = null;
                try {
                    tracks = getGridComponent().getMidiDescriptor().getSequence().getTrackProxies();
                } catch (InvalidMidiDataException e1) {
                } catch (IOException e1) {
                }
                if (tracks != null) {
                    for (int i = 0; i < tracks.length; i++) {
                        if (tracks[i] == getGridComponent().getTrack()) {
                            trackIndex = i;
                            break;
                        }
                    }
                }
                try {
                    gridComponent.getMidiDescriptor().getSequence().setSelectedTrackProxy(
                            gridComponent.getTrack(), gridComponent );
                } catch (InvalidMidiDataException ex) {
                } catch (IOException ex) {
                }
                getGridComponent().getMidiDescriptor().putPersistentClientProperty(
                    "grid.track", "" + trackIndex );
            } else if ("gridDivisor".equals( e.getPropertyName() ) && saveViewInSessionEnabled()) {
                getGridComponent().getMidiDescriptor().putPersistentClientProperty(
                    "grid.tacting", e.getNewValue().toString() );
            } else if ("snapToGridEnabled".equals( e.getPropertyName() ) && saveViewInSessionEnabled()) {
                getGridComponent().getMidiDescriptor().putPersistentClientProperty(
                    "grid.snapToGrid", e.getNewValue().toString() );
            } else if ("gridPainted".equals( e.getPropertyName() ) && saveViewInSessionEnabled()) {
                getGridComponent().getMidiDescriptor().putPersistentClientProperty(
                    "grid.painted", e.getNewValue().toString() );
            } else if ("rectangularSelectionMode".equals( e.getPropertyName() )) {
                boolean b = !((Boolean) e.getNewValue()).booleanValue();
                for (int i = 0; i < rectangularSelectionButtons.length; i++) {
                    if (rectangularSelectionButtons[i] != null) {
                        rectangularSelectionButtons[i].setSelected( b );
                    }
                }
            }
        }
    }
    
    private void updateTempo( float bpm, float mpq ) {
        if (tempoUpdatable != null) {
            tempoUpdatable.update( new float[] { bpm, mpq } );
        }
    }
    
    /**
     * Sets the tempo <code>Updatable</code>.
     * @param tempoUpdatable The tempo updatable. If the tempo of the current midi
     *        descriptor's sequence changes, the tempoUpdatable is invoked with a float
     *        array as object, where the first array element denotes the tempo in BPM
     *        and the second array element denotes the tempo in MPQ. May be <code>null</code>.
     */
    public void setTempoUpdatable( Updatable tempoUpdatable ) {
        this.tempoUpdatable = tempoUpdatable;
    }
    
    private boolean saveViewInSessionEnabled() {
        return SgEngine.getInstance().getProperties().getPluginProperty(
            plugin, "saveViewInSession", true );
    }

    private EventTypeAction[] createEventTypeActions() {
        EventTypeAction[] result = new EventTypeAction[] {
            new EventTypeAction( ShortMessage.ACTIVE_SENSING, EventMap.EVENT_NAME_MAP.get( ShortMessage.ACTIVE_SENSING ).toString() ),
            new EventTypeAction( ShortMessage.CHANNEL_PRESSURE, EventMap.EVENT_NAME_MAP.get( ShortMessage.CHANNEL_PRESSURE ).toString() ),
            new EventTypeAction( ShortMessage.CONTROL_CHANGE, EventMap.EVENT_NAME_MAP.get( ShortMessage.CONTROL_CHANGE ).toString() ),
            new EventTypeAction( ShortMessage.END_OF_EXCLUSIVE, EventMap.EVENT_NAME_MAP.get( ShortMessage.END_OF_EXCLUSIVE ).toString() ),
            new EventTypeAction( ShortMessage.MIDI_TIME_CODE, EventMap.EVENT_NAME_MAP.get( ShortMessage.MIDI_TIME_CODE ).toString() ),
            new EventTypeAction( ShortMessage.PITCH_BEND, EventMap.EVENT_NAME_MAP.get( ShortMessage.PITCH_BEND ).toString() ),
            new EventTypeAction( ShortMessage.POLY_PRESSURE, EventMap.EVENT_NAME_MAP.get( ShortMessage.POLY_PRESSURE ).toString() ),
            new EventTypeAction( ShortMessage.PROGRAM_CHANGE, EventMap.EVENT_NAME_MAP.get( ShortMessage.PROGRAM_CHANGE ).toString() ),
            new EventTypeAction( ShortMessage.SONG_POSITION_POINTER, EventMap.EVENT_NAME_MAP.get( ShortMessage.SONG_POSITION_POINTER ).toString() ),
            new EventTypeAction( ShortMessage.SONG_SELECT, EventMap.EVENT_NAME_MAP.get( ShortMessage.SONG_SELECT ).toString() ),
            new EventTypeAction( ShortMessage.START, EventMap.EVENT_NAME_MAP.get( ShortMessage.START ).toString() ),
            new EventTypeAction( ShortMessage.SYSTEM_RESET, EventMap.EVENT_NAME_MAP.get( ShortMessage.SYSTEM_RESET ).toString() ),
            new EventTypeAction( ShortMessage.TIMING_CLOCK, EventMap.EVENT_NAME_MAP.get( ShortMessage.TIMING_CLOCK ).toString() ),
            new EventTypeAction( ShortMessage.TUNE_REQUEST, EventMap.EVENT_NAME_MAP.get( ShortMessage.TUNE_REQUEST ).toString() ),
        };
        return result;
    }
    
    // opens a note mapping file
    private void importNoteMapping() {
        JFileChooser fileChooser = new JFileChooser( System.getProperty( "user.dir" ) );
        ExtensionFileFilter fileFilter = new ExtensionFileFilter(
                "xml", SgEngine.getInstance().getResourceBundle().getString( "file.xml" ), false );
        fileChooser.setFileFilter( fileFilter );
        fileChooser.setDialogTitle(
            SgEngine.getInstance().getResourceBundle().getString(
                "plugin.gridView.openNoteMapping" ) );
        int option = fileChooser.showOpenDialog( gridComponent.getParent() );
        if (option == JFileChooser.APPROVE_OPTION) {
            importNoteMapping( null, fileChooser.getSelectedFile() );
        }
    }
    
    private static class ImportEventMapEditImpl extends ImportEventMapEdit {
        private static final long serialVersionUID = 1L;
        public ImportEventMapEditImpl(EventMap eventMap, File binaryFile) {
            super(eventMap, binaryFile);
        }
        public ImportEventMapEditImpl(EventMap eventMap, URL xmlDescriptionUrl) {
            super(eventMap, xmlDescriptionUrl);
        }
        @Override
        public void perform() {
            try {
                if (binaryFile != null) {
                    byte[] b = FSTools.getFileContent( binaryFile );
                    eventMap.loadFromBinary( b );
                } else if (xmlDescriptionUrl != null) {
                    eventMap.loadFromXmlDescription( xmlDescriptionUrl );
                }
            } catch (Exception ex) {
                throw new RuntimeException( ex );
            }
        }
    }
    
    private void importNoteMapping( String urlString, File file ) {
        if (file == null) {
            return;
        }
        try {
            boolean binary = false;
            int lastIndex = file.getName().lastIndexOf( '.' );
            if (lastIndex < 0) {
                binary = true;
            } else {
                String extension = file.getName().substring( lastIndex + 1 );
                if (!"xml".equalsIgnoreCase( extension )) {
                    binary = true;
                }
            }
            EventMap eventMap = gridComponent.getTrack().getEventMap();
            ImportEventMapEditImpl edit;
            if (binary) {
                edit = new ImportEventMapEditImpl( eventMap, file );
            } else {
                URL url;
                if (file != null) { url = file.toURI().toURL(); }
                else { url = new URL( urlString ); }
                edit = new ImportEventMapEditImpl( eventMap, url );
            }
            edit.perform();
            gridComponent.getMidiDescriptor().getUndoManager().addEdit( edit );
            System.out.println( eventMap.toXml() );
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog( getGridComponent().getParent(),
                ex.getMessage(),
                SgEngine.getInstance().getResourceBundle().getString(
                    "error.errorOnOpeningNoteMappingFile" ),
                JOptionPane.ERROR_MESSAGE );
        }
    }
    
    private void exportNoteMapping() {
        JFileChooser fileChooser = new JFileChooser( System.getProperty( "user.dir" ) );
        fileChooser.setDialogTitle(
                SgEngine.getInstance().getResourceBundle().getString(
                        "plugin.gridView.saveNoteMapping" ) );
        ExtensionFileFilter fileFilter = new ExtensionFileFilter(
                "xml", SgEngine.getInstance().getResourceBundle().getString( "file.xml" ), false );
        fileChooser.setFileFilter( fileFilter );
        boolean loop = true;
        while (loop) {
            int option = fileChooser.showSaveDialog( getGridComponent().getParent() );
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String filename = file.getName().trim();
                boolean bin = false;
                if (filename.endsWith( "." )) {
                    bin = true;
                    filename = filename.substring( 0, filename.length() - 1 );
                } else if (filename.lastIndexOf( '.' ) < 0) {
                    filename += ".xml";
                } else {
                    String ext = filename.substring( filename.lastIndexOf( '.' ) + 1 );
                    if (!ext.equalsIgnoreCase( "xml" )) {
                        bin = true;
                    }
                }
                file = new File( file.getParentFile(), filename );
                boolean write = false;
                //System.out.println( "file is " + file.getAbsolutePath() + ", bin = " + bin );
                if (!file.exists()) {
                    loop = false;
                    write = true;
                } else {
                    String message =
                        SgEngine.getInstance().getResourceBundle().getString(
                            "object.saveAs.confirmOverwriteText" );
                    String title =
                        SgEngine.getInstance().getResourceBundle().getString(
                            "object.saveAs.confirmOverwriteTitle" );
                    int r = JOptionPane.showConfirmDialog(
                        getGridComponent().getParent(), message, title, JOptionPane.YES_NO_CANCEL_OPTION );
                    if (r == JOptionPane.YES_OPTION) {
                        loop = false;
                        write = true;
                    } else if (r == JOptionPane.CANCEL_OPTION) {
                        loop = false;
                        write = false;
                    }
                }
                
                if (!loop && write) {
                    TrackProxy track = getGridComponent().getTrack();
                    if (track != null) {
                        try {
                            if (bin) {
                                FSTools.writeToFile( file, track.getEventMap().toBinary() );
                            } else {
                                FSTools.writeToFile( file, track.getEventMap().toXml() );
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(
                                    getGridComponent().getParent(),
                                    ex.getMessage(),
                                    SgEngine.getInstance().getResourceBundle().getString(
                                        "error.saveFailed" ),
                                    JOptionPane.ERROR_MESSAGE );
                        }
                    }
                }
            } else {
                loop = false;
            }
        }
    }
    
    /**
     * Gets the actions for selecting/deselecting certain MIDI
     * event types for the GridComponent display.
     * @return An array containing the actions
     */
    public EventTypeAction[] getEventTypeActions()
    {
        return eventTypeActions;
    }
    
    /**
     * Gets the according function's <code>Action</code>.
     * @return An <code>Action</code> object.
     */
    public Action getInsertMidiNoteAction() { return insertMidiNoteAction; }
    
    /**
     * Gets the according function's <code>Action</code>.
     * @return An <code>Action</code> object.
     */
    public Action getRepeatMidiNoteAction() { return repeatMidiNoteAction; }
    
    /**
     * Gets the according function's <code>Action</code>.
     * @return An <code>Action</code> object.
     */
    public Action getEditMidiEventsAction() { return editMidiEventsAction; }
    
    /**
     * Gets the according function's <code>Action</code>.
     * @return An <code>Action</code> object.
     */
    public Action getApplyMinimumVolumeAction() { return applyMinimumVolumeAction; }
    
    /**
     * Gets the according function's <code>Action</code>.
     * @return An <code>Action</code> object.
     */
    public Action getApplyMaximumVolumeAction() { return applyMaximumVolumeAction; }
    
    /**
     * Gets the according function's <code>Action</code>.
     * @return An <code>Action</code> object.
     */
    public Action getIncreaseVolumeAction() { return increaseVolumeAction; }
    
    /**
     * Gets the according function's <code>Action</code>.
     * @return An <code>Action</code> object.
     */
    public Action getDecreaseVolumeAction() { return decreaseVolumeAction; }
    
    /**
     * Gets the according function's <code>Action</code>.
     * @return An <code>Action</code> object.
     */
    public Action getSetTempoAction() { return setTempoAction; }

    /**
     * Gets the midi descriptor.
     * @return The midi descriptor.
     */
    public MidiDescriptor getMidiDescriptor()
    {
        return gridComponent.getMidiDescriptor();
    }
    
    /**
     * Gets the grid component.
     * @return The grid component.
     */
    public GridComponent getGridComponent()
    {
        return gridComponent;
    }
    
    /**
     * Gets the grid's <code>JScrollPane</code>.
     * @return The <code>JScrollPane</code> the grid is placed in.
     */
    public JScrollPane getGridScrollPane()
    {
        return scrollPane;
    }
    
    /**
     * Scrolls the <code>JScrollPane</code> containing the <code>GridComponent</code>
     * so that events with the given tick number are displayed. 
     */
    public void scrollTickToVisible( long tick )
    {
        int x = gridComponent.translateTickX( tick );
        Rectangle rect = scrollPane.getVisibleRect();
        rect.x = x;
        scrollPane.scrollRectToVisible( rect );
    }

    /* (non-Javadoc)
     */
    public void midiEventSelectionUpdate( MidiEventSelectionEvent e ) {
        
        //System.out.println( "midiEventSelectionUpdate( " +
        //        e.getTrack().isAllSelected() + ", " + e.getTrack().getSelectedEventCount() + " events )" );
        if (editMidiEventsAction != null) {
            editMidiEventsAction.setEnabled( gridComponent.isEditMidiEventsPossible() );
        }
        
        gridComponent.midiEventSelectionUpdate( e );
        gridComponent.updateSelectionState();
        
        sessionActionPool.getAction( SessionActionPool.PROPERTIES ).setEnabled(
            !getGridComponent().isSelectionEmpty() );
    }

    /**
     * This method should be called at <code>GridVi.activate()</code>.
     */
    public void activate()
    {
        // set action receivers
        sessionActionPool.getAction( SessionActionPool.CUT ).setActionReceiver( new Updatable()
        {
            public void update( Object o )
            {
                copySelectedEvents();
                getGridComponent().deleteSelectedEvents();
            }
        } );
        sessionActionPool.getAction( SessionActionPool.COPY ).setActionReceiver( new Updatable()
        {
            public void update( Object o )
            {
                copySelectedEvents();
            }
        } );
        sessionActionPool.getAction( SessionActionPool.PASTE ).setActionReceiver( new Updatable()
        {
            public void update( Object o )
            {
                pasteFromClipboard();
            }
        } );
        sessionActionPool.getAction( SessionActionPool.DELETE ).setActionReceiver( new Updatable()
        {
            public void update( Object o )
            {
                getGridComponent().deleteSelectedEvents();
            }
        } );
        sessionActionPool.getAction( SessionActionPool.SELECT_ALL ).setActionReceiver( new Updatable()
        {
            public void update( Object o )
            {
                getGridComponent().selectAll();
            }
        } );
        sessionActionPool.getAction( SessionActionPool.SELECT_NONE ).setActionReceiver( new Updatable()
        {
            public void update( Object o )
            {
                getGridComponent().clearSelection();
            }
        } );
        sessionActionPool.getAction( SessionActionPool.INVERT_SELECTION ).setActionReceiver( new Updatable()
        {
            public void update( Object o )
            {
                getGridComponent().invertSelection();
            }
        } );
        sessionActionPool.getAction( SessionActionPool.PROPERTIES ).setActionReceiver( new Updatable()
        {
            public void update( Object o )
            {
                // TODO: change to properties dialog here!
                getGridComponent().showInformationDialog();
            }
        } );
        sessionActionPool.getAction( SessionActionPool.PROPERTIES ).setEnabled(
            !getGridComponent().isSelectionEmpty() );
        
        if (insertMidiNoteAction != null) {
            insertMidiNoteAction.setActionReceiver( new Updatable() {
                public void update( Object o ) {
                    gridComponent.startNoteAddMode();
                }
            });
        }
        
        getGridComponent().updateSelectionState();
        
        // add own menues/menu items to menu bar
        final JMenuBar menuBar = UiToolkit.getSessionUi(
            getGridComponent().getMidiDescriptor().getSession() ).getMenuBar();
        final JMenu editMenu = menuBar.getMenu( 1 );
        if (menuBar != null && editMenu != null) {
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        editMenu.add( rectangularSelectionButtons[0] );
                        menuBar.add( gridMenu, 4 );
                        menuBar.add( functionsMenu, 5 );
                        menuBar.validate();
                    }
                } );
        }
        
        getGridComponent().requestFocus();
        try {
            gridComponent.getMidiDescriptor().getSequence().setSelectedTrackProxy(
                    gridComponent.getTrack(), gridComponent );
        } catch (InvalidMidiDataException e) {
        } catch (IOException e) {
        }
    }

    /**
     * This method should be called at <code>GridVi.deactivate()</code>.
     */
    public void deactivate() {
        // remove own menues from menu bar
        SessionUi sessionUi = UiToolkit.getSessionUi(
            getGridComponent().getMidiDescriptor().getSession() );
        if (sessionUi != null) {
            final JMenuBar menuBar = sessionUi.getMenuBar();
            final JMenu editMenu = menuBar.getMenu( 1 );
            if (menuBar != null && editMenu != null) {
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        editMenu.remove( rectangularSelectionButtons[0] );
                        menuBar.remove( gridMenu );
                        menuBar.remove( functionsMenu );
                        menuBar.validate();
                    }
                } );
            }
        }
        if (insertMidiNoteAction != null) {
            insertMidiNoteAction.setActionReceiver( null );
        }
    }
    
    /**
     * This method shall be called from <code>GridVi.open()</code>.
     */
    public void open() {
        TrackProxy[] tracks = null;
        try {
            tracks = gridComponent.getMidiDescriptor().getSequence().getTrackProxies();
            for (int i = 0; i < tracks.length; i++) {
                tracks[i].addMidiEventSelectionListener( this );
            }
        } catch (InvalidMidiDataException e1) {
        } catch (IOException e1) {
        }

        // this is required for UNDO and REDO
        ((UndoAction) sessionActionPool.getAction( SessionActionPool.UNDO )).addButton( undoButton );
        ((UndoAction) sessionActionPool.getAction( SessionActionPool.REDO )).addButton( redoButton );

        getMidiDescriptor().addPropertyChangeListener( this );
        try {
            getMidiDescriptor().getSequence().addMidiChangeMonitor( this );
        } catch (InvalidMidiDataException e) {
        } catch (IOException e) {
        }
        getGridComponent().updateTitle();
        getMidiDescriptor().getMidiRecorder().addMidiUpdatable( getGridComponent() );
        SgEngine.getInstance().getProperties().addPropertyChangeListener( this );
        getGridComponent().requestFocus();
    }

    /**
     * This method shall be called from <code>GridVi.close()</code>.
     */
    public void close()
    {
        TrackProxy[] tracks = null;
        try {
            tracks = gridComponent.getMidiDescriptor().getSequence().getTrackProxies();
            for (int i = 0; i < tracks.length; i++) {
                tracks[i].removeMidiEventSelectionListener( this );
            }
        } catch (InvalidMidiDataException e1) {
        } catch (IOException e1) {
        }

        deactivate();
        // this is required for UNDO and REDO
        ((UndoAction) sessionActionPool.getAction( SessionActionPool.UNDO )).removeButton( undoButton );
        ((UndoAction) sessionActionPool.getAction( SessionActionPool.REDO )).removeButton( redoButton );

        getMidiDescriptor().removePropertyChangeListener( this );
        try {
            getMidiDescriptor().getSequence().removeMidiChangeMonitor( this );
        } catch (InvalidMidiDataException e) {
        } catch (IOException e) {
        }
        getMidiDescriptor().getMidiRecorder().removeMidiUpdatable( gridComponent );
        SgEngine.getInstance().getProperties().removePropertyChangeListener( this );
    }

    /**
     * Copies the selected events to the clipboard.
     */
    private void copySelectedEvents()
    {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        MidiEvent[] selectedEvents = getGridComponent().getSelectedEvents();
        int resolution = 48; // a default value
        Sequence seq = null;
        try {
            seq = getGridComponent().getMidiDescriptor().getSequence();
        } catch (InvalidMidiDataException e) {
        } catch (IOException e) {
        }
        if (seq != null) { resolution = seq.getResolution(); }
        MidiTransferable transferable = new MidiTransferable(
            new MidiSerializer( selectedEvents, resolution ) );
//        DataFlavor[] dfs = transferable.getTransferDataFlavors();
//        for (int i = 0; i < dfs.length; i++)
//        {
//            System.out.println( dfs[i] );
//        }
        clipboard.setContents( transferable, transferable );
    }
    
    /**
     * Pastes the events from the clipboard to the <code>GridComponent</code>.
     */
    private void pasteFromClipboard()
    {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable t = clipboard.getContents( this );
        DataFlavor df = new DataFlavor( MidiSerializer.class, null );
        if (t.isDataFlavorSupported( df ))
        {
            try
            {
                MidiSerializer ms = (MidiSerializer) t.getTransferData( df );
                MidiEvent[] events = ms.getMidiEvents();
                // translate events into new resolution
                int resolution = ms.getResolution();
                Sequence seq = null;
                try {
                    seq = getGridComponent().getMidiDescriptor().getSequence();
                } catch (InvalidMidiDataException e) {
                }
                if (seq != null) { resolution = seq.getResolution(); }
                MidiToolkit.translateResolutions( ms.getResolution(), resolution, events );
                // start paste mode with translated events
                getGridComponent().startPasteMidiEventsMode( events );
            }
            catch (UnsupportedFlavorException ignored) {} // shall not occur
            catch (IOException ioex)
            {
                JOptionPane.showMessageDialog( getGridComponent(),
                    ioex.getMessage(),
                    SgEngine.getInstance().getResourceBundle().getString(
                        "error.invalidMidiData" ),
                    JOptionPane.ERROR_MESSAGE );
            }
        }
        else
        {
            System.out.println( "*** CLIPBOARD DATA NOT SUPPORTED: " + df.getDefaultRepresentationClass() );
        }
    }

    /**
     * Gets the track selection combo box.
     * @return The combo box.
     */
    public JComboBox getTracksComboBox()
    {
        return tracksComboBox;
    }
    
    /**
     * Gets the button that toggles the "grid painted" state
     * on/off.
     * @return The button.
     */
    public AbstractButton getGridToggleButton()
    {
        return gridToggleButton;
    }
    
    class SelectTrackAction extends AbstractAction
    {
        static final long serialVersionUID = 0;
        TrackProxy t;
        SelectTrackAction( TrackProxy t, String title )
        {
            super( title, UiToolkit.SPACER );
            this.t = t;
        }
        public void actionPerformed( ActionEvent e )
        {
            try
            {
                getGridComponent().setTrack( t );
                JComboBox cb = getTracksComboBox();
                for (int i = 0; i < cb.getItemCount(); i++)
                {
                    if (((TrackWrapper) cb.getItemAt( i )).track == t)
                    {
                        cb.setSelectedIndex( i );
                        break;
                    }
                }
            }
            catch (Exception ex)
            {
                JOptionPane.showMessageDialog( getGridComponent(),
                    ex.getMessage(),
                    SgEngine.getInstance().getResourceBundle().getString(
                        "error.invalidMidiData" ),
                    JOptionPane.ERROR_MESSAGE );
            }
        }
    }
    
    class ShowGridAction extends AbstractAction {
        static final long serialVersionUID = 0;
        ShowGridAction( String title, String text ) {
            super( title, UiToolkit.SPACER );
            putValue( SgAction.TOOL_TIP_TEXT, text );
        }
        
        public void actionPerformed( ActionEvent e ) {
            boolean b = !getGridComponent().isGridPainted();
            getGridComponent().setGridPainted( b );
            gridToggleButton.setSelected( b );
            gridToggleMenuItem.setSelected( b );
        }
    }

    class SnapToGridAction extends AbstractAction {
        static final long serialVersionUID = 0;
        SnapToGridAction( String title, String text ) {
            super( title, UiToolkit.SPACER );
            putValue( SgAction.TOOL_TIP_TEXT, text );
        }
        
        public void actionPerformed( ActionEvent e ) {
            boolean b = !getGridComponent().isSnapToGridEnabled();
            getGridComponent().setSnapToGridEnabled( b );
            for (int i = 0; i < snapToGridToggleMenuItems.length; i++) {
                snapToGridToggleMenuItems[i].setSelected( b );
            }
        }
    }

    class TrackWrapper {
        TrackProxy track;
        String name;
        TrackWrapper( TrackProxy track, int trackNum ) {
            this.track = track;
            name = track.getTrackName();
            if (name == null) {
                name = SgEngine.getInstance().getResourceBundle().getString(
                    "track" ) + " " + (trackNum + 1);
            }
        }
        public String toString() { return name; }
    }
    
    public class EventTypeAction extends AbstractAction {
        static final long serialVersionUID = 0;
        int type;
        int index;
        EventDescriptor eventDescriptor;
        
        EventTypeAction( int type, String name ) {
            super( name, UiToolkit.SPACER );
            this.type = type;
            eventDescriptor = null;
            index = -1;
        }

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed( ActionEvent e ) {
            TrackProxy track = gridComponent.getTrack();
            if (track != null) {
                EventMap eventMap = track.getEventMap();
                int index = eventMap.getIndexFor( new ShortMessageEventDescriptor( null, null, type ) );
                SgUndoableEdit edit;
                if (index >= 0) {
                    eventDescriptor = eventMap.getEventAt( index );
                    edit = new RemoveEventMapEdit( eventMap, index );
                    this.index = index;
                } else {
                    if (eventDescriptor == null) {
                        eventDescriptor =
                            new ShortMessageEventDescriptor(
                                    EventMap.EVENT_NAME_MAP.get( "" + type ).toString(), eventMap, type );
                    }
                    edit = new InsertEventMapEdit( eventMap, eventDescriptor, this.index < 0 ? 0 : this.index );
                }
                edit.perform();
                gridComponent.getMidiDescriptor().getUndoManager().addEdit( edit );
            }
		}
    }
    
    class ZoomMenuItem extends JCheckBoxMenuItem implements ActionListener, PropertyChangeListener {
        static final long serialVersionUID = 0;
        double zoom;
        ZoomMenuItem( double zoom ) {
            super( "" + ((int) (zoom * 100.0)) + "%", UiToolkit.SPACER );
            setSelected( gridComponent.getZoom() == zoom );
            gridComponent.addPropertyChangeListener( "zoom", this );
            this.zoom = zoom;
            addActionListener( this );
        }
        
        public void actionPerformed( ActionEvent e ) {
            if (gridComponent.getZoom() != zoom) {
               gridComponent.setZoom( zoom );
            }
        }

        public void propertyChange( PropertyChangeEvent e ) {
            double zoom = ((Double) e.getNewValue()).doubleValue();
            setSelected( (zoom == this.zoom) );
        }
    }

    public void midiEventsAdded(
        SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj ) {
        if (events.length == 1 &&
            MidiToolkit.isTrackNameEvent(events[0])) {
            createTrackControls();
        }

        getGridComponent().midiEventsAdded( sequence, track, events, changeObj );
    }

    public void midiEventsRemoved(
        SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj ) {
        if (events.length == 1 && MidiToolkit.isTrackNameEvent(events[0])) {
            createTrackControls();
        }
        
        getGridComponent().midiEventsRemoved( sequence, track, events, changeObj );
    }

    public void midiEventsChanged( SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj ) {
        System.out.println( "midiEventsChanged()" );
        //System.out.println( "midiEventsChanged()" );
        if (events.length == 1 &&
            MidiToolkit.isTrackNameEvent( events[0] )) {
            createTrackControls();
        }
        
        getGridComponent().midiEventsChanged( sequence, track, events, changeObj );
    }
    
    public void midiTrackAdded( SgMidiSequence sequence, TrackProxy track, Object changeObj ) {
        track.addMidiEventSelectionListener( this );
        //System.out.println( "midiTrackAdded() : " + MidiToolkit.getTrackName( track.getTrack() ) );
        createTrackControls();
        if (changeObj instanceof GridEditSource &&
            ((GridEditSource) changeObj).isResponsible( getGridComponent() )) {
            int index = sequence.getIndexOf( track );
            if (index >= 0) {
                tracksComboBox.setSelectedIndex( index );
            }
        }
    }

    public void midiTrackRemoved( SgMidiSequence sequence, TrackProxy track, Object changeObj ) {
        track.removeMidiEventSelectionListener( this );
        createTrackControls();
        
        TrackWrapper trw = (TrackWrapper) tracksComboBox.getSelectedItem();
        if (trw != null && trw.track != gridComponent.getTrack()) {
            gridComponent.setTrack( trw.track );
        }
    }

    public void midiTrackLengthChanged( SgMidiSequence sequence, TrackProxy track, Object changeObj ) {
        getGridComponent().midiTrackLengthChanged( sequence, track, changeObj );
    }
    
    public void midiTrackEventMapChanged( SgMidiSequence sequence, TrackProxy track, Object changeObj ) {
        //System.out.println( "midiTrackEventMapChanged()" );
        gridComponent.midiTrackEventMapChanged( sequence, track, changeObj );
        updateEventTypeSelectedStates();
    }
}