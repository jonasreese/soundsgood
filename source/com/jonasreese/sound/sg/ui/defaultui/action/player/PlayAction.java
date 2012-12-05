/*
 * Created on 18.04.2005
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
 * This class implements the player action that starts the playback.
 * </p>
 * @author jonas.reese
 */
public class PlayAction extends SgAction {

    private static final long serialVersionUID = 1;
    
    private static final Icon PLAY_ICON =
        new ResourceLoader( PlayAction.class, "resource/play.gif" ).getAsIcon();

    /**
     * Constructs a new <code>PlayAction</code>.
     */
    public PlayAction() {
        super( SgEngine.getInstance().getResourceBundle().getString(
                "player.control.play" ), PLAY_ICON );
    }
    
    public void actionPerformed( ActionEvent e ) {
        MidiDescriptor[] midiDescriptors = getMidiDescriptors();
        if (midiDescriptors != null) {
            ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
            try {
                for (int i = 0; i < midiDescriptors.length; i++) {
                    midiDescriptors[i].getMidiRecorder().play();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                    UiToolkit.getMainFrame(),
                    rb.getString( "player.errorOnPlayText" ) + "\n" +
                    ex.getMessage(),
                    rb.getString( "player.errorOnPlay" ),
                    JOptionPane.ERROR_MESSAGE );
            }
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
