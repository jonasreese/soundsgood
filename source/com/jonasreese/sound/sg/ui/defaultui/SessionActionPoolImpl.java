/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 05.12.2003
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JOptionPane;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.ui.defaultui.action.RevertAction;
import com.jonasreese.sound.sg.ui.defaultui.action.SaveAction;
import com.jonasreese.sound.sg.ui.defaultui.action.SaveAllAction;
import com.jonasreese.sound.sg.ui.defaultui.action.SaveAsAction;
import com.jonasreese.sound.sg.ui.defaultui.action.SaveCopyAsAction;
import com.jonasreese.sound.sg.ui.defaultui.action.UndoAction;
import com.jonasreese.sound.sg.ui.defaultui.action.ViewAction;
import com.jonasreese.util.ParametrizedResourceBundle;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <b>
 * An implementation of <code>StaticActionPool</code>.
 * </b>
 * @author jreese
 */
public class SessionActionPoolImpl implements SessionActionPool
{
    private HashMap<String,Action> hm;
    private SgFrame parent;
    private Session session;
    private ViewAction[] viewActions;
    
    /**
     * Constructs a new <code>SessionActionPoolImpl</code>.
     * @param parent The parent <code>SgFrame</code> used for this 
     *        <code>StaticActionPool</code> implementation.
     * @param session The according <code>Session</code>.
     */
    public SessionActionPoolImpl( SgFrame parent, Session session )
    {
        this.parent = parent;
        this.session = session;

        // count number of ViewActions to be created
        Plugin[] plugins = SgEngine.getInstance().getPlugins();
        int c = 0;        
        for (int i = 0; i < plugins.length; i++)
        {
            if (plugins[i] instanceof View)
            {
                c++;
            }
        }
        viewActions = new ViewAction[c];
        c = 0;
        for (int i = 0; i < plugins.length; i++)
        {
            if (plugins[i] instanceof View)
            {
                viewActions[c++] = new ViewAction( (View) plugins[i], parent, session );
            }
        }
        
        hm = new HashMap<String,Action>();

        final ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        
        // *************************************
        // *** Action definition: SAVE
        // *************************************
        Action saveAction = new SaveAction(
            SessionActionPoolImpl.this.parent,
            rb.getString(
                "menu.file.save" ),
            new ResourceLoader( getClass(), "/resource/save.gif" ).getAsIcon(),
            session );
        saveAction.putValue(
            Action.ACCELERATOR_KEY, UiToolkit.getKeyStroke(
                    "menu.file.save.acceleratorKey" ) );
        saveAction.putValue(
            "toolTipText",
            rb.getString(
                "menu.file.save.shortDescription" ) );
        saveAction.setEnabled( false );
        hm.put( SAVE, saveAction );
        // *************************************
        // *** Action definition: SAVE_ALL
        // *************************************
        Action saveAllAction = new SaveAllAction(
            SessionActionPoolImpl.this.parent,
            rb.getString(
                "menu.file.saveAll" ),
            new ResourceLoader( getClass(), "/resource/saveall.gif" ).getAsIcon(),
            session );
        saveAllAction.setEnabled( false );
        saveAllAction.putValue(
            "toolTipText",
            rb.getString(
                "menu.file.saveAll.shortDescription" ) );
        hm.put( SAVE_ALL, saveAllAction );
        // *************************************
        // *** Action definition: SAVE_AS
        // *************************************
        Action saveAsAction = new SaveAsAction(
            SessionActionPoolImpl.this.parent,
            rb.getString(
                "menu.file.saveAs" ), UiToolkit.SPACER, session );
        saveAsAction.setEnabled( false );
        saveAsAction.putValue(
            "toolTipText",
            rb.getString(
                "menu.file.saveAs.shortDescription" ) );
        hm.put( SAVE_AS, saveAsAction );
        // *************************************
        // *** Action definition: SAVE_COPY_AS
        // *************************************
        Action saveCopyAsAction = new SaveCopyAsAction(
            SessionActionPoolImpl.this.parent,
            rb.getString(
                "menu.file.saveCopyAs" ), UiToolkit.SPACER, session );
        saveCopyAsAction.setEnabled( false );
        saveCopyAsAction.putValue(
            "toolTipText",
            rb.getString(
                "menu.file.saveCopyAs.shortDescription" ) );
        hm.put( SAVE_COPY_AS, saveCopyAsAction );
        // *************************************
        // *** Action definition: REVERT
        // *************************************
        Action revertAction = new RevertAction(
            SessionActionPoolImpl.this.parent,
            rb.getString(
                "menu.file.revert" ), UiToolkit.SPACER, session );
        revertAction.setEnabled( false );
        revertAction.putValue(
            "toolTipText",
            rb.getString(
                "menu.file.revert.shortDescription" ) );
        hm.put( REVERT, revertAction );
        // *************************************
        // *** Action definition: SAVE_SESSION
        // *************************************
        Action saveSessionAction = new SgAction(
            rb.getString(
                "menu.file.saveSession" ),
            UiToolkit.SPACER )
        {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e )
            {
                Session activeSession = SgEngine.getInstance().getActiveSession();
                if (activeSession != null)
                {
                    try
                    {
                        activeSession.saveSessionToFile();
                    }
                    catch (IOException ex)
                    {
                        JOptionPane.showMessageDialog(
                            SessionActionPoolImpl.this.parent,
                            rb.getString(
                                "session.save.errorOnSaveFileText" ) + "\n" +
                            ex.getMessage(),
                            rb.getString(
                                "session.save.errorOnSaveFile" ),
                            JOptionPane.ERROR_MESSAGE );
                    }
                }
                super.actionPerformed( e );
            }
        };
        session.addPropertyChangeListener( "changed", new PropertyChangeListener()
        {
            public void propertyChange( PropertyChangeEvent e )
            {
                getAction( SAVE_SESSION ).setEnabled( ((Boolean) e.getNewValue()).booleanValue() ) ;
            }
        } );
        saveSessionAction.putValue(
            "toolTipText",
            rb.getString(
                "menu.file.saveSession.shortDescription" ) );
        saveSessionAction.setEnabled( session.hasChanged() );
        hm.put( SAVE_SESSION, saveSessionAction );
        // *************************************
        // *** Action definition: PROPERTIES
        // *************************************
        Action propertiesAction = new SgAction(
            rb.getString(
                "menu.file.properties" ),
            new ResourceLoader( getClass(), "/resource/properties.gif" ).getAsIcon() );
        propertiesAction.putValue(
            Action.ACCELERATOR_KEY, UiToolkit.getKeyStroke(
                    "menu.file.properties.acceleratorKey" ) );
        propertiesAction.putValue(
            "toolTipText",
            rb.getString(
                "menu.file.properties.shortDescription" ) );
        hm.put( PROPERTIES, propertiesAction );
        // *************************************
        // *** Action definition: UNDO
        // *************************************
        UndoAction undoAction = UndoAction.getUndoInstance(
            new ResourceLoader( getClass(), "resource/undo.gif" ).getAsIcon(), session );
        hm.put( UNDO, undoAction );
        // *************************************
        // *** Action definition: REDO
        // *************************************
        UndoAction redoAction = UndoAction.getRedoInstance(
            new ResourceLoader( getClass(), "resource/redo.gif" ).getAsIcon(), session );
        hm.put( REDO, redoAction );
        // *************************************
        // *** Action definition: DELETE
        // *************************************
        SgAction deleteAction = new SgAction(
            rb.getString(
                "menu.edit.delete" ),
            new ResourceLoader( getClass(), "resource/remove.gif" ).getAsIcon() );
        deleteAction.putValue(
            "toolTipText",
            rb.getString(
                "menu.edit.delete.shortDescription" ) );
        hm.put( DELETE, deleteAction );
        // *************************************
        // *** Action definition: CUT
        // *************************************
        SgAction cutAction = new SgAction(
            rb.getString( "menu.edit.cut" ),
            new ResourceLoader( getClass(),"resource/cut.gif" ).getAsIcon(),
            rb.getString( "menu.edit.cut.shortDescription" ) );
        cutAction.putValue(
            Action.ACCELERATOR_KEY, UiToolkit.getKeyStroke(
                    "menu.edit.cut.acceleratorKey" ) );
        hm.put( CUT, cutAction );
        // *************************************
        // *** Action definition: COPY
        // *************************************
        SgAction copyAction = new SgAction(
            rb.getString( "menu.edit.copy" ),
            new ResourceLoader( getClass(), "resource/copy.gif" ).getAsIcon(),
            rb.getString( "menu.edit.copy.shortDescription" ) );
        copyAction.putValue(
            Action.ACCELERATOR_KEY, UiToolkit.getKeyStroke(
                    "menu.edit.copy.acceleratorKey" ) );
        hm.put( COPY, copyAction );
        // *************************************
        // *** Action definition: PASTE
        // *************************************
        SgAction pasteAction = new SgAction(
            rb.getString( "menu.edit.paste" ),
            new ResourceLoader( getClass(), "resource/paste.gif" ).getAsIcon(),
            rb.getString( "menu.edit.paste.shortDescription" ) );
        pasteAction.putValue(
            Action.ACCELERATOR_KEY, UiToolkit.getKeyStroke(
                    "menu.edit.paste.acceleratorKey" ) );
        hm.put( PASTE, pasteAction );
        // *************************************
        // *** Action definition: SELECT_ALL
        // *************************************
        SgAction selectAllAction = new SgAction(
            rb.getString(
                "menu.edit.selectAll" ), UiToolkit.SPACER,
                rb.getString( "menu.edit.selectAll.shortDescription" ));
        selectAllAction.putValue(
            Action.ACCELERATOR_KEY, UiToolkit.getKeyStroke(
                    "menu.edit.selectAll.acceleratorKey" ) );
        hm.put( SELECT_ALL, selectAllAction );
        // *************************************
        // *** Action definition: SELECT_NONE
        // *************************************
        SgAction selectNoneAction = new SgAction(
            rb.getString(
                "menu.edit.selectNone" ), UiToolkit.SPACER,
                rb.getString( "menu.edit.selectNone.shortDescription" ));
        selectNoneAction.putValue(
            Action.ACCELERATOR_KEY, UiToolkit.getKeyStroke(
                    "menu.edit.selectNone.acceleratorKey" ) );
        hm.put( SELECT_NONE, selectNoneAction );
        // *************************************
        // *** Action definition: INVERT_SELECTION
        // *************************************
        SgAction invertSelectionAction = new SgAction(
            rb.getString(
                "menu.edit.invertSelection" ), UiToolkit.SPACER,
                rb.getString( "menu.edit.invertSelection.shortDescription" ) );
        invertSelectionAction.putValue(
            Action.ACCELERATOR_KEY, UiToolkit.getKeyStroke(
                    "menu.edit.invertSelection.acceleratorKey" ) );
        hm.put( INVERT_SELECTION, invertSelectionAction );
    }
    
    public SgAction getAction( String id )
    {
        synchronized (hm)
        {
            return (SgAction) hm.get( id );
        }
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.ui.ActionPool#getActions()
     */
    public SgAction[] getActions()
    {
        synchronized (hm)
        {
            SgAction[] actions = new SgAction[hm.size()];
            Iterator<String> keys = hm.keySet().iterator();
            
            for (int i = 0; i < actions.length; i++)
            {
                actions[i] = getAction( keys.next() );
            }
            
            return actions;
        }
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.ui.defaultui.SessionActionPool#getSession()
     */
    public Session getSession()
    {
        return session;
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.ui.defaultui.SessionActionPool#getViewActions()
     */
    public ViewAction[] getViewActions()
    {
        return viewActions;
    }
}
