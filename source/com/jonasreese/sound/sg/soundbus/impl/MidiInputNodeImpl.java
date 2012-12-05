/*
 * Created on 28.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDeviceDescriptor;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.MidiInputNode;
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
public class MidiInputNodeImpl implements MidiInputNode, NodeImpl {

    private SoundbusImpl parent;
    private String name;
    private SbOutput[] outputs;
    private MidiDeviceDescriptor midiDevice;
    private MidiDevice openDevice;
    private boolean deviceWasOpen;
    private Transmitter transmitter;

    private SbMidiInput connectedInput;
    private SbMidiOutputImpl connectedOutput;
    
    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;

    public MidiInputNodeImpl( SoundbusImpl parent, String name ) {
        this.parent = parent;
        clientProperties = new HashMap<String,String>();
        propertyChangeSupport = new PropertyChangeSupport( this );
        outputs = new SbOutput[] {
                new SbMidiOutputImpl( SgEngine.getInstance().getResourceBundle().getString( "midi.output" ) )
        };
    }

    public MidiDeviceDescriptor getMidiDevice() {
        return midiDevice;
    }

    public void setMidiDevice( MidiDeviceDescriptor midiDevice ) {
        if (isOpenImpl()) {
            throw new IllegalStateException( "Cannot set MIDI device: Soundbus is open" );
        }
        if (this.midiDevice == midiDevice ||
                (midiDevice != null && this.midiDevice != null &&
                        midiDevice.getId().equals( this.midiDevice.getId() ))) {
            return;
        }
        MidiDeviceDescriptor oldDevice = this.midiDevice;
        this.midiDevice = midiDevice;
        propertyChangeSupport.firePropertyChange( "midiDevice", oldDevice, midiDevice );
        parent.getSoundbusDescriptor().setChanged( true );
    }

    public SbInput[] getInputs() {
        return NO_INPUTS;
    }

    public SbOutput[] getOutputs() {
        return outputs;
    }

    public Soundbus getSoundbus() {
        return parent;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
       return name;
    }
    
    public String getType() {
        return "midiInput";
    }
    
    private void connectToInput(
            final SbMidiOutputImpl output, final SbMidiInput input, boolean fireEvent )
    throws CannotConnectException {
        disconnectFromInput();
        connectedInput = input;
        connectedOutput = output;
        //System.out.println( "input.getConnectedOutput() = " + input.getConnectedOutput() );
        if (input.getConnectedOutput() == output && fireEvent) {
            parent.fireNodesConnectedEvent( new SoundbusNodesConnectionEvent( parent, input, output ) );
        }
    }
    
    private void disconnectFromInput() {
        //System.out.println( "MidiInputNodeImpl.disconnectFromInput()" );
        if (connectedInput != null && connectedOutput != null && connectedInput.getConnectedOutput() == null) {
            parent.fireNodesDisconnectedEvent(
                    new SoundbusNodesConnectionEvent( parent, connectedInput, connectedOutput ) );
        }
        connectedInput = null;
        connectedOutput = null;
    }
    

    public void openImpl() throws SoundbusException {
        final SbMidiInput input = connectedInput;
        final SbMidiOutput output = connectedOutput;
        if (input == null || output == null) {
            return;
        }
        try {
            System.out.println( "MidiInputNodeImpl.connectToInput() : opening MIDI device" );
            openDevice = MidiToolkit.getMidiDevice( getMidiDevice() );
            deviceWasOpen = openDevice.isOpen();
            if (!deviceWasOpen) {
                openDevice.open();
            }
            transmitter = openDevice.getTransmitter();
            transmitter.setReceiver( new Receiver() {
                public void send( MidiMessage m, long tick ) {
                    input.receive( m, output );
                }
                public void close() {
                }
            } );
        } catch (MidiUnavailableException e) {
            throw new CannotConnectException( e );
        }
    }

    public void closeImpl() throws SoundbusException {
        if (transmitter != null) {
            try {
                transmitter.close();
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
            System.out.println( "MidiInputNodeImpl.disconnectFromInput() : closing transmitter" );
        }
        if (openDevice != null && !deviceWasOpen) {
            try {
                openDevice.close();
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
            System.out.println( "MidiInputNodeImpl.disconnectFromInput() : closing device" );
        }
        transmitter = null;
        openDevice = null;
    }

    public void destroyImpl() throws SoundbusException {
    }

    public boolean isOpenImpl() {
        return (openDevice != null);
    }
    
    // MIDI output connector impl
    class SbMidiOutputImpl implements SbMidiOutput {

        SbMidiInput in;
        String name;
        
        SbMidiOutputImpl( String name ) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }

        public String getDescription() {
            return null;
        }

        public boolean canConnect( SbInput in ) {
            return (in instanceof SbMidiInput);
        }

        public void connect( SbInput in ) throws CannotConnectException {
            if (!canConnect( in )) {
                throw new CannotConnectException( "Incompatible types" );
            }
            if (parent.isOpen()) {
                throw new IllegalStateException( "Cannot connect: Parent soundbus is open" );
            }
            this.in = (SbMidiInput) in;
            connectToInput( this, this.in, true );
        }

        public SbInput getConnectedInput() {
            return in;
        }

        public void disconnect() {
            if (parent.isOpen()) {
                throw new IllegalStateException( "Cannot connect: Parent soundbus is open" );
            }
            disconnectFromInput();
            in = null;
        }

        public SbNode getSbNode() {
            return MidiInputNodeImpl.this;
        }
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

    public Map<String, String> getParameters() {
        MidiDeviceDescriptor midiDevice = this.midiDevice;
        if (midiDevice == null) {
            return null;
        }
        return Collections.singletonMap( "midiDevice", midiDevice.getId().getIdString() );
    }
    
    public void setParameters( Map<String, String> parameters ) {
        if (parameters != null && parameters.containsKey( "midiDevice" )) {
            setMidiDevice( MidiToolkit.getMidiInputDeviceList().getDescriptorById(
                    parameters.get( "midiDevice"  ) ) );
        }
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