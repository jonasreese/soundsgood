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

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.audio.AudioDataPump;
import com.jonasreese.sound.sg.audio.AudioToolkit;
import com.jonasreese.sound.sg.audio.SilenceAudioDataPump;
import com.jonasreese.sound.sg.midi.MidiToolkit;
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
import com.jonasreese.sound.sg.soundbus.VstNode;
import com.jonasreese.sound.vstcontainer.VstContainer;
import com.jonasreese.sound.vstcontainer.VstEvent;
import com.jonasreese.sound.vstcontainer.VstEventListener;
import com.jonasreese.sound.vstcontainer.VstMidiEvent;
import com.jonasreese.sound.vstcontainer.VstPlugin;
import com.jonasreese.sound.vstcontainer.VstPluginDescriptor;
import com.jonasreese.sound.vstcontainer.VstPluginNotAvailableException;
import com.jonasreese.sound.vstcontainer.VstSpeakerArrangement;
import com.jonasreese.sound.vstcontainer.VstSpeakerProperties;

/**
 * @author jonas.reese
 */
public class VstNodeImpl implements VstNode, NodeImpl {
    private String name;
    private SoundbusImpl parent;
    private boolean open;
    private SbInput[] inputs;
    private SbOutput[] outputs;
    
    private VstPlugin vstPlugin;
    private String pluginName;
    
    private SbAudioInputImpl audioInput;
    private SbAudioOutputImpl audioOutput;
    private SbMidiInputImpl midiInput;
    private SbMidiOutputImpl midiOutput;
    private SbMidiInputImpl cachedMidiInput;
    private SbMidiOutputImpl cachedMidiOutput;
    private SilenceAudioDataPump dataPump;
    private VstEventListener listener;
    
    private Map<SbInput,SbOutput> connectionMap;
    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;
    


    public VstNodeImpl( SoundbusImpl parent ) {
        this.parent = parent;
        this.name = null;
        open = false;
        connectionMap = new HashMap<SbInput,SbOutput>();
        clientProperties = new HashMap<String,String>();
        propertyChangeSupport = new PropertyChangeSupport( this );
        init();
    }
    
    public void setVstPlugin( VstPlugin vstPlugin ) {
        if (isOpenImpl() || this.vstPlugin == vstPlugin) {
            return;
        }
        VstPlugin oldPlugin = this.vstPlugin;
        if (oldPlugin != null) {
            oldPlugin.close();
        }
        this.vstPlugin = vstPlugin;
        init();
        propertyChangeSupport.firePropertyChange( "vstPlugin", oldPlugin, vstPlugin );
        parent.getSoundbusDescriptor().setChanged( true );
    }
    
    private void init() {
        if (vstPlugin != null) {
            vstPlugin.open();
        }
        ArrayList<SbInput> inputsList = new ArrayList<SbInput>();
        ArrayList<SbOutput> outputsList = new ArrayList<SbOutput>();
        if (vstPlugin == null || vstPlugin.canDo( VstPlugin.CANDO_PLUG_RECEIVE_VST_MIDI_EVENT ) >= 0) {
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
        if (vstPlugin == null || vstPlugin.canDo( VstPlugin.CANDO_PLUG_SEND_VST_MIDI_EVENT ) > 0) {
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
        if (vstPlugin != null) {
            if (audioOutput == null) {
                audioOutput = new SbAudioOutputImpl();
            }
            audioOutput.audioFormat = AudioToolkit.getDefaultAudioFormat( vstPlugin.getNumOutputs() );
        } else {
            audioOutput = null;
        }
        if (vstPlugin != null && vstPlugin.getNumOutputs() > 0 && audioOutput != null) {
            outputsList.add( audioOutput );
        }
        if (vstPlugin != null) {
            if (audioInput == null) {
                audioInput = new SbAudioInputImpl();
            }
            audioInput.replacing = vstPlugin.canProcessReplacing();
            audioInput.numInputs = vstPlugin.getNumInputs();
            audioInput.numOutputs = vstPlugin.getNumOutputs();
        } else {
            audioInput = null;
        }
        if (audioInput != null) {
            audioInput.visible = (vstPlugin != null && vstPlugin.getNumInputs() > 0);
            if (audioInput.visible) {
                inputsList.add( audioInput );
            }
        }
        inputs = new SbInput[inputsList.size()];
        inputsList.toArray( inputs );
        outputs = new SbOutput[outputsList.size()];
        outputsList.toArray( outputs );
    }
    
    public String getVstPluginName() {
        return (vstPlugin != null ? vstPlugin.getName() : (pluginName != null ? pluginName : "Unknown"));
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return (name != null ? name : getVstPluginName());
    }
    
    public String getType() {
        return "vst";
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

    public VstPlugin getVstPlugin() {
        return vstPlugin;
    }

    public String toString() {
        return "VstNodeImpl " + name + " vstPlugin.name = " +
            getVstPluginName()  + " parentSoundbus = " +
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
    
    private static void sCloseImpl( Soundbus soundbus ) {
        soundbus.removeSoundbusListener( sbListener );
    }
    public void closeImpl() throws SoundbusException {
        sCloseImpl( parent );
        open = false;
        SilenceAudioDataPump dataPump = this.dataPump;
        if (dataPump != null) {
            System.out.println( "VstNodeImpl: dataPump running, stopping..." );
            dataPump.stop();
            this.dataPump = null;
        }
        VstPlugin vstPlugin = this.vstPlugin;
        if (vstPlugin != null) {
            if (listener != null) {
                vstPlugin.removeVstEventListener( listener );
            }
            vstPlugin.stopProcess();
            vstPlugin.suspend();
        }
        
        for (int i = 0; i < vstPlugin.getNumParams(); i++) {
            if (vstPlugin.canParameterBeAutomated( i )) {
                try {
                    float value = vstPlugin.getParameter( i );
                    //System.out.println( "save: storing parameter " + i +
                    //        vstPlugin.getParameterName( i ) + ", value " + value );
                    putClientProperty( "parameter" + i + vstPlugin.getParameterName( i ), Float.toString( value ) );
                } catch (NumberFormatException nfex) {
                }
            }
        }
    }
    
    public void destroyImpl() throws SoundbusException {
        if (vstPlugin != null) {
            vstPlugin.close();
        }
    }
    
    public boolean isOpenImpl() {
        return open;
    }

    private static SoundbusListener sbListener = new SoundbusAdapter() {
        public void tempoChanged( SoundbusEvent e ) {
            VstContainer.getInstance().setTempo( e.getSoundbus().getTempo() );
        }
    };
    private static void sOpenImpl( Soundbus soundbus ) {
        soundbus.addSoundbusListener( sbListener );
    }
    
    public void openImpl() throws SoundbusException {
        sOpenImpl( parent );
        VstContainer.getInstance().setTempo( parent.getTempo() );
        // create audio input data pump if no input device present
        int channels = (vstPlugin != null ? vstPlugin.getNumOutputs() : 0);
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
            listener = new VstEventListener() {
                MidiMessage m = new ShortMessage();
                public void process( VstEvent[] events, VstPlugin plugin ) {
                    for (int i = 0; i < events.length; i++) {
                        _input.receive( m, _output );
                    }
                }
            };
            if (vstPlugin != null) {
                vstPlugin.addVstEventListener( listener );
            }
        }
        if (vstPlugin != null) {
            vstPlugin.setSampleRate( format.getSampleRate() );
            vstPlugin.setBlockSize( AudioToolkit.getBufferSize( format ) / format.getFrameSize() );
            VstSpeakerProperties[] speakers = new VstSpeakerProperties[] {
                    new VstSpeakerProperties( 0, 0, 10, "Left", VstSpeakerProperties.SPEAKER_LC ),
                    new VstSpeakerProperties( 0, 0, 10, "Right", VstSpeakerProperties.SPEAKER_RC )
            };
            for (int i = 0; i < vstPlugin.getNumParams(); i++) {
                String f = getClientProperty( "parameter" + i + vstPlugin.getParameterName( i ) );
                if (f != null && vstPlugin.canParameterBeAutomated( i )) {
                    try {
                        float value = Float.parseFloat( f );
                        //System.out.println( "restore: setting parameter " + i +
                        //        vstPlugin.getParameterName( i ) + " to " + value );
                        vstPlugin.setParameter( i, value );
                    } catch (NumberFormatException nfex) {
                    }
                }
            }
            VstSpeakerArrangement sp = new VstSpeakerArrangement(
                    VstSpeakerArrangement.SPEAKER_ARRANGEMENT_STEREO_CENTER, speakers );
            System.out.println( "setSpeakerArrangement() returns " + vstPlugin.setSpeakerArrangement( sp, sp ) );
            vstPlugin.resume();
            vstPlugin.startProcess();
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
        boolean replacing;
        boolean visible;
        int numInputs;
        int numOutputs;
        byte[] outputData;
        
        SbAudioInputImpl() {
            visible = true;
            outputData = null;
        }
        
        void open() {
            // check if input data has to be translated
        }
        
        public AudioFormat getAudioFormat() {
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
            if (!open || vstPlugin == null) {
                return;
            }
            
            byte[] outputData;
            if (this.outputData != null) {
                outputData = this.outputData;
            } else {
                outputData = inputData;
            }
            
            if (replacing) {
                vstPlugin.processReplacing(
                        inputData,
                        outputData,
                        audioFormat.getChannels(),
                        audioOutput.audioFormat.getChannels(),
                        audioFormat.getFrameSize(),
                        audioFormat.isBigEndian(),
                        audioFormat.getEncoding() );
            }
            if (audioOutput.in != null) {
                audioOutput.in.receive( outputData, offset, length, audioOutput );
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
            return VstNodeImpl.this;
        }

        public SbOutput getConnectedOutput() {
            return output;
        }

        public boolean canReceive( AudioFormat format ) {
            return true; // TODO: ask VST
        }

        public void setAudioFormat( AudioFormat format ) {
            this.audioFormat = format;
        }

        public boolean isRealtimeSynchonous() {
            return true; // TODO: ask VST
        }

        public boolean isRealtimeOnly() {
            return true; // TODO: ask VST
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
            return VstNodeImpl.this;
        }
    }
    
    class SbMidiInputImpl implements SbMidiInput {
        SbMidiOutput output;
        String name;
        VstMidiEvent[] events = new VstMidiEvent[1];
        SbMidiInputImpl( String name ) {
            this.name = name;
            events[0] = new VstMidiEvent();
        }
        public String getName() {
            return name;
        }
        public String getDescription() {
            return null;
        }
        public void receive( MidiMessage m, SbOutput output ) {
            if (vstPlugin != null) {
                MidiToolkit.javaMidiToVstMidi( m, events[0] );
                vstPlugin.processEvents( events );
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
            return VstNodeImpl.this;
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
            return VstNodeImpl.this;
        }
    }

    public Map<String,String> getParameters() {
        return Collections.singletonMap( "vstPlugin", (vstPlugin != null ? vstPlugin.getName() : pluginName ) );
    }
    public void setParameters( Map<String, String> parameters ) {
        if (parameters != null && parameters.containsKey( "vstPlugin" )) {
            pluginName = parameters.get( "vstPlugin" );
            VstPluginDescriptor desc = VstContainer.getInstance().getVstPluginDescriptorByName( pluginName );
            VstPlugin plugin = null;
            if (desc != null) {
                try {
                    plugin = desc.createPlugin();
                } catch (VstPluginNotAvailableException e) {
                    e.printStackTrace();
                }
            }
            setVstPlugin( plugin );
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