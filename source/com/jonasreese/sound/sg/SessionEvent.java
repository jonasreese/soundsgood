/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 18.09.2003
 */
package com.jonasreese.sound.sg;

import java.util.EventObject;

/**
 * <b>
 * </b>
 * @author jreese
 */
public class SessionEvent extends EventObject {
    
    private static final long serialVersionUID = 1;
    
    private Session session;
    private int index;
    private boolean newSession;
    private boolean showErrorsEnabled;

    /**
     * Constructs a new <code>SessionEvent</code>.
     * @param source The source to the event.
     * @param session The according <code>Session</code> object.
     * @param index The session's index in the current list of sessions.
     * @param newSession Shall be <code>true</code> if <code>session</code>
     *        is a newly created session.
     * @param showErrorsEnabled Indicates if errors occurring due to this
     *        <code>SessionEvent</code> shall be displayed to the user.
     */
    public SessionEvent(
            Object source, Session session, int index, boolean newSession, boolean showErrorsEnabled )
    {
        super( source );
        this.session = session;
        this.index = index;
        this.newSession = newSession;
        this.showErrorsEnabled = showErrorsEnabled;
    }
    
    /**
     * Constructs a new <code>SessionEvent</code>.
     * @param source The source to the event.
     * @param session The according <code>Session</code> object.
     * @param index The session's index in the current list of sessions.
     * @param newSession Shall be <code>true</code> if <code>session</code>
     *        is a newly created session.
     */
    public SessionEvent( Object source, Session session, int index, boolean newSession )
    {
        this( source, session, index, newSession, true );
    }
    
    public boolean isNewSession() { return newSession; }
    
    public boolean isShowErrorsEnabled() { return showErrorsEnabled; }
    
    /**
     * Gets the <code>Session</code> that is associated with this
     * <code>SessionEvent</code>.
     * @return The session.
     */
    public Session getSession() { return session; }
    
    /**
     * Gets the according session's index in the list of
     * sessions.
     * @return The index.
     */
    public int getIndex() { return index; }
}
