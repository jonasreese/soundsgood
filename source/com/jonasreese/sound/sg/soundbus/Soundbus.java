/*
 * Created on 28.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * A <code>Soundbus</code> is a container for <code>SbNode</code> objects
 * (soundbus nodes). Soundbus nodes can be connected to each other, resulting
 * in a graph that has input and output nodes as it's leafs.
 * </p>
 * <p>
 * Please note that a <code>Soundbus</code> does not have graphical properties
 * itself. However, it can be provided with client data containing information
 * about how to display a <code>Soundbus</code> on the screen so that this
 * information can automatically be persisted using the
 * <code>SoundbusDescriptor</code>'s <code>save()</code> methods
 * </p>
 * @author jonas.reese
 */
public interface Soundbus {
    
    /**
     * Adds a <code>SoundbusListener</code> to this <code>Soundbus.</code>
     * @param l The <code>SoundbusListener</code> to be added. If it has
     * already been added, this method does nothing.
     */
    public void addSoundbusListener( SoundbusListener l );
    
    /**
     * Gets all registered <code>SoundbusListener</code>s.
     * @return An array with all registered listeners.
     */
    public SoundbusListener[] getSoundbusListeners();

    /**
     * Removes a <code>SoundbusListener</code> from this <code>Soundbus.</code>
     * @param l The <code>SoundbusListener</code> to be removed. If it is not
     * registered as a listener, this method does nothing.
     */
    public void removeSoundbusListener( SoundbusListener l );
    
    /**
     * Gets a copied array of all <code>SbNode</code>s contained in this
     * <code>Soundbus</code>.
     * @return All nodes as a newly created array.
     */
    public SbNode[] getNodes();
    
    /**
     * Gets the current node count.
     * @return The number of nodes in this <code>Soundbus</code>.
     */
    public int getNodeCount();
    
    /**
     * Adds the given <code>SbNode</code> to this soundbus. A newly added
     * node is initially not connected to any other node.
     * @param node The <code>SbNode</code> that shall be added.
     * @return <code>true</code> if the given node has been added successfuly,
     * <code>false</code> otherwise.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public boolean addNode( SbNode node );

    /**
     * Removes the given <code>SbNode</code> from this soundbus. 
     * @param node The node to be removed.
     * @return <code>true</code> if <code>node</code> was successfully removed,
     * <code>false</code> otherwise.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public boolean removeNode( SbNode node ) throws IllegalStateException;

    /**
     * Creates a new <code>TempoNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.<p>
     * @return The newly created <code>TempoNode</code>, or <code>null</code>
     * if no <code>TempoNode</code> was added to this <code>Soundbus</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public TempoNode addTempoNode() throws IllegalStateException;
    
    /**
     * Creates a new <code>MidiInputNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @return The newly created <code>MidiInputNode</code>, or <code>null</code>
     * if no <code>MidiInputNode</code> was added to this <code>Soundbus</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public MidiInputNode addMidiInputNode() throws IllegalStateException;
    
    /**
     * Creates a new <code>MidiOutputNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @return The newly created <code>MidiOutputNode</code>, or <code>null</code>
     * if no <code>MidiOutputNode</code> was added to this <code>Soundbus</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public MidiOutputNode addMidiOutputNode() throws IllegalStateException;
    
    /**
     * Creates a new <code>MidiBranchNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @param numOutputs The number of outputs the created <code>MidiBranchNode</code>
     * shall have.
     * @return The newly created <code>MidiBranchNode</code>, or <code>null</code>
     * if no <code>MidiBranchNode</code> was added to this <code>Soundbus</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public MidiBranchNode addMidiBranchNode( int numOutputs ) throws IllegalStateException;
    
    /**
     * Creates a new <code>MidiJunctionNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @param numOutputs The number of outputs the created <code>MidiJunctionNode</code>
     * shall have.
     * @return The newly created <code>MidiJunctionNode</code>, or <code>null</code>
     * if no <code>MidiJunctionNode</code> was added to this <code>Soundbus</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public MidiJunctionNode addMidiJunctionNode( int numOutputs ) throws IllegalStateException;

    /**
     * Creates a new <code>MidiFilterNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @return The newly created <code>MidiFilterNode</code>, or <code>null</code>
     * if no <code>MidiFilterNode</code> was added to this <code>Soundbus</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public MidiFilterNode addMidiFilterNode() throws IllegalStateException;

    /**
     * Creates a new <code>MidiSamplerNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @return The newly created <code>MidiSamplerNode</code>, or <code>null</code>
     * if no <code>MidiSamplerNode</code> was added to this <code>Soundbus</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public MidiSamplerNode addMidiSamplerNode() throws IllegalStateException;

    /**
     * Creates a new <code>MidiNoteCounterNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @return The newly created <code>MidiNoteCounterNode</code>, or <code>null</code>
     * if no <code>MidiNoteCounterNode</code> was added to this <code>Soundbus</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public MidiNoteCounterNode addMidiNoteCounterNode() throws IllegalStateException;

    /**
     * Creates a new <code>VstNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @return A newly created <code>VstNode</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public VstNode addVstPluginNode() throws IllegalStateException;

    /**
     * Creates a new <code>AudioUnitNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @return A newly created <code>AudioUnitNode</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public AudioUnitNode addAudioUnitNode() throws IllegalStateException;
    
    /**
     * Creates a new <code>AudioInputNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @return The newly created <code>AudioInputNode</code>, or <code>null</code>
     * if no <code>AudioInputNode</code> was added to this <code>Soundbus</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public AudioInputNode addAudioInputNode() throws IllegalStateException;
    
    /**
     * Creates a new <code>NetworkAudioInputNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @return The newly created <code>NetworkAudioInputNode</code>, or <code>null</code>
     * if no <code>NetworkAudioInputNode</code> was added to this <code>Soundbus</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public NetworkAudioInputNode addNetworkAudioInputNode() throws IllegalStateException;
    
    /**
     * Creates a new <code>AudioOutputNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @return The newly created <code>AudioOutputNode</code>, or <code>null</code>
     * if no <code>AudioOutputNode</code> was added to this <code>Soundbus</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public AudioOutputNode addAudioOutputNode() throws IllegalStateException;
    
    /**
     * Creates a new <code>NetworkAudioOutputNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @return The newly created <code>NetworkAudioOutputNode</code>, or <code>null</code>
     * if no <code>NetworkAudioOutputNode</code> was added to this <code>Soundbus</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public NetworkAudioOutputNode addNetworkAudioOutputNode();
    
    /**
     * Creates a new <code>AudioSamplerNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @return The newly created <code>AudioSamplerNode</code>, or <code>null</code>
     * if no <code>AudioSamplerNode</code> was added to this <code>Soundbus</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public AudioSamplerNode addAudioSamplerNode() throws IllegalStateException;
    
    /**
     * Creates a new <code>OSCReceiverNode</code>, adds it to this
     * <code>Soundbus</code> and returns it.
     * @return The newly created <code>OSCReceiverNode</code>, or <code>null</code>
     * if no <code>OSCReceiverNode</code> was added to this <code>Soundbus</code>.
     * @throws IllegalStateException if this <code>Soundbus</code> is open.
     */
    public OSCNode addOSCReceiverNode() throws IllegalStateException;
    
    /**
     * Creates a new <code>SbNode</code> of the type specified by <code>noteType</code>
     * and adds it to this <code>Soundbus</code>.
     * @param nodeType The node type name.
     * @param parameters The node parameters in their externalized form.
     * @param clientProperties The initial client properties to set.
     * @param plugDescriptors A <code>List</code> of <code>PlugDescriptor</code>
     * objects 
     * @return The newly created <code>AudioOutputNode</code>, or <code>null</code>
     * if no <code>AudioOutputNode</code> was added to this <code>Soundbus</code>.
     * @throws UnknownNodeTypeException If the given node type name could not be resolved.
     * @throws IllegalStateException
     */
    public SbNode addNode(
            String nodeType, Map<String,String> parameters,
            Map<String,String> clientProperties,
            List<PlugDescriptor> plugDescriptors )
    throws UnknownNodeTypeException, IllegalStateException;
    
    /**
     * Opens this <code>Soundbus</code> and acquires all required resources such
     * as input and output devices.
     * @throws SoundbusException if an error occurred while trying to open this
     * <code>Soundbus</code>.
     */
    public void open() throws SoundbusException;
    
    /**
     * Closes this <code>Soundbus</code> and releases all resources that have been
     * reserved, such as input and output devices.<br>
     * A <code>Soundbus</code> which has been closed can be re-opened using the
     * <code>open()</code> method.
     * @throws SoundbusException if an error occurred while trying to close this
     * <code>Soundbus</code>.
     */
    public void close() throws SoundbusException;
    
    /**
     * Destroys this <code>Soundbus</code>. A destroyed soundbus cannot be re-used.
     * @throws SoundbusException if an error occurred (e.g., this soundbus has already
     * been destroyed).
     */
    public void destroy() throws SoundbusException;
    
    /**
     * Gets the current <code>open</code> state.
     * @return <code>true</code> if this soundbus is open.
     */
    public boolean isOpen();
    
    /**
     * Sets the <code>mute</code> state on this soundbus.
     * @param mute If <code>true</code>, sound output from this soundbus will be
     * deactivated without releasing any resources that are reserved for this
     * <code>Soundbus</code>. 
     */
    public void setMute( boolean mute );
    
    /**
     * Gets the current <code>mute</code> state.
     * @return <code>true</code> if sound output is currently deactivated.
     */
    public boolean isMuted();
    
    /**
     * Sets the tempo this <code>Soundbus</code> shall publish to any
     * nodes that require/support a tempo setting.
     * @param tempo The tempo to set in beats per minute (BPM).
     */
    public void setTempo( float tempo );
    
    /**
     * Gets the tempo this <code>Soundbus</code> is currently set to.
     * @return The tempo in beats per minute (BPM). Default is 120.
     */
    public float getTempo();
    
    /**
     * Sets a generic client property on this <code>Soundbus</code>.
     * Client properties will also be persisted when this <code>Soundbus</code>
     * is persisted.
     * @param property The property name.
     * @param value The property value.
     */
    public void putClientProperty( String property, String value );

    /**
     * Removes the given property.
     * @param property The property name.
     * @return <code>true</code> if the property has been removed, <code>false</code>
     * otherwise.
     */
    public boolean removeClientProperty( String property );
    
    /**
     * Gets a generic client property from this <code>Soudbus</code>.
     * @param property The property name.
     * @return The property value if found, or <code>null</code>.
     */
    public String getClientProperty( String property );
    
    /**
     * Gets all client properties as a typesafe <code>Map</code>.
     * @return All client properties for this <code>Soundbus</code>.
     */
    public Map<String,String> getClientProperties();
}
