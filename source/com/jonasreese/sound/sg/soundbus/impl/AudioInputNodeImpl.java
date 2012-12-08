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
import javax.sound.sampled.TargetDataLine;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.audio.AudioDeviceDescriptor;
import com.jonasreese.sound.sg.audio.AudioToolkit;
import com.jonasreese.sound.sg.soundbus.AudioInputNode;
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
public class AudioInputNodeImpl implements AudioInputNode, NodeImpl {
    private SoundbusImpl parent;
    private String name;
    private SbOutput[] outputs;
    private AudioDeviceDescriptor audioDevice;
    private Mixer openDevice;
    private TargetDataLine line;

    private SbAudioInput connectedInput;
    private SbAudioOutputImpl connectedOutput;
    
    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;

    public AudioInputNodeImpl( SoundbusImpl parent, String name ) {
        this.parent = parent;
        clientProperties = new HashMap<String,String>();
        outputs = new SbOutput[] {
                new SbAudioOutputImpl()
        };
        propertyChangeSupport = new PropertyChangeSupport( this );
    }

    public AudioDeviceDescriptor getAudioDevice() {
        return audioDevice;
    }

    public void setAudioDevice( AudioDeviceDescriptor audioDevice )
            throws IllegalStateException {
        if (isOpenImpl()) {
            throw new IllegalStateException( "Cannot set Audio device: Soundbus is open" );
        }
        if (this.audioDevice == audioDevice ||
                (audioDevice != null && this.audioDevice != null &&
                        audioDevice.getId().equals( this.audioDevice.getId() ))) {
            return;
        }
        AudioDeviceDescriptor oldDevice = this.audioDevice;
        this.audioDevice = audioDevice;
        propertyChangeSupport.firePropertyChange( "audioDevice", oldDevice, audioDevice );
        parent.getSoundbusDescriptor().setChanged( true );
    }

    public SbInput[] getInputs() {
        return NO_INPUTS;
    }

    public String getName() {
        return name;
    }
    
    public String getType() {
        return "audioInput";
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
    


    public synchronized void openImpl() throws SoundbusException {
        final SbAudioInput input = connectedInput;
        final SbAudioOutput output = connectedOutput;
        if (input == null || output == null) {
            return;
        }
        try {
            System.out.println( "AudioInputNodeImpl.connectToInput() : opening Audio device" );
            AudioDeviceDescriptor audioDeviceDescriptor = getAudioDevice();
            if (audioDeviceDescriptor == null) {
                throw new SoundbusException(
                        SgEngine.getInstance().getResourceBundle().getString(
                                "soundbus.node.audio.input.noInputDeviceSpecified" ) );
            }
            Mixer openDevice = AudioToolkit.getAudioDevice( audioDeviceDescriptor );
            final AudioFormat format = AudioToolkit.getDefaultAudioFormat();
            Line l = openDevice.getLine( new DataLine.Info( TargetDataLine.class, format ) );
            if (!(l instanceof TargetDataLine)) {
                throw new LineUnavailableException( "Invalid data line type" );
            }
            line = (TargetDataLine) l;
            line.open();
            if (!openDevice.isOpen()) {
                openDevice.open();
                this.openDevice = openDevice;
            }
            line.start();
            Thread thread = new Thread( "Audio I/O" ) {
                public void run() {
                    TargetDataLine line = AudioInputNodeImpl.this.line;
                    int bufferSize = AudioToolkit.getBufferSize(format);
                    byte[] buffer = new byte[bufferSize];
                    System.out.println( "audio buffer size is " + buffer.length + " bytes" );
                    while (line.isOpen()) {
                        int read = line.read( buffer, 0, buffer.length );
                        connectedInput.receive( buffer, 0, read, output );
                    }
                }
            };
            thread.start();
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
            throw new CannotConnectException( e );
        }
    }

    public void destroyImpl() throws SoundbusException {
    }

    public synchronized void closeImpl() throws SoundbusException {
        if (line != null) {
            try {
                System.out.println( "AudioInputNodeImpl.disconnectFromInput() : closing line" );
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
            try {
                System.out.println( "AudioInputNodeImpl.disconnectFromInput() : closing device" + openDevice );
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
    
    public synchronized boolean isOpenImpl() {
        return (openDevice != null);
    }

    // Audio output connector impl
    class SbAudioOutputImpl implements SbAudioOutput {

        SbAudioInput in;

        public AudioFormat getAudioFormat() {
            if (line == null) {
                return null;
            }
            return line.getFormat();
        }
        
        public String getName() {
            return AudioToolkit.getAudioOutputName( AudioToolkit.getDefaultAudioFormat() );
        }
        
        public String getOutputId() {
            return "output_1";
        }
        
        public String getDescription() {
            return AudioToolkit.getAudioOutputDescription( AudioToolkit.getDefaultAudioFormat() );
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
            return AudioInputNodeImpl.this;
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
        return Collections.singletonMap( "audioDevice", audioDevice.getId().getIdString() );
    }
    
    public void setParameters( Map<String, String> parameters ) {
        if (parameters != null && parameters.containsKey( "audioDevice" )) {
            setAudioDevice( AudioToolkit.getAudioInputDeviceList().getDescriptorById(
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
