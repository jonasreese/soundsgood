/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 04.12.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.jonasreese.sound.sg.ObjectSelectionChangeListener;
import com.jonasreese.sound.sg.ObjectSelectionChangedEvent;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.ui.defaultui.SessionActionPool;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;

/**
 * <b>
 * This class implements the application's save action.
 * </b>
 * @author jreese
 */
public class SaveAction extends SgAction
    implements ObjectSelectionChangeListener, PropertyChangeListener {
    
    private static final long serialVersionUID = 1;
    
    private Component parent;
    private Session session;
    private Object[] selectedObjects;
    
    /**
     * Constructs a new <code>SaveAction</code>.
     * @param parent The parent <code>Component</code>. This parameter
     *        is used for error message displaying with a
     *        <code>JOptionPane</code>.
     * @param name The name.
     * @param icon The icon, or <code>null</code>.
     */
    public SaveAction( Component parent, String name, Icon icon, Session session )
    {
        super( name, icon );
        this.parent = parent;
        this.session = session;
        
        selectedObjects = null;
        update();
        session.addObjectSelectionChangeListener( this );
    }
    
    public void actionPerformed( ActionEvent e )
    {
        Object[] o = session.getSelectedElements();
        if (o != null && o.length > 0)
        {
            for (int i = 0; i < o.length; i++)
            {
                SessionElementDescriptor ed = (SessionElementDescriptor) o[i];
                try
                {
                    if (ed.isChanged())
                    {
                        if (ed.getFile() != null)
                        {
                            ed.save();
                        }
                        else
                        {
                            SessionActionPool actionPool =
                                UiToolkit.getSessionUi( session ).getActionPool();
                            if (actionPool != null)
                            {
                                SgAction action = actionPool.getAction( SessionActionPool.SAVE_AS );
                                if (action != null)
                                {
                                    action.actionPerformed( e );
                                }
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(
                        parent,
                        ex.getMessage(),
                        SgEngine.getInstance().getResourceBundle().getString(
                            "error.saveFailed" ),
                        JOptionPane.ERROR_MESSAGE );
                }
            }
        }
    }

    private void update()
    {
        System.out.println( "SaveAction.update()" );
        if (selectedObjects != null && selectedObjects.length > 0)
        {
            for (int i = 0; i < selectedObjects.length; i++)
            {
                ((SessionElementDescriptor) selectedObjects[i]).removePropertyChangeListener( this );
            }
        }
        selectedObjects = null;
        Object[] o = session.getSelectedElements();
        if (o == null || o.length == 0)
        {
            setEnabled( false );
        }
        else
        {
            selectedObjects = o;
            // check if at least one element from the current
            // selection has changed
            boolean oneChanged = false;
            for (int i = 0; i < o.length; i++)
            {
                ((SessionElementDescriptor) o[i]).addPropertyChangeListener( this );
                if (((SessionElementDescriptor) o[i]).isChanged())
                {
                    oneChanged = true;
                    break;
                }
            }
            setEnabled( oneChanged );
        }
    }

    public void objectSelectionChanged( ObjectSelectionChangedEvent e )
    {
        update();
    }

    public void propertyChange( PropertyChangeEvent e )
    {
        if ("changed".equals( e.getPropertyName() ))
        {
            update();
        }
    }
}
