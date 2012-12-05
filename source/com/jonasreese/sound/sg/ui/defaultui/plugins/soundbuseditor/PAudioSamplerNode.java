/*
 * Created on 18.12.2007
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
import com.jonasreese.sound.sg.audio.AudioDescriptor;
import com.jonasreese.sound.sg.soundbus.AudioSamplerNode;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.AudioSamplerNode.RetriggerMode;
import com.jonasreese.sound.sg.soundbus.AudioSamplerNode.SampleMode;
import com.jonasreese.sound.sg.soundbus.edit.SbNodeStateChangeEdit;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author jonas.reese
 */
public class PAudioSamplerNode extends PSbNode implements PropertyChangeListener {

    private static final long serialVersionUID = 1L;
    
    public PAudioSamplerNode(
            SbEditorComponent editor, AudioSamplerNode node, SoundbusDescriptor soundbusDescriptor ) {
        super( editor, node, soundbusDescriptor );
        backgroundPaint = new Color( 0, 0, 70, 70 );
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
        JList list = new JList( session.getAudioElements() );
        list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION  );
        list.setSelectedValue( ((AudioSamplerNode) getSbNode()).getAudioDescriptor(), true );
        SampleMode sm = ((AudioSamplerNode) getSbNode()).getSampleMode();
        JRadioButton leftToRight = new JRadioButton(
                SgEngine.getInstance().getResourceBundle().getString(
                        "plugin.sbEditor.node.audioSampler.play.startToEnd" ),
                        sm == SampleMode.START_TO_END );
        ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        JComboBox retriggerModeComboBox = new JComboBox( new Object[] {
                rb.getString( "plugin.sbEditor.node.audioSampler.retrigger.restart" ),
                rb.getString( "plugin.sbEditor.node.audioSampler.retrigger.stopOnNextEvent" ),
                rb.getString( "plugin.sbEditor.node.audioSampler.retrigger.stopOnNoteOff" ),
                rb.getString( "plugin.sbEditor.node.audioSampler.retrigger.continue" )
        } );
        JCheckBox enableDefaultOutputsCheckBox = new JCheckBox(
                rb.getString( "plugin.sbEditor.node.audioSampler.enableDefaultOutputs" ),
                ((AudioSamplerNode) getSbNode()).getDefaultOutputsEnabled() );
        int index = 0;
        switch (((AudioSamplerNode) getSbNode()).getRetriggerMode()) {
        case STOP: index = 1; break;
        case STOP_ON_NOTE_OFF: index = 2; break;
        case CONTINUE: index = 3; break;
        }
        retriggerModeComboBox.setSelectedIndex( index );
        ButtonGroup bgr = new ButtonGroup();
        bgr.add( leftToRight );
        Object[] message = new Object[] {
                new JScrollPane( list ),
                leftToRight,
                retriggerModeComboBox,
                enableDefaultOutputsCheckBox };
        int option = JOptionPane.showConfirmDialog(
            UiToolkit.getMainFrame(), message,
            SgEngine.getInstance().getResourceBundle().getString(
                "plugin.sbEditor.node.audioSampler.edit" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );
        if (option == JOptionPane.OK_OPTION) {
            if (leftToRight.isSelected()) {
                sm = SampleMode.START_TO_END;
            }
            AudioSamplerNode.RetriggerMode rm = AudioSamplerNode.RetriggerMode.RESTART;
            switch (retriggerModeComboBox.getSelectedIndex()) {
            case 1: rm = AudioSamplerNode.RetriggerMode.STOP; break;
            case 2: rm = AudioSamplerNode.RetriggerMode.STOP_ON_NOTE_OFF; break;
            case 3: rm = AudioSamplerNode.RetriggerMode.CONTINUE; break;
            }
            return new Object[] {
                    (AudioDescriptor) list.getSelectedValue(),
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
            AudioDescriptor audioDesc = (AudioDescriptor) o[0];
            SampleMode sm = (SampleMode) o[1];
            RetriggerMode retriggerMode = (RetriggerMode) o[2];
            boolean enableDefaultOutputs = ((Boolean) o[3]).booleanValue();
            AudioSamplerNode msn = (AudioSamplerNode) getSbNode();
            if ((audioDesc != null && msn.getAudioDescriptor() != audioDesc) ||
                    (sm != msn.getSampleMode()) ||
                    (retriggerMode != msn.getRetriggerMode()) ||
                    (enableDefaultOutputs != msn.getDefaultOutputsEnabled())) {
                SgUndoableEdit edit = new ChangeSamplerEdit(
                        getSoundbusDescriptor(), msn, audioDesc, sm, retriggerMode, enableDefaultOutputs );
                getSoundbusDescriptor().getUndoManager().addEdit( edit );
                edit.perform();
            }
        }
    }
    
    public void setTitleTextSecondLine() {
        AudioSamplerNode sn = (AudioSamplerNode) node;
        if (sn.getAudioDescriptor() != null) {
            setTitleTextSecondLine( sn.getAudioDescriptor().getName() );
        } else {
            setTitleTextSecondLine( "" );
        }
    }

    protected void paintInOutNode( InOutNode n, PPaintContext paintContext ) {
        super.paintInOutNode( n, paintContext );
        PBounds b = n.getBoundsReference();
        paintContext.getGraphics().drawImage(
                n.isInputNode() ? PMidiOutputNode.MIDI_IN_ICON :
                    PAudioInputNode.AUDIO_OUT_ICON, (int) b.getX(), (int) b.getY(), null );
    }
    
    static class ChangeSamplerEdit extends SbNodeStateChangeEdit {

        private static final long serialVersionUID = 1L;

        AudioDescriptor desc;
        SampleMode sm;
        RetriggerMode retriggerMode;
        boolean enableDefaultOutputs;
        
        ChangeSamplerEdit(
                SoundbusDescriptor soundbusDescriptor, AudioSamplerNode node,
                AudioDescriptor desc, SampleMode sm, RetriggerMode retriggerMode,
                boolean enableDefaultOutputs ) {
            super( soundbusDescriptor, node,
                    SgEngine.getInstance().getResourceBundle().getString( "edit.changeAudioSampler" ),
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
            AudioSamplerNode node = (AudioSamplerNode) getNode();
            AudioDescriptor desc = node.getAudioDescriptor();
            SampleMode sm = node.getSampleMode();
            RetriggerMode retriggerMode = node.getRetriggerMode();
            boolean enableDefaultOutputs = node.getDefaultOutputsEnabled();
            node.setAudioDescriptor( this.desc );
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
