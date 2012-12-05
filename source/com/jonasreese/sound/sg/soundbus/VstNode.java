/*
 * Created on 04.02.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import com.jonasreese.sound.vstcontainer.VstPlugin;

/**
 * <p>
 * This interface defines methods provided by a VST branch node.
 * A VST branch node is a connector that wraps a VST plugin and has
 * <i>m</i> MIDI inputs/outputs and <i>n</i> Audio inputs/outputs,
 * depending on the capabilities of the VST plugin wrapped by the
 * <code>VstNode</code>. 
 * </p>
 * @author jonas.reese
 */
public interface VstNode extends SbNode {
    /**
     * Gets the <code>VstPlugin</code> that is contained by this <code>VstNode</code>.
     * @return The associated <code>VstPlugin</code>.
     */
    public VstPlugin getVstPlugin();
    
    /**
     * Sets the <code>VstPlugin</code> for this <code>VstNode</code>.
     * @param vstPlugin The <code>VstPlugin</code> to set.
     */
    public void setVstPlugin( VstPlugin vstPlugin );
    
    /**
     * Returns the (human-readable) VST plugin name.
     * @return The VST plugin name.
     */
    public String getVstPluginName();
}
