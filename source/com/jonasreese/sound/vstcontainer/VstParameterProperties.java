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
public class VstParameterProperties {
    private float stepFloat;
    private float smallStepFloat;
    private float largeStepFloat;
    private String label;
    private int flags;
    private int minInteger;    
    private int maxInteger;    
    private int stepInteger;   
    private int largeStepInteger;
    private String shortLabel;
    private short displayIndex;
    private short category;
    private short numParametersInCategory;
    private String categoryLabel;

    public VstParameterProperties() {
        this( 0, 0, 0, null, 0, 0, 0, 0, 0, null, (short) 0, (short) 0, (short) 0, null );
    }

    public VstParameterProperties(
            float stepFloat,
            float smallStepFloat,
            float largeStepFloat,
            String label,
            int flags,
            int minInteger,    
            int maxInteger,    
            int stepInteger,   
            int largeStepInteger,
            String shortLabel,
            short displayIndex,
            short category,
            short numParametersInCategory,
            String categoryLabel ) {
        
        this.stepFloat = stepFloat;
        this.smallStepFloat = smallStepFloat;
        this.largeStepFloat = largeStepFloat;
        this.label = label;
        this.flags = flags;
        this.minInteger = minInteger;
        this.maxInteger = maxInteger;
        this.stepInteger = stepInteger;
        this.largeStepInteger = largeStepInteger;
        this.shortLabel = shortLabel;
        this.displayIndex = displayIndex;
        this.category = category;
        this.numParametersInCategory = numParametersInCategory;
        this.categoryLabel = categoryLabel;
    }

    public short getCategory() {
        return category;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public short getDisplayIndex() {
        return displayIndex;
    }

    public int getFlags() {
        return flags;
    }

    public String getLabel() {
        return label;
    }

    public float getLargeStepFloat() {
        return largeStepFloat;
    }

    public int getLargeStepInteger() {
        return largeStepInteger;
    }

    public int getMaxInteger() {
        return maxInteger;
    }

    public int getMinInteger() {
        return minInteger;
    }

    public short getNumParametersInCategory() {
        return numParametersInCategory;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public float getSmallStepFloat() {
        return smallStepFloat;
    }

    public float getStepFloat() {
        return stepFloat;
    }

    public int getStepInteger() {
        return stepInteger;
    }
    
    public String toString() {
        return "stepFloat = " + stepFloat + ", smallStepFloat = " + smallStepFloat +
            ", largeStepFloat = " + largeStepFloat + ", " + ", label = " + label +
            ", flags = " + flags + ", minInteger = " + minInteger + ", maxInteger = " + maxInteger +
            ", stepInteger = " + stepInteger + ", largeStepInteger = " + largeStepInteger +
            ", shortLabel = " + shortLabel + ", displayIndex = " + displayIndex +
            ", category = " + category + ", numParametersInCategory = " + numParametersInCategory +
            ", categoryLabel = " + categoryLabel;
    }
}
