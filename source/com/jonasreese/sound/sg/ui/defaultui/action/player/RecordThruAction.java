/*
 * Created on 19.04.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.action.player;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.jonasreese.sound.sg.RecorderException;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <p>
 * This class implements the player action that enables the recording
 * with (MIDI) thru.
 * </p>
 * @author jonas.reese
 */
public class RecordThruAction extends SgAction {

    private static final long serialVersionUID = 1;
    
    private static final Icon REC_THRU_ICON =
        new ResourceLoader( RecordThruAction.class, "resource/record_thru.gif" ).getAsIcon();

    /**
     * Constructs a new <code>RecordThruAction</code>.
     */
    public RecordThruAction() {
        super( SgEngine.getInstance().getResourceBundle().getString(
                "player.control.recordThru" ), REC_THRU_ICON ); 
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
            ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
            try {
                midiDescriptor.getMidiRecorder().setRecordLoopbackEnabled(
                    !midiDescriptor.getMidiRecorder().isRecordLoopbackEnabled() );
            } catch (RecorderException rex) {
                rex.printStackTrace();
                JOptionPane.showMessageDialog(
                    UiToolkit.getMainFrame(),
                    rex.getMessage(),
                    rb.getString( "player.errorOnEnableRecordThru" ),
                    JOptionPane.ERROR_MESSAGE );
            }
        }
    }
}
