/*
 * Created on 01.07.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.notedisplay;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.JPanel;

import com.jonasreese.sound.sg.midi.EventDescriptor;
import com.jonasreese.sound.sg.midi.EventMap;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.TrackProxy;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;

/**
 * @author jonas.reese
 */
public class NoteDisplayVi implements ViewInstance, Receiver {

    private MidiDescriptor midiDescriptor;
    private NoteDisplayComponent noteDisplayComponent;
    private JPanel uiObject;
    private View parent;
    
    NoteDisplayVi( View parent, MidiDescriptor midiDescriptor ) {
        noteDisplayComponent = new NoteDisplayComponent();
        noteDisplayComponent.setPreferredSize( new Dimension( 160, 80 ) );
        noteDisplayComponent.setVerticalAlignment( NoteDisplayComponent.CENTER );
        noteDisplayComponent.setHorizontalAlignment( NoteDisplayComponent.CENTER );
        uiObject = new JPanel( new BorderLayout() );
        uiObject.add( noteDisplayComponent );
        this.parent = parent;
        this.midiDescriptor = midiDescriptor;
    }
    
    public Object getUiObject( ViewContainer parentUiObject ) {
        return uiObject;
    }

    public View getView() {
        return parent;
    }

    public void open() {
        System.out.println( "NoteDisplayVi.open()" );
        midiDescriptor.getMidiRecorder().addMidiOutputReceiver( this );
    }

    public void activate() {
        
    }

    public void deactivate() {
        
    }

    public void close() {
        System.out.println( "NoteDisplayVi.close()" );
        midiDescriptor.getMidiRecorder().removeMidiOutputReceiver( this );
    }

    public boolean isSetBoundsAllowed() {
        return false;
    }
    
    private EventMap em;

    public void send( MidiMessage m, long t ) {
        if (em == null) {
            em = TrackProxy.createDefaultEventMap();
        }
        if (m instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) m;
            if (sm.getCommand() == ShortMessage.NOTE_ON &&
                    sm.getData2() > 0) {
                EventDescriptor ed = em.getEventDescriptorFor( sm );
                noteDisplayComponent.setText( ed.getDescription()/* + " (" + sm.getData1() + ")"*/ );
            }
        }
    }
}
