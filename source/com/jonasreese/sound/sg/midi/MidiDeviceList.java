/*
 * Created on 23.05.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

/**
 * <p>
 * This class encapsulates an ordered list of <code>MidiDeviceDescriptor</code>s.
 * </p>
 * @author jonas.reese
 */
public class MidiDeviceList {
    
    private MidiDeviceDescriptor[] descriptors;
    
    /**
     * Constructs a new <code>MidiDeviceList</code>.
     * @param descriptors The <code>MidiDeviceDescriptor</code> objects.
     */
    public MidiDeviceList( MidiDeviceDescriptor[] descriptors ) {
        this.descriptors = descriptors;
    }
    
    /**
     * Gets the <code>MidiDeviceDescriptor</code> at the specified index.
     * @param index The index. The first index starts with 0.
     * @return The <code>MidiDeviceDescriptor</code>, or <code>null</code> if
     * <code>index</code> is out of range.
     */
    public MidiDeviceDescriptor getMidiDeviceDescriptor( int index ) {
        if (index < 0 || index >= descriptors.length) {
            return null;
        }
        return descriptors[index];
    }
    
    /**
     * Gets the logical index of the given <code>MidiDeviceDescriptor</code>
     * within this <code>MidiDeviceList</code>, or <code>-1</code> if the
     * described device is not part of this list.
     * @param deviceDescriptor The descriptor to get the logical index for.
     * @return An index.
     */
    public int getDeviceIndex( MidiDeviceDescriptor deviceDescriptor ) {
        if (deviceDescriptor == null) {
            return -1;
        }
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].equals( deviceDescriptor )) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Gets the number of <code>MidiDeviceDescriptor</code>s.
     * @return The descriptors count.
     */
    public int getCount() {
        return descriptors.length;
    }
    
    /**
     * Gets all contained <code>MidiDeviceDescriptor</code>s as an array.
     * @return The array.
     */
    public MidiDeviceDescriptor[] getDeviceDescriptors() {
        return descriptors;
    }
    
    /**
     * Gets a <code>MidiDeviceDescriptor</code> by it's ID string.
     * @param idString The ID string to search for. Shall not be <code>null</code>
     * @return A <code>MidiDeviceDescriptor</code>, or <code>null</code> if none
     * with the given ID string was found.
     */
    public MidiDeviceDescriptor getDescriptorById( String idString ) {
        return getDescriptorById(new MidiDeviceId(idString));
    }
    
    /**
     * Gets a <code>MidiDeviceDescriptor</code> by it's ID string.
     * @param id The ID to search for. Shall not be <code>null</code>
     * @return A <code>MidiDeviceDescriptor</code>, or <code>null</code> if none
     * with the given ID was found.
     */
    public MidiDeviceDescriptor getDescriptorById( MidiDeviceId id ) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < descriptors.length; i++) {
            if (id.equals( descriptors[i].getId() )) {
                return descriptors[i];
            }
        }
        return null;
    }
}
