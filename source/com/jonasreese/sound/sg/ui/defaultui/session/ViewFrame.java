/*
 * Created on 27.10.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.session;


import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.ui.defaultui.SessionUi;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.ui.swing.JrDialog;

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
public class ViewFrame extends JrDialog implements ViewContainer {
    
    private static final long serialVersionUID = 1;
    
    private View view;
    private SessionUi sessionUi;
    private ViewInstance viewInstance;
    
    public ViewFrame(
        SessionUi sessionUi, View view, ViewInstance viewInstance )
    {
        super( UiToolkit.getMainFrame(), false );
        setTitle( view.getName() );
        this.sessionUi = sessionUi;
        this.view = view;
        this.viewInstance = viewInstance;
    }
    
    
    /**
     * Gets the assigned <code>View</code>.
     * @return The <code>View</code>.
     */
    public View getView() {
        return view;
    }

    /**
     * Gets the assigned <code>ViewInstance</code>.
     * @return The <code>ViewInstance</code>.
     */
    public ViewInstance getViewInstance() {
        return viewInstance;
    }

    /**
     * Gets the <code>SessionUi</code> assigned with this
     * <code>ViewInternalFrame</code>.
     * @return The session UI.
     */
    public SessionUi getSessionUi() {
        return sessionUi;
    }


    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.ViewContainer#setTitleText(java.lang.String)
     */
    public void setTitleText(String titleText) {
        setTitle(titleText);
    }
    
    public String getTitleText() { return getTitle(); }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.ViewContainer#setHasFixedSize(boolean)
     */
    public void setHasFixedSize(boolean fixedSize) {
        setResizable(false);
    }


    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.plugin.view.ViewContainer#adjustToPreferredSize()
     */
    public void adjustToPreferredSize() {
        pack();
    }
}
