/*
 * Created on 06.10.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui;

import java.util.EventObject;

import com.jonasreese.sound.sg.plugin.view.ViewInstance;

/**
 * @author jreese
 */
public class ViewInstanceEvent extends EventObject
{
    private static final long serialVersionUID = 1;
    
    private ViewInstance vi;

	/**
	 * @param source The source of the event. Usually, this should be
     *        an instance of <code>SessionUi</code>.
	 */
	public ViewInstanceEvent( Object source, ViewInstance vi )
	{
		super( source );
        this.vi = vi;
	}
    
    /**
     * Gets the assigned <code>ViewInstance</code> object.
     * @return The <code>ViewInstance</code>.
     */
    public ViewInstance getViewInstance()
    {
        return vi;
    }
}