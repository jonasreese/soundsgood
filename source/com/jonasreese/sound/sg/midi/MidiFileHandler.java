/*
 * Created on 05.01.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import java.io.File;

import com.jonasreese.sound.sg.FileHandler;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SgEngine;

/**
 * <p>
 * This class implements the <code>FileHandler</code> for MIDI files.
 * </p>
 * @author jonas.reese
 */
public class MidiFileHandler extends FileHandler {

    public MidiFileHandler() {
        super( new String[] { "mid", "midi" },
                SgEngine.getInstance().getResourceBundle().getString(
                                "file.midi" ) );
    }
    
    public MidiFileHandler( String[] extensions, String description ) {
        super( extensions, description );
    }

    @Override
    public void openFile( File file ) {
        MidiDescriptor md = new MidiDescriptor();
        Session session = SgEngine.getInstance().getActiveSession();
        md.setSession( session );
        md.setFile( file );
        session.addElement( md );
    }
}
