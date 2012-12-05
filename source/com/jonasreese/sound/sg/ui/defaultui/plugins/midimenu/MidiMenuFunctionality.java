/*
 * Created on 14.10.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.midimenu;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SessionEvent;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.MidiEventSelectionEvent;
import com.jonasreese.sound.sg.midi.MidiEventSelectionListener;
import com.jonasreese.sound.sg.midi.SgMidiSequence;
import com.jonasreese.sound.sg.midi.TrackProxy;
import com.jonasreese.sound.sg.midi.TrackSelectionEvent;
import com.jonasreese.sound.sg.midi.TrackSelectionListener;
import com.jonasreese.sound.sg.midi.edit.AddTrackEdit;
import com.jonasreese.sound.sg.midi.edit.RemoveTrackEdit;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.plugin.functionality.SessionElementSelectionDependentFunctionality;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.sound.sg.ui.defaultui.plugins.grid.GridEditSource;
import com.jonasreese.util.ParametrizedResourceBundle;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <p>
 * This <code>Functionality</code> implementation adds midi-specific menues to
 * the menu bar if a single <code>MidiDescriptor</code> is selected within the current
 * session.
 * </p> 
 * @author jonas.reese
 */
public class MidiMenuFunctionality extends SessionElementSelectionDependentFunctionality
implements TrackSelectionListener, MidiEventSelectionListener {

    private MidiDescriptor midiDescriptor;
    private JMenu midiMenu;
    private Action setTempoAction;
    private Action changeTempoAction;
    private Action addTrackAction;
    private Action renameTrackAction;
    private Action deleteTrackAction;
    private Action assignInputDevicesAction;
    private Action assignOutputDevicesAction;
    private Action editMidiEventsAction;
    private Action playMidiNoteAction;
    private SgAction insertMidiNoteAction;
    private Action repeatMidiNoteAction;
    private static final MidiEvent[] NON_NULL_ME_ARRAY = new MidiEvent[0];
    private TrackProxy track;
    
    public MidiMenuFunctionality() {
        midiMenu = new JMenu( SgEngine.getInstance().getResourceBundle().getString(
        "plugin.midiMenu" ) );
    
        final ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        editMidiEventsAction = new AbstractAction(
                rb.getString(
                    "plugin.midiMenu.editMidi" ),
                UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e )
            {
                MidiDescriptor midiDescriptor = MidiMenuFunctionality.this.midiDescriptor;
                TrackProxy track = null;
                if (midiDescriptor != null) {
                    try {
                        track = midiDescriptor.getSequence().getSelectedTrackProxy();
                    } catch (InvalidMidiDataException e1) {
                    } catch (IOException e1) {
                    }
                }
                if (track != null) {
                    UiToolkit.showEditMidiEventsDialog( track, midiDescriptor );
                }
            }
        };
        editMidiEventsAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.midiMenu.editMidi.shortDescription" ) );
        editMidiEventsAction.setEnabled( false );
        midiMenu.add( editMidiEventsAction );
    
        playMidiNoteAction = new AbstractAction(
                rb.getString(
                    "plugin.midiMenu.playMidiNote" ),
                new ResourceLoader( "com/jonasreese/sound/sg/ui/defaultui/resource/play.gif" ).getAsIcon() ) {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e ) {
                // workaround: remove gridComponent from deviceUpdatable list
                // to avoid scrolling to the player position when playing a note
                try {
                    MidiEvent[] onoff = getSelectedNote( false );
                    if (onoff != null && midiDescriptor != null) {
                        midiDescriptor.getMidiRecorder().playSingleNote( onoff[0], onoff[1] );
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(
                        UiToolkit.getMainFrame(),
                        rb.getString(
                            "plugin.playerView.errorOnPlayText" ) + "\n" +
                        ex.getMessage(),
                        rb.getString(
                            "plugin.playerView.errorOnPlay" ),
                        JOptionPane.ERROR_MESSAGE );
                }
                // add to deviceUpdatable list where we removed it before...
            }
        };
        playMidiNoteAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.midiMenu.playMidiNote.shortDescription" ) );
        playMidiNoteAction.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( "ctrl P" ) );
        playMidiNoteAction.setEnabled( false );
        midiMenu.add( playMidiNoteAction );
    
        insertMidiNoteAction = new SgAction(
                rb.getString(
                    "plugin.midiMenu.insertMidiNote" ),
                UiToolkit.SPACER );
        insertMidiNoteAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.midiMenu.insertMidiNote.shortDescription" ) );
        midiMenu.add( insertMidiNoteAction );

        repeatMidiNoteAction = new AbstractAction(
                rb.getString(
                    "plugin.midiMenu.repeatMidiNote" ),
                UiToolkit.SPACER ) {
                    private static final long serialVersionUID = 1L;
                    public void actionPerformed( ActionEvent e ) {
                        try {
                            SgMidiSequence sequence = midiDescriptor.getSequence();
                            TrackProxy track = sequence.getSelectedTrackProxy();
                            if (track != null) {
                                MidiEvent[] events = track.getSelectedEvents();
                                if (events != null) {
                                    UiToolkit.showRepeatNoteDialog( midiDescriptor, track, events );
                                }
                            }
                        } catch (InvalidMidiDataException imdex) {
                            imdex.printStackTrace();
                        } catch (IOException ioex) {
                            ioex.printStackTrace();
                        }
                    }
        };
        repeatMidiNoteAction.putValue( SgAction.TOOL_TIP_TEXT, 
                rb.getString( "plugin.midiMenu.repeatMidiNote.shortDescription" ) );
        repeatMidiNoteAction.setEnabled( false );
        midiMenu.add( repeatMidiNoteAction );
        
        midiMenu.addSeparator();
        
        addTrackAction = new AbstractAction(
                rb.getString(
                    "plugin.midiMenu.addTrack" ),
                UiToolkit.SPACER )
        {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e )
            {
                if (midiDescriptor != null) {
                    try {
                        AddTrackEdit edit = new AddTrackEdit(
                            midiDescriptor.getSequence(),
                            midiDescriptor,
                            MidiMenuFunctionality.this );
                        edit.perform();
                        midiDescriptor.getUndoManager().addEdit( edit );
                    } catch (InvalidMidiDataException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };
        addTrackAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.midiMenu.addTrack.shortDescription" ) );
        midiMenu.add( addTrackAction );
        renameTrackAction = new AbstractAction(
            rb.getString(
                "plugin.midiMenu.renameTrack" ),
            UiToolkit.SPACER )
        {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e )
            {
                TrackProxy track = getTrack();
                if (track != null) {
                    UiToolkit.showEditTrackNameDialog(
                        MidiMenuFunctionality.this,
                        midiDescriptor,
                        track,
                        UiToolkit.getMainFrame() );
                }
            }
        };
        renameTrackAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.midiMenu.renameTrack.shortDescription" ) );
        midiMenu.add( renameTrackAction );
        deleteTrackAction = new AbstractAction(
            rb.getString(
                "plugin.midiMenu.deleteTrack" ),
            new ResourceLoader(
                    "com/jonasreese/sound/sg/ui/defaultui/resource/remove.gif" ).getAsIcon() )
        {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e )
            {
                TrackProxy track = getTrack();
                if (track != null) {
                    try {
                        RemoveTrackEdit edit = new RemoveTrackEdit(
                            midiDescriptor.getSequence(),
                            track,
                            midiDescriptor,
                            new GridEditSource( GridEditSource.REMOVE_TRACK, hashCode(), null ) );
                        edit.perform();
                        midiDescriptor.getUndoManager().addEdit( edit );
                    } catch (InvalidMidiDataException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };
        deleteTrackAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.midiMenu.deleteTrack.shortDescription" ) );
        midiMenu.add( deleteTrackAction );
        
        setTempoAction = new AbstractAction(
            rb.getString(
                "plugin.midiMenu.setTempo" ),
            new ResourceLoader( getClass(), "resource/tempo.gif" ).getAsIcon() ) {
            static final long serialVersionUID = 0;
            public void actionPerformed( ActionEvent e ) {
                UiToolkit.showSetTempoDialog( midiDescriptor, 0 );
            }
        };
        midiMenu.add( setTempoAction );
        changeTempoAction = new AbstractAction(
            rb.getString(
                "plugin.midiMenu.changeTempo" ), UiToolkit.SPACER ) {
            static final long serialVersionUID = 0;
            public void actionPerformed( ActionEvent e ) {
                UiToolkit.showSetTempoDialog( midiDescriptor, midiDescriptor.getMidiRecorder().getTickPosition() );
            }
        };
        changeTempoAction.putValue( "toolTipText",
            rb.getString(
                "plugin.midiMenu.changeTempo.description" ) );
        midiMenu.add( changeTempoAction );
        midiMenu.addSeparator();
        assignOutputDevicesAction = new AbstractAction(
                rb.getString(
                "plugin.midiMenu.assignOutputDevices" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e ) {
                UiToolkit.showAssignMidiOutputDevicesDialog( midiDescriptor );
            }
        };
        assignOutputDevicesAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.midiMenu.assignOutputDevices.shortDescription" ) );
        midiMenu.add( assignOutputDevicesAction );
        assignInputDevicesAction = new AbstractAction(
                rb.getString(
                "plugin.midiMenu.assignInputDevices" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e ) {
                TrackProxy track = getTrack();
                UiToolkit.showAssignMidiInputDevicesDialog(
                        midiDescriptor, track );
            }
        };
        assignInputDevicesAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.midiMenu.assignInputDevices.shortDescription" ) );
        midiMenu.add( assignInputDevicesAction );
    }
    
    protected void updateSelection( SessionElementDescriptor[] selObjs ) {
        if (selObjs != null && selObjs.length == 1 && selObjs[0] instanceof MidiDescriptor) {
            if (this.midiDescriptor != null) {
                try {
                    this.midiDescriptor.getSequence().removeTrackSelectionListener( this );
                } catch (InvalidMidiDataException e) {
                } catch (IOException e) {
                }
            }
            this.midiDescriptor = (MidiDescriptor) selObjs[0];
            if (this.midiDescriptor != null) {
                try {
                    this.midiDescriptor.getSequence().addTrackSelectionListener( this );
                    setTrack( this.midiDescriptor.getSequence().getSelectedTrackProxy() );
                    //System.out.println( "trackSelectionListener added - " + this.midiDescriptor );
                } catch (InvalidMidiDataException e) {
                } catch (IOException e) {
                }
            }
            enableMidiMenu();
        } else {
            if (this.midiDescriptor != null) {
                try {
                    this.midiDescriptor.getSequence().removeTrackSelectionListener( this );
                } catch (InvalidMidiDataException e) {
                } catch (IOException e) {
                }
            }
            this.midiDescriptor = null;
            disableMidiMenu();
        }
    }
    
    public Object getProperty( String name ) {
        if ("editMidiEventsAction".equals( name )) {
            return editMidiEventsAction;
        }
        if ("playMidiNoteAction".equals( name )) {
            return playMidiNoteAction;
        }
        if ("insertMidiNoteAction".equals( name )) {
            return insertMidiNoteAction;
        }
        if ("repeatMidiNoteAction".equals( name )) {
            return repeatMidiNoteAction;
        }
        if ("addTrackAction".equals( name )) {
            return addTrackAction;
        }
        if ("assignInputDevicesAction".equals( name )) {
            return assignInputDevicesAction;
        }
        if ("assignOutputDevicesAction".equals( name )) {
            return assignOutputDevicesAction;
        }
        if ("deleteTrackAction".equals( name )) {
            return deleteTrackAction;
        }
        if ("renameTrackAction".equals( name )) {
            return renameTrackAction;
        }
        if ("setTempoAction".equals( name )) {
            return setTempoAction;
        }

        return null;
    }
    
    public void init() {
        super.init();
        
        disableMidiMenu();
    }
    
    private MidiEvent[] getSelectedNote( boolean checkOnly ) {
        //System.out.println( "getSelectedNote()" );
        MidiDescriptor midiDescriptor = this.midiDescriptor;
        TrackProxy track = getTrack();
        if (track == null) {
            //System.out.println( "getSelectedNote() : track == null, getTrack() = " + getTrack() );
            return null;
        }
        int ec = track.getSelectedEventCount();
        //System.out.println( "number of selected events: " + ec );
        if (midiDescriptor != null && ec > 0 && ec <= 2) {
            MidiEvent noteOn = track.getSelectedEventAt( 0 );
            if (noteOn.getMessage() instanceof ShortMessage) {
                // play single NOTE_OFF event
                if (((ShortMessage) noteOn.getMessage()).getCommand() == ShortMessage.NOTE_OFF) {
                    return (checkOnly ? NON_NULL_ME_ARRAY : new MidiEvent[] { noteOn, null });
                } else if (((ShortMessage) noteOn.getMessage()).getCommand() == ShortMessage.NOTE_ON) {
                    if (ec == 1) {
                        //System.out.println( "getSelectedNote() : ec == 1" );
                        return null;
                    }
                    MidiEvent noteOff = track.getSelectedEventAt( 1 );
                    if (noteOff.getMessage() instanceof ShortMessage) {
                        ShortMessage sm = (ShortMessage) noteOff.getMessage();
                        if (sm.getCommand() == ShortMessage.NOTE_OFF ||
                                sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() == 0) {
                            return (checkOnly ? NON_NULL_ME_ARRAY : new MidiEvent[] { noteOn, noteOff });
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private void setTrack( TrackProxy track ) {
        //System.out.println( "setTrack( " + (track == null ? "null" : track.getTrackName()) + " )" );
        if (track == this.track) {
            return;
        }
        if (this.track != null) {
            this.track.removeMidiEventSelectionListener( this );
        }
        this.track = track;
        if (this.track != null) {
            this.track.addMidiEventSelectionListener( this );
        }
        
        deleteTrackAction.setEnabled( track != null );
        renameTrackAction.setEnabled( track != null );
    }
    
    private void enableMidiMenu() {
        addTrackAction.setEnabled( true );
        assignOutputDevicesAction.setEnabled( true );
        assignInputDevicesAction.setEnabled( true );
        setTempoAction.setEnabled( true );
    }
    
    private void disableMidiMenu() {
        addTrackAction.setEnabled( false );
        assignInputDevicesAction.setEnabled( false );
        assignOutputDevicesAction.setEnabled( false );
        deleteTrackAction.setEnabled( false );
        editMidiEventsAction.setEnabled( false );
        insertMidiNoteAction.setEnabled( false );
        repeatMidiNoteAction.setEnabled( false );
        playMidiNoteAction.setEnabled( false );
        renameTrackAction.setEnabled( false );
        setTempoAction.setEnabled( false );
    }
    
    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.Plugin#getName()
     */
    public String getName() {
        return "Midi menu";
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.Plugin#getShortDescription()
     */
    public String getShortDescription() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.Plugin#getDescription()
     */
    public String getDescription() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.Plugin#getPluginName()
     */
    public String getPluginName() {
        return "MIDI menu functionality";
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.Plugin#getPluginVersion()
     */
    public String getPluginVersion() {
        return "1.0";
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.Plugin#getPluginVendor()
     */
    public String getPluginVendor() {
        return "Jonas Reese";
    }
    
    public void sessionActivated( SessionEvent e ) {
        super.sessionActivated( e );
        
        if (e.getSession() != null) {
            final JMenuBar menuBar = UiToolkit.getSessionUi(
                    e.getSession()).getMenuBar();
            if (menuBar != null) {
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        menuBar.add( midiMenu, 3 );
                        menuBar.validate(); // this must be called due to some strange reason...
                    }
                } );
            }
        }
    }
    
    public void sessionDeactivated( SessionEvent e ) {
        super.sessionDeactivated( e );
        
        if (e.getSession() != null) {
            final JMenuBar menuBar = UiToolkit.getSessionUi(
                    e.getSession()).getMenuBar();
            if (menuBar != null) {
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        menuBar.remove( midiMenu );
                        menuBar.validate();
                    }
                } );
            }
        }
    }
    
    private TrackProxy getTrack() {
        MidiDescriptor md = midiDescriptor;
        if (md == null) {
            return null;
        }
        try {
            return midiDescriptor.getSequence().getSelectedTrackProxy();
        } catch (InvalidMidiDataException e1) {
        } catch (IOException e1) {
        }
        return null;
    }


    public void trackSelectionChanged( TrackSelectionEvent e ) {
        //System.out.println( "trackSelectionChanged: " + (e.getTrack() == null ? "null" : e.getTrack().getTrackName()) );
        setTrack( e.getTrack() );
    }

    public void midiEventSelectionUpdate( MidiEventSelectionEvent e ) {
//        System.out.println( "midiEventSelectionUpdate(): cleared=" + e.isSelectionCleared()
//                + ", all=" + e.isAllSelected() + ", removed=" + e.isRemovedFromSelection()
//                + ", added=" + e.isAddedToSelection() + ", empty=" + e.getTrack().isSelectionEmpty() );
        boolean notEmpty = !e.getTrack().isSelectionEmpty();
        editMidiEventsAction.setEnabled( notEmpty );
        playMidiNoteAction.setEnabled( getSelectedNote( true ) != null );
        repeatMidiNoteAction.setEnabled( getSelectedNote( true ) != null );
    }

    public PluginConfigurator getPluginConfigurator() {
        return null;
    }
}