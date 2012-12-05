/*
 * Created on 05.01.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import java.io.File;

import com.jonasreese.sound.sg.FileHandler;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SgEngine;

/**
 * @author jonas.reese
 */
public class SoundbusFileHandler extends FileHandler {

    public SoundbusFileHandler() {
        super( new String[] { "sbs.xml", "sbs" },
                SgEngine.getInstance().getResourceBundle().getString(
                                "file.soundbus" ) );
    }
    
    public SoundbusFileHandler( String[] extensions, String description ) {
        super( extensions, description );
    }

    @Override
    public void openFile( File file ) {
        SoundbusDescriptor sd = new SoundbusDescriptor();
        Session session = SgEngine.getInstance().getActiveSession();
        sd.setSession( session );
        sd.setFile( file );
        session.addElement( sd );
    }
}