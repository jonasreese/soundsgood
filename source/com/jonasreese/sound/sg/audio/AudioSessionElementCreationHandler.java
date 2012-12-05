/*
 * Created on 18.12.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementCreationHandler;
import com.jonasreese.sound.sg.SessionElementType;
import com.jonasreese.sound.sg.SgEngine;

/**
 * @author jonas.reese
 */
public class AudioSessionElementCreationHandler implements SessionElementCreationHandler {

    public void createSessionElement( Session session ) {
        AudioDescriptor ad = new AudioDescriptor();
        ad.setSession( session );
        ad.setFile( null );
        ad.setChanged( true );
        session.addElement( ad );
    }

    public SessionElementType getType() {
        return AudioDescriptor.TYPE;
    }

    public String getDescription() {
        return SgEngine.getInstance().getResourceBundle().getString(
            "menu.file.new.midi.shortDescription" );
    }
}
