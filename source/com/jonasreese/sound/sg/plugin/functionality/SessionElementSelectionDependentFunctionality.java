/*
 * Created on 20.06.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.plugin.functionality;

import com.jonasreese.sound.sg.ObjectSelectionChangeListener;
import com.jonasreese.sound.sg.ObjectSelectionChangedEvent;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SessionEvent;
import com.jonasreese.sound.sg.SessionListener;
import com.jonasreese.sound.sg.SgEngine;


/**
 * <p>
 * This class is a partial implementation of the <code>Functionality</code> interface.
 * It is intended to be subclasses by functionalities which depend on the current
 * selection within the current session. This class notifies the subclass of selection
 * change by calling the <code>updateSelection(SessionElementDescriptor[])</code> method.
 * </p>
 * @author jonas.reese
 */
public abstract class SessionElementSelectionDependentFunctionality
    implements Functionality, SessionListener {

    private ObjectSelectionChangeListener objectSelectionChangeListener;
    
    /**
     * Public default constructor.
     */
    public SessionElementSelectionDependentFunctionality() {
        objectSelectionChangeListener = new ObjectSelectionChangeListener() {
            public void objectSelectionChanged( ObjectSelectionChangedEvent e ) {
                updateSelection( e.getSelectedElements() );
            }
        };
    }
    
    /**
     * This method is called when a <code>Session</code>'s selection
     * of <code>SessionElementDescriptor</code> objects changes.
     * @param selObjs The new selection, an empty array if currently active session has
     * no selected objects, or <code>null</code> if no session is active at the current time.
     */
    protected abstract void updateSelection( SessionElementDescriptor[] selObjs );
    
    /**
     * Do not overwrite without calling <code>super.init()</code>.
     * @see com.jonasreese.sound.sg.plugin.Plugin#init()
     */
    public void init() {
        SgEngine.getInstance().addSessionListener( this );
        Session[] sessions = SgEngine.getInstance().getSessions();
        for (int i = 0; i < sessions.length; i++) {
            sessions[i].addObjectSelectionChangeListener( objectSelectionChangeListener );
        }
        Session s = SgEngine.getInstance().getActiveSession();
        if (s != null) {
            updateSelection( s.getSelectedElements() );
        } else {
            updateSelection( null );
        }
    }

    /**
     * Do not overwrite without calling <code>super.exit()</code>.
     * @see com.jonasreese.sound.sg.plugin.Plugin#init()
     */
    public void exit() {
        SgEngine.getInstance().removeSessionListener( this );
        Session[] sessions = SgEngine.getInstance().getSessions();
        for (int i = 0; i < sessions.length; i++) {
            sessions[i].removeObjectSelectionChangeListener( objectSelectionChangeListener );
        }
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.SessionListener#sessionAdded(com.jonasreese.sound.sg.SessionEvent)
     */
    public void sessionAdded( SessionEvent e ) {
        e.getSession().addObjectSelectionChangeListener( objectSelectionChangeListener );
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.SessionListener#sessionRemoved(com.jonasreese.sound.sg.SessionEvent)
     */
    public void sessionRemoved( SessionEvent e ) {
        e.getSession().removeObjectSelectionChangeListener( objectSelectionChangeListener );
        Session s = SgEngine.getInstance().getActiveSession();
        if (s != null) {
            updateSelection( s.getSelectedElements() );
        } else {
            updateSelection( null );
        }
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.SessionListener#sessionActivated(com.jonasreese.sound.sg.SessionEvent)
     */
    public void sessionActivated( SessionEvent e ) {
        if (e.getSession() != null) {
            updateSelection( e.getSession().getSelectedElements() );
        } else {
            updateSelection( null );
        }
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.SessionListener#sessionDeactivated(com.jonasreese.sound.sg.SessionEvent)
     */
    public void sessionDeactivated( SessionEvent e ) {
    }
}
