/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 19.09.2003
 */
package com.jonasreese.sound.sg.plugin;

/**
 * <b>
 * This interface shall be implemented by all plugins.
 * </b>
 * @author jreese
 */
public interface Plugin
{
    /**
     * Gets the name of this <code>Plugin</code>.
     * @return The name. Shall not be <code>null</code>.
     */
    public String getName();
    
    /**
     * Gets a short description of this <code>Plugin</code>.
     * @return A short description, or <code>null</code>.
     */
    public String getShortDescription();

    /**
     * Gets a description of this <code>Plugin</code>.
     * @return A description, or <code>null</code>.
     */
    public String getDescription();
    
    /**
     * Gets the (technical) plugin name.
     * @return The plugin name.
     */
    public String getPluginName();
    
    /**
     * Gets the plugin version. Shall be in a dotted format
     * (order required for automatic update functionality).
     * @return The plugin version.
     */
    public String getPluginVersion();
    
    /**
     * Gets the plugin vendor.
     * @return The vendor.
     */
    public String getPluginVendor();
    
    /**
     * Initializes this plugin. Called after the plugin has been loaded,
     * after application startup.
     */
    public void init();
    
    /**
     * Called when this plugin is terminated, before application shutdown.
     */
    public void exit();

    /**
     * Gets the <code>PluginConfigurator</code> for this <code>Plugin</code>.
     * @return The <code>PluginConfigurator</code> instance, or <code>null</code>
     *         if this <code>Plugin</code> does not need to be configured.
     */
    public PluginConfigurator getPluginConfigurator();
}
