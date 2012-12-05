/*
 * Created on 08.01.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementCreationHandler;
import com.jonasreese.sound.sg.SessionElementType;
import com.jonasreese.sound.sg.SgEngine;

/**
 * @author jonas.reese
 */
public class MidiSessionElementCreationHandler implements SessionElementCreationHandler {

    public void createSessionElement( Session session ) {
        MidiDescriptor md = new MidiDescriptor();
        md.setSession( session );
        md.setFile( null );
        md.setChanged( true );
        session.addElement( md );
    }

    public SessionElementType getType() {
        return MidiDescriptor.TYPE;
    }

    public String getDescription() {
        return SgEngine.getInstance().getResourceBundle().getString(
            "menu.file.new.midi.shortDescription" );
    }
}
