/*
 * Created on 23.05.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio;


/**
 * <p>
 * This class encapsulates an ordered list of <code>AudioDeviceDescriptor</code>s.
 * </p>
 * @author jonas.reese
 */
public class AudioDeviceList {
    
    private AudioDeviceDescriptor[] descriptors;
    
    /**
     * Constructs a new <code>AudioDeviceList</code>.
     * @param descriptors The <code>AudioDeviceDescriptor</code> objects.
     */
    public AudioDeviceList( AudioDeviceDescriptor[] descriptors ) {
        this.descriptors = descriptors;
    }
    
    /**
     * Gets the <code>AudioDeviceDescriptor</code> at the specified index.
     * @param index The index. The first index starts with 0.
     * @return The <code>AudioDeviceDescriptor</code>.
     */
    public AudioDeviceDescriptor getAudioDeviceDescriptor( int index ) {
        return descriptors[index];
    }
    
    /**
     * Gets the logical index of the given <code>AudioDeviceDescriptor</code>
     * within this <code>AudioDeviceList</code>, or <code>-1</code> if the
     * described device is not part of this list.
     * @param deviceDescriptor The descriptor to get the logical index for.
     * @return An index.
     */
    public int getDeviceIndex( AudioDeviceDescriptor deviceDescriptor ) {
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].equals( deviceDescriptor )) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Gets the number of <code>AudioDeviceDescriptor</code>s.
     * @return The descriptors count.
     */
    public int getCount() {
        return descriptors.length;
    }
    
    /**
     * Gets all contained <code>AudioDeviceDescriptor</code>s as an array.
     * @return The array.
     */
    public AudioDeviceDescriptor[] getDeviceDescriptors() {
        return descriptors;
    }
    
    /**
     * Gets an <code>AudioDeviceDescriptor</code> by it's ID string.
     * @param idString The ID string to search for. Shall not be <code>null</code>
     * @return A <code>AudioDeviceDescriptor</code>, or <code>null</code> if none
     * with the given ID string was found.
     */
    public AudioDeviceDescriptor getDescriptorById( String idString ) {
        if (idString == null) {
            return null;
        }
        for (int i = 0; i < descriptors.length; i++) {
            if (idString.equals( descriptors[i].getIdString() )) {
                return descriptors[i];
            }
        }
        return null;
    }
}
