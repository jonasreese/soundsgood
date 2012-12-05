/*
 * Created on 07.05.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SessionElementEvent;
import com.jonasreese.sound.sg.SessionElementListener;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.ui.defaultui.SessionActionPool;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;

/**
 * @author jreese
 */
public class SaveAllAction extends SgAction implements SessionElementListener, PropertyChangeListener {
    
    private static final long serialVersionUID = 1;
    
    private Component parent;
    private Session session;
    
    /**
     * Constructs a new <code>SaveAllAction</code>.
     * @param parent The parent <code>Component</code>. This parameter
     *        is used for error message displaying with a
     *        <code>JOptionPane</code>.
     * @param name The name.
     * @param icon The icon, or <code>null</code>.
     */
    public SaveAllAction( Component parent, String name, Icon icon, Session session )
    {
        super( name, icon );
        this.parent = parent;
        this.session = session;
        SessionElementDescriptor[] descs = session.getAllElements();
        for (int i = 0; i < descs.length; i++)
        {
            descs[i].addPropertyChangeListener( this );
        }
        session.addSessionElementListener( this );
    }

    public void actionPerformed( ActionEvent e )
    {
        SessionElementDescriptor[] descs = session.getAllElements();
        for (int i = 0; i < descs.length; i++)
        {
            try
            {
                if (descs[i].isChanged())
                {
                    if (descs[i].getFile() != null)
                    {
                        descs[i].save();
                    }
                    else
                    {
                        session.setSelectedElements( new SessionElementDescriptor[] { descs[i] } );
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
    
    private void update()
    {
        // check if at least one element from the current
        // selection has changed
        SessionElementDescriptor[] descs = session.getAllElements();
        boolean oneChanged = false;
        for (int i = 0; i < descs.length; i++)
        {
            if (descs[i].isChanged())
            {
                oneChanged = true;
                break;
            }
        }
        setEnabled( oneChanged );
    }

    public void propertyChange( PropertyChangeEvent e )
    {
        if ("changed".equals( e.getPropertyName() ))
        {
            update();
        }
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.SessionElementListener#elementAdded(com.jonasreese.sound.sg.SessionElementEvent)
     */
    public void elementAdded( SessionElementEvent e )
    {
        e.getSessionElement().addPropertyChangeListener( this );
        setEnabled( isEnabled() || e.getSessionElement().isChanged() );
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.SessionElementListener#elementRemoved(com.jonasreese.sound.sg.SessionElementEvent)
     */
    public void elementRemoved( SessionElementEvent e )
    {
        e.getSessionElement().removePropertyChangeListener( this );
        update();
    }
}
