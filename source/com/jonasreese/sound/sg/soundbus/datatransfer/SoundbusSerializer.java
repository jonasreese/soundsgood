/*
 * Created on 19.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.datatransfer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * <p>
 * This class is capable of serializing and deserializing <code>Soundbus</code>
 * elements.
 * </p>
 * @author jonas.reese
 */
public class SoundbusSerializer implements Serializable {
    private static final long serialVersionUID = 1;
    
    private String soundbusDescription;
    
    /**
     * Constructs a new <code>SoundbusSerializer</code>.
     * @param nodeDescription The node description.
     */
    public SoundbusSerializer( String nodeDescription ) {
        this.soundbusDescription = nodeDescription;
    }
    
    /**
     * Gets the soundbus description.
     * @return The soundbus description.
     */
    public String getSoundbusDescription() {
        return soundbusDescription;
    }
    
    private void writeObject( ObjectOutputStream out ) throws IOException {
        out.writeUTF( soundbusDescription );
    }
    
    private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException {
        soundbusDescription = in.readUTF();
    }
}
