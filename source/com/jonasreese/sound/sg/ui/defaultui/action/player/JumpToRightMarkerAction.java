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
import com.jonasreese.sound.sg.midi.MidiRecorder;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <p>
 * This class implements the player action that 'jumps to the right marker'.
 * </p>
 * @author jonas.reese
 */
public class JumpToRightMarkerAction extends SgAction {

    private static final long serialVersionUID = 1;
    
    private static final Icon END_ICON =
        new ResourceLoader( JumpToRightMarkerAction.class, "resource/end.gif" ).getAsIcon();

    /**
     * Constructs a new <code>JumpToRightMarkerAction</code>.
     */
    public JumpToRightMarkerAction() {
        super( SgEngine.getInstance().getResourceBundle().getString(
                "player.control.jumpToRightMarker" ), END_ICON );
    }
    
    public void actionPerformed( ActionEvent e ) {
        Session session = SgEngine.getInstance().getActiveSession();
        if (session != null) {
            SessionElementDescriptor[] descs = session.getSelectedElements();
            if (descs != null && descs.length == 1 && descs[0] instanceof MidiDescriptor) {
                MidiRecorder midiRecorder = ((MidiDescriptor) descs[0]).getMidiRecorder();
                long tick = midiRecorder.getTickPosition();
                midiRecorder.jumpToRightMarker();
                if (midiRecorder.getTickPosition() == tick) {
                    midiRecorder.jumpToEnd();
                }
            }
        }
    }
}
