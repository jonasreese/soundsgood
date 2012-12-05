package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.sound.sampled.AudioFormat;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.soundbus.NetworkAudioOutputNode;
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
public class PNetworkAudioOutputNode extends PSbNode implements PropertyChangeListener {
    private static final long serialVersionUID = 1L;
    
    static final Image AUDIO_IN_ICON = new ResourceLoader(
        PMidiInputNode.class, "/resource/audio_in.gif" ).getAsImage();

    public PNetworkAudioOutputNode(
            SbEditorComponent editor, NetworkAudioOutputNode node, SoundbusDescriptor soundbusDescriptor ) {
        super( editor, node, soundbusDescriptor );
        backgroundPaint = new Color( 0, 120, 40, 100 );
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
        NetworkAudioOutputNode n = (NetworkAudioOutputNode) node;
        String d = SgEngine.getInstance().getResourceBundle().getString(
                "plugin.sbEditor.node.networkAudioOutput.destination" );
        String p = SgEngine.getInstance().getResourceBundle().getString(
                "plugin.sbEditor.node.networkAudioOutput.port" );
        setTitleTextSecondLine( d + " " + n.getDestination() + "\n" + p + " " + n.getPort() );
    }

    public void editNode() {

        AudioFormatPanel p = new AudioFormatPanel( ((NetworkAudioOutputNode) node).getAudioFormat() );
        p.setBorder( new TitledBorder( SgEngine.getInstance().getResourceBundle().getString( "audio.format" ) ) );

        EditNodePanel panel = new EditNodePanel( (NetworkAudioOutputNode) node );
        Object[] message = new Object[] { p, panel };
        int option = JOptionPane.showConfirmDialog(
            UiToolkit.getMainFrame(), message,
            SgEngine.getInstance().getResourceBundle().getString(
                "plugin.sbEditor.node.networkAudioOutput.edit.title" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );

        if (option == JOptionPane.OK_OPTION && (p.hasChanged() || panel.hasChanged())) {
            ChangeNodeEdit edit = new ChangeNodeEdit(
                    getSoundbusDescriptor(), (NetworkAudioOutputNode) node,
                    panel.getNewDestination(), panel.getNewPort(), p.getAudioFormat() );
            getSoundbusDescriptor().getUndoManager().addEdit( edit );
            edit.perform();
        }
    }
    
    static class EditNodePanel extends JPanel {
        private static final long serialVersionUID = 1L;

        JTextField destinationTextField;
        JSpinner portSpinner;
        String destination;
        int port;
        
        EditNodePanel( NetworkAudioOutputNode node ) {
            super( new BorderLayout() );
            
            this.destination = node.getDestination();
            this.port = node.getPort();
            destinationTextField = new JTextField( destination, 25 );
            portSpinner = new JSpinner( new SpinnerNumberModel( port, 1, 0xffff, 1 ) );

            ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
            
            JLabel destinationLabel = new JLabel(
                    rb.getString( "plugin.sbEditor.node.networkAudioOutput.edit.destination" ) + " " );
            destinationLabel.setLabelFor( destinationTextField );
            JLabel portLabel = new JLabel(
                    rb.getString( "plugin.sbEditor.node.networkAudioOutput.edit.port" ) + " " );
            portLabel.setLabelFor( portSpinner );
            int width = Math.max( destinationLabel.getPreferredSize().width,
                    portLabel.getPreferredSize().width );
            destinationLabel.setPreferredSize( new Dimension( width, destinationLabel.getPreferredSize().height ) );
            portLabel.setPreferredSize( new Dimension( width, portLabel.getPreferredSize().height ) );
            JPanel destinationPanel = new JPanel( new BorderLayout() );
            destinationPanel.add( BorderLayout.WEST, destinationLabel );
            destinationPanel.add( destinationTextField );
            JPanel portPanel = new JPanel( new BorderLayout() );
            portPanel.add( BorderLayout.WEST, portLabel );
            portPanel.add( portSpinner );
            add( BorderLayout.NORTH, destinationPanel );
            add( portPanel );
        }
        int getNewPort() {
            return ((Number) ((SpinnerNumberModel) portSpinner.getModel()).getValue()).intValue();
        }
        String getNewDestination() {
            return destinationTextField.getText();
        }
        boolean hasChanged() {
            int port = getNewPort();
            String destination = getNewDestination();
            return port != this.port || !destination.equals( this.destination );
        }
    }

    protected void paintInOutNode( InOutNode n, PPaintContext paintContext ) {
        super.paintInOutNode( n, paintContext );
        PBounds b = n.getBoundsReference();
        paintContext.getGraphics().drawImage( AUDIO_IN_ICON, (int) b.getX(), (int) b.getY(), null );
    }
    
    static class ChangeNodeEdit extends SbNodeStateChangeEdit {

        private static final long serialVersionUID = 1L;

        String destination;
        int port;
        AudioFormat format;
        
        ChangeNodeEdit(
                SoundbusDescriptor soundbusDescriptor, NetworkAudioOutputNode node, String destination, int port, AudioFormat format ) {
            super( soundbusDescriptor, node,
                    SgEngine.getInstance().getResourceBundle().getString( "edit.changeNetworkAudioOutput" ),
                    true );
            this.destination = destination;
            this.port = port;
            this.format = format;
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
            NetworkAudioOutputNode node = (NetworkAudioOutputNode) getNode();
            int port = node.getPort();
            AudioFormat format = node.getAudioFormat();
            String destination = node.getDestination();
            node.setDestination( this.destination );
            node.setPort( this.port );
            node.setAudioFormat( this.format );
            this.port = port;
            this.destination = destination;
            this.format = format;
        }
    }
}
