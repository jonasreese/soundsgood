/*
 * Created on 18.12.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio;

import java.io.File;

import com.jonasreese.sound.sg.FileHandler;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.audio.AudioDescriptor;

/**
 * <p>
 * This class implements the <code>FileHandler</code> for audio files.
 * </p>
 * @author jonas.reese
 */
public class AudioFileHandler extends FileHandler {

    public AudioFileHandler() {
        super( new String[] { "wav" },
                SgEngine.getInstance().getResourceBundle().getString(
                                "file.audio" ) );
    }
    
    public AudioFileHandler( String[] extensions, String description ) {
        super( extensions, description );
    }

    @Override
    public void openFile( File file ) {
        AudioDescriptor ad = new AudioDescriptor();
        Session session = SgEngine.getInstance().getActiveSession();
        ad.setSession( session );
        ad.setFile( file );
        session.addElement( ad );
    }
}
