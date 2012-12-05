/*
 * Created on 02.12.2009
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.soundbus.MidiNoteCounterElement;
import com.jonasreese.sound.sg.soundbus.MidiNoteCounterNode;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.edit.SbNodeStateChangeEdit;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Jonas Reese
 */
public class PMidiNoteCounterNode extends PSbNode implements PropertyChangeListener {

    private static final long serialVersionUID = 1L;
    
    private static final CounterElementType ADJUST_TEMPO_TYPE =
        new CounterElementType( com.jonasreese.sound.sg.soundbus.impl.AdjustTempoMidiNoteCounterElement.class );
    
    public PMidiNoteCounterNode(
            SbEditorComponent editor, MidiNoteCounterNode node, SoundbusDescriptor soundbusDescriptor ) {
        super( editor, node, soundbusDescriptor );
        backgroundPaint = new Color( 50, 85, 60, 100 );
    }

    public void nodeAdded() {
        node.addPropertyChangeListener( this );
    }

    public void nodeRemoved() {
        node.removePropertyChangeListener( this );
    }
    
    public void editNode() {
        try {
            if (getSoundbusDescriptor().getSoundbus().isOpen()) {
                return;
            }
        } catch (Exception ioex) {
            return;
        }
        try {
            //Soundbus s = getSoundbusDescriptor().getSoundbus();
            //final SetTempoPanel tempoPanel = new SetTempoPanel( s.getTempo() );

            MidiNoteCounterNode n = (MidiNoteCounterNode) node;
            MidiNoteCounterElement[] elements = n.getCounterElements();
            JList list = new JList( elements );
            final JScrollPane listPanel = new JScrollPane( list );
            final List<MidiNoteCounterElement> newElements = new ArrayList<MidiNoteCounterElement>( elements.length );
            for (int i = 0; i < elements.length; i++) {
                newElements.add( elements[i] );
            }
            JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
            JComboBox typeCombo = new JComboBox( new Object[] { ADJUST_TEMPO_TYPE } );
            buttonPanel.add( typeCombo );
            JButton addButton = new JButton( SgEngine.getInstance().getResourceBundle().getString( "plugin.sbEditor.node.midiNoteCounter.addCounterElement" ) );
            buttonPanel.add( addButton );

            Object[] message = new Object[] { listPanel, buttonPanel };
            JOptionPane pane = new JOptionPane(
                message, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null, listPanel );
            JDialog d = pane.createDialog(
                    UiToolkit.getMainFrame(), SgEngine.getInstance().getResourceBundle().getString( "plugin.sbEditor.node.midiNoteCounter.editNodeTitle" ) );
            d.addComponentListener( new ComponentAdapter() {
                public void componentShown(ComponentEvent e) {
                    listPanel.requestFocus();
                }
            } );
            d.setVisible( true );
            
            Integer selectedValue = (Integer) pane.getValue();
            if (selectedValue != null && selectedValue.intValue() == JOptionPane.OK_OPTION) {

                MidiNoteCounterElement[] newElementsArray = new MidiNoteCounterElement[newElements.size()];
                newElements.toArray( newElementsArray );
                
                ChangeNodeEdit edit = new ChangeNodeEdit(
                        getSoundbusDescriptor(), (MidiNoteCounterNode) getSbNode(),
                        elements, newElementsArray );
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
    
    static class ChangeNodeEdit extends SbNodeStateChangeEdit {

        private static final long serialVersionUID = 1L;
        
        private MidiNoteCounterElement[] oldElements;
        private MidiNoteCounterElement[] newElements;

        ChangeNodeEdit(
                SoundbusDescriptor soundbusDescriptor,
                MidiNoteCounterNode node,
                MidiNoteCounterElement[] oldElements,
                MidiNoteCounterElement[] newElements) {
            super( soundbusDescriptor, node,
                    SgEngine.getInstance().getResourceBundle().getString( "edit.changeMidiNoteCounter" ),
                    false );
            this.oldElements = oldElements;
            this.newElements = newElements;
        }

        @Override
        protected void redoImpl() {
            performImpl();
        }

        @Override
        protected void undoImpl() {
            ((MidiNoteCounterNode) getNode()).setCounterElements( oldElements );
        }

        @Override
        public void performImpl() {
            ((MidiNoteCounterNode) getNode()).setCounterElements( newElements );
            
        }
    }

    public void propertyChange( PropertyChangeEvent evt ) {
        if ("counter".equals( evt.getPropertyName() )) {
            setTitleTextSecondLine( evt.getNewValue().toString() );
        }
    }
    
    static class CounterElementType {
        Class<? extends MidiNoteCounterElement> clazz;
        CounterElementType( Class<? extends MidiNoteCounterElement> clazz ) {
            this.clazz = clazz;
        }
        public String toString() {
            String s = SgEngine.getInstance().getResourceBundle().getString( "plugin.sbEditor.node.midiNoteCounter.type." + clazz.getSimpleName() );
            return (s == null ? "unknown" : s);
        }
    }
}
