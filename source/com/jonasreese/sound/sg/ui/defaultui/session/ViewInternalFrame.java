/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 25.09.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.session;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.ui.defaultui.SessionUi;

/**
 * <b>
 * This class represents an internal frame for an instance of a
 * <code>View</code>. It wraps the <code>View</code> in order to keep
 * it by the visible instance (the <code>ViewInternalFrame</code>
 * itself), but does <b>not</b> add a <code>ViewInstance</code> from
 * the assigned <code>View</code> to itself. This must be done externally. 
 * </b>
 * @author jreese
 */
public class ViewInternalFrame extends JInternalFrame implements ViewContainer
{
    
    private static final long serialVersionUID = 1;
    
    private View view;
    private SessionUi sessionUi;
    private ViewInstance viewInstance;
    
    /**
     * Constructs a new <code>ViewInternalFrame</code>.
     * @param sessionUi The object keeping UI-specific information for
     *        the session of the given <code>ViewInstance</code>.
     * @param view The assigned <code>View</code>.
     * @param viewInstance The assigned <code>ViewInstance</code>.
     */
    public ViewInternalFrame(
        SessionUi sessionUi, View view, ViewInstance viewInstance )
    {
        super( view.getName(), true, true, true, true );
        this.sessionUi = sessionUi;
        this.view = view;
        this.viewInstance = viewInstance;
        addInternalFrameListener( new InternalFrameAdapter() {
            public void internalFrameActivated(InternalFrameEvent e) {
                e.getInternalFrame().toFront();
            }
        } );
    }
    
    
    /**
     * Gets the assigned <code>View</code>.
     * @return The <code>View</code>.
     */
    public View getView()
    {
        return view;
    }

    public void setViewInstance( ViewInstance viewInstance ) {
        this.viewInstance = viewInstance;
    }
    
    /**
     * Gets the assigned <code>ViewInstance</code>.
     * @return The <code>ViewInstance</code>.
     */
    public ViewInstance getViewInstance()
    {
        return viewInstance;
    }

	/**
     * Gets the <code>SessionUi</code> assigned with this
     * <code>ViewInternalFrame</code>.
     * @return The session UI.
	 */
	public SessionUi getSessionUi()
	{
		return sessionUi;
	}


    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.ViewContainer#setTitleText(java.lang.String)
     */
    public void setTitleText( String titleText )
    {
        setTitle( titleText );
    }

    public String getTitleText() { return getTitle(); }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.ViewContainer#setHasFixedSize(boolean)
     */
    public void setHasFixedSize( boolean fixedSize )
    {
        setResizable( !fixedSize );
        setMaximizable( !fixedSize );
    }


	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.plugin.view.ViewContainer#adjustToPreferredSize()
	 */
	public void adjustToPreferredSize()
	{
        pack();
	}
}