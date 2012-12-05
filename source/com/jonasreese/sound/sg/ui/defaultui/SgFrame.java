/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 07.09.2003
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoundedRangeModel;
import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementCreationHandler;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SessionEvent;
import com.jonasreese.sound.sg.SessionListener;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.SgProperties;
import com.jonasreese.sound.sg.edit.UndoableEditUpdateListener;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.SoundbusListener;
import com.jonasreese.sound.sg.ui.defaultui.action.UndoAction;
import com.jonasreese.sound.sg.ui.defaultui.action.ViewAction;
import com.jonasreese.sound.sg.ui.defaultui.session.DockingSessionUi;
import com.jonasreese.sound.sg.ui.defaultui.session.FlexDockSessionUi;
import com.jonasreese.sound.sg.ui.defaultui.session.InternalFrameSessionUi;
import com.jonasreese.sound.sg.ui.defaultui.session.TabbedSessionUi;
import com.jonasreese.ui.swing.JrFrame;
import com.jonasreese.ui.swing.JrWindow;
import com.jonasreese.util.AbstractEventRedirector;
import com.jonasreese.util.EventQueueHandler;
import com.jonasreese.util.EventRedirector;
import com.jonasreese.util.ParamRunnable;
import com.jonasreese.util.ParametrizedResourceBundle;
import com.jonasreese.util.ProgressMonitoringInputStream;
import com.jonasreese.util.Updatable;
import com.jonasreese.util.resource.ResourceLoader;
import com.jonasreese.util.swing.DefaultPopupListener;

/**
 * <b>
 * This is the main frame class of the <i>SoundsGood</i> application.
 * It is intended for <b>single</b> use only. This means, after not being
 * visible any more, the <code>SgFrame</code> object shall not be re-used.
 * </b>
 * @author jreese
 */
public class SgFrame extends JrFrame
    implements PropertyChangeListener, SessionListener, Updatable {
    
    private static final long serialVersionUID = 1;

    private static int frameCount = 0;

    
    private ParametrizedResourceBundle rb;

    private Icon newIcon = null;
    
    private StaticActionPool actionPool;
    private JTabbedPane sessionPane;
    private StatusBar statusBar;
    private HashMap<ProgressMonitoringInputStream,BoundedRangeModel> statusWindows;
    private JMenuBar defaultMenuBar;
    private JRadioButtonMenuItem internalFrameViewModeItem;
    private JRadioButtonMenuItem tabbedViewModeItem;
    private JRadioButtonMenuItem dockingViewItem;
    private JRadioButtonMenuItem extendedDockingViewItem;
    private SgToolBar defaultToolBar;
    private JToolBar currentToolBar;
    private String defaultTitle;
    
    private boolean switchingViewMode;
    
    private BoundedRangeModel sessionOpeningBrm;
    private HashMap<JComponent,Session> sessions;
    private JrWindow fullscreenWindow;
    private ChangeListener menuChangeListener;
    
    private static boolean debug = false;
    
    public static void setDebug( boolean debug ) {
        SgFrame.debug = debug;
    }

    /**
     * Constructs a new <code>SgFrame</code>.
     * @param title The title.
     * @param properties The properties. Must <b>not</b> be <code>null</code>.
     */
    public SgFrame( String title ) {
        super( title );
        this.defaultTitle = title;
        sessions = new HashMap<JComponent,Session>();

        switchingViewMode = false;
        fullscreenWindow = null;

        UiToolkit.setMainFrame( this );

        Toolkit.getDefaultToolkit().setDynamicLayout( true );

        this.rb = SgEngine.getInstance().getResourceBundle();
        
        SgEngine.getInstance().setLoadingUpdatable( this );
        
        statusWindows = new HashMap<ProgressMonitoringInputStream,BoundedRangeModel>();
        
        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );

        // create and set action pool
        actionPool = new StaticActionPoolImpl( this );
        UiToolkit.setActionPool( actionPool );

        SgEngine.getInstance().addSessionListener( 0, SgFrame.this );
        
        setIconImage(
            new ResourceLoader( getClass(), "resource/sg.gif" ).getAsImage() );
        
        this.addComponentListener( new ComponentAdapter() {
            public void componentMoved( ComponentEvent e ) {
                if (getExtendedState() == 0) {
                    SgEngine.getInstance().getProperties().setFrameBounds( getBounds() );
                }
            }
            public void componentResized( ComponentEvent e ) {
                if (getExtendedState() == 0) {
                    SgEngine.getInstance().getProperties().setFrameBounds( getBounds() );
                }
            }
        });
        this.addWindowListener( new WindowAdapter() {
			public void windowOpened(WindowEvent arg0) {
                frameCount++;
                SgEngine.getInstance().getProperties().addPropertyChangeListener(
                    SgFrame.this );
            }
			public void windowClosing( WindowEvent e ) {
			    UiToolkit.getActionPool().getAction( StaticActionPool.EXIT_APPLICATION ).actionPerformed( null );
			}
			public void windowClosed( WindowEvent e ) {
                SgEngine.getInstance().getProperties().removePropertyChangeListener(
                    SgFrame.this );
                SgEngine.getInstance().removeSessionListener( SgFrame.this );
                if (--frameCount == 0) {
                    boolean maximized = ((SgFrame.this.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0);
                    SgEngine.getInstance().getProperties().setFrameMaximized( maximized );
                    SgEngine.getInstance().stopEngine();
                }
			}
        });
        
        enableEvents( AWTEvent.WINDOW_EVENT_MASK );
        
        prepareUi();
        createUi();
    }
    
    protected void processWindowEvent( WindowEvent e ) {
        if (e.getID() == WindowEvent.WINDOW_OPENED) {
            menuChangeListener = new ChangeListener() {
                boolean textChanged = false;
                public void stateChanged( ChangeEvent e ) {
                    MenuSelectionManager source = (MenuSelectionManager) e.getSource();
                    MenuElement[] path = source.getSelectedPath();
                    if (path != null && path.length > 0) {
                        MenuElement currentElement = path[path.length - 1];
                        if (currentElement instanceof JMenuItem) {
                            Action action = ((JMenuItem) currentElement).getAction();                                
                            if (action != null) {
                                Object text = action.getValue( "toolTipText" );
                                if (text != null) {
                                    statusBar.setText( text.toString() );
                                    textChanged = true;
                                } else {
                                    statusBar.setText( "" );
                                    textChanged = true;
                                }
                            } else {
                                statusBar.setText( "" );
                                textChanged = true;
                            }
                        } else {
                            if (textChanged) {
                                textChanged = false;
                                statusBar.popText();
                            }
                        }
                    } else {
                        if (textChanged) {
                            textChanged = false;
                            statusBar.popText();
                        }
                    }
                }
            };
            MenuSelectionManager.defaultManager().addChangeListener( menuChangeListener );
        } else if (e.getID() == WindowEvent.WINDOW_CLOSED) {
            MenuSelectionManager.defaultManager().removeChangeListener( menuChangeListener );
        }
        super.processWindowEvent( e );
    }
    
    /**
     * Creates a <code>JToolBar</code> for the given <code>SessionActionPool</code>
     * @param sessionActionPool The <code>SessionActionPool</code> to use for creating
     *        the tool bar, or <code>null</code> if the main application tool bar
     *        (without session-specific items) shall be created.
     * @return A newly created <code>Vector</code> containing the toolbar information.
     */
    private SgToolBar createToolBar( SessionActionPool sessionActionPool )
    {
        // create tool bar
        SgToolBar toolBar = new SgToolBar();
        toolBar.add( UiToolkit.createToolbarButton( actionPool.getAction( StaticActionPool.NEW_OBJECT ) ) );
        toolBar.add( UiToolkit.createToolbarButton( actionPool.getAction( StaticActionPool.OPEN_SESSION ) ) );
        if (sessionActionPool != null)
        {
            toolBar.add( UiToolkit.createToolbarButton(
                actionPool.getAction( StaticActionPool.INSERT_FILE_INTO_SESSION ) ) );
            toolBar.add( UiToolkit.createToolbarButton(
                sessionActionPool.getAction( SessionActionPool.SAVE ) ) );
        }

        // add tool (plugin) buttons
        if (sessionActionPool != null)
        {
            ViewAction[] viewActions = sessionActionPool.getViewActions();
            if (viewActions.length > 0)
            {
                toolBar.addSeparator();
                
                for (int i = 0; i < viewActions.length; i++)
                {
                    ViewAction va = (ViewAction) viewActions[i];
                    toolBar.add( UiToolkit.createToolbarButton( va ) );
                }
            }
        }

        return toolBar;
    }
    
    /**
     * Creates a <code>JMenuBar</code> for the given <code>SessionActionPool</code>.
     * @param sessionActionPool The <code>SessionActionPool</code> to use for creating
     *        the menu bar, or <code>null</code> if the main application menu bar
     *        (without session-specific items) shall be created.
     * @return The newly created JMenuBar.
     */
    private JMenuBar createMenuBar( SessionActionPool sessionActionPool )
    {
        JMenuBar menuBar = new JMenuBar();
        menuBar.putClientProperty( Options.HEADER_STYLE_KEY, HeaderStyle.BOTH );
        
        JMenu fileMenu = new JMenu( rb.getString( "menu.file" ) );
        JMenu editMenu = new JMenu( rb.getString( "menu.edit" ) );
        JMenu viewMenu = new JMenu( rb.getString( "menu.view" ) );
        JMenu windowMenu = new JMenu( rb.getString( "menu.window" ) );
        JMenu helpMenu = new JMenu( rb.getString( "menu.help" ) );
        
        // fill file menu
        if (newIcon == null)
        {
            newIcon = new ResourceLoader( getClass(), "resource/new.gif" ).getAsIcon();
        }
        JMenu newMenu = new JMenu( new AbstractAction(
            rb.getString( "menu.file.new" ), newIcon ) {
            private static final long serialVersionUID = 1;
            public void actionPerformed(ActionEvent e) {}
        } );
        newMenu.add( actionPool.getAction( StaticActionPool.NEW_SESSION ) );
        if (sessionActionPool != null)
        {
            SessionElementCreationHandler[] handlers =
                SgEngine.getInstance().getSessionElementCreationHandlers();
            if (handlers.length > 0) {
                newMenu.addSeparator();
            }
            for (int i = 0; i < handlers.length; i++) {
                final SessionElementCreationHandler handler = handlers[i];
                Action action = new AbstractAction(
                        handler.getType().getName(), new ImageIcon( handler.getType().getSmallIcon() ) ) {
                    private static final long serialVersionUID = 1L;
                    public void actionPerformed( ActionEvent e ) {
                        Session session = SgEngine.getInstance().getActiveSession();
                        if (session != null) {
                            handler.createSessionElement( session );
                        }
                    }
                };
                action.putValue( SgAction.TOOL_TIP_TEXT, handler.getType().getDescription() );
                newMenu.add( action );
            }
        }
        fileMenu.add( newMenu );
        fileMenu.add( actionPool.getAction( StaticActionPool.OPEN_SESSION ) );
        if (sessionActionPool != null)
        {
            fileMenu.add( actionPool.getAction( StaticActionPool.INSERT_FILE_INTO_SESSION ) );
        }
        fileMenu.addSeparator();
        if (sessionActionPool != null)
        {
            fileMenu.add( sessionActionPool.getAction( SessionActionPool.SAVE ) );
            fileMenu.add( sessionActionPool.getAction( SessionActionPool.SAVE_AS ) );
            fileMenu.add( sessionActionPool.getAction( SessionActionPool.SAVE_COPY_AS ) );
            fileMenu.add( sessionActionPool.getAction( SessionActionPool.SAVE_ALL ) );
            fileMenu.add( sessionActionPool.getAction( SessionActionPool.REVERT ) );
            fileMenu.add( sessionActionPool.getAction( SessionActionPool.SAVE_SESSION ) );
            fileMenu.add( actionPool.getAction( StaticActionPool.CLOSE_SESSION ) );
            fileMenu.add( actionPool.getAction( StaticActionPool.CLOSE_ALL_SESSIONS ) );
            fileMenu.addSeparator();
            fileMenu.add( sessionActionPool.getAction( SessionActionPool.PROPERTIES ) );
            fileMenu.addSeparator();
        }
        fileMenu.add( actionPool.getAction( StaticActionPool.EXIT_APPLICATION ) );
        
        // fill edit menu
        if (sessionActionPool != null)
        {
            JMenuItem undoItem = new JMenuItem( sessionActionPool.getAction( SessionActionPool.UNDO ) );
            ((UndoAction) sessionActionPool.getAction( SessionActionPool.UNDO )).addButton( undoItem );
            JMenuItem redoItem = new JMenuItem( sessionActionPool.getAction( SessionActionPool.REDO ) );
            ((UndoAction) sessionActionPool.getAction( SessionActionPool.REDO )).addButton( redoItem );
            editMenu.add( undoItem );
            editMenu.add( redoItem );
            editMenu.addSeparator();
            editMenu.add( sessionActionPool.getAction( SessionActionPool.CUT ) );
            editMenu.add( sessionActionPool.getAction( SessionActionPool.COPY ) );
            editMenu.add( sessionActionPool.getAction( SessionActionPool.PASTE ) );
            editMenu.addSeparator();
            editMenu.add( sessionActionPool.getAction( SessionActionPool.DELETE ) );
            editMenu.add( sessionActionPool.getAction( SessionActionPool.SELECT_ALL ) );
            editMenu.add( sessionActionPool.getAction( SessionActionPool.SELECT_NONE ) );
            editMenu.add( sessionActionPool.getAction( SessionActionPool.INVERT_SELECTION ) );
        }
        
        // fill view menu
        viewMenu.add( new JCheckBoxMenuItem( actionPool.getAction( StaticActionPool.FULLSCREEN ) ) );

        JMenu layoutMenu = new JMenu( rb.getString( "menu.view.layout" ) );
        int vm = SgEngine.getInstance().getProperties().getViewMode();
        internalFrameViewModeItem = new JRadioButtonMenuItem(
                rb.getString( "options.program.view.mode.internalFrame" ),
                (vm == SgProperties.VIEW_MODE_INTERNAL_FRAMES) );
        internalFrameViewModeItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                SgEngine.getInstance().getProperties().setViewMode( SgProperties.VIEW_MODE_INTERNAL_FRAMES );
            }
        } );
        ButtonGroup bgr = new ButtonGroup();
        bgr.add( internalFrameViewModeItem );
        tabbedViewModeItem = new JRadioButtonMenuItem(
                rb.getString( "options.program.view.mode.tabbed" ),
                (vm == SgProperties.VIEW_MODE_TABBED) );
        tabbedViewModeItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                SgEngine.getInstance().getProperties().setViewMode( SgProperties.VIEW_MODE_TABBED );
            }
        } );
        bgr.add( tabbedViewModeItem );
        dockingViewItem = new JRadioButtonMenuItem(
                rb.getString( "options.program.view.mode.docking" ),
                (vm == SgProperties.VIEW_MODE_DOCKING) );
        dockingViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                SgEngine.getInstance().getProperties().setViewMode( SgProperties.VIEW_MODE_DOCKING );
            }
        } );
        bgr.add( dockingViewItem );
        extendedDockingViewItem = new JRadioButtonMenuItem(
                rb.getString( "options.program.view.mode.extendedDocking" ),
                (vm == SgProperties.VIEW_MODE_EXTENDED_DOCKING) );
        extendedDockingViewItem.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                SgEngine.getInstance().getProperties().setViewMode( SgProperties.VIEW_MODE_EXTENDED_DOCKING );
            }
        } );
        bgr.add( extendedDockingViewItem );
        layoutMenu.add( internalFrameViewModeItem );
        layoutMenu.add( tabbedViewModeItem );
        layoutMenu.add( dockingViewItem );
        layoutMenu.add( extendedDockingViewItem );
        if (layoutMenu.getMenuComponentCount() > 0) {
            viewMenu.addSeparator();
            viewMenu.add( layoutMenu );
        }

        // add tool (plugin) buttons
        if (sessionActionPool != null) {
            ViewAction[] viewActions = sessionActionPool.getViewActions();
            for (int i = 0; i < viewActions.length; i++) {
                ViewAction va = (ViewAction) viewActions[i];
//                if (!va.getView().isMultipleInstanceView()) {
                JCheckBoxMenuItem item = new JCheckBoxMenuItem( va );
                va.addButton( item );
                windowMenu.add( item );
//                } else {
//                    JMenuItem item = new JMenuItem( va );
//                    va.addButton( item );
//                    windowMenu.add( item );
//                }
            }
        }
        
        windowMenu.addSeparator();
        windowMenu.add(
            new JMenuItem(
                new AbstractAction(
                    rb.getString( "menu.window.showMidi" ),
                    new ResourceLoader( getClass(), "resource/info.gif" ).getAsIcon() )
        {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e )
            {
                SgMidiInformationFrame f = new SgMidiInformationFrame(
                    rb.getString( "midiInfoFrame.title" ) );
                f.setIconImage( ((ImageIcon) getValue( Action.SMALL_ICON )).getImage() );
                f.pack();
                f.setLocation( getX() + 20, getY() + 20 );
                f.setVisible( true );
            }
        } ) );
        windowMenu.addSeparator();
        windowMenu.add( new JMenuItem( actionPool.getAction( StaticActionPool.PROGRAM_SETTINGS ) ) );
//        windowMenu.addSeparator();
//        windowMenu.add( new JMenuItem( actionPool.getAction( StaticActionPool.GARBAGE_COLLECTION ) ) );
        
        Action tipAction = new AbstractAction(
            rb.getString( "menu.help.tips" ),
            new ResourceLoader( getClass(), "resource/tip_small.gif" ).getAsIcon() )
        {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e )
            {
                showTipsDialog();
            }
        };
        tipAction.putValue( SgAction.TOOL_TIP_TEXT, rb.getString( "menu.help.tips.shortDescription" ) );
        helpMenu.add( tipAction );
        helpMenu.addSeparator();
        Action aboutAction = new AbstractAction(
                rb.getString( "menu.help.about" ),
                new ResourceLoader( getClass(), "resource/info.gif" ).getAsIcon() ) {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e ) {
                showAboutDialog();
            }
        };
        aboutAction.putValue( SgAction.TOOL_TIP_TEXT, rb.getString( "menu.help.about.shortDescription" ) );
        helpMenu.add( new JMenuItem( aboutAction ) );
        if (debug) {
            helpMenu.addSeparator();
            helpMenu.add( new AbstractAction( "GC" ) {
                private static final long serialVersionUID = 1L;
                public void actionPerformed( ActionEvent e ) {
                    System.gc();
                } 
            } );
            helpMenu.add( new AbstractAction( "Debug listeners" ) {
                private static final long serialVersionUID = 1L;
                public void actionPerformed( ActionEvent e ) {
                    System.out.println( "Listing SgProperties listeners:" );
                    for (PropertyChangeListener pcl :
                        SgEngine.getInstance().getProperties().getPropertyChangeListeners()) {
                        System.out.println( "\t" + pcl );
                    }
                    System.out.println();
                    System.out.println( "Listing SessionActionPool ActionReceivers:" );
                    for (SgAction action : UiToolkit.getSessionUi(
                                SgEngine.getInstance().getActiveSession() ).getActionPool().getActions()) {
                        System.out.println( "\t" + action.getActionReceiver() );
                    }
                    System.out.println();
                    System.out.println( "Listing SessionElement listeners:" );
                    for (SessionElementDescriptor sed : SgEngine.getInstance().getActiveSession().getAllElements()) {
                        System.out.println( "\t" + sed.getName() + " PropertyChangeListeners" );
                        for (PropertyChangeListener pcl : sed.getPropertyChangeListeners()) {
                            System.out.println( "\t\t" + pcl );
                        }
                        System.out.println( "\t" + sed.getName() + " PropertyHooks" );
                        for (Updatable u : sed.getPropertyHooks()) {
                            System.out.println( "\t\t" + u );
                        }
                        System.out.println( "\t" + sed.getName() + " UndoableEditUpdateListeners" );
                        for (UndoableEditUpdateListener l : sed.getUndoableEditUpdateListeners()) {
                            System.out.println( "\t\t" + l );
                        }
                        if (sed instanceof SoundbusDescriptor) {
                            try {
                                Soundbus sb = ((SoundbusDescriptor) sed).getSoundbus();
                                System.out.println( "\t" + sed.getName() + " Soundbus: SoundbusListeners" );
                                for (SoundbusListener l : sb.getSoundbusListeners()) {
                                    System.out.println( "\t\t" + l );
                                }
                                System.out.println();
                                System.out.println( "\t" + sed.getName() + " Soundbus: Nodes: PropertyChangeListeners" );
                                for (SbNode n : sb.getNodes()) {
                                    for (PropertyChangeListener l : n.getPropertyChangeListeners()) {
                                        System.out.println( "\t\t" + l );
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        System.out.println();
                    }
                } 
            } );
        }
        
        if (fileMenu.getItemCount() > 0) { menuBar.add( fileMenu ); }
        if (editMenu.getItemCount() > 0) { menuBar.add( editMenu ); }
        if (viewMenu.getItemCount() > 0) { menuBar.add( viewMenu ); }
        if (windowMenu.getItemCount() > 0) { menuBar.add( windowMenu ); }
        if (helpMenu.getItemCount() > 0) { menuBar.add( helpMenu ); }
        
        return menuBar;
    }
    
    /**
     * Prepares the UI (especially for the given OS).
     */
    private void prepareUi() {
        if (UiToolkit.isMacOs()) {
            Application app = Application.getApplication();
            app.setEnabledPreferencesMenu( true );
            app.addApplicationListener( new ApplicationListener() {
                public void handleAbout( ApplicationEvent e ) {
                    showAboutDialog();
                    e.setHandled( true );
                }
                public void handleOpenApplication( ApplicationEvent e ) {
                }
                public void handleOpenFile( ApplicationEvent e ) {
                }
                public void handlePreferences( ApplicationEvent e ) {
                    actionPool.getAction( StaticActionPool.PROGRAM_SETTINGS ).actionPerformed( null );
                    e.setHandled( true );
                }
                public void handlePrintFile( ApplicationEvent e ) {
                }
                public void handleQuit( ApplicationEvent e ) {
                    e.setHandled( false );
                    requestClose();
                }
                public void handleReOpenApplication( ApplicationEvent e ) {
                }
            } );
        }
    }
    
    /**
     * Creates all UI components and add them to the content pane.
     */
    private void createUi() {
        // Create menu bar
        defaultMenuBar = createMenuBar( null );
        UiToolkit.setDefaultMenuBar( defaultMenuBar );
        setJMenuBar( defaultMenuBar );
        
        // Create session tab pane
        sessionPane = new JTabbedPane();
        JPopupMenu sessionMenu = new JPopupMenu();
        sessionMenu.add( actionPool.getAction( StaticActionPool.CLOSE_SESSION ) );
        sessionMenu.add( actionPool.getAction( StaticActionPool.CLOSE_ALL_SESSIONS ) );
        sessionPane.addMouseListener( new DefaultPopupListener( sessionMenu ) );
        sessionPane.getModel().addChangeListener( new ChangeListener() {
			public void stateChanged( ChangeEvent e ) {
                //System.out.println( "sessionPane.stateChanged()" );
                Object o = sessionPane.getSelectedComponent();
                Session session = (Session) sessions.get( o );
                SgEngine.getInstance().setActiveSession( session );
			}
        } );
        Session[] sessions = SgEngine.getInstance().getSessions();
        Session activeSession = SgEngine.getInstance().getActiveSession();
        int tabIndex = -1;
        for (int i = 0; i < sessions.length; i++) {
            if (sessions[i] == activeSession) {
                tabIndex = i;
            }
            sessionPane.addTab(
                (sessions[i].hasChanged() ? "*" : "") +
                sessions[i].getName(), createSessionPane( sessions[i] ) );
        }
        if (tabIndex >= 0) {
            sessionPane.setSelectedIndex( tabIndex );
        }

        statusBar = new StatusBar( rb.getString( "statusbar.defaultText" ) );

        defaultToolBar = createToolBar( null );
        
        // Add components to this frame's content pane
        JPanel cp = new JPanel( new BorderLayout() );
        cp.add( sessionPane );
        cp.add( statusBar, BorderLayout.SOUTH );
        setToolBar( defaultToolBar );
        getContentPane().add( cp );
    }
    
    private void setToolBar( SgToolBar toolBar ) {
        if (currentToolBar == null) {
            currentToolBar = new JToolBar( rb.getString( "toolbar.title" ) );
            currentToolBar.putClientProperty( Options.HEADER_STYLE_KEY, HeaderStyle.BOTH );
            currentToolBar.setRollover( true );
            getContentPane().add( currentToolBar, BorderLayout.PAGE_START );
        } else {
            currentToolBar.removeAll();
        }
        toolBar.fillJToolBar( currentToolBar );
        currentToolBar.repaint(); // graphical update does not work properly
    }
    
    /**
     * Adjusts the <code>SgFrame</code>s bounds.
     */
    public void adjustBounds() {
        Rectangle bounds = SgEngine.getInstance().getProperties().getFrameBounds();
        
        adjustBounds( bounds );
        
        if (SgEngine.getInstance().getProperties().isFrameMaximized()) {
            setExtendedState( MAXIMIZED_BOTH );
        }
    }

    /**
     * Applies the LNF that is currently set in the properties.
     */
    protected void applyLNF() throws ClassNotFoundException,
                                     InstantiationException,
                                     IllegalAccessException,
                                     UnsupportedLookAndFeelException
    {
        String s = SgEngine.getInstance().getProperties().getLNFClassName();
        if (s != null && !UIManager.getLookAndFeel().getClass().getName().equals( s ))
        {
            UIManager.setLookAndFeel( s );
            SwingUtilities.updateComponentTreeUI( this );
        }
    }

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange( PropertyChangeEvent e ) {
        String pn = e.getPropertyName();
        
        if (e.getSource() instanceof SgProperties) {
            if (pn.equals( "lnfClassName" )) {
                // set new LNF
                try {
                    applyLNF();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        rb.getString( "error.lnfApply" ),
                        JOptionPane.ERROR_MESSAGE );
                }
            } else if (pn.equals( "undoSteps" )) {
                // update undo steps for all existing SessionElementDescriptors
                Session[] sessions = SgEngine.getInstance().getSessions();
                for (int i = 0; i < sessions.length; i++) {
					SessionElementDescriptor[] descs = sessions[i].getAllElements();
                    for (int j = 0; j < descs.length; j++) {
//                        System.out.println(
//                            "element :  " +
//                            descs[j].getName() +
//                            " undo limit is now " +
//                            e.getNewValue() );
						descs[j].getUndoManager().setLimit(
                            ((Integer) e.getNewValue()).intValue() );
					}
				}
            } else if (pn.equals( "viewMode" )) {
                // use event queue to avoid interference with pending operations,
                // especially on sessionListener method calls
                EventQueueHandler eventQueue = SgEngine.getInstance().getEventQueue();
                
                final int newVal = ((Integer) e.getNewValue()).intValue();
                
                EventRedirector redirector = new AbstractEventRedirector( null ) {
                    public void redirectEvent( EventObject eo ) {
                        Session[] openSessions = SgEngine.getInstance().getSessions();
                        switchingViewMode = true;
                        for (int i = 0; i < openSessions.length; i++) {
                            sessionRemoved( new SessionEvent( this, openSessions[i], 0, false, false ) );
                        }
                        for (int i = 0; i < openSessions.length; i++) {
                            sessionAdded( new SessionEvent( this, openSessions[i], i, false, false ) );
                        }
                        switchingViewMode = false;
                        sessionActivated(
                                new SessionEvent(
                                        this,
                                        SgEngine.getInstance().getActiveSession(),
                                        SgEngine.getInstance().getActiveSessionIndex(), false ) );
                        
                        // set correct item to 'selected'
                        if (internalFrameViewModeItem != null) {
                            internalFrameViewModeItem.setSelected( (newVal == SgProperties.VIEW_MODE_INTERNAL_FRAMES) );
                        }
                        if (tabbedViewModeItem != null) {
                            tabbedViewModeItem.setSelected( (newVal == SgProperties.VIEW_MODE_TABBED) );
                        }
                        if (dockingViewItem != null) {
                            dockingViewItem.setSelected( (newVal == SgProperties.VIEW_MODE_DOCKING) );
                        }
                        if (extendedDockingViewItem != null) {
                            extendedDockingViewItem.setSelected( (newVal == SgProperties.VIEW_MODE_EXTENDED_DOCKING) );
                        }
                        JMenuBar mb = getJMenuBar();
                        if (mb != null) {
                            mb.updateUI();
                        }
                    }
                };
                eventQueue.addQueueEntry( redirector, null );
                eventQueue.processEvents();
            }
        } else if (e.getSource() instanceof Session) {
            Session session = (Session) e.getSource();
            if ("changed".equals( pn )) {
                Session[] sessions = SgEngine.getInstance().getSessions();
                for (int i = 0; i < sessions.length; i++) {
                    if (sessions[i] == session) {
                        sessionPane.setTitleAt( i,
                            (session.hasChanged() ? "*" : "") +
                            session.getName() );
                    }
                }
            }
        }
	}
    
    public void update( Object data ) {
        if (data instanceof ProgressMonitoringInputStream) {
            ProgressMonitoringInputStream in =
                (ProgressMonitoringInputStream) data;
            BoundedRangeModel brm = statusWindows.get( data );
            if (brm == null) {
                brm = new DefaultBoundedRangeModel(
                        0, 0, 0, in.getMaximumBytes() );
                StatusWindow statusWindow = new StatusWindow( brm, 
                        rb.getString( "engine.loadingFile" ) +
                        ((File) in.getData()).toString() );
                statusWindows.put( in, brm );
                statusWindow.setVisible( true );
            }
            int n = in.getReadBytes();
            //System.out.println( "" + brm.getValue() + " of " + brm.getMaximum() + ", n = " + n );
            brm.setValue( n );
            //System.out.println( "done" );
        }
    }

    /**
     * Opens a session. If the opening fails, this method does
     * not throw an exception but shows an error dialog instead.
     * @param sessionFile The <code>File</code> pointing to the resource
     *        that describes the session.
     * @return <code>true</code> if was successfull, <code>false</code> otherwise.
     */
    private boolean openSessionImpl( File sessionFile ) {
        try {
            SgEngine.getInstance().loadSession( sessionFile );
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                rb.getString( "session.open.errorOnLoadFileText" ) + "\n" +
                ex.getMessage(),
                rb.getString( "session.open.errorOnLoadFile" ),
                JOptionPane.ERROR_MESSAGE );
        }
        return false;
    }
    
    /**
     * Opens a session. If the opening fails, this method does
     * not throw an exception but shows an error dialog instead.
     * @param sessionFile The <code>File</code> pointing to the resource
     *        that describes the session.
     */
    public void openSession( File sessionFile ) {
        openSessions( new File[]{ sessionFile }, -1 );
    }
    
    /**
     * Opens a number of sessions.
     * @param sessionFiles An array of <code>File</code> objects, each pointing
     *        to a file that describes a session.
     * @param selectedIndex The session index to be selected (activated) afterwards.
     */
    public void openSessions( File[] sessionFiles, int selectedIndex ) {
        if (sessionFiles == null || sessionFiles.length == 0) { return; }
        JDialog d = new JDialog( this, rb.getString( "session.opening" ), true );
        JProgressBar pb = new JProgressBar( 0, sessionFiles.length * 100 );
        sessionOpeningBrm = pb.getModel();
        d.getContentPane().add(
            new JLabel( rb.getString( "session.open.openingSessions" ) ), BorderLayout.NORTH );
        d.getContentPane().add( pb );
        d.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
        d.pack();
        d.setSize( d.getWidth() * 2, d.getHeight() );
        d.setResizable( false );
        d.setLocation(
            getX() + getWidth() / 2 - d.getWidth() / 2,
            getY() + getHeight() / 2 - d.getHeight() / 2 );
        ParamRunnable pr = new ParamRunnable( d ) {
            public void run() {
                JDialog d = (JDialog) getParameter();
                d.setVisible( true );
            }
        };
        new Thread( pr ).start();
        for (int i = 0; i < sessionFiles.length; i++) {
            pb.setValue( (i + 1) * 100 - 50 );
            synchronized (sessionOpeningBrm) {
                boolean b = openSessionImpl( sessionFiles[i] );
                if (b) {
                    try {
                        sessionOpeningBrm.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            pb.setValue( (i + 1) * 100 );
        }
        if (selectedIndex >= 0) {
            SgEngine.getInstance().setActiveSession( selectedIndex );
        }
        d.setVisible( false );
        sessionOpeningBrm = null;
    }
    
    /**
     * Creates a graphical panel for the given session.
     * @param session The session to create a panel for.
     * @return The panel, as a <code>JComponent</code> object.
     */
    private JComponent createSessionPane( Session session ) {
        JPanel p = new JPanel( new BorderLayout() );
        return p;
    }
    
    /**
     * Closes the given view instance.
     * @param vi The <code>ViewInstance</code> to be closed.
     */
    public void closeViewInstance( ViewInstance vi ) {
    }
    
    /**
     * Gets the current session pane.
     * @return The current session pane, or <code>null</code> if no session
     *         is currently active.
     */
    public JTabbedPane getSessionPane() {
        return sessionPane;
    }
    
    public void sessionAdded( SessionEvent e ) {
        //System.out.println( "sessionAdded(), switchingViewMode = " + switchingViewMode );
        JComponent panel = createSessionPane( e.getSession() );
        sessions.put( panel, e.getSession() );

        SessionActionPoolImpl sessionActionPool =
            new SessionActionPoolImpl( this, e.getSession() );
        // remove all SessionUi objects that are registered as SessionListeners from the session
        // this is required for very special cases where a SessionUi registers itself as listener 
        SessionListener[] sessionListeners = e.getSession().getSessionListeners();
        for (int i = 0; i < sessionListeners.length; i++){
            if (sessionListeners[i] instanceof SessionUi) {
                e.getSession().removeSessionListener( sessionListeners[i] );
            }
        }
        
        SessionUi sessionUi = null;
        if (SgEngine.getInstance().getProperties().getViewMode() == SgProperties.VIEW_MODE_INTERNAL_FRAMES) {
            sessionUi = new InternalFrameSessionUi(
                panel,
                createMenuBar( sessionActionPool ),
                createToolBar( sessionActionPool ),
                sessionActionPool );
        } else if (SgEngine.getInstance().getProperties().getViewMode() == SgProperties.VIEW_MODE_TABBED) {
            sessionUi = new TabbedSessionUi(
                    panel,
                    createMenuBar( sessionActionPool ),
                    createToolBar( sessionActionPool ),
                    sessionActionPool );
        } else if (SgEngine.getInstance().getProperties().getViewMode() == SgProperties.VIEW_MODE_DOCKING) {
            sessionUi = new DockingSessionUi(
                    panel,
                    createMenuBar( sessionActionPool ),
                    createToolBar( sessionActionPool ),
                    sessionActionPool );
        } else if (SgEngine.getInstance().getProperties().getViewMode() == SgProperties.VIEW_MODE_EXTENDED_DOCKING) {
            sessionUi = new FlexDockSessionUi(
                    panel,
                    createMenuBar( sessionActionPool ),
                    createToolBar( sessionActionPool ),
                    sessionActionPool );
        }

        if (sessionUi != null) {
            UiToolkit.addSessionUi( e.getSession(), sessionUi );
        }

        BoundedRangeModel brm = sessionOpeningBrm;
        int initialVal = brm == null ? 0 : brm.getValue();
        sessionUi.restoreViewInstances( brm, e.isNewSession(), switchingViewMode, e.isShowErrorsEnabled() );
        
        if (brm != null) { brm.setValue( initialVal + 100 ); }

        e.getSession().addPropertyChangeListener( this );
        sessionPane.addTab(
            (e.getSession().hasChanged() ? "*" : "") +
            e.getSession().getName(), panel );
        if (brm != null) {
            synchronized (brm) {
                brm.notify();
            }
        }
        
        // enable CLOSE_ALL_SESSIONS action
        actionPool.getAction( StaticActionPool.CLOSE_ALL_SESSIONS ).setEnabled( true );
    }

    public void sessionRemoved( SessionEvent e ) {
        //System.out.println( "sessionRemoved()" );
    	synchronized (sessions) {
            Object removeKey = null;
            for (Object key : sessions.keySet()) {
                Object value = sessions.get( key );
                if (value == e.getSession()) {
                    removeKey = key;
                }
            }
            if (removeKey != null) {
                sessions.remove( removeKey );
            }
		}
        e.getSession().removePropertyChangeListener( this );
        sessionPane.removeTabAt( e.getIndex() );
        UiToolkit.removeSessionUi( e.getSession() );
        
        // disable CLOSE_ALL_SESSIONS action if last session has been removed
        if (SgEngine.getInstance().getSessionCount() == 0) {
            actionPool.getAction( StaticActionPool.CLOSE_ALL_SESSIONS ).setEnabled( false );
        }
        // disable CLOSE_SESSION action if no active session present
        if (SgEngine.getInstance().getActiveSession() == null) {
            actionPool.getAction( StaticActionPool.CLOSE_SESSION ).setEnabled( false );
        }
    }

    public void sessionActivated( SessionEvent e ) {
        if (e.getSession() == null) {
            setTitle( defaultTitle );
        } else {
            setTitle( defaultTitle + " - " + e.getSession().getName() );
        }
        final int index = SgEngine.getInstance().getActiveSessionIndex();
        if (index != sessionPane.getSelectedIndex()) {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    sessionPane.setSelectedIndex( index );
                }
            } );
        }
        
        try {
            if (e.getSession() != null) {
                final SessionUi sessionUi = UiToolkit.getSessionUi( e.getSession() );
                UiToolkit.setSessionUi( sessionUi );
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        setJMenuBar( sessionUi.getMenuBar() );
                        setToolBar( sessionUi.getToolBar() );
                    }
                } );
            } else {
                setJMenuBar( defaultMenuBar );
                setToolBar( defaultToolBar );
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // update actions
        boolean b = (e.getSession() != null);
        actionPool.getAction( StaticActionPool.INSERT_FILE_INTO_SESSION ).setEnabled( b );
        actionPool.getAction( StaticActionPool.CLOSE_SESSION ).setEnabled( b );
    }
    
    public void sessionDeactivated( SessionEvent e )
    {
    }
    
    /**
     * Displays the application's "about" dialog.
     */
    public void showAboutDialog() {
        JOptionPane p = new JOptionPane();
        p.setOptions( new Object[0] );
        p.setBackground( Color.WHITE );
        p.setMessage( rb.getString( "help.about.text" ) );
        p.setIcon( new ResourceLoader( getClass(), "resource/logo.jpg" ).getAsIcon() );
        JOptionPane.showMessageDialog(
            SgFrame.this,
            p,
            rb.getString( "menu.help.about" ),
            JOptionPane.PLAIN_MESSAGE,
            null );
    }

    /**
     * Displays a "TIPS" dialog.
     */
    public void showTipsDialog()
    {
        TipDialog dialog = new TipDialog( rb.getString( "tips.title" ), this );
        dialog.setSize( 560, 400 );
        dialog.setLocation( getX() + getWidth() / 2 - dialog.getWidth() / 2,
                            getY() + getHeight() / 2 - dialog.getHeight() / 2 );

        dialog.setVisible( true );
    }
    
    /**
     * Saves the given <code>Session</code>.
     * @param session The <code>Session</code> to be saved.
     * @return <code>true</code> if it has been saved, <code>false</code> otherwise.
     */
    public boolean saveSession( Session session )
    {
        Object retryOption = rb.getString( "session.save.errorRetry" );
        Object cancelOption = rb.getString( "session.save.errorCancel" );
        boolean retry = true;
        boolean saved = false;
        while (retry)
        {
            try
            {
                retry = false;
                session.saveSessionToFile();
                saved = true;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                if (JOptionPane.showOptionDialog(
                    this,
                    rb.getString( "session.save.errorOnSaveFileText" ) + "\n" +
                    ex.getMessage(),
                    rb.getString( "session.save.errorOnSaveFile" ),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    new Object[] { retryOption, cancelOption },
                    retryOption ) == 0)
                {
                    retry = true;
                }
            }
        }
        return saved;
    }
    
    /**
     * Asks to close the given <code>Session</code>.
     * @param session The <code>Session</code> to be closed.
     * @return 1 if the session has been saved and closed, 0 if the session
     * 			has been closed but not saved and -1 if the session has not
     * 			been saved or closed.
     */
    public int closeSession( Session session ) {
        int option = JOptionPane.NO_OPTION;
        
        // check if session elements have changed
        SessionElementDescriptor[] elements = session.getAllElements();
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].isChanged()) {
                option = JOptionPane.showConfirmDialog(
                this,
                rb.getString( "session.close.saveFileText", elements[i].getName() ),
                rb.getString( "session.close.saveFile" ),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE );
                if (option == JOptionPane.CANCEL_OPTION) {
                    return -1;
                } else if (option == JOptionPane.YES_OPTION) {
                    try {
                        elements[i].save();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(
                                this,
                                ex.getMessage(),
                                rb.getString( "error.saveFailed" ),
                                JOptionPane.ERROR_MESSAGE );
                    }
                }
            }
        }
        
        option = JOptionPane.NO_OPTION;
        
        // check if session has changed
        if (session.hasChanged()) {
            if (SgEngine.getInstance().getProperties().getAutoSaveSessionOnClose()) {
                saveSession( session );
                SgEngine.getInstance().removeSession( session );
                return 1;
            }
	        option = JOptionPane.showConfirmDialog(
	            this,
	            rb.getString( "session.close.saveSessionText" ),
	            rb.getString( "session.close.saveSession" ),
	            JOptionPane.YES_NO_CANCEL_OPTION,
	            JOptionPane.WARNING_MESSAGE );
        }
        if (option == JOptionPane.CANCEL_OPTION) {
            return -1;
        }
        if (option == JOptionPane.YES_OPTION) {
            saveSession( session );
            SgEngine.getInstance().removeSession( session );
            return 1;
        }
        SgEngine.getInstance().removeSession( session );
        return 0;
    }
    
    /**
     * Closes all sessions that are currently opened within this <code>SgFrame</code>.
     * @return <code>true</code> if all sessions were closed, <code>false</code> otherwise.
     */
    public boolean closeSessions() {
        ArrayList<Session> sessions = new ArrayList<Session>();
        sessions.addAll( this.sessions.values() );
        for (Session session : sessions) {
            int result = closeSession( session );
            if (result < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ask this <code>SgFrame</code> to be closed.
     * @return <code>true</code> if this <code>SgFrame</code> has been closed, <code>false</code>
     * 			otherwise.
     */
    public boolean requestClose() {
        ArrayList<Session> list = new ArrayList<Session>();
        list.addAll( sessions.values() );
        int index = SgEngine.getInstance().getActiveSessionIndex();
        boolean b = closeSessions();
        if (b) {
            SgEngine.getInstance().getProperties().addOpenSessions( list );
            SgEngine.getInstance().getProperties().setActiveSessionIndex( index );
            dispose();
        }
        return b;
    }
    
    /**
     * Sets the fullscreen mode to the given value.
     * @param fullscreen
     */
    public void setFullscreen( boolean fullscreen ) {
        if (fullscreen && isFullscreen()) {
            return;
        }
        if (!fullscreen && !isFullscreen()) {
            return;
        }
        
        if (fullscreen) {
            //setVisible( false );
            fullscreenWindow = new JrWindow( this );
            fullscreenWindow.setContentPane( getContentPane() );
            fullscreenWindow.setBounds( getBounds() );
            fullscreenWindow.getRootPane().setJMenuBar( getJMenuBar() );
            fullscreenWindow.setFullscreen( true );
            fullscreenWindow.setVisible( true );
        } else {
            fullscreenWindow.setVisible( false );
            setContentPane( fullscreenWindow.getContentPane() );
            setJMenuBar( fullscreenWindow.getRootPane().getJMenuBar() );
            fullscreenWindow = null;
            setVisible( true );
        }
    }
    
    /**
     * Gets the current <code>fullscreen</code> mode value.
     * @return
     */
    public boolean isFullscreen() {
        return (fullscreenWindow != null);
    }
    
    /**
     * The application window's status bar
     */
    class StatusBar extends JPanel {
        private static final long serialVersionUID = 1;
        JLabel textLabel;
        String initialText;
        Stack<String> s;
        StatusBar( String initialText ) {
            super( new FlowLayout( FlowLayout.LEFT, 3, 3 ) );
            this.initialText = initialText;
            textLabel = new JLabel();
            setText( initialText );
            add( textLabel );
            s = new Stack<String>();
        }
        void setText( String text ) {
            if (text == null || text.equals( "" )) {
                text = " ";
            }
            textLabel.setText( text );
        }
        
        void setText( String text, boolean push ) {
            setText( text );
            if (push) {
                s.push( text );
            }
        }
        
        void popText() {
            String text;
            if (s.isEmpty()) {
                text = initialText;
            } else {
                text = s.pop().toString();
            }
            setText( text );
        }
    }


    class StatusWindow extends JWindow implements ChangeListener
    {
        private static final long serialVersionUID = 1;
        StatusWindow( BoundedRangeModel brm, String text )
        {
            super( SgFrame.this );
            JProgressBar progressBar = new JProgressBar( brm );
            brm.addChangeListener( this );
            JPanel panel = new JPanel( new BorderLayout() );
            panel.add( progressBar );
            panel.setBorder( new TitledBorder( text ) );
            getContentPane().add( panel );
            pack();
            setSize( getWidth() * 2, getHeight() );
            Rectangle d = SgFrame.this.getBounds();
            setLocation( d.x + d.width / 2 - getWidth() / 2,
                d.y + d.height / 2 - getHeight() / 2 );
        }
    
        public void stateChanged( final ChangeEvent e )
        {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    BoundedRangeModel brm = (BoundedRangeModel) e.getSource();
                    if (brm.getValue() == brm.getMaximum())
                    {
                        dispose();
                        for (ProgressMonitoringInputStream in : statusWindows.keySet()) {
                            BoundedRangeModel m = statusWindows.get( in );
                            if (m == brm) {
                                statusWindows.remove( in );
                                break;
                            }
                        }
                    }
                }
            } );
        }
    }
}
