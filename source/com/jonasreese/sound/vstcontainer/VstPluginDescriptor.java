/*
 * Created on 23.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.vstcontainer;

import java.io.File;

/**
 * @author jonas.reese
 */
public class VstPluginDescriptor {

    private File pluginLibrary;
    private VstNode parent;
    private boolean lazy;
    
    public VstPluginDescriptor( File pluginLibrary, VstNode parent, boolean lazy ) {
        this.pluginLibrary = pluginLibrary;
        this.parent = parent;
        this.lazy = lazy;
    }

    public boolean isLazy() {
        return lazy;
    }

    public VstNode getParent() {
        return parent;
    }

    public File getPluginLibrary() {
        return pluginLibrary;
    }
    
    public String getName() {
        String s = pluginLibrary.getName();
        int index = s.lastIndexOf( "." );
        if (index >= 0) {
            s = s.substring( 0, index );
        }
        return s;
    }
    
    public VstPlugin createPlugin() throws VstPluginNotAvailableException {
        return new VstPlugin( this );
    }
}
