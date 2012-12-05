/*
 * Created on 28.05.2006
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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.Line.Info;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.audio.AudioDataPump;
import com.jonasreese.sound.sg.audio.AudioDeviceDescriptor;
import com.jonasreese.sound.sg.audio.AudioToolkit;
import com.jonasreese.sound.sg.soundbus.AudioOutputNode;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.SbAudioInput;
import com.jonasreese.sound.sg.soundbus.SbAudioOutput;
import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusException;
import com.jonasreese.sound.sg.soundbus.SoundbusNodesConnectionEvent;

/**
 * @author jonas.reese
 */
public class AudioOutputNodeImpl implements AudioOutputNode, NodeImpl {
    private SoundbusImpl parent;
    private String name;
    private SbInput[] inputs;
    private AudioDeviceDescriptor audioDevice;
    private Mixer openDevice;
    private SourceDataLine line;
    
    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;

    private SbAudioInputImpl connectedInput;
    private SbAudioOutput connectedOutput;
    
    public AudioOutputNodeImpl( SoundbusImpl parent, String name ) {
        this.parent = parent;
        clientProperties = new HashMap<String,String>();
        propertyChangeSupport = new PropertyChangeSupport( this );
        inputs = new SbInput[] {
                new SbAudioInputImpl()
        };
    }

    public AudioDeviceDescriptor getAudioDevice() {
        return audioDevice;
    }

    public void setAudioDevice( AudioDeviceDescriptor audioDevice ) throws IllegalStateException {
        if (isOpenImpl()) {
            throw new IllegalStateException( "Cannot set Audio device: Soundbus is open" );
        }
        if (this.audioDevice == audioDevice ||
                (audioDevice != null && this.audioDevice != null &&
                        audioDevice.getIdString().equals( this.audioDevice.getIdString() ))) {
            return;
        }
        AudioDeviceDescriptor oldDevice = this.audioDevice;
        this.audioDevice = audioDevice;
        propertyChangeSupport.firePropertyChange( "audioDevice", oldDevice, audioDevice );
        parent.getSoundbusDescriptor().setChanged( true );
    }

    public SbInput[] getInputs() {
        return inputs;
    }

    public SbOutput[] getOutputs() {
        return NO_OUTPUTS;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }
    
    public String getType() {
        return "audioOutput";
    }

    public Soundbus getSoundbus() {
        return parent;
    }

    private void connectToOutput(
            SbAudioInputImpl input, SbAudioOutput output ) throws CannotConnectException {
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
    
    public boolean isOpenImpl() {
        return (openDevice != null);
    }

    private void debugLineInfo( Info[] infoList ) {
        for (int i = 0; i < infoList.length; i++) {
            Info info = infoList[i];
            System.out.println( "info[" + i + "] = " + info + ", class " + info.getLineClass() );
            if (info instanceof DataLine.Info) {
                DataLine.Info dlInfo = (DataLine.Info) info;
                for (AudioFormat format : dlInfo.getFormats()) {
                    debugFormat( "  supported format: ", format );
                }
            }
        }
    }
    
    private void debugFormat( String s, AudioFormat format ) {
        System.out.println( s + format );
    }
    
    public void openImpl() throws SoundbusException {
        if (connectedInput == null || connectedOutput == null) {
            return;
        }
        try {
            System.out.println( "AudioOutputNodeImpl.connectToOutput() : opening Audio device" );
            AudioDeviceDescriptor audioDeviceDescriptor = getAudioDevice();
            if (audioDeviceDescriptor == null) {
                throw new SoundbusException(
                        SgEngine.getInstance().getResourceBundle().getString(
                                "soundbus.node.audio.output.noOutputDeviceSpecified" ) );
            }
            Mixer openDevice = AudioToolkit.getAudioDevice( audioDeviceDescriptor );
            debugLineInfo( openDevice.getSourceLineInfo() );
            AudioFormat format = connectedOutput.getAudioFormat();
            debugFormat( "selected format: ", format );
            ((SbAudioInputImpl) inputs[0]).audioFormat = format;
            Line l = openDevice.getLine( new DataLine.Info( SourceDataLine.class, format ) );
            if (!(l instanceof SourceDataLine)) {
                throw new LineUnavailableException( "Invalid data line type" );
            }
            line = (SourceDataLine) l;
            line.open();
            if (!openDevice.isOpen()) {
                openDevice.open();
                this.openDevice = openDevice;
            }
            line.start();
        } catch (Exception e) {
            if (line != null) {
                if (line.isActive()) {
                    line.stop();
                }
                if (line.isOpen()) {
                    line.close();
                }
            }
            if (openDevice != null && openDevice.isOpen()) {
                openDevice.close();
            }
            throw new SoundbusException( e );
        }
    }

    public void closeImpl() throws SoundbusException {
        if (line != null) {
            try {
                System.out.println( "AudioOutputNodeImpl.disconnectFromOutput() : closing line" );
                if (line.isActive()) {
                    line.stop();
                }
                if (line.isOpen()) {
                    line.close();
                }
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
        }
        if (openDevice != null) {
            System.out.println( "AudioOutputNodeImpl.disconnectFromOutput() : closing device " + openDevice );
            try {
                if (openDevice.isOpen()) {
                    openDevice.close();
                }
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }
        }
        line = null;
        openDevice = null;
    }
    
    public void destroyImpl() throws SoundbusException {
    }

    // Audio input connector impl
    class SbAudioInputImpl implements SbAudioInput {
        SbAudioOutput output;
        AudioFormat audioFormat;
        
        public String getName() {
            return AudioToolkit.getAudioInputName( AudioToolkit.getDefaultAudioFormat() );
        }
        
        public AudioFormat getAudioFormat() {
            return audioFormat;
        }
        
        public String getDescription() {
            return AudioToolkit.getAudioInputDescription( AudioToolkit.getDefaultAudioFormat() );
        }

        public void receive( byte[] audioData, int offset, int length, AudioDataPump pump ) {
            SourceDataLine line = AudioOutputNodeImpl.this.line;
            if (line != null && line.isOpen()) {
                if (pump.getAudioFormat().getChannels() == audioFormat.getChannels()) {
                    line.write( audioData, offset, length );
                } else {
                    // TODO: implement this!
                }
            }
        }

        public void connect( SbOutput output ) throws CannotConnectException {
            if (!(output instanceof SbAudioOutput)) {
                throw new CannotConnectException( "Incompatible types" );
            }
            if (parent.isOpen()) {
                throw new IllegalStateException( "Cannot connect: Parent soundbus is open" );
            }            this.output = (SbAudioOutput) output;
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
            return AudioOutputNodeImpl.this;
        }

        public SbOutput getConnectedOutput() {
            return output;
        }

        public boolean canReceive( AudioFormat format ) {
            return true;
        }

        public void setAudioFormat( AudioFormat format ) {
            this.audioFormat = format;
        }

        public boolean isRealtimeSynchonous() {
            return true;
        }

        public boolean isRealtimeOnly() {
            return true;
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
        AudioDeviceDescriptor audioDevice = this.audioDevice;
        if (audioDevice == null) {
            return null;
        }
        return Collections.singletonMap( "audioDevice", audioDevice.getIdString() );
    }

    public void setParameters( Map<String, String> parameters ) {
        if (parameters != null && parameters.containsKey( "audioDevice" )) {
            setAudioDevice( AudioToolkit.getAudioOutputDeviceList().getDescriptorById(
                    parameters.get( "audioDevice"  ) ) );
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
