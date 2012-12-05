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
public class PluginNameAndId {

    private String name;
    private long id;
    
    public PluginNameAndId( String name, long id ) {
        this.name = name;
        this.id = id;
    }
    
    public long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
}
