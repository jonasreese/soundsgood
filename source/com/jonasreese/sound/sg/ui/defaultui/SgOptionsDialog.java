/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 11.09.2003
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.SgProperties;
import com.jonasreese.sound.sg.audio.AudioDeviceDescriptor;
import com.jonasreese.sound.sg.audio.AudioDeviceList;
import com.jonasreese.sound.sg.audio.AudioToolkit;
import com.jonasreese.sound.sg.midi.MidiDeviceDescriptor;
import com.jonasreese.sound.sg.midi.MidiDeviceList;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.midi.TrackProxy;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.ui.swing.JrDialog;
import com.jonasreese.ui.swing.SubListSelectionPanel;
import com.jonasreese.util.ParametrizedResourceBundle;
import com.jonasreese.util.resource.ResourceLoader;
import com.jonasreese.util.swing.EmptyAction;
import com.jonasreese.util.swing.TreeTabMap;
import com.jonasreese.util.swing.TreeTabPane;

/**
 * <b>
 * </b>
 * @author jreese
 */
public class SgOptionsDialog extends JrDialog
    implements PropertyChangeListener, ItemListener, ListSelectionListener, DocumentListener
{
    
    private static final long serialVersionUID = 1;
    
    private Icon bar;

    private JCheckBox showTipsOnStartupCheckBox;
    private JCheckBox restoreSessionsOnStartupCheckBox;
//    private JCheckBox saveAllSessionsCheckBox;
    private JCheckBox saveSessionOnCloseCheckBox;
    private JCheckBox openSessionViewsOnSessionOpenCheckBox;
    
    private JRadioButton internalFrameViewModeRadioButton;
    private JRadioButton tabbedViewModeRadioButton;
    private JRadioButton dockingViewRadioButton;
    private JRadioButton extendedDockingViewRadioButton;

    private JSpinner midiUpdateTimeSpinner;
    private JSpinner midiResolutionSpinner;
    private JSpinner tempoSpinner;
    private JSpinner undoSpinner;
    private JCheckBox loopbackCheckBox;

    private JComboBox lnfComboBox;
    private ParametrizedResourceBundle rb;
    private JList sequencerList;
    private JComboBox sampleRateComboBox;
    private JComboBox sampleSizeComboBox;
    private JRadioButton monoRadioButton;
    private JRadioButton stereoRadioButton;
    private JCheckBox endianCheckBox;
    private JCheckBox signedCheckBox;
    private JSlider realtimeSlider;
    private SubListSelectionPanel audioInputDeviceList;
    private SubListSelectionPanel audioOutputDeviceList;
    private SubListSelectionPanel midiInputDeviceList;
    private SubListSelectionPanel midiOutputDeviceList;
    private SubListSelectionPanel clickDeviceList;
    private Plugin[] plugins;
    private TreeTabPane treeTabPane;
    private JButton okButton;
    private JButton applyButton;
    private ArrayList<Component> listeningComponents;
    private TreeTabMap treeTabMap;
    private JCheckBox stressOnOneCheckBox;
    private JComboBox clicksPerTactComboBox;
    private JComboBox oneClickNoteComboBox;
    private JComboBox otherClickNoteComboBox;
    private JComboBox oneClickChannelComboBox;
    private JComboBox otherClickChannelComboBox;

    private JSpinner oneClickVolumeSpinner;

    private JSpinner oneClickDurationSpinner;

    private JSpinner otherClickVolumeSpinner;

    private JSpinner otherClickDurationSpinner;
    
    
    public SgOptionsDialog( Frame parent, Plugin[] plugins )
    {
        super( parent,
            SgEngine.getInstance().getResourceBundle().getString(
                "menu.options.general" ),
            true );
        this.plugins = plugins;
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        listeningComponents = new ArrayList<Component>();
        addWindowListener( new WindowAdapter()
        {
            public void windowClosing( WindowEvent e )
            {
                cancel();
            }
            public void windowClosed( WindowEvent e )
            {
                removeAllListeners();
            }
            public void windowOpened( WindowEvent e )
            {
                addAllListeners();
            }
        } );
        rb = SgEngine.getInstance().getResourceBundle();
        
        bar = new ResourceLoader( getClass(), "resource/bar.jpg" ).getAsIcon();
        
        // create hierarchical tree
        treeTabMap = new TreeTabMap();
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        String programTitle = rb.getString( "options.program" );
        DefaultMutableTreeNode programNode = new DefaultMutableTreeNode( programTitle );
        treeTabMap.addMap( programNode, createProgramPanel( programTitle ) );
        String sessionTitle = rb.getString( "options.program.session" );
        DefaultMutableTreeNode sessionNode = new DefaultMutableTreeNode( sessionTitle );
        treeTabMap.addMap( sessionNode, createSessionPanel( sessionTitle ) );
        programNode.add( sessionNode );
        String viewTitle = rb.getString( "options.program.view" );
        DefaultMutableTreeNode viewNode = new DefaultMutableTreeNode( viewTitle );
        treeTabMap.addMap( viewNode, createViewPanel( viewTitle ) );
        programNode.add( viewNode );
        rootNode.add( programNode );
        String midiTitle = rb.getString( "options.midi" );
        DefaultMutableTreeNode midiNode = new DefaultMutableTreeNode( midiTitle );
        treeTabMap.addMap( midiNode, createMidiPanel( midiTitle ) );
        String sequencerTitle = rb.getString( "options.midi.sequencer" );
        DefaultMutableTreeNode sequencerNode = new DefaultMutableTreeNode( sequencerTitle );
        treeTabMap.addMap( sequencerNode, createSequencerPanel( sequencerTitle ) );
        String midiInputTitle = rb.getString( "options.midi.inputDevices" );
        DefaultMutableTreeNode midiInputNode = new DefaultMutableTreeNode( midiInputTitle );
        treeTabMap.addMap( midiInputNode, createMidiInputPanel( midiInputTitle ) );
        String midiOutputTitle = rb.getString( "options.midi.outputDevices" );
        DefaultMutableTreeNode midiOutputNode = new DefaultMutableTreeNode( midiOutputTitle );
        treeTabMap.addMap( midiOutputNode, createMidiOutputPanel( midiOutputTitle ) );
        String midiClickTitle = rb.getString( "options.midi.click" );
        DefaultMutableTreeNode midiClickNode = new DefaultMutableTreeNode( midiClickTitle );
        treeTabMap.addMap( midiClickNode, createMidiClickPanel( midiClickTitle ) );
        midiNode.add( sequencerNode );
        midiNode.add( midiInputNode );
        midiNode.add( midiOutputNode );
        midiNode.add( midiClickNode );
        rootNode.add( midiNode );
        String audioTitle = rb.getString( "options.audio" );
        DefaultMutableTreeNode audioNode = new DefaultMutableTreeNode( audioTitle );
        treeTabMap.addMap( audioNode, createAudioPanel( audioTitle ) );
        String audioInputTitle = rb.getString( "options.audio.inputDevices" );
        DefaultMutableTreeNode audioInputNode = new DefaultMutableTreeNode( audioInputTitle );
        treeTabMap.addMap( audioInputNode, createAudioInputPanel( audioInputTitle ) );
        String audioOutputTitle = rb.getString( "options.audio.outputDevices" );
        DefaultMutableTreeNode audioOutputNode = new DefaultMutableTreeNode( audioOutputTitle );
        treeTabMap.addMap( audioOutputNode, createAudioOutputPanel( audioOutputTitle ) );
        audioNode.add( audioInputNode );
        audioNode.add( audioOutputNode );
        rootNode.add( audioNode );
        // plugins
        String pluginsTitle = rb.getString( "options.views" );
        DefaultMutableTreeNode pluginsNode = new DefaultMutableTreeNode( pluginsTitle );
        treeTabMap.addMap( pluginsNode, createPluginsPanel( pluginsTitle ) );
        for (int i = 0; i < plugins.length; i++)
        {
            if (plugins[i] != null)
            {
                PluginConfigurator pc = plugins[i].getPluginConfigurator();
                if (pc != null)
                {
                    DefaultMutableTreeNode pluginNode = new DefaultMutableTreeNode( pc.getTitle() );
                    treeTabMap.addMap( pluginNode, createPluginPanel( pc ) );
                    pluginsNode.add( pluginNode );
                }
            }
        }
        rootNode.add( pluginsNode );

        TreeModel treeModel = new DefaultTreeModel( rootNode, true );
        
        treeTabPane = new TreeTabPane( treeModel, treeTabMap, 2 );
        treeTabPane.addContainerListener( new ContainerListener()
        {
            public void componentAdded( ContainerEvent e )
            {
                if (e.getChild() instanceof ViewPanel)
                {
                    ((ViewPanel) e.getChild()).vc.open(); 
                }
            }
            public void componentRemoved( ContainerEvent e ) {}
        } );
        getContentPane().add( treeTabPane );
        JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        okButton = new JButton( rb.getString( "ok" ) );
        okButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                ok();
                dispose();
            }
        } );
        int prefButtonWidth = okButton.getPreferredSize().width;
        applyButton = new JButton( rb.getString( "apply" ) );
        applyButton.setEnabled( false );
        applyButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                ok();
                applyButton.setEnabled( false );
            }
        } );
        prefButtonWidth = Math.max( prefButtonWidth, applyButton.getPreferredSize().width );
        JButton cancelButton = new JButton( rb.getString( "cancel" ) );
        cancelButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                cancel();
                dispose();
            }
        } );
        prefButtonWidth = Math.max( prefButtonWidth, cancelButton.getPreferredSize().width );
        getRootPane().setDefaultButton( okButton );
        okButton.setPreferredSize(
            new Dimension( prefButtonWidth, okButton.getPreferredSize().height ) );
        applyButton.setPreferredSize(
            new Dimension( prefButtonWidth, applyButton.getPreferredSize().height ) );
        cancelButton.setPreferredSize(
            new Dimension( prefButtonWidth, cancelButton.getPreferredSize().height ) );
        buttonPanel.add( okButton );
        buttonPanel.add( applyButton );
        buttonPanel.add( cancelButton );
        getContentPane().add( buttonPanel, BorderLayout.SOUTH );
        
        pack();
        setLocation(
            parent.getX() + parent.getWidth() / 2 - getWidth() / 2,
            parent.getY() + parent.getHeight() / 2 - getHeight() / 2 );

        getRootPane().setDefaultButton( okButton );
        Action cancelKeyAction = new AbstractAction() {
            private static final long serialVersionUID = 1;
            public void actionPerformed(ActionEvent e) {
                ((AbstractButton)e.getSource()).doClick();
            }
        }; 
        KeyStroke cancelKeyStroke = KeyStroke.getKeyStroke( (char) KeyEvent.VK_ESCAPE );
        InputMap inputMap = cancelButton.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
        ActionMap actionMap = cancelButton.getActionMap();
        if (inputMap != null && actionMap != null) {
            inputMap.put(cancelKeyStroke, "cancel");
            actionMap.put("cancel", cancelKeyAction);
        }
    }
    
    public void pack() {
        super.pack();
        setSize( 810, getHeight() );
    }
    
//    /**
//     * Creates the according panel.
//     * @param lnfTitle The title
//     * @return The panel.
//     */
//    private JPanel createPluginPanel( String title )
//    {
//        JPanel p = new JPanel( new BorderLayout() );
//        p.add( getTitleLabel( title ), BorderLayout.NORTH );
//        
//        return p;
//    }

    /**
     * Creates the according panel.
     */
    private JPanel createSessionPanel( String title )
    {
        JPanel p = new JPanel( new BorderLayout() );
        p.add( getTitleLabel( title ), BorderLayout.NORTH );

        JPanel sessionPanel = new JPanel( new BorderLayout() );

        JPanel saveAndRestorePanel = new JPanel( new GridLayout( 2, 1 ) );
        saveAndRestorePanel.setBorder(
            new TitledBorder(
                rb.getString( "options.program.session.saveAndRestore" ) ) );
        saveSessionOnCloseCheckBox = new JCheckBox(
            rb.getString( "options.program.session.saveOnClose" ),
            SgEngine.getInstance().getProperties().getAutoSaveSessionOnClose() );
        saveAndRestorePanel.add( saveSessionOnCloseCheckBox );
        openSessionViewsOnSessionOpenCheckBox = new JCheckBox(
            rb.getString( "options.program.session.openViewsOnOpen" ),
            SgEngine.getInstance().getProperties().getRestoreViewsFromSession() );
        saveAndRestorePanel.add( openSessionViewsOnSessionOpenCheckBox );

        sessionPanel.add( saveAndRestorePanel, BorderLayout.NORTH );
        
        JPanel centerPanel = new JPanel( new BorderLayout() );
        
        JPanel undoPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        undoPanel.setBorder( new TitledBorder( rb.getString( "options.program.session.undo" ) ) );
        JLabel undoLabel = new JLabel( rb.getString( "options.program.session.undoSteps" ) );
        
        int value = SgEngine.getInstance().getProperties().getUndoSteps();
        undoSpinner = new JSpinner( new SpinnerNumberModel( value, 1, 100, 1 ) );
        undoPanel.add( undoLabel );
        undoPanel.add( undoSpinner );
        
        centerPanel.add( undoPanel, BorderLayout.NORTH );
        
        sessionPanel.add( centerPanel );

        p.add( sessionPanel );

        return p;
    }

    /**
     * Creates the according panel.
     */
    private JPanel createProgramPanel( String title )
    {
        JPanel p = new JPanel( new BorderLayout() );
        p.add( getTitleLabel( title ), BorderLayout.NORTH );

        JPanel programPanel = new JPanel( new BorderLayout() );

        JPanel startupPanel = new JPanel( new GridLayout( 2, 1 ) );
        startupPanel.setBorder( new TitledBorder( rb.getString( "options.program.startup" ) ) );
        showTipsOnStartupCheckBox = new JCheckBox(
            rb.getString( "options.program.startup.showTips" ),
            SgEngine.getInstance().getProperties().getShowTipsMode() != SgProperties.SHOW_TIPS_NEVER );
        startupPanel.add( showTipsOnStartupCheckBox );
        restoreSessionsOnStartupCheckBox = new JCheckBox(
            rb.getString( "options.program.startup.restoreSessions" ),
            SgEngine.getInstance().getProperties().getOpenLastSessionsOnStartup() );
        startupPanel.add( restoreSessionsOnStartupCheckBox );

//        JPanel endPanel = new JPanel( new GridLayout( 1, 1 ) );
//        endPanel.setBorder( new TitledBorder( rb.getString( "options.program.end" ) ) );
//        saveAllSessionsCheckBox = new JCheckBox( rb.getString( "options.program.end.saveAllSessions" ) );
//        endPanel.add( saveAllSessionsCheckBox );

        JPanel programPanel2 = new JPanel( new BorderLayout() );
//        programPanel2.add( endPanel, BorderLayout.NORTH );

        programPanel.add( startupPanel, BorderLayout.NORTH );
        programPanel.add( programPanel2 );
        p.add( programPanel );
        return p;
    }

    /**
     * Creates the according panel.
     */
    private JPanel createViewPanel( String title )
    {
        JPanel p = new JPanel( new BorderLayout() );
        p.add( getTitleLabel( title ), BorderLayout.NORTH );

        JPanel viewPanel = new JPanel( new BorderLayout() );

        JPanel lnfPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        UIManager.LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
        LNFDesc[] lnfDescs = new LNFDesc[lnfs.length];
        int selIndex = 0;
        for (int i = 0; i < lnfDescs.length; i++)
        {
            lnfDescs[i] = new LNFDesc( lnfs[i] );
            if (lnfs[i].getClassName().equals( UIManager.getLookAndFeel().getClass().getName() ))
            {
                selIndex = i;
            }
        }
        lnfComboBox = new JComboBox( lnfDescs );
        JLabel label = new JLabel( rb.getString( "options.program.view.lnf.explanation" ) );
        label.setLabelFor( lnfComboBox );
        lnfComboBox.setSelectedIndex( selIndex );
        lnfPanel.add( label );
        lnfPanel.add( lnfComboBox );
        lnfPanel.setBorder( new TitledBorder( rb.getString( "options.program.view.lnf" ) ) );

        viewPanel.add( lnfPanel, BorderLayout.NORTH );
        
        JPanel viewModePanel = new JPanel( new BorderLayout() );
        JPanel vmPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        vmPanel.setBorder( new TitledBorder( rb.getString( "options.program.view.mode" ) ) );
        internalFrameViewModeRadioButton = new JRadioButton( rb.getString( "options.program.view.mode.internalFrame" ) );
        int vm = SgEngine.getInstance().getProperties().getViewMode();
        internalFrameViewModeRadioButton.setSelected( (vm == SgProperties.VIEW_MODE_INTERNAL_FRAMES) );
        ButtonGroup bgr = new ButtonGroup();
        bgr.add( internalFrameViewModeRadioButton );
        vmPanel.add( internalFrameViewModeRadioButton );
        tabbedViewModeRadioButton = new JRadioButton( rb.getString( "options.program.view.mode.tabbed" ) );
        tabbedViewModeRadioButton.setSelected( (vm == SgProperties.VIEW_MODE_TABBED) );
        bgr.add( tabbedViewModeRadioButton );
        vmPanel.add( tabbedViewModeRadioButton );
        dockingViewRadioButton = new JRadioButton( rb.getString( "options.program.view.mode.docking" ) );
        dockingViewRadioButton.setSelected( (vm == SgProperties.VIEW_MODE_DOCKING) );
        bgr.add( dockingViewRadioButton );
        vmPanel.add( dockingViewRadioButton );
        extendedDockingViewRadioButton = new JRadioButton( rb.getString( "options.program.view.mode.extendedDocking" ) );
        extendedDockingViewRadioButton.setSelected( (vm == SgProperties.VIEW_MODE_EXTENDED_DOCKING) );
        bgr.add( extendedDockingViewRadioButton );
        vmPanel.add( extendedDockingViewRadioButton );
        viewModePanel.add( vmPanel, BorderLayout.NORTH );
        viewPanel.add( viewModePanel );

        p.add( viewPanel );

        return p;
    }
    
    /**
     * Creates the according panel.
     * @param lnfTitle The title
     * @return The panel.
     */
    private JPanel createMidiPanel( String title )
    {
        JPanel p = new JPanel( new BorderLayout() );
        p.add( getTitleLabel( title ), BorderLayout.NORTH );

        JPanel midiPanel = new JPanel( new BorderLayout() );

        JPanel midiFormatPanel = new JPanel( new GridLayout( 2, 1 ) );
        JPanel explanation = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        explanation.add( new JLabel( rb.getString( "options.midi.format.resolution.explanation" ) ) );
        JPanel resPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        int res = SgEngine.getInstance().getProperties().getMidiResolution();
        midiResolutionSpinner = new JSpinner( new SpinnerNumberModel( res, 1, 100, 1 ) );
        JLabel resolutionLabel = new JLabel( rb.getString( "options.midi.format.resolution" ) );
        resolutionLabel.setLabelFor( midiResolutionSpinner );
        resPanel.add( resolutionLabel );
        resPanel.add( midiResolutionSpinner );
        midiFormatPanel.add( explanation );
        midiFormatPanel.add( resPanel );
        
        JPanel defaultTempoPanel = new JPanel( new GridLayout( 2, 1 ) );
        explanation = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        explanation.add( new JLabel( rb.getString( "options.midi.format.tempo.explanation" ) ) );
        JPanel tempoPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        int tempo = SgEngine.getInstance().getProperties().getDefaultMidiTempo();
        tempoSpinner = new JSpinner( new SpinnerNumberModel( tempo, 40, 280, 1 ) );
        JLabel tempoLabel = new JLabel( rb.getString( "options.midi.format.tempo" ) );
        tempoLabel.setLabelFor( tempoSpinner );
        tempoPanel.add( tempoLabel );
        tempoPanel.add( tempoSpinner );
        defaultTempoPanel.add( explanation );
        defaultTempoPanel.add( tempoPanel );
        
        JPanel midiFormatContainerPanel = new JPanel( new BorderLayout() );
        midiFormatContainerPanel.setBorder( new TitledBorder( rb.getString( "options.midi.format" ) ) );
        midiFormatContainerPanel.add( midiFormatPanel, BorderLayout.WEST );
        midiFormatContainerPanel.add( defaultTempoPanel, BorderLayout.EAST );

        JPanel midiSystemPanel = new JPanel( new GridLayout( 2, 1 ) );
        midiSystemPanel.setBorder( new TitledBorder( rb.getString( "options.midi.system" ) ) );
        explanation = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        explanation.add( new JLabel( rb.getString( "options.midi.system.updateTime.explanation" ) ) );
        JPanel updatePanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        int value = SgEngine.getInstance().getProperties().getMidiUpdateTime();
        midiUpdateTimeSpinner = new JSpinner( new SpinnerNumberModel( value, 0, 1000, 1 ) );
        JLabel updateLabel = new JLabel( rb.getString( "options.midi.system.updateTime" ) );
        updateLabel.setLabelFor( midiUpdateTimeSpinner );
        updatePanel.add( updateLabel );
        updatePanel.add( midiUpdateTimeSpinner );
        midiSystemPanel.add( explanation );
        midiSystemPanel.add( updatePanel );
        
        JPanel midiRecordPanel = new JPanel( new BorderLayout() );
        midiRecordPanel.setBorder( new TitledBorder( rb.getString( "options.midi.record" ) ) );
        JPanel loopbackPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        loopbackCheckBox = new JCheckBox( rb.getString( "options.midi.record.enableLoopback" ) );
        loopbackCheckBox.setSelected( SgEngine.getInstance().getProperties().getDefaultRecordLoopbackEnabled() );
        loopbackPanel.add( loopbackCheckBox );
        midiRecordPanel.add( loopbackPanel, BorderLayout.WEST );
        
        midiPanel.add( midiFormatContainerPanel, BorderLayout.NORTH );

        JPanel midiPanel2 = new JPanel( new BorderLayout() );
        midiPanel2.add( midiSystemPanel, BorderLayout.NORTH );
        JPanel midiPanel3 = new JPanel( new BorderLayout() );
        midiPanel3.add( midiRecordPanel, BorderLayout.NORTH );
        midiPanel2.add( midiPanel3 );
        midiPanel.add( midiPanel2 );

        p.add( midiPanel );
        return p;
    }

    /**
     * Creates the according panel.
     * @param lnfTitle The title
     * @return The panel.
     */
    private JPanel createPluginsPanel( String title )
    {
        JPanel p = new JPanel( new BorderLayout() );
        p.add( getTitleLabel( title ), BorderLayout.NORTH );
        
        JPanel contentPanel = new JPanel( new BorderLayout() );
        contentPanel.setBorder( new TitledBorder( rb.getString( "options.views.install" ) ) );
        JOptionPane text = new JOptionPane();
        text.setOptions( new Object[0] );
        text.setMessage( rb.getString( "options.views.install.text" ) );
        text.setIcon( null );
        contentPanel.add( text, BorderLayout.NORTH );
        JPanel buttonPanel = new JPanel();
        buttonPanel.add( new JButton( rb.getString( "options.views.install.online" ) ) );
        buttonPanel.add( new JButton( rb.getString( "options.views.install.new" ) ) );
        JButton infoButton = new JButton( rb.getString( "options.views.install.info" ) );
        infoButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                JDialog d = new SgPluginInformationDialog(
                    SgOptionsDialog.this, rb.getString( "options.views.install.info.title" ) );
                d.pack();
                d.setLocation(
                    SgOptionsDialog.this.getX() + SgOptionsDialog.this.getWidth() / 2 - d.getWidth() / 2,
                    SgOptionsDialog.this.getY() + SgOptionsDialog.this.getHeight() / 2 - d.getHeight() / 2 );
                d.setVisible( true );
            }
        } );
        buttonPanel.add( infoButton );
        contentPanel.add( buttonPanel );
        
        p.add( contentPanel );

        return p;
    }

    /**
     * Creates the according panel.
     * @param lnfTitle The title
     * @return The panel.
     */
    private JPanel createPluginPanel( PluginConfigurator vc )
    {
        return new ViewPanel( vc );
    }

    /**
     * Creates the according panel.
     * @param lnfTitle The title
     * @return The panel.
     */
    private JPanel createSequencerPanel( String title )
    {
        JPanel p = new JPanel( new BorderLayout() );
        p.add( getTitleLabel( title ), BorderLayout.NORTH );

        MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
        Vector<MidiDesc> descs = new Vector<MidiDesc>();
        MidiDevice.Info currInfo = SgEngine.getInstance().getProperties().getMidiSequencerInfo();
        //System.out.println( "currInfo[s] = " + currInfo );
        int index = -1;
        for (int i = 0; i < info.length; i++)
        {
            MidiDevice dev = null;
            try
            {
                dev = MidiSystem.getMidiDevice( info[i] );
            }
            catch (MidiUnavailableException e) {}
            if (dev instanceof Sequencer)
            {
                if (info[i].equals( currInfo )) { index = descs.size(); }
                descs.add( new MidiDesc( new MidiDeviceDescriptor( info[i], null ) ) );
            }
        }
        
        JPanel inputPanel = new JPanel( new BorderLayout() );
        inputPanel.setBorder(
            new TitledBorder( rb.getString( "options.midi.sequencer.text" ) ) );
        sequencerList = new JList( descs );
        if (index >= 0) { sequencerList.setSelectedIndex( index ); }
        inputPanel.add( new JScrollPane( sequencerList ) );
        p.add( inputPanel );

        return p;
    }

    /**
     * Creates the according panel.
     * @param lnfTitle The title
     * @return The panel.
     */
    private JPanel createMidiInputPanel( String title )
    {
        JPanel p = new JPanel( new BorderLayout() );
        p.add( getTitleLabel( title ), BorderLayout.NORTH );

        ArrayList<MidiDesc> descs = new ArrayList<MidiDesc>();
        MidiDeviceList deviceList = MidiToolkit.getMidiInputDeviceList();
        for (int i = 0; i < deviceList.getCount(); i++) {
            descs.add( new MidiDesc( deviceList.getMidiDeviceDescriptor( i ) ) );
        }
        ArrayList<MidiDesc> selected = getMidiDescSubList( descs, true, false );
        
        Action toLeftAction = new EmptyAction( rb.getString( "options.midi.inputDevice.remove" ) );
        Action toRightAction = new EmptyAction( rb.getString( "options.midi.inputDevice.add" ) );
        Action allToLeftAction = new EmptyAction( rb.getString( "options.midi.inputDevice.removeAll" ) );
        Action allToRightAction = new EmptyAction( rb.getString( "options.midi.inputDevice.addAll" ) );
        midiInputDeviceList = new SubListSelectionPanel( descs.toArray(), null, toLeftAction, toRightAction );
        midiInputDeviceList.setAllToLeftAction( allToLeftAction );
        midiInputDeviceList.setAllToRightAction( allToRightAction );
        midiInputDeviceList.setKeepLeftOrder( true );
        midiInputDeviceList.moveFromLeftToRight( selected );

        midiInputDeviceList.getRightList().setCellRenderer( new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1;
            public Component getListCellRendererComponent(
                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
                String s = "[" + (index + 1) + "] " + value;
                return super.getListCellRendererComponent( list, s, index, isSelected, cellHasFocus );
            }
        });
        midiInputDeviceList.getLeftListContainerPanel().setBorder(
                new TitledBorder( rb.getString( "options.midi.inputDevices.text" ) ) );
        midiInputDeviceList.getRightListContainerPanel().setBorder(
                new TitledBorder( rb.getString( "options.midi.selectedInputDevices.text" ) ) );
        midiInputDeviceList.getSplitPane().setBorder( null );
        p.add( midiInputDeviceList );

        return p;
    }

    /**
     * Creates the according panel.
     * @param lnfTitle The title
     * @return The panel.
     */
    private JPanel createMidiOutputPanel( String title )
    {
        JPanel p = new JPanel( new BorderLayout() );
        p.add( getTitleLabel( title ), BorderLayout.NORTH );
        

        ArrayList<MidiDesc> descs = new ArrayList<MidiDesc>();
        MidiDeviceList deviceList = MidiToolkit.getMidiOutputDeviceList();
        for (int i = 0; i < deviceList.getCount(); i++) {
            descs.add( new MidiDesc( deviceList.getMidiDeviceDescriptor( i ) ) );
        }
        ArrayList<MidiDesc> selected = getMidiDescSubList( descs, false, false );

        Action toLeftAction = new EmptyAction( rb.getString( "options.midi.outputDevice.remove" ) );
        Action toRightAction = new EmptyAction( rb.getString( "options.midi.outputDevice.add" ) );
        Action allToLeftAction = new EmptyAction( rb.getString( "options.midi.outputDevice.removeAll" ) );
        Action allToRightAction = new EmptyAction( rb.getString( "options.midi.outputDevice.addAll" ) );
        midiOutputDeviceList = new SubListSelectionPanel( descs.toArray(), null, toLeftAction, toRightAction );
        midiOutputDeviceList.setAllToLeftAction( allToLeftAction );
        midiOutputDeviceList.setAllToRightAction( allToRightAction );
        midiOutputDeviceList.getRightList().setCellRenderer( new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1;
            public Component getListCellRendererComponent(
                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
                String s = "[" + (index + 1) + "] " + value;
                return super.getListCellRendererComponent( list, s, index, isSelected, cellHasFocus );
            }
        });
        midiOutputDeviceList.setKeepLeftOrder( true );
        midiOutputDeviceList.moveFromLeftToRight( selected );

        midiOutputDeviceList.getSplitPane().setBorder( null );
        midiOutputDeviceList.getLeftListContainerPanel().setBorder(
                new TitledBorder( rb.getString( "options.midi.outputDevices.text" ) ) );
        midiOutputDeviceList.getRightListContainerPanel().setBorder(
                new TitledBorder( rb.getString( "options.midi.selectedOutputDevices.text" ) ) );
        p.add( midiOutputDeviceList );
        
        return p;
    }

    /**
     * Creates the according panel.
     * @param lnfTitle The title
     * @return The panel.
     */
    private JPanel createMidiClickPanel( String title )
    {
        JPanel p = new JPanel( new BorderLayout() );
        p.add( getTitleLabel( title ), BorderLayout.NORTH );
        

        ArrayList<MidiDesc> descs = new ArrayList<MidiDesc>();
        MidiDeviceList deviceList = MidiToolkit.getMidiOutputDeviceList();
        for (int i = 0; i < deviceList.getCount(); i++) {
            descs.add( new MidiDesc( deviceList.getMidiDeviceDescriptor( i ) ) );
        }
        ArrayList<MidiDesc> selected = getMidiDescSubList( descs, false, true );

        Action toLeftAction = new EmptyAction( rb.getString( "options.midi.click.remove" ) );
        Action toRightAction = new EmptyAction( rb.getString( "options.midi.click.add" ) );
        Action allToLeftAction = new EmptyAction( rb.getString( "options.midi.click.removeAll" ) );
        Action allToRightAction = new EmptyAction( rb.getString( "options.midi.click.addAll" ) );
        clickDeviceList = new SubListSelectionPanel( descs.toArray(), null, toLeftAction, toRightAction );
        clickDeviceList.setAllToLeftAction( allToLeftAction );
        clickDeviceList.setAllToRightAction( allToRightAction );
        clickDeviceList.setKeepLeftOrder( true );
        clickDeviceList.moveFromLeftToRight( selected );

        clickDeviceList.getSplitPane().setBorder( null );
        clickDeviceList.getLeftListContainerPanel().setBorder(
                new TitledBorder( rb.getString( "options.midi.click.devices.text" ) ) );
        clickDeviceList.getRightListContainerPanel().setBorder(
                new TitledBorder( rb.getString( "options.midi.click.selectedDevices.text" ) ) );
        p.add( clickDeviceList );
        JPanel clickPanel = new JPanel( new BorderLayout() );
        clickPanel.setBorder( new TitledBorder( rb.getString( "options.midi.click.click" ) ) );
        
        final JLabel l0 = new JLabel( rb.getString( "options.midi.click.one" ) );
        final JLabel l01 = new JLabel( rb.getString( "options.midi.click.volume" ) );
        final JLabel l02 = new JLabel( rb.getString( "options.midi.click.duration" ) );
        final JLabel l0001 = new JLabel( rb.getString( "options.midi.click.duration.ms" ) );

        JPanel controlPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        JPanel stressPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        stressOnOneCheckBox = new JCheckBox(
                rb.getString( "options.midi.click.stressOnOne" ),
                SgEngine.getInstance().getProperties().isStressOnClickOne() );
        ActionListener al = new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                boolean b = stressOnOneCheckBox.isSelected();
                l0.setEnabled( b );
                l01.setEnabled( b );
                l02.setEnabled( b );
                l0001.setEnabled( b );
                oneClickNoteComboBox.setEnabled( b );
                oneClickChannelComboBox.setEnabled( b );
                oneClickVolumeSpinner.setEnabled( b );
                oneClickDurationSpinner.setEnabled( b );
            }
        };
        stressOnOneCheckBox.addActionListener( al );
        stressPanel.add( stressOnOneCheckBox );
        controlPanel.add( stressPanel );
        JPanel cptPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        clicksPerTactComboBox = new JComboBox( new String[] { "1", "2", "3", "4", "6", "8" } );
        clicksPerTactComboBox.setSelectedItem(
                Integer.toString( SgEngine.getInstance().getProperties().getClicksPerTact() ) );
        cptPanel.add( clicksPerTactComboBox );
        JLabel l00 = new JLabel( rb.getString( "options.midi.click.clicksPerTact" ) );
        l00.setLabelFor( clicksPerTactComboBox );
        cptPanel.add( l00 );
        controlPanel.add( cptPanel );
        
        JPanel notePanel = new JPanel( new GridLayout( 2, 1 ) );
        JPanel onePanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        oneClickNoteComboBox = new JComboBox( TrackProxy.createDefaultEventMap().getNoteDescriptors() );
        short[] one = SgEngine.getInstance().getProperties().getNoteClickOne();
        oneClickNoteComboBox.setSelectedIndex( 127 - one[0] );
        l0.setLabelFor( oneClickNoteComboBox );
        onePanel.add( l0 );
        oneClickChannelComboBox = new JComboBox( MidiToolkit.getDefaultMidiChannelNames( false ) );
        oneClickChannelComboBox.setSelectedIndex( one[1] );
        oneClickVolumeSpinner = new JSpinner( new SpinnerNumberModel( one[2], 1, 127, 1 ) );
        oneClickDurationSpinner = new JSpinner( new SpinnerNumberModel( one[3], 1, 999, 1 ) );
        l01.setLabelFor( oneClickVolumeSpinner );
        l02.setLabelFor( oneClickDurationSpinner );
        l0001.setLabelFor( oneClickDurationSpinner );
        onePanel.add( oneClickNoteComboBox );
        onePanel.add( oneClickChannelComboBox );
        onePanel.add( l01 );
        onePanel.add( oneClickVolumeSpinner );
        onePanel.add( l02 );
        onePanel.add( oneClickDurationSpinner );
        onePanel.add( l0001 );
        JPanel otherPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        JLabel l1 = new JLabel( rb.getString( "options.midi.click.other" ) );
        otherPanel.add( l1 );
        otherClickNoteComboBox = new JComboBox( TrackProxy.createDefaultEventMap().getNoteDescriptors() );
        short[] other = SgEngine.getInstance().getProperties().getNoteClick();
        otherClickNoteComboBox.setSelectedIndex( 127 - other[0] );
        l1.setLabelFor( otherClickNoteComboBox );
        otherPanel.add( otherClickNoteComboBox );
        otherClickChannelComboBox = new JComboBox( MidiToolkit.getDefaultMidiChannelNames( false ) );
        otherClickChannelComboBox.setSelectedIndex( other[1] );
        otherClickVolumeSpinner = new JSpinner( new SpinnerNumberModel( other[2], 1, 127, 1 ) );
        otherClickDurationSpinner = new JSpinner( new SpinnerNumberModel( other[3], 1, 999, 1 ) );
        JLabel l001 = new JLabel( rb.getString( "options.midi.click.volume" ) );
        JLabel l002 = new JLabel( rb.getString( "options.midi.click.duration" ) );
        l001.setLabelFor( otherClickVolumeSpinner );
        l002.setLabelFor( otherClickDurationSpinner );
        otherPanel.add( otherClickChannelComboBox );
        otherPanel.add( l001 );
        otherPanel.add( otherClickVolumeSpinner );
        otherPanel.add( l002 );
        otherPanel.add( otherClickDurationSpinner );
        JLabel l00001 = new JLabel( rb.getString( "options.midi.click.duration.ms" ) );
        l00001.setLabelFor( otherClickDurationSpinner );
        otherPanel.add( l00001 );
        notePanel.add( onePanel );
        notePanel.add( otherPanel );
        
        clickPanel.add( controlPanel, BorderLayout.NORTH );
        clickPanel.add( notePanel );
        p.add( clickPanel, BorderLayout.SOUTH );
        
        al.actionPerformed( null );
        
        return p;
    }

    private ArrayList<MidiDesc> getMidiDescSubList( ArrayList<MidiDesc> list, boolean input, boolean click ) {
        ArrayList<MidiDesc> result = new ArrayList<MidiDesc>();
        MidiDeviceList deviceList =
            click ? SgEngine.getInstance().getProperties().getMidiClickDeviceList() :
            (input ?
                    SgEngine.getInstance().getProperties().getMidiInputDeviceList() :
                    SgEngine.getInstance().getProperties().getMidiOutputDeviceList());
        for (int i = 0; i < deviceList.getCount(); i++) {
            for (int j = 0; j < list.size(); j++) {
                MidiDesc md = list.get( j );
                if (md.descriptor.equals( deviceList.getMidiDeviceDescriptor( i ) )) {
                    result.add( md );
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Creates the according panel.
     * @param lnfTitle The title
     * @return The panel.
     */
    private JPanel createAudioInputPanel( String title )
    {
        JPanel p = new JPanel( new BorderLayout() );
        p.add( getTitleLabel( title ), BorderLayout.NORTH );

        ArrayList<AudioDesc> descs = new ArrayList<AudioDesc>();
        AudioDeviceList deviceList = AudioToolkit.getAudioInputDeviceList();
        for (int i = 0; i < deviceList.getCount(); i++) {
            descs.add( new AudioDesc( deviceList.getAudioDeviceDescriptor( i ) ) );
        }
        ArrayList<AudioDesc> selected = getAudioDescSubList( descs, true );
        
        Action toLeftAction = new EmptyAction( rb.getString( "options.audio.inputDevice.remove" ) );
        Action toRightAction = new EmptyAction( rb.getString( "options.audio.inputDevice.add" ) );
        Action allToLeftAction = new EmptyAction( rb.getString( "options.audio.inputDevice.removeAll" ) );
        Action allToRightAction = new EmptyAction( rb.getString( "options.audio.inputDevice.addAll" ) );
        audioInputDeviceList = new SubListSelectionPanel( descs.toArray(), null, toLeftAction, toRightAction );
        audioInputDeviceList.setAllToLeftAction( allToLeftAction );
        audioInputDeviceList.setAllToRightAction( allToRightAction );
        audioInputDeviceList.setKeepLeftOrder( true );
        audioInputDeviceList.moveFromLeftToRight( selected );

        audioInputDeviceList.getRightList().setCellRenderer( new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1;
            public Component getListCellRendererComponent(
                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
                String s = "[" + (index + 1) + "] " + value;
                return super.getListCellRendererComponent( list, s, index, isSelected, cellHasFocus );
            }
        });
        audioInputDeviceList.getLeftListContainerPanel().setBorder(
                new TitledBorder( rb.getString( "options.audio.inputDevices.text" ) ) );
        audioInputDeviceList.getRightListContainerPanel().setBorder(
                new TitledBorder( rb.getString( "options.audio.selectedInputDevices.text" ) ) );
        audioInputDeviceList.getSplitPane().setBorder( null );
        p.add( audioInputDeviceList );

        return p;
    }

    /**
     * Creates the according panel.
     * @param lnfTitle The title
     * @return The panel.
     */
    private JPanel createAudioOutputPanel( String title ) {
        JPanel p = new JPanel( new BorderLayout() );
        p.add( getTitleLabel( title ), BorderLayout.NORTH );
        

        ArrayList<AudioDesc> descs = new ArrayList<AudioDesc>();
        AudioDeviceList deviceList = AudioToolkit.getAudioOutputDeviceList();
        for (int i = 0; i < deviceList.getCount(); i++) {
            descs.add( new AudioDesc( deviceList.getAudioDeviceDescriptor( i ) ) );
        }
        ArrayList<AudioDesc> selected = getAudioDescSubList( descs, false );

        Action toLeftAction = new EmptyAction( rb.getString( "options.audio.outputDevice.remove" ) );
        Action toRightAction = new EmptyAction( rb.getString( "options.audio.outputDevice.add" ) );
        Action allToLeftAction = new EmptyAction( rb.getString( "options.audio.outputDevice.removeAll" ) );
        Action allToRightAction = new EmptyAction( rb.getString( "options.audio.outputDevice.addAll" ) );
        audioOutputDeviceList = new SubListSelectionPanel( descs.toArray(), null, toLeftAction, toRightAction );
        audioOutputDeviceList.setAllToLeftAction( allToLeftAction );
        audioOutputDeviceList.setAllToRightAction( allToRightAction );
        audioOutputDeviceList.getRightList().setCellRenderer( new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1;
            public Component getListCellRendererComponent(
                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
                String s = "[" + (index + 1) + "] " + value;
                return super.getListCellRendererComponent( list, s, index, isSelected, cellHasFocus );
            }
        });
        audioOutputDeviceList.setKeepLeftOrder( true );
        audioOutputDeviceList.moveFromLeftToRight( selected );

        audioOutputDeviceList.getSplitPane().setBorder( null );
        audioOutputDeviceList.getLeftListContainerPanel().setBorder(
                new TitledBorder( rb.getString( "options.audio.outputDevices.text" ) ) );
        audioOutputDeviceList.getRightListContainerPanel().setBorder(
                new TitledBorder( rb.getString( "options.audio.selectedOutputDevices.text" ) ) );
        p.add( audioOutputDeviceList );
        
        return p;
    }
    
    private void selectSampleRate( float samplerate ) {
        ComboBoxModel model = sampleRateComboBox.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            SampleRate sr = (SampleRate) model.getElementAt( i );
            if (sr.sr == samplerate) {
                sampleRateComboBox.setSelectedIndex( i );
                break;
            }
        }
    }
    
    private void selectSampleSize( int samplesize ) {
        ComboBoxModel model = sampleSizeComboBox.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            SampleSize ss = (SampleSize) model.getElementAt( i );
            if (ss.ss == samplesize) {
                sampleSizeComboBox.setSelectedIndex( i );
                break;
            }
        }
    }

    /**
     * Creates the according panel.
     * @param lnfTitle The title
     * @return The panel.
     */
    private JPanel createAudioPanel( String title ) {
        JPanel p = new JPanel( new BorderLayout() );
        p.add( getTitleLabel( title ), BorderLayout.NORTH );
        
        JPanel sampleRatePanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        JLabel l0 = new JLabel( rb.getString( "options.audio.format.samplerate" ) );
        JPanel audioFormatPanel = new JPanel( new BorderLayout() );
        sampleRatePanel.add( l0 );
        sampleRateComboBox = new JComboBox(
                new Object[] {
                        new SampleRate( 96000f, rb.getString( "options.audio.format.sr96" ) ),
                        new SampleRate( 48000f, rb.getString( "options.audio.format.sr48" ) ),
                        new SampleRate( 44100f, rb.getString( "options.audio.format.sr44_1" ) ),
                        new SampleRate( 22050f, rb.getString( "options.audio.format.sr22_05" ) ),
                        new SampleRate( 16000f, rb.getString( "options.audio.format.sr16" ) ),
                        new SampleRate( 8000f, rb.getString( "options.audio.format.sr8" ) ),
                } );
        selectSampleRate( SgEngine.getInstance().getProperties().getDefaultAudioFormatSampleRate() );
        l0.setLabelFor( sampleRateComboBox );
        sampleRatePanel.add( sampleRateComboBox );
        audioFormatPanel.add( sampleRatePanel, BorderLayout.WEST );
        JPanel sampleSizePanel = new JPanel();
        JLabel l1 = new JLabel( rb.getString( "options.audio.format.samplesize" ) );
        sampleSizeComboBox = new JComboBox(
                new Object[] {
                        new SampleSize( 8, rb.getString( "options.audio.format.ss8" ) ),
                        new SampleSize( 16, rb.getString( "options.audio.format.ss16" ) ),
                        new SampleSize( 32, rb.getString( "options.audio.format.ss32" ) ),
                } );
        selectSampleSize( SgEngine.getInstance().getProperties().getDefaultAudioFormatSampleSize() );
        l1.setLabelFor( sampleSizeComboBox );
        sampleSizePanel.add( l1 );
        sampleSizePanel.add( sampleSizeComboBox );
        audioFormatPanel.add( sampleSizePanel );
        JPanel monoStereoPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        monoRadioButton = new JRadioButton(
                rb.getString( "options.audio.format.mono" ),
                SgEngine.getInstance().getProperties().isDefaultAudioFormatMono() );
        stereoRadioButton = new JRadioButton(
                rb.getString( "options.audio.format.stereo" ),
                !SgEngine.getInstance().getProperties().isDefaultAudioFormatMono());
        ButtonGroup bgr = new ButtonGroup();
        bgr.add( monoRadioButton );
        bgr.add( stereoRadioButton );
        monoStereoPanel.add( monoRadioButton );
        monoStereoPanel.add( stereoRadioButton );
        audioFormatPanel.add( monoStereoPanel, BorderLayout.EAST );
        JPanel defaultPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        JButton defaultsButton = new JButton( rb.getString( "options.audio.format.defaults" ) );
        defaultsButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                selectSampleRate( SgProperties.AUDIO_FORMAT_DEFAULT_SAMPLE_RATE );
                selectSampleSize( SgProperties.AUDIO_FORMAT_DEFAULT_SAMPLE_SIZE );
                monoRadioButton.setSelected( SgProperties.AUDIO_FORMAT_DEFAULT_MONO );
                stereoRadioButton.setSelected( !SgProperties.AUDIO_FORMAT_DEFAULT_MONO );
                endianCheckBox.setSelected( SgProperties.AUDIO_FORMAT_DEFAULT_BIG_ENDIAN );
                signedCheckBox.setSelected( SgProperties.AUDIO_FORMAT_DEFAULT_SIGNED );
            }
        } );
        defaultPanel.add( defaultsButton );
        JPanel endianPanel = new JPanel();
        endianCheckBox = new JCheckBox( rb.getString( "options.audio.format.bigEndian" ),
                SgEngine.getInstance().getProperties().isDefaultAudioFormatBigEndian() );
        endianPanel.add( endianCheckBox );
        signedCheckBox = new JCheckBox( rb.getString( "options.audio.format.signed" ),
                SgEngine.getInstance().getProperties().isDefaultAudioFormatSigned() );
        JPanel signedPanel = new JPanel();
        signedPanel.add( signedCheckBox );
        JPanel southPanel = new JPanel( new BorderLayout() );
        southPanel.add( endianPanel, BorderLayout.WEST );
        southPanel.add( signedPanel );
        southPanel.add( defaultPanel, BorderLayout.EAST );
        audioFormatPanel.add( southPanel, BorderLayout.SOUTH );
        audioFormatPanel.setBorder( new TitledBorder( rb.getString( "options.audio.format" ) ) );
        JPanel audioPanel = new JPanel( new BorderLayout() );
        audioPanel.add( audioFormatPanel, BorderLayout.NORTH );
        
        JPanel realtimePanel = new JPanel( new BorderLayout() );
        realtimePanel.setBorder( new TitledBorder( rb.getString( "options.audio.realtime" ) ) );

        JPanel realtimeWrapperPanel = new JPanel( new BorderLayout() );
        realtimeWrapperPanel.add( realtimePanel, BorderLayout.NORTH );
        JPanel realtimeSliderPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        realtimeSlider = new JSlider(
                JSlider.HORIZONTAL,
                0,
                SgProperties.MAX_AUDIO_BUFFER_LENGTH,
                SgEngine.getInstance().getProperties().getAudioBufferLength() );
        realtimeSlider.setMinorTickSpacing( 1 );
        realtimeSlider.setMajorTickSpacing( 10 );
        realtimeSlider.setSnapToTicks( true );
        realtimeSlider.setPaintTicks( true );
        realtimeSlider.setPaintLabels( true );
        Dimension d = new Dimension( realtimeSlider.getPreferredSize() );
        d.width *= 2;
        realtimeSlider.setPreferredSize( d );
        final JLabel realtimeSliderLabel = new JLabel();
        setRealtimeSliderText( realtimeSliderLabel );
        realtimeSlider.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                setRealtimeSliderText( realtimeSliderLabel );
            }
        } );
        realtimeSliderPanel.add( realtimeSliderLabel );
        realtimeSliderPanel.add( realtimeSlider );
        JLabel bufferSizeTextLabel = new JLabel( rb.getString( "options.audio.realtime.buffer.length.text" ) );
        JPanel bufferSizeTextLabelPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        bufferSizeTextLabelPanel.add( bufferSizeTextLabel );
        realtimePanel.add( bufferSizeTextLabelPanel, BorderLayout.NORTH );
        realtimePanel.add( realtimeSliderPanel );
        audioPanel.add( realtimeWrapperPanel );
        
        p.add( audioPanel );

        return p;
    }
    
    private void setRealtimeSliderText( JLabel label ) {
        label.setText(
                rb.getString( "options.audio.realtime.buffer.length", realtimeSlider.getValue() ) );
    }

    private ArrayList<AudioDesc> getAudioDescSubList( ArrayList<AudioDesc> list, boolean input ) {
        ArrayList<AudioDesc> result = new ArrayList<AudioDesc>();
        AudioDeviceList deviceList =
            (input ?
                    SgEngine.getInstance().getProperties().getAudioInputDeviceList() :
                    SgEngine.getInstance().getProperties().getAudioOutputDeviceList());
        for (int i = 0; i < deviceList.getCount(); i++) {
            for (int j = 0; j < list.size(); j++) {
                AudioDesc ad = list.get( j );
                if (ad.descriptor.equals( deviceList.getAudioDeviceDescriptor( i ) )) {
                    result.add( ad );
                    break;
                }
            }
        }
        return result;
    }

    private JPanel getTitleLabel( String title )
    {
        JPanel p = new JPanel( new BorderLayout() );
        p.add( new JLabel( "    " + title ), BorderLayout.NORTH );
        JLabel l = new JLabel( bar, JLabel.LEFT );
        p.add( l );
        JLabel bl = new JLabel();
        bl.setPreferredSize( new Dimension( 10, 20 ) );
        p.add( bl, BorderLayout.SOUTH );
        
        return p;
    }

    /**
     * Applies all settings.
     */
    private void ok()
    {
        // save 'usual' properties
        SgProperties p = SgEngine.getInstance().getProperties();

        if (p.getShowTipsMode() == SgProperties.SHOW_TIPS_NEVER && showTipsOnStartupCheckBox.isSelected()) {
            p.setShowTipsMode( SgProperties.SHOW_TIPS_ON_EVERY_START );
        } else if (p.getShowTipsMode() != SgProperties.SHOW_TIPS_NEVER &&
                !showTipsOnStartupCheckBox.isSelected()) {
            p.setShowTipsMode( SgProperties.SHOW_TIPS_NEVER );
        }
        p.setOpenLastSessionsOnStartup( restoreSessionsOnStartupCheckBox.isSelected() );
        p.setAutoSaveSessionOnClose( saveSessionOnCloseCheckBox.isSelected() );
        p.setRestoreViewsFromSession( openSessionViewsOnSessionOpenCheckBox.isSelected() );

        p.setViewMode(
                internalFrameViewModeRadioButton.isSelected() ?
                        SgProperties.VIEW_MODE_INTERNAL_FRAMES :
                            (tabbedViewModeRadioButton.isSelected() ?
                                    SgProperties.VIEW_MODE_TABBED :
                                        (dockingViewRadioButton.isSelected() ? SgProperties.VIEW_MODE_DOCKING :
                                            SgProperties.VIEW_MODE_EXTENDED_DOCKING) ) );
        
        p.setUndoSteps( ((Integer) undoSpinner.getValue()).intValue() );

        p.setMidiUpdateTime( ((Integer) midiUpdateTimeSpinner.getValue()).intValue() );
        p.setMidiResolution( ((Integer) midiResolutionSpinner.getValue()).intValue() );
        p.setDefaultMidiTempo( ((Integer) tempoSpinner.getValue()).intValue() );
        p.setDefaultRecordLoopbackEnabled( loopbackCheckBox.isSelected() );

        p.setLNFClassName( ((LNFDesc) lnfComboBox.getSelectedItem()).getClassName() );
        Object o = sequencerList.getSelectedValue();
        if (o != null)
        {
            p.setMidiSequencerInfo( ((MidiDesc) o).descriptor.getDeviceInfo() );
        }
        o = (midiOutputDeviceList.getRightList().getModel().getSize() == 0 ?
                null : midiOutputDeviceList.getRightList().getModel());
        if (o != null)
        {
            ListModel lm = (ListModel) o;
            MidiDeviceDescriptor[] descriptors = new MidiDeviceDescriptor[lm.getSize()];
            for (int i = 0; i < lm.getSize(); i++) {
                descriptors[i] = ((MidiDesc) lm.getElementAt( i )).descriptor;
            }
            p.setMidiOutputDeviceList( new MidiDeviceList( descriptors ) );
        }
        else
        {
            p.setMidiOutputDeviceList( null );
        }
        o = (midiInputDeviceList.getRightList().getModel().getSize() == 0 ?
                null : midiInputDeviceList.getRightList().getModel());
        if (o != null)
        {
            ListModel lm = (ListModel) o;
            MidiDeviceDescriptor[] descriptors = new MidiDeviceDescriptor[lm.getSize()];
            for (int i = 0; i < lm.getSize(); i++) {
                descriptors[i] = ((MidiDesc) lm.getElementAt( i )).descriptor;
            }
            p.setMidiInputDeviceList( new MidiDeviceList( descriptors ) );
        }
        else
        {
            p.setMidiInputDeviceList( null );
        }
        o = (clickDeviceList.getRightList().getModel().getSize() == 0 ?
                null : clickDeviceList.getRightList().getModel());
        if (o != null) {
            ListModel lm = (ListModel) o;
            MidiDeviceDescriptor[] descriptors = new MidiDeviceDescriptor[lm.getSize()];
            for (int i = 0; i < lm.getSize(); i++) {
                descriptors[i] = ((MidiDesc) lm.getElementAt( i )).descriptor;
            }
            p.setMidiClickDeviceList( new MidiDeviceList( descriptors ) );
        } else {
            p.setMidiClickDeviceList( null );
        }
        p.setDefaultAudioFormatMono( monoRadioButton.isSelected() );
        p.setDefaultAudioFormatSampleRate( ((SampleRate) sampleRateComboBox.getSelectedItem()).sr );
        p.setDefaultAudioFormatSampleSize( ((SampleSize) sampleSizeComboBox.getSelectedItem()).ss );
        p.setDefaultAudioFormatBigEndian( endianCheckBox.isSelected() );
        p.setDefaultAudioFormatSigned( signedCheckBox.isSelected() );
        o = (audioInputDeviceList.getRightList().getModel().getSize() == 0 ?
                null : audioInputDeviceList.getRightList().getModel());
        if (o != null) {
            ListModel lm = (ListModel) o;
            AudioDeviceDescriptor[] descriptors = new AudioDeviceDescriptor[lm.getSize()];
            for (int i = 0; i < lm.getSize(); i++) {
                descriptors[i] = ((AudioDesc) lm.getElementAt( i )).descriptor;
            }
            p.setAudioInputDeviceList( new AudioDeviceList( descriptors ) );
        } else {
            p.setAudioInputDeviceList( null );
        }
        o = (audioOutputDeviceList.getRightList().getModel().getSize() == 0 ?
                null : audioOutputDeviceList.getRightList().getModel());
        if (o != null) {
            ListModel lm = (ListModel) o;
            AudioDeviceDescriptor[] descriptors = new AudioDeviceDescriptor[lm.getSize()];
            
            for (int i = 0; i < lm.getSize(); i++) {
                descriptors[i] = ((AudioDesc) lm.getElementAt( i )).descriptor;
            }
            p.setAudioOutputDeviceList( new AudioDeviceList( descriptors ) );
        } else {
            p.setAudioOutputDeviceList( null );
        }
        // metronome settings
        p.setClicksPerTact( Integer.parseInt( clicksPerTactComboBox.getSelectedItem().toString() ) );
        p.setStressOnClickOne( stressOnOneCheckBox.isSelected() );
        p.setNoteClick(
                (short) (127 - otherClickNoteComboBox.getSelectedIndex()),
                (short) otherClickChannelComboBox.getSelectedIndex(),
                (short) ((Integer) otherClickVolumeSpinner.getValue()).intValue(),
                (short) ((Integer) otherClickDurationSpinner.getValue()).intValue() );
        p.setNoteClickOne(
                (short) (127 - oneClickNoteComboBox.getSelectedIndex()),
                (short) oneClickChannelComboBox.getSelectedIndex(),
                (short) ((Integer) oneClickVolumeSpinner.getValue()).intValue(),
                (short) ((Integer) oneClickDurationSpinner.getValue()).intValue() );
        p.setAudioBufferLength( realtimeSlider.getValue() );
        
        // notify view plugin configurators
        for (int i = 0; i < plugins.length; i++)
        {
            if (plugins[i] != null)
            {
                PluginConfigurator pc = plugins[i].getPluginConfigurator();
                if (pc != null)
                {
                    pc.ok();
                }
            }
        }
    }
    
    /**
     * Cancels.
     */
    private void cancel()
    {
        // notify view plugin configurators
        for (int i = 0; i < plugins.length; i++)
        {
            if (plugins[i] != null)
            {
                PluginConfigurator pc = plugins[i].getPluginConfigurator();
                if (pc != null)
                {
                    pc.cancel();
                }
            }
        }
    }
    
    private void addAllListeners()
    {
        // perform a depth search through the component hierarchy
        for (Object key : treeTabMap.getKeys()) {
            Component c = treeTabMap.getMappedComponent( key );
            addListeners( c );
        }
    }
    private void addListeners( Component c ) {
        //System.out.println( "adding PC listener: " + c );
        if (c instanceof JList) { // workaround for JList
            ((JList) c).addListSelectionListener( this );
            listeningComponents.add( c );
        } else {
            if (!(c instanceof JPanel || c instanceof JOptionPane)) {
                if (c instanceof JTextField) {
                    ((JTextField) c).getDocument().addDocumentListener( this );
                } else if (c instanceof ItemSelectable) {
                    ((ItemSelectable) c).addItemListener( this );
                }
                c.addPropertyChangeListener( this );
                listeningComponents.add( c );
            }
            if (c instanceof Container) {
                Component[] children = ((Container) c).getComponents();
                for (int i = 0; i < children.length; i++) {
                    addListeners( children[i] );
                }
            }
        }
    }
    
    private void removeAllListeners() {
        System.out.println(
            "removeAllListeners(): removing listeners from " +
            listeningComponents.size() + " components" );
        for (int i = 0; i < listeningComponents.size(); i++) {
            Component c = listeningComponents.get( i );
            c.removePropertyChangeListener( this );
            if (c instanceof JList) {
                ((JList) c).removeListSelectionListener( this );
            }
            if (c instanceof ItemSelectable) {
                ((ItemSelectable) c).removeItemListener( this );
            }
            if (c instanceof JTextField) {
                ((JTextField) c).getDocument().removeDocumentListener( this );
            }
        }
        listeningComponents.clear();
    }

    public void valueChanged( ListSelectionEvent e )
    {
        applyButton.setEnabled( true );
    }

    public void itemStateChanged( ItemEvent e )
    {
        applyButton.setEnabled( true );
    }

    public void propertyChange( PropertyChangeEvent e )
    {
        if ("ancestor".equals( e.getPropertyName() )) { return; }
        if (e.getSource() instanceof JSplitPane) { return; }
        applyButton.setEnabled( true );
    }


    public void insertUpdate( DocumentEvent e ) {
        applyButton.setEnabled( true );
    }

    public void removeUpdate( DocumentEvent e ) {
        applyButton.setEnabled( true );
    }

    public void changedUpdate( DocumentEvent e ) {
        applyButton.setEnabled( true );
    }

    private class LNFDesc
    {
        UIManager.LookAndFeelInfo info;
        public LNFDesc( UIManager.LookAndFeelInfo info )
        {
            this.info = info;
        }
        
        public String getClassName() { return info.getClassName(); }
        
        public String toString() { return info.getName(); }
    }
    
    private class MidiDesc {
        MidiDeviceDescriptor descriptor;
        public MidiDesc( MidiDeviceDescriptor descriptor ) {
            this.descriptor = descriptor;
        }
        public String toString() {
            return descriptor.toString();
        }
    }
    
    private class AudioDesc {
        AudioDeviceDescriptor descriptor;
        public AudioDesc( AudioDeviceDescriptor descriptor ) {
            this.descriptor = descriptor;
        }
        public String toString() {
            return descriptor.toString();
        }
    }
    
    private class ViewPanel extends JPanel
    {
        private static final long serialVersionUID = 1;
        
        PluginConfigurator vc;
        ViewPanel( PluginConfigurator vc )
        {
            super( new BorderLayout() );
            this.vc = vc;
            add( getTitleLabel( vc.getTitle() ), BorderLayout.NORTH );
            Object o = vc.getUiObject();
            if (o instanceof Component)
            {
                add( (Component) vc.getUiObject() );
            }
        }
    }
    
    private static class SampleRate {
        float sr;
        String s;
        SampleRate( float sr, String s ) {
            this.sr = sr;
            this.s = s;
        }
        public String toString() {
            return s;
        }
    }

    private static class SampleSize {
        int ss;
        String s;
        SampleSize( int ss, String s ) {
            this.ss = ss;
            this.s = s;
        }
        public String toString() {
            return s;
        }
    }
}
