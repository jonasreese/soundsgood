/*
 * Copyright (c) 2004 Jonas Reese
 * Created on 23.10.2004
 */
package com.jonasreese.sound.sg.ui.defaultui.session;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.ui.defaultui.SessionUi;

/**
 * <b>
 * This class represents a tab panel for an instance of a
 * <code>View</code>. It wraps the <code>View</code> in order to keep
 * it by the visible instance (which is a <code>JPanel</code>
 * itself), but does <b>not</b> add a <code>ViewInstance</code> from
 * the assigned <code>View</code> to itself. This must be done externally. 
 * </b>
 * @author jreese
 */
public class ViewTab extends JPanel implements ViewContainer
{
    
    private static final long serialVersionUID = 1;
    
    private View view;
    private TabbedSessionUi sessionUi;
    private ViewInstance viewInstance;
    
    public ViewTab(
        TabbedSessionUi sessionUi, View view, ViewInstance viewInstance )
    {
        super( new BorderLayout() );
        this.sessionUi = sessionUi;
        this.view = view;
        this.viewInstance = viewInstance;
    }
    
    
    /**
     * Gets the assigned <code>View</code>.
     * @return The <code>View</code>.
     */
    public View getView()
    {
        return view;
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
        sessionUi.getTabbedPane().setTitleAt( getTabIndex(), titleText );
    }
    
    public int getTabIndex()
    {
        JTabbedPane tabbedPane = sessionUi.getTabbedPane();
        for (int i = 0; i < tabbedPane.getTabCount(); i++)
        {
            if (tabbedPane.getComponentAt( i ) == this) { return i; }
        }
        return -1;
    }

    public String getTitleText() { return sessionUi.getTabbedPane().getTitleAt( getTabIndex() ); }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.ViewContainer#setHasFixedSize(boolean)
     */
    public void setHasFixedSize( boolean fixedSize )
    {
        // has no effect
    }


    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.plugin.view.ViewContainer#adjustToPreferredSize()
     */
    public void adjustToPreferredSize()
    {
    }
}