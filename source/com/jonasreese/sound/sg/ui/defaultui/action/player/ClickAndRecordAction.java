/*
 * Created on 01.09.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.action.player;


import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <p>
 * This class implements the player action that starts the recording.
 * </p>
 * @author jonas.reese
 */
public class ClickAndRecordAction extends SgAction {
    
    private static final long serialVersionUID = 1;
    
    private static final Icon REC_ICON =
        new ResourceLoader( ClickAndRecordAction.class, "resource/click_record.gif" ).getAsIcon();
    
    /**
     * Constructs a new <code>ClickAndRecordAction</code>.
     */
    public ClickAndRecordAction() {
        super( SgEngine.getInstance().getResourceBundle().getString(
                "player.control.clickAndRecord" ), REC_ICON );
    }
    
    
    public void actionPerformed( ActionEvent e ) {
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
                midiDescriptor.getMidiRecorder().clickAndRecord();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                    UiToolkit.getMainFrame(),
                    rb.getString( "player.errorOnRecordText" ) + "\n" +
                    ex.getMessage(),
                    rb.getString( "player.errorOnRecord" ),
                    JOptionPane.ERROR_MESSAGE );
            }
        }
    }
}
