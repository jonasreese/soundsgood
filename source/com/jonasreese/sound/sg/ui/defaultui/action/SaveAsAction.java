/*
 * Created on 29.04.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.jonasreese.sound.sg.ObjectSelectionChangeListener;
import com.jonasreese.sound.sg.ObjectSelectionChangedEvent;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;

/**
 * <b>
 * This class implements the application's 'save as' action.
 * </b>
 * @author jreese
 */
public class SaveAsAction extends SgAction
    implements ObjectSelectionChangeListener, PropertyChangeListener {
    
    private static final long serialVersionUID = 1;
    
    private Component parent;
    private Session session;
    private Object[] selectedObjects;
    
    /**
     * Constructs a new <code>SaveAsAction</code>.
     * @param parent The parent <code>Component</code>. This parameter
     *        is used for error message displaying with a
     *        <code>JOptionPane</code>.
     * @param name The name.
     * @param icon The icon, or <code>null</code>.
     */
    public SaveAsAction( Component parent, String name, Icon icon, Session session )
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
            SessionElementDescriptor ed = (SessionElementDescriptor) o[0];
            try
            {
                boolean loop = true;
                while (loop)
                {
                    JFileChooser saveAsChooser = new JFileChooser(
                        SgEngine.getInstance().getProperties().getSaveDirectory() );
                    saveAsChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
                    saveAsChooser.setDialogTitle(
                        SgEngine.getInstance().getResourceBundle().getString(
                            "object.saveAs.title" ) );
                    int option = saveAsChooser.showSaveDialog( parent );
                    if (option == JFileChooser.APPROVE_OPTION)
                    {
                        boolean write = false;
                        File selFile = saveAsChooser.getSelectedFile();
                        if (!selFile.exists() || selFile.equals( ed.getFile() ))
                        {
                            loop = false;
                            write = true;
                        }
                        else
                        {
                            String message =
                                SgEngine.getInstance().getResourceBundle().getString(
                                    "object.saveAs.confirmOverwriteText" );
                            String title =
                                SgEngine.getInstance().getResourceBundle().getString(
                                    "object.saveAs.confirmOverwriteTitle" );
                            int r = JOptionPane.showConfirmDialog(
                                parent, message, title, JOptionPane.YES_NO_CANCEL_OPTION );
                            if (r == JOptionPane.YES_OPTION)
                            {
                                loop = false;
                                write = true;
                            }
                            else if (r == JOptionPane.CANCEL_OPTION)
                            {
                                loop = false;
                                write = false;
                            }
                        }
                        
                        if (!loop && write)
                        {
                            ed.setFile( selFile );
                            SgEngine.getInstance().getProperties().setSaveDirectory(
                                selFile.getParent() );
                            ed.save();
                        }
                    }
                    else
                    {
                        loop = false;
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
        if (selectedObjects != null && selectedObjects.length > 0)
        {
            for (int i = 0; i < selectedObjects.length; i++)
            {
                ((SessionElementDescriptor) selectedObjects[i]).removePropertyChangeListener( this );
            }
        }
        selectedObjects = null;
        Object[] o = session.getSelectedElements();
        setEnabled( (o != null && o.length > 0) );
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
