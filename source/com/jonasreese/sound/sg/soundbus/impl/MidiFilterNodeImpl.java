/*
 * Created on 02.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiMessage;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiFilter;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.MidiFilterNode;
import com.jonasreese.sound.sg.soundbus.MidiInputMonitor;
import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbMidiInput;
import com.jonasreese.sound.sg.soundbus.SbMidiOutput;
import com.jonasreese.sound.sg.soundbus.SbMonitorableMidiInput;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusException;
import com.jonasreese.sound.sg.soundbus.SoundbusNodesConnectionEvent;

/**
 * @author jonas.reese
 */
public class MidiFilterNodeImpl implements MidiFilterNode, NodeImpl {

    private SbMidiOutputImpl[] outputs;
    private SbMidiInputImpl[] inputs;
    private SoundbusImpl parent;
    private String name;
    private boolean open;

    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;
    private MidiFilter midiFilter;
    
    
    public MidiFilterNodeImpl( SoundbusImpl parent, String name ) {
        this.parent = parent;
        this.name = name;
        clientProperties = new HashMap<String,String>();
        propertyChangeSupport = new PropertyChangeSupport( this );
        open = false;
        inputs = new SbMidiInputImpl[] {
                new SbMidiInputImpl( SgEngine.getInstance().getResourceBundle().getString( "midi.input" ) )
        };
        outputs = new SbMidiOutputImpl[] {
                new SbMidiOutputImpl( SgEngine.getInstance().getResourceBundle().getString( "midi.output" ) )
        };
    }
    
    public void setMidiFilter( MidiFilter midiFilter ) {
        MidiFilter oldFilter = this.midiFilter;
        this.midiFilter = midiFilter;
        propertyChangeSupport.firePropertyChange( "midiFilter", oldFilter, this.midiFilter );
        parent.getSoundbusDescriptor().setChanged( true );
    }

    public MidiFilter getMidiFilter() {
        return midiFilter;
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


    /**
     * Returns exactly one instance of <code>MonitorableMidiInput</code>.
     * @return A one-element array with a <code>MonitorableMidiInput</code> object.
     */
    public SbMidiInput[] getInputs() {
        return inputs;
    }

    public String getName() {
        return name;
    }

    public SbOutput[] getOutputs() {
        return outputs;
    }

    public Map<String, String> getParameters() {
        MidiFilter midiFilter = this.midiFilter;
        if (midiFilter == null) {
            return null;
        }
        return Collections.singletonMap( "midiFilter", midiFilter.getStringRepresentation() );
    }
    
    public void setParameters( Map<String, String> parameters ) {
        if (parameters != null && parameters.containsKey( "midiFilter" )) {
            setMidiFilter( new MidiFilter( parameters.get( "midiFilter" ) ) );
        }
    }

    public Soundbus getSoundbus() {
        return parent;
    }

    public String getType() {
        return "midiFilter";
    }

    public void setName( String name ) {
        this.name = name;
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
    
    public void destroyImpl() throws SoundbusException {
    }

    public void closeImpl() throws SoundbusException {
        open = true;
    }

    public void openImpl() throws SoundbusException {
        open = false;
    }

    public boolean isOpenImpl() {
        return open;
    }
    
    class SbMidiInputImpl implements SbMonitorableMidiInput {
        private SbOutput connectedOutput;
        private String name;
        private List<MidiInputMonitor> monitors;
        
        SbMidiInputImpl( String name ) {
            this.name = name;
            monitors = new ArrayList<MidiInputMonitor>();
        }
        
        public void receive( MidiMessage m, SbOutput output ) {
            MidiFilter midiFilter = MidiFilterNodeImpl.this.midiFilter;
            boolean f = outputs[0].connectedInput != null && (midiFilter == null || midiFilter.filter( m ));
            if (f) {
                outputs[0].connectedInput.receive( m, output );
            }
            for (MidiInputMonitor monitor : monitors) {
                monitor.messageProcessed( m, output, (f ? Boolean.TRUE : Boolean.FALSE) );
            }
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return null;
        }

        public SbNode getSbNode() {
            return MidiFilterNodeImpl.this;
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

        public void addMidiInputMonitor( MidiInputMonitor monitor ) {
            if (monitors.contains( monitor )) {
                return;
            }
            monitors.add( monitor );
        }

        public void removeMidiInputMonitor( MidiInputMonitor monitor ) {
            monitors.remove( monitor );
        }
    }
    
    class SbMidiOutputImpl implements SbMidiOutput {
        private SbMidiInput connectedInput;
        private String name;

        SbMidiOutputImpl( String name ) {
            this.name = name;
        }
        
        public boolean canConnect( SbInput in ) {
            return (in instanceof SbMidiInput);
        }
        
        public void connect( SbInput in ) throws CannotConnectException, IllegalStateException {
            if (!canConnect( in )) {
                throw new CannotConnectException( "Incompatible types" );
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

        public String getName() {
            return name;
        }

        public String getDescription() {
            return null;
        }

        public SbNode getSbNode() {
            return MidiFilterNodeImpl.this;
        }
    }
}
