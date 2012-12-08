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

import com.jonasreese.sound.sg.RecorderException;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.MidiSamplerNode;
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
public class MidiSamplerNodeImpl implements MidiSamplerNode, NodeImpl {

    private SbOutput[] outputs;
    private SbMidiInputImpl[] inputs;
    private SoundbusImpl parent;
    private String name;
    private boolean open;
    
    private MidiDescriptor midiDescriptor;
    private RetriggerMode retriggerMode;
    private SampleMode sampleMode;
    private boolean defaultOutputsEnabled;
    
    private Receiver midiOutputReceiver;

    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;
    
    
    public MidiSamplerNodeImpl( SoundbusImpl parent, String name ) {
        this.parent = parent;
        this.name = name;
        clientProperties = new HashMap<String,String>();
        propertyChangeSupport = new PropertyChangeSupport( this );
        open = false;
        inputs = new SbMidiInputImpl[] {
                new SbMidiInputImpl( SgEngine.getInstance().getResourceBundle().getString( "midi.trigger.input" ), "input_1" )
        };
        outputs = new SbMidiOutputImpl[] {
                new SbMidiOutputImpl( SgEngine.getInstance().getResourceBundle().getString( "midi.output" ), "output_1" )
        };
        midiOutputReceiver = null;
        midiDescriptor = null;
        retriggerMode = RetriggerMode.RESTART;
        sampleMode = SampleMode.LEFT_TO_RIGHT_MARKER;
        defaultOutputsEnabled = true;
    }
    
    public void setMidiDescriptor( MidiDescriptor midiDescriptor ) {
        if (this.midiDescriptor == midiDescriptor) {
            return;
        }
        MidiDescriptor oldMidiDescriptor = this.midiDescriptor;
        this.midiDescriptor = midiDescriptor;
        propertyChangeSupport.firePropertyChange( "midiDescriptor", oldMidiDescriptor, midiDescriptor );
        parent.getSoundbusDescriptor().setChanged( true );
    }
    
    public MidiDescriptor getMidiDescriptor() {
        return midiDescriptor;
    }
    
    public void setRetriggerMode( RetriggerMode retriggerMode ) {
        if (this.retriggerMode == retriggerMode) {
            return;
        }
        RetriggerMode oldRetriggerMode = this.retriggerMode;
        this.retriggerMode = retriggerMode;
        propertyChangeSupport.firePropertyChange( "retriggerMode", oldRetriggerMode, retriggerMode );
        parent.getSoundbusDescriptor().setChanged( true );
    }
    
    public RetriggerMode getRetriggerMode() {
        return retriggerMode;
    }

    public void setSampleMode( SampleMode sampleMode ) {
        if (this.sampleMode == sampleMode) {
            return;
        }
        SampleMode oldSampleMode = this.sampleMode;
        this.sampleMode = sampleMode;
        propertyChangeSupport.firePropertyChange( "sampleMode", oldSampleMode, sampleMode );
        parent.getSoundbusDescriptor().setChanged( true );
    }
    
    public SampleMode getSampleMode() {
        return sampleMode;
    }
    
    public void setDefaultOutputsEnabled( boolean enabled ) {
        if (this.defaultOutputsEnabled == enabled) {
            return;
        }
        this.defaultOutputsEnabled = enabled;
        propertyChangeSupport.firePropertyChange(
                "defaultOutputsEnabled", !defaultOutputsEnabled, defaultOutputsEnabled );
        parent.getSoundbusDescriptor().setChanged( true );
    }

    public boolean getDefaultOutputsEnabled() {
        return defaultOutputsEnabled;
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
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        if (midiDescriptor != null) {
            map.put( "midiDescriptor", midiDescriptor.getName() );
        }
        map.put( "retriggerMode", retriggerMode.toString() );
        map.put( "sampleMode", sampleMode.toString() );
        map.put( "defaultOutputsEnabled", Boolean.toString( defaultOutputsEnabled ) );
        return map;
    }
    
    public void setParameters( Map<String, String> parameters ) {
        if (parameters.containsKey( "midiDescriptor" )) {
            String name = parameters.get( "midiDescriptor" );
            setMidiDescriptor( parent.getSoundbusDescriptor().getSession().getMidiElementByName( name ) );
        }
        if (parameters.containsKey( "retriggerMode" )) {
            String s = parameters.get( "retriggerMode" );
            if (s != null) {
                RetriggerMode rm = RetriggerMode.valueOf( s );
                if (rm != null) {
                    setRetriggerMode( rm );
                }
            }
        }
        if (parameters.containsKey( "sampleMode" )) {
            String s = parameters.get( "sampleMode" );
            for (SampleMode sm : SampleMode.values()) {
                if (sm.toString().equals( s )) {
                    sampleMode = sm;
                    break;
                }
            }
        }
        if (parameters.containsKey( "defaultOutputsEnabled" )) {
            setDefaultOutputsEnabled( Boolean.TRUE.toString().equals( parameters.get( "defaultOutputsEnabled" ) ) );
        }
    }

    public Soundbus getSoundbus() {
        return parent;
    }

    public String getType() {
        return "midiSampler";
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
        if (midiDescriptor != null) {
            midiDescriptor.getMidiRecorder().stop();
        }
        if (midiOutputReceiver != null) {
            if (midiDescriptor != null) {
                midiDescriptor.getMidiRecorder().removeMidiOutputReceiver( midiOutputReceiver );
            }
            midiOutputReceiver = null;
        }
        open = false;
    }

    public void openImpl() throws SoundbusException {
        // verify that sequence is loaded
        final SbMidiOutputImpl midiOut = (SbMidiOutputImpl) getOutputs()[0];
        final SbMidiInput midiIn = midiOut.connectedInput;
        if (midiIn != null) {
            midiOutputReceiver = new Receiver() {
                public void close() {
                }
                public void send( MidiMessage message, long timeStamp ) {
                    midiIn.receive( message, midiOut );
                }
            };
            if (midiDescriptor != null) {
                midiDescriptor.getMidiRecorder().addMidiOutputReceiver( midiOutputReceiver );
            }
        }
        if (midiDescriptor != null) {
            System.out.print( "preparePlayback..." );
            midiDescriptor.getMidiRecorder().preparePlayback();
            System.out.println( "done" );
        }
        open = true;
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
                if (((sm.getCommand() == ShortMessage.NOTE_ON || sm.getCommand() == ShortMessage.NOTE_OFF) && sm.getData2() > 0) ||
                        (sm.getCommand() == ShortMessage.CONTROL_CHANGE && sm.getData2() > 0)) {
                    if (midiDescriptor != null) {
                        try {
                            if (retriggerMode == RetriggerMode.STOP && midiDescriptor.getMidiRecorder().isPlaying()) {
                                midiDescriptor.getMidiRecorder().stop();
                            } else if (retriggerMode == RetriggerMode.RESTART ||
                                    !midiDescriptor.getMidiRecorder().isPlaying()) {
                                midiDescriptor.getMidiRecorder().setNextPlaybackMuted( !defaultOutputsEnabled );
                                switch (sampleMode) {
                                case LEFT_TO_RIGHT_MARKER:
                                    midiDescriptor.getMidiRecorder().playFromLeftMarker();
                                    break;
                                case START_TO_END:
                                    midiDescriptor.getMidiRecorder().setTickPosition( 0 );
                                    midiDescriptor.getMidiRecorder().play();
                                    break;
                                case LEFT_TO_RIGHT_MARKER_LOOP:
                                    midiDescriptor.getMidiRecorder().loopFromLeftToRightMarker();
                                    break;
                                }
                            }
                            midiDescriptor.getMidiRecorder().setTempoInBPM( getSoundbus().getTempo() );
                        } catch (RecorderException e) {
                        }
                    }
                } else if (retriggerMode == RetriggerMode.STOP_ON_NOTE_OFF) {
                    if ((sm.getCommand() == ShortMessage.NOTE_OFF) ||
                        (sm.getData2() == 0 &&
                                (sm.getCommand() == ShortMessage.NOTE_ON) ||
                                (sm.getCommand() == ShortMessage.CONTROL_CHANGE))) {
                        if (midiDescriptor != null) {
                            midiDescriptor.getMidiRecorder().stop();
                        }
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
        
        public String getInputId() {
            return inputId;
        }

        public SbNode getSbNode() {
            return MidiSamplerNodeImpl.this;
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
        
        public String getOutputId() {
            return outputId;
        }

        public SbNode getSbNode() {
            return MidiSamplerNodeImpl.this;
        }
    }
}
