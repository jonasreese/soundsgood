/*
 * Created on 31.12.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.aucontainer;

/**
 * Instances of this class describe an AudioUnit and shall be used as
 * static proxies when it is not required to get a "real" instance of
 * <code>AudioUnit</code>. In order to obtain an audio unit, an
 * <code>AudioUnitDescriptor</code> object can be used as identifier.
 * 
 * @author Jonas Reese
 */
public class AudioUnitDescriptor {

    private static int signedToUnsigned( byte value ) {
        return (value < 0 ? 256 + value : value);
    }
    
    public enum AudioUnitType {
        OUTPUT( "auou" ),
        MUSIC_DEVICE( "aumu" ),
        MUSIC_EFFECT( "aumf" ),
    
        FORMAT_CONVERTER( "aufc" ),
        
        EFFECT( "aufx" ),
        
        MIXER( "aumx" ),
    
        PANNER( "aupn" ),
    
        OFFLINE_EFFECT( "auol" ),
    
        GENERATOR( "augn" );

        int value;
        AudioUnitType( String id ) {
            byte[] bs = id.getBytes();
            value = 0;
            for (int i = 0; i < bs.length; i++) {
                value |= (signedToUnsigned( bs[i] ) & 0xff) << ((bs.length - i - 1) * 8);
            }
        }
        
        public int getValue() {
            return value;
        }
    }
    
    private String name;
    private String description;
    private int componentType;
    private int componentSubType;
    private int componentManufacturer;
    private long componentFlags;

    private AudioUnitType type;
    
    /**
     * Constructs an <code>AudioUnitDescriptor</code>.
     * @param name The name.
     * @param description The description.
     */
    public AudioUnitDescriptor(
            String name,
            String description,
            int componentType,
            int componentSubType,
            int componentManufacturer,
            long componentFlags ) {
        this.name = name;
        this.description = description;
        this.componentType = componentType;
        this.componentSubType = componentSubType;
        this.componentManufacturer = componentManufacturer;
        this.componentFlags = componentFlags;
        type = null;
        for (AudioUnitType type : AudioUnitType.values()) {
            if (componentType == type.value) {
                this.type = type;
                break;
            }
        }
    }
    
    /**
     * Gets the audio unit name.
     * @return The audio unit name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the description.
     * @return The description.
     */
    public String getDescription() {
        return description;
    }
    
    public int getComponentType() {
        return componentType;
    }
    
    public AudioUnitType getType() {
        return type;
    }

    public int getComponentSubType() {
        return componentSubType;
    }

    public int getComponentManufacturer() {
        return componentManufacturer;
    }

    public long getComponentFlags() {
        return componentFlags;
    }
    
    public AudioUnit createPlugin() throws AudioUnitNotAvailableException {
        AudioUnit audioUnit = new AudioUnit( this );
        return audioUnit;
    }

    public String toString() {
        return "Audio Unit " + name + " (" + description + "), type " + componentType + ", sub type " + componentSubType + ", manufacturer ID " + componentManufacturer + ", flags " + componentFlags;
    }
}
