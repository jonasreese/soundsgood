/*
 * Created on 03.10.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.sampled.AudioFormat;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import com.jonasreese.sound.aucontainer.AUContainer;
import com.jonasreese.sound.aucontainer.AudioUnitDescriptor;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.audio.AudioDeviceDescriptor;
import com.jonasreese.sound.sg.midi.EventMap;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.MidiDeviceDescriptor;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.midi.SgMidiSequence;
import com.jonasreese.sound.sg.midi.TrackProxy;
import com.jonasreese.sound.sg.midi.edit.AddEventsEdit;
import com.jonasreese.sound.sg.midi.edit.ChangeEventsEdit;
import com.jonasreese.sound.sg.midi.edit.ChangeTempoEdit;
import com.jonasreese.sound.sg.midi.edit.ChangeTrackNameEdit;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.ui.defaultui.action.ViewAction;
import com.jonasreese.sound.vstcontainer.VstContainer;
import com.jonasreese.sound.vstcontainer.VstPluginDescriptor;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <p>
 * This class is a toolkit class for the Swing UI
 * implementation of the SoundsGood application framework.
 * </p>
 * @author jreese
 */
public class UiToolkit {
    public static final Icon SPACER =
        new ResourceLoader( UiToolkit.class, "resource/spacer.gif" ).getAsIcon();

    private static boolean macOs = System.getProperty( "mrj.version" ) != null;
    private static StaticActionPool actionPool;
    private static Frame mainFrame;
    private static JMenuBar defaultMenuBar;
    private static SessionUi sessionUi;
    private static HashMap<Session,SessionUi> sessionUiMap = new HashMap<Session,SessionUi>();

    private UiToolkit() {
    }

    /**
     * Tries to find the <code>ViewContainer</code> component
     * of some child component. This method searches through the
     * component hierarchy until it finds either a parent that is
     * instance of <code>ViewContainer</code>, or no parent any more.
     * @param someChildComponent The child component to find the
     *        view container for.
     * @return The view container as a <code>Component</code> which
     *         is also an instance of <code>ViewContainer</code>,
     *         or <code>null</code> if not found.
     */
    public static Component getViewContainer( Component someChildComponent ) {
        Component c = someChildComponent;
        while (c != null && !(c instanceof ViewContainer)) {
            c = c.getParent();
        }
        return c;
    }
    
    public static boolean isMacOs() {
        return macOs;
    }
    
    /**
     * Gets a <code>KeyStroke</code> for the given resource key.
     * @param resourceKey The key to the keystroke value in the resource bundle.
     * A keystroke resource value shall have the format
     * <code>keystroke "|" alternat_keystroke</code>, e.g. <code>ctrl c | meta c</code>
     * @return The corresponding <code>KeyStroke</code>. If an alternate keystroke is
     * defined, it will be returned on Mac systems.
     */
    public static KeyStroke getKeyStroke( String resourceKey ) {
        String s = SgEngine.getInstance().getResourceBundle().getString( resourceKey );
        if (s != null) {
            StringTokenizer st = new StringTokenizer( s, "|" );
            String key = st.nextToken();
            if (isMacOs() && st.hasMoreTokens()) {
                return KeyStroke.getKeyStroke( st.nextToken().trim() );
            }
            return KeyStroke.getKeyStroke( key.trim() );
        }
        
        return null;
    }
    
    /**
     * Constructs a toolbar button for the given action and returns it.
     * @param action The action to create a toolbar button for.
     * @return The according <code>AbstractButton</code>.
     */
    public static AbstractButton createToolbarButton( Action action ) {
        return createToolbarButton( action, false );
    }
    
    /**
     * Constructs a toolbar button for the given action and returns it.
     * @param action The action to create a toolbar button for.
     * @param toggle If set to <code>true</code>, indicates that a <code>JToggleButton</code>
     * shall be returned rather than a <code>JButton</code>.
     * @return The according <code>AbstractButton</code>.
     */
    public static AbstractButton createToolbarButton( Action action, boolean toggle ) {
        AbstractButton butt = null;
        if (action instanceof ViewAction) {
            if (((ViewAction) action).getView().isMultipleInstancePerSessionElementAllowed()) {
                butt = new JButton( action );
            } else {
                butt = new JToggleButton( action );
            }
            butt.setText( null );
            ((ViewAction) action).addButton( butt );
        }
        if (butt == null) {
            if (toggle) {
                butt = new JButton( action );
            } else {
                butt = new JButton( action/*(Icon) action.getValue( Action.SMALL_ICON )*/ );
            }
            butt.setText( null );
            action.putValue( "button", butt );
            if (action instanceof ViewAction) {
                ((ViewAction) action).addButton( butt );
                butt.setDisabledIcon( SPACER );
            } else {
                action.addPropertyChangeListener( new PropertyChangeListener()
                {
                    public void propertyChange( PropertyChangeEvent e )
                    {
                        if ("enabled".equals( e.getPropertyName() ))
                        {
                            ((AbstractButton)
                                ((Action) e.getSource()).getValue( "button" )).setEnabled(
                                    ((Boolean) e.getNewValue()).booleanValue() );
                        }
                    }
                } );
            }
        }
        //butt.addActionListener( action );
        butt.setEnabled( action.isEnabled() );
        butt.setMaximumSize( new Dimension( 22, 22 ) );
        butt.setToolTipText( (String) action.getValue( "toolTipText" ) );
        butt.setMargin( new Insets( 2, 2, 2, 2 ) );
        butt.setFocusable( false );
        return butt;
    }
    
    /**
     * Sets the active <code>StaticActionPool</code>.
     * @param pool The <code>StaticActionPool</code> to set.
     */
    public static void setActionPool( StaticActionPool pool ) {
        actionPool = pool;
    }
    
    /**
     * Gets the currently active <code>StaticActionPool</code>.
     * @return The current StaticActionPool.
     */
    public static StaticActionPool getActionPool() { return actionPool; }
    
    /**
     * Gets the main application's <code>Frame</code>.
     * The frame can be used for modal <code>Dialog</code>s.
     * @return The application <code>Frame</code>.
     */
    public static Frame getMainFrame() { return mainFrame; }
    
    /**
     * Sets the main application's <code>Frame</code>.
     * The frame can be used for modal <code>Dialog</code>s.
     * @param frame The application <code>Frame</code>.
     */
    public static void setMainFrame( Frame frame ) { mainFrame = frame; }
    
    /**
     * Sets the current <code>SessionUi</code>.
     * @param theSessionUi The <code>SessionUi</code>, or <code>null</code>.
     */
    public static void setSessionUi( SessionUi theSessionUi ) {
        sessionUi = theSessionUi;
    }

    /**
     * Gets the current <code>SessionUi</code>.
     * @return The <code>SessionUi</code>, or <code>null</code>.
     */
    public static SessionUi getSessionUi() {
        return sessionUi;
    }

    /**
     * Sets the default menu bar.
     * @param defaultMB The default <code>JMenuBar</code> to set.
     */
    public static void setDefaultMenuBar( JMenuBar defaultMB ) {
        defaultMenuBar = defaultMB;
    }

    /**
     * Gets the default menu bar.
     * @return The default menu bar.
     */
    public static JMenuBar getDefaultMenuBar() {
        return defaultMenuBar;
    }
    
    /**
     * Gets the <code>SessionUi</code> for the given session.
     * @param session The session to get the session UI for.
     * @return The according <code>SessionUi</code>, or <code>null</code> if none
     * exists for the given <code>Session</code>.
     */
    public static SessionUi getSessionUi( Session session ) {
        synchronized (sessionUiMap) {
            return sessionUiMap.get( session );
        }
    }
    
    /**
     * Adds a <code>SessionUi</code> mapping.
     * @param session The session.
     * @param theSessionUi The session UI to be mapped.
     */
    public static void addSessionUi( Session session, SessionUi theSessionUi ) {
        synchronized (sessionUiMap) {
            removeSessionUi( session );
            theSessionUi.sessionUiAdded( session );
            sessionUiMap.put( session, theSessionUi );
        }
    }

    /**
     * Removes a <code>SessionUi</code> mapping.
     * @param session The session.
     * @return <code>true</code> if the session map has been removed,
     *         <code>false</code> otherwise.
     */
    public static boolean removeSessionUi( Session session ) {
        synchronized (sessionUiMap) {
            SessionUi sessionUi = sessionUiMap.remove( session );
            if (sessionUi != null) {
                sessionUi.sessionUiRemoved( session );
                return true;
            }
            return false;
        }
    }
    
    /**
     * Displays a dialog that allows the user to edit the name of the
     * given MIDI track.
     * @param changeObj The change object for the undoable edit that is automatically created.
     * @param midiDescriptor The MIDI descriptor containing the sequence that contains
     *        the given track.
     * @param track The track.
     * @param parent The component that shall be the parent of the created dialog.
     * @return The new track name, or <code>null</code> if the user chose not
     *         to set a track name at all or aborted the dialog.
     */
    public static String showEditTrackNameDialog(
        Object changeObj, MidiDescriptor midiDescriptor, TrackProxy track, Component parent ) {
        
        if (track == null) {
            return null;
        }
        String trackName = track.getTrackName();
        JCheckBox cb = new JCheckBox( SgEngine.getInstance().getResourceBundle().getString(
            "midi.track.editTrackName.storeNameInTrack" ), (trackName != null) );
        cb.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                JComponent c = (JComponent) e.getSource();
                c = (JComponent) c.getParent();
                Component[] comps = ((JComponent) c.getParent()).getComponents();
                for (int i = 0; i < comps.length; i++) {
                    if (comps[i] instanceof JPanel && comps[i] != c) {
                        if (((JPanel) comps[i]).getComponent( 0 ) instanceof JTextField) {
                            ((JTextField) ((JPanel) comps[i]).getComponent( 0 )).setEnabled(
                                ((JCheckBox) e.getSource()).isSelected() );
                        }
                    }
                }
            }
        } );
        JPanel panel = new JPanel( new BorderLayout() );
        JPanel cbPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        cbPanel.add( cb );
        cbPanel.setBorder( new TitledBorder(
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.track.editTrackName.setName" ) ) );
        panel.add( cbPanel, BorderLayout.NORTH );
        JPanel p = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        JTextField textField = new JTextField( (trackName != null ? trackName : ""), 30 );
        textField.setEnabled( cb.isSelected() );
        textField.selectAll();
        p.add( textField );
        p.setBorder( new TitledBorder(
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.track.editTrackName.trackName" ) ) );
        panel.add( p );
        Object[] message = new Object[] {
            panel,
        };

        int option = JOptionPane.showConfirmDialog(
            parent,
            message,
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.track.editTrackName.title" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION) {
            String name;
            if (cb.isSelected()) {
                name = textField.getText();
            } else {
                name = null;
            }
            if (name == null && trackName == null ||
                name != null && name.equals( trackName ) ) {
                System.out.println( "track name left unchanged!" );
                // nothing to do...
            } else {
                ChangeTrackNameEdit edit = new ChangeTrackNameEdit(
                    track,
                    name,
                    midiDescriptor,
                    changeObj );
                edit.perform();
                midiDescriptor.getUndoManager().addEdit( edit );
                return name;
            }
        }
        return null;
    }

    /**
     * Displays a dialog where the user can set the tempo (in BPM or MPQ).
     * @param midiDescriptor The <code>MidiDescriptor</code> for which the tempo is to be
     * changed. Must not be <code>null</code>.
     * @param tick The tick position where the tempo shall be set. Default is 0 (track tempo).
     */
    public static void showSetTempoDialog( MidiDescriptor midiDescriptor, long tick ) {
        ResourceBundle rb = SgEngine.getInstance().getResourceBundle();

        SgMidiSequence seq = null;
        try {
            seq = midiDescriptor.getSequence();
        } catch (InvalidMidiDataException e) {
        } catch (IOException e) {
        }
        if (seq == null) {
            return;
        }
        TrackProxy[] tracks = seq.getTrackProxies();
        MidiEvent event = null;
        TrackProxy track = null;
        for (int i = 0; event == null && i < tracks.length; i++) {
            event = tracks[i].getTempoEvent();
            track = tracks[i];
        }
        if (event == null) {
            int option = JOptionPane.showConfirmDialog(
                getMainFrame(), rb.getString( "midi.tempo.noTempoSet.message" ),
                rb.getString( "midi.tempo.noTempoSet.title" ),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE );
            if (option == JOptionPane.YES_OPTION) {
                TrackProxy tempTrack = null;
                try {
                    tempTrack = midiDescriptor.getSequence().getSelectedTrackProxy();
                } catch (InvalidMidiDataException e) {
                } catch (IOException e) {
                }
                if (tempTrack != null) {
                    track = tempTrack;
                    float mpq = MidiToolkit.bpmToMPQ( SgEngine.getInstance().getProperties().getDefaultMidiTempo() );
                    event = MidiToolkit.createTempoEvent( mpq );
                    final MidiDescriptor descriptor = midiDescriptor;
                    AddEventsEdit edit = new AddEventsEdit(
                        track,
                        new MidiEvent[] { event },
                        midiDescriptor,
                        rb.getString( "midi.tempo.addTempoEvent" ),
                        UiToolkit.class )
                    {
                        private static final long serialVersionUID = 1;
                        public void perform()
                        {
                            super.perform();
                            float mpq = MidiToolkit.getTempoInMPQ( (MetaMessage) events[0].getMessage() );
                            descriptor.setTempoInMpq( mpq );
                        }
                        public void undo()
                        {
                            super.undo();
                            descriptor.setTempoInMpq( -1 );
                        }
                    };
                    edit.perform();
                    midiDescriptor.getUndoManager().addEdit( edit );
                }
            }
        }
        if (event != null) {
            float mpq = MidiToolkit.getTempoInMPQ( (MetaMessage) event.getMessage() );
            
            final SetTempoPanel tempoPanel = new SetTempoPanel( mpq );
            
            Object[] message = new Object[]
            {
                tempoPanel,
            };
            JOptionPane pane = new JOptionPane(
                message, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null, tempoPanel );
            JDialog d = pane.createDialog(
                    UiToolkit.getMainFrame(), rb.getString( "midi.tempo.title" ) );
            d.addComponentListener( new ComponentAdapter() {
                public void componentShown(ComponentEvent e) {
                    tempoPanel.requestFocus();
                }
            } );
            d.setVisible( true );
            Integer selectedValue = (Integer) pane.getValue();
            if (selectedValue != null && selectedValue.intValue() == JOptionPane.OK_OPTION && tempoPanel.hasChanged()) {
                // TODO: improve this!
                
                float mpqVal = tempoPanel.getMPQValue();
                ChangeTempoEdit undoableEdit = new ChangeTempoEdit(
                    midiDescriptor,
                    track,
                    event,
                    mpqVal,
                    UiToolkit.class );
                undoableEdit.perform();
                midiDescriptor.getUndoManager().addEdit( undoableEdit );
            }
        }
    }
    
    /**
     * Displays a dialog that allows the user to assign input devices and MIDI channels to the
     * given MIDI track.
     * @param midiDescriptor The MIDI descriptor containing the sequence that contains
     * the given track.
     * @param track The track.
     * @param trackName The track name.
     */
    public static void showAssignMidiInputDevicesDialog(
            MidiDescriptor midiDescriptor, TrackProxy track ) {
        
        Frame m = getMainFrame();
        AssignInputDevicesDialog d = new AssignInputDevicesDialog( m, midiDescriptor, track );
        d.pack();
        d.setLocation( m.getX() + m.getWidth() / 2 - d.getWidth() / 2, m.getY() + m.getHeight() / 2 - d.getHeight() / 2 );
        d.setVisible( true );
    }

    /**
     * Displays a dialog that allows the user to assign output devices and MIDI channels to the
     * given MIDI sequence.
     * @param midiDescriptor The MIDI descriptor containing the sequence.
     * @param track The track.
     */
    public static void showAssignMidiOutputDevicesDialog(
            MidiDescriptor midiDescriptor ) {
        
        Frame m = getMainFrame();
        AssignOutputDevicesDialog d = new AssignOutputDevicesDialog( m, midiDescriptor );
        d.pack();
        d.setLocation( m.getX() + m.getWidth() / 2 - d.getWidth() / 2, m.getY() + m.getHeight() / 2 - d.getHeight() / 2 );
        d.setVisible( true );
    }
    
    /**
     * Shows an edit midi events dialog. This method calls either
     * <code>showEditShortMessageEventDialog()</code> or
     * <code>showEditNoteDialog()</code>, or none, depending on the current
     * selection. Please note that this dialog is
     * only for ONE event component. If more than one event component is
     * selected, this method takes only the first component within the
     * selection. If no event components are selected when this method is
     * called, it returns and does nothing.
     */
    public static void showEditMidiEventsDialog( TrackProxy track, MidiDescriptor midiDescriptor ) {
        if (track.isSelectionEmpty()) { return; }
        MidiEvent[] selEvents = track.getSelectedEvents();

        // Since the note dialog is more restrictive, it will be called first.
        if (!showEditNoteDialog( selEvents, track, midiDescriptor ) && selEvents.length > 0) {
            showEditShortMessageEventDialog( selEvents[0], track, midiDescriptor );
        }
    }

    /**
     * Shows an edit midi event dialog. 
     * @param event The events to be edited.
     * @return <code>true</code> if the dialog has been shown.
     */
    public static boolean showEditShortMessageEventDialog(
            MidiEvent event, TrackProxy track, MidiDescriptor midiDescriptor ) {
        MidiEditPanel mep = new MidiEditPanel(
            track,
            event,
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.event.edit.event" ) );
        Object[] message = new Object[] { mep };
        int option = JOptionPane.showConfirmDialog(
            getMainFrame(), message,
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.event.edit.title" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION && mep.hasChanged()) {
            try {
                // create change edit and perform changes
                Object changeObj = new Object();
                ChangeEventsEdit edit = new ChangeEventsEdit(
                    track,
                    new MidiEvent[] { event },
                    midiDescriptor,
                    null,
                    changeObj );
                mep.applyChanges( changeObj );
                edit.perform();
                midiDescriptor.getUndoManager().addEdit( edit );
            } catch (InvalidMidiDataException imdex) {
                JOptionPane.showMessageDialog(
                    getMainFrame(),
                    imdex.getMessage(),
                    SgEngine.getInstance().getResourceBundle().getString(
                        "error.invalidMidiData" ),
                    JOptionPane.ERROR_MESSAGE );
            }
        }
        return true;
    }


    /**
     * Shows an edit midi notes dialog. Please note that this dialog is
     * only for ONE note component (for two events - on/off). If more
     * than one note component is selected, this method takes only the
     * first component within the selection. If no event components are
     * selected when this method is called, it returns and does nothing.
     * @param events The events array containing the midi events for the
     *        note to be edited (at least two elements).
     * @return <code>true</code> if the dialog has been shown.
     */
    public static boolean showEditNoteDialog( MidiEvent[] events, TrackProxy track, MidiDescriptor midiDescriptor ) {
        if (!checkEditNote( events )) {
            return false;
        }
        NoteEditPanel notePanel = new NoteEditPanel(
            track,
            events,
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.event.edit.event" ),
            track.getEventMap() );

        Object[] message = new Object[] { notePanel };
        int option = JOptionPane.showConfirmDialog(
            getMainFrame(), message,
            SgEngine.getInstance().getResourceBundle().getString(
                "midi.event.note.edit.title" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION && notePanel.hasChanged()) {
            try {
                // create change edit and perform changes
                Object changeObj = new Object();
                ChangeEventsEdit edit = new ChangeEventsEdit(
                    track,
                    events,
                    midiDescriptor,
                    null,
                    changeObj );
                notePanel.applyChanges( changeObj );
                edit.perform();
                midiDescriptor.getUndoManager().addEdit( edit );
            } catch (InvalidMidiDataException imdex) {
                JOptionPane.showMessageDialog(
                    getMainFrame(),
                    imdex.getMessage(),
                    SgEngine.getInstance().getResourceBundle().getString(
                        "error.invalidMidiData" ),
                    JOptionPane.ERROR_MESSAGE );
            }
        }
        return true;
    }
    
    private static boolean checkEditNote( MidiEvent[] events ) {
        // check condition
        if (events.length < 2 || events.length % 2 != 0) {
            return false;
        }
        for (int i = 0; i < events.length; i+= 2) {
            if (!(events[i].getMessage() instanceof ShortMessage) ||
                !(events[i + 1].getMessage() instanceof ShortMessage) ||
                (((ShortMessage) events[i].getMessage()).getCommand() != ShortMessage.NOTE_ON &&
                    ((ShortMessage) events[i + 1].getMessage()).getCommand() != ShortMessage.NOTE_OFF) ||
                (((ShortMessage) events[i + 1].getMessage()).getCommand() != ShortMessage.NOTE_ON &&
                    ((ShortMessage) events[i + 1].getMessage()).getCommand() != ShortMessage.NOTE_OFF)) {
                
                return false;
            }
        }
        return true;
    }

    /**
     * Shows a dialog that enables the user to select MIDI output devices.
     * @param multipleSelectionAllowed if <code>true</code>, indicates that more than
     * one output device can be selected.
     * @return The selected <code>MidiDeviceDescriptor</code>s as an array,
     * or <code>null</code> if the dialog was cancelled.
     */
    public static MidiDeviceDescriptor[] showSelectMidiOutputDeviceDialog(
            boolean multipleSelectionAllowed ) {
        JList list = new JList(
                SgEngine.getInstance().getProperties().getMidiOutputDeviceList().getDeviceDescriptors() );
        list.setSelectionMode(
                multipleSelectionAllowed ?
                        ListSelectionModel.MULTIPLE_INTERVAL_SELECTION :
                            ListSelectionModel.SINGLE_SELECTION  );
        Object[] message = new Object[] { new JScrollPane( list ) };
        int option = JOptionPane.showConfirmDialog(
            getMainFrame(), message,
            SgEngine.getInstance().getResourceBundle().getString(
                "device.select.output.midi" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION) {
            Object[] o = list.getSelectedValues();
            MidiDeviceDescriptor[] result = new MidiDeviceDescriptor[o.length];
            for (int i = 0; i < o.length; i++) {
                result[i] = (MidiDeviceDescriptor) o[i];
            }
            return result;
        }
        return null;
    }

    /**
     * Shows a dialog that enables the user to select MIDI input devices.
     * @param multipleSelectionAllowed if <code>true</code>, indicates that more than
     * one output device can be selected.
     * @return The selected <code>MidiDeviceDescriptor</code>s as an array,
     * or <code>null</code> if the dialog was cancelled.
     */
    public static MidiDeviceDescriptor[] showSelectMidiInputDeviceDialog(
            boolean multipleSelectionAllowed ) {
        JList list = new JList(
                SgEngine.getInstance().getProperties().getMidiInputDeviceList().getDeviceDescriptors() );
        list.setSelectionMode(
                multipleSelectionAllowed ?
                        ListSelectionModel.MULTIPLE_INTERVAL_SELECTION :
                            ListSelectionModel.SINGLE_SELECTION  );
        Object[] message = new Object[] { new JScrollPane( list ) };
        int option = JOptionPane.showConfirmDialog(
            getMainFrame(), message,
            SgEngine.getInstance().getResourceBundle().getString(
                "device.select.input.midi" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION) {
            Object[] o = list.getSelectedValues();
            MidiDeviceDescriptor[] result = new MidiDeviceDescriptor[o.length];
            for (int i = 0; i < o.length; i++) {
                result[i] = (MidiDeviceDescriptor) o[i];
            }
            return result;
        }
        return null;
    }

    /**
     * Shows a dialog that enables the user to select audio output devices.
     * @param multipleSelectionAllowed if <code>true</code>, indicates that more than
     * one output device can be selected.
     * @return The selected <code>AudioDeviceDescriptor</code>s as an array,
     * or <code>null</code> if the dialog was cancelled.
     */
    public static AudioDeviceDescriptor[] showSelectAudioOutputDeviceDialog(
            boolean multipleSelectionAllowed ) {
        JList list = new JList(
                SgEngine.getInstance().getProperties().getAudioOutputDeviceList().getDeviceDescriptors() );
        list.setSelectionMode(
                multipleSelectionAllowed ?
                        ListSelectionModel.MULTIPLE_INTERVAL_SELECTION :
                            ListSelectionModel.SINGLE_SELECTION  );
        Object[] message = new Object[] { new JScrollPane( list ) };
        int option = JOptionPane.showConfirmDialog(
            getMainFrame(), message,
            SgEngine.getInstance().getResourceBundle().getString(
                "device.select.output.audio" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION) {
            Object[] o = list.getSelectedValues();
            AudioDeviceDescriptor[] result = new AudioDeviceDescriptor[o.length];
            for (int i = 0; i < o.length; i++) {
                result[i] = (AudioDeviceDescriptor) o[i];
            }
            return result;
        }
        return null;
    }

    /**
     * Shows a dialog that enables the user to select audio input devices.
     * @param multipleSelectionAllowed if <code>true</code>, indicates that more than
     * one output device can be selected.
     * @return The selected <code>AudioDeviceDescriptor</code>s as an array,
     * or <code>null</code> if the dialog was cancelled.
     */
    public static AudioDeviceDescriptor[] showSelectAudioInputDeviceDialog(
            boolean multipleSelectionAllowed ) {
        JList list = new JList(
                SgEngine.getInstance().getProperties().getAudioInputDeviceList().getDeviceDescriptors() );
        list.setSelectionMode(
                multipleSelectionAllowed ?
                        ListSelectionModel.MULTIPLE_INTERVAL_SELECTION :
                            ListSelectionModel.SINGLE_SELECTION  );
        Object[] message = new Object[] { new JScrollPane( list ) };
        int option = JOptionPane.showConfirmDialog(
            getMainFrame(), message,
            SgEngine.getInstance().getResourceBundle().getString(
                "device.select.input.audio" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION) {
            Object[] o = list.getSelectedValues();
            AudioDeviceDescriptor[] result = new AudioDeviceDescriptor[o.length];
            for (int i = 0; i < o.length; i++) {
                result[i] = (AudioDeviceDescriptor) o[i];
            }
            return result;
        }
        return null;
    }
    
    /**
     * Shows a dialog that allows the user to select a VST plugin from list that
     * displays all VSTPlugins that are available in the system.
     * @param preSelection The pre-selected element.
     * @return A <code>VstPlugin</code> if the user selcted one, or <code>null</code>
     * if the user selected none or cancelled the dialog.
     */
    public static VstPluginDescriptor showSelectVstPluginDialog( VstPluginDescriptor preSelection ) {
        VstPluginDescriptor[] vstPlugins = VstContainer.getInstance().getAllVstPluginDescriptors();
        VstItem[] items = new VstItem[vstPlugins.length];
        VstItem sel = null;
        for (int i = 0; i < vstPlugins.length; i++) {
            items[i] = new VstItem( vstPlugins[i] );
            if (vstPlugins[i] == preSelection) {
                sel = items[i];
            }
        }
        JList list = new JList( items );
        list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        if (sel != null) {
            list.setSelectedValue( sel, true );
        }
        Object[] message = new Object[] { new JScrollPane( list ) };
        int option = JOptionPane.showConfirmDialog(
            getMainFrame(), message,
            SgEngine.getInstance().getResourceBundle().getString(
                "vstplugin.select" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION) {
            Object[] o = list.getSelectedValues();
            if (o != null && o.length > 0) {
                VstPluginDescriptor result = ((VstItem) o[0]).vstPlugin;
                return result;
            }
        }
        return null;
    }
    
    private static class VstItem {
        VstPluginDescriptor vstPlugin;
        VstItem( VstPluginDescriptor vstPlugin ) { this.vstPlugin = vstPlugin; }
        public String toString() { return vstPlugin.getName(); }
    }

    /**
     * Shows a dialog that allows the user to select an Audio Unit from list that
     * displays all AudioUnits that are available in the system.
     * @param preSelection The pre-selected element.
     * @return An <code>AudioUnitDescriptor</code> if the user selcted one, or <code>null</code>
     * if the user selected none or cancelled the dialog.
     */
    public static AudioUnitDescriptor showSelectAudioUnitDialog( AudioUnitDescriptor preSelection ) {
        AudioUnitDescriptor[] audioUnits = AUContainer.getInstance().getAllAudioUnitDescriptors();
        AuItem[] items = new AuItem[audioUnits.length];
        AuItem sel = null;
        for (int i = 0; i < audioUnits.length; i++) {
            items[i] = new AuItem( audioUnits[i] );
            if (audioUnits[i] == preSelection) {
                sel = items[i];
            }
        }
        JList list = new JList( items );
        list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        if (sel != null) {
            list.setSelectedValue( sel, true );
        }
        Object[] message = new Object[] { new JScrollPane( list ) };
        int option = JOptionPane.showConfirmDialog(
            getMainFrame(), message,
            SgEngine.getInstance().getResourceBundle().getString(
                "audiounit.select" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION) {
            Object[] o = list.getSelectedValues();
            if (o != null && o.length > 0) {
                AudioUnitDescriptor result = ((AuItem) o[0]).audioUnit;
                return result;
            }
        }
        return null;
    }
    
    private static class AuItem {
        AudioUnitDescriptor audioUnit;
        AuItem( AudioUnitDescriptor audioUnit ) { this.audioUnit = audioUnit; }
        public String toString() { return audioUnit.getName() + " (" + audioUnit.getType() + ")"; }
    }

    
    /**
     * Displays a dialog that allows the user to set the number of repeatings of the
     * given MIDI note on the given MIDI track. If the user confirms, an undoable edit
     * is created and added to the provided <code>MidiDescriptor</code>'s
     * <code>UndoManager</code>.
     * @param midiDescriptor The <code>MidiDescriptor</code>.
     * @param track The MIDI track. It is assumed that this track belongs to the
     * given <code>MidiDescriptor</code>'s <code>SgMidiSequence</code>.
     * @param events The events that describe the note to be repeated. It is assumed
     * that these events belong to the given MIDI track.
     */
    public static void showRepeatNoteDialog(
            MidiDescriptor midiDescriptor, TrackProxy track, MidiEvent[] events ) {
        ResourceBundle rb = SgEngine.getInstance().getResourceBundle();

        SgMidiSequence sequence = null;
        try {
            sequence = midiDescriptor.getSequence();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (sequence == null) {
            return;
        }
        
        RepeatNotePanel repeatNotePanel = new RepeatNotePanel( 1, 4 );
        
        Object[] message = new Object[] {
            repeatNotePanel,
        };
        int option = JOptionPane.showConfirmDialog(
            getMainFrame(), message,
            rb.getString( "midi.note.repeat.title" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION) {
            int tactCount = repeatNotePanel.getTactCountValue();
            int countPerTact = repeatNotePanel.getCountPerTactValue();
            MidiEvent[] addedEvents = MidiToolkit.repeatEvents( sequence, events, countPerTact, tactCount );
            
            
            AddEventsEdit undoableEdit = new AddEventsEdit(
                    track,
                    addedEvents,
                    midiDescriptor,
                    rb.getString( "edit.repeatEventsEdit" ),
                    UiToolkit.class );
            undoableEdit.perform();
            midiDescriptor.getUndoManager().addEdit( undoableEdit );
        }
    }
    
    /**
     * Displays a dialog that allows the user to add/remove MIDI event types to to/from
     * a MIDI event map.
     * @param midiDescriptor The parent <code>MidiDescriptor</code>
     * @param eventMap The <code>EventMap</code> to be edited.
     */
    public static void showEditEventMappingDialog( MidiDescriptor midiDescriptor, EventMap eventMap ) {
        Frame m = getMainFrame();
        EditEventMappingDialog d = new EditEventMappingDialog( m, midiDescriptor, eventMap );
        d.pack();
        d.setLocation( m.getX() + m.getWidth() / 2 - d.getWidth() / 2, m.getY() + m.getHeight() / 2 - d.getHeight() / 2 );
        d.setVisible( true );
    }
    
    
    /**
     * Shows a dialog where the user can input an audio format based on the given
     * default format.
     * @param format The default format. <code>null</code> indicates that the SoundsGood
     * default audio format shall be used.
     * @return The adjusted audio format, or <code>null</code> if the user canceled the
     * dialog.
     */
    public static AudioFormat showAudioFormatDialog( AudioFormat format ) {

        AudioFormatPanel p = new AudioFormatPanel( format );
        int option = JOptionPane.showConfirmDialog(
                getMainFrame(), p,
                SgEngine.getInstance().getResourceBundle().getString( "audio.format" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION) {
            return p.getAudioFormat();
        }
        return null;
    }
    
}