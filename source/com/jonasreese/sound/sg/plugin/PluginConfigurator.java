/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 01.12.2003
 */
package com.jonasreese.sound.sg.plugin;


/**
 * <b>
 * This interface shall be implemented by classes that can create a UI panel
 * that allows the user to configure a <code>View</code> plugin.
 * </b>
 * @author jreese
 */
public interface PluginConfigurator
{
    /**
     * Gets this <code>PluginConfigurator</code>'s title.
     * @return The title.
     */
    public String getTitle();
    
    /**
     * Gets the UI object for this <code>ViewInstance</code>. The UI object
     * depends on the UI that is used with the SoundsGood environment.
     * If, for example, a swing UI is used, this mehtod should return
     * an object of type <code>JComponent</code>.
     * @return The UI specific object.
     */
    public Object getUiObject();
    
    /**
     * Gets the <code>Plugin</code> that is assigned to this
     * <code>PluginConfigurator</code>.
     * @return The assigned <code>Plugin</code>. Must not be <code>null</code>!
     */
    public Plugin getPlugin();

    /**
     * Notifies this <code>PluginConfigurator</code> that it has been opened.
     */
    public void open();
    
    /**
     * Notifies this <code>PluginConfigurator</code> that it shall save
     * the changes to the settings that have been made.
     */
    public void ok();
    
    /**
     * Notifies this <code>PluginConfigurator</code> that it shall cancel
     * the changes to the settings that have been made.
     */
    public void cancel();
}
