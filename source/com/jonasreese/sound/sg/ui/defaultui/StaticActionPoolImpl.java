/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 05.12.2003
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.jonasreese.sound.sg.FileHandler;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.util.ExtensionFileFilter;
import com.jonasreese.util.ParametrizedResourceBundle;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <b>
 * An implementation of <code>StaticActionPool</code>.
 * </b>
 * @author jreese
 */
public class StaticActionPoolImpl implements StaticActionPool
{
    private HashMap<String,SgAction> hm;
    private SgFrame parent;
    
    /**
     * Constructs a new <code>StaticActionPoolImpl</code>.
     * @param parent The parent <code>SgFrame</code> used for this 
     *        <code>StaticActionPool</code> implementation.
     */
    public StaticActionPoolImpl( SgFrame parent )
    {
        this.parent = parent;
        
        hm = new HashMap<String,SgAction>();
        synchronized (hm)
        {
            final ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
            // *************************************
            // *** Action definition: NEW_SESSION
            // *************************************
            SgAction newSessionAction = new SgAction(
                rb.getString(
                    "menu.file.new.session" ),
                new ResourceLoader( getClass(), "resource/new.gif" ).getAsIcon() )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    NewSessionDialog d = new NewSessionDialog(
                        StaticActionPoolImpl.this.parent,
                        rb.getString(
                            "session.new.title" ) );
                    d.setVisible( true );
                    super.actionPerformed( e );
                }
            };
            newSessionAction.putValue(
                "toolTipText",
                rb.getString(
                    "menu.file.new.session.shortDescription" ) );
            hm.put( NEW_SESSION, newSessionAction );
            // *************************************
            // *** Action definition: NEW_OBJECT
            // *************************************
            SgAction newObjectAction = new SgAction(
                rb.getString(
                    "menu.file.new.object" ),
                new ResourceLoader( getClass(), "resource/new.gif" ).getAsIcon() )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    NewObjectDialog d = new NewObjectDialog(
                        StaticActionPoolImpl.this.parent,
                        rb.getString(
                            "object.new.title" ) );
                    d.setVisible( true );
                    super.actionPerformed( e );
                }
            };
            newObjectAction.putValue(
                "toolTipText",
                rb.getString(
                    "menu.file.new.object.shortDescription" ) );
            hm.put( NEW_OBJECT, newObjectAction );
            // *************************************
            // *** Action definition: OPEN_SESSION
            // *************************************
            SgAction openSessionAction = new SgAction(
                rb.getString(
                    "menu.file.openSession" ),
                new ResourceLoader( getClass(), "resource/open.gif" ).getAsIcon() )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    new Thread()
                    {
                        public void run()
                        {
                            // get the default directoy
                            String directory = SgEngine.getInstance().getProperties().getSessionDirectory();
                            
                            JFileChooser fileChooser = new JFileChooser( directory );
                            fileChooser.setFileFilter(
                                new ExtensionFileFilter(
                                    "sgs",
                                    rb.getString(
                                        "file.soundsGoodSession" ) ) );
                            fileChooser.setDialogTitle(
                                rb.getString(
                                    "session.open.title" ) );
                            int result = fileChooser.showOpenDialog( StaticActionPoolImpl.this.parent );
                            
                            // perform...
                            if (result == JFileChooser.APPROVE_OPTION)
                            {
                                File f = fileChooser.getSelectedFile();
                                SgEngine.getInstance().getProperties().setSessionDirectory( f.getParent() );
                                
                                StaticActionPoolImpl.this.parent.openSession( f );
                            }
                        }
                    }.start();
                    super.actionPerformed( e );
                }
            };
            openSessionAction.putValue(
                Action.ACCELERATOR_KEY, UiToolkit.getKeyStroke(
                        "menu.file.openSession.acceleratorKey" ) );
            openSessionAction.putValue(
                "toolTipText",
                rb.getString(
                    "menu.file.openSession.shortDescription" ) );
            hm.put( OPEN_SESSION, openSessionAction );
            // *************************************
            // *** Action definition: INSERT_MIDI_INTO_SESSION
            // *************************************
            SgAction openFileAction = new SgAction(
                rb.getString(
                    "menu.file.openFile" ),
                new ResourceLoader( getClass(), "resource/import.gif" ).getAsIcon() )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    // get the default directoy
                    String directory = SgEngine.getInstance().getProperties().getFileDirectory();
                    
                    JFileChooser fileChooser = new JFileChooser( directory );

                    ExtensionFileFilter ff = new ExtensionFileFilter(
                            rb.getString(
                                "file.supported" ), true );
                    FileHandler[] fileHandlers = SgEngine.getInstance().getFileHandlers();
                    for (int i = 0; i < fileHandlers.length; i++) {
                        ExtensionFileFilter fileFilter = new ExtensionFileFilter(
                                fileHandlers[i].getDescription(), true );
                        String[] extensions = fileHandlers[i].getExtensions();
                        for (int j = 0; j < extensions.length; j++) {
                            fileFilter.add( extensions[j] );
                            ff.add( extensions[j] );
                        }
                        fileChooser.addChoosableFileFilter( fileFilter );
                    }
                    fileChooser.addChoosableFileFilter( ff );
                    
                    fileChooser.setDialogTitle(
                        rb.getString(
                            "file.open.title" ) );
                    int result = fileChooser.showOpenDialog( StaticActionPoolImpl.this.parent );
                    
                    // perform...
                    if (result == JFileChooser.APPROVE_OPTION)
                    {
                        File f = fileChooser.getSelectedFile();
                        SgEngine.getInstance().getProperties().setFileDirectory( f.getParent() );
                        FileHandler fileHandler = null;
                        for (int i = 0; i < fileHandlers.length; i++) {
                            if (fileHandlers[i].canOpenFileName( f )) {
                                fileHandler = fileHandlers[i];
                                break;
                            }
                        }
                        if (fileHandler == null) {
                            for (int i = 0; i < fileHandlers.length; i++) {
                                if (fileHandlers[i].canOpenFileContent( f )) {
                                    fileHandler = fileHandlers[i];
                                    break;
                                }
                            }
                        }
                        if (fileHandler != null) {
                            fileHandler.openFile( f );
                        } else {
                            JOptionPane.showMessageDialog(
                                UiToolkit.getMainFrame(),
                                rb.getString( "error.cannotOpenFileType.text" ),
                                rb.getString( "error.cannotOpenFileType" ),
                                JOptionPane.ERROR_MESSAGE );
                        }
                    }
                    super.actionPerformed( e );
                }
            };
            openFileAction.setEnabled( false );
            openFileAction.putValue(
                "toolTipText",
                rb.getString(
                    "menu.file.openFile.shortDescription" ) );
            hm.put( INSERT_FILE_INTO_SESSION, openFileAction );
            // *************************************
            // *** Action definition: CLOSE_SESSION
            // *************************************
            SgAction closeAction = new SgAction(
                rb.getString(
                    "menu.file.close" ), UiToolkit.SPACER,
                    rb.getString( "menu.file.close.shortDescription" ) ) {

                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e ) {
                    Session session = SgEngine.getInstance().getActiveSession();
                    StaticActionPoolImpl.this.parent.closeSession( session );
                    super.actionPerformed( e );
                }
            };
            closeAction.setEnabled( false );
            hm.put( CLOSE_SESSION, closeAction );
            // *************************************
            // *** Action definition: CLOSE_ALL_SESSIONS
            // *************************************
            SgAction closeAllAction = new SgAction(
                rb.getString(
                    "menu.file.closeAll" ), UiToolkit.SPACER )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    boolean autoSave = SgEngine.getInstance().getProperties().getAutoSaveSessionOnClose();
                    Session[] sessions = SgEngine.getInstance().getSessions();
                    for (int i = 0; i < sessions.length; i++) {
                        Session session = sessions[i];
                        if (autoSave) {
                            if (session.hasChanged()) {
                                StaticActionPoolImpl.this.parent.saveSession( session );
                            }
                            SgEngine.getInstance().removeSession( session );
                        } else {
                            StaticActionPoolImpl.this.parent.closeSession( session );
                        }
                    }
                    super.actionPerformed( e );
                }
            };
            closeAllAction.setEnabled( false );
            hm.put( CLOSE_ALL_SESSIONS, closeAllAction );
            // *************************************
            // *** Action definition: EXIT
            // *************************************
            SgAction exitAction = new SgAction(
                rb.getString(
                    "menu.file.exit" ),
                UiToolkit.SPACER,
                rb.getString( "menu.file.exit.shortDescription" ) )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    super.actionPerformed( e );
                    StaticActionPoolImpl.this.parent.requestClose();
                }
            };
            exitAction.putValue(
                Action.ACCELERATOR_KEY, UiToolkit.getKeyStroke(
                        "menu.file.exit.acceleratorKey" ) );
            hm.put( EXIT_APPLICATION, exitAction );
            // *************************************
            // *** Action definition: PROGRAM_SETTINGS
            // *************************************
            SgAction generalOptionsAction = new SgAction(
                rb.getString(
                    "menu.options.general" ),
                new ResourceLoader( getClass(), "resource/options.gif" ).getAsIcon(),
                rb.getString( "menu.options.general.shortDescription" ) )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    SgOptionsDialog optionsDialog = new SgOptionsDialog(
                        StaticActionPoolImpl.this.parent,
                        SgEngine.getInstance().getPlugins() );
                    optionsDialog.setVisible( true );
                    super.actionPerformed( e );
                }
            };
            hm.put( PROGRAM_SETTINGS, generalOptionsAction );
            // *************************************
            // *** Action definition: PROGRAM_SETTINGS
            // *************************************
            SgAction gcAction = new SgAction(
                rb.getString(
                    "menu.options.gc" ) )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    System.gc();
                }
            };
            hm.put( GARBAGE_COLLECTION, gcAction );
            // *************************************
            // *** Action definition: FULLSCREEN
            // *************************************
            SgAction fullscreenAction = new SgAction(
                rb.getString(
                    "menu.options.fullscreen" ),
                new ResourceLoader( getClass(), "resource/fullscreen.gif" ).getAsIcon(),
                rb.getString( "menu.options.fullscreen.shortDescription" ) )
            {
                private static final long serialVersionUID = 1;
                public void actionPerformed( ActionEvent e )
                {
                    // toggle fullscreen.
                    if (UiToolkit.getMainFrame() instanceof SgFrame) {
                        SgFrame f = (SgFrame) UiToolkit.getMainFrame();
                        f.setFullscreen( !f.isFullscreen() );
                    }
                    super.actionPerformed( e );
                }
            };
            fullscreenAction.putValue(
                    Action.ACCELERATOR_KEY, UiToolkit.getKeyStroke(
                                "menu.options.fullscreen.acceleratorKey" ) );
            hm.put( FULLSCREEN, fullscreenAction );
        }
    }
    
    public SgAction getAction( String id ) {
        synchronized (hm) {
            return hm.get( id );
        }
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.ui.ActionPool#getActions()
     */
    public SgAction[] getActions() {
        synchronized (hm) {
            SgAction[] actions = new SgAction[hm.size()];
            hm.values().toArray( actions );
            
            return actions;
        }
    }
}
