package com.jonasreese.sound.sg.soundbus.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Mixer;

import com.jonasreese.sound.sg.audio.AudioDataPump;
import com.jonasreese.sound.sg.audio.AudioToolkit;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.NetworkAudioOutputNode;
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
public class NetworkAudioOutputNodeImpl implements NetworkAudioOutputNode, NodeImpl {
    
    public static final int DEFAULT_PORT = 9545;
    public static final String DEFAULT_DESTINATION = "255.255.255.255";
    
    private SoundbusImpl parent;
    private String name;
    private SbInput[] inputs;
    private Mixer openDevice;
    
    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;

    private SbAudioInputImpl connectedInput;
    private SbAudioOutput connectedOutput;

    private DatagramSocket socket;
    private InetAddress broadcastAddress;
    private int port;
    private String destination;

    private AudioFormat targetFormat;
    
    public NetworkAudioOutputNodeImpl( SoundbusImpl parent, String name ) {
        this.parent = parent;
        
        port = DEFAULT_PORT;
        destination = DEFAULT_DESTINATION;
        
        clientProperties = new HashMap<String,String>();
        propertyChangeSupport = new PropertyChangeSupport( this );
        inputs = new SbInput[] {
                new SbAudioInputImpl()
        };
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
        return "networkAudioOutput";
    }

    public Soundbus getSoundbus() {
        return parent;
    }

    public AudioFormat getAudioFormat() {
        if (targetFormat == null) {
            targetFormat = AudioToolkit.getDefaultAudioFormat();
        }
        return targetFormat;
    }
    
    public synchronized void setAudioFormat( AudioFormat format ) {
        if (isOpenImpl()) {
            throw new IllegalStateException( "Cannot set audio format: Soundbus is open" );
        }
        if (format == null) {
            this.targetFormat = AudioToolkit.getDefaultAudioFormat();
        }
        if (!AudioToolkit.isSameAudioFormat( format, this.targetFormat )) {
            AudioFormat oldFormat = this.targetFormat;
            this.targetFormat = format;
            propertyChangeSupport.firePropertyChange( "audioFormat", oldFormat, format );
            parent.getSoundbusDescriptor().setChanged( true );
        }
    }
    
    private void connectToOutput(
            SbAudioInputImpl input, SbAudioOutput output ) throws CannotConnectException {
        disconnectFromOutput();
        connectedInput = input;
        connectedOutput = output;

        if (output.getConnectedInput() == input) {
            parent.fireNodesConnectedEvent( new SoundbusNodesConnectionEvent( parent, input, output ) );
        }
    }
    
    private void disconnectFromOutput() {
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

    public void openImpl() throws SoundbusException {
        if (connectedInput == null || connectedOutput == null) {
            return;
        }
        
        try {
            socket = new DatagramSocket();
            socket.setBroadcast( true );
            broadcastAddress = InetAddress.getByName( getDestination() );
        } catch (SocketException e) {
            throw new SoundbusException( e );
        } catch (UnknownHostException e) {
            throw new SoundbusException( e );
        }
        
    }

    public void closeImpl() throws SoundbusException {
        if (socket != null) {
            socket.close();
        }
        socket = null;
    }
    
    public void destroyImpl() throws SoundbusException {
    }

    // Audio input connector impl
    class SbAudioInputImpl implements SbAudioInput {
        SbAudioOutput output;
        AudioFormat audioFormat = null;
        DatagramPacket p = new DatagramPacket( new byte[0], 0 );
        
        public String getName() {
            return AudioToolkit.getAudioInputName( NetworkAudioOutputNodeImpl.this.getAudioFormat() );
        }
        
        public AudioFormat getAudioFormat() {
            return audioFormat == null ? NetworkAudioOutputNodeImpl.this.getAudioFormat() : audioFormat;
        }
        
        public String getDescription() {
            return AudioToolkit.getAudioInputDescription( getAudioFormat() );
        }

        public void receive( byte[] audioData, int offset, int length, AudioDataPump pump ) {
            if (socket == null) {
                return;
            }
            p.setData( audioData, offset, length );
            p.setPort( port );
            p.setAddress( broadcastAddress );
            try {
                socket.send( p );
            } catch (IOException e) {
                e.printStackTrace();
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
            return NetworkAudioOutputNodeImpl.this;
        }

        public SbOutput getConnectedOutput() {
            return output;
        }

        public boolean canReceive( AudioFormat format ) {
            return AudioToolkit.isSameAudioFormat( getAudioFormat(), format );
        }

        public void setAudioFormat( AudioFormat format ) {
            this.audioFormat = format;
        }

        public boolean isRealtimeSynchonous() {
            return false;
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
        Map<String, String> map = new HashMap<String, String>( 2 );
        map.put( "destination", destination );
        map.put( "port", Integer.toString( port ) );
        if (targetFormat != null) { 
            AudioToolkit.saveAudioFormat( map, targetFormat );
        }
        return map;
    }

    public void setParameters( Map<String, String> parameters ) {
        if (parameters != null) {
            if (parameters.containsKey( "port" )) {
                String port = parameters.get( "port" );
                if (port != null) {
                    setPort( Integer.parseInt( port ) );
                }
            }
            if (parameters.containsKey( "destination" )) {
                String dest = parameters.get( "destination" );
                if (dest != null) {
                    setDestination( dest );
                }
            }
            setAudioFormat( AudioToolkit.restoreAudioFormat( parameters ) );
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

    public int getPort() {
        return port;
    }
    
    public void setPort( int port ) throws IllegalStateException {
        if (this.port == port) {
            return;
        }
        int oldPort = this.port;
        this.port = port;
        propertyChangeSupport.firePropertyChange( "port", oldPort, port );
        parent.getSoundbusDescriptor().setChanged( true );
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination( String destination ) throws IllegalStateException {
        if (this.destination == destination ||
                destination != null && destination.equals( this.destination )) {
            return;
        }
        String oldDestination = this.destination;
        this.destination = destination;
        propertyChangeSupport.firePropertyChange( "destination", oldDestination, destination );
        parent.getSoundbusDescriptor().setChanged( true );
    }
     
}
