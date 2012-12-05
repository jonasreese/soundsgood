/*
 * Created on 28.05.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.Color;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.sound.sampled.Mixer;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.audio.AudioDeviceDescriptor;
import com.jonasreese.sound.sg.soundbus.AudioInputNode;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.edit.SbNodeStateChangeEdit;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.util.resource.ResourceLoader;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author jonas.reese
 */
public class PAudioInputNode extends PSbNode implements PropertyChangeListener {
    private static final long serialVersionUID = 1L;

    static final Image AUDIO_OUT_ICON = new ResourceLoader(
        PAudioInputNode.class, "/resource/audio_out.gif" ).getAsImage();

    
    public PAudioInputNode(
            SbEditorComponent editor, AudioInputNode node, SoundbusDescriptor soundbusDescriptor ) {
        super( editor, node, soundbusDescriptor );
        backgroundPaint = new Color( 100, 205, 200, 100 );
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
        AudioInputNode n = (AudioInputNode) node;
        if (n.getAudioDevice() != null) {
            Mixer.Info devInfo = n.getAudioDevice().getDeviceInfo();
            setTitleTextSecondLine( devInfo != null ? devInfo.getName() : n.getAudioDevice().toString() );
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
        AudioDeviceDescriptor[] devices = UiToolkit.showSelectAudioInputDeviceDialog( false );
        if (devices != null) {
            AudioDeviceDescriptor device = null;
            if (devices.length > 0) {
                device = devices[0];
            }
            ChangeNodeEdit edit = new ChangeNodeEdit( getSoundbusDescriptor(), (AudioInputNode) node, device );
            getSoundbusDescriptor().getUndoManager().addEdit( edit );
            edit.perform();
        }
    }
    
    protected void paintInOutNode( InOutNode n, PPaintContext paintContext ) {
        super.paintInOutNode( n, paintContext );
        PBounds b = n.getBoundsReference();
        paintContext.getGraphics().drawImage( AUDIO_OUT_ICON, (int) b.getX(), (int) b.getY(), null );
    }
    
    static class ChangeNodeEdit extends SbNodeStateChangeEdit {

        private static final long serialVersionUID = 1L;

        AudioDeviceDescriptor dev;
        
        ChangeNodeEdit(
                SoundbusDescriptor soundbusDescriptor, AudioInputNode node, AudioDeviceDescriptor dev ) {
            super( soundbusDescriptor, node,
                    SgEngine.getInstance().getResourceBundle().getString( "edit.changeAudioInputDevice" ),
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
            AudioInputNode node = (AudioInputNode) getNode();
            AudioDeviceDescriptor dev = node.getAudioDevice();
            node.setAudioDevice( this.dev );
            this.dev = dev;
        }
    }
}
