/*
 * Created on 31.12.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.aucontainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.jonasreese.sound.aucontainer.AudioUnitDescriptor.AudioUnitType;


/**
 * <p>
 * Please note that this class is <b>not</b> thread safe! External synchronization
 * or single-threaded access required.
 * </p>
 * @author Jonas Reese
 */
public class AUContainer {
    private static final String NATIVE_LIBRARY_NAME = "AUContainer";

    private static AUContainer instance = null;

    private String userDefinedNativeLibPath;
    private boolean available = false;
    private String initFailedMessage;


    /**
     * Gets the singleton <code>AUContainer</code> instance.
     * @return The singleton instance of <code>AUContainer</code>.
     */
    public static AUContainer getInstance() {
        if (instance == null) {
            instance = new AUContainer();
        }
        return instance;
    }
    
    private AUContainer() {
        userDefinedNativeLibPath = null; // default
        initFailedMessage = null;
    }

    
    private void init() {
        if (available) {
            return;
        }
        try {
            if (userDefinedNativeLibPath != null) {
                System.load(
                        new File(
                                userDefinedNativeLibPath,
                                System.mapLibraryName( NATIVE_LIBRARY_NAME ) ).getAbsolutePath() );
            } else {
                System.loadLibrary( NATIVE_LIBRARY_NAME );
            }
            available = true;
        } catch (Throwable t) {
            if (t.getMessage() != null) {
                initFailedMessage = t.getMessage();
            }
            available = false;
        }
    }
    
    
    /**
     * This method can be used to define a custom location for the native library that is
     * required for AU operation. This method shall be called (if it is called) before
     * the method <code>getAllAudioUnits()</code> is called the first time.
     * @param userDefinedNativeLibPath The user defined path where to find the native library.
     * If <code>null</code>, the system default paths will be used.
     */
    public void setUserDefinedNativeLibraryPath( String userDefinedNativeLibPath ) {
        this.userDefinedNativeLibPath = userDefinedNativeLibPath;
    }

    /**
     * Returns the currently set user defined native lib search path.
     * @return The user defined path where to find the native library, or <code>null</code>
     * if the system default paths are used.
     */
    public String getUserDefinedNativeLibraryPath() {
        return userDefinedNativeLibPath;
    }
    
    /**
     * Gets an error message that describes why the initialization failed if it
     * failed and such a message is available.
     * @return The initialization failed message, or <code>null</code>.
     */
    public String getInitFailedMessage() {
        return initFailedMessage;
    }
    
    /**
     * Gets the availability state of the <code>AUContainer</code>.
     * @return <code>true</code> if the AU container is available, <code>false</code> otherwise.
     */
    public boolean isAUContainerAvailable() {
        init();
        return available;
    }

    /**
     * Gets all audio unit descriptors that are available for the given type.
     * @param type The type.
     * @return All available audio unit descriptors.
     */
    public AudioUnitDescriptor[] getAllAudioUnitDescriptors( AudioUnitType type ) {
        init();
        return nGetAllAudioUnits( type.getValue() );
    }
    private native AudioUnitDescriptor[] nGetAllAudioUnits( long type );
    
    /**
     * Gets all audio unit descriptors that are available.
     * @return All available audio unit descriptors.
     */
    public AudioUnitDescriptor[] getAllAudioUnitDescriptors() {
        List<AudioUnitDescriptor> list = new ArrayList<AudioUnitDescriptor>();
        for (AudioUnitType type : AudioUnitType.values()) {
            AudioUnitDescriptor[] descs = getAllAudioUnitDescriptors( type );
            for (AudioUnitDescriptor desc : descs) {
                list.add( desc );
            }
        }
        AudioUnitDescriptor[] result = new AudioUnitDescriptor[list.size()];
        return list.toArray( result );
    }
 
    /**
     * Gets the first audio unit descriptor found for the specified name.
     * @param name The name to search for.
     * @return An <code>AudioUnitDescriptor</code>, or <code>null</code> if none
     * was found.
     */
    public AudioUnitDescriptor getAudioUnitDescriptorByName( String name ) {
        AudioUnitDescriptor[] ds = getAllAudioUnitDescriptors();
        for (AudioUnitDescriptor d : ds) {
            if (name != null && name.equals( d.getName() )) {
                return d;
            }
        }
        return null;
    }
}
