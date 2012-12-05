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

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <p>
 * This class implements the player action that starts the playback from
 * the left to the right marker.
 * </p>
 * @author jonas.reese
 */
public class PlayFromLeftToRightMarkerAction extends SgAction {

    private static final long serialVersionUID = 1;
    
    private boolean loop;
    
    private static final Icon createIcon( boolean loop ) {
        if (loop) {
            return new ResourceLoader(
                    PlayFromLeftToRightMarkerAction.class,
                    "resource/loop_left_to_right_marker.gif" ).getAsIcon();
        } else {
            return new ResourceLoader(
                    PlayFromLeftToRightMarkerAction.class,
                    "resource/play_left_to_right_marker.gif" ).getAsIcon();
        }
    }

    /**
     * Constructs a new <code>PlayFromLeftToRightMarkerAction</code> without loop.
     */
    public PlayFromLeftToRightMarkerAction() {
        this( false );
    }
    
    /**
     * Constructs a new <code>PlayFromLeftToRightMarkerAction</code>.
     * @param loop If set to <code>true</code>, indicates that the sequence shall be
     * looped from left to right marker instead of playing it only once.
     */
    public PlayFromLeftToRightMarkerAction( boolean loop ) {
        super( SgEngine.getInstance().getResourceBundle().getString(
                (loop ? "player.control.loopFromLeftToRightMarker" :
                    "player.control.playFromLeftToRightMarker" ) ), createIcon( loop ) );
        this.loop = loop;
    }
    
    public void actionPerformed( ActionEvent e ) {
        MidiDescriptor[] midiDescriptors = getMidiDescriptors();
        if (midiDescriptors != null) {
            ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
            try {
                if (loop) {
                    for (int i = 0; i < midiDescriptors.length; i++) {
                        midiDescriptors[i].getMidiRecorder().loopFromLeftToRightMarker();
                    }
                } else {
                    for (int i = 0; i < midiDescriptors.length; i++) {
                        midiDescriptors[i].getMidiRecorder().playFromLeftToRightMarker();
                    }
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
