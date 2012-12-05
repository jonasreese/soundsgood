/*
 * Created on 02.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.soundbus.MidiSamplerNode;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.MidiSamplerNode.RetriggerMode;
import com.jonasreese.sound.sg.soundbus.MidiSamplerNode.SampleMode;
import com.jonasreese.sound.sg.soundbus.edit.SbNodeStateChangeEdit;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author jonas.reese
 */
public class PMidiSamplerNode extends PSbNode implements PropertyChangeListener {

    private static final long serialVersionUID = 1L;
    
    public PMidiSamplerNode(
            SbEditorComponent editor, MidiSamplerNode node, SoundbusDescriptor soundbusDescriptor ) {
        super( editor, node, soundbusDescriptor );
        backgroundPaint = new Color( 200, 85, 200, 100 );
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
    
    private Object[] showOptionsDialog( Session session ) {
        JList list = new JList( session.getMidiElements() );
        list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION  );
        list.setSelectedValue( ((MidiSamplerNode) getSbNode()).getMidiDescriptor(), true );
        SampleMode sm = ((MidiSamplerNode) getSbNode()).getSampleMode();
        JRadioButton leftToRight = new JRadioButton(
                SgEngine.getInstance().getResourceBundle().getString(
                        "plugin.sbEditor.node.midiSampler.play.leftToRightMarker" ),
                        sm == SampleMode.LEFT_TO_RIGHT_MARKER );
        JRadioButton startToEnd = new JRadioButton(
                SgEngine.getInstance().getResourceBundle().getString(
                        "plugin.sbEditor.node.midiSampler.play.startToEnd" ),
                        sm == SampleMode.START_TO_END );
        JRadioButton loop = new JRadioButton(
                SgEngine.getInstance().getResourceBundle().getString(
                        "plugin.sbEditor.node.midiSampler.play.loopLeftToRightMarker" ),
                        sm == SampleMode.LEFT_TO_RIGHT_MARKER_LOOP );
        ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        JComboBox retriggerModeComboBox = new JComboBox( new Object[] {
                rb.getString( "plugin.sbEditor.node.midiSampler.retrigger.restart" ),
                rb.getString( "plugin.sbEditor.node.midiSampler.retrigger.stopOnNextEvent" ),
                rb.getString( "plugin.sbEditor.node.midiSampler.retrigger.stopOnNoteOff" ),
                rb.getString( "plugin.sbEditor.node.midiSampler.retrigger.continue" )
        } );
        JCheckBox enableDefaultOutputsCheckBox = new JCheckBox(
                rb.getString( "plugin.sbEditor.node.midiSampler.enableDefaultOutputs" ),
                ((MidiSamplerNode) getSbNode()).getDefaultOutputsEnabled() );
        int index = 0;
        switch (((MidiSamplerNode) getSbNode()).getRetriggerMode()) {
        case STOP: index = 1; break;
        case STOP_ON_NOTE_OFF: index = 2; break;
        case CONTINUE: index = 3; break;
        }
        retriggerModeComboBox.setSelectedIndex( index );
        ButtonGroup bgr = new ButtonGroup();
        bgr.add( leftToRight );
        bgr.add( startToEnd );
        bgr.add( loop );
        Object[] message = new Object[] {
                new JScrollPane( list ),
                leftToRight,
                startToEnd,
                loop,
                retriggerModeComboBox,
                enableDefaultOutputsCheckBox };
        int option = JOptionPane.showConfirmDialog(
            UiToolkit.getMainFrame(), message,
            SgEngine.getInstance().getResourceBundle().getString(
                "plugin.sbEditor.node.midiSampler.edit" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION) {
            if (leftToRight.isSelected()) {
                sm = SampleMode.LEFT_TO_RIGHT_MARKER;
            } else if (startToEnd.isSelected()) {
                sm = SampleMode.START_TO_END;
            } else if (loop.isSelected()) {
                sm = SampleMode.LEFT_TO_RIGHT_MARKER_LOOP;
            }
            MidiSamplerNode.RetriggerMode rm = MidiSamplerNode.RetriggerMode.RESTART;
            switch (retriggerModeComboBox.getSelectedIndex()) {
            case 1: rm = MidiSamplerNode.RetriggerMode.STOP; break;
            case 2: rm = MidiSamplerNode.RetriggerMode.STOP_ON_NOTE_OFF; break;
            case 3: rm = MidiSamplerNode.RetriggerMode.CONTINUE; break;
            }
            return new Object[] {
                    (MidiDescriptor) list.getSelectedValue(),
                    sm,
                    rm,
                    (enableDefaultOutputsCheckBox.isSelected() ? Boolean.TRUE : Boolean.FALSE)
            };
        }
        return null;
    }
    
    public void editNode() {
        try {
            if (getSoundbusDescriptor().getSoundbus().isOpen()) {
                return;
            }
        } catch (Exception ioex) {
            return;
        }
        Object[] o = showOptionsDialog( getSoundbusDescriptor().getSession() );
        if (o != null) {
            MidiDescriptor midiDesc = (MidiDescriptor) o[0];
            SampleMode sm = (SampleMode) o[1];
            RetriggerMode retriggerMode = (RetriggerMode) o[2];
            boolean enableDefaultOutputs = ((Boolean) o[3]).booleanValue();
            MidiSamplerNode msn = (MidiSamplerNode) getSbNode();
            if ((midiDesc != null && msn.getMidiDescriptor() != midiDesc) ||
                    (sm != msn.getSampleMode()) ||
                    (retriggerMode != msn.getRetriggerMode()) ||
                    (enableDefaultOutputs != msn.getDefaultOutputsEnabled())) {
                SgUndoableEdit edit = new ChangeSamplerEdit(
                        getSoundbusDescriptor(), msn, midiDesc, sm, retriggerMode, enableDefaultOutputs );
                getSoundbusDescriptor().getUndoManager().addEdit( edit );
                edit.perform();
            }
        }
    }
    
    public void setTitleTextSecondLine() {
        MidiSamplerNode sn = (MidiSamplerNode) node;
        if (sn.getMidiDescriptor() != null) {
            setTitleTextSecondLine( sn.getMidiDescriptor().getName() );
        } else {
            setTitleTextSecondLine( "" );
        }
    }

    protected void paintInOutNode( InOutNode n, PPaintContext paintContext ) {
        super.paintInOutNode( n, paintContext );
        PBounds b = n.getBoundsReference();
        paintContext.getGraphics().drawImage(
                n.isInputNode() ? PMidiOutputNode.MIDI_IN_ICON :
                    PMidiInputNode.MIDI_OUT_ICON, (int) b.getX(), (int) b.getY(), null );
    }
    
    static class ChangeSamplerEdit extends SbNodeStateChangeEdit {

        private static final long serialVersionUID = 1L;

        MidiDescriptor desc;
        SampleMode sm;
        RetriggerMode retriggerMode;
        boolean enableDefaultOutputs;
        
        ChangeSamplerEdit(
                SoundbusDescriptor soundbusDescriptor, MidiSamplerNode node,
                MidiDescriptor desc, SampleMode sm, RetriggerMode retriggerMode,
                boolean enableDefaultOutputs ) {
            super( soundbusDescriptor, node,
                    SgEngine.getInstance().getResourceBundle().getString( "edit.changeMidiSampler" ),
                    true );
            this.desc = desc;
            this.sm = sm;
            this.retriggerMode = retriggerMode;
            this.enableDefaultOutputs = enableDefaultOutputs;
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
            MidiSamplerNode node = (MidiSamplerNode) getNode();
            MidiDescriptor desc = node.getMidiDescriptor();
            SampleMode sm = node.getSampleMode();
            RetriggerMode retriggerMode = node.getRetriggerMode();
            boolean enableDefaultOutputs = node.getDefaultOutputsEnabled();
            node.setMidiDescriptor( this.desc );
            node.setSampleMode( this.sm );
            node.setRetriggerMode( this.retriggerMode );
            node.setDefaultOutputsEnabled( this.enableDefaultOutputs );
            this.desc = desc;
            this.sm = sm;
            this.retriggerMode = retriggerMode;
            this.enableDefaultOutputs = enableDefaultOutputs;
        }
    }
}
