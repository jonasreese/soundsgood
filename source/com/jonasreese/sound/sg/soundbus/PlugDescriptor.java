/*
 * Created on 12.07.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

/**
 * A simple class describing a soundbus node's input or output plug.
 * 
 * @author jonas.reese
 */
public class PlugDescriptor {
    private boolean input;
    private String type;
    private String name;
    private String id;
    
    /**
     * Constructs a new <code>PlugDescriptor</code>.
     * @param input Indicates an input plug if <code>true</code>, otherwise
     * an output plug.
     * @param type The type identifier string.
     * @param name The plug's name.
     * @param id The ID. Optional.
     */
    public PlugDescriptor( boolean input, String type, String name, String id ) {
        this.input = input;
        this.type = type;
        this.name = name;
        this.id = id;
    }

    /**
     * Gets the input flag.
     * @return <code>true</code> if an input is described, <code>false</code>
     * otherwise.
     */
    public boolean isInput() {
        return input;
    }

    /**
     * Gets the plug's name.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the plug's type.
     * @return The type.
     */
    public String getType() {
        return type;
    }
    
    /**
     * Gets the ID.
     * @return The ID. May be <code>null</code>.
     */
    public String getId() {
        return id;
    }
}
