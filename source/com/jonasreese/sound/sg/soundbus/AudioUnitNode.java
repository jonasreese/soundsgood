/*
 * Created on 04.02.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import com.jonasreese.sound.aucontainer.AudioUnit;


/**
 * <p>
 * This interface defines methods provided by an Audio Unit branch node.
 * An Audio Unit branch node is a connector that wraps an Audio Unit and has
 * <i>m</i> MIDI inputs/outputs and <i>n</i> Audio inputs/outputs,
 * depending on the capabilities of the AU wrapped by the
 * <code>AudioUnitNode</code>. 
 * </p>
 * @author jonas.reese
 */
public interface AudioUnitNode extends SbNode {
    /**
     * Gets the <code>AudioUnit</code> that is contained by this <code>AudioUnitNode</code>.
     * @return The associated <code>AudioUnit</code>.
     */
    public AudioUnit getAudioUnit();
    
    /**
     * Sets the <code>AudioUnit</code> for this <code>AudioUnitNode</code>.
     * @param vstPlugin The <code>AudioUnit</code> to set.
     */
    public void setAudioUnit( AudioUnit audioUnit );
    
    /**
     * Returns the (human-readable) Audio Unit name.
     * @return The Audio Unit name.
     */
    public String getAudioUnitName();
}
