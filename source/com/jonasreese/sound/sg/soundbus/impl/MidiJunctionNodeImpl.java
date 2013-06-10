/*
 * Created on 21.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.MidiMessage;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.MidiJunctionNode;
import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbMidiInput;
import com.jonasreese.sound.sg.soundbus.SbMidiOutput;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusException;
import com.jonasreese.sound.sg.soundbus.SoundbusNodesConnectionEvent;

/**
 * @author jonas.reese
 */
public class MidiJunctionNodeImpl implements MidiJunctionNode, NodeImpl {

    private String name;
    private SoundbusImpl parent;
    private SbMidiOutputImpl[] outputs;
    private SbMidiInputImpl[] inputs;
    private boolean open;
    
    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;

    public MidiJunctionNodeImpl( int numInputs, SoundbusImpl parent, String name ) {
        this.name = name;
        this.parent = parent;
        open = false;
        clientProperties = new HashMap<String,String>();
        propertyChangeSupport = new PropertyChangeSupport( this );
        outputs = new SbMidiOutputImpl[] {
                new SbMidiOutputImpl( SgEngine.getInstance().getResourceBundle().getString( "midi.output" ), "output_1" )
        };
        inputs = new SbMidiInputImpl[numInputs];
        String inputName = SgEngine.getInstance().getResourceBundle().getString( "midi.input" );
        for (int i = 0; i < numInputs; i++) {
            inputs[i] = new SbMidiInputImpl( inputName, "input_" + (i + 1) );
        };

    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public String getType() {
        return "midiJunction";
    }

    public SbInput[] getInputs() {
        return inputs;
    }

    public SbOutput[] getOutputs() {
        return outputs;
    }

    public Soundbus getSoundbus() {
        return parent;
    }

    public void putClientProperty( String property, String value ) {
        String oldValue = clientProperties.get( property );
        clientProperties.put( property, value );
        propertyChangeSupport.firePropertyChange( property, oldValue, value );
        parent.getSoundbusDescriptor().setChanged( true );
    }

    public boolean removeClientProperty( String property ) {
        Object oldValue = clientProperties.remove( property );
        if (oldValue != null) {
            propertyChangeSupport.firePropertyChange( property, oldValue, null );
            parent.getSoundbusDescriptor().setChanged( true );
            return true;
        }
        return false;
    }

    public String getClientProperty( String property ) {
        return clientProperties.get( property );
    }

    public Map<String, String> getClientProperties() {
        return clientProperties;
    }

    public void openImpl() throws SoundbusException {
        open = true;
    }

    public void closeImpl() throws SoundbusException {
        open = false;
    }

    public void destroyImpl() throws SoundbusException {
    }

    public boolean isOpenImpl() {
        return open;
    }

    class SbMidiInputImpl implements SbMidiInput {
        private SbOutput connectedOutput;
        private String name;
        private String inputId;
        
        SbMidiInputImpl( String name, String inputId ) {
            this.name = name;
            this.inputId = inputId;
        }
        
        public void receive( MidiMessage m, SbOutput output ) {
            SbMidiInput input = outputs[0].connectedInput;
            if (input != null) {
                input.receive( m, output );
            }
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return null;
        }

        public String getInputId() {
            return inputId;
        }
        
        public SbNode getSbNode() {
            return MidiJunctionNodeImpl.this;
        }

        public void connect( SbOutput output ) throws CannotConnectException, IllegalStateException {
            if (connectedOutput != null) {
                throw new IllegalStateException( "Cannot connect: already connected" );
            }
            if (parent.isOpen()) {
                throw new IllegalStateException( "Cannot connect: parent soundbus is open" );
            }
            connectedOutput = output;
            if (output.getConnectedInput() != null) {
                parent.fireNodesConnectedEvent( new SoundbusNodesConnectionEvent( parent, this, output ) );
            }
        }

        public void disconnect() throws IllegalStateException {
            if (parent.isOpen()) {
                throw new IllegalStateException( "Cannot disconnect: parent soundbus is open" );
            }
            if (connectedOutput != null) {
                SbOutput out = connectedOutput;
                connectedOutput = null;
                if (out.getConnectedInput() == null) {
                    parent.fireNodesDisconnectedEvent(
                            new SoundbusNodesConnectionEvent( parent, this, out ) );
                }
            }
        }

        public SbOutput getConnectedOutput() {
            return connectedOutput;
        }
    }
    
    class SbMidiOutputImpl implements SbMidiOutput {
        private SbMidiInput connectedInput;
        private String name;
        private String outputId;
        
        SbMidiOutputImpl( String name, String outputId ) {
            this.name = name;
            this.outputId = outputId;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return null;
        }

        public String getOutputId() {
            return outputId;
        }
        
        public SbNode getSbNode() {
            return MidiJunctionNodeImpl.this;
        }

        public boolean canConnect( SbInput in ) {
            return (in instanceof SbMidiInput);
        }

        public void connect( SbInput in ) throws CannotConnectException, IllegalStateException {
            if (!canConnect( in )) {
                throw new CannotConnectException( "Incompatible types" );
            }
            if (connectedInput != null) {
                throw new IllegalStateException( "Cannot connect: already connected" );
            }
            if (parent.isOpen()) {
                throw new IllegalStateException( "Cannot connect: parent soundbus is open" );
            }
            connectedInput = (SbMidiInput) in;
            if (in.getConnectedOutput() != null) {
                parent.fireNodesConnectedEvent( new SoundbusNodesConnectionEvent( parent, in, this ) );
            }
        }

        public void disconnect() throws IllegalStateException {
            if (parent.isOpen()) {
                throw new IllegalStateException( "Cannot disconnect: parent soundbus is open" );
            }
            if (connectedInput != null) {
                SbInput in = connectedInput;
                connectedInput = null;
                if (in.getConnectedOutput() == null) {
                    parent.fireNodesDisconnectedEvent(
                            new SoundbusNodesConnectionEvent( parent, in, this ) );
                }
            }
        }

        public SbInput getConnectedInput() {
            return connectedInput;
        }
    }

    public Map<String, String> getParameters() {
        return null;
    }

    public void setParameters( Map<String, String> parameters ) {
    }
    
    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        propertyChangeSupport.addPropertyChangeListener( listener );
    }

    public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener ) {
        propertyChangeSupport.addPropertyChangeListener( propertyName, listener );
    }

    public void removePropertyChangeListener( PropertyChangeListener listener ) {
        propertyChangeSupport.removePropertyChangeListener( listener );
    }

    public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener ) {
        propertyChangeSupport.removePropertyChangeListener( propertyName, listener );
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return propertyChangeSupport.getPropertyChangeListeners();
    }
}
