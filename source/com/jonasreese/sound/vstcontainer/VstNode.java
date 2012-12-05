/*
 * Created on 25.10.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.vstcontainer;

import java.io.File;

/**
 * @author jonas.reese
 */
public interface VstNode {
    /**
     * Gets the children for this <code>VstNode</code>.
     * @return The children. May be an empty array, but not <code>null</code>.
     */
    public VstNode[] getChildren();
    
    /**
     * Gets the parent <code>VstNode</code>.
     * @return The parent, or <code>null</code> if this is the root.
     */
    public VstNode getParent();
    
    /**
     * Gets the root path for this <code>VstNode</code>. 
     * @return The root path.
     */
    public File getRootPath();
    
    /**
     * Gets the path for this <code>VstNode</code>.
     * @return The path.
     */
    public File getPath();
    
    /**
     * Gets this <code>VstNode</code>'s name.
     * @return This <code>VstNode</code>'s name.
     */
    public String getName();
    
    /**
     * Gets all VST plugins contained in this <code>VstNode</code>.
     * @return A list of all VST plugins in this <code>VstNode</code>.
     * May be an empty array, but not <code>null</code>.
     */
    public VstPluginDescriptor[] getPluginDescriptors();
    
    /**
     * Returns <code>true</code> if this <code>VstNode</code> contains plugins
     * or child <code>VstNode</code> objects that contain plugins.
     * @return <code>true</code> if this node contains plugins or any child node
     * returns <code>true</code> when calling <code>containsPlugins()</code>.
     */
    public boolean containsPlugins();
}
