/*
 * Created on 26.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.Color;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.sound.midi.MidiDevice;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDeviceDescriptor;
import com.jonasreese.sound.sg.soundbus.MidiOutputNode;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.edit.SbNodeStateChangeEdit;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.util.resource.ResourceLoader;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author jonas.reese
 */
public class PMidiOutputNode extends PSbNode implements PropertyChangeListener {

    private static final long serialVersionUID = 1L;

    static final Image MIDI_IN_ICON = new ResourceLoader(
            PMidiInputNode.class, "/resource/midi_in.gif" ).getAsImage();
    
    public PMidiOutputNode(
            SbEditorComponent editor, MidiOutputNode node, SoundbusDescriptor soundbusDescriptor ) {
        super( editor, node, soundbusDescriptor );
        backgroundPaint = new Color( 200, 20, 50, 100 );
        setTitleTextSecondLine();
    }
    
    @Override
    public void nodeAdded() {
        getSbNode().addPropertyChangeListener( this );
    }
    
    @Override
    public void nodeRemoved() {
        getSbNode().removePropertyChangeListener( this );
    }
    
    public void propertyChange( PropertyChangeEvent evt ) {
        setTitleTextSecondLine();
    }
    
    public void setTitleTextSecondLine() {
        MidiOutputNode mn = (MidiOutputNode) node;
        if (mn.getMidiDevice() != null) {
            MidiDevice.Info devInfo = mn.getMidiDevice().getDeviceInfo();
            setTitleTextSecondLine( devInfo != null ? devInfo.getName() : mn.getMidiDevice().toString() );
        } else {
            setTitleTextSecondLine(
                    SgEngine.getInstance().getResourceBundle().getString( "unassigned" ) );
        }
    }

    public void editNode() {
        try {
            if (getSoundbusDescriptor().getSoundbus().isOpen()) {
                return;
            }
        } catch (Exception ioex) {
            return;
        }
        MidiDeviceDescriptor[] devices = UiToolkit.showSelectMidiOutputDeviceDialog( false );
        if (devices != null) {
            MidiDeviceDescriptor device = null;
            if (devices.length > 0) {
                device = devices[0];
            }
            ChangeNodeEdit edit = new ChangeNodeEdit( getSoundbusDescriptor(), (MidiOutputNode) node, device );
            getSoundbusDescriptor().getUndoManager().addEdit( edit );
            edit.perform();
        }
    }

    protected void paintInOutNode( InOutNode n, PPaintContext paintContext ) {
        super.paintInOutNode( n, paintContext );
        PBounds b = n.getBoundsReference();
        paintContext.getGraphics().drawImage( MIDI_IN_ICON, (int) b.getX(), (int) b.getY(), null );
    }
    
    static class ChangeNodeEdit extends SbNodeStateChangeEdit {

        private static final long serialVersionUID = 1L;

        MidiDeviceDescriptor dev;
        
        ChangeNodeEdit(
                SoundbusDescriptor soundbusDescriptor, MidiOutputNode node, MidiDeviceDescriptor dev ) {
            super( soundbusDescriptor, node,
                    SgEngine.getInstance().getResourceBundle().getString( "edit.changeMidiOutputDevice" ),
                    true );
            this.dev = dev;
        }
        
        
        @Override
        protected void redoImpl() {
            performImpl();
        }

        @Override
        protected void undoImpl() {
            performImpl();
        }

        @Override
        public void performImpl() {
            MidiOutputNode node = (MidiOutputNode) getNode();
            MidiDeviceDescriptor dev = node.getMidiDevice();
            node.setMidiDevice( this.dev );
            this.dev = dev;
        }
    }
}
