/*
 * Created on 02.12.2009
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.MidiNoteCounterElement;
import com.jonasreese.sound.sg.soundbus.MidiNoteCounterNode;
import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbMidiInput;
import com.jonasreese.sound.sg.soundbus.SbMidiOutput;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusException;
import com.jonasreese.sound.sg.soundbus.SoundbusNodesConnectionEvent;

/**
 * @author Jonas Reese
 */
public class MidiNoteCounterNodeImpl implements MidiNoteCounterNode {

    private static final String ELEMENT_PARAM_PREFIX = "element_";
    
    private List<MidiNoteCounterElement> elements;
    private String name;
    private SoundbusImpl parent;
    private SbMidiOutputImpl[] outputs;
    private SbMidiInputImpl[] inputs;
    private boolean open;
    
    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;
    
    private int counter;
    private int lastCounterFired;

    public MidiNoteCounterNodeImpl( SoundbusImpl parent, String name ) {
        elements = new ArrayList<MidiNoteCounterElement>();
        this.name = name;
        this.parent = parent;
        open = false;
        clientProperties = new HashMap<String,String>();
        propertyChangeSupport = new PropertyChangeSupport( this );
        outputs = new SbMidiOutputImpl[] {
                new SbMidiOutputImpl( SgEngine.getInstance().getResourceBundle().getString( "midi.output" ), "output_1" )
        };
        inputs = new SbMidiInputImpl[1];
        String inputName = SgEngine.getInstance().getResourceBundle().getString( "midi.input" );
        inputs[0] = new SbMidiInputImpl( inputName, "input_1" );

    }
    
    public int getCounter() {
        return counter;
    }
    
    public int getLastCounterFired() {
        return lastCounterFired;
    }
    
    public MidiNoteCounterElement[] getCounterElements() {
        MidiNoteCounterElement[] elements = new MidiNoteCounterElement[this.elements.size()];
        this.elements.toArray( elements );
        return elements;
    }
    
    public void setCounterElements( MidiNoteCounterElement[] elements ) {
        this.elements.clear();
        for (int i = 0; i < elements.length; i++) {
            this.elements.add( elements[i] );
        }
    }

    public void addCounterElement( MidiNoteCounterElement element ) {
        elements.add(element);
    }

    public MidiNoteCounterElement getCounterElementAt( int index ) {
        return elements.get( index );
    }

    public int getCounterElementCount() {
        return elements.size();
    }

    public boolean removeCounterElement(MidiNoteCounterElement element) {
        return elements.remove( element );
    }

    public void removeCounterElementAt( int index ) {
        elements.remove( index );
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public String getType() {
        return "midiNoteCounter";
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
            if (m instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) m;
                if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                    ++counter;
                    int lastLastCounterFired = lastCounterFired;
                    for (MidiNoteCounterElement element : elements) {
                        if (element.notifyCounterChanged( counter, lastCounterFired )) {
                            lastCounterFired = counter;
                        }
                    }
                    propertyChangeSupport.firePropertyChange( "counter", Integer.valueOf( counter - 1 ), Integer.valueOf( counter ) );
                    propertyChangeSupport.firePropertyChange(
                            "lastCounterFired", Integer.valueOf( lastLastCounterFired ), Integer.valueOf( lastCounterFired ) );
                }
            }
            if (outputs[0].connectedInput != null) {
                outputs[0].connectedInput.receive(m, output);
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
            return MidiNoteCounterNodeImpl.this;
        }

        public void connect( SbOutput output ) throws CannotConnectException, IllegalStateException {
            counter = 0;
            lastCounterFired = 0; 
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
            return MidiNoteCounterNodeImpl.this;
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
        Map<String, String> map = new LinkedHashMap<String, String>();
        
        // parametrize elements
        for (int i = 0; i < elements.size(); i++) {
            MidiNoteCounterElement element = elements.get( i );
            Map<String, String> m = element.getParameters();
            m.put( ELEMENT_PARAM_PREFIX + i + "_class", element.getClass().getName() );
            for (String key : m.keySet()) {
                m.put( ELEMENT_PARAM_PREFIX + i + "_" + key, m.get( key ) );
            }
        }
        
        return map;
    }

    public void setParameters( Map<String, String> parameters ) {
        // collect elements by their indices
        Map<Integer, Map<String, String>> indexMap = new TreeMap<Integer, Map<String, String>>(); 
        for (String key : parameters.keySet()) {
            if (key != null && key.startsWith( ELEMENT_PARAM_PREFIX )) {
                String value = parameters.get( key );
                key = key.substring( ELEMENT_PARAM_PREFIX.length() );
                int index = key.indexOf( '_' );
                if (index > 0) {
                    String elemIndexStr = key.substring( 0, index );
                    try {
                        int elemIndex = Integer.parseInt( elemIndexStr );
                        Map<String, String> map = indexMap.get( elemIndex );
                        if (map == null) {
                            map = new LinkedHashMap<String, String>();
                            indexMap.put( elemIndex, map );
                        }
                        map.put( key.substring( index ), value );
                    } catch (NumberFormatException nfex) {
                        nfex.printStackTrace();
                    }
                }
            }
        }
        
        // go through all elements
        for (Integer key : indexMap.keySet()) {
            Map<String, String> map = indexMap.get( key );
            String className = map.get( "class" );
            // create instance from class name
            if (className != null) {
                try {
                    Object o = Class.forName( className );
                    if (o instanceof MidiNoteCounterElement) {
                        MidiNoteCounterElement element = (MidiNoteCounterElement) o;
                        element.setParameters( map );
                        elements.add( element );
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
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
