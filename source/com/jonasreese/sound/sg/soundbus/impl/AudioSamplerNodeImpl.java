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
import javax.sound.midi.ShortMessage;
import javax.sound.sampled.AudioFormat;

import com.jonasreese.sound.sg.RecorderException;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.audio.AudioDescriptor;
import com.jonasreese.sound.sg.audio.AudioToolkit;
import com.jonasreese.sound.sg.soundbus.AudioSamplerNode;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.SbAudioInput;
import com.jonasreese.sound.sg.soundbus.SbAudioOutput;
import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbMidiInput;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusException;
import com.jonasreese.sound.sg.soundbus.SoundbusNodesConnectionEvent;

/**
 * @author jonas.reese
 */
public class AudioSamplerNodeImpl implements AudioSamplerNode, NodeImpl {

    private SbOutput[] outputs;
    private SbMidiInputImpl[] inputs;
    private SoundbusImpl parent;
    private String name;
    private boolean open;
    
    private AudioDescriptor audioDescriptor;
    private RetriggerMode retriggerMode;
    private SampleMode sampleMode;
    private boolean defaultOutputsEnabled;

    private SbAudioInput connectedInput;
    private SbAudioOutputImpl connectedOutput;

    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;
    
    
    public AudioSamplerNodeImpl( SoundbusImpl parent, String name ) {
        this.parent = parent;
        this.name = name;
        clientProperties = new HashMap<String,String>();
        propertyChangeSupport = new PropertyChangeSupport( this );
        open = false;
        inputs = new SbMidiInputImpl[] {
                new SbMidiInputImpl( SgEngine.getInstance().getResourceBundle().getString( "midi.trigger.input" ) )
        };
        outputs = new SbAudioOutputImpl[] { new SbAudioOutputImpl() };
        audioDescriptor = null;
        retriggerMode = RetriggerMode.RESTART;
        sampleMode = SampleMode.START_TO_END;
        defaultOutputsEnabled = true;
    }
    
    public void setAudioDescriptor( AudioDescriptor audioDescriptor ) {
        if (this.audioDescriptor == audioDescriptor) {
            return;
        }
        AudioDescriptor oldAudioDescriptor = this.audioDescriptor;
        this.audioDescriptor = audioDescriptor;
        propertyChangeSupport.firePropertyChange( "audioDescriptor", oldAudioDescriptor, audioDescriptor );
        parent.getSoundbusDescriptor().setChanged( true );
    }
    
    public AudioDescriptor getAudioDescriptor() {
        return audioDescriptor;
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
        if (audioDescriptor != null) {
            map.put( "audioDescriptor", audioDescriptor.getName() );
        }
        map.put( "retriggerMode", retriggerMode.toString() );
        map.put( "sampleMode", sampleMode.toString() );
        map.put( "defaultOutputsEnabled", Boolean.toString( defaultOutputsEnabled ) );
        return map;
    }
    
    public void setParameters( Map<String, String> parameters ) {
        if (parameters.containsKey( "audioDescriptor" )) {
            String name = parameters.get( "audioDescriptor" );
            setAudioDescriptor( parent.getSoundbusDescriptor().getSession().getAudioElementByName( name ) );
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
        return "audioSampler";
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
        if (audioDescriptor != null) {
            SbAudioOutputImpl audioOut = (SbAudioOutputImpl) getOutputs()[0];
            SbAudioInput audioIn = (SbAudioInput) audioOut.getConnectedInput();
            if (audioIn != null && audioDescriptor != null) {
                audioDescriptor.getAudioRecorder().removeAudioOutputReceiver( audioIn );
            }
            audioDescriptor.getAudioRecorder().stop();
        }
        open = false;
    }

    public void openImpl() throws SoundbusException {
        // verify that sequence is loaded
        SbAudioOutputImpl audioOut = (SbAudioOutputImpl) getOutputs()[0];
        SbAudioInput audioIn = (SbAudioInput) audioOut.getConnectedInput();
        if (audioIn != null && audioDescriptor != null) {
            audioDescriptor.getAudioRecorder().addAudioOutputReceiver( audioIn );
        }
        if (audioDescriptor != null) {
            audioDescriptor.getAudioRecorder().preparePlayback();
        }
        open = true;
    }

    public boolean isOpenImpl() {
        return open;
    }
    
    private void connectToInput(
            final SbAudioOutputImpl output, final SbAudioInput input, boolean fireEvent )
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
        //System.out.println( "AudioInputNodeImpl.disconnectFromInput()" );
        if (connectedInput != null && connectedOutput != null && connectedInput.getConnectedOutput() == null) {
            parent.fireNodesDisconnectedEvent(
                    new SoundbusNodesConnectionEvent( parent, connectedInput, connectedOutput ) );
        }
        connectedInput = null;
        connectedOutput = null;
    }

    
    class SbMidiInputImpl implements SbMidiInput {
        private SbOutput connectedOutput;
        private String name;
        
        SbMidiInputImpl( String name ) {
            this.name = name;
        }
        
        public void receive( MidiMessage m, SbOutput output ) {
            if (m instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) m;
                if (((sm.getCommand() == ShortMessage.NOTE_ON || sm.getCommand() == ShortMessage.NOTE_OFF) && sm.getData2() > 0) ||
                        (sm.getCommand() == ShortMessage.CONTROL_CHANGE && sm.getData2() > 0)) {
                    if (audioDescriptor != null) {
                        System.out.println( "Triggering audio recorder " + audioDescriptor.getAudioRecorder().getAudioOutputReceivers().size() );
                        try {
                            if (retriggerMode == RetriggerMode.STOP && audioDescriptor.getAudioRecorder().isPlaying()) {
                                audioDescriptor.getAudioRecorder().stop();
                            } else if (retriggerMode == RetriggerMode.RESTART ||
                                    !audioDescriptor.getAudioRecorder().isPlaying()) {
                                audioDescriptor.getAudioRecorder().setNextPlaybackMuted( !defaultOutputsEnabled );
                                switch (sampleMode) {
                                case START_TO_END:
                                    audioDescriptor.getAudioRecorder().jumpToStart();
                                    audioDescriptor.getAudioRecorder().play();
                                    break;
                                }
                            }
                        } catch (RecorderException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (retriggerMode == RetriggerMode.STOP_ON_NOTE_OFF) {
                    if ((sm.getCommand() == ShortMessage.NOTE_OFF) ||
                        (sm.getData2() == 0 &&
                                (sm.getCommand() == ShortMessage.NOTE_ON) ||
                                (sm.getCommand() == ShortMessage.CONTROL_CHANGE))) {
                        if (audioDescriptor != null) {
                            audioDescriptor.getAudioRecorder().stop();
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

        public SbNode getSbNode() {
            return AudioSamplerNodeImpl.this;
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
    
    class SbAudioOutputImpl implements SbAudioOutput {

        SbAudioInput in;
        
        SbAudioOutputImpl() {
        }

        public AudioFormat getAudioFormat() {
            try {
                return audioDescriptor.getAudioRecorder().getAudioFormat();
            } catch (RecorderException rex) {
                rex.printStackTrace();
            }
            return AudioToolkit.getDefaultAudioFormat();
        }
        
        public String getName() {
            return AudioToolkit.getAudioOutputName( getAudioFormat() );
        }
        
        public String getDescription() {
            return AudioToolkit.getAudioOutputDescription( getAudioFormat() );
        }

        public boolean canConnect( SbInput in ) {
            return (in instanceof SbAudioInput);
        }

        public void connect( SbInput in ) throws CannotConnectException {
            if (!canConnect( in )) {
                throw new CannotConnectException( "Incompatible types" );
            }
            if (parent.isOpen()) {
                throw new IllegalStateException( "Cannot connect: Parent soundbus is open" );
            }
            this.in = (SbAudioInput) in;
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
            return AudioSamplerNodeImpl.this;
        }
    }
}
