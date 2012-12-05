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

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.SbAudioInput;
import com.jonasreese.sound.sg.soundbus.SbAudioOutput;
import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbMidiInput;
import com.jonasreese.sound.sg.soundbus.SbMidiOutput;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.VstNode;
import com.jonasreese.sound.sg.soundbus.edit.SbNodeStateChangeEdit;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.sound.vstcontainer.VstContainer;
import com.jonasreese.sound.vstcontainer.VstPlugin;
import com.jonasreese.sound.vstcontainer.VstPluginDescriptor;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author jonas.reese
 */
public class PVstNode extends PSbNode implements PropertyChangeListener {
    private static final long serialVersionUID = 1L;

    public PVstNode( SbEditorComponent editor, VstNode node, SoundbusDescriptor soundbusDescriptor ) {
        super( editor, node, soundbusDescriptor );
        backgroundPaint = Color.LIGHT_GRAY.brighter();
        setTitleTextSecondLine( node.getVstPluginName() );
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
        if ("vstPlugin".equals( e.getPropertyName() )) {
            VstNode vstNode = (VstNode) getSbNode();
            setTitleTextSecondLine( vstNode.getVstPluginName() );
            if (vstNode != null) {
                // check if inputs/outputs have been added
                SbOutput[] outs = vstNode.getOutputs();
                SbInput[] ins = vstNode.getInputs();
                synchronized (inputList) {
                    for (int i = 0; i < ins.length; i++) {
                        InOutNode io = getInOutNodeFor( ins[i] );
                        if (io == null) { // new input node
                            System.out.println( "PVstNode.propertyChange(): New input found: " + ins[i] );
                            addInputHandle( i, ins.length, outs.length, ins[i] );
                        }
                    }
                }
                synchronized (outputList) {
                    for (int i = 0; i < outs.length; i++) {
                        InOutNode io = getInOutNodeFor( outs[i] );
                        if (io == null) { // new output node
                            System.out.println( "PVstNode.propertyChange(): New output found: " + outs[i] );
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
                            System.out.println( "PVstNode.propertyChange(): Removed input found: " + io.in );
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
                            System.out.println( "PVstNode.propertyChange(): Removed output found: " + io.out );
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
        VstNode vstNode = (VstNode) node;
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
                if (vstNode.getVstPlugin() == null) {
                    JOptionPane.showMessageDialog(
                                UiToolkit.getMainFrame(),
                                SgEngine.getInstance().getResourceBundle().getString(
                                        "plugin.sbEditor.node.vst.notFound.description", vstNode.getVstPluginName() ),
                                SgEngine.getInstance().getResourceBundle().getString(
                                        "plugin.sbEditor.node.vst.error.title" ),
                                JOptionPane.ERROR_MESSAGE );
                    return;
                }
                if (vstNode.getVstPlugin() != null) {
                    vstNode.getVstPlugin().openEditWindow();
                }
            } else {
                VstPluginDescriptor pluginDesc = null;
                if (vstNode.getVstPlugin() != null) {
                    pluginDesc = vstNode.getVstPlugin().getDescriptor();
                }
                VstPluginDescriptor desc = UiToolkit.showSelectVstPluginDialog( pluginDesc );
                if (desc != null && desc != pluginDesc) {
                    VstPlugin plugin = desc.createPlugin();
                    SgUndoableEdit edit = new ChangePluginEdit( getSoundbusDescriptor(), vstNode, plugin );
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

        VstPlugin vstPlugin;
        SbOutput[] connectedIns;
        SbInput[] connectedOuts;

        ChangePluginEdit(
                SoundbusDescriptor soundbusDescriptor, VstNode node, VstPlugin vstPlugin ) {
            super( soundbusDescriptor, node,
                    SgEngine.getInstance().getResourceBundle().getString( "edit.changeVstPlugin" ),
                    true );
            this.vstPlugin = vstPlugin;
        }
        
        
        @Override
        protected void redoImpl() {
            performImpl();
        }

        @Override
        protected void undoImpl() {
            VstNode vstNode = (VstNode) getNode();
            VstPlugin vstPlugin = vstNode.getVstPlugin();
            vstNode.setVstPlugin( this.vstPlugin );
            this.vstPlugin = vstPlugin;
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
            VstNode vstNode = (VstNode) getNode();
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
            VstPlugin vstPlugin = vstNode.getVstPlugin();
            vstNode.setVstPlugin( this.vstPlugin );
            this.vstPlugin = vstPlugin;
        }
    }
}
