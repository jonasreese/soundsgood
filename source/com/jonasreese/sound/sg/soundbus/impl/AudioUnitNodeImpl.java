/*
 * Created on 04.02.2006
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
import java.util.Map;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.sampled.AudioFormat;

import com.jonasreese.sound.aucontainer.AUContainer;
import com.jonasreese.sound.aucontainer.AudioUnit;
import com.jonasreese.sound.aucontainer.AudioUnitDescriptor;
import com.jonasreese.sound.aucontainer.AudioUnitEvent;
import com.jonasreese.sound.aucontainer.AudioUnitListener;
import com.jonasreese.sound.aucontainer.AudioUnitNotAvailableException;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.audio.AudioDataPump;
import com.jonasreese.sound.sg.audio.AudioToolkit;
import com.jonasreese.sound.sg.audio.SilenceAudioDataPump;
import com.jonasreese.sound.sg.soundbus.AudioUnitNode;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.SbAudioInput;
import com.jonasreese.sound.sg.soundbus.SbAudioOutput;
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

/**
 * @author jonas.reese
 */
public class AudioUnitNodeImpl implements AudioUnitNode, NodeImpl {
    private String name;
    private SoundbusImpl parent;
    private boolean open;
    private SbInput[] inputs;
    private SbOutput[] outputs;
    
    private AudioUnit audioUnit;
    private String pluginName;
    
    private SbAudioInputImpl audioInput;
    private SbAudioOutputImpl audioOutput;
    private SbMidiInputImpl midiInput;
    private SbMidiOutputImpl midiOutput;
    private SbMidiInputImpl cachedMidiInput;
    private SbMidiOutputImpl cachedMidiOutput;
    private SilenceAudioDataPump dataPump;
    private AudioUnitListener listener;
    
    private Map<SbInput,SbOutput> connectionMap;
    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;
    


    public AudioUnitNodeImpl( SoundbusImpl parent ) {
        this.parent = parent;
        this.name = null;
        open = false;
        connectionMap = new HashMap<SbInput,SbOutput>();
        clientProperties = new HashMap<String,String>();
        propertyChangeSupport = new PropertyChangeSupport( this );
        init();
    }
    
    public void setAudioUnit( AudioUnit audioUnit ) {
        if (isOpenImpl() || this.audioUnit == audioUnit) {
            return;
        }
        AudioUnit oldPlugin = this.audioUnit;
        if (oldPlugin != null) {
            oldPlugin.close();
        }
        this.audioUnit = audioUnit;
        init();
        propertyChangeSupport.firePropertyChange( "audioUnit", oldPlugin, audioUnit );
        parent.getSoundbusDescriptor().setChanged( true );
    }
    
    private void init() {
        if (audioUnit != null) {
            audioUnit.open();
        }
        ArrayList<SbInput> inputsList = new ArrayList<SbInput>();
        ArrayList<SbOutput> outputsList = new ArrayList<SbOutput>();
        if (audioUnit == null || audioUnit.hasMidiInput()) {
            if (midiInput == null) {
                midiInput = (cachedMidiInput != null ? cachedMidiInput : new SbMidiInputImpl( 
                        SgEngine.getInstance().getResourceBundle().getString( "midi.input" ) ));
            }
        } else {
            if (midiInput != null) {
                if (midiInput.output != null) {
                    SbMidiOutput out = midiInput.output;
                    midiInput.disconnect();
                    out.disconnect();
                }
            }
            cachedMidiInput = midiInput;
            midiInput = null;
        }
        if (midiInput != null) {
            inputsList.add( midiInput );
        }
        if (audioUnit == null || audioUnit.hasMidiOutput()) {
            if (midiOutput == null) {
                midiOutput = (cachedMidiOutput != null ? cachedMidiOutput : new SbMidiOutputImpl(
                        SgEngine.getInstance().getResourceBundle().getString( "midi.output" ) ));
            }
        } else {
            if (midiOutput != null) {
                if (midiOutput.in != null) {
                    SbMidiInput in = midiOutput.in;
                    in.disconnect();
                    midiOutput.disconnect();
                }
            }
            cachedMidiOutput = midiOutput;
            midiOutput = null;
        }
        
        if (midiOutput != null) {
            outputsList.add( midiOutput );
        }
        
        // create audio outputs
        if (audioUnit != null) {
            if (audioOutput == null) {
                audioOutput = new SbAudioOutputImpl();
            }
            audioOutput.audioFormat = AudioToolkit.getDefaultAudioFormat( audioUnit.getNumOutputs() );
        } else {
            audioOutput = null;
        }
        if (audioUnit != null && audioUnit.getNumOutputs() > 0 && audioOutput != null) {
            outputsList.add( audioOutput );
        }
        if (audioUnit != null) {
            if (audioInput == null) {
                audioInput = new SbAudioInputImpl();
            }
            audioInput.numInputs = audioUnit.getNumInputs();
            audioInput.numOutputs = audioUnit.getNumOutputs();
        } else {
            audioInput = null;
        }
        if (audioInput != null) {
            audioInput.visible = (audioUnit != null && audioUnit.getNumInputs() > 0);
            if (audioInput.visible) {
                inputsList.add( audioInput );
            }
        }
        inputs = new SbInput[inputsList.size()];
        inputsList.toArray( inputs );
        outputs = new SbOutput[outputsList.size()];
        outputsList.toArray( outputs );
    }
    
    public String getAudioUnitName() {
        return (audioUnit != null ? audioUnit.getName() : (pluginName != null ? pluginName : "Unknown"));
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return (name != null ? name : getAudioUnitName());
    }
    
    public String getType() {
        return "audioUnit";
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

    public AudioUnit getAudioUnit() {
        return audioUnit;
    }

    public String toString() {
        return "AudioUnitImpl " + name + " audioUnit.name = " +
            getAudioUnitName()  + " parentSoundbus = " +
            parent + ", numOutputs = " + getOutputs().length +
            ", numInputs = " + getInputs().length + ", open = " + open;
    }
    

    private void connectToInput(
            final SbOutput output, final SbInput input, boolean fireEvent )
    throws CannotConnectException {
        disconnectFromInput( output, input );
        connectionMap.put( input, output );
        //System.out.println( "input.getConnectedOutput() = " + input.getConnectedOutput() );
        if (input.getConnectedOutput() == output && fireEvent) {
            parent.fireNodesConnectedEvent( new SoundbusNodesConnectionEvent( parent, input, output ) );
        }
    }
    
    private void disconnectFromInput( SbOutput out, SbInput in ) {
        if (in != null && out != null && in.getConnectedOutput() == null) {
            parent.fireNodesDisconnectedEvent(
                    new SoundbusNodesConnectionEvent( parent, in, out ) );
        }
        connectionMap.remove( in );
    }
    
    private void sCloseImpl( Soundbus soundbus ) {
        soundbus.removeSoundbusListener( sbListener );
    }
    public void closeImpl() throws SoundbusException {
        sCloseImpl( parent );
        open = false;
        SilenceAudioDataPump dataPump = this.dataPump;
        if (dataPump != null) {
            System.out.println( "AudioUnitImpl: dataPump running, stopping..." );
            dataPump.stop();
            this.dataPump = null;
        }
        AudioUnit audioUnit = this.audioUnit;
        if (audioUnit != null) {
            if (listener != null) {
                audioUnit.removeAudioUnitListener( listener );
            }
        }
        
        for (int i = 0; i < audioUnit.getNumParams(); i++) {
            if (audioUnit.canParameterBeAutomated( i )) {
                try {
                    float value = audioUnit.getParameter( i );
                    //System.out.println( "save: storing parameter " + i +
                    //        vstPlugin.getParameterName( i ) + ", value " + value );
                    putClientProperty( "parameter" + i + audioUnit.getParameterName( i ), Float.toString( value ) );
                } catch (NumberFormatException nfex) {
                }
            }
        }
    }
    
    public void destroyImpl() throws SoundbusException {
        if (audioUnit != null) {
            audioUnit.close();
        }
    }
    
    public boolean isOpenImpl() {
        return open;
    }

    private SoundbusListener sbListener = new SoundbusAdapter() {
        public void tempoChanged( SoundbusEvent e ) {
            AudioUnit audioUnit = AudioUnitNodeImpl.this.audioUnit;
            if (audioUnit != null) {
                audioUnit.setTempo( e.getSoundbus().getTempo() );
            }
        }
    };
    private void sOpenImpl( Soundbus soundbus ) {
        soundbus.addSoundbusListener( sbListener );
    }
    
    public void openImpl() throws SoundbusException {
        sOpenImpl( parent );
        // create audio input data pump if no input device present
        int channels = (audioUnit != null ? audioUnit.getNumOutputs() : 0);
        AudioFormat format = AudioToolkit.getDefaultAudioFormat( channels );
        if (audioInput != null && audioOutput != null &&
                audioInput.getConnectedOutput() == null &&
                audioOutput.getConnectedInput() != null) {
            System.out.println( "AudioFormat.frameSize=" + format.getFrameSize() );
            SilenceAudioDataPump dataPump = this.dataPump;
            if (dataPump != null) {
                dataPump.stop();
            }
            dataPump = new SilenceAudioDataPump( format, audioInput );
            dataPump.start();
            this.dataPump = dataPump;
        }
        
        if (midiOutput != null && midiOutput.getConnectedInput() instanceof SbMidiInput) {
            final SbMidiInput _input =  (SbMidiInput) midiOutput.getConnectedInput();
            final SbMidiOutput _output = midiOutput;
            listener = new AudioUnitListener() {
                MidiMessage m = new ShortMessage();
                public void process( AudioUnitEvent[] events, AudioUnit plugin ) {
                    for (int i = 0; i < events.length; i++) {
                        // TODO: get MIDI event from AU event
                        _input.receive( m, _output );
                    }
                }
            };
            if (audioUnit != null) {
                audioUnit.addAudioUnitListener( listener );
            }
        }
        if (audioUnit != null) {
            audioUnit.setAudioFormat( format );
            System.out.println( "set audio format: " + format );
            audioUnit.setBlockSize( AudioToolkit.getBufferSize( format ) / format.getFrameSize() );
            for (int i = 0; i < audioUnit.getNumParams(); i++) {
                String f = getClientProperty( "parameter" + i + audioUnit.getParameterName( i ) );
                if (f != null && audioUnit.canParameterBeAutomated( i )) {
                    try {
                        float value = Float.parseFloat( f );
                        //System.out.println( "restore: setting parameter " + i +
                        //        vstPlugin.getParameterName( i ) + " to " + value );
                        audioUnit.setParameter( i, value );
                    } catch (NumberFormatException nfex) {
                    }
                }
            }
        }
        
        if (audioInput != null) {
            audioInput.open();
        }
        if (audioOutput != null) {
            audioOutput.open();
        }
        
        open = true;
    }


    // Audio input connector impl
    class SbAudioInputImpl implements SbAudioInput {
        SbAudioOutput output;
        AudioFormat audioFormat;
        boolean visible;
        int numInputs;
        int numOutputs;
        
        SbAudioInputImpl() {
            visible = true;
        }
        
        void open() {
            // check if input data has to be translated
        }
        
        public AudioFormat getAudioFormat() {
            if (audioFormat == null) {
                audioFormat = AudioToolkit.getDefaultAudioFormat( numInputs );
            }
            return audioFormat;
        }
        
        public String getName() {
            return AudioToolkit.getAudioInputName( AudioToolkit.getDefaultAudioFormat( numInputs ) );
        }

        public String getDescription() {
            return AudioToolkit.getAudioInputDescription( AudioToolkit.getDefaultAudioFormat( numInputs ) );
        }

        public void receive( byte[] inputData, int offset, int length, AudioDataPump pump ) {
            if (audioFormat == null) {
                audioFormat = pump.getAudioFormat();
            }
            if (!open || audioUnit == null) {
                return;
            }
            
            audioUnit.process(
                    inputData,
                    audioFormat.getChannels(),
                    audioOutput.audioFormat.getChannels(),
                    audioFormat.getFrameSize(),
                    audioFormat.isBigEndian(),
                    audioFormat.getEncoding() );

            if (audioOutput.in != null) {
                audioOutput.in.receive( inputData, offset, length, audioOutput );
            }
        }

        public void connect( SbOutput output ) throws CannotConnectException {
            if (!(output instanceof SbAudioOutput)) {
                throw new CannotConnectException( "Incompatible types" );
            }
            if (parent.isOpen()) {
                throw new IllegalStateException( "Cannot connect: Parent soundbus is open" );
            }
            this.output = (SbAudioOutput) output;
        }
        
        public void disconnect() {
            if (parent.isOpen()) {
                throw new IllegalStateException( "Cannot connect: Parent soundbus is open" );
            }
            this.output = null;
        }

        public SbNode getSbNode() {
            return AudioUnitNodeImpl.this;
        }

        public SbOutput getConnectedOutput() {
            return output;
        }

        public boolean canReceive( AudioFormat format ) {
            return true; // TODO: ask AU
        }

        public void setAudioFormat( AudioFormat format ) {
            this.audioFormat = format;
        }

        public boolean isRealtimeSynchonous() {
            return true; // TODO: ask AU
        }

        public boolean isRealtimeOnly() {
            return true; // TODO: ask AU
        }
    }

    // Audio output connector impl
    class SbAudioOutputImpl implements SbAudioOutput {

        SbAudioInput in;
        AudioFormat audioFormat;
        
        void open() {
        }
        
        public AudioFormat getAudioFormat() {
            return audioFormat;
        }
        
        public String getName() {
            return AudioToolkit.getAudioOutputName( audioFormat );
        }

        public String getDescription() {
            return AudioToolkit.getAudioOutputDescription( audioFormat );
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
            disconnectFromInput( this, in );
            in = null;
        }

        public SbNode getSbNode() {
            return AudioUnitNodeImpl.this;
        }
    }
    
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
            if (audioUnit != null) {
                audioUnit.processMidi( m );
            }
        }
        public void connect( SbOutput output ) throws CannotConnectException {
            if (!(output instanceof SbMidiOutput)) {
                throw new CannotConnectException( "Incompatible types" );
            }
            if (parent.isOpen()) {
                throw new IllegalStateException( "Cannot connect: Parent soundbus is open" );
            }
            this.output = (SbMidiOutput) output;
        }
        public void disconnect() {
            if (parent.isOpen()) {
                throw new IllegalStateException( "Cannot connect: Parent soundbus is open" );
            }
            this.output = null;
        }
        public SbNode getSbNode() {
            return AudioUnitNodeImpl.this;
        }
        public SbOutput getConnectedOutput() {
            return output;
        }
    }
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
            disconnectFromInput( this, in );
            in = null;
        }
        public SbNode getSbNode() {
            return AudioUnitNodeImpl.this;
        }
    }

    public Map<String,String> getParameters() {
        return Collections.singletonMap( "audioUnit", (audioUnit != null ? audioUnit.getName() : pluginName ) );
    }
    public void setParameters( Map<String, String> parameters ) {
        if (parameters != null && parameters.containsKey( "audioUnit" )) {
            pluginName = parameters.get( "audioUnit" );
            AudioUnitDescriptor desc = AUContainer.getInstance().getAudioUnitDescriptorByName( pluginName );
            AudioUnit plugin = null;
            if (desc != null) {
                try {
                    plugin = desc.createPlugin();
                } catch (AudioUnitNotAvailableException e) {
                    e.printStackTrace();
                }
            }
            setAudioUnit( plugin );
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