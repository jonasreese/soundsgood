/*
 * Created on 28.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.impl;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.soundbus.AudioInputNode;
import com.jonasreese.sound.sg.soundbus.AudioOutputNode;
import com.jonasreese.sound.sg.soundbus.AudioSamplerNode;
import com.jonasreese.sound.sg.soundbus.AudioUnitNode;
import com.jonasreese.sound.sg.soundbus.MidiBranchNode;
import com.jonasreese.sound.sg.soundbus.MidiFilterNode;
import com.jonasreese.sound.sg.soundbus.MidiInputNode;
import com.jonasreese.sound.sg.soundbus.MidiJunctionNode;
import com.jonasreese.sound.sg.soundbus.MidiNoteCounterNode;
import com.jonasreese.sound.sg.soundbus.MidiOutputNode;
import com.jonasreese.sound.sg.soundbus.MidiSamplerNode;
import com.jonasreese.sound.sg.soundbus.NetworkAudioInputNode;
import com.jonasreese.sound.sg.soundbus.NetworkAudioOutputNode;
import com.jonasreese.sound.sg.soundbus.OSCNode;
import com.jonasreese.sound.sg.soundbus.PlugDescriptor;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.SoundbusEvent;
import com.jonasreese.sound.sg.soundbus.SoundbusException;
import com.jonasreese.sound.sg.soundbus.SoundbusListener;
import com.jonasreese.sound.sg.soundbus.SoundbusNodesConnectionEvent;
import com.jonasreese.sound.sg.soundbus.TempoNode;
import com.jonasreese.sound.sg.soundbus.UnknownNodeTypeException;
import com.jonasreese.sound.sg.soundbus.VstNode;
import com.jonasreese.util.AbstractEventRedirector;
import com.jonasreese.util.EventQueueHandler;
import com.jonasreese.util.EventRedirector;

/**
 * <p>
 * The default <code>Soundbus</code> implementation.
 * </p>
 * @author jonas.reese
 */
public class SoundbusImpl implements Soundbus {
    
    private SoundbusDescriptor soundbusDescriptor;
    private List<SbNode> nodes;
    private List<SoundbusListener> soundbusListeners;
    private Map<String,String> clientProperties;
    private boolean open;
    private boolean mute;
    private float tempo;
    private float originalTempo;
    private SoundbusEvent defaultEvent;
    private Object nodeAddSynchronizer;
    
    public SoundbusImpl( SoundbusDescriptor soundbusDescriptor ) {
        this.soundbusDescriptor = soundbusDescriptor;
        nodes = new ArrayList<SbNode>();
        soundbusListeners = new ArrayList<SoundbusListener>();
        clientProperties = new HashMap<String,String>();
        open = false;
        defaultEvent = new SoundbusEvent( this, null );
        mute = false;
        tempo = 120;
        nodeAddSynchronizer = new Object();
    }
    
    /**
     * Allow SoundbusDescriptor access for package, so that it's
     * changed status can be set by node implementations.
     * @return
     */
    SoundbusDescriptor getSoundbusDescriptor() {
        return soundbusDescriptor;
    }

    public SbNode[] getNodes() {
        SbNode[] ns = new SbNode[nodes.size()];
        nodes.toArray( ns );
        return ns;
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public boolean addNode( SbNode node ) {
        if (isOpen()) {
            throw new IllegalStateException( "Cannot add node to open soundbus" );
        }
        boolean b = nodes.add( node );
        if (b) {
            fireNodeAddedEvent( new SoundbusEvent( this, node ) );
        }
        return b;
    }
    
    public TempoNode addTempoNode() {
        TempoNode node = new TempoNodeImpl( this, "Tempo" );
        if (addNode( node )) {
            return node;
        }
        return null;
    }
    
    public MidiInputNode addMidiInputNode() {
        MidiInputNode node = new MidiInputNodeImpl( this, "MIDI Input" );
        if (addNode( node )) {
            return node;
        }
        return null;
    }
    
    public MidiOutputNode addMidiOutputNode() {
        MidiOutputNode node = new MidiOutputNodeImpl( this, "MIDI Output" );
        if (addNode( node )) {
            return node;
        }
        return null;
    }
    
    public MidiBranchNode addMidiBranchNode( int numOutputs ) {
        MidiBranchNode node = new MidiBranchNodeImpl( numOutputs, this, "MIDI Branch" );
        if (addNode( node )) {
            return node;
        }
        return null;
    }
    
    public MidiJunctionNode addMidiJunctionNode( int numInputs ) {
        MidiJunctionNode node = new MidiJunctionNodeImpl( numInputs, this, "MIDI Junction" );
        if (addNode( node )) {
            return node;
        }
        return null;
    }

    public MidiFilterNode addMidiFilterNode() {
        MidiFilterNode node = new MidiFilterNodeImpl( this, "MIDI Filter" );
        if (addNode( node )) {
            return node;
        }
        return null;
    }

    public MidiSamplerNode addMidiSamplerNode() {
        MidiSamplerNode node = new MidiSamplerNodeImpl( this, "MIDI Sampler" );
        if (addNode( node )) {
            return node;
        }
        return null;
    }

    public MidiNoteCounterNode addMidiNoteCounterNode() {
        MidiNoteCounterNode node = new MidiNoteCounterNodeImpl( this, "MIDI Note Counter" );
        if (addNode( node )) {
            return node;
        }
        return null;
    }

    public VstNode addVstPluginNode() {
        VstNode node = new VstNodeImpl( this );
        if (addNode( node )) {
            return node;
        }
        return null;
    }
    
    public AudioUnitNode addAudioUnitNode() {
        AudioUnitNode node = new AudioUnitNodeImpl( this );
        if (addNode( node )) {
            return node;
        }
        return null;
    }
    
    public AudioInputNode addAudioInputNode() {
        AudioInputNode node = new AudioInputNodeImpl( this, "Audio Input" );
        if (addNode( node )) {
            return node;
        }
        return null;
    }
    
    public NetworkAudioInputNode addNetworkAudioInputNode() {
        NetworkAudioInputNode node = new NetworkAudioInputNodeImpl( this, "Network Audio Input" );
        if (addNode( node )) {
            return node;
        }
        return null;
    }
    
    public AudioOutputNode addAudioOutputNode() {
        AudioOutputNode node = new AudioOutputNodeImpl( this, "Audio Output" );
        if (addNode( node )) {
            return node;
        }
        return null;
    }
    
    public NetworkAudioOutputNode addNetworkAudioOutputNode() {
        NetworkAudioOutputNode node = new NetworkAudioOutputNodeImpl( this, "Network Audio Output" );
        if (addNode( node )) {
            return node;
        }
        return null;
    }
    
    public AudioSamplerNode addAudioSamplerNode() {
        AudioSamplerNode node = new AudioSamplerNodeImpl( this, "Audio Sampler" );
        if (addNode( node )) {
            return node;
        }
        return null;
    }
    
    public OSCNode addOSCReceiverNode() {
        OSCNode node = new OSCNodeImpl( this, "OSC Receiver" );
        if (addNode( node )) {
            return node;
        }
        return null;
    }
    
    public SbNode addNode(
            String nodeType,
            Map<String,String> parameters,
            Map<String,String> clientProperties,
            List<PlugDescriptor> plugDescriptors )
    throws UnknownNodeTypeException, IllegalStateException {
        
        // avoid that node added event is dispatched before parameters
        // and properties have been set on the node
        synchronized (nodeAddSynchronizer) {
            // count inputs/outputs
            int inputCount = 0;
            for (PlugDescriptor pd : plugDescriptors) {
                if (pd.isInput()) {
                    inputCount++;
                }
            }
            int outputCount = plugDescriptors.size() - inputCount;
            
            SbNode node = null;
            if ("tempo".equals( nodeType )) {
                node = addTempoNode();
            } else if ("midiInput".equals( nodeType )) {
                node = addMidiInputNode();
            } else if ("midiOutput".equals( nodeType )) {
                node = addMidiOutputNode();
            } else if ("midiJunction".equals( nodeType )) {
                node = addMidiJunctionNode( inputCount );
            } else if ("midiBranch".equals( nodeType )) {
                node = addMidiBranchNode( outputCount );
            } else if ("midiFilter".equals( nodeType )) {
                node = addMidiFilterNode();
            } else if ("midiSampler".equals( nodeType )) {
                node = addMidiSamplerNode();
            } else if ("midiNoteCounter".equals( nodeType )) {
                node = addMidiNoteCounterNode();
            } else if ("audioInput".equals( nodeType )) {
                node = addAudioInputNode();
            } else if ("networkAudioInput".equals( nodeType )) {
                node = addNetworkAudioInputNode();
            } else if ("audioOutput".equals( nodeType )) {
                node = addAudioOutputNode();
            } else if ("networkAudioOutput".equals( nodeType )) {
                node = addNetworkAudioOutputNode();
            } else if ("audioSampler".equals( nodeType )) {
                node = addAudioSamplerNode();
            } else if ("vst".equals( nodeType )) {
                node = addVstPluginNode();
            } else if ("audioUnit".equals( nodeType )) {
                node = addAudioUnitNode();
            } else if ("osc".equals( nodeType )) {
                node = addOSCReceiverNode();
            } else {
                throw new UnknownNodeTypeException( "Unknown node type: " + nodeType );
            }
            
            if (parameters != null && !parameters.isEmpty()) {
                try {
                    node.setParameters( parameters );
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (clientProperties != null && !clientProperties.isEmpty()) {
                for (String key : clientProperties.keySet()) {
                    node.putClientProperty( key, clientProperties.get( key ) );
                }
            }
            
            return node;
        }
    }

    
    public boolean removeNode( SbNode node ) {
        if (isOpen()) {
            throw new IllegalStateException( "Cannot remove node from open soundbus" );
        }
        boolean b = (nodes.remove( node ));
        if (b) {
            fireNodeRemovedEvent( new SoundbusEvent( node.getSoundbus(), node ) );
        }
        return b;
    }

    public synchronized void open() throws SoundbusException {
        if (open) {
            return;
        }
        this.originalTempo = tempo;
        SoundbusException sex = null;
        int i;
        for (i = 0; i < nodes.size() && sex == null; i++) {
            SbNode n = nodes.get( i );
            if (n instanceof NodeImpl) {
                try {
                    ((NodeImpl) n).openImpl();
                } catch (SoundbusException moreSex) {
                    sex = moreSex;
                } catch (RuntimeException rex) {
                    rex.printStackTrace();
                    sex = new SoundbusException( rex );
                }
                if (sex != null) {
                    sex.setNode( n );
                }
            }
        }
        // if exception occurred, close nodes that have already been opened
        if (sex != null) {
            for (i--; i >= 0; i--) {
                SbNode n = nodes.get( i );
                if (n instanceof NodeImpl) {
                    try {
                        ((NodeImpl) n).closeImpl();
                    } catch (SoundbusException ignored) {
                    }
                }
            }
            fireSoundbusClosedEvent( defaultEvent );
            throw sex;
        }
        open = true;
        fireSoundbusOpenedEvent( defaultEvent );
    }

    public synchronized void close() throws SoundbusException {
        if (!open) {
            return;
        }
        SoundbusException sex = null;
        for (Iterator<SbNode> iter = nodes.iterator(); iter.hasNext(); ) {
            SbNode n = iter.next();
            if (n instanceof NodeImpl) {
                System.out.print( "closing " + n.getName() + "..." );
                try {
                    ((NodeImpl) n).closeImpl();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (ex instanceof SoundbusException) {
                        sex = (SoundbusException) ex;
                    } else {
                        sex = new SoundbusException( ex );
                    }
                }
                System.out.println( "done " );
            }
        }
        setTempo( originalTempo );
        open = false;
        fireSoundbusClosedEvent( defaultEvent );
        if (sex != null) {
            throw sex;
        }
    }
    
    public synchronized void destroy() {
        if (open) {
            try {
                close();
            } catch (SoundbusException e) {
                e.printStackTrace();
            }
        }
        for (Iterator<SbNode> iter = nodes.iterator(); iter.hasNext(); ) {
            SbNode n = iter.next();
            if (n instanceof NodeImpl) {
                try {
                    ((NodeImpl) n).destroyImpl();
                } catch (SoundbusException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized boolean isOpen() {
        return open;
    }

    public void setMute( boolean mute ) {
        if (this.mute == mute) {
            return;
        }
        this.mute = mute;
        fireMuteStatusChangedEvent( defaultEvent );
    }

    public boolean isMuted() {
        return mute;
    }
    
    public void setTempo( float tempo ) {
        if (this.tempo == tempo) {
            return;
        }
        this.tempo = tempo;
        fireTempoChangedEvent( defaultEvent );
    }
    
    public float getTempo() {
        return tempo;
    }

    public void putClientProperty( String property, String value ) {
        clientProperties.put( property, value );
        soundbusDescriptor.setChanged( true );
    }

    public boolean removeClientProperty( String property ) {
        boolean b = (clientProperties.remove( property ) != null);
        if (b) {
            soundbusDescriptor.setChanged( true );
        }
        return b;
    }

    public String getClientProperty( String property ) {
        return clientProperties.get( property );
    }

    public Map<String, String> getClientProperties() {
        return clientProperties;
    }

    public void addSoundbusListener( SoundbusListener l ) {
        synchronized (soundbusListeners) {
            if (!soundbusListeners.contains( l )) {
                soundbusListeners.add( l );
            }
        }
    }
    
    public SoundbusListener[] getSoundbusListeners() {
        synchronized (soundbusListeners) {
            SoundbusListener[] result = new SoundbusListener[soundbusListeners.size()];
            return soundbusListeners.toArray( result );
        }
    }

    public void removeSoundbusListener( SoundbusListener l ) {
        synchronized (soundbusListeners) {
            soundbusListeners.remove( l );
        }
    }

    
    protected void fireMuteStatusChangedEvent( SoundbusEvent e ) {
        soundbusDescriptor.setChanged( true );
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (soundbusListeners) {
            for (int i = 0; i < soundbusListeners.size(); i++) {
                EventRedirector erd = new AbstractEventRedirector( soundbusListeners.get( i ) ) {
                    public void redirectEvent( EventObject e ) {
                        ((SoundbusListener) getListener()).muteStatusChanged( (SoundbusEvent) e );
                    }
                };
                eqh.addQueueEntry( erd, e );
            }
        }
        eqh.processEvents();
    }
    
    protected void fireTempoChangedEvent( SoundbusEvent e ) {
        soundbusDescriptor.setChanged( true );
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (soundbusListeners) {
            for (int i = 0; i < soundbusListeners.size(); i++) {
                EventRedirector erd = new AbstractEventRedirector( soundbusListeners.get( i ) ) {
                    public void redirectEvent( EventObject e ) {
                        ((SoundbusListener) getListener()).tempoChanged( (SoundbusEvent) e );
                    }
                };
                eqh.addQueueEntry( erd, e );
            }
        }
        eqh.processEvents();
    }
    
    protected void fireNodeAddedEvent( SoundbusEvent e ) {
        soundbusDescriptor.setChanged( true );
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (soundbusListeners) {
            for (int i = 0; i < soundbusListeners.size(); i++) {
                EventRedirector erd = new AbstractEventRedirector( soundbusListeners.get( i ) ) {
                    public void redirectEvent( EventObject e ) {
                        // avoid that the event is dispatched before the node properties
                        // and parameters have been set
                        synchronized (nodeAddSynchronizer) {
                            ((SoundbusListener) getListener()).nodeAdded( (SoundbusEvent) e );
                        }
                    }
                };
                eqh.addQueueEntry( erd, e );
            }
        }
        eqh.processEvents();
    }
    
    protected void fireNodeRemovedEvent( SoundbusEvent e ) {
        soundbusDescriptor.setChanged( true );
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (soundbusListeners) {
            for (int i = 0; i < soundbusListeners.size(); i++) {
                EventRedirector erd = new AbstractEventRedirector( soundbusListeners.get( i ) ) {
                    public void redirectEvent( EventObject e ) {
                        ((SoundbusListener) getListener()).nodeRemoved( (SoundbusEvent) e );
                    }
                };
                eqh.addQueueEntry( erd, e );
            }
        }
        eqh.processEvents();
    }
    
    protected void fireSoundbusOpenedEvent( SoundbusEvent e ) {
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (soundbusListeners) {
            for (int i = 0; i < soundbusListeners.size(); i++) {
                EventRedirector erd = new AbstractEventRedirector( soundbusListeners.get( i ) ) {
                    public void redirectEvent( EventObject e ) {
                        ((SoundbusListener) getListener()).soundbusOpened( (SoundbusEvent) e );
                    }
                };
                eqh.addQueueEntry( erd, e );
            }
        }
        eqh.processEvents();
    }
    
    protected void fireSoundbusClosedEvent( SoundbusEvent e ) {
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (soundbusListeners) {
            for (int i = 0; i < soundbusListeners.size(); i++) {
                EventRedirector erd = new AbstractEventRedirector( soundbusListeners.get( i ) ) {
                    public void redirectEvent( EventObject e ) {
                        ((SoundbusListener) getListener()).soundbusClosed( (SoundbusEvent) e );
                    }
                };
                eqh.addQueueEntry( erd, e );
            }
        }
        eqh.processEvents();
    }
    
    void fireNodesConnectedEvent( SoundbusNodesConnectionEvent e ) {
        soundbusDescriptor.setChanged( true );
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (soundbusListeners) {
            for (int i = 0; i < soundbusListeners.size(); i++) {
                EventRedirector erd = new AbstractEventRedirector( soundbusListeners.get( i ) ) {
                    public void redirectEvent( EventObject e ) {
                        ((SoundbusListener) getListener()).nodesConnected(
                                (SoundbusNodesConnectionEvent) e );
                    }
                };
                eqh.addQueueEntry( erd, e );
            }
        }
        eqh.processEvents();
    }

    void fireNodesDisconnectedEvent( SoundbusNodesConnectionEvent e ) {
        soundbusDescriptor.setChanged( true );
        EventQueueHandler eqh = SgEngine.getInstance().getEventQueue();
        synchronized (soundbusListeners) {
            for (int i = 0; i < soundbusListeners.size(); i++) {
                EventRedirector erd = new AbstractEventRedirector( soundbusListeners.get( i ) ) {
                    public void redirectEvent( EventObject e ) {
                        ((SoundbusListener) getListener()).nodesDisconnected(
                                (SoundbusNodesConnectionEvent) e );
                    }
                };
                eqh.addQueueEntry( erd, e );
            }
        }
        eqh.processEvents();
    }
}
