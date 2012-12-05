/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 20.09.2003
 */
package com.jonasreese.sound.sg.plugin.view;

/**
 * <b>
 * This interface shall be implemented by classes that
 * represent an instance of a <code>View</code> plugin
 * implementation. Such a view instance is the peer to a panel
 * on the "real screen". 
 * </b>
 * @author jreese
 */
public interface ViewInstance {
    /**
     * Gets the UI object for this <code>ViewInstance</code>. The UI object
     * depends on the UI that is used with the SoundsGood environment.
     * If, for example, a swing UI is used, this method should return
     * an object of type <code>JComponent</code>.
     * @param parentUiObject The parent UI object may be passed to this method
     *        using this parameter. Might also be <code>null</code>.
     * @return The UI specific object.
     */
    public Object getUiObject( ViewContainer parentUiObject );
    
    /**
     * Gets the <code>View</code> that is assigned to this
     * <code>ViewInstance</code>.
     * @return The assigned <code>View</code>. Must not be <code>null</code>!
     */
    public View getView();

    /**
     * Notifies this <code>ViewInstance</code> that it has been opened.
     */
    public void open();
    
    /**
     * Notifies this <code>ViewInstance</code> that it has been activated.
     */
    public void activate();
    
    /**
     * Notifies this <code>ViewInstance</code> that it has been deactivated.
     */
    public void deactivate();
    
    /**
     * Notifies this <code>ViewInstance</code> that it is about to be closed.
     */
    public void close();
    
    /**
     * Gets the 'set bounds allowed' flag. If <code>false</code> is returned
     * by this method, the UI object's preferred size will always be set. Otherwise,
     * the bounds (e.g., from last time this <code>ViewInstance</code> was visible)
     * can be restored and so a size differing from the preferred size can be set.<br>
     * Please note that the interpretation of this method is implementation-dependent.
     * @return <code>true</code> if the bounds can be set from outside,
     *         <code>false</code> if the preferred size from the UI object shall
     *         be used only.
     */
    public boolean isSetBoundsAllowed();
}
