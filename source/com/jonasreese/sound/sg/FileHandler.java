/*
 * Created on 25.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg;

/**
 * <p>
 * A <code>FileHandler</code> shall be implemented to provide the
 * functionality of opening a certain type of file.
 * </p>
 * @author jonas.reese
 */
public abstract class FileHandler {

    private String[] extensions;
    private String description;
    
    /**
     * Constructs a new <code>FileHandler</code>.
     * @param extensions An array of all accepted file extensions. Typically, such
     * file extensions are not case sensitive. Example: { "midi", "mid" } for MIDI files.
     * @param description The human-readable file type description.
     */
    public FileHandler( String[] extensions, String description ) {
        this.extensions = extensions == null ? new String[0] : extensions;
        this.description = description;
    }

    /**
     * Gets the human-readable file type description.
     * @return The human-readable file type description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets all accepted file extensions.
     * @return An array of all accepted file extensions.
     */
    public String[] getExtensions() {
        return extensions;
    }
    
    /**
     * Overwrite this method to determine if a <code>File</code> with the
     * specified name can be opened by this <code>FileHandler</code>. The
     * default implementation checks the file extension.
     * @param file The file to be checked by name.
     * @return <code>true</code> if file can be opened, <code>false</code>
     * otherwise.
     */
    public boolean canOpenFileName( java.io.File file ) {
        int index = file.getName().lastIndexOf( '.' );
        if (index < 0) {
            return false;
        } else {
            String name = file.getName().toLowerCase();
            for (int i = 0; i < extensions.length; i++) {
                if (name.endsWith( "." + extensions[i].toLowerCase() )) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * This method can be overwritten in order to determine the ability to open
     * this file by it's content. This feature can be supported, but it does not
     * have to be. The default implementation returns <code>canOpenFileName(file)</code>.
     * @param file The file to check.
     * @return <code>true</code> if it can be opened, <code>false</code> otherwise.
     */
    public boolean canOpenFileContent( java.io.File file ) {
        return canOpenFileName( file );
    }
    
    /**
     * Invoked when a file is being opened. Override and implement all
     * functionality that has to be provided...
     * @param file The file that's being opened.
     */
    public abstract void openFile( java.io.File file );
}
