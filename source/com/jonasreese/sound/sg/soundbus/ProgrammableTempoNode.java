/*
 * Created on 05.06.2011
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

/**
 * <p>
 * This interface defines methods provided by a programmable tempo node.
 * A programmable tempo node can control the tempo (BPM) for the whole soundbus using
 * a programming-language-style tempo script.
 * </p>
 * @author jonas.reese
 */
public interface ProgrammableTempoNode extends TempoNode {

    /**
     * Parses a script text and returns a <code>TempoScript</code>
     * object (even if the parsing failed). It contains structural or error information
     * on the tempo script. 
     * @param scriptText The script text. Shall not be <code>null</code>.
     * @return The parsed script text as a <code>TempoScript</code> object.
     */
    public TempoScript parse(String scriptText);
    
    /**
     * Sets the tempo script by a <code>TempoScript</code> object.
     * @param script The script.
     */
    public void setTempoScript(TempoScript script);
    
    /**
     * Gets the tempo script.
     * @return The <code>TempoScript</code> object.
     */
    public TempoScript getTempoScript();
}
