/*
 * Created on 05.06.2011
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.impl;

import com.jonasreese.sound.sg.soundbus.ProgrammableTempoNode;
import com.jonasreese.sound.sg.soundbus.TempoScript;

/**
 * <p>
 * The default <code>ProgrammableTempoNode</code> implementation.
 * </p>
 * @author jonas.reese
 */
public class ProgrammableTempoNodeimpl extends TempoNodeImpl implements
        ProgrammableTempoNode {

    private TempoScript tempoScript;
    
    public ProgrammableTempoNodeimpl(SoundbusImpl parent, String name) {
        super(parent, name);
    }

    public TempoScript parse(String scriptText) {
        return null;
    }

    public void setTempoScript(TempoScript tempoScript) {
        this.tempoScript = tempoScript;
    }

    public TempoScript getTempoScript() {
        return tempoScript;
    }
}
