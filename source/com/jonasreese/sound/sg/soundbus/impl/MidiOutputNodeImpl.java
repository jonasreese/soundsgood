/*
 * Created on 29.11.2005
 *
 * To change this generated comment go to 
 * Woutdow>Preferences>Java>Code Generation>Code Template
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

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDeviceDescriptor;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.MidiOutputNode;
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
public class MidiOutputNodeImpl implements MidiOutputNode, NodeImpl {

    private SoundbusImpl parent;
    private String name;
    private SbInput[] inputs;
    private MidiDeviceDescriptor midiDevice;
    private MidiDevice openDevice;
    private boolean deviceWasOpen;
    private Receiver receiver;
    
    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;

    private SbMidiInputImpl connectedInput;
    private SbMidiOutput connectedOutput;
    
    public MidiOutputNodeImpl( SoundbusImpl parent, String name ) {
        this.parent = parent;
        clientProperties = new HashMap<String,String>();
        propertyChangeSupport = new PropertyChangeSupport( this );
        inputs = new SbInput[] {
                new SbMidiInputImpl( SgEngine.getInstance().getResourceBundle().getString( "midi.input" ) )
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

    public SbOutput[] getOutputs() {
        return NO_OUTPUTS;
    }

    public SbInput[] getInputs() {
        return inputs;
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
        return "midiOutput";
    }
    
    private void connectToOutput(
            SbMidiInputImpl input, SbMidiOutput output ) throws CannotConnectException {
        disconnectFromOutput();
        connectedInput = input;
        connectedOutput = output;
        //System.out.println( "output.getConnectedInput() = " + output.getConnectedInput() );
        if (output.getConnectedInput() == input) {
            parent.fireNodesConnectedEvent( new SoundbusNodesConnectionEvent( parent, input, output ) );
        }
    }
    
    private void disconnectFromOutput() {
        //System.out.println( "MidiOutputNodeImpl.disconnectFromOutput()" );
        if (connectedInput != null && connectedOutput != null && connectedOutput.getConnectedInput() == null) {
            parent.fireNodesDisconnectedEvent(
                    new SoundbusNodesConnectionEvent( parent, connectedInput, connectedOutput ) );
        }
        
        connectedInput = null;
        connectedOutput = null;
    }
    

    public synchronized void openImpl() throws SoundbusException {
        try {
            System.out.println( "MidiOutputNodeImpl.connectToOutput() : opening MIDI device" );
            openDevice = MidiToolkit.getMidiDevice( getMidiDevice() );
            deviceWasOpen = openDevice.isOpen();
            if (!deviceWasOpen) {
                openDevice.open();
            }
            receiver = openDevice.getReceiver();
        } catch (MidiUnavailableException e) {
            openDevice.close();
            throw new SoundbusException( e );
        }
    }
    
    public synchronized void closeImpl() throws SoundbusException {
        if (receiver != null) {
            try {
                receiver.close();
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
        }
        if (openDevice != null && !deviceWasOpen) {
            try {
                openDevice.close();
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
            System.out.println( "MidiOutputNodeImpl.disconnectFromOutput() : closing device" );
        }
        receiver = null;
        openDevice = null;
    }

    public void destroyImpl() throws SoundbusException {
    }

    public boolean isOpenImpl() {
        return (openDevice != null);
    }
    
    // MIDI input connector impl
    class SbMidiInputImpl implements SbMidiInput {

        SbMidiOutput output;
        String name;
        
        
        SbMidiInputImpl( String name ) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }

        public String getDescription() {
            return null;
        }

        public void receive( MidiMessage m, SbOutput output ) {
            if (receiver != null) {
                receiver.send( m, 0 );
            }
        }

        public void connect( SbOutput output ) throws CannotConnectException {
            if (!(output instanceof SbMidiOutput)) {
                throw new CannotConnectException( "Incompatible types" );
            }
            if (parent.isOpen()) {
                throw new IllegalStateException( "Cannot connect: Parent soundbus is open" );
            }            this.output = (SbMidiOutput) output;
            connectToOutput( this, this.output );
        }
        
        public void disconnect() {
            if (parent.isOpen()) {
                throw new IllegalStateException( "Cannot connect: Parent soundbus is open" );
            }
            disconnectFromOutput();
            this.output = null;
        }

        public SbNode getSbNode() {
            return MidiOutputNodeImpl.this;
        }

        public SbOutput getConnectedOutput() {
            return output;
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
            setMidiDevice( MidiToolkit.getMidiOutputDeviceList().getDescriptorById(
                    parameters.get( "midiDevice" ) ) );
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