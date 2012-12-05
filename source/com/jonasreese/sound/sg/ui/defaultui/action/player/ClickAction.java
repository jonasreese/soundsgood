/*
 * Created on 09.04.2006
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
 * This class implements the player action that enables/disables the MIDI click.
 * </p>
 * @author jonas.reese
 */
public class ClickAction extends SgAction {

    private static final long serialVersionUID = 1L;

    private static final Icon CLICK_ICON =
        new ResourceLoader( ClickAction.class, "resource/click.gif" ).getAsIcon();

    /**
     * Constructs a new <code>ClickAction</code>.
     */
    public ClickAction() {
        super( SgEngine.getInstance().getResourceBundle().getString(
                "player.control.click" ), CLICK_ICON );
    }

    public void actionPerformed( ActionEvent e )
    {
        MidiDescriptor midiDescriptor = null;
        Session session = SgEngine.getInstance().getActiveSession();
        if (session != null) {
            SessionElementDescriptor[] descs = session.getSelectedElements();
            if (descs != null && descs.length == 1 && descs[0] instanceof MidiDescriptor) {
                midiDescriptor = (MidiDescriptor) descs[0];
            }
        }
        if (midiDescriptor != null) {
            MidiRecorder midiRecorder = midiDescriptor.getMidiRecorder();
            if (midiRecorder.getMetronome().isRunning()) {
                midiRecorder.getMetronome().stop();
            } else {
                midiRecorder.getMetronome().start();
            }
        }
    }
}
