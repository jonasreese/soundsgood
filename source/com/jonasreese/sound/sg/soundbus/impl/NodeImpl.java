/*
 * Created on 17.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.impl;

import com.jonasreese.sound.sg.soundbus.SoundbusException;

/**
 * <p>
 * This interface is used for internal implementation purposes only. It should not
 * be referenced from outside the <code>com.jonasreese.sound.sg.soundbus.**</code>
 * packages.
 * </p>
 * @author jonas.reese
 */
public interface NodeImpl {
    public void openImpl() throws SoundbusException;
    
    public void closeImpl() throws SoundbusException;
    
    public boolean isOpenImpl();
    
    public void destroyImpl() throws SoundbusException;
}
