/*
 * Created on 20.11.2008
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import javax.sound.sampled.AudioFormat;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.audio.AudioToolkit;
import com.jonasreese.sound.sg.soundbus.CannotConnectException;
import com.jonasreese.sound.sg.soundbus.NetworkAudioInputNode;
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
public class NetworkAudioInputNodeImpl implements NetworkAudioInputNode, NodeImpl {
    
    public static final int PACKET_SIZE = 4096;
    public static final int QUEUE_SIZE = 16;
    
    private ArrayBlockingQueue<DatagramPacket> queue;
    private SoundbusImpl parent;
    private String name;
    private SbOutput[] outputs;
    private int port;
    private DatagramSocket socket;
    private AudioFormat format;

    private SbAudioInput connectedInput;
    private SbAudioOutputImpl connectedOutput;
    private boolean open;
    
    private Map<String,String> clientProperties;
    private PropertyChangeSupport propertyChangeSupport;
    private PropertyChangeListener audioFormatChangeListener;

    public NetworkAudioInputNodeImpl( SoundbusImpl parent, String name ) {
        this.parent = parent;
        open = false;
        queue = new ArrayBlockingQueue<DatagramPacket>( QUEUE_SIZE );
        clientProperties = new HashMap<String,String>();
        outputs = new SbOutput[] {
                new SbAudioOutputImpl()
        };
        propertyChangeSupport = new PropertyChangeSupport( this );
        format = AudioToolkit.getDefaultAudioFormat();
        audioFormatChangeListener = new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent evt ) {
                String n = evt.getPropertyName();
                if (n.startsWith( "defaultAudioFormat" )) {
                    format = AudioToolkit.getDefaultAudioFormat();
                }
            }
        };
        SgEngine.getInstance().getProperties().addPropertyChangeListener( audioFormatChangeListener );
    }

    public int getPort() {
        return port;
    }

    public void setPort( int port )
            throws IllegalStateException {
        if (isOpenImpl()) {
            throw new IllegalStateException( "Cannot set Audio device: Soundbus is open" );
        }
        if (this.port == port) {
            return;
        }
        int oldPort = this.port;
        this.port = port;
        propertyChangeSupport.firePropertyChange( "port", oldPort, port );
        parent.getSoundbusDescriptor().setChanged( true );
    }

    public SbInput[] getInputs() {
        return NO_INPUTS;
    }

    public String getName() {
        return name;
    }
    
    public String getType() {
        return "networkAudioInput";
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
    
    public AudioFormat getAudioFormat() {
        return format;
    }
    
    public synchronized void setAudioFormat( AudioFormat format ) {
        if (isOpenImpl()) {
            throw new IllegalStateException( "Cannot set audio format: Soundbus is open" );
        }
        if (format == null) {
            format = AudioToolkit.getDefaultAudioFormat();
        }
        if (!AudioToolkit.isSameAudioFormat( format, this.format )) {
            AudioFormat oldFormat = this.format;
            this.format = format;
            propertyChangeSupport.firePropertyChange( "audioFormat", oldFormat, format );
            parent.getSoundbusDescriptor().setChanged( true );
        }
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
            System.out.println( "NetworkAudioInputNodeImpl.openImpl() : creating UDP socket" );

            socket = new DatagramSocket( port );
            socket.setBroadcast( true );
            final DatagramPacket[] datagramPackets = new DatagramPacket[QUEUE_SIZE];
            queue.clear();
            for (int i = 0; i < datagramPackets.length; i++) {
                byte[] b = new byte[PACKET_SIZE];
                datagramPackets[i] = new DatagramPacket( b, b.length );
            }
            Thread audioInput = new Thread( "Network audio input" ) {
                public void run() {
                    try {
                        int i = 0;
                        while (isOpenImpl()) {
                            DatagramPacket p = datagramPackets[i++];
                            socket.receive( p );
                            boolean b = queue.offer( p );
                            Thread.yield();
                            if (!b) {
                                //System.out.println( "could not add packet to queue: " + queue.size() );
                            }
                            if (i >= QUEUE_SIZE) {
                                i = 0;
                            }
                        }
                    } catch (SocketException sex) {
                        // assume socket was closed
                    } catch (IOException ioex) {
                        ioex.printStackTrace();
                    }
                }
            };
            Thread audioForward = new Thread( "Network audio forward" ) {
                public void run() {
                    while (isOpenImpl()) {
                        try {
                            DatagramPacket p;
                            p = queue.take();
                            if (p != null) {
                                input.receive( p.getData(), p.getOffset(), p.getLength(), output );
                            }
                            Thread.yield();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            
            open = true;
            audioForward.start();
            audioInput.start();
        } catch (Exception e) {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            throw new CannotConnectException( e );
        }
    }

    public void destroyImpl() throws SoundbusException {
        SgEngine.getInstance().getProperties().removePropertyChangeListener( audioFormatChangeListener );
    }

    public void closeImpl() throws SoundbusException {
        DatagramSocket s = socket;
        if (s != null) {
            s.close();
            socket = null;
        }
        open = false;
    }
    
    public boolean isOpenImpl() {
        return open;
    }

    // Audio output connector impl
    class SbAudioOutputImpl implements SbAudioOutput {

        SbAudioInput in;

        public AudioFormat getAudioFormat() {
            return format;
        }
        
        public String getName() {
            return AudioToolkit.getAudioOutputName( AudioToolkit.getDefaultAudioFormat() );
        }
        
        public String getDescription() {
            return AudioToolkit.getAudioOutputDescription( AudioToolkit.getDefaultAudioFormat() );
        }
        
        public String getOutputId() {
            return "output_1";
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
            return NetworkAudioInputNodeImpl.this;
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
        Map<String, String> p = new HashMap<String, String>();
        p.put( "port", Integer.toString( port ) );
        AudioToolkit.saveAudioFormat( p, format );
        return p;
    }
    
    public void setParameters( Map<String, String> parameters ) {
        if (parameters != null) {
            if (parameters.containsKey( "port" )) {
                String port = parameters.get( "port" );
                if (port != null) {
                    setPort( Integer.parseInt( port ) );
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
}
