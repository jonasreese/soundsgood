/*
 * Created on 09.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbusmenu;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.xml.sax.SAXException;

import com.jonasreese.sound.aucontainer.AUContainer;
import com.jonasreese.sound.aucontainer.AudioUnit;
import com.jonasreese.sound.aucontainer.AudioUnitDescriptor;
import com.jonasreese.sound.aucontainer.AudioUnitNotAvailableException;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SessionEvent;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.plugin.functionality.SessionElementSelectionDependentFunctionality;
import com.jonasreese.sound.sg.soundbus.AudioInputNode;
import com.jonasreese.sound.sg.soundbus.AudioOutputNode;
import com.jonasreese.sound.sg.soundbus.AudioSamplerNode;
import com.jonasreese.sound.sg.soundbus.AudioUnitNode;
import com.jonasreese.sound.sg.soundbus.IllegalSoundbusDescriptionException;
import com.jonasreese.sound.sg.soundbus.MidiBranchNode;
import com.jonasreese.sound.sg.soundbus.MidiFilterNode;
import com.jonasreese.sound.sg.soundbus.MidiInputNode;
import com.jonasreese.sound.sg.soundbus.MidiJunctionNode;
import com.jonasreese.sound.sg.soundbus.MidiNoteCounterNode;
import com.jonasreese.sound.sg.soundbus.MidiOutputNode;
import com.jonasreese.sound.sg.soundbus.MidiSamplerNode;
import com.jonasreese.sound.sg.soundbus.NetworkAudioInputNode;
import com.jonasreese.sound.sg.soundbus.NetworkAudioOutputNode;
import com.jonasreese.sound.sg.soundbus.OSCNode;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusAdapter;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.SoundbusEvent;
import com.jonasreese.sound.sg.soundbus.SoundbusException;
import com.jonasreese.sound.sg.soundbus.SoundbusListener;
import com.jonasreese.sound.sg.soundbus.TempoNode;
import com.jonasreese.sound.sg.soundbus.VstNode;
import com.jonasreese.sound.sg.soundbus.edit.AddSbNodeEdit;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.sound.vstcontainer.VstContainer;
import com.jonasreese.sound.vstcontainer.VstPlugin;
import com.jonasreese.sound.vstcontainer.VstPluginDescriptor;
import com.jonasreese.sound.vstcontainer.VstPluginNotAvailableException;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * @author jonas.reese
 */
public class SoundbusMenuFunctionality extends SessionElementSelectionDependentFunctionality {
    private static Icon liveIcon = new ResourceLoader(
            SoundbusMenuFunctionality.class, "resource/live.gif" ).getAsIcon();

    private SoundbusDescriptor soundbusDescriptor;
    private JMenu soundbusMenu;
    private Action addTempoAction;
    private Action addMidiInputAction;
    private Action addMidiOutputAction;
    private Action addMidiJunctionAction;
    private Action addMidiBranchAction;
    private Action addMidiFilterAction;
    private Action addMidiSamplerAction;
    private Action addMidiNoteCounterAction;
    private Action addAudioInputAction;
    private Action addNetworkAudioInputAction;
    private Action addAudioOutputAction;
    private Action addNetworkAudioOutputAction;
    private Action addAudioSamplerAction;
    private Action addVstPluginAction;
    private Action addAudioUnitAction;
    private Action addOSCReceiverAction;
    private Action setLiveAction;
    private JMenu addMenu;
    private JCheckBoxMenuItem setLiveMenuItem;
    private SoundbusListener soundbusListener;
    
    public SoundbusMenuFunctionality() {
        ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        addMenu = new JMenu( rb.getString( "plugin.soundbusMenu.add" ) );
        addMenu.setIcon( UiToolkit.SPACER );
        addTempoAction = new AbstractAction(
                rb.getString( "soundbus.node.tempo" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                addTempo();
            }
        };
        addTempoAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.tempo.shortDescription" ) );
        addMenu.add( addTempoAction );
        addMidiInputAction = new AbstractAction(
                rb.getString( "soundbus.node.midiInput" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                addMidiInput();
            }
        };
        addMidiInputAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.midiInput.shortDescription" ) );
        addMenu.addSeparator();
        addMenu.add( addMidiInputAction );
        addMidiOutputAction = new AbstractAction(
                rb.getString( "soundbus.node.midiOutput" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                addMidiOutput();
            }
        };
        addMidiOutputAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.midiOutput.shortDescription" ) );
        addMenu.add( addMidiOutputAction );
        addMidiBranchAction = new AbstractAction(
                rb.getString( "soundbus.node.midiBranch" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                addMidiBranch( 3 );
            }
        };
        addMidiBranchAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.midiBranch.shortDescription" ) );
        addMenu.add( addMidiBranchAction );
        addMidiJunctionAction = new AbstractAction(
                rb.getString( "soundbus.node.midiJunction" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                addMidiJunction( 3 );
            }
        };
        addMidiJunctionAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.midiJunction.shortDescription" ) );
        addMenu.add( addMidiJunctionAction );
        addMidiFilterAction = new AbstractAction(
                rb.getString( "soundbus.node.midiFilter" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                addMidiFilter();
            }
        };
        addMidiFilterAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.midiFilter.shortDescription" ) );
        addMenu.add( addMidiFilterAction );
        addMidiSamplerAction = new AbstractAction(
                rb.getString( "soundbus.node.midiSampler" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                addMidiSampler();
            }
        };
        addMidiSamplerAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.midiSampler.shortDescription" ) );
        addMenu.add( addMidiSamplerAction );
        addMidiNoteCounterAction = new AbstractAction(
                rb.getString( "soundbus.node.midiNoteCounter" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                addMidiNoteCounter();
            }
        };
        addMidiNoteCounterAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.midiNoteCounter.shortDescription" ) );
        addMenu.add( addMidiNoteCounterAction );
        addMenu.addSeparator();
        addAudioInputAction = new AbstractAction(
                rb.getString( "soundbus.node.audioInput" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                addAudioInput();
            }
        };
        addAudioInputAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.audioInput.shortDescription" ) );
        addMenu.add( addAudioInputAction );

        addNetworkAudioInputAction = new AbstractAction(
                rb.getString( "soundbus.node.networkAudioInput" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                addNetworkAudioInput();
            }
        };
        addNetworkAudioInputAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.networkAudioInput.shortDescription" ) );
        addMenu.add( addNetworkAudioInputAction );
        addAudioOutputAction = new AbstractAction(
                rb.getString( "soundbus.node.audioOutput" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                addAudioOutput();
            }
        };
        addAudioOutputAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.audioOutput.shortDescription" ) );
        addMenu.add( addAudioOutputAction );
        addNetworkAudioOutputAction = new AbstractAction(
                rb.getString( "soundbus.node.networkAudioOutput" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                addNetworkAudioOutput();
            }
        };
        addNetworkAudioOutputAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.networkAudioOutput.shortDescription" ) );
        addMenu.add( addNetworkAudioOutputAction );
        addAudioSamplerAction = new AbstractAction(
                rb.getString( "soundbus.node.audioSampler" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                addAudioSampler();
            }
        };
        addAudioSamplerAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.audioSampler.shortDescription" ) );
        addMenu.add( addAudioSamplerAction );

        
        addVstPluginAction = new AbstractAction(
                rb.getString( "soundbus.node.vstPlugin" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                if (VstContainer.getInstance().isVstContainerAvailable()) {
                    VstPluginDescriptor vstPluginDesc = UiToolkit.showSelectVstPluginDialog( null );
                    if (vstPluginDesc != null) {
                        try {
                            addVstPlugin( vstPluginDesc.createPlugin() );
                        } catch (VstPluginNotAvailableException vpnaex) {
                            vpnaex.printStackTrace();
                            JOptionPane.showMessageDialog(
                                UiToolkit.getMainFrame(),
                                SgEngine.getInstance().getResourceBundle().getString( "error.cannotCreateVstPlugin.text",
                                                vpnaex.getMessage() ),
                                SgEngine.getInstance().getResourceBundle().getString(
                                        "error.cannotCreateVstPlugin" ),
                                JOptionPane.ERROR_MESSAGE );
                        }
                    }
                }
            }
        };
        addVstPluginAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.vstPlugin.shortDescription" ) );
        if (VstContainer.getInstance().isVstContainerAvailable()) {
            addMenu.addSeparator();
            addMenu.add( addVstPluginAction );
        }

        addAudioUnitAction = new AbstractAction(
                rb.getString( "plugin.soundbusMenu.add.audioUnit" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                if (VstContainer.getInstance().isVstContainerAvailable()) {
                    AudioUnitDescriptor auDesc = UiToolkit.showSelectAudioUnitDialog( null );
                    if (auDesc != null) {
                        try {
                            addAudioUnit( auDesc.createPlugin() );
                        } catch (AudioUnitNotAvailableException aunaex) {
                            aunaex.printStackTrace();
                            JOptionPane.showMessageDialog(
                                UiToolkit.getMainFrame(),
                                SgEngine.getInstance().getResourceBundle().getString( "error.cannotCreateAudioUnit.text",
                                                aunaex.getMessage() ),
                                SgEngine.getInstance().getResourceBundle().getString(
                                        "error.cannotCreateAudioUnit" ),
                                JOptionPane.ERROR_MESSAGE );
                        }
                    }
                }
            }
        };
        addAudioUnitAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.vstPlugin.shortDescription" ) );
        if (AUContainer.getInstance().isAUContainerAvailable()) {
            addMenu.add( addAudioUnitAction );
        }

        addOSCReceiverAction = new AbstractAction(
                rb.getString( "plugin.soundbusMenu.add.oscReceiver" ), UiToolkit.SPACER ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                addOSCReceiver();
            }
        };
        addOSCReceiverAction.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.soundbusMenu.add.oscReceiver.shortDescription" ) );
        addMenu.addSeparator();
        addMenu.add( addOSCReceiverAction );

        soundbusMenu = new JMenu( rb.getString( "plugin.soundbusMenu" ) );
        soundbusMenu.add( addMenu );
        soundbusMenu.addSeparator();

        setLiveAction = new AbstractAction( rb.getString( "plugin.soundbusMenu.sbEnable" ), liveIcon ) {
            private static final long serialVersionUID = 1L;
            public void actionPerformed( ActionEvent e ) {
                setSoundbusLive( !isSoundbusLive() );
            }
        };
        setLiveAction.putValue(
                SgAction.TOOL_TIP_TEXT, rb.getString( "plugin.soundbusMenu.sbEnable.shortDescription" ) );
        setLiveMenuItem = new JCheckBoxMenuItem( setLiveAction );
        soundbusMenu.add( setLiveMenuItem );
        
        soundbusListener = new SoundbusAdapter() {
            public void soundbusClosed( SoundbusEvent e ) {
                setLiveMenuItem.setSelected( false );
            }
            public void soundbusOpened( SoundbusEvent e ) {
                setLiveMenuItem.setSelected( true );
            }
            public void tempoChanged( SoundbusEvent e ) {
            }
        };
    }
    
    protected void updateSelection( SessionElementDescriptor[] selObjs ) {
        if (selObjs != null && selObjs.length == 1 && selObjs[0] instanceof SoundbusDescriptor) {
            this.soundbusDescriptor = (SoundbusDescriptor) selObjs[0];
            enableSbMenu();
        } else {
            disableSbMenu();
            this.soundbusDescriptor = null;
        }
    }
    
    public boolean isSoundbusLive() {
        Soundbus soundbus = null;
        try {
            soundbus = soundbusDescriptor.getSoundbus();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (soundbus != null && soundbus.isOpen());
    }
    
    public void setSoundbusLive( boolean live ) {
        final Soundbus soundbus;
        try {
            soundbus = soundbusDescriptor.getSoundbus();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (soundbus == null || soundbus.isOpen() == live) {
            return;
        }
        
        boolean open = true;
        try {
            if (soundbus.isOpen()) {
                open = false;
                soundbus.close();
            } else {
                soundbus.open();
            }
        } catch (SoundbusException sex) {
            sex.printStackTrace();
            String nodeName = (sex.getNode() == null ? 
                    SgEngine.getInstance().getResourceBundle().getString( "error.cannotOpenSoundbus.unknownNode" )
                    : sex.getNode().getName());
            JOptionPane.showMessageDialog(
                UiToolkit.getMainFrame(),
                SgEngine.getInstance().getResourceBundle().getString(
                        open ? "error.cannotOpenSoundbus.text" : "error.cannotCloseSoundbus.text",
                                nodeName,
                                sex.getMessage() ),
                SgEngine.getInstance().getResourceBundle().getString(
                        open ? "error.cannotOpenSoundbus" : "error.cannotCloseSoundbus" ),
                JOptionPane.ERROR_MESSAGE );
        }
    }
    
    public Object getProperty( String name ) {
        if ("addTempoAction".equals( name )) {
            return addTempoAction;
        }
        if ("addMidiInputAction".equals( name )) {
            return addMidiInputAction;
        }
        if ("addMidiOutputAction".equals( name )) {
            return addMidiOutputAction;
        }
        if ("addMidiJunctionAction".equals( name )) {
            return addMidiJunctionAction;
        }
        if ("addMidiBranchAction".equals( name )) {
            return addMidiBranchAction;
        }
        if ("addMidiFilterAction".equals( name )) {
            return addMidiFilterAction;
        }
        if ("addMidiSamplerAction".equals( name )) {
            return addMidiSamplerAction;
        }
        if ("addMidiNoteCounterAction".equals( name )) {
            return addMidiNoteCounterAction;
        }
        if ("addAudioInputAction".equals( name )) {
            return addAudioInputAction;
        }
        if ("addNetworkAudioInputAction".equals( name )) {
            return addNetworkAudioInputAction;
        }
        if ("addAudioOutputAction".equals( name )) {
            return addAudioOutputAction;
        }
        if ("addNetworkAudioOutputAction".equals( name )) {
            return addNetworkAudioOutputAction;
        }
        if ("addAudioSamplerAction".equals( name )) {
            return addAudioSamplerAction;
        }
        if ("addVstPluginAction".equals( name )) {
            return addVstPluginAction;
        }
        if ("addAudioUnitAction".equals( name )) {
            return addAudioUnitAction;
        }
        if ("addOSCReceiverAction".equals( name )) {
            return addOSCReceiverAction;
        }
        if ("setLiveAction".equals( name )) {
            return setLiveAction;
        }

        return null;
    }
    
    public void init() {
        super.init();
        disableSbMenu();
    }
    
    private void enableSbMenu() {
        SoundbusDescriptor sd = this.soundbusDescriptor;
        if (sd != null) {
            try {
                Soundbus s = sd.getSoundbus();
                s.addSoundbusListener( soundbusListener );
            } catch (IllegalSoundbusDescriptionException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
        addMenu.setEnabled( true );
        addTempoAction.setEnabled( true );
        addMidiInputAction.setEnabled( true );
        addMidiOutputAction.setEnabled( true );
        addMidiJunctionAction.setEnabled( true );
        addMidiBranchAction.setEnabled( true );
        addMidiFilterAction.setEnabled( true );
        addMidiSamplerAction.setEnabled( true );
        addAudioInputAction.setEnabled( true );
        addNetworkAudioInputAction.setEnabled( true );
        addAudioOutputAction.setEnabled( true );
        addNetworkAudioOutputAction.setEnabled( true );
        addVstPluginAction.setEnabled( true );
        setLiveAction.setEnabled( true );
        setLiveMenuItem.setEnabled( true );
    }
    
    private void disableSbMenu() {
        SoundbusDescriptor sd = this.soundbusDescriptor;
        if (sd != null) {
            try {
                Soundbus s = sd.getSoundbus();
                s.removeSoundbusListener( soundbusListener );
            } catch (IllegalSoundbusDescriptionException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
        addMenu.setEnabled( false );
        addTempoAction.setEnabled( false );
        addMidiInputAction.setEnabled( false );
        addMidiOutputAction.setEnabled( false );
        addMidiJunctionAction.setEnabled( false );
        addMidiBranchAction.setEnabled( false );
        addMidiFilterAction.setEnabled( false );
        addMidiSamplerAction.setEnabled( false );
        addAudioInputAction.setEnabled( false );
        addNetworkAudioInputAction.setEnabled( false );
        addAudioOutputAction.setEnabled( false );
        addNetworkAudioOutputAction.setEnabled( false );
        addVstPluginAction.setEnabled( false );
        setLiveAction.setEnabled( false );
        setLiveMenuItem.setEnabled( false );
    }
    
    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.Plugin#getName()
     */
    public String getName() {
        return "Soundbus menu";
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
        return "Soundbus menu functionality";
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
                        menuBar.add( soundbusMenu, 3 );
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
                        menuBar.remove( soundbusMenu );
                        menuBar.validate();
                    }
                } );
            }
        }
    }
    
    public PluginConfigurator getPluginConfigurator() {
        return null;
    }
    
    
    
    /**
     * Adds a tempo node using an <code>UndoableEdit</code>.
     */
    public void addTempo() {
        AddSbNodeEdit edit = new AddTempoNodeEdit( soundbusDescriptor );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }
    
    static class AddTempoNodeEdit extends AddSbNodeEdit {
        AddTempoNodeEdit( SoundbusDescriptor desc ) {
            super( desc, null );
        }
        private static final long serialVersionUID = 1L;
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            TempoNode node = sb.addTempoNode();
            return node;
        }
    }
    
    /**
     * Adds a MIDI input node using an <code>UndoableEdit</code>.
     */
    public void addMidiInput() {
        AddSbNodeEdit edit = new AddMidiInputNodeEdit( soundbusDescriptor );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }
    
    static class AddMidiInputNodeEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;

        AddMidiInputNodeEdit( SoundbusDescriptor desc ) {
            super( desc, null );
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            MidiInputNode node = sb.addMidiInputNode();
            return node;
        }
    }
    
    /**
     * Adds a MIDI output node using an <code>UndoableEdit</code>.
     */
    public void addMidiOutput() {
        AddSbNodeEdit edit = new AddMidiOutputNodeEdit( soundbusDescriptor );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }
    
    static class AddMidiOutputNodeEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;
        AddMidiOutputNodeEdit( SoundbusDescriptor desc ) {
            super( desc, null );
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            MidiOutputNode node = sb.addMidiOutputNode();
            return node;
        }
    }

    /**
     * Adds a MIDI branch node using an <code>UndoableEdit</code>.
     * @param numOutputs The number of outputs the created MIDI branch node shall have.
     */
    public void addMidiBranch( int numOutputs ) {
        AddSbNodeEdit edit = new AddMidiBranchNodeEdit( soundbusDescriptor, numOutputs );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }

    static class AddMidiBranchNodeEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;
        int numOutputs;
        AddMidiBranchNodeEdit( SoundbusDescriptor desc, int numOutputs ) {
            super( desc, null );
            this.numOutputs = numOutputs;
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            MidiBranchNode node = sb.addMidiBranchNode( numOutputs );
            return node;
        }
    }
    
    /**
     * Adds a MIDI junction node using an <code>UndoableEdit</code>.
     * @param numInputs The number of inputs the created MIDI branch node shall have.
     */
    public void addMidiJunction( int numInputs ) {
        AddSbNodeEdit edit = new AddMidiJunctionNodeEdit( soundbusDescriptor, numInputs );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }
    
    static class AddMidiJunctionNodeEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;
        int numInputs;
        AddMidiJunctionNodeEdit( SoundbusDescriptor desc, int numInputs ) {
            super( desc, null );
            this.numInputs = numInputs;
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            MidiJunctionNode node = sb.addMidiJunctionNode( numInputs );
            return node;
        }
    }
    
    /**
     * Adds a MIDI filter node using an <code>UndoableEdit</code>.
     */
    public void addMidiFilter() {
        AddSbNodeEdit edit = new AddMidiFilterNodeEdit( soundbusDescriptor );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }
    
    static class AddMidiFilterNodeEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;
        AddMidiFilterNodeEdit( SoundbusDescriptor desc ) {
            super( desc, null );
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            MidiFilterNode node = sb.addMidiFilterNode();
            return node;
        }
    }

    /**
     * Adds a MIDI sampler node using an <code>UndoableEdit</code>.
     */
    public void addMidiSampler() {
        AddSbNodeEdit edit = new AddMidiSamplerNodeEdit( soundbusDescriptor );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }
    
    static class AddMidiSamplerNodeEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;
        AddMidiSamplerNodeEdit( SoundbusDescriptor desc ) {
            super( desc, null );
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            MidiSamplerNode node = sb.addMidiSamplerNode();
            return node;
        }
    }

    /**
     * Adds a MIDI sampler node using an <code>UndoableEdit</code>.
     */
    public void addMidiNoteCounter() {
        AddSbNodeEdit edit = new AddMidiNoteCounterNodeEdit( soundbusDescriptor );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }
    
    static class AddMidiNoteCounterNodeEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;
        AddMidiNoteCounterNodeEdit( SoundbusDescriptor desc ) {
            super( desc, null );
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            MidiNoteCounterNode node = sb.addMidiNoteCounterNode();
            return node;
        }
    }

    /**
     * Adds a VST plugin node using an <code>UndoableEdit</code>.
     * @param vstPlugin The VST plugin.
     */
    public void addVstPlugin( VstPlugin vstPlugin ) {
        AddSbNodeEdit edit = new AddVstNodeEdit( soundbusDescriptor, vstPlugin );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }
    
    /**
     * Adds an AudioUnit node using an <code>UndoableEdit</code>.
     * @param audioUnit The audio unit.
     */
    public void addAudioUnit( AudioUnit audioUnit ) {
        AddSbNodeEdit edit = new AddAudioUnitNodeEdit( soundbusDescriptor, audioUnit );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }
    
    static class AddVstNodeEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;
        VstPlugin vstPlugin;
        AddVstNodeEdit( SoundbusDescriptor desc, VstPlugin vstPlugin ) {
            super( desc, null );
            this.vstPlugin = vstPlugin;
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            VstNode node = sb.addVstPluginNode();
            node.setVstPlugin( vstPlugin );
            return node;
        }
    }

    static class AddAudioUnitNodeEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;
        AudioUnit audioUnit;
        AddAudioUnitNodeEdit( SoundbusDescriptor desc, AudioUnit audioUnit ) {
            super( desc, null );
            this.audioUnit = audioUnit;
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }

            AudioUnitNode node = sb.addAudioUnitNode();
            node.setAudioUnit( audioUnit );
            return node;
        }
    }

    public void addAudioInput() {
        AddSbNodeEdit edit = new AddAudioInputEdit( soundbusDescriptor );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }

    static class AddAudioInputEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;
        AddAudioInputEdit( SoundbusDescriptor desc ) {
            super( desc, null );
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            AudioInputNode node = sb.addAudioInputNode();
            return node;
        }
    }

    public void addNetworkAudioInput() {
        AddSbNodeEdit edit = new AddNetworkAudioInputEdit( soundbusDescriptor );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }
    
    static class AddNetworkAudioInputEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;
        AddNetworkAudioInputEdit( SoundbusDescriptor desc ) {
            super( desc, null );
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            NetworkAudioInputNode node = sb.addNetworkAudioInputNode();
            return node;
        }
    }

    public void addAudioOutput() {
        AddSbNodeEdit edit = new AddAudioOutputEdit( soundbusDescriptor );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }

    static class AddAudioOutputEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;
        AddAudioOutputEdit( SoundbusDescriptor desc ) {
            super( desc, null );
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            AudioOutputNode node = sb.addAudioOutputNode();
            return node;
        }
    }

    public void addNetworkAudioOutput() {
        AddSbNodeEdit edit = new AddNetworkAudioOutputEdit( soundbusDescriptor );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }
    
    static class AddNetworkAudioOutputEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;
        AddNetworkAudioOutputEdit( SoundbusDescriptor desc ) {
            super( desc, null );
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            NetworkAudioOutputNode node = sb.addNetworkAudioOutputNode();
            return node;
        }
    }

    public void addAudioSampler() {
        AddSbNodeEdit edit = new AddAudioSamplerEdit( soundbusDescriptor );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }

    static class AddAudioSamplerEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;
        AddAudioSamplerEdit( SoundbusDescriptor desc ) {
            super( desc, null );
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            AudioSamplerNode node = sb.addAudioSamplerNode();
            return node;
        }
    }

    public void addOSCReceiver() {
        AddOSCReceiverEdit edit = new AddOSCReceiverEdit( soundbusDescriptor );
        edit.perform();
        soundbusDescriptor.getUndoManager().addEdit( edit );
    }

    static class AddOSCReceiverEdit extends AddSbNodeEdit {
        private static final long serialVersionUID = 1L;
        AddOSCReceiverEdit( SoundbusDescriptor desc ) {
            super( desc, null );
        }
        protected SbNode addNode() {
            Soundbus sb = null;
            try {
                sb = soundbusDescriptor.getSoundbus();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            OSCNode node = sb.addOSCReceiverNode();
            return node;
        }
    }
}