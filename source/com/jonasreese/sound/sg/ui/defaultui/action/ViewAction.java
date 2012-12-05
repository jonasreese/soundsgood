/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 25.09.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.jonasreese.sound.sg.ObjectSelectionChangeListener;
import com.jonasreese.sound.sg.ObjectSelectionChangedEvent;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.plugin.view.ViewInstanceCreationFailedException;
import com.jonasreese.sound.sg.ui.ViewInstanceEvent;
import com.jonasreese.sound.sg.ui.ViewInstanceListener;
import com.jonasreese.sound.sg.ui.defaultui.SessionUi;
import com.jonasreese.sound.sg.ui.defaultui.SgFrame;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.util.ParamRunnable;

/**
 * <b>
 * This class implements an <code>AbstractAction</code> that wraps a <code>View</code>
 * plugin and is capable to create <code>ViewInstance</code>s from it. For creating
 * a <code>ViewInstance</code>, the according <code>SessionUi</code> is being fetched
 * from the <code>UiToolkit</code> and then invoked to create a new <code>ViewInstance</code>.
 * </b>
 * <b>
 * <code>ViewAction</code> also bewares consistency with the environment concerning
 * the properties defined by the <code>View</code>. As an example for this,
 * <code>ViewAction</code> allows the creation of one session-wide
 * <code>singleInstance</code>-<code>View</code> only and one per
 * <code>SessionElementDescriptor</code> for <code>multipleInstance</code>-<code>View</code>s.
 * </b>
 * @author jreese
 */
public class ViewAction extends AbstractAction implements
    ObjectSelectionChangeListener, ViewInstanceListener {
    
    private static final long serialVersionUID = 1;
    
    private View view;
    private List<AbstractButton> buttons;
    private SgFrame parent;
    private Session session;
    private ViewInstance currentVi;
    private boolean notYetAddedViListener;
    private Map<ViewInstance,SessionElementDescriptor> createdVIs;
    
    /**
     * Constructs a new <code>ViewAction</code>.
     * @param view The <code>View</code> this <code>ViewAction</code> is triggering.
     * @param parent The parent <code>SgFrame</code>.
     * @param session The session.
     */
    public ViewAction( View view, SgFrame parent, Session session )
    {
        super( view.getName(), getIconFromView( view ) );
        this.view = view;
        this.parent = parent;
        buttons = new ArrayList<AbstractButton>();
        this.session = session;
        notYetAddedViListener = true;
        session.addObjectSelectionChangeListener( this );
        putValue( "toolTipText", view.getShortDescription() );

        createdVIs = new HashMap<ViewInstance,SessionElementDescriptor>();

        setEnabled( view.isAutostartView() || view.canHandle( null ) );
    }

    private static Icon getIconFromView( View view )
    {
        if (view instanceof Icon)
        {
            return (Icon) view;
        }
        return null;
    }
    
    /**
     * Gets the <code>Session</code> this <code>ViewAction</code> belongs to.
     * @return The <code>Session</code>.
     */
    public Session getSession() { return session; }
    
    /**
     * Gets the <code>View</code> associated with this <code>ViewAction</code>.
     * @return
     */
    public View getView() { return view; }

    /**
     * Adds a button that is associated with this <code>ViewAction</code>.
     * Use this method if you want to keep track of buttons assigned with
     * this abstract action, e.g. in order to toggle the button's state
     * depending on the focus.
     * @param button The button to add.
     */
    public void addButton( AbstractButton button ) {
        synchronized (buttons) {
            buttons.add( button );
        }
    }

    /**
     * Gets all buttons that are assigned with this <code>ViewAction</code>.
     * @return All buttons, as an array of <code>AbstractButton</code> objects.
     */
    public AbstractButton[] getButtons() {
        synchronized (buttons) {
            AbstractButton[] b = new AbstractButton[buttons.size()];
            for (int i = 0; i < b.length; i++) {
                b[i] = (AbstractButton) buttons.get( i );
            }
            return b;
        }
    }
    
    /**
     * Performs this <code>ViewAction</code>s action.
     * @param d The session element descriptor to be used.
     * @param showErrors If set to <code>true</code>, indicates that errors shall
     * be displayed in a message dialog.
     */
    public void actionPerformed( SessionElementDescriptor d, boolean showErrors ) {
        Runnable r = new ParamRunnable( new Object[] { d, (showErrors ? Boolean.TRUE : Boolean.FALSE) } ) {
            public void run() {
                syncAp(
                        (SessionElementDescriptor) ((Object[]) getParameter())[0],
                        ((Boolean) ((Object[]) getParameter())[1]).booleanValue() );
            }
        };
        new Thread( r ).start();
    }

    /**
     * Performs this <code>ViewAction</code>s action.
     * @param d The session element descriptor to be used.
     */
    public void actionPerformed( SessionElementDescriptor d ) {
        actionPerformed( d, true );
    }
    
    /**
     * Performs this <code>ViewAction</code>s action, using the current selection as data.
     * @param showErrors If set to <code>true</code>, indicates that errors shall
     * be displayed in a message dialog.
     */
    public void actionPerformed( boolean showErrors ) {
        actionPerformed( (SessionElementDescriptor) null, showErrors );
    }

    /**
     * Performs this <code>ViewAction</code>s action, using the current selection as data.
     */
    public void actionPerformed() {
        actionPerformed( (SessionElementDescriptor) null, true );
    }

    /**
     * This method is used for a synchronized actionPerformed()
     * call in a seprate thread. Shall only be called from
     * <code>actionPerformed()</code>.
     * @param data The session element descriptor to be used.
     * @param showErrors If set to <code>true</code>, indicates that errors shall
     * be displayed in a message dialog.
     */
    private synchronized void syncAp( SessionElementDescriptor d, boolean showErrors ) {
        // this veryfies that only one listener is added
        if (notYetAddedViListener) {
            UiToolkit.getSessionUi( session ).addViewInstanceListener( this );
            notYetAddedViListener = false;
        }

        // get currently selected value(s)
        if (d == null) {
            SessionElementDescriptor[] selectedObjects = session.getSelectedElements();
            if (selectedObjects == null ||
                selectedObjects.length == 0) {
                d = null;
            } else {
                d = selectedObjects[0];
            }
        }

        // check if there is already a ViewInstance in current scope
        // (scope is 'session' for singleViewInstance and
        // 'sessionElement' for multipleViewInstance)
        ViewInstance vi = null;
        
        if (view.isMultipleInstancePerSessionAllowed()) {
            if (!view.isMultipleInstancePerSessionElementAllowed() && d != null) {
                synchronized (createdVIs) {
                    Iterator<ViewInstance> keys = createdVIs.keySet().iterator();
                    while (keys.hasNext()) {
                        ViewInstance key = keys.next();
                        if (createdVIs.get( key ) == d) {
                            vi = key;
                            break;
                        }
                    }
                }
            } 
        } else {
            vi = currentVi;
            System.out.println( "currentVi = " + vi );
        }
        
        if (vi != null) {
            UiToolkit.getSessionUi( session ).removeViewInstance( vi );
        } else {
            SessionUi su = UiToolkit.getSessionUi( session );
            if (su != null) {
                try {
                    vi = su.createViewInstance( ViewAction.this, session, d );
                } catch (ViewInstanceCreationFailedException e) {
                    e.printStackTrace();
                    if (showErrors) {
                        JOptionPane.showMessageDialog(
                            parent,
                            SgEngine.getInstance().getResourceBundle().getString(
                                "error.viewInstanceCreationFailedText" ) + "\n" +
                            e.getMessage(),
                            SgEngine.getInstance().getResourceBundle().getString(
                                "error.viewInstanceCreationFailed" ),
                            JOptionPane.ERROR_MESSAGE );
                    }
                }
                
                if (vi != null) {
                    if (d != null && view.isMultipleInstancePerSessionAllowed()) {
                        synchronized (createdVIs) {
                            createdVIs.put( vi, d );
        
                            Object[] selObjs = session.getSelectedElements();
                            if (selObjs != null && selObjs.length > 0) {
                                setButtonsSelected( createdVIs.get( vi ) == selObjs[0] );
                            } else {
                                setButtonsSelected( false );
                            }
                        }
                    } else {
                        currentVi = vi;
                    }
                } else {
                    setButtonsSelected( false );
                }
            }
        }
    }

    public void setEnabled( boolean b ) {
        AbstractButton[] buttons = getButtons();
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setEnabled( b );
        }
        super.setEnabled( b );
    }

    public void actionPerformed( ActionEvent e ) {
        actionPerformed();
    }

    private void setButtonsSelected( boolean b ) {
        AbstractButton[] buttons = getButtons();
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setSelected( b );
        }
    }

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.ObjectSelectionChangeListener#objectSelectionChanged(com.jonasreese.sound.sg.ObjectSelectionChangedEvent)
	 */
	public void objectSelectionChanged( ObjectSelectionChangedEvent e ) {
        //System.out.println( "objectSelectionChanged: " + e.getSelectedObjects() );
        if ((e.getSelectedElements() != null && e.getSelectedElements().length > 0)) {
            setEnabled( view.canHandle( e.getSelectedElements()[0] ) );
            if (view.isMultipleInstancePerSessionAllowed()) {
                setButtonsSelected( createdVIs.containsValue( e.getSelectedElements()[0] ) );
            }
        } else {
            setEnabled( view.canHandle( null ) );
        }
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.ui.ViewInstanceListener#viewInstanceAdded(com.jonasreese.sound.sg.ui.ViewInstanceEvent)
	 */
	public void viewInstanceAdded( ViewInstanceEvent e ) {
        if (e.getViewInstance().getView() != view) { return; }
        System.out.println( "viewInstanceAdded()" );
        if (!view.isMultipleInstancePerSessionAllowed()) {
            setButtonsSelected( true );
            currentVi = e.getViewInstance();
        }
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.ui.ViewInstanceListener#viewInstanceRemoved(com.jonasreese.sound.sg.ui.ViewInstanceEvent)
	 */
	public void viewInstanceRemoved( ViewInstanceEvent e ) {
        if (e.getViewInstance().getView() != view) { return; }
        System.out.println( "ViewAction.viewInstanceRemoved()" );
        currentVi = null;
        if (!view.isMultipleInstancePerSessionAllowed()) {
            setButtonsSelected( false );
        } else {
            Object[] selectedObjects = session.getSelectedElements();
            if (selectedObjects != null &&
                selectedObjects.length > 0) {
                synchronized (createdVIs) {
                    if (createdVIs.get( e.getViewInstance() ) == selectedObjects[0]) {
                        setButtonsSelected( false );
                    }
                }
            } else {
                setButtonsSelected( false );
            }
        }
        // remove value from hashtable
        createdVIs.remove( e.getViewInstance() );
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.ui.ViewInstanceListener#viewInstanceActivated(com.jonasreese.sound.sg.ui.ViewInstanceEvent)
	 */
	public void viewInstanceActivated( ViewInstanceEvent e ) {
        if (e.getViewInstance().getView() != view) { return; }
	}
}