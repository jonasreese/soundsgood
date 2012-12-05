/*
 * Created on 28.05.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.sound.sampled.AudioFormat;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.soundbus.NetworkAudioInputNode;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.edit.SbNodeStateChangeEdit;
import com.jonasreese.sound.sg.ui.defaultui.AudioFormatPanel;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.util.resource.ResourceLoader;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author jonas.reese
 */
public class PNetworkAudioInputNode extends PSbNode implements PropertyChangeListener {
    private static final long serialVersionUID = 1L;

    static final Image AUDIO_OUT_ICON = new ResourceLoader(
        PNetworkAudioInputNode.class, "/resource/audio_out.gif" ).getAsImage();

    
    public PNetworkAudioInputNode(
            SbEditorComponent editor, NetworkAudioInputNode node, SoundbusDescriptor soundbusDescriptor ) {
        super( editor, node, soundbusDescriptor );
        backgroundPaint = new Color( 0, 205, 200, 100 );
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
        NetworkAudioInputNode n = (NetworkAudioInputNode) node;
        setTitleTextSecondLine( "Port: " + n.getPort() );
    }
    
    public void editNode() {
        try {
            AudioFormatPanel p = new AudioFormatPanel( ((NetworkAudioInputNode) node).getAudioFormat() );
            p.setBorder( new TitledBorder( SgEngine.getInstance().getResourceBundle().getString( "audio.format" ) ) );
            JPanel pp = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
            JLabel portLabel = new JLabel(
                    SgEngine.getInstance().getResourceBundle().getString(
                            "plugin.sbEditor.node.networkAudioInput.edit.port" ) + " " );
            int port = ((NetworkAudioInputNode) node).getPort();
            JSpinner portSpinner = new JSpinner( new SpinnerNumberModel( port, 1, 0xffff, 1 ) );
            portLabel.setLabelFor( portSpinner );
            JPanel portSpinnerPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
            portSpinnerPanel.add( portSpinner );
            pp.add( portLabel );
            pp.add( portSpinnerPanel );
            Object[] message = new Object[] { p, pp };
            int option = JOptionPane.showConfirmDialog(
                    UiToolkit.getMainFrame(), message,
                    SgEngine.getInstance().getResourceBundle().getString(
                            "plugin.sbEditor.node.networkAudioInput.edit.title" ),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
            int newPort = ((Number) ((SpinnerNumberModel) portSpinner.getModel()).getValue()).intValue();
            if (option == JOptionPane.OK_OPTION && (p.hasChanged() || newPort != port)) {
                ChangeNetworkAudioInputEdit edit = new ChangeNetworkAudioInputEdit(
                        getSoundbusDescriptor(), node, p.getAudioFormat(), newPort );
                getSoundbusDescriptor().getUndoManager().addEdit( edit );
                edit.perform();
            }
        } catch (Exception ioex) {
            return;
        }
    }
    
    protected void paintInOutNode( InOutNode n, PPaintContext paintContext ) {
        super.paintInOutNode( n, paintContext );
        PBounds b = n.getBoundsReference();
        paintContext.getGraphics().drawImage( AUDIO_OUT_ICON, (int) b.getX(), (int) b.getY(), null );
    }
    
    static class ChangeNetworkAudioInputEdit extends SbNodeStateChangeEdit {

        private static final long serialVersionUID = 1L;
        
        private AudioFormat audioFormat;
        private int port;
        

        ChangeNetworkAudioInputEdit(
                SoundbusDescriptor soundbusDescriptor, SbNode node, AudioFormat audioFormat, int port ) {
            super( soundbusDescriptor, node,
                    SgEngine.getInstance().getResourceBundle().getString( "edit.changeNetworkAudioInput" ),
                    true );
            this.audioFormat = audioFormat;
            this.port = port;
        }

        @Override
        protected void performImpl() {
            NetworkAudioInputNode n = (NetworkAudioInputNode) getNode();
            AudioFormat af = n.getAudioFormat();
            int p = n.getPort();
            n.setAudioFormat( audioFormat );
            n.setPort( port );
            audioFormat = af;
            port = p;
        }

        @Override
        protected void redoImpl() {
            performImpl();
        }

        @Override
        protected void undoImpl() {
            performImpl();
        }

    }
}
