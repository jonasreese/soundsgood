/*
 * Created on 20.10.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.vstcontainer;

/**
 * @author jonas.reese
 */
public class MidiKeyName {

    private int flags;
    private String name;
    private int thisKeyNumber;
    private int thisProgramIndex;

    public int getFlags() {
        return flags;
    }
    public void setFlags(int flags) {
        this.flags = flags;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getThisKeyNumber() {
        return thisKeyNumber;
    }
    public void setThisKeyNumber(int thisKeyNumber) {
        this.thisKeyNumber = thisKeyNumber;
    }
    public int getThisProgramIndex() {
        return thisProgramIndex;
    }
    public void setThisProgramIndex(int thisProgramIndex) {
        this.thisProgramIndex = thisProgramIndex;
    }
}
