/*
 * Created on 08.01.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementCreationHandler;
import com.jonasreese.sound.sg.SessionElementType;
import com.jonasreese.sound.sg.SgEngine;

/**
 * @author jonas.reese
 */
public class SoundbusSessionElementCreationHandler implements SessionElementCreationHandler {

    public void createSessionElement( Session session ) {
        SoundbusDescriptor sd = new SoundbusDescriptor();
        sd.setSession( session );
        sd.setFile( null );
        sd.setChanged( true );
        session.addElement( sd );
    }

    public String getDescription() {
        return SgEngine.getInstance().getResourceBundle().getString(
                "menu.file.new.soundbus.shortDescription" );
    }

    public SessionElementType getType() {
        return SoundbusDescriptor.TYPE;
    }
}
