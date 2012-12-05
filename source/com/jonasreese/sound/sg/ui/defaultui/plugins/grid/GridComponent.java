/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 18.10.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.grid;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import com.jonasreese.sound.sg.RecorderException;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.EventDescriptor;
import com.jonasreese.sound.sg.midi.EventMap;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.MidiEventSelectionEvent;
import com.jonasreese.sound.sg.midi.MidiRecorder;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.midi.MidiUpdatable;
import com.jonasreese.sound.sg.midi.NoteDescriptor;
import com.jonasreese.sound.sg.midi.SgMidiSequence;
import com.jonasreese.sound.sg.midi.ShortMessageEventDescriptor;
import com.jonasreese.sound.sg.midi.TrackProxy;
import com.jonasreese.sound.sg.midi.edit.AddEventsEdit;
import com.jonasreese.sound.sg.midi.edit.AddTrackEdit;
import com.jonasreese.sound.sg.midi.edit.ChangeEventsEdit;
import com.jonasreese.sound.sg.midi.edit.DeleteEventsEdit;
import com.jonasreese.sound.sg.midi.edit.MoveEventsEdit;
import com.jonasreese.sound.sg.midi.edit.RemoveTrackEdit;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.ui.defaultui.SessionActionPool;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <b>
 * The grid's (visible) component implementation.
 * </b>
 * @author jreese
 */
public class GridComponent extends JComponent implements Scrollable, MidiUpdatable {
    
    private static final long serialVersionUID = 1;
    
    private static EventMap defaultEventMap = null;
    
    
    // default values
    public static final int DEFAULT_ROW_HEIGHT = 12;
    public static final int DEFAULT_MIDI_EVENT_TICK_LENGTH = 5;
    public static final int LENGTH_DRAGAREA_WIDTH = 100;
    
    // edit modes
    public static final int EDIT_MODE_DEFAULT = 0;
    public static final int EDIT_MODE_RESIZE_WEST = 1;
    public static final int EDIT_MODE_RESIZE_EAST = 2;
    public static final int EDIT_MODE_ADD = 1024;
    public static final int EDIT_MODE_PASTE = 1025;
    
    private static Map<String,Color> defaultColors;
    private static String[] defaultColorKeys;
    
    private Object invalidEditSource = new Object();
    
    private double zoom;
    private double zoomCorrection;
    private MidiDescriptor midiDescriptor;
    private TrackProxy track;
    private int rowHeight;
    private int midiEventTickLength;
    private boolean gridPainted;
    private boolean drawFineGrid;
    private boolean snapToGrid;
    private boolean chaseCursor;
    private boolean chaseCursorPagewise;
    private boolean rectangularSelectionMode;
    private int gridDivisor;
    private Color pointerColor;
    private Color leftMarkerColor;
    private Color rightMarkerColor;
    private Color gridColor;
    private Color dragAreaColor;
    private Color dragAreaBorderColor;
    private boolean addSelectionMode;
    private boolean removeSelectionMode;
    private boolean eventsMoved;
    private MoveEventsEdit moveEventsEdit;
    private EventComponent[] movedEventComponents;
    private EventComponent[] taggedEventComponents;
    private boolean showNoteEditDialog;

    private Map<MidiEvent,EventComponent> eventComponentMap;
    
    private Dimension prefSize = new Dimension();
    private Rectangle r = new Rectangle();
    private int pos = 0;
    private int leftMarkerPos = -1;
    private int rightMarkerPos = -1;
    private Rectangle dragArea;
    private Rectangle paintedDragArea;
    private Point mousePressedPoint;
    private EventComponent mousePressedEventComponent;
    private boolean removeDragArea;
    private boolean popupTriggered;
    private boolean clearSelectionOnMouseRelease;
    
    private RulerComponent ruler;
    private EventMappingComponent eventMappingComponent;
    
    private Cursor addSelectionCursor;
    private Cursor removeSelectionCursor;
    private Cursor strikethroughCursor;
    
    private Color noteColor1;
    private Color noteColor2;
    private Color selectedNoteColor;
    private Color eventColor;
    private Color selectedEventColor;
    private Color tactColor;
    private boolean displayTactNumbers;
    private boolean toolTipsEnabled;
    
    private int editMode;
    private Object editObject;
    
    static {
        // apply default settings maps
        defaultColorKeys = new String[] {
            "backgroundColor",
            "gridColor",
            "noteColor1",
            "noteColor2",
            "selectedNoteColor",
            "eventColor",
            "selectedEventColor",
            "tactColor",
        };
        defaultColors = new HashMap<String,Color>();
        int c = 0;
        defaultColors.put( defaultColorKeys[c++], Color.WHITE );
        defaultColors.put( defaultColorKeys[c++], new Color( 235, 235, 235 ) );
        defaultColors.put( defaultColorKeys[c++], new Color( 153, 153, 255 ) );
        defaultColors.put( defaultColorKeys[c++], new Color( 0, 0, 102 ) );
        defaultColors.put( defaultColorKeys[c++], Color.MAGENTA );
        defaultColors.put( defaultColorKeys[c++], new Color( 150, 100, 80 ) );
        defaultColors.put( defaultColorKeys[c++], Color.PINK );
        defaultColors.put( defaultColorKeys[c++], new Color( 188, 188, 188 ) );
    }
    
    /**
     * Gets the keys that can be used to identify default color settings.
     * @return A <code>String</code> array containing all keys.
     */
    public static String[] getDefaultColorKeys() {
        return defaultColorKeys;
    }
    
    /**
     * Gets the default color with the given identifier name.
     * @param key The key to identify the default color.
     * @return The default color, or <code>null</code> if the identifier
     *         is not mapped to a default color.
     */
    public static Color getDefaultColor( String key ) {
        return (Color) defaultColors.get( key );
    }
    
    
    /**
     * Constructs a new <code>GridComponent</code>.
     * @param midiDescriptor The <code>MidiDescriptor</code> describing the MIDI
     *        object within this <code>GridComponent</code>. It is assumed that
     *        the <code>getSequence()</code> method of <code>midiDescriptor</code>
     *        returns a non-<code>null</code> value.
     */
    public GridComponent( MidiDescriptor midiDescriptor ) {
        this.midiDescriptor = midiDescriptor;

        // defaults
        zoom = 1.0;
        gridDivisor = 16;
        gridPainted = true;
        drawFineGrid = true;
        rowHeight = DEFAULT_ROW_HEIGHT;
        midiEventTickLength = DEFAULT_MIDI_EVENT_TICK_LENGTH;
        mousePressedPoint = null;
        removeDragArea = false;
        popupTriggered = false;
        clearSelectionOnMouseRelease = false;
        addSelectionMode = false;
        removeSelectionMode = false;
        eventsMoved = false;
        displayTactNumbers = true;
        toolTipsEnabled = true;
        chaseCursor = true;
        chaseCursorPagewise = true;
        rectangularSelectionMode = true;
        
        eventComponentMap = new HashMap<MidiEvent,EventComponent>();
        
        editMode = EDIT_MODE_DEFAULT;
        
        ruler = new RulerComponent();
        
        // calculate zoom correction
        calcZoomCorrection();
        
        // configure component settings
        dragArea = null;
        setLayout( null );
        setFocusable( true );

        // colors
        setBackground( getDefaultColor( "backgroundColor" ) );
        pointerColor = new Color( 255, 0, 0, 100 ); // red pointer with some transparency
        leftMarkerColor = Color.GREEN;
        rightMarkerColor = Color.ORANGE;
        dragAreaColor = new Color( 0, 50, 240, 60 );
        dragAreaBorderColor = new Color( 80, 80, 255, 200 );
        gridColor = getDefaultColor( "gridColor" );
        noteColor1 = getDefaultColor( "noteColor1" );
        noteColor2 = getDefaultColor( "noteColor2" );
        selectedNoteColor = getDefaultColor( "selectedNoteColor" );
        eventColor = getDefaultColor( "eventColor" );
        selectedEventColor = getDefaultColor( "selectedEventColor" );
        tactColor = getDefaultColor( "tactColor" );
        
        super.setAutoscrolls( true );
        
        // create cursors
        addSelectionCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            new ResourceLoader( getClass(), "resource/cursor_add_selection.gif" ).getAsImage(),
                new Point( 0, 0 ), "addSelectionCursor" );
        removeSelectionCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            new ResourceLoader( getClass(), "resource/cursor_remove_selection.gif" ).getAsImage(),
                new Point( 0, 0 ), "removeSelectionCursor" );
        strikethroughCursor = null;
        try {
            strikethroughCursor = Cursor.getSystemCustomCursor( "Invalid.32x32" );
        } catch (HeadlessException e) {
            e.printStackTrace();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        leftMarkerPos = translateTickX( midiDescriptor.getMidiRecorder().getLeftMarkerTick() );
        rightMarkerPos = translateTickX( midiDescriptor.getMidiRecorder().getRightMarkerTick() );
        
        enableEvents(
            AWTEvent.MOUSE_MOTION_EVENT_MASK |
            AWTEvent.MOUSE_EVENT_MASK |
            AWTEvent.KEY_EVENT_MASK );
    }
    
    /**
     * This method calculates the correcting value for the zoom.
     * The zoom correction depends on the MIDI resolution.
     */
    private void calcZoomCorrection() {
        zoomCorrection = 1.0;
        SgMidiSequence seq = null;
        try {
            seq = midiDescriptor.getSequence();
        } catch (InvalidMidiDataException e) {
        } catch (IOException e) {
        }
        if (seq != null) {
            System.out.println( "midi res = " + seq.getResolution() );
            zoomCorrection = (double) (40.0 / (double) seq.getResolution());
            if (zoomCorrection < 0.005) {
                zoomCorrection = 0.005;
            }
        }
    }
    
    /**
     * Enables/disables the rectangular selection mode.
     * @param rectangularSelectionMode If set to <code>true</code>, the user
     *        selects within a rectangular area, otherwise the selection will
     *        span over the whole component height.
     */
    public void setRectangularSelectionMode( boolean rectangularSelectionMode ) {
        if (this.rectangularSelectionMode == rectangularSelectionMode) {
            return;
        }
        this.rectangularSelectionMode = rectangularSelectionMode;
        firePropertyChange(
            "rectangularSelectionMode", !rectangularSelectionMode, rectangularSelectionMode );
    }
    
    /**
     * Gets the current <code>rectangularSelectionMode</code> property.
     * @return <code>true</code>, if the user selects within a rectangular area,
     *         <code>false</code> if the selection will span over the whole component height.
     */
    public boolean getRectangularSelectionMode() {
        return rectangularSelectionMode;
    }
    
    /**
     * Enables/disables the MIDI event/note tooltips.
     * @param toolTipsEnabled If set to <code>true</code>, tool tips will be displayed,
     *        otherwise not.
     */
    public void setToolTipsEnabled( boolean toolTipsEnabled ) {
        if (toolTipsEnabled == this.toolTipsEnabled) { return; }
        this.toolTipsEnabled = toolTipsEnabled;
        Component[] components = getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof EventComponent) {
                if (toolTipsEnabled) {
                    ToolTipManager.sharedInstance().registerComponent( (JComponent) components[i] );
                } else {
                    ToolTipManager.sharedInstance().unregisterComponent( (JComponent) components[i] );
                }
            }
        }
        firePropertyChange( "toolTipsEnabled", !toolTipsEnabled, toolTipsEnabled );
    }
    
    /**
     * This is incorrect english... Nevertheless, this method
     * returns the current tool tips enabled state.
     * @return <code>true</code> if tool tips are enabled for MIDI events/notes,
     *         <code>false</code> otherwise.
     */
    public boolean isToolTipsEnabled() {
        return toolTipsEnabled;
    }
    
    /**
     * Sets the <code>display tact numbers</code> property.
     * @param displayTactNumbers If set to <code>true</code>, tact numbers
     *        will be painted on the grid, otherwise not.
     */
    public void setDisplayTactNumbers( boolean displayTactNumbers ) {
        if (displayTactNumbers == this.displayTactNumbers) { return; }
        this.displayTactNumbers = displayTactNumbers;
        ruler.repaint();
        firePropertyChange(
            "displayTactNumbers", !displayTactNumbers, displayTactNumbers );
    }
    
    /**
     * Gets the <code>display tact numbers</code> property.
     * @return <code>true</code> if tact numbers are painted on the grid,
     *         <code>false</code> otherwise.
     */
    public boolean isDisplayingTactNumbers() {
        return displayTactNumbers;
    }
    
    /**
     * Sets the display length for MIDI events that are <b>not</b> note events.
     * Use this so that MIDI system events can be displayed with a larger width
     * that one tick. This is a visual setting only and does not affect MIDI events
     * or sequences in any way.
     * @param midiEventTickLength The number of ticks to use for MIDI event width
     *        calculation. Must be &gt;= 1.
     */
    public void setMidiEventTickLength( int midiEventTickLength ) {
        if (this.midiEventTickLength == midiEventTickLength) { return; }
        int oldVal = this.midiEventTickLength;
        this.midiEventTickLength = midiEventTickLength;
        Component[] components = getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof ShortMessageComponent) {
                components[i].setSize(
                    translateTickWidth( midiEventTickLength ), components[i].getHeight() );
            }
        }
        firePropertyChange( "midiEventTickLength", oldVal, midiEventTickLength );
    }
    
    /**
     * Gets the display length for MIDI events that are <b>not</b> note events.
     * @return The number of ticks to use for MIDI event width calculation.
     */
    public int getMidiEventTickLength() {
        return midiEventTickLength;
    }
    
    /**
     * Sets the color in which the grid lines shall be painted.
     * @param gridColor The grid color to set.
     */
    public void setGridColor( Color gridColor ) {
        Color oldVal = this.gridColor;
        this.gridColor = gridColor;
        repaint();
        ruler.repaint();
        firePropertyChange( "gridColor", oldVal, gridColor );
    }
    
    /**
     * Gets the color in which the grid lines are painted.
     * @return The grid color.
     */
    public Color getGridColor() {
        return gridColor;
    }
    
    /**
     * Sets the color in which the whole-tacted grid lines shall be painted.
     * @param tactColor The tact color to set.
     */
    public void setTactColor( Color tactColor ) {
        Color oldVal = this.tactColor;
        this.tactColor = tactColor;
        repaint();
        ruler.repaint();
        firePropertyChange( "tactColor", oldVal, tactColor );
    }
    
    /**
     * Gets the color in which the whole-tacted grid lines are painted.
     * @return The grid color.
     */
    public Color getTactColor() {
        return tactColor;
    }
    
    /**
     * Sets the color in which MIDI notes (lowest volume) shall be painted.
     * @param noteColor1 The note color 1.
     */
    public void setNoteColor1( Color noteColor1 ) {
        Color oldVal = this.noteColor1;
        this.noteColor1 = noteColor1;
        repaint();
        firePropertyChange( "tactColor", oldVal, noteColor1 );
    }
    
    /**
     * Gets the color in which MIDI notes (lowest volume) are painted.
     * @return The note color.
     */
    public Color getNoteColor1() {
        return noteColor1;
    }
    
    /**
     * Sets the color in which MIDI notes (highest volume) shall be painted.
     * @param noteColor1 The note color 2.
     */
    public void setNoteColor2( Color noteColor2 ) {
        Color oldVal = this.noteColor2;
        this.noteColor2 = noteColor2;
        repaint();
        firePropertyChange( "tactColor", oldVal, noteColor2 );
    }
    
    /**
     * Gets the color in which MIDI notes (highest volume) are painted.
     * @return The note color 2.
     */
    public Color getNoteColor2() {
        return noteColor2;
    }
    
    /**
     * Sets the color in which selected MIDI notes.
     * @param selectedNoteColor The selected note color.
     */
    public void setSelectedNoteColor( Color selectedNoteColor ) {
        Color oldVal = this.selectedNoteColor;
        this.selectedNoteColor = selectedNoteColor;
        repaint();
        firePropertyChange( "selectedNoteColor", oldVal, selectedNoteColor );
    }
    
    /**
     * Gets the color in which selected MIDI notes are painted.
     * @return The selected note color.
     */
    public Color getSelectedNoteColor() {
        return selectedNoteColor;
    }
    
    /**
     * Sets the color in which MIDI events shall be painted.
     * @param eventColor The event color.
     */
    public void setEventColor( Color eventColor ) {
        Color oldVal = this.eventColor;
        this.eventColor = eventColor;
        repaint();
        firePropertyChange( "eventColor", oldVal, eventColor );
    }
    
    /**
     * Gets the color in which MIDI events are painted.
     * @return The event color.
     */
    public Color getEventColor() {
        return eventColor;
    }
    
    /**
     * Sets the color in which selected MIDI events shall be painted.
     * @param selectedEventColor The selected event color.
     */
    public void setSelectedEventColor( Color selectedEventColor ) {
        Color oldVal = this.selectedEventColor;
        this.selectedEventColor = selectedEventColor;
        repaint();
        firePropertyChange( "selectedEventColor", oldVal, selectedEventColor );
    }
    
    /**
     * Gets the color in which selected MIDI events are painted.
     * @return The selected event color.
     */
    public Color getSelectedEventColor() {
        return selectedEventColor;
    }
    
    public void setBackground( Color background ) {
        super.setBackground( background );
        ruler.setBackground( background );
    }
    
    /**
     * Gets this <code>GridComponent</code>'s <code>MidiDescriptor</code>.
     * @return The midi descriptor.
     */
    public MidiDescriptor getMidiDescriptor() {
        return midiDescriptor;
    }
    
    /**
     * Updates the title
     */
    public void updateTitle() {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                ViewContainer vc = (ViewContainer) UiToolkit.getViewContainer( GridComponent.this );
                if (vc != null) {
                    vc.setTitleText( midiDescriptor.getName() + (midiDescriptor.isChanged() ? "*" : "") );
                }
            }
        } );
    }
    
    /**
     * This method is called when the selection changes
     * (events added to / removed from the selection).
     */
    public void updateSelectionState() {
        boolean ne = false;
        if (track != null) {
            ne = !track.isSelectionEmpty();
        }
        
        SessionActionPool sessionActionPool =
            UiToolkit.getSessionUi( midiDescriptor.getSession() ).getActionPool();
        sessionActionPool.getAction( SessionActionPool.CUT ).setEnabled( ne );
        sessionActionPool.getAction( SessionActionPool.COPY ).setEnabled( ne );
        sessionActionPool.getAction( SessionActionPool.DELETE ).setEnabled( ne );
    }
    
    /**
     * Called by the <code>GridController</code> when the midi event selection
     * has been updated.
     * @param e The <code>MidiEventSelectionEvent</code>.
     */
    void midiEventSelectionUpdate( MidiEventSelectionEvent e ) {
        if (e.getTrack() == getTrack()) {
            MidiEvent[] events = e.getMidiEvents();
            synchronized (eventComponentMap) {
                Rectangle r = new Rectangle();
                if (e.isSelectionCleared()) {
                    for (EventComponent ec : eventComponentMap.values()) {
                        ec.setSelected( false, r );
                    }
                } else if (e.isAllSelected()) {
                    for (EventComponent ec : eventComponentMap.values()) {
                        ec.setSelected( true, r );
                    }
                } else if (e.isAddedToSelection()) {
                    for (int i = 0; i < events.length; i++) {
                        EventComponent ec = eventComponentMap.get( events[i] );
                        if (ec != null) {
                            ec.setSelected( true, r );
                        }
                    }
                } else if (e.isRemovedFromSelection()) {
                    for (int i = 0; i < events.length; i++) {
                        EventComponent ec = eventComponentMap.get( events[i] );
                        if (ec != null) {
                            ec.setSelected( false, r );
                        }
                    }
                }
                repaint( r );
            }
        }
    }

    private void updateEventComponentBounds() {
        Component[] components = getComponents();
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof EventComponent) {
                ((EventComponent) components[i]).adjustBounds();
            }
        }

        super.revalidate();;
        ruler.revalidate();
    }

    /**
     * Sets the height (in pixels) for a single row (note line).
     * @param rowHeight The total pixel height for a row.
     */
    public void setRowHeight( int rowHeight ) {
        if (this.rowHeight == rowHeight) { return; }
        int oldVal = this.rowHeight;
        this.rowHeight = rowHeight;
        updateEventComponentBounds();
        if (eventMappingComponent != null) {
            eventMappingComponent.updateLayout();
            java.awt.Container parent = getParent();
            if (parent != null && parent.getParent() instanceof JComponent) {
                ((JComponent) parent.getParent()).repaint();
            }
        }

        firePropertyChange( "rowHeight", oldVal, rowHeight );
    }

    /**
     * Gets the height (in pixels) for a single row (note line).
     * @return The total height in pixels.
     */
    public int getRowHeight() {
        return rowHeight;
    }

    /**
     * Gets the (horizontal) ruler component that should be used with this
     * <code>GridComponent</code>.
     * @return The ruler component, as a <code>JComponent</code>.
     */
    public JComponent getRulerComponent() {
        return ruler;
    }
    
    EventMap getEventMap() {
        if (track == null) {
            if (defaultEventMap == null) {
                defaultEventMap = TrackProxy.createDefaultEventMap();
            }
            return defaultEventMap;
        }
        return track.getEventMap();
    }
    
    /**
     * Sets the <code>EventMappingComponent</code>.
     * @param eventMappingComponent A non-<code>null</code>
     *        <code>EventMappingComponent</code>.
     */
    public void setEventMappingComponent( EventMappingComponent eventMappingComponent ) {
        this.eventMappingComponent = eventMappingComponent;
    }
    
    /**
     * Gets the MIDI event mapping component that should be used with this
     * <code>GridComponent</code>. The event mapping component should be the
     * vertical ruler component.
     * @return The event mapping component.
     */
    public EventMappingComponent getEventMappingComponent() {
        return eventMappingComponent;
    }
    
    protected void processKeyEvent( KeyEvent e ) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                if (editMode == EDIT_MODE_ADD) {
                    stopNoteAddMode();
                } else if (editMode == EDIT_MODE_PASTE) {
                    stopPasteMidiEventsMode();
                } else if (eventsMoved) {
                    // abort event moving: create MoveEventsEdit and undo() it
                    moveEventsEdit.abort();
                    eventsMoved = false;
                    movedEventComponents = null;
                }
            } else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                addSelectionMode = true;
                try {
                    setCursor( addSelectionCursor );
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                removeSelectionMode = true;
                try {
                    setCursor( removeSelectionCursor );
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteSelectedEvents();
            } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                MidiRecorder midiRecorder = midiDescriptor.getMidiRecorder();
                if (midiRecorder != null) {
                    if (midiRecorder.isPlaying()) {
                        midiRecorder.stop();
                    } else {
                        // record
                        if (e.isControlDown()) {
                            try {
                                midiRecorder.record();
                            } catch (RecorderException rex) {
                                JOptionPane.showMessageDialog(
                                        getParent(),
                                        rex.getMessage(),
                                        SgEngine.getInstance().getResourceBundle().getString(
                                            "player.errorOnRecord" ),
                                        JOptionPane.ERROR_MESSAGE );
                                }
                            e.consume();
                        } else { // play
                            try {
                                long tick = midiRecorder.getTickPosition();
                                if (midiRecorder.getRightMarkerTick() <= tick) {
                                    tick = 0;
                                }
                                if (tick < midiRecorder.getLeftMarkerTick()) {
                                    tick = midiRecorder.getLeftMarkerTick();
                                }
                                midiRecorder.setTickPosition( tick );
                                midiRecorder.playToRightMarker();
                            } catch (RecorderException ignored) {
                                ignored.printStackTrace();
                            }
                        }
                    }
                }
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                MidiRecorder midiRecorder = midiDescriptor.getMidiRecorder();
                if (midiRecorder != null) {
                    midiRecorder.startFastBackward();
                    e.consume();
                }
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                MidiRecorder midiRecorder = midiDescriptor.getMidiRecorder();
                if (midiRecorder != null) {
                    midiRecorder.startFastForward();
                    e.consume();
                }
            } else if (e.getKeyCode() == KeyEvent.VK_HOME) {
                MidiRecorder midiRecorder = midiDescriptor.getMidiRecorder();
                if (midiRecorder != null) {
                    long tick = midiRecorder.getTickPosition();
                    midiRecorder.jumpToLeftMarker();
                    if (midiRecorder.getTickPosition() == tick) {
                        midiRecorder.setTickPosition( 0 );
                    }
                    e.consume();
                }
            } else if (e.getKeyCode() == KeyEvent.VK_END) {
                MidiRecorder midiRecorder = midiDescriptor.getMidiRecorder();
                if (midiRecorder != null) {
                    long tick = midiRecorder.getTickPosition();
                    midiRecorder.jumpToRightMarker();
                    if (midiRecorder.getTickPosition() == tick) {
                        midiRecorder.jumpToEnd();
                    }
                    e.consume();
                }
            } else if (e.getKeyCode() == KeyEvent.VK_L) {
                if (e.isControlDown()) {
                    MidiRecorder midiRecorder = midiDescriptor.getMidiRecorder();
                    if (midiRecorder != null) {
                        midiRecorder.setLeftMarkerTick( midiRecorder.getTickPosition() );
                        e.consume();
                    }
                }
            } else if (e.getKeyCode() == KeyEvent.VK_R) {
                if (e.isControlDown()) {
                    MidiRecorder midiRecorder = midiDescriptor.getMidiRecorder();
                    if (midiRecorder != null) {
                        midiRecorder.setRightMarkerTick( midiRecorder.getTickPosition() );
                        e.consume();
                    }
                }
            }
        } else if (e.getID() == KeyEvent.KEY_RELEASED) {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
                addSelectionMode = false;
                setCursor( Cursor.getDefaultCursor() );
            } else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                removeSelectionMode = false;
                setCursor( Cursor.getDefaultCursor() );
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                MidiRecorder midiRecorder = midiDescriptor.getMidiRecorder();
                if (midiRecorder != null) {
                    midiRecorder.stopFastBackward();
                    e.consume();
                }
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                MidiRecorder midiRecorder = midiDescriptor.getMidiRecorder();
                if (midiRecorder != null) {
                    midiRecorder.stopFastForward();
                    e.consume();
                }
            }
        }
        
        super.processKeyEvent( e );
    }
    
    /**
     * Tags all selected <code>EventComponent</code>s.
     */
    private synchronized void tagSelection() {
        if (taggedEventComponents != null) {
            untagSelection();
        }
        taggedEventComponents = getSelectedEventComponents();
        if (taggedEventComponents != null) {
            for (int i = 0; i < taggedEventComponents.length; i++) {
                taggedEventComponents[i].tagged = true;
            }
        }
    }
    
    private synchronized void untagSelection() {
        if (taggedEventComponents != null) {
            for (int i = 0; i < taggedEventComponents.length; i++) {
                taggedEventComponents[i].tagged = false;
            }
            taggedEventComponents = null;
        }
    }
    
    /**
     * Translates the given point on the given component into an absolute
     * Point on this GridComponent.
     * @param px The x coordinate.
     * @param py The y coordinate.
     * @param c The component (must be child component of this)
     * @return The absolute position.
     */
    private Point getAbsolutePoint( int px, int py, Component c ) {
        int x = px + c.getX();
        int y = py + c.getY();
        while (c != this) {
            c = c.getParent();
            x += c.getX();
            y += c.getY();
        }
        Rectangle r = getVisibleRect();
        return new Point( x + r.x, y + r.y );
    }
    
    @SuppressWarnings("unchecked")
    protected void processMouseEvent( MouseEvent e ) {
        // "mouse pressed"-operations
        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            if (addSelectionMode || removeSelectionMode) {
                tagSelection();
            }
            // remember mouse pressed position
            mousePressedPoint = e.getPoint();
            mousePressedEventComponent = null;
            if (e.getSource() != this) {
                Component c = (Component) e.getSource();
                if (c instanceof EventComponent) {
                    mousePressedEventComponent = (EventComponent) c;
                }
            }

            if (editMode < EDIT_MODE_ADD) {
                eventsMoved = false;
                movedEventComponents = null;
                
                if (!hasFocus()) { requestFocus(); }
                popupTriggered = e.isPopupTrigger();
                if (e.getSource() instanceof EventComponent) {
                    EventComponent ec = (EventComponent) e.getSource();
                    if (removeSelectionMode) {
                        deselectEventComponent( ec );
                    } else {
                        if (!addSelectionMode) {
                            if (ec.isSelected()) {
                                clearSelectionOnMouseRelease = true;
                            } else {
                                clearSelection();
                            }
                        }
                        selectEventComponent( ec );
                    }
                    ec.repaint();
                } else {
                    if (!addSelectionMode) {
                        clearSelection();
                    }
                }
            } else if (editMode == EDIT_MODE_ADD) {
                EventDescriptor ed = translateYPos( e.getY() );
                if (ed != null) {
                    showNoteEditDialog = false;
                    try {
                        if (ed instanceof NoteDescriptor) {
                            ShortMessage onMsg = new ShortMessage();
                            ShortMessage offMsg = new ShortMessage();
                            int note = ((NoteDescriptor) ed).getNote();
                            long tick = translateXPos( e.getX(), snapToGrid );
                            MidiEvent event = track.getShortMessageNoteOnEventNextTo( tick, true );
                            int channel;
                            int volume;
                            if (event == null || (!(event.getMessage() instanceof ShortMessage))) {
                                showNoteEditDialog = true;
                                channel = 0;
                                volume = 64;
                            } else {
                                ShortMessage sm = (ShortMessage) event.getMessage();
                                channel = sm.getChannel();
                                volume = sm.getData2();
                                System.out.println( "volume = " + volume );
                            }
                            onMsg.setMessage( ShortMessage.NOTE_ON, channel, note, volume );
                            offMsg.setMessage( ShortMessage.NOTE_ON, channel, note, 0 );
                            MidiEvent on = new MidiEvent( onMsg, tick );
                            MidiEvent off = new MidiEvent( offMsg, tick + 5 );
                            editObject = addNoteComponent( on, off, false );
                        } else if (ed instanceof ShortMessageEventDescriptor) {
                            ShortMessage msg = new ShortMessage();
                            int status = ((ShortMessageEventDescriptor) ed).getStatus();
                            if (MidiToolkit.isChannelMessageStatusByte( status )) {
                                msg.setMessage( status, 0, 0, 0 );
                            } else {
                                msg.setMessage( status, 0, 0 );
                            }
                            showNoteEditDialog = true;
                            long tick = translateXPos( e.getX() );
                            MidiEvent event = new MidiEvent( msg, tick );
                            editObject = addShortMessageEventComponent( event, false );
                        }
                    } catch (InvalidMidiDataException imdex) {
                        imdex.printStackTrace();
                    }
                }
            }  else if (editMode == EDIT_MODE_PASTE) { // edit mode is EDIT_MODE_PASTE - put moved events here
                if (editObject instanceof List) {
                    List<EventComponent> list = (List<EventComponent>) editObject;
                    stopPasteMidiEventsMode();
                    if (!list.isEmpty()) {
                        MidiEvent[] events = getEventsFromEventComponentCollection( list );
                        addMidiEvents( events, new GridEditSource( GridEditSource.PASTE, hashCode(), null ) );
                    }
                }
            }
        } else if (e.getID() == MouseEvent.MOUSE_RELEASED) {// "mouse released"-operations
            if (editMode < EDIT_MODE_ADD) {
                if (dragArea != null) {
                    if (popupTriggered || e.isPopupTrigger()) {
                        popupTriggered = true;
                    } else {
                    }
                    removeDragArea = true;
                    repaintDragArea();
                }
                
                // mouse released on an event component
                if (e.getSource() instanceof EventComponent) {
                    EventComponent ec = (EventComponent) e.getSource();

                    // user changed size of an event component
                    if (mousePressedPoint != null &&
                        (e.getX() != mousePressedPoint.x ||
                            e.getY() != mousePressedPoint.y) && 
                        (editMode == EDIT_MODE_RESIZE_EAST ||
                            editMode == EDIT_MODE_RESIZE_WEST) &&
                        editObject == ec &&
                        ec instanceof NoteComponent) {
                        
                        NoteComponent nc = (NoteComponent) ec;
                        GridEditSource ges =
                            new GridEditSource( GridEditSource.CHANGE, hashCode(), null );
                        ChangeEventsEdit edit = new ChangeEventsEdit(
                            track,
                            new MidiEvent[] { nc.event, nc.noteOff },
                            midiDescriptor,
                            SgEngine.getInstance().getResourceBundle().getString(
                                ((editMode == EDIT_MODE_RESIZE_EAST) ?
                                    "edit.changeNoteLength" :
                                    "edit.changeNoteLengthAndPosition") ),
                            ges );

                        if (editMode == EDIT_MODE_RESIZE_WEST) {
                            // set note tick position
                            ec.event.setTick( translateXPos( ec.getX() ) );
                        } else {
                            // set note length
                            if (nc.noteOff != null && nc.noteOff != nc.event) {
                                long diff = Math.max( 1, translateXPos( nc.getWidth() ) );
                                nc.noteOff.setTick( nc.event.getTick() + diff );
                            }
                        }

                        edit.perform();
                        midiDescriptor.getUndoManager().addEdit( edit );
                        ec.adjustBounds();
                    }

                    // user selected a single event within a selection
                    if (clearSelectionOnMouseRelease && !e.isPopupTrigger()) {
                        clearSelection();
                        selectEventComponent( ec );
                        clearSelectionOnMouseRelease = false;
                    }
                }

                // user moved events
                if (eventsMoved) {
                    if (mousePressedEventComponent != null) {
                        moveEventsEdit.perform(); // move 'real' MIDI events
                        midiDescriptor.getUndoManager().addEdit( moveEventsEdit );
                        if (track != null) {
                            track.selectAll( moveEventsEdit.getEvents(),
                                    new GridEditSource( GridEditSource.MOVE, hashCode(), null ) );
                        }
                    }
                }
            } else if (editMode == EDIT_MODE_ADD) { // else : edit mode is EDIT_MODE_ADD
                // add midi notes that were created for graphical display to track
                if (editObject instanceof EventComponent) {
                    EventComponent ec = (EventComponent) editObject;
                    MidiEvent[] events = new MidiEvent[ec.getEventCount()];
                    for (int i = 0; i < events.length; i++) {
                        events[i] = ec.getEventAt( i );
                    }
                    remove( ec ); // since addMidiEvents() creates new component, remove old one
                    for (int i = 0; i < ec.getEventCount(); i++) {
                        eventComponentMap.remove( ec.getEventAt( i ) );
                    }
                    addMidiEvents( events );

                    if (showNoteEditDialog) {
                        if (ec instanceof ShortMessageComponent && events.length == 1) {
                            UiToolkit.showEditShortMessageEventDialog( events[0], track, midiDescriptor );
                        } else {
                            UiToolkit.showEditNoteDialog( events, track, midiDescriptor );
                        }
                    }
                }
                stopNoteAddMode();
            }  else if (editMode == EDIT_MODE_PASTE) { // else : edit mode is EDIT_MODE_PASTE - should not happen, stop!
                stopPasteMidiEventsMode();
            }
            untagSelection();
        }
        super.processMouseEvent( e );
    }
    
    @SuppressWarnings("unchecked")
    protected void processMouseMotionEvent( MouseEvent e ) {
        if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
            if (editMode < EDIT_MODE_ADD) {
                // drag selected event(s) to another position
                if (e.getSource() instanceof EventComponent) {
                    // distinguish between edit modes
                    if (editMode == EDIT_MODE_DEFAULT) {
                        clearSelectionOnMouseRelease = false;
                        if (mousePressedEventComponent != null) {
                            // nothing moved, yet
                            Point p = getAbsolutePoint( e.getX(), e.getY(), mousePressedEventComponent );
                            long tick = translateXPos( p.x - mousePressedPoint.x, snapToGrid );
                            if (!eventsMoved) {
                                movedEventComponents = getSelectedEventComponents();
                                moveEventsEdit = new MoveEventsEdit(
                                        track,
                                        getEventsFromEventComponentArray( movedEventComponents ),
                                        midiDescriptor,
                                        new GridEditSource( GridEditSource.MOVE, hashCode(), null ) );
                            }
                            moveEventComponentsTo(
                                    movedEventComponents,
                                    mousePressedEventComponent,
                                    tick, e.getY() );
                            eventsMoved = true;
                        }
                    } else if (editMode == EDIT_MODE_RESIZE_WEST) {
                        if (editObject instanceof NoteComponent) {
                            NoteComponent nc = (NoteComponent) editObject;
                            int oldX = nc.getX();
                            nc.setLocation(
                                    translateTickX(
                                            translateXPos( nc.getX() + e.getX(), snapToGrid ) ),
                                    nc.getY() );
                            nc.setSize( nc.getWidth() + oldX - nc.getX(), nc.getHeight() );
                        }
                    } else if (editMode == EDIT_MODE_RESIZE_EAST) {
                        if (editObject instanceof NoteComponent) {
                            NoteComponent nc = (NoteComponent) editObject;
                            nc.setSize(
                                    translateTickX( translateXPos( e.getX() + 1, snapToGrid ) ),
                                    nc.getHeight() );
                        }
                    }
                } else {
                    if (e.isControlDown() && !addSelectionMode) {
                        addSelectionMode = true;
                        tagSelection();
                    }
                    if (e.isShiftDown() && !removeSelectionMode) {
                        removeSelectionMode = true;
                        tagSelection();
                    }
                    if (dragArea == null) {
                        int x;
                        int y;
                        if (mousePressedPoint != null) {
                            x = mousePressedPoint.x;
                            y = mousePressedPoint.y;
                        } else {
                            x = e.getX();
                            y = e.getY();
                        }
                        dragArea = new Rectangle(
                            x, rectangularSelectionMode ? y : 0,
                            0, rectangularSelectionMode ? 0 : getHeight() );
                    } else {
                        dragArea.width = e.getX() - dragArea.x;
                        dragArea.height =
                            rectangularSelectionMode ? (e.getY() - dragArea.y) : getHeight();
                    }
                    if (!getVisibleRect().contains( e.getX(), e.getY() )) {
                        scrollRectToVisible( new Rectangle( e.getX(), e.getY(), 1, 1 ) );
                    }
                    repaintDragArea();
                }
            } else if (editMode == EDIT_MODE_ADD) { // else : edit mode is EDIT_MODE_ADD
                if (editObject instanceof NoteComponent) {
                    NoteComponent nc = (NoteComponent) editObject;
                    long offset = translateXPos( e.getX() - mousePressedPoint.x, snapToGrid );
                    if (offset < 1) { offset = 1; }
                    nc.noteOff.setTick( nc.event.getTick() + offset );
                    nc.adjustBounds();
                } else if (editObject instanceof ShortMessageComponent) {
                    ShortMessageComponent sc = (ShortMessageComponent) editObject;
                    long tick = translateXPos( e.getX() );
                    sc.event.setTick( tick );
                    sc.adjustBounds();
                }
            }
        } else if (e.getID() == MouseEvent.MOUSE_MOVED) {
            if (editMode == EDIT_MODE_PASTE) {
                clearSelectionOnMouseRelease = false;
                // moves the selected event component on the screen
                // take first selected eventcomponent as lead selection
                if (editObject instanceof List) {
                    List<EventComponent> list = (List<EventComponent>) editObject;
                    if (!list.isEmpty()) {
                        EventComponent lead = list.get( 0 );
                        Point p = getAbsolutePoint(  e.getX(), e.getY(), e.getComponent() );
                        long tick = translateXPos( p.x, snapToGrid );
                        moveEventComponentsTo( list, lead, tick, p.y - lead.getY() );
                    }
                }
            }
        }
        super.processMouseMotionEvent( e );
    }
    
    /**
     * Handles the painting when the mouse is dragged over the
     * <code>GridComponent</code>.
     */
    private void repaintDragArea() {
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;
        if (dragArea != null) {
            x = dragArea.x;
            y = dragArea.y;
            width = dragArea.width;
            height = dragArea.height;
        }
        if (width < 0) {
            x += width;
            width = -width;
        }
        if (height < 0) {
            y += height;
            height = -height;
        }

        Rectangle r = new Rectangle( x, y, width, height );

        if (dragArea != null) {
            Rectangle da = new Rectangle( r );
            for (int i = 0; i < getComponentCount(); i++) {
                Component c = getComponent( i );
                if (c instanceof EventComponent) {
                    EventComponent ec = (EventComponent) c;
                    if (da.intersects( c.getBounds() )) {
                        if (removeSelectionMode) {
                            if (ec.tagged) {
                                deselectEventComponent( ec );
                            }
                        } else {
                            selectEventComponent( ec );
                        }
                        r = r.union( c.getBounds() );
                    } else {
                        boolean b = true;
                        if (removeSelectionMode) {
                            if (ec.tagged) {
                                selectEventComponent( ec );
                            }
                        } else {
                            if (addSelectionMode && ec.tagged) {
                                selectEventComponent( ec );
                            } else {
                                b = deselectEventComponent( ec );
                            }
                        }
                        if (b) {
                            r = r.union( c.getBounds() );
                        }
                    }
                }
            }
        }
        
        if (paintedDragArea != null) {
            r = paintedDragArea.union( r );
        }
        repaint( r.x, r.y, r.width, r.height );
    }
    
    /**
     * Sets the zoom factor. Default is 1.0
     * @param zoom The zoom value.
     */
    public void setZoom( double zoom ) {
        if (this.zoom == zoom) { return; }

        drawFineGrid = true;
        
        Rectangle visibleRect = new Rectangle( getVisibleRect() );
        long tick;
        // if mouse cursor is over GridComponent, center it after zooming
        Point p = getMousePosition( true );
        if (p != null) {
            tick = translateXPos( visibleRect.x + p.x );
        } else if (pos >= visibleRect.x &&
            pos <= visibleRect.x + visibleRect.width) {
            // otherwise, if MIDI pointer is visible, then we center it after zooming
            tick = midiDescriptor.getMidiRecorder().getTickPosition();
        } else { // otherwise, simply center the current center point after zooming
            tick = translateXPos( visibleRect.x + visibleRect.width / 2 );
        }

        Double oldVal = new Double( this.zoom );
        this.zoom = zoom;
        if (rowHeight < 3) { rowHeight = 3; }

        pos = translateTickX( midiDescriptor.getMidiRecorder().getTickPosition() );
        leftMarkerPos = translateTickX( midiDescriptor.getMidiRecorder().getLeftMarkerTick() );
        rightMarkerPos = translateTickX( midiDescriptor.getMidiRecorder().getRightMarkerTick() );

        firePropertyChange( "zoom", oldVal, new Double( this.zoom ) );

        updateEventComponentBounds();
        visibleRect.x = translateTickX( tick ) - visibleRect.width / 2;
        if (visibleRect.x < 0) { visibleRect.x = 0; }
        scrollRectToVisible( visibleRect );
        repaint();
        getRulerComponent().repaint();
    }
    
    /**
     * Gets the current zoom factor.
     * @return The current zoom factor.
     */
    public double getZoom() {
        return zoom;
    }
    
    /**
     * Sets the <code>chaseCursor</code> property.
     * @param chaseCursor If set to <code>true</code>, the player cursor
     *        will be chased by this <code>GridComponent</code>, otherwise
     *        it will not be chased and the user is free to navigate within
     *        the grid while the player is running.
     */
    public void setChaseCursor( boolean chaseCursor ) {
        if (this.chaseCursor == chaseCursor) {
            return;
        }
        this.chaseCursor = chaseCursor;
        firePropertyChange( "chaseCursor", !chaseCursor, chaseCursor );
    }
    
    /**
     * Gets the <code>chaseCursor</code> property.
     * @return <code>true</code> if the player cursor is chased by this
     *        <code>GridComponent</code>. Otherwise it will not be chased
     *        and the user is free to navigate within
     *        the grid while the player is running.
     */
    public boolean isChasingCursor() {
        return chaseCursor;
    }
    
    /**
     * Sets the <code>chaseCursorPagewise</code> property.
     * @param pagewise If set to <code>true</code>, the player cursor
     *        will be chased page by page if <code>isChasingCursor()</code>
     *        returns <code>true</code>. Otherwise, the cursor will either
     *        not be chased or it will strictly be chased, meaning that
     *        the view port is scrolled on every cursor update.
     */
    public void setChaseCursorPagewise( boolean pagewise ) {
        if (this.chaseCursorPagewise == pagewise) {
            return;
        }
        this.chaseCursorPagewise = pagewise;
        firePropertyChange( "chaseCursorPagewise", !pagewise, pagewise );
    }
    
    /**
     * Gets the <code>chaseCursorPagewise</code> property.
     * @return <code>true</code> if the player cursor is chased page by page
     *        if <code>isChasingCursor()</code> returns <code>true</code>.
     *        Otherwise, the cursor will either not be chased or it will
     *        strictly be chased, meaning that the view port is scrolled
     *        on every cursor update.
     */
    public boolean isChasingCursorPagewise() {
        return chaseCursorPagewise;
    }
    
    /**
     * Sets the <code>gridPainted</code> property.
     * @param gridPainted If set to <code>true</code>, the
     *        MIDI grid is painted, otherwise it is not painted.
     */
    public void setGridPainted( boolean gridPainted ) {
        if (this.gridPainted == gridPainted) { return; }
        this.gridPainted = gridPainted;
        repaint();
        firePropertyChange( "gridPainted", !gridPainted, gridPainted );
    }
    
    /**
     * Gets the <code>gridPainted</code> property.
     * @return <code>true</code> if the MIDI grid is painted,
     *         <code>false</code> otherwise.
     */
    public boolean isGridPainted() {
        return gridPainted;
    }
    
    /**
     * Sets the 'Snap to grid' enabled state.
     * @param snapToGrid <code>true</code> if the user shall experience a
     * 'Snap to grid' behavior when moving MIDI event, <code>false</code> otherwise.
     */
    public void setSnapToGridEnabled( boolean snapToGrid ) {
        if (this.snapToGrid == snapToGrid) {
            return;
        }
        this.snapToGrid = snapToGrid;
        firePropertyChange( "snapToGridEnabled", !snapToGrid, snapToGrid );
    }
    
    /**
     * Gets the 'Snap to grid' enabled state.
     * @return <code>true</code> if the user experiences a 'Snap to grid' behavior when
     * moving MIDI event, <code>false</code> otherwise.
     */
    public boolean isSnapToGridEnabled() {
        return snapToGrid;
    }
    
    /**
     * Sets the divisor for the grid.
     * A divisor of 4 e.g. sets the grid tacting to four lines per tact.
     * @param gridDivisor The grid divisor.
     */
    public void setGridDivisor( int gridDivisor ) {
        if (this.gridDivisor == gridDivisor) { return; }
        drawFineGrid = true;
        int oldVal = this.gridDivisor;
        this.gridDivisor = gridDivisor;
        repaint();
        firePropertyChange( "gridDivisor", oldVal, gridDivisor );
    }
    
    /**
     * Gets the grid divisor.
     * @return The grid divisor.
     */
    public int getGridDivisor() {
        return gridDivisor;
    }
    
    /**
     * Gets the current track.
     * @return The current MIDI track.
     */
    public TrackProxy getTrack() {
        return track;
    }
    
    /**
     * Determines if the given <code>TrackProxy</code> is currently being
     * displayed within this <code>GridComponent</code>.
     * @param track The MIDI track.
     * @return <code>true</code> if the given MIDI track is being displayed,
     *         <code>false</code> otherwise.
     */
    public boolean isTrackDisplayed( TrackProxy track ) {
        if (track == this.track)  { return true; }
        if (this.track != null) { return this.track.equals( track ); }
        return track.equals( this.track );
    }
    
    /**
     * Sets the track.
     * @param track The track to set.
     */
    public void setTrack( TrackProxy track ) {
        if (this.track == track) { return; }

        TrackProxy oldVal = this.track;
        this.track = track;
        setTrackImpl();

        firePropertyChange( "track", oldVal, this.track );
    }
    
    /**
     * Private implementation of the <code>setTrack()</code> method.
     */
    private void setTrackImpl() {
        removeAll();

        if (track == null) { super.revalidate(); return; }
        addEventComponents( track, false, false );
        synchronized (eventComponentMap) {
            for (int i = 0; i < track.getSelectedEventCount(); i++) {
                MidiEvent event = track.getSelectedEventAt( i );
                EventComponent ec = eventComponentMap.get( event );
                ec.setSelected( true, null );
            }
        }

        if (eventMappingComponent != null && eventMappingComponent.isVisible()) {
            eventMappingComponent.updateLabels();
            eventMappingComponent.repaint();
        }
        repaint(); // dunno why this doesn't work without...
        ruler.revalidate();
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
    public TrackProxy[] getEditableTracks() {
        try {
            return midiDescriptor.getSequence().getTrackProxies();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TrackProxy[0];
    }

    /**
     * Deletes the currently selected events, using an <code>UndoableEdit</code>.
     */
    public void deleteSelectedEvents() {
        if (track == null) {
            return;
        }
        // create edit so that the whole thing can be undone
        DeleteEventsEdit edit = new DeleteEventsEdit(
            track,
            track.getSelectedEvents(),
            midiDescriptor,
            new GridEditSource( GridEditSource.DELETE, hashCode(), null ) );
        edit.perform();
        midiDescriptor.getUndoManager().addEdit( edit );
    }
    
    /**
     * Adds the given MIDI events to the current track and creates
     * the according undo action. Please note that this method does
     * also add event components for the given MIDI events.
     * @param events The events to add.
     */
    public void addMidiEvents( MidiEvent[] events ) {
        addMidiEvents( events, new GridEditSource( GridEditSource.ADD, hashCode(), null ) );
    }
    
    /**
     * Adds the given MIDI events to the current track and creates
     * the according undo action. Please note that this method does
     * also add event components for the given MIDI events.
     * @param events The events to add.
     * @param source The source for the add operation.
     */
    private void addMidiEvents( MidiEvent[] events, GridEditSource source ) {
        AddEventsEdit edit = new AddEventsEdit(
            track, events, midiDescriptor, source );
        edit.perform();
        midiDescriptor.getUndoManager().addEdit( edit );
    }
    
    /**
     * Adds the given MIDI events, using the PASTE mode. This means,
     * the user has to move the events to the correct position first.
     * @param events The events to add in PASTE mode.
     */
    public void startPasteMidiEventsMode( MidiEvent[] events ) {
        stopPasteMidiEventsMode();
        stopNoteAddMode(); // it is necessary not to be in add mode
        clearSelection();
        List<EventComponent> eventComponents = addEventComponents( events, true, true );
        if (!eventComponents.isEmpty()) {
            //EventComponent lead = (EventComponent) eventComponents.get( 0 );
            //System.out.println( "tick = " + events[0].getTick() + " - " + lead.event.getTick() );
            editObject = eventComponents;
            editMode = EDIT_MODE_PASTE;
        }
        repaint();
    }
    
    /**
     * Stops the 'paste MIDI events' mode. If not in PASTE mode,
     * this method does nothing.
     */
    public void stopPasteMidiEventsMode() {
        if (editMode == EDIT_MODE_PASTE) {
            if (editObject instanceof List) {
                List<?> list = (List<?>) editObject;
                synchronized (eventComponentMap) {
                    for (int i = 0; i < list.size(); i++) {
                        EventComponent ec = (EventComponent) list.get( i );
                        remove( ec );
                        for (int j = 0; j < ec.getEventCount(); j++) {
                            eventComponentMap.remove( ec.getEventAt( j ) );
                        }
                    }
                }
                repaint();
            }
            editMode = EDIT_MODE_DEFAULT;
            editObject = null;
        }
    }
    
    /**
     * Starts the MIDI note adding mode. Since MIDI notes are
     * added asynchronuously by the user, this method returns
     * immediately.
     */
    public void startNoteAddMode() {
        stopPasteMidiEventsMode(); // verify that we're not in paste mode
        editMode = EDIT_MODE_ADD;
        setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ) );
    }
    
    /**
     * Stops the MIDI note adding mode.
     */
    public void stopNoteAddMode() {
        if (editMode == EDIT_MODE_ADD) {
            setCursor( Cursor.getDefaultCursor() );
            editMode = EDIT_MODE_DEFAULT;
            editObject = null;
        }
    }
    
    /**
     * Moves all given event components by setting the correct parameters on the MidiEvent
     * objects to the correct values and then calling EventComponent.adjustBounds();.
     * this method.
     * @param tick The absolute tick to set for the mousePressedEventComponentPoint.
     * @param yOffset The offset along the Y axis.
     * @param originY The original value along the Y axis.
     */
    private void moveEventComponentsTo(
            EventComponent[] ecs, EventComponent leadComponent, long tick, int yOffset ) {
        moveEventComponentsTo( null, ecs, leadComponent, tick, yOffset );
    }
    /**
     * Moves all given event components by setting the correct parameters on the MidiEvent
     * objects to the correct values and then calling EventComponent.adjustBounds();.
     * this method.
     * @param tick The absolute tick to set for the mousePressedEventComponentPoint.
     * @param yOffset The offset along the Y axis.
     * @param originY The original value along the Y axis.
     */
    private void moveEventComponentsTo(
            List<EventComponent> ecs, EventComponent leadComponent, long tick, int yOffset ) {
        moveEventComponentsTo( ecs, null, leadComponent, tick, yOffset );
    }
    
    /**
     * Moves all given event components by setting the correct parameters on the MidiEvent
     * objects to the correct values and then calling EventComponent.adjustBounds();.
     * this method.
     * @param tick The absolute tick to set for the mousePressedEventComponentPoint.
     * @param yOffset The offset along the Y axis.
     * @param originY The original value along the Y axis.
     */
    private void moveEventComponentsTo(
            List<EventComponent> l, EventComponent[] ecs, EventComponent leadComponent, long tick, int yOffset ) {
        // check if there are only NoteComponents in the selection
        boolean onlyNoteComponents = true;
        int size = (l == null ? ecs.length : l.size());
        for (int i = 0; i < size; i++) {
            if (!((l == null ? ecs[i] : l.get( i )) instanceof NoteComponent)) {
                onlyNoteComponents = false;
                break;
            }
        }
        // check if all components can be moved into their y-axis destination...
        boolean yValid = onlyNoteComponents;
        if (yValid) {
            for (int i = 0; i < size; i++) {
                EventComponent ec = (l == null ? ecs[i] : (EventComponent) l.get( i ));
                EventDescriptor ed = translateYPos( ec.getY() + yOffset, false );
                if (!(ed instanceof NoteDescriptor)) {
                    yValid = false;
                    break;
                }
            }
        }
        
        long offset = tick - leadComponent.event.getTick();
        
        for (int i = 0; i < size; i++) {
            EventComponent ec = (l == null ? ecs[i] : (EventComponent) l.get( i ));
            short noteOffset = getNoteOffset( ec, yOffset );
            for (int j = 0; j < ec.getEventCount(); j++) {
                MidiEvent e = ec.getEventAt( j );
                e.setTick( e.getTick() + offset );
                if (yValid) {
                    ShortMessage msg = (ShortMessage) e.getMessage();
                    try {
                        //System.out.println( "yValid = true, noteOffset = " + noteOffset );
                        int status = msg.getStatus();
                        int data1 = msg.getData1();
                        int data2 = msg.getData2();
                        msg.setMessage( status, data1 + noteOffset, data2 );
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            ec.adjustBounds();
        }
    }
    
    /**
     * Gets an array that contains references to the <code>EventComponent</code>s
     * according to <code>events</code>.
     * @param events The events to get the components for.
     */
    protected synchronized EventComponent[] getEventComponentsFor( MidiEvent[] events ) {
        HashSet<EventComponent> eventComponents = new HashSet<EventComponent>();
        synchronized (eventComponentMap) {
            for (int i = 0; i < events.length; i++) {
                EventComponent c = eventComponentMap.get( events[i] );
                if (c != null) {
                    eventComponents.add( c );
                }
            }
        }
        EventComponent[] result = new EventComponent[eventComponents.size()];
        eventComponents.toArray( result );
        return result;
    }
    
    /**
     * Removes the <code>EventComponent</code>s that contain the given
     * <code>MidiEvent</code>s. Does NO removing from the current track.
     * @param events A <code>MidiEvent</code> array of all events whose
     *        graphical components shall be removed.
     */
    protected synchronized void removeEventComponents( MidiEvent[] events ) {
        EventComponent[] ec = getEventComponentsFor( events );
        System.out.println( "removing " + ec.length + " event components - " + events.length + " events" );
        for (int i = 0; i < ec.length; i++) {
            remove( ec[i] );
            for (int j = 0; j < ec[i].getEventCount(); j++) {
                eventComponentMap.remove( ec[i].getEventAt( j ) );
            }
        }
    }
    
    /**
     * Adds all given events as <code>EventComponent</code>s graphically.
     * (does NO adding to the current track).
     * @param events The events to be added grapically.
     * @param select If set to <code>true</code>, the newly created and added
     *        <code>EventComponent</code>s will be added to the current selection.
     * @param createResultList If <code>true</code>, a <code>List</code> is returned
     *        by this method. Otherwise, <code>null</code> is returned.
     * @return A <code>Vector</code> with all <code>EventComponent</code> objects that
     *         were created by this method, or <code>null</code>.
     */
    protected List<EventComponent> addEventComponents(
            MidiEvent[] events, boolean select, boolean createResultList ) {
        return addEventComponents( events, null, select, createResultList );
    }
    
    /**
     * Adds all given events as <code>EventComponent</code>s graphically.
     * (does NO adding to the current track).
     * @param events The events to be added grapically.
     * @param select If set to <code>true</code>, the newly created and added
     *        <code>EventComponent</code>s will be added to the current selection.
     * @param createResultVector If <code>true</code>, a <code>Vector</code> is returned
     *        by this method. Otherwise, <code>null</code> is returned.
     * @return A <code>List</code> with all <code>EventComponent</code> objects that
     *         were created by this method, or <code>null</code>.
     */
    protected List<EventComponent> addEventComponents(
            TrackProxy track, boolean select, boolean createResultVector ) {
        return addEventComponents( null, track, select, createResultVector );
    }
    
    /**
     * Adds all given events as <code>EventComponent</code>s graphically.
     * (does NO adding to the current track).
     * @param events The events to be added grapically.
     *        If <code>events</code> is <code>null</code>, the <code>Track</code> parameter
     *        used instead.
     * @param track The MIDI track containing the events to be added graphically.
     *        shall be <code>null</code> if and only if <code>events</code> is not
     *        <code>null</code>.
     * @param select If set to <code>true</code>, the newly created and added
     *        <code>EventComponent</code>s will be added to the current selection.
     * @param createResultList If <code>true</code>, a <code>List</code> is returned
     *        by this method. Otherwise, <code>null</code> is returned.
     * @return A <code>List</code> with all <code>EventComponent</code> objects that
     *         were created by this method, or <code>null</code>.
     */
    private List<EventComponent> addEventComponents(
            MidiEvent[] events, TrackProxy t, boolean select, boolean createResultList ) {
        List<EventComponent> list = null;
        if (createResultList) {
            list = new ArrayList<EventComponent>();
        }
        
        // buffer for fast hashing
        MidiEvent[][] noteBuffer = new MidiEvent[256][16];
        
        int cc = 0;
        int nc = 0;
        int ec = 0;
        int eventNum = (events != null ? events.length : t.size());
        for (int i = 0; i < eventNum; i++) {
            MidiEvent event = (events != null ? events[i] : t.get( i ));
            //System.out.println( "event = " + event + ", i = " + i );
            MidiMessage msg = event.getMessage();
            if (msg instanceof ShortMessage) {
                // short messages that carry note information
                ShortMessage sm = (ShortMessage) msg;
                if (sm.getCommand() == ShortMessage.NOTE_ON ||
                    sm.getCommand() == ShortMessage.NOTE_OFF) {
                    
                    cc++;
                    short note = (short) sm.getData1();
                    int channel = sm.getChannel();

                    // note off
                    if (noteBuffer[note][channel] != null) {
                        if (events == null) {
                            select = t.isSelected( event );
                        }
                        NoteComponent noteComponent =
                            addNoteComponent( noteBuffer[note][channel], event, select );
                        if (createResultList && noteComponent != null) {
                            list.add( noteComponent );
                        }
                        nc++;
                        noteBuffer[note][channel] = null;
                    } else {
                        if (sm.getCommand() == ShortMessage.NOTE_OFF) {
                            addNoteComponent( event, event, select );
                        } else {
                            noteBuffer[note][channel] = event;
                        }
                    }
                } else { // short messages that do NOT carry note information 
                    ShortMessageComponent smComponent =
                        addShortMessageEventComponent( event, select );
                    if (createResultList && smComponent != null) {
                        list.add( smComponent );
                    }
                    ec++;
                }
            }
        }
        
        super.revalidate();
        return list;
    }
    
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    
    public Dimension getPreferredSize() {
        prefSize.width = getParent() == null ? 0 : getParent().getWidth();

        SgMidiSequence seq = null;
        try {
            seq = midiDescriptor.getSequence();
        } catch (InvalidMidiDataException e) {
        } catch (IOException e) {
        }
        if (seq != null) {
            int tact = seq.getResolution() * 4;
            prefSize.width = Math.max( translateTickX( seq.getLength() + tact ), prefSize.width );
                Math.min(
                        Math.max( translateTickX( seq.getLength() + tact ), prefSize.width * 1.5 ),
                        Math.max( translateTickX( seq.getActualLength() ) * 2 + tact, prefSize.width ) );
        }
        EventMap eventMap = getEventMap();
        int index = eventMap.getSize() - 1;
        if (index < 0) {
            prefSize.height = 0;
        } else {
            prefSize.height = translateEventY( eventMap.getEventAt( index ) ) + rowHeight;
        }
        
        return prefSize;
    }
    
    public Dimension getMaximumSize() {
        return new Dimension( Integer.MAX_VALUE, getPreferredSize().height );
    }
    
    /**
     * Adds a <code>NoteComponent</code> to the current editable MIDI track.
     * @param event The note on event.
     * @param noteOff The note off event.
     * @param select If <code>true</code>, selects the note component after
     *        it's creation.
     */
    private NoteComponent addNoteComponent(
        MidiEvent event,
        MidiEvent noteOff,
        boolean select ) {
        
        if (!getEventMap().contains( event )) {
            //System.out.println( "throwing away note component, not in mapping!" );
            return null;
        }

        NoteComponent c = new NoteComponent( event, noteOff, track );
        synchronized (eventComponentMap) {
            for (int i = 0; i < c.getEventCount(); i++) {
                eventComponentMap.put( c.getEventAt( i ), c );
            }
        }
        if (select) { selectEventComponent( c ); }
        c.adjustBounds();
        add( c );
        c.repaint();
        return c;
    }
    
    /**
     * Adds a <code>ShortMessageComponent</code> to the current editable MIDI track.
     * @param event The midi event with a ShortMessage.
     * @param select If <code>true</code>, selects the note component after
     *        it's creation.
     * @return The created <code>ShortMessageComponent</code>, or <code>null</code>
     *         if this method failed to create one.
     */
    private ShortMessageComponent addShortMessageEventComponent(
        MidiEvent event, boolean select ) {
        
        if (!getEventMap().contains( event )) {
            //System.out.println( "throwing away sm component, not in mapping!" );
            return null;
        }

        ShortMessageComponent c = new ShortMessageComponent( event, track );
        synchronized (eventComponentMap) {
            for (int i = 0; i < c.getEventCount(); i++) {
                eventComponentMap.put( c.getEventAt( i ), c );
            }
        }
        if (select) { selectEventComponent( c ); }
        c.adjustBounds();
        add( c );
        c.repaint();
        return c;
    }
    
    /**
     * Translates a MidiEvent into a position along the y-axis.
     * @param event The MIDI event.
     * @return The y position, or -1 if the given event cannot be translated.
     */
    private int translateEventY( MidiEvent event ) {
        int index = getEventMap().getIndexFor( event );
        return (index * rowHeight + 1);
    }
    
    /**
     * Translates a MidiEvent into a position along the y-axis.
     * @param event The MIDI event.
     * @return The y position, or -1 if the given event cannot be translated.
     */
    int translateEventY( EventDescriptor event ) {
        int index = getEventMap().getIndexFor( event );
        return (index * rowHeight + 1);
    }
    
    /**
     * Safely translates the given position on the y-axis into a MIDI event descriptor.
     * @param yPos The y position to be translated
     * @return The note value.
     */
    EventDescriptor translateYPos( int yPos ) {
        return translateYPos( yPos, true );
    }
    
    /**
     * Translates the given position on the y-axis into a MIDI event descriptor.
     * @param yPos The y position to be translated
     * @param safe If <code>true</code>, indices smaller than 0 are mapped to 0
     *        and indices greater than the event map are mapped to the event map size.
     *        If this parameter is <code>false</code>, <code>null</code> is returned
     *        when indices are out of bounds.
     * @return The note value.
     */
    private EventDescriptor translateYPos( int yPos, boolean safe ) {
        short index = (short) Math.round( yPos / rowHeight );
        EventMap eventMap = getEventMap();
        if (index >= eventMap.getSize()) {
            if (safe) {
                index = (short) (eventMap.getSize() - 1);
            } else {
                return null;
            }
        }
        if (index < 0) {
            if (safe) {
                index = 0;
            } else {
                return null;
            }
        }
        return eventMap.getEventAt( index );
    }
    
    /**
     * Translates a position along the x-axis into a tick position.
     * @param xPos The position along the x-axis.
     * @param snapToGrid If <code>true</code>, the translation will snap to the
     * nearest grid.
     * @return The translated tick.
     */
    private long translateXPos( int xPos, boolean snapToGrid ) {
        long tick = translateXPos( xPos );
        if (snapToGrid) {
            // search nearest grid tick
            Sequence sequence = null;
            try {
                sequence = midiDescriptor.getSequence();
            } catch (InvalidMidiDataException e) {
            } catch (IOException e) {
            }
    
            // currently, only PPQ (pulses per quarternote) is supported...
            if (sequence != null &&
                sequence.getDivisionType() == Sequence.PPQ) {
    
                int tact = (sequence.getResolution() * 4) / gridDivisor;
                long prevTick = tick - (tick % tact);
                long nextTick = prevTick + tact;
                if (Math.abs(tick - prevTick) < Math.abs(tick - nextTick)) {
                    tick = prevTick;
                } else {
                    tick = nextTick;
                }
            }

        }
        return tick;
    }
    
    /**
     * Translates a position along the x-axis into a tick position without
     * snap-to-grid.
     * @param xPos The position along the x-axis.
     * @return The translated tick.
     */
    private long translateXPos( int xPos ) {
        return (long) (xPos / (zoom * zoomCorrection));
    }
    
    /**
     * Translates a tick to a position along the x-axis.
     * @param tick The tick to be translated.
     * @return The translated x-coordinate.
     */
    int translateTickX( long tick ) {
        return (int) (tick * (zoom * zoomCorrection));
    }
    
    private int translateTickWidth( long tick ) {
        return Math.max( 1, (int) (tick * (zoom * zoomCorrection)) );
    }
    
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement( Rectangle visibleRect, int orientation, int direction ) {
        if (orientation == SwingConstants.VERTICAL) {
            return rowHeight * 2;
        }
        return (int) ((zoom * zoomCorrection) * 5);
    }

    public int getScrollableBlockIncrement( Rectangle visibleRect, int orientation, int direction ) {
        if (orientation == SwingConstants.VERTICAL) {
            return visibleRect.height;
        }
        return visibleRect.width;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /**
     * Does the grid painting.
     * @param g
     */
    private void paintGrid( Graphics g, Rectangle clipBounds, boolean ruler ) {
        g.setColor( gridColor );
        if (!ruler) {
            // horizontal lines
            for (int y = clipBounds.y - (clipBounds.y % rowHeight);
                y < clipBounds.y + clipBounds.height; y += rowHeight) {

                g.drawLine( clipBounds.x, y, clipBounds.x + clipBounds.width, y );
            }
        }
        
        Sequence sequence = null;
        try {
            sequence = midiDescriptor.getSequence();
        } catch (InvalidMidiDataException e) {
        } catch (IOException e) {
        }

        // vertical lines
        // currently, only PPQ (pulses per quarternote) is supported...
        if (sequence != null &&
            sequence.getDivisionType() == Sequence.PPQ) {

            int tact = sequence.getResolution() * 4;
            long startTick = translateXPos( clipBounds.x );
            startTick = startTick - (startTick % tact);
            int startCount = (int) (startTick / tact);
            if (!ruler) {
                double quotient = (double) gridDivisor / 4.0d;
                long currentTick = startTick;
                int count = 0;
                int x = -100000;
                if (drawFineGrid) {
                    while (x < clipBounds.x + clipBounds.width) {
                        int newX = translateTickX( currentTick );
                        if (newX - x <= 1) {
                            drawFineGrid = false; break;
                        }
                        x = newX;
                        g.drawLine( x, clipBounds.y, x, clipBounds.y + clipBounds.height );
                        currentTick = startTick + (long)
                            ((++count * (double) sequence.getResolution()) / quotient);
                    }
                }
            }
            
            
            if (tact > 0) {
                long currentTick = startTick - (startTick % tact);
                g.setColor( tactColor );
                int x = 0;
                int count = startCount;
                FontMetrics fm = getFontMetrics( getFont() );
                g.setFont( getFont() );
                while (x < clipBounds.x + clipBounds.width) {
                    x = translateTickX( currentTick );
                    g.drawLine( x, clipBounds.y, x, clipBounds.y + clipBounds.height );
                    count++;
                    currentTick += tact;
                    if (ruler && displayTactNumbers) {
                        int width = translateTickX( currentTick ) - x;
                        String s = Integer.toString( count );
                        int xPos = x + (width / 2) - (fm.stringWidth( s ) / 2);
                        int yPos = (this.ruler.getHeight() / 2) - (fm.getHeight() / 2) + fm.getAscent();
                        g.setColor( getForeground() );
                        g.drawString( s, xPos, yPos );
                        g.setColor( tactColor );
                    }
                }
            }
        }
    }

    public void paint( Graphics g ) {
        Rectangle r = g.getClipBounds();
        g.setColor( getBackground() );
        g.fillRect( r.x, r.y, r.width, r.height );

        g.setColor( getForeground() );

        // paint grid
        if (gridPainted) {
            paintGrid( g, r, false );
        }

        paintChildren( g );

        // paint pointer        
        g.setColor( pointerColor );
        g.fillRect( pos - 1, 0, 2, getHeight() );
        
        // paint left and right markers
        if (leftMarkerPos >= 0) {
            g.setColor( leftMarkerColor );
            g.drawLine( leftMarkerPos, 0, leftMarkerPos, getHeight() );
        }
        if (rightMarkerPos >= 0) {
            g.setColor( rightMarkerColor );
            g.drawLine( rightMarkerPos, 0, rightMarkerPos, getHeight() );
        }
        
        // paint dragArea
        if (dragArea != null) {
            if (removeDragArea) {
                dragArea = null;
                removeDragArea = false;
            } else {
                int x = dragArea.x;
                int y = dragArea.y;
                int width = dragArea.width;
                int height = dragArea.height;
                if (width < 0) {
                    x += width;
                    width = -width;
                }
                if (height < 0) {
                    y += height;
                    height = -height;
                }
                g.setColor( dragAreaBorderColor );
                g.drawRect( x, y, width - 1, height - 1 );
                g.setColor( dragAreaColor );
                g.fillRect( x + 1, y + 1, width - 2, height - 2 );
                if (paintedDragArea == null) {
                    paintedDragArea = new Rectangle( x, y, width, height );
                } else {
                    paintedDragArea.x = x;
                    paintedDragArea.y = y;
                    paintedDragArea.width = width;
                    paintedDragArea.height = height;
                }
            }
        }
    }
    
    private void updateCursorPosition( int oldPos, int pos ) {
        if (pos == oldPos) { return; }
        if (ruler != null) {
            ruler.updatePlayer( oldPos, pos );
        }
        boolean b = true;
        MidiRecorder recorder = midiDescriptor.getMidiRecorder();
        if (recorder != null &&
            (recorder.isPlaying() || recorder.isInFastForwardMode() || recorder.isInFastBackwardMode()) &&
            chaseCursor) {

            Rectangle vr = getVisibleRect();
            if (chaseCursorPagewise) {
                if (recorder.isInFastBackwardMode()) {
                    if (pos < vr.x + 10) {
                        r.x = pos - vr.width;
                        r.y = vr.y;
                        r.width = vr.width;
                        r.height = vr.height;
                        b = (r.x != vr.x);
                        scrollRectToVisible( r );
                    } else if (pos > vr.x + vr.width) {
                        r.x = pos - vr.width + 10;
                        r.y = vr.y;
                        r.width = vr.width;
                        r.height = vr.height;
                        b = (r.x != vr.x);
                        scrollRectToVisible( r );
                    }
                } else {
                    if (pos + 10 >= vr.x + vr.width) {
                        r.x = pos - 10;
                        r.y = vr.y;
                        r.width = vr.width;
                        r.height = vr.height;
                        b = false;
                        scrollRectToVisible( r );
                    } else if (pos < vr.x) {
                        r.x = Math.max( pos - 10, 0 );
                        r.y = vr.y;
                        r.width = vr.width;
                        r.height = vr.height;
                        b = false;
                        scrollRectToVisible( r );
                    }
                }
            } else {
                r.x = pos - vr.width / 2;
                r.width = vr.width;
                r.y = getVisibleRect().y;
                if (r.x < 0) { r.x = 0; }
                else if (r.x + vr.width > getWidth()) {
                    r.x = getWidth() - vr.width;
                }
                if (r.x == vr.x)
                {
                    b = true;
                } else {
                    scrollRectToVisible( r );
                }
            }
        }
        if (b) {
            int x0 = Math.min( oldPos, pos ) - 1;
            repaint( x0, 0, Math.max( oldPos, pos ) - x0 + 1, getHeight() );
        }
    }
    
    /**
     * Sets the position along the x-axis of the left marker.
     * @param leftMarkerPosition The left marker position along the x-axis.
     */
    private void setLeftMarkerPosition( int leftMarkerPosition ) {
        int oldMarkerPos = leftMarkerPos;
        leftMarkerPos = leftMarkerPosition;
        repaintMarker( oldMarkerPos, leftMarkerPos );
    }
    
    /**
     * Sets the position along the x-axis of the right marker.
     * @param rightMarkerPosition The right marker position along the x-axis.
     */
    private void setRightMarkerPosition( int rightMarkerPosition ) {
        int oldMarkerPos = rightMarkerPos;
        rightMarkerPos = rightMarkerPosition;
        repaintMarker( oldMarkerPos, rightMarkerPos );
    }
    
    private void repaintMarker( int oldMarkerPos, int newMarkerPos ) {
        int x = Math.min( oldMarkerPos, newMarkerPos );
        int width = Math.max( oldMarkerPos, newMarkerPos ) - x;
        if (x < 0) {
            x = Math.max( oldMarkerPos, newMarkerPos );
            width = 1;
        }
        repaint( x - 1, 0, width + 2, getHeight() );
        getRulerComponent().repaint( x - 6, 0, width + 12, getRulerComponent().getHeight() );
    }
    
    /* (non-Javadoc)
     */
    public void deviceUpdate( MidiRecorder recorder, MidiDescriptor midiDescriptor, int updateHint ) {
        if ((updateHint & MidiUpdatable.TICK) != 0) {
            int oldPos = pos;
            pos = (int) ((double) recorder.getTickPosition() * (zoom * zoomCorrection));
            updateCursorPosition( oldPos, pos );
        }
        if ((updateHint & MidiUpdatable.LEFT_MARKER_TICK) != 0) {
            setLeftMarkerPosition( translateTickX( recorder.getLeftMarkerTick() ) );
        }
        if ((updateHint & MidiUpdatable.RIGHT_MARKER_TICK) != 0) {
            setRightMarkerPosition( translateTickX( recorder.getRightMarkerTick() ) );
        }
    }

    
    /*
     */
    public void deviceClosed( MidiDevice device, MidiDescriptor midiDescriptor ) {
        int oldPos = pos;
        pos = 0;
        updateCursorPosition( oldPos, pos );
    }

    /**
     * Gets all MIDI events from this <code>GridComponent</code>, selected or not.
     * @param editTrackOnly If set to <code>true</code>, indicates that only notes from
     *        the current editable MIDI track shall be returned.
     * @return An array containing all MIDI events.
     */
    public MidiEvent[] getAllEvents( boolean editTrackOnly ) {
        Component[] components = getComponents();
        // count required event space
        int c = 0;
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof EventComponent) {
                EventComponent ec = (EventComponent) components[i];
                if (!editTrackOnly || (ec.track == this.track)) {
                    c += ec.getEventCount();
                }
            }
        }
        MidiEvent[] events = new MidiEvent[c];
        c = 0;
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof EventComponent) {
                EventComponent ec = (EventComponent) components[i];
                if (!editTrackOnly || (ec.track == this.track)) {
                    for (int j = 0; j < ec.getEventCount(); j++) {
                        events[c++] = ec.getEventAt( j );
                    }
                }
            }
        }
        
        return events;
    }

    /**
     * Gets all MIDI events from this <code>GridComponent</code> that describe
     * MIDI notes, selected or not.
     * @param editTrackOnly If set to <code>true</code>, indicates that only notes from
     *        the current editable MIDI track shall be returned.
     * @return An array containing MIDI note events.
     */
    public MidiEvent[] getAllNoteEvents( boolean editTrackOnly ) {
        Component[] components = getComponents();
        // count required event space
        int c = 0;
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof NoteComponent) {
                EventComponent ec = (EventComponent) components[i];
                if (!editTrackOnly || (ec.track == this.track)) {
                    c += ec.getEventCount();
                }
            }
        }
        MidiEvent[] events = new MidiEvent[c];
        c = 0;
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof NoteComponent) {
                EventComponent ec = (EventComponent) components[i];
                if (!editTrackOnly || (ec.track == this.track)) {
                    for (int j = 0; j < ec.getEventCount(); j++) {
                        events[c++] = ec.getEventAt( j );
                    }
                }
            }
        }
        
        return events;
    }
        
    /**
     * Gets all MIDI events from this <code>GridComponent</code> that describe
     * MIDI <code>NOTE_ON</code> events, selected or not.
     * @param editTrackOnly If set to <code>true</code>, indicates that only notes from
     *        the current editable MIDI track shall be returned.
     * @return An array containing MIDI note on events.
     */
    public MidiEvent[] getAllNoteOnEvents( boolean editTrackOnly ) {
        Component[] components = getComponents();
        // count required event space
        int c = 0;
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof NoteComponent) {
                EventComponent ec = (EventComponent) components[i];
                if (!editTrackOnly || (ec.track == this.track)) {
                    c++;
                }
            }
        }
        MidiEvent[] events = new MidiEvent[c];
        c = 0;
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof NoteComponent) {
                EventComponent ec = (EventComponent) components[i];
                if (!editTrackOnly || (ec.track == this.track)) {
                    events[c++] = ec.event;
                }
            }
        }
        
        return events;
    }
    
    /**
     * Helper method to extract MIDI events from an event component
     * <code>Collection</code>.
     * @param coll The <code>Collection</code>, containing <code>EventComponent</code>s.
     * @return An array of MIDI events.
     */
    private MidiEvent[] getEventsFromEventComponentCollection( Collection<EventComponent> coll ) {
        Iterator<EventComponent> iter = coll.iterator();
        // count required event space
        int c = 0;
        for (int i = 0; iter.hasNext(); i++) {
            EventComponent ec = iter.next();
            c += ec.getEventCount();
        }
        iter = coll.iterator();
        MidiEvent[] events = new MidiEvent[c];
        for (int i = 0; iter.hasNext();) {
            EventComponent ec = iter.next();
            for (int j = 0; j < ec.getEventCount(); j++) {
                events[i++] = ec.getEventAt( j );
            }
        }
        
        return events;
    }

    /**
     * Helper method to extract MIDI events from an event component array
     * @param coll The <code>Collection</code>, containing <code>EventComponent</code>s.
     * @return An array of MIDI events.
     */
    private MidiEvent[] getEventsFromEventComponentArray( EventComponent[] ecs ) {
        // count required event space
        int c = 0;
        for (int i = 0; i < ecs.length; i++) {
            c += ecs[i].getEventCount();
        }
        MidiEvent[] events = new MidiEvent[c];
        int k = 0;
        for (int i = 0; i < ecs.length; i++) {
            EventComponent ec = ecs[i];
            for (int j = 0; j < ec.getEventCount(); j++) {
                events[k++] = ec.getEventAt( j );
            }
        }
        
        return events;
    }

    /**
     * Gets all <code>MidiEvent</code>s currently selected by the user.
     * @return The currently selected <code>MidiEvent</code>s as a copied array,
     *         or an empty array if no midi events are currently selected.
     */
    public MidiEvent[] getSelectedEvents() {
        if (track == null) {
            return new MidiEvent[0];
        }
        return track.getSelectedEvents();
    }
    
    /**
     * Gets all <code>MidiEvent</code>s describing MIDI <code>NOTE_ON</code> events
     * that are currently selected by the user.
     * @return The currently selected <code>MidiEvent</code>s as a copied array,
     *         or an empty array if no midi note events are currently selected.
     */
    public MidiEvent[] getSelectedNoteOnEvents() {
        if (track == null) {
            return new MidiEvent[0];
        }
        return track.getSelectedNoteOnEvents();
    }
    
    /**
     * Gets the <code>selectionEmpty</code> state.
     * @return <code>true</code> if there are no elements in the current user
     *         selection, <code>false</code> otherwise.
     */
    public boolean isSelectionEmpty() {
        if (track == null) {
            return true;
        }
        return track.isSelectionEmpty();
    }
    
    /**
     * Gets the number of MIDI events that are currently selected.<br>
     * @return The selected events count.
     */
    public int getSelectedEventCount() {
        if (track == null) {
            return 0;
        }
        return track.getSelectedEventCount();
    }
    
    /**
     * Gets the currently selected event components.
     * @return The currently selected event components.
     */
    private EventComponent[] getSelectedEventComponents() {
        if (track == null) {
            return new EventComponent[0];
        }
        synchronized (eventComponentMap) {
            HashSet<EventComponent> set = new HashSet<EventComponent>();
            MidiEvent[] events = track.getSelectedEvents();
            for (int i = 0; i < events.length; i++) {
                EventComponent val = eventComponentMap.get( events[i] );
                if (val != null) {
                    set.add( val );
                }
            }
            
            EventComponent[] result = new EventComponent[set.size()];
            Iterator<EventComponent> iter = set.iterator();
            for (int i = 0; iter.hasNext(); i++) {
                EventComponent ec = iter.next();
                result[i] = ec;
            }
            return result;
        }
    }
    
    /**
     * Selects the MIDI note that is the closest to the currently selected
     * MIDI note. If no MIDI note is currently selected, or more than
     * one MIDI note is selected, this method does nothing.
     */
    public void selectNearestNote() {
        EventComponent[] eventComponents = getSelectedEventComponents();
        if (eventComponents == null || eventComponents.length != 1 || !(eventComponents[0] instanceof NoteComponent)) {
            return;
        }
        NoteComponent nc = (NoteComponent) eventComponents[0];
        boolean b = true;
        MidiEvent event = nc.event;
        while (b) {
            event = track.getMidiEventNextTo( event.getTick() - 1, true );
            if (event == null || event.getMessage() instanceof ShortMessage) {
                b = false;
            }
        }
        if (event != null) {
            System.out.println( "noteOn.tick = " + nc.event.getTick() + "noteOff.tick = " + nc.noteOff.getTick() + " found.tick = " + event.getTick() );
            deselectEventComponent( nc );
            selectAll( new MidiEvent[] { event } );
            nc.repaint();
        }
    }
    
    /**
     * Adds an <code>EventComponent</code> to the list of selected
     * event components.
     * @param ec The <code>EventComponent</code>.
     */
    private void selectEventComponent( EventComponent ec ) {
        if (ec.getEventCount() == 1) {
            track.select( ec.event, this );
        } else {
            MidiEvent[] e = new MidiEvent[ec.getEventCount()];
            for (int i = 0; i < e.length; i++) {
                e[i] = ec.getEventAt( i );
            }
            track.selectAll( e, this );
        }
    }
    
    /**
     * Selects the <code>EventComponent</code>s for the specified index
     * ("all events in a row").
     * @param index The logical index.
     */
    public void selectRow( int index ) {
        if (track == null) {
            return;
        }
        EventMap eventMap = getEventMap();
        EventDescriptor ed = eventMap.getEventAt( index );
        if (ed == null) { return; }
        Component[] components = getComponents();
        ArrayList<EventComponent> l = new ArrayList<EventComponent>();
        int count = 0;
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof EventComponent) {
                EventComponent ec = (EventComponent) components[i];
                // get event descriptor from event mapping
                if (ed == eventMap.getEventDescriptorFor( ec.event )) {
                    l.add( ec );
                    count += ec.getEventCount();
                }
            }
        }
        MidiEvent[] e = new MidiEvent[count];
        int c = 0;
        for (Iterator<EventComponent> iter = l.iterator(); iter.hasNext(); ) {
            EventComponent ec = iter.next();
            for (int j = 0; j < ec.getEventCount(); j++) {
                e[c++] = ec.getEventAt( j );
            }
        }
        track.selectAll( e, this );
    }
    
    /**
     * Deselects the <code>EventComponent</code>s for the specified index
     * ("all events in a row").
     * @param index The logical index.
     */
    public void deselectRow( int index ) {
        if (track == null) {
            return;
        }
        EventMap eventMap = getEventMap();
        EventDescriptor ed = eventMap.getEventAt( index );
        if (ed == null) { return; }
        Component[] components = getComponents();
        ArrayList<EventComponent> l = new ArrayList<EventComponent>();
        int count = 0;
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof EventComponent) {
                EventComponent ec = (EventComponent) components[i];
                // get event descriptor from event mapping
                if (ed == eventMap.getEventDescriptorFor( ec.event )) {
                    l.add( ec );
                    count += ec.getEventCount();
                }
            }
        }
        MidiEvent[] e = new MidiEvent[count];
        int c = 0;
        for (Iterator<EventComponent> iter = l.iterator(); iter.hasNext(); ) {
            EventComponent ec = iter.next();
            for (int j = 0; j < ec.getEventCount(); j++) {
                e[c++] = ec.getEventAt( j );
            }
        }
        track.deselectAll( e, this );
    }
    
    /**
     * Selects all events from the current <code>TrackProxy</code>.
     */
    public void selectAll() {
        if (track == null) {
            return;
        }
        track.selectAll( this );
    }
    
    /**
     * Selects all given <code>MidiEvent</code> objects from the current
     * <code>TrackProxy</code>.
     * @param events The events.
     */
    public void selectAll( MidiEvent[] events ) {
        if (track == null) {
            return;
        }
        track.selectAll( events, this );
    }
    
    /**
     * Inverts the selection. Selects all if nothing is selected.
     */
    public void invertSelection() {
        if (isSelectionEmpty()) {
            selectAll();
        } else {
            Component[] components = getComponents();
            for (int i = 0; i < components.length; i++) {
                if (components[i] instanceof EventComponent) {
                    EventComponent ec = (EventComponent) components[i];
                    if (ec.isSelected()) {
                        deselectEventComponent( ec );
                    } else {
                        selectEventComponent( ec );
                    }
                }
            }
        }
    }

    /**
     * Removes an <code>EventComponent</code> from the list of selected
     * event components.
     * @param ec The <code>EventComponent</code>.
     * @return <code>true</code> if the component has been removed,
     *         <code>false</code> if it was not in the list of selected
     *         event components.
     */
    private boolean deselectEventComponent( EventComponent ec ) {
        if (ec.getEventCount() == 1) {
            track.deselect( ec.event, this );
        } else {
            MidiEvent[] e = new MidiEvent[ec.getEventCount()];
            for (int i = 0; i < e.length; i++) {
                e[i] = ec.getEventAt( i );
            }
            track.deselectAll( e, this );
        }
        return true;
    }
    
    /**
     * Removes all <code>EventComponent</code>s that are currently selected
     * from the list of selected event components.
     */
    public void clearSelection() {
        if (track == null) {
            return;
        }
        track.clearSelection( this );
    }

    /**
     * Plays a MIDI note if there is currently one selected.
     * @return <code>true</code> if a selected note has been played,
     *         <code>false</code> otherwise.
     * @throws MidiUnavailableException If no MIDI device is available.
     */
    public boolean playMidiNote() throws RecorderException {
        EventComponent[] ecs = getSelectedEventComponents();
        if (ecs == null || ecs.length == 0 || !(ecs[0] instanceof NoteComponent)) {
            return false;
        }
        midiDescriptor.getMidiRecorder().playSingleNote( ecs[0].event, ((NoteComponent) ecs[0]).noteOff );
        
        return true;
    }
    
    /**
     * Gets the current possibility of editing the selected midi events.
     * @return
     */
    public boolean isEditMidiEventsPossible() {
        return true;
    }

    /**
     * Adds a MIDI track to the current sequence and returns it.
     * An undoable edit is automatically created and attached by this method.
     * @return The newly created <code>TrackProxy</code>
     */
    public TrackProxy addTrack() {
        SgMidiSequence sequence = null;
        try {
            sequence = midiDescriptor.getSequence();
        } catch (InvalidMidiDataException e) {
        } catch (IOException e) {
        }
        if (sequence != null) {
            AddTrackEdit edit = new AddTrackEdit(
                sequence,
                midiDescriptor,
                new GridEditSource( GridEditSource.ADD_TRACK, hashCode(), null ) );
            edit.perform();
            midiDescriptor.getUndoManager().addEdit( edit );
    
            return edit.getNewTrackProxy();
        }
        return null;
    }
    
    /**
     * Removes the current MIDI track from the current sequence and returns it.
     * An undoable edit is automatically created and attached by this method.
     * @return The remove <code>TrackProxy</code>, or <code>null</code>
     *         if no MIDI track has been removed.
     */
    public TrackProxy removeTrack() {
        SgMidiSequence sequence = null;
        try {
            sequence = midiDescriptor.getSequence();
        } catch (InvalidMidiDataException e) {
        } catch (IOException e) {
        }
        if (sequence != null && track != null) {
            RemoveTrackEdit edit = new RemoveTrackEdit(
                sequence,
                track,
                midiDescriptor,
                new GridEditSource( GridEditSource.REMOVE_TRACK, hashCode(), null ) );
            edit.perform();
            midiDescriptor.getUndoManager().addEdit( edit );
        }

        return track;
    }
    
    /**
     * Displays a dialog that allows the user to edit the name of the
     * MIDI track that is currently set as the default editing track.
     * If no track is currently set, this method does nothing.
     * @return The new track name, or <code>null</code> if the user chose not
     *         to set a track name at all or aborted the dialog.
     */
    public String showEditTrackNameDialog() {
        return UiToolkit.showEditTrackNameDialog(
            new GridEditSource( GridEditSource.CHANGE_TRACK_NAME, hashCode(), null ),
            midiDescriptor, track, getParent() );
    }

    /**
     * Shows a dialog where the user can set a minimum volume
     * for all notes within this <code>GridComponent</code>.
     * @param defaultValue The default volume. Must be between 0 and 127.
     * @return The volume that has been set, or <code>-1</code>
     *         if the dialog has not been shown, or <code>-2</code> if the
     *         dialog has been cancelled.
     */
    public int showSetMinimumVolumeDialog( int defaultValue )
    {
        return showSetVolumeDialog(
            defaultValue,
            true,
            SgEngine.getInstance().getResourceBundle().getString(
            "plugin.gridView.setMinimumVolumeDalog" ),
            SgEngine.getInstance().getResourceBundle().getString(
                "plugin.gridView.setMinimumVolumeDalog.minimumVolume" ) );
    }
    
    /**
     * Shows a dialog where the user can set a maximum volume
     * for all notes within this <code>GridComponent</code>.
     * @param defaultValue The default volume. Must be between 0 and 127.
     * @return The volume that has been set, or <code>-1</code>
     *         if the dialog has not been shown, or <code>-2</code> if the
     *         dialog has been cancelled.
     */
    public int showSetMaximumVolumeDialog( int defaultValue ) {
        return showSetVolumeDialog(
            defaultValue,
            false,
            SgEngine.getInstance().getResourceBundle().getString(
            "plugin.gridView.setMaximumVolumeDalog" ),
            SgEngine.getInstance().getResourceBundle().getString(
                "plugin.gridView.setMaximumVolumeDalog.maximumVolume" ) );
    }
    
    /**
     * The private implementation of <code>showSetMinimumVolumeDialog()</code>
     * and <code>showSetMaximumVolumeDialog()</code>.
     * @param defaultValue The default volume value.
     * @param minimum <code>true</code> if minimum volume shall be set,
     *        <code>false</code> otherwise.
     * @param title The title.
     * @param label The label caption.
     * @return The volume that has been set, or <code>-1</code>
     *         if the dialog has not been shown, or <code>-2</code> if the
     *         dialog has been cancelled.
     */
    private int showSetVolumeDialog( int defaultValue, boolean minimum, String title, String label ) {
        SetVolumePanel panel = new SetVolumePanel(
            this,
            defaultValue,
            title,
            label );
        EditFocusPanel focusPanel = new EditFocusPanel( this );

        Object[] message = new Object[] {
            panel,
            focusPanel,
        };
        int option = JOptionPane.showConfirmDialog(
            getParent(), message,
            title,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION && panel.hasChanged()) {
            try {
                MidiEvent[] events =
                    (focusPanel.isApplicableForSelection() ?
                        getSelectedNoteOnEvents() :
                            getAllNoteOnEvents( focusPanel.isApplicableForCurrentTrack() ));
                // create change edit and perform changes
                ChangeEventsEdit edit = new ChangeEventsEdit(
                    track,
                    events,
                    midiDescriptor,
                    SgEngine.getInstance().getResourceBundle().getString(
                        (minimum ? "edit.setMinimumVolumeEdit" : "edit.setMaximumVolumeEdit") ),
                    new GridEditSource( GridEditSource.CHANGE, hashCode(), null ) );
                // perform update...
                int vol = panel.getVolume();
                System.out.println( "setting volume for " + events.length + " events" );
                for (int i = 0; i < events.length; i++) {
                    ShortMessage msg = (ShortMessage) events[i].getMessage();
                    if ((minimum && msg.getData2() < vol) ||
                        (!minimum && vol < msg.getData2())) {
                        msg.setMessage( msg.getCommand(), msg.getChannel(), msg.getData1(), vol );
                    }
                }
                // call the edit.perform()
                edit.perform();
                midiDescriptor.getUndoManager().addEdit( edit );
                return vol;
            } catch (InvalidMidiDataException imdex) {
                JOptionPane.showMessageDialog(
                    this,
                    imdex.getMessage(),
                    SgEngine.getInstance().getResourceBundle().getString(
                        "error.invalidMidiData" ),
                    JOptionPane.ERROR_MESSAGE );
            }
            return -1;
        }
        return -2;
    }
    
    /**
     * Shows a 'increase volume" dialog.
     * @param defaultValue The default volume value.
     * @param title
     * @param label
     */
    public void showIncreaseVolumeDialog(
        boolean linear,
        boolean constant,
        float defaultFactor,
        int defaultConstant,
        String dialogTitle,
        String title,
        String factorCaption,
        String constantCaption ) {
        
        showXcreaseVolumeDialog(
            true,
            linear,
            constant,
            defaultFactor,
            defaultConstant,
            dialogTitle,
            title,
            factorCaption,
            constantCaption );
    }

    /**
     * Shows a 'decrease volume" dialog.
     * @param defaultValue The default volume value.
     * @param title
     * @param label
     */
    public void showDecreaseVolumeDialog(
        boolean linear,
        boolean constant,
        float defaultFactor,
        int defaultConstant,
        String dialogTitle,
        String title,
        String factorCaption,
        String constantCaption ) {

        showXcreaseVolumeDialog(
            false,
            linear,
            constant,
            defaultFactor,
            defaultConstant,
            dialogTitle,
            title,
            factorCaption,
            constantCaption );

    }
    
    /**
     * Private implementation of showIncrease/showDecreaseVolumeDialog.
     * @param increase
     * @param defaultValue
     * @param title
     * @param label
     * @return
     */
    private int showXcreaseVolumeDialog(
        boolean increase,
        boolean linear,
        boolean constant,
        float defaultFactor,
        int defaultConstant,
        String dialogTitle,
        String title,
        String factorCaption,
        String constantCaption ) {

        XcreaseVolumePanel panel = new XcreaseVolumePanel(
            increase,
            this,
            true,
            false,
            defaultFactor,
            defaultConstant,
            title,
            factorCaption,
            constantCaption,
            title );
        EditFocusPanel focusPanel = new EditFocusPanel( this );

        Object[] message = new Object[] {
            panel,
            focusPanel,
        };
        int option = JOptionPane.showConfirmDialog(
            getParent(), message,
            dialogTitle,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION && panel.hasChanged()) {
            try {
                MidiEvent[] events =
                    (focusPanel.isApplicableForSelection() ?
                        getSelectedNoteOnEvents() :
                            getAllNoteOnEvents( focusPanel.isApplicableForCurrentTrack() ));
                // create change edit and perform changes
                ChangeEventsEdit edit = new ChangeEventsEdit(
                    track,
                    events,
                    midiDescriptor,
                    SgEngine.getInstance().getResourceBundle().getString(
                        (increase ? "edit.increaseVolume" : "edit.decreaseVolume") ),
                    new GridEditSource( GridEditSource.CHANGE, hashCode(), null ) );
                // perform update...
                int con = panel.getConstant();
                if (!increase) { con = -con; }
                float factor = panel.getFactor();
                for (int i = 0; i < events.length; i++) {
                    ShortMessage msg = (ShortMessage) events[i].getMessage();
                    int vol = msg.getData2();
                    float f = factor * vol + con;
                    if (f > 127) { f = 127; }
                    if (f < 0) { f = 0; }
                    msg.setMessage( msg.getCommand(), msg.getChannel(), msg.getData1(), (int) f );
                }
                GridEditSource ges = new GridEditSource( GridEditSource.CHANGE, hashCode(), null );
                track.removeAll( events, ges );
                track.addAll( events, ges );
                // call the edit.perform()
                edit.perform();
                midiDescriptor.getUndoManager().addEdit( edit );
                repaint();
                return 0;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    SgEngine.getInstance().getResourceBundle().getString(
                        "error.invalidMidiData" ),
                    JOptionPane.ERROR_MESSAGE );
            }
            return -1;
        }
        return -2;
    }
    

    /**
     * Displays a dialog with some information.
     */
    public void showInformationDialog() {
        ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        JTextArea ta = new JTextArea( 6, 40 );
        ta.setEditable( false );
        JScrollPane sp = new JScrollPane( ta );
        EditFocusPanel efp = new EditFocusPanel(
            this,
            rb.getString( "plugin.gridView.info.focusTitle" ) );
        
        JPanel infoPanel = new JPanel( new BorderLayout() );
        JComboBox infoCombo = new JComboBox(
            new Object[] {
                rb.getString( "plugin.gridView.info.pleaseSelect" ),
                new VolumeTextAction( rb.getString( "plugin.gridView.info.volume" ), ta, efp ),
                new EventTextAction( rb.getString( "plugin.gridView.info.event" ), ta, efp ),
            } );
        efp.setClientData( infoCombo );
        infoCombo.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                Object o = ((JComboBox) e.getSource()).getSelectedItem();
                if (o instanceof TextAction)
                {
                    ((TextAction) o ).execute();
                }
            }
        } );
        infoPanel.add( infoCombo );
        
        ActionListener al = new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                EditFocusPanel efp = ((EditFocusPanel) ((JComponent) e.getSource()).getParent());
                JComboBox cb = (JComboBox) efp.getClientData();
                Object o = cb.getSelectedItem();
                if (o instanceof TextAction)
                {
                    ((TextAction) o ).execute();
                }
            }
        };
        efp.getSelectionButton().addActionListener( al );
        efp.getAllButton().addActionListener( al );

        Object[] message = new Object[] {
            infoPanel,
            efp,
            sp,
        };
        JOptionPane.showMessageDialog(
            getParent(), message,
            rb.getString( "plugin.gridView.info.title" ),
            JOptionPane.PLAIN_MESSAGE, null );
    }
    
    // helper class for the information dialog
    class TextAction {
        String s;
        JTextArea ta;
        EditFocusPanel efp;
        TextAction( String s, JTextArea ta, EditFocusPanel efp ) {
            this.s = s;
            this.ta = ta;
            this.efp = efp;
        }
        public String toString() { return s; }
        void execute() {}
    }
    
    class VolumeTextAction extends TextAction {
        VolumeTextAction( String s, JTextArea ta, EditFocusPanel efp ) {
            super( s, ta, efp );
        }
        void execute() {
            ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
            boolean sel = efp.isApplicableForSelection();
            ta.setText( "" );
            MidiEvent[] events =
                (sel ? getSelectedNoteOnEvents() :
                    getAllNoteOnEvents( efp.isApplicableForCurrentTrack() ));
            int volAcc = 0;
            int min = -1;
            int max = -1;
            for (int i = 0; i < events.length; i++) {
                ShortMessage sm = (ShortMessage) events[i].getMessage();
                int vol = sm.getData2();
                if (min < 0) { min = vol; }
                else { min = Math.min( min, vol ); }
                if (max < 0) { max = vol; }
                else { max = Math.max( max, vol ); }
                volAcc += vol;
            }
            int avrg = (events.length > 0 ? volAcc / events.length : 0);
            ta.append( rb.getString( "plugin.gridView.info.volume" ) );
            ta.append( "\n" );
            ta.append(
                rb.getString( "plugin.gridView.info.volume.eventCount" ) + " " + events.length + "\n" );
            ta.append(
                rb.getString( "plugin.gridView.info.volume.lowest" ) + " " + min + "\n" );
            ta.append(
                rb.getString( "plugin.gridView.info.volume.highest" ) + " " + max + "\n" );
            ta.append(
                rb.getString( "plugin.gridView.info.volume.average" ) + " " + (events.length > 0 ? "" + avrg : "%") );
        }
    }
    
    class EventTextAction extends TextAction {
        EventTextAction( String s, JTextArea ta, EditFocusPanel efp ) {
            super( s, ta, efp );
        }
        void execute() {
            ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
            boolean sel = efp.isApplicableForSelection();
            ta.setText( "" );
            MidiEvent[] events = (sel ? getSelectedEvents() : getTrack().getAllMidiEvents());
            int shortMessageCount = 0;
            int metaMessageCount = 0;
            int sysexCount = 0;
            TreeMap<String,int[]> map = new TreeMap<String,int[]>();
            
            for (int i = 0; i < events.length; i++) {
                MidiMessage m = events[i].getMessage();
                if (m instanceof ShortMessage) {
                    shortMessageCount++;
                    String command = MidiToolkit.getCommandString( ((ShortMessage) m).getCommand() );
                    int[] o = map.get( command );
                    if (o != null) {
                        o[0]++;
                    } else {
                        int[] val = new int[1];
                        val[0] = 1;
                        map.put( command, val );
                    }
                } else if (m instanceof MetaMessage) {
                    //System.out.println( "found metaMessage at tick " + events[i].getTick() +
                    //        " : " + m + " " + Hex.getHexString( m.getMessage(), true ) + " type: " +
                    //        ((MetaMessage) m).getType() );
                    metaMessageCount++;
                } else if (m instanceof SysexMessage) {
                    sysexCount++;
                }
            }
            ta.append( rb.getString( "plugin.gridView.info.event" ) );
            ta.append( "\n" );
            ta.append(
                rb.getString( "plugin.gridView.info.event.eventCount" ) + " " + events.length + "\n" );
            ta.append(
                rb.getString( "plugin.gridView.info.event.shortMessageCount" ) + " " + shortMessageCount + "\n" );
            ta.append(
                    rb.getString( "plugin.gridView.info.event.metaMessageCount" ) + " " + metaMessageCount + "\n" );
            String title = rb.getString( "plugin.gridView.info.event.sysexMessageCount" );
            ta.append(
                    title + " " + sysexCount + "\n" );
            for (String s : map.keySet()) {
                ta.append( s );
                int i = 0;
                while (s.length() + i + 1 < title.length()) {
                    ta.append( "." );
                    i++;
                }
                ta.append( ": " + ((int[]) map.get( s ))[0] + "\n" );
            }
        }
    }
    
    /**
     * <p>
     * The base class for classes that encapsulate MIDI events and
     * display them as a visible component.
     * </p>
     * @author jreese
     */
    private abstract class EventComponent extends JComponent {
        private static final long serialVersionUID = 1L;

        MidiEvent event;
        TrackProxy track;
        boolean selected;
        boolean tagged;
        
        /**
         * Constructs a new <code>EventComponent</code> with one event
         * per default.
         * @param event The event.
         * @param track The track.
         */
        public EventComponent( MidiEvent event, TrackProxy track ) {
            this.event = event;
            this.track = track;
            this.setBackground( Color.BLACK );
            tagged = false;

            enableEvents( AWTEvent.FOCUS_EVENT_MASK |
                          AWTEvent.MOUSE_EVENT_MASK |
                          AWTEvent.MOUSE_MOTION_EVENT_MASK);
        }
        
        public void addNotify() {
            super.addNotify();
            if (toolTipsEnabled) {
                ToolTipManager.sharedInstance().registerComponent( this );
            }
        }
        
        public void removeNotify() {
            super.removeNotify();
            if (toolTipsEnabled) {
                ToolTipManager.sharedInstance().unregisterComponent( this );
            }
        }
        
        public void setY( int y ) {
            setLocation( getX(), y );
        }

        /**
         * Gets the <code>MidiEvent</code> at the requested index.
         * This default implementation returns the event passed to the constructor.
         * @param index A value with <code>0 &lt;= value &lt; getEventCount()</code>.
         * @return The requested <code>MidiEvent</code>.
         */
        public MidiEvent getEventAt( int index ) { return event; }
        /**
         * Gets the number of events encapsulated by this
         * <code>EventComponent</code>.
         * @return This implementation of this method returns 1.
         */
        public int getEventCount() { return 1; }

        /**
         * Sets the <code>EventComponent</code>s bounds on the
         * <code>GridComponent</code>.
         */
        public void adjustBounds() {
            int x = translateTickX( event.getTick() );
            int y = translateEventY( event );
            int width = Math.max( translateTickX( midiEventTickLength ), 1 );
            int height = rowHeight - 2;
            setBounds( x, y, width, height );
        }
        protected void processMouseEvent( MouseEvent e ) {
            GridComponent.this.processMouseEvent( e );
            if (e.getID() == MouseEvent.MOUSE_ENTERED &&
                editMode == EDIT_MODE_ADD) {
                setCursor( strikethroughCursor );
            } else if (e.getID() == MouseEvent.MOUSE_EXITED) {
                setCursor( Cursor.getDefaultCursor() );
            }
            super.processMouseEvent( e );
        }
        protected void processMouseMotionEvent( MouseEvent e ) {
            GridComponent.this.processMouseMotionEvent( e );
            super.processMouseMotionEvent( e );
        }
        /**
         * Sets the <code>selected</code> state. This has influence
         * on the component's visuality, but will <b>not</b> repaint
         * the component.
         * @param selected The <code>selected</code> state to set,
         * @param r A <code>Rectangle</code> that will be widened by
         * this method if the state changes. Can be used for optimized
         * repainting. May be <code>null</code>
         */
        public void setSelected( boolean selected, Rectangle r ) {
            if (this.selected == selected) { return; }
            if (r != null) {
                r.add( getBounds() );
            }
            this.selected = selected;
        }
        /**
         * Gets the <code>selected</code> state.
         * @return The <code>selected</code> state.
         */
        public boolean isSelected() { return selected; }
    }

    /**
     * <p>
     * A class encapsulating two <code>MidiEvent</code> objects (note_on and
     * note_off) that represent a single note and displaying it as a visible
     * component.
     * </p>
     * @author jreese
     */
    private class NoteComponent extends EventComponent {
        
        private static final long serialVersionUID = 1;
        
        private MidiEvent noteOff;
        
        /**
         * Constructs a new <code>EventComponent</code>.
         * @param event The note_on event.
         * @param noteOff The note_off event.
         */
        public NoteComponent( MidiEvent event, MidiEvent noteOff, TrackProxy track ) {
            super( event, track );
            this.noteOff = noteOff;
        }
        public int getEventCount() { return 2; }
        public MidiEvent getEventAt( int index ) {
            if (index == 0) { return event; }
            if (index == 1) { return noteOff; }
            return null;
        }
        public void adjustBounds() {
            int x = translateTickX( event.getTick() );
            int y = translateEventY( event );
            int width = Math.max( translateTickX( noteOff.getTick() - event.getTick() ), 1 );
            int height = rowHeight - 1;
            setBounds( x, y, width, height );
        }
        public void paintComponent( Graphics g ) {
            if (selected) {
                g.setColor( selectedNoteColor );
            } else {
                int[] v0 = new int[] {
                    noteColor1.getRed(),
                    noteColor1.getGreen(),
                    noteColor1.getBlue(),
                    noteColor1.getAlpha()
                };
                int[] v1 = new int[] {
                    noteColor2.getRed(),
                    noteColor2.getGreen(),
                    noteColor2.getBlue(),
                    noteColor2.getAlpha()
                };
                for (int i = 0; i < v0.length; i++) {
                    if (v0[i] < v1[i]) {
                        v0[i] = v0[i] +
                            (((v1[i] - v0[i]) * ((ShortMessage) event.getMessage()).getData2()) / 127);
                    } else if (v0[i] > v1[i]) {
                        v0[i] = v0[i] -
                            (((v0[i] - v1[i]) * ((ShortMessage) event.getMessage()).getData2()) / 127);
                    }
                }
                g.setColor( new Color( v0[0], v0[1], v0[2], v0[3] ) );
            }
            g.fillRect( 0, 0, getWidth(), getHeight() );
        }
        protected void processMouseMotionEvent( MouseEvent e ) {
            // the following code sets the cursor and the edit modes
            // for mousedragged resizing of note components
            if (e.getID() == MouseEvent.MOUSE_MOVED) {
                if (editMode < EDIT_MODE_ADD) {
                    int width = getWidth();
                    if (width >= 3) {
                        if (e.getX() == 0 || (width >= 5 && e.getX() == 1)) {
                            setCursor( Cursor.getPredefinedCursor( Cursor.W_RESIZE_CURSOR ) );
                            editMode = EDIT_MODE_RESIZE_WEST;
                            editObject = this;
                        } else if (e.getX() == width - 1 || (width >= 5 && e.getX() == width - 2)) {
                            setCursor( Cursor.getPredefinedCursor( Cursor.E_RESIZE_CURSOR ) );
                            editMode = EDIT_MODE_RESIZE_EAST;
                            editObject = this;
                        } else {
                            setCursor( GridComponent.this.getCursor() );
                            editMode = EDIT_MODE_DEFAULT;
                            editObject = null;
                        }
                    }
                }
            }
            super.processMouseMotionEvent( e );
        }
        public String getToolTipText() {
            EventDescriptor ed = getEventMap().getEventDescriptorFor( event );
            return
                "<html>" + SgEngine.getInstance().getResourceBundle().getString(
                    "plugin.gridView.tooltip.midiNote" ) + "<br>" +
                SgEngine.getInstance().getResourceBundle().getString(
                    "plugin.gridView.tooltip.midiNote.position" ) + ": " +
                event.getTick() + "<br>" +
                SgEngine.getInstance().getResourceBundle().getString(
                    "plugin.gridView.tooltip.midiNote.noteValue" ) + ": " +
                    ed.getDescription() + (ed instanceof NoteDescriptor ?
                            " (" + ((NoteDescriptor) ed).getNote() + ")" : "") + "<br>" +
                SgEngine.getInstance().getResourceBundle().getString(
                    "plugin.gridView.tooltip.midiNote.channel" ) + ": " +
                    MidiToolkit.getDefaultMidiChannelNames(
                            false )[((ShortMessage) event.getMessage()).getChannel()] + "<br>" +
                SgEngine.getInstance().getResourceBundle().getString(
                    "plugin.gridView.tooltip.midiNote.volume" ) + ": " +
                ((ShortMessage) event.getMessage()).getData2() + "<br>" +
                SgEngine.getInstance().getResourceBundle().getString(
                    "midi.event.edit.length" ) + ": " +
                (noteOff.getTick() - event.getTick()) + "</html>";
        }
    }

    /**
     * <p>
     * A class encapsulating a <code>MidiEvent</code> object with a
     * <code>ShortMessage</code> payload and displaying it as a visible
     * component.
     * </p>
     * @author jreese
     */
    private class ShortMessageComponent extends EventComponent {
        
        private static final long serialVersionUID = 1;
        
        /**
         * @param event
         */
        public ShortMessageComponent( MidiEvent event, TrackProxy track ) {
            super( event, track );
        }
        public void paintComponent( Graphics g ) {
            if (selected) {
                g.setColor( selectedEventColor );
            } else {
                g.setColor( eventColor );
            }
            g.fillRect( 0, 0, getWidth(), getHeight() );
        }
        public String getToolTipText() {
            return
                "<html>" + SgEngine.getInstance().getResourceBundle().getString(
                    "plugin.gridView.tooltip.shortMessageEvent" ) + "<br>" +
                SgEngine.getInstance().getResourceBundle().getString(
                    "plugin.gridView.editMidiEvents.event.command" ) + ": " +
                (MidiToolkit.isChannelMessage( event.getMessage() ) ?
                    MidiToolkit.getCommandString(
                        ((ShortMessage) event.getMessage()).getCommand() ) + "<br>" +
                        SgEngine.getInstance().getResourceBundle().getString(
                        "plugin.gridView.editMidiEvents.event.channel" ) + ": " :
                            MidiToolkit.getStatusString(
                                    ((ShortMessage) event.getMessage()).getCommand() ) ) +
                ((ShortMessage) event.getMessage()).getChannel() + "</html>";
        }
    }
    
    /**
     * <b>
     * This class implements the ruler component that shall be used with this
     * <code>GridComponent</code>.
     * </b>
     * @author jreese
     */
    private class RulerComponent extends JComponent {
        
        private static final long serialVersionUID = 1;
        
        private Polygon ppoly;
        private Polygon lpoly;
        private Polygon rpoly;
        private JPopupMenu popupMenu;
        private int x;
        private boolean moveLeftMarker;
        private boolean moveRightMarker;
        private boolean movePlayerMarker;
        private Rectangle rect;
        private int rulerHeight = 30;
        
        public RulerComponent() {
            this.setBackground( GridComponent.this.getBackground() );
            setPreferredSize(
                new Dimension(
                    GridComponent.this.getPreferredSize().width, rulerHeight ) );
            enableEvents( AWTEvent.MOUSE_EVENT_MASK );
            enableEvents( AWTEvent.MOUSE_MOTION_EVENT_MASK );
            
            rect = new Rectangle( 0, 0, 1, 1 );
            
            // create player marker polygon
            ppoly = new Polygon();
            ppoly.addPoint( -6, rulerHeight - 5 );
            ppoly.addPoint( -1, rulerHeight );
            ppoly.addPoint( 0, rulerHeight );
            ppoly.addPoint( 5, rulerHeight - 5 );
            lpoly = new Polygon();
            lpoly.addPoint( -5, rulerHeight - 5 );
            lpoly.addPoint( 0, rulerHeight );
            lpoly.addPoint( 1, rulerHeight );
            lpoly.addPoint( 1, rulerHeight - 5 );
            rpoly = new Polygon();
            rpoly.addPoint( 0, rulerHeight - 5 );
            rpoly.addPoint( 0, rulerHeight );
            rpoly.addPoint( 1, rulerHeight );
            rpoly.addPoint( 5, rulerHeight - 5 );
            
            // create popup menu
            popupMenu = new JPopupMenu();
            popupMenu.add( new AbstractAction(
                SgEngine.getInstance().getResourceBundle().getString(
                    "plugin.gridView.trackbar.menu.setLeftMarker" ) )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    midiDescriptor.getMidiRecorder().setLeftMarkerTick( translateXPos( x ) );
                }
            } );
            popupMenu.add( new AbstractAction(
                SgEngine.getInstance().getResourceBundle().getString(
                    "plugin.gridView.trackbar.menu.setRightMarker" ) )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    midiDescriptor.getMidiRecorder().setRightMarkerTick( translateXPos( x ) );
                }
            } );
            popupMenu.add( new AbstractAction(
                SgEngine.getInstance().getResourceBundle().getString(
                    "plugin.gridView.trackbar.menu.setPlayerMarker" ) )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    midiDescriptor.getMidiRecorder().setTickPosition( translateXPos( x ) );
                }
            } );
            popupMenu.addSeparator();
            popupMenu.add( new AbstractAction(
                SgEngine.getInstance().getResourceBundle().getString(
                    "plugin.gridView.trackbar.menu.removeLeftMarker" ) )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    midiDescriptor.getMidiRecorder().setLeftMarkerTick( -1 );
                }
            } );
            popupMenu.add( new AbstractAction(
                SgEngine.getInstance().getResourceBundle().getString(
                    "plugin.gridView.trackbar.menu.removeRightMarker" ) )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    midiDescriptor.getMidiRecorder().setRightMarkerTick( -1 );
                }
            } );
            popupMenu.add( new AbstractAction(
                SgEngine.getInstance().getResourceBundle().getString(
                    "plugin.gridView.trackbar.menu.removePlayerMarker" ) )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    midiDescriptor.getMidiRecorder().setTickPosition( 0 );
                }
            } );
        }
        protected void processMouseEvent( MouseEvent e ) {
            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                moveLeftMarker = false;
                moveRightMarker = false;
                movePlayerMarker = false;
                if (e.isShiftDown()) { // shift down: set left marker
                    midiDescriptor.getMidiRecorder().setLeftMarkerTick( translateXPos( e.getX() ) );
                    moveLeftMarker = true;
                } else if (e.isControlDown()) { // ctrl down: set right marker
                    midiDescriptor.getMidiRecorder().setRightMarkerTick( translateXPos( e.getX() ) );
                    moveRightMarker = true;
                } else {
                    // check if cursor is on left or right marker
                    Polygon p0 = new Polygon(lpoly.xpoints, lpoly.ypoints, lpoly.npoints);
                    p0.translate( leftMarkerPos, 0 );
                    Polygon p1 = new Polygon(rpoly.xpoints, rpoly.ypoints, rpoly.npoints);
                    p1.translate( rightMarkerPos, 0 );
                    Polygon p2 = new Polygon(ppoly.xpoints, ppoly.ypoints, ppoly.npoints);
                    p2.translate( pos, 0 );
                    if (p0.contains( e.getPoint() )) {
                        moveLeftMarker = true;
                    } else if (p1.contains( e.getPoint() )) {
                        moveRightMarker = true;
                    } else if (p2.contains( e.getPoint() )) {
                        movePlayerMarker = true;
                    }
                }
            } else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                if (e.isPopupTrigger()) {
                    this.x = e.getX();
                    popupMenu.show( this, e.getX(), e.getY() );
                } else {
                    if (!moveRightMarker && !moveLeftMarker && !movePlayerMarker) {
                        midiDescriptor.getMidiRecorder().setTickPosition( translateXPos( e.getX() ) );
                    }
                }
            }
            super.processMouseEvent( e );
        }
        protected void processMouseMotionEvent( MouseEvent e ) {
            if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
                rect.x = e.getX();
                rect.y = GridComponent.this.getVisibleRect().y;
                GridComponent.this.scrollRectToVisible( rect );
                if (moveLeftMarker) {
                    midiDescriptor.getMidiRecorder().setLeftMarkerTick( translateXPos( e.getX() ) );
                }
                if (moveRightMarker) {
                    midiDescriptor.getMidiRecorder().setRightMarkerTick( translateXPos( e.getX() ) );
                }
                if (movePlayerMarker) {
                    midiDescriptor.getMidiRecorder().setTickPosition( translateXPos( e.getX() ) );
                }
            }
        }
        public void paintComponent( Graphics g ) {
            Rectangle r = g.getClipBounds();
            g.setColor( getBackground() );
            g.fillRect( r.x, r.y, r.width, r.height );
            paintGrid( g, r, true );
            
            if (pos >= 0) {
                g.translate( pos, 0 );
                g.setColor( pointerColor );
                g.fillPolygon( ppoly );
                g.translate( -pos, 0 );
            }
            if (leftMarkerPos >= 0) {
                g.translate( leftMarkerPos, 0 );
                g.setColor( leftMarkerColor );
                g.fillPolygon( lpoly );
                g.translate( -leftMarkerPos, 0 );
            }
            if (rightMarkerPos >= 0) {
                g.translate( rightMarkerPos, 0 );
                g.setColor( rightMarkerColor );
                g.fillPolygon( rpoly );
                g.translate( -rightMarkerPos, 0 );
            }
        }
        
        private void updatePlayer( int oldPos, int newPos ) {
            int x0 = Math.min( oldPos, newPos );
            repaint( x0 - 6, 0, Math.max( oldPos, newPos ) - x0 + 12, getHeight() );
        }
        
        public Dimension getPreferredSize() {
            int pw = GridComponent.this.getPreferredSize().width;
            Dimension d = super.getPreferredSize();
            if (d.width != pw) {
                d.width = pw;
                //System.out.println( "setting preferred size to " + d );
                setPreferredSize( d );
            }
            return super.getPreferredSize();
        }
    }
    
    // midi events removed
    void midiEventsRemoved(
        SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj ) {
        
        if (changeObj == invalidEditSource) { return; }
        if (changeObj instanceof GridEditSource) {
            GridEditSource ges = (GridEditSource) changeObj;
            System.out.println( ges.toString() );
            if (!isTrackDisplayed( track )) { return; }
    
            // caused by DeleteEventsEdit        
            if (ges.getEditType() == GridEditSource.DELETE) {
                clearSelection();
                removeEventComponents( events );
                repaint();
            } else if (ges.getEditType() == GridEditSource.MOVE) {
            } else if (ges.getEditType() == GridEditSource.ADD) {
                clearSelection();
                removeEventComponents( events);
                repaint();
            } else if (ges.getEditType() == GridEditSource.PASTE) {
                removeEventComponents( events );
                repaint();
            }
            super.revalidate();
            ruler.revalidate();
        } else if (this.track == track) {
            removeEventComponents( events );
            repaint();
        }
    }
    
    // midi added
    void midiEventsAdded(
        SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj ) {
        
        if (changeObj == invalidEditSource) { return; }
        if (changeObj instanceof GridEditSource) {
            GridEditSource ges = (GridEditSource) changeObj;
            System.out.println( ges.toString() );
            if (!isTrackDisplayed( track )) { return; }
            // caused by DeleteEventsEdit
            if (ges.getEditType() == GridEditSource.DELETE) {
                clearSelection();
                addEventComponents( events, true, false );
                repaint();
            } else if (ges.getEditType() == GridEditSource.MOVE) {
                //addEventComponents( events, true );
                selectAll( events );
                updateEventComponentBounds();
            } else if (ges.getEditType() == GridEditSource.CHANGE) {
                //selectEventComponents( events );
                updateEventComponentBounds();
            } else if (ges.getEditType() == GridEditSource.ADD) {
                List<EventComponent> list = addEventComponents( events, true, true );
                // dunno why, but repaint on new components seems
                // necessary
                for (int i = 0; i < list.size(); i++) {
                    list.get( i ).repaint();
                }
            } else if (ges.getEditType() == GridEditSource.PASTE) {
                addEventComponents( events, true, false );
                clearSelection();
                selectAll( events );
            }
            super.revalidate();
            ruler.revalidate();
        } else {
            //System.out.println( "midiEventsAdded( " + changeObj + " )" );
            
            if (this.track == track) {
                addEventComponents( events, false, false );
            }
        }
    }
    
    void midiEventsChanged( SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj ) {
        // just repaint GridComponent, since no
        // event tick change can have happened
        if (changeObj instanceof GridEditSource) {
            repaint();
        } else {
            midiEventsRemoved( sequence, track, events, changeObj );
            midiEventsAdded( sequence, track, events, changeObj );
        }
    }
    
    void midiTrackLengthChanged( SgMidiSequence sequence, TrackProxy track, Object changeObj ) {
        //System.out.println( "GridComponent.midiTrackLengthChanged() : " + track.ticks() );
        revalidate();
        ruler.revalidate();
        
        // if the right marker is set to end of track ('removed' state), update
        setRightMarkerPosition( translateTickX( midiDescriptor.getMidiRecorder().getRightMarkerTick() ) );
    }
    
    /**
     * Sets the event map for the given <code>TrackProxy</code>.
     * @param eventMap The <code>EventMap</code> to set.
     * @param track The <code>TrackProxy</code>.
     */
    void midiTrackEventMapChanged( SgMidiSequence sequence, TrackProxy track, Object changeObj ) {
        if (track == this.track) {
            setTrackImpl();
        }
    }
    
    /**
     * Gets the note offset that would result if the given <code>EventComponent</code> is
     * moved by the given y offset.
     * @param ec The <code>EventComponent</code>.
     * @param yOffset The y offset. A negative value indicates upward direction, positive
     * downward.
     * @return The note offset that has to be added to the EventComponent's <code>MidiEvent</code>s.
     */
    private short getNoteOffset( EventComponent ec, int yOffset ) {
        EventDescriptor ed = getEventMap().getEventDescriptorFor( ec.event );
        if (ed instanceof NoteDescriptor) {
            NoteDescriptor nd = (NoteDescriptor) ed;
            int noteY = translateEventY( ec.event );
            EventDescriptor translatedEd = translateYPos( noteY + yOffset );
            if (translatedEd instanceof NoteDescriptor) {
                return (short) (((NoteDescriptor) translatedEd).getNote() - nd.getNote());
            } else {
                return 0;
            }
        }
        return 0;
    }
}
