/*
 * Created on 18.04.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.action.player;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <p>
 * This class implements the player action that stops the player.
 * </p>
 * @author jonas.reese
 */
public class StopAction extends SgAction {

    private static final long serialVersionUID = 1;
    
    private static final Icon STOP_ICON =
        new ResourceLoader( StopAction.class, "resource/stop.gif" ).getAsIcon();

    /**
     * Constructs a new <code>StopAction</code>.
     */
    public StopAction() {
        super( SgEngine.getInstance().getResourceBundle().getString(
                "player.control.stop" ), STOP_ICON );
    }
    
    public void actionPerformed( ActionEvent e ) {
        MidiDescriptor[] midiDescriptors = getMidiDescriptors();
        if (midiDescriptors != null) {
            try {
                for (int i = 0; i < midiDescriptors.length; i++) {
                    midiDescriptors[i].getMidiRecorder().stop();
                }
            } catch (Exception ignored) {}
        }
    }
    
    private MidiDescriptor[] getMidiDescriptors() {
        MidiDescriptor[] midiDescriptors = null;
        Session session = SgEngine.getInstance().getActiveSession();
        if (session != null) {
            SessionElementDescriptor[] sel = session.getSelectedElements();
            boolean b = true;
            for (int i = 0; i < sel.length; i++) {
                if (!(sel[i] instanceof MidiDescriptor)) {
                    b = false;
                }
            }
            if (b) {
                midiDescriptors = new MidiDescriptor[sel.length];
                for (int i = 0; i < midiDescriptors.length; i++) {
                    midiDescriptors[i] = (MidiDescriptor) sel[i];
                }
            }
        }
        return midiDescriptors;
    }
}
