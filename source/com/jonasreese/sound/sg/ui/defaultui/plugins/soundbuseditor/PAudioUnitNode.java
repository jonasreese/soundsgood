/*
 * Created on 04.02.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.Color;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;

import com.jonasreese.sound.aucontainer.AudioUnit;
import com.jonasreese.sound.aucontainer.AudioUnitDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.soundbus.AudioUnitNode;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.SbAudioInput;
import com.jonasreese.sound.sg.soundbus.SbAudioOutput;
import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbMidiInput;
import com.jonasreese.sound.sg.soundbus.SbMidiOutput;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.edit.SbNodeStateChangeEdit;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.sound.vstcontainer.VstContainer;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author jonas.reese
 */
public class PAudioUnitNode extends PSbNode implements PropertyChangeListener {
    private static final long serialVersionUID = 1L;

    public PAudioUnitNode( SbEditorComponent editor, AudioUnitNode node, SoundbusDescriptor soundbusDescriptor ) {
        super( editor, node, soundbusDescriptor );
        backgroundPaint = Color.LIGHT_GRAY.brighter();
        setTitleTextSecondLine( node.getAudioUnitName() );
    }
    
    @Override
    public void nodeAdded() {
        getSbNode().addPropertyChangeListener( this );
    }
    
    @Override
    public void nodeRemoved() {
        getSbNode().removePropertyChangeListener( this );
    }

    public void propertyChange( PropertyChangeEvent e ) {
        if ("audioUnit".equals( e.getPropertyName() )) {
            AudioUnitNode vstNode = (AudioUnitNode) getSbNode();
            setTitleTextSecondLine( vstNode.getAudioUnitName() );
            if (vstNode != null) {
                // check if inputs/outputs have been added
                SbOutput[] outs = vstNode.getOutputs();
                SbInput[] ins = vstNode.getInputs();
                synchronized (inputList) {
                    for (int i = 0; i < ins.length; i++) {
                        InOutNode io = getInOutNodeFor( ins[i] );
                        if (io == null) { // new input node
                            System.out.println( "PAudioUnitNode.propertyChange(): New input found: " + ins[i] );
                            addInputHandle( i, ins.length, outs.length, ins[i] );
                        }
                    }
                }
                synchronized (outputList) {
                    for (int i = 0; i < outs.length; i++) {
                        InOutNode io = getInOutNodeFor( outs[i] );
                        if (io == null) { // new output node
                            System.out.println( "PAudioUnitNode.propertyChange(): New output found: " + outs[i] );
                            addOutputHandle( i, ins.length, outs.length, outs[i] );
                        }
                    }
                }
                // check if inputs/outputs have been removed
                synchronized (inputList) {
                    for (int j = 0; j < inputList.size(); j++) {
                        InOutNode io = inputList.get( j );
                        boolean found = false;
                        for (int i = 0; i < ins.length; i++) {
                            if (io.in == ins[i]) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) { // removed input node
                            System.out.println( "PAudioUnitNode.propertyChange(): Removed input found: " + io.in );
                            io.connectedInOut = null;
                            if (io.arrow != null) {
                                io.arrow.getParent().removeChild( io.arrow );
                                io.arrow.setInput( null );
                                io.arrow.setOutput( null );
                                io.arrow = null;
                            }
                            removeChild( io );
                            inputList.remove( j-- );
                        }
                    }
                }
                synchronized (outputList) {
                    for (int j = 0; j < outputList.size(); j++) {
                        InOutNode io = outputList.get( j );
                        boolean found = false;
                        for (int i = 0; i < outs.length; i++) {
                            if (io.out == outs[i]) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) { // removed input node
                            System.out.println( "PAudioUnitNode.propertyChange(): Removed output found: " + io.out );
                            if (io.connectedInOut != null && io.connectedInOut.arrow != null) {
                                io.connectedInOut.arrow.getParent().removeChild( io.arrow );
                                io.connectedInOut.arrow.setInput( null );
                                io.connectedInOut.arrow.setOutput( null );
                                io.connectedInOut.arrow = null;
                                io.connectedInOut = null;
                            }
                            removeChild( io );
                            outputList.remove( j-- );
                        }
                    }
                }
            }
            repaint();
        }
    }
    
    public void editNode() {
        AudioUnitNode auNode = (AudioUnitNode) node;
        if (!VstContainer.getInstance().isVstContainerAvailable()) {
            JOptionPane.showMessageDialog(
                        UiToolkit.getMainFrame(),
                        SgEngine.getInstance().getResourceBundle().getString(
                                "plugin.sbEditor.node.vst.noVst.description" ),
                        SgEngine.getInstance().getResourceBundle().getString(
                                "plugin.sbEditor.node.vst.error.title" ),
                        JOptionPane.ERROR_MESSAGE );
            return;
        }
        try {
            if (getSoundbusDescriptor().getSoundbus().isOpen()) {
                if (auNode.getAudioUnit() == null) {
                    JOptionPane.showMessageDialog(
                                UiToolkit.getMainFrame(),
                                SgEngine.getInstance().getResourceBundle().getString(
                                        "plugin.sbEditor.node.vst.notFound.description", auNode.getAudioUnitName() ),
                                SgEngine.getInstance().getResourceBundle().getString(
                                        "plugin.sbEditor.node.vst.error.title" ),
                                JOptionPane.ERROR_MESSAGE );
                    return;
                }
                if (auNode.getAudioUnit() != null) {
                    auNode.getAudioUnit().openEditWindow();
                }
            } else {
                AudioUnitDescriptor pluginDesc = null;
                if (auNode.getAudioUnit() != null) {
                    pluginDesc = auNode.getAudioUnit().getDescriptor();
                }
                AudioUnitDescriptor desc = UiToolkit.showSelectAudioUnitDialog( pluginDesc );
                if (desc != null && desc != pluginDesc) {
                    AudioUnit audioUnit = desc.createPlugin();
                    SgUndoableEdit edit = new ChangePluginEdit( getSoundbusDescriptor(), auNode, audioUnit );
                    getSoundbusDescriptor().getUndoManager().addEdit( edit );
                    edit.perform();
                }
            }
        } catch (Exception ioex) {
            ioex.printStackTrace();
            return;
        }
    }
    protected void paintInOutNode( InOutNode n, PPaintContext paintContext ) {
        super.paintInOutNode( n, paintContext );
        PBounds b = n.getBoundsReference();
        Image icon = null;
        if (n.in != null) {
            if (n.in instanceof SbMidiInput) {
                icon = PMidiOutputNode.MIDI_IN_ICON;
            } else if (n.in instanceof SbAudioInput) {
                icon = PAudioOutputNode.AUDIO_IN_ICON;
            }
        } else if (n.out != null) {
            if (n.out instanceof SbMidiOutput) {
                icon = PMidiInputNode.MIDI_OUT_ICON;
            } else if (n.out instanceof SbAudioOutput) {
                icon = PAudioInputNode.AUDIO_OUT_ICON;
            }
        }
        paintContext.getGraphics().drawImage( icon, (int) b.getX(), (int) b.getY(), null );
    }
    
    static class ChangePluginEdit extends SbNodeStateChangeEdit {

        private static final long serialVersionUID = 1L;

        AudioUnit audioUnit;
        SbOutput[] connectedIns;
        SbInput[] connectedOuts;

        ChangePluginEdit(
                SoundbusDescriptor soundbusDescriptor, AudioUnitNode node, AudioUnit audioUnit ) {
            super( soundbusDescriptor, node,
                    SgEngine.getInstance().getResourceBundle().getString( "edit.changeAudioUnit" ),
                    true );
            this.audioUnit = audioUnit;
        }
        
        
        @Override
        protected void redoImpl() {
            performImpl();
        }

        @Override
        protected void undoImpl() {
            AudioUnitNode vstNode = (AudioUnitNode) getNode();
            AudioUnit audioUnit = vstNode.getAudioUnit();
            vstNode.setAudioUnit( this.audioUnit );
            this.audioUnit = audioUnit;
            SbInput[] ins = vstNode.getInputs();
            for (int i = 0; i < connectedIns.length; i++) {
                if (connectedIns[i] != null && ins[i].getConnectedOutput() == null) {
                    try {
                        ins[i].connect( connectedIns[i] );
                        connectedIns[i].connect( ins[i] );
                    } catch (CannotConnectException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
            SbOutput[] outs = vstNode.getOutputs();
            for (int i = 0; i < connectedOuts.length; i++) {
                if (connectedOuts[i] != null && outs[i].getConnectedInput() == null) {
                    try {
                        connectedOuts[i].connect( outs[i] );
                        outs[i].connect( connectedOuts[i] );
                    } catch (CannotConnectException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void performImpl() {
            AudioUnitNode vstNode = (AudioUnitNode) getNode();
            SbInput[] ins = vstNode.getInputs();
            connectedIns = new SbOutput[ins.length];
            for (int i = 0; i < ins.length; i++) {
                connectedIns[i] = ins[i].getConnectedOutput();
            }
            SbOutput[] outs = vstNode.getOutputs();
            connectedOuts = new SbInput[outs.length];
            for (int i = 0; i < outs.length; i++) {
                connectedOuts[i] = outs[i].getConnectedInput();
            }
            AudioUnit audioUnit = vstNode.getAudioUnit();
            vstNode.setAudioUnit( this.audioUnit );
            this.audioUnit = audioUnit;
        }
    }
}
