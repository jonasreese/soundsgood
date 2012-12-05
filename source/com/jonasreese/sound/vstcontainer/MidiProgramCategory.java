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
public class MidiProgramCategory {
    private int flags;
    private String name;
    private int parentCategoryIndex;
    private int thisCategoryIndex;
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
    public int getParentCategoryIndex() {
        return parentCategoryIndex;
    }
    public void setParentCategoryIndex(int parentCategoryIndex) {
        this.parentCategoryIndex = parentCategoryIndex;
    }
    public int getThisCategoryIndex() {
        return thisCategoryIndex;
    }
    public void setThisCategoryIndex(int thisCategoryIndex) {
        this.thisCategoryIndex = thisCategoryIndex;
    }
    
    
}
