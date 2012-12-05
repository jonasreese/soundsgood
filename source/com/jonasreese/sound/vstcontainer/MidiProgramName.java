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
public class MidiProgramName {
    private int flags;
    private int midiBankLsb;
    private int midiBankMsb;
    private int midiProgram;
    private String name;
    private int parentCategoryIndex;
    private int thisProgramIndex;
    public int getFlags() {
        return flags;
    }
    public void setFlags(int flags) {
        this.flags = flags;
    }
    public int getMidiBankLsb() {
        return midiBankLsb;
    }
    public void setMidiBankLsb(int midiBankLsb) {
        this.midiBankLsb = midiBankLsb;
    }
    public int getMidiBankMsb() {
        return midiBankMsb;
    }
    public void setMidiBankMsb(int midiBankMsb) {
        this.midiBankMsb = midiBankMsb;
    }
    public int getMidiProgram() {
        return midiProgram;
    }
    public void setMidiProgram(int midiProgram) {
        this.midiProgram = midiProgram;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getParentCategoryIndex() {
        return parentCategoryIndex;
    }
    public void setParentCategoryIndex(int parentCategoryIndex) {
        this.parentCategoryIndex = parentCategoryIndex;
    }
    public int getThisProgramIndex() {
        return thisProgramIndex;
    }
    public void setThisProgramIndex(int thisProgramIndex) {
        this.thisProgramIndex = thisProgramIndex;
    }
    
    public String toString() {
        return getName() + ", midiBankLsb = " + midiBankLsb + ", midiBankMsb = "
            + midiBankMsb + ", midiProgram = " + midiProgram + ", parentCategoryIndex = "
            + parentCategoryIndex + ", flags = " + flags;
    }
}
