/*
 * Created on 02.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Paint;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.Metronome;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.SoundbusEvent;
import com.jonasreese.sound.sg.soundbus.SoundbusListener;
import com.jonasreese.sound.sg.soundbus.SoundbusNodesConnectionEvent;
import com.jonasreese.sound.sg.soundbus.TempoNode;
import com.jonasreese.sound.sg.soundbus.edit.SbNodeStateChangeEdit;
import com.jonasreese.sound.sg.ui.defaultui.SetTempoPanel;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author jonas.reese
 */
public class PTempoNode extends PSbNode implements SoundbusListener {

    private static final long serialVersionUID = 1L;

    private static final Paint DEFAULT_BACKGROUND_PAINT = new Color( 170, 170, 170, 100 );
    
    private DecimalFormat format;
    private Receiver midiOutputReceiver;
    
    public PTempoNode( SbEditorComponent editor, TempoNode node, SoundbusDescriptor soundbusDescriptor ) {
        super( editor, node, soundbusDescriptor );
        backgroundPaint = DEFAULT_BACKGROUND_PAINT;
        format = new DecimalFormat();
        format.setMaximumFractionDigits( 2 );
        midiOutputReceiver = new Receiver() {
            public void close() {
            }
            public void send( MidiMessage message, long timeStamp ) {
                if (getOpticalClick() && message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                        backgroundPaint = Color.RED;
                        repaint();
                    } else if (sm.getCommand() == ShortMessage.NOTE_OFF || sm.getData2() == 0) {
                        backgroundPaint = DEFAULT_BACKGROUND_PAINT;
                        repaint();
                    }
                }
            }
        };
    }
    
    @Override
    public void nodeAdded() {
        try {
            Soundbus s = getSoundbusDescriptor().getSoundbus();
            s.addSoundbusListener( this );
            tempoChanged( s );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public void nodeRemoved() {
        try {
            getSoundbusDescriptor().getSoundbus().removeSoundbusListener( this );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public boolean getOpticalClick() {
        return Boolean.TRUE.toString().equals( getSbNode().getClientProperty( "opticalClick" ) );
    }
    
    public void setOpticalClick( boolean opticalClick ) {
        if (getOpticalClick() != opticalClick) {
            getSbNode().putClientProperty( "opticalClick", Boolean.toString( opticalClick ) ); 
        }
    }

    @Override
    public void editNode() {
        try {
            Soundbus s = getSoundbusDescriptor().getSoundbus();
            float mpq = MidiToolkit.bpmToMPQ( s.getTempo() );
            final SetTempoPanel tempoPanel = new SetTempoPanel( mpq );
            JCheckBox enableClickCheckBox = new JCheckBox(
                    SgEngine.getInstance().getResourceBundle().getString( "plugin.sbEditor.node.tempo.enableClick" ),
                    ((TempoNode) getSbNode()).isClickEnabled() );
            JCheckBox defaultClickCheckBox = new JCheckBox(
                    SgEngine.getInstance().getResourceBundle().getString( "plugin.sbEditor.node.tempo.defaultClick" ),
                    ((TempoNode) getSbNode()).getPlayDefaultClick() );
            JCheckBox sendControlEventsCheckBox = new JCheckBox(
                    SgEngine.getInstance().getResourceBundle().getString( "plugin.sbEditor.node.tempo.sendControlEvents" ),
                    ((TempoNode) getSbNode()).isSendTempoControlEventsEnabled() );
            JCheckBox opticalClickCheckBox = new JCheckBox(
                    SgEngine.getInstance().getResourceBundle().getString( "plugin.sbEditor.node.tempo.opticalClick" ),
                    getOpticalClick() );
            JComboBox clicksPerTactComboBox = new JComboBox( new String[] { "1", "2", "3", "4", "6", "8" } );
            String oldBpt = Integer.toString( ((TempoNode) getSbNode()).getBeatsPerTact() );
            clicksPerTactComboBox.setSelectedItem( oldBpt );
            JLabel cptLabel = new JLabel( "  " + SgEngine.getInstance().getResourceBundle().getString(
                    "options.midi.click.clicksPerTact" ) );
            JPanel cptPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
            cptPanel.add( clicksPerTactComboBox );
            cptPanel.add( cptLabel );
            
            Object[] message = new Object[] {
                    tempoPanel, enableClickCheckBox, defaultClickCheckBox, sendControlEventsCheckBox, opticalClickCheckBox, cptPanel };
            JOptionPane pane = new JOptionPane(
                message, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null, tempoPanel );
            JDialog d = pane.createDialog(
                    UiToolkit.getMainFrame(), SgEngine.getInstance().getResourceBundle().getString( "midi.tempo.title" ) );
            d.addComponentListener( new ComponentAdapter() {
                public void componentShown(ComponentEvent e) {
                    tempoPanel.requestFocus();
                }
            } );
            d.setVisible( true );
            
            Integer selectedValue = (Integer) pane.getValue();
            if (selectedValue != null && selectedValue.intValue() == JOptionPane.OK_OPTION &&
                    (tempoPanel.hasChanged() ||
                            defaultClickCheckBox.isSelected() != ((TempoNode) getSbNode()).getPlayDefaultClick() ||
                            sendControlEventsCheckBox.isSelected() != ((TempoNode) getSbNode()).isSendTempoControlEventsEnabled() ||
                            opticalClickCheckBox.isSelected() != getOpticalClick() ||
                            enableClickCheckBox.isSelected() != ((TempoNode) getSbNode()).isClickEnabled() ||
                            !oldBpt.equals( clicksPerTactComboBox.getSelectedItem()))) {
                ChangeNodeEdit edit = new ChangeNodeEdit(
                        getSoundbusDescriptor(), (TempoNode) getSbNode(),
                        tempoPanel.getBPMValue(), defaultClickCheckBox.isSelected(),
                        sendControlEventsCheckBox.isSelected(),
                        opticalClickCheckBox.isSelected(), enableClickCheckBox.isSelected(),
                        Integer.parseInt( (String) clicksPerTactComboBox.getSelectedItem() ) );
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
        paintContext.getGraphics().drawImage(
                n.isInputNode() ? PMidiOutputNode.MIDI_IN_ICON :
                    PMidiInputNode.MIDI_OUT_ICON, (int) b.getX(), (int) b.getY(), null );
    }

    public void muteStatusChanged( SoundbusEvent e ) {
    }
    public void nodeAdded( SoundbusEvent e ) {
    }
    public void nodeRemoved( SoundbusEvent e ) {
    }
    public void nodesConnected( SoundbusNodesConnectionEvent e ) {
    }
    public void nodesDisconnected( SoundbusNodesConnectionEvent e ) {
    }
    public void soundbusClosed( SoundbusEvent e ) {
        Metronome metronome = ((TempoNode) getSbNode()).getMetronome();
        if (metronome != null) {
            metronome.removeMidiOutputReceiver( midiOutputReceiver );
        }
    }
    public void soundbusOpened( SoundbusEvent e ) {
        Metronome metronome = ((TempoNode) getSbNode()).getMetronome();
        if (metronome != null) {
            metronome.addMidiOutputReceiver( midiOutputReceiver );
        }
    }
    public void tempoChanged( SoundbusEvent e ) {
        tempoChanged( e.getSoundbus() );
    }
    private void tempoChanged( Soundbus s ) {
        setTitleTextSecondLine( format.format( s.getTempo() ) );
    }
    
    static class ChangeNodeEdit extends SbNodeStateChangeEdit {

        private static final long serialVersionUID = 1L;

        float tempo;
        boolean playDefaultClick;
        boolean sendControlEvents;
        boolean opticalClick;
        boolean enableClickCheckBox;
        int clicksPerTact;
        
        ChangeNodeEdit(
                SoundbusDescriptor soundbusDescriptor, TempoNode node, float tempo,
                boolean playDefaultClick, boolean sendControlEvents, boolean opticalClick, boolean enableClickCheckBox, int clicksPerTact ) {
            super( soundbusDescriptor, node,
                    SgEngine.getInstance().getResourceBundle().getString( "edit.changeTempoEdit" ),
                    false );
            this.tempo = tempo;
            this.playDefaultClick = playDefaultClick;
            this.sendControlEvents = sendControlEvents;
            this.opticalClick = opticalClick;
            this.enableClickCheckBox = enableClickCheckBox;
            this.clicksPerTact = clicksPerTact;
        }

        @Override
        protected void redoImpl() {
            performImpl();
        }

        @Override
        protected void undoImpl() {
            performImpl();
        }

        public boolean getOpticalClick() {
            return Boolean.TRUE.toString().equals( getNode().getClientProperty( "opticalClick" ) );
        }
        
        public void setOpticalClick( boolean opticalClick ) {
            if (getOpticalClick() != opticalClick) {
                getNode().putClientProperty( "opticalClick", Boolean.toString( opticalClick ) ); 
            }
        }
        
        @Override
        public void performImpl() {
            float tempo = getNode().getSoundbus().getTempo();
            boolean playDefaultClick = ((TempoNode) getNode()).getPlayDefaultClick();
            boolean sendControlEvents = ((TempoNode) getNode()).isSendTempoControlEventsEnabled();
            boolean opticalClick = getOpticalClick();
            boolean enableClickCheckBox = ((TempoNode) getNode()).isClickEnabled();
            int clicksPerTact = ((TempoNode) getNode()).getBeatsPerTact();
            getNode().getSoundbus().setTempo( this.tempo );
            ((TempoNode) getNode()).setPlayDefaultClick( this.playDefaultClick );
            ((TempoNode) getNode()).setSendTempoControlEventsEnabled( this.sendControlEvents );
            setOpticalClick( this.opticalClick );
            ((TempoNode) getNode()).setClickEnabled( this.enableClickCheckBox );
            ((TempoNode) getNode()).setBeatsPerTact( this.clicksPerTact );
            this.tempo = tempo;
            this.playDefaultClick = playDefaultClick;
            this.sendControlEvents = sendControlEvents;
            this.opticalClick = opticalClick;
            this.enableClickCheckBox = enableClickCheckBox;
            this.clicksPerTact = clicksPerTact;
        }
    }
}
