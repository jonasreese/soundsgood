/*
 * Created on 02.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.Metronome;
import com.jonasreese.sound.sg.midi.impl.MetronomeImpl;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbMidiInput;
import com.jonasreese.sound.sg.soundbus.SbMidiOutput;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusAdapter;
import com.jonasreese.sound.sg.soundbus.SoundbusEvent;
import com.jonasreese.sound.sg.soundbus.SoundbusException;
import com.jonasreese.sound.sg.soundbus.SoundbusListener;
import com.jonasreese.sound.sg.soundbus.SoundbusNodesConnectionEvent;
import com.jonasreese.sound.sg.soundbus.TempoNode;

/**
 * @author jonas.reese
 */
public class TempoNodeImpl implements TempoNode, NodeImpl {

    private SbMidiOutputImpl[] outputs;
    private SbMidiInput[] inputs;
    private SoundbusImpl parent;
    private boolean clickEnabled;
    private String name;
    private boolean open;
    private Metronome metronome;
    private SoundbusListener soundbusListener;
    private Receiver midiOutputReceiver;

    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;
    
    
    public TempoNodeImpl( SoundbusImpl parent, String name ) {
        this.parent = parent;
        this.name = name;
        clickEnabled = true;
        clientProperties = new HashMap<String,String>();
        propertyChangeSupport = new PropertyChangeSupport( this );
        open = false;
        inputs = new SbMidiInput[] {
                new SbMidiToggleInputImpl( SgEngine.getInstance().getResourceBundle().getString( "midi.click.toggleInput" ) ),
                new SbMidiInputImpl( SgEngine.getInstance().getResourceBundle().getString( "midi.click.input" ) )
        };
        outputs = new SbMidiOutputImpl[] {
                new SbMidiOutputImpl( SgEngine.getInstance().getResourceBundle().getString( "midi.click.output" ) )
        };
        metronome = new MetronomeImpl( true );
        soundbusListener = new SoundbusAdapter() {
            public void tempoChanged( SoundbusEvent e ) {
                Metronome metronome = TempoNodeImpl.this.metronome;
                if (metronome != null) {
                    metronome.setTempoInBpm( e.getSoundbus().getTempo() );
                }
            }
        };
        midiOutputReceiver = new Receiver() {
            public void close() {
            }
            public void send( MidiMessage message, long timeStamp ) {
                SbMidiOutputImpl out = outputs[0];
                if (out.connectedInput != null) {
                    out.connectedInput.receive( message, out );
                }
            }
        };
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

    public SbInput[] getInputs() {
        return inputs;
    }

    public String getName() {
        return name;
    }

    public SbOutput[] getOutputs() {
        return outputs;
    }

    public Map<String, String> getParameters() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put( "clickEnabled", Boolean.toString( clickEnabled ) );
        map.put( "playDefaultClick", Boolean.toString( getPlayDefaultClick() ) );
        map.put( "sendTempoControlEventsEnabled", Boolean.toString( isSendTempoControlEventsEnabled() ) );
        map.put( "beatsPerTact", Integer.toString( getBeatsPerTact() ) );
        return map;
    }

    public void setParameters( Map<String, String> parameters ) {
        if (parameters.containsKey( "clickEnabled" )) {
            setClickEnabled( Boolean.TRUE.toString().equals( parameters.get( "clickEnabled" ) ) );
        }
        if (parameters.containsKey( "playDefaultClick" )) {
            setPlayDefaultClick( Boolean.TRUE.toString().equals( parameters.get( "playDefaultClick" ) ) );
        }
        if (parameters.containsKey( "sendTempoControlEventsEnabled" )) {
            setSendTempoControlEventsEnabled( Boolean.TRUE.toString().equals( parameters.get( "sendTempoControlEventsEnabled" ) ) );
        }
        if (parameters.containsKey( "beatsPerTact" )) {
            String s = parameters.get( "beatsPerTact" );
            if (s != null) {
                try {
                    setBeatsPerTact( Integer.parseInt( s ) );
                } catch (Exception ignored) {
                }
            }
        }
    }

    public Soundbus getSoundbus() {
        return parent;
    }

    public String getType() {
        return "tempo";
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
    
    public void setPlayDefaultClick( boolean defaultClick ) {
        if (getPlayDefaultClick() == defaultClick) {
            return;
        }
        metronome.setDefaultDeviceOutputEnabled( defaultClick );
        propertyChangeSupport.firePropertyChange( "playDefaultClick", !defaultClick, defaultClick );
        parent.getSoundbusDescriptor().setChanged( true );
    }
    
    public boolean getPlayDefaultClick() {
        return metronome.isDefaultDeviceOutputEnabled();
    }

    public synchronized void setClickEnabled( boolean clickEnabled ) {
        if (this.clickEnabled == clickEnabled) {
            return;
        }
        this.clickEnabled = clickEnabled;
        propertyChangeSupport.firePropertyChange( "clickEnabled", !clickEnabled, clickEnabled );
        parent.getSoundbusDescriptor().setChanged( true );
        if (isOpenImpl() && metronome.isRunning() && !clickEnabled) {
            stopMetronome();
        } else if (isOpenImpl() && !metronome.isRunning() && clickEnabled) {
            startMetronome();
        }
    }
    
    public boolean isClickEnabled() {
        return clickEnabled;
    }

    public void setBeatsPerTact( int beatsPerTact ) {
        int oldBeatsPerTact = metronome.getBeatsPerTact();
        if (oldBeatsPerTact == beatsPerTact) {
            return;
        }
        metronome.setBeatsPerTact( beatsPerTact );
        propertyChangeSupport.firePropertyChange( "beatsPerTact", oldBeatsPerTact, beatsPerTact );
        parent.getSoundbusDescriptor().setChanged( true );
    }

    public int getBeatsPerTact() {
        return metronome.getBeatsPerTact();
    }
    
    public void setSendTempoControlEventsEnabled(boolean sendTempoControlEventsEnabled) {
        if (metronome.isSendMidiClockEnabled() == sendTempoControlEventsEnabled) {
            return;
        }
        metronome.setSendMidiClockEnabled(sendTempoControlEventsEnabled);
        metronome.setSendMidiClockEnabled(sendTempoControlEventsEnabled);
        propertyChangeSupport.firePropertyChange(
                "sendTempoControlEventsEnabled", !sendTempoControlEventsEnabled, sendTempoControlEventsEnabled);
        parent.getSoundbusDescriptor().setChanged(true);
    }

    public boolean isSendTempoControlEventsEnabled() {
        return metronome.isSendMidiClockEnabled();
    }

    public Metronome getMetronome() {
        return metronome;
    }

    public void destroyImpl() throws SoundbusException {
    }

    public void closeImpl() throws SoundbusException {
        open = false;
        parent.removeSoundbusListener( soundbusListener );
        if (metronome != null) {
            stopMetronome();
        }
    }
    
    private void startMetronome() {
        metronome.addMidiOutputReceiver( midiOutputReceiver );
        metronome.setTempoInBpm( parent.getTempo() );
        metronome.start();
    }
    
    private void stopMetronome() {
        metronome.removeMidiOutputReceiver( midiOutputReceiver );
        metronome.stop();
    }

    public void openImpl() throws SoundbusException {
        parent.addSoundbusListener( soundbusListener );
        open = true;
        if (clickEnabled) {
            startMetronome();
        }
    }

    public boolean isOpenImpl() {
        return open;
    }
    
    class SbMidiInputImpl implements SbMidiInput {
        private SbOutput connectedOutput;
        private String name;
        
        private long lastTime;
        private long lastDuration;
        private double[] values;
        private int index;
        private int pointer;
        
        SbMidiInputImpl( String name ) {
            this.name = name;
            lastTime = -1;
            lastDuration = -1;
            values = new double[8];
            index = 0;
            pointer = 0;
        }
        
        public void receive( MidiMessage m, SbOutput output ) {
            if (m instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) m;
                if ((sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) ||
                        (sm.getCommand() == ShortMessage.CONTROL_CHANGE && sm.getData2() > 0)) {
                    long currTime = System.currentTimeMillis();
                    if (lastTime >= 0) {
                        long time = currTime - lastTime;
                        if (lastDuration >= 0 &&
                                Math.abs( time - lastDuration ) > (lastDuration / 2)) { // timeout condition
                            lastTime = -1;
                            lastDuration = -1;
                            index = 0;
                            pointer = 0;
                        } else {
                            lastDuration = time;
                            double lastVal;
                            if (time > 0) {
                                lastVal = (60000.0 / (double) time);
                            } else {
                                lastVal = Double.MAX_VALUE;
                            }
                            if (lastVal > 40) {
                                pointer %= values.length;
                                values[pointer++] = lastVal;
                                if (index < values.length) {
                                    index++;
                                }
                                double average = 0;
                                for (int i = 0; i < index; i++) {
                                    average += values[i];
                                }
                                average /= index;
                                if (average > 280.0) {
                                    currTime = -1;
                                    lastDuration = -1;
                                    index = 0;
                                    pointer = 0;
                                } else {
                                    getSoundbus().setTempo( (float) average );
                                }
                            } else { // too slow condition
                                currTime = -1;
                                lastDuration = -1;
                                index = 0;
                                pointer = 0;
                            }
                        }
                    }
                    lastTime = currTime;
                    if (lastTime >= 0 && metronome != null) {
                        metronome.sync();
                    }
                }
            }
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return null;
        }

        public SbNode getSbNode() {
            return TempoNodeImpl.this;
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
    
    class SbMidiToggleInputImpl implements SbMidiInput {
        
        private SbOutput connectedOutput;
        private String name;
        
        SbMidiToggleInputImpl( String name ) {
            this.name = name;
        }
        
        public void receive( MidiMessage m, SbOutput output ) {
            if (m instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) m;
                if ((sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) ||
                        (sm.getCommand() == ShortMessage.CONTROL_CHANGE && sm.getData2() > 0)) {
                    setClickEnabled(!isClickEnabled());
                }
            }
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return null;
        }

        public SbNode getSbNode() {
            return TempoNodeImpl.this;
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
            return TempoNodeImpl.this;
        }
    }
}
