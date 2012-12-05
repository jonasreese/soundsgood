/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 16.09.2003
 */
package com.jonasreese.sound.sg;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jonasreese.sound.sg.audio.AudioDescriptor;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.util.AbstractEventRedirector;
import com.jonasreese.util.EventQueueHandler;

/**
 * <b>
 * Represents a <code>SoundsGood</code> application session.
 * Within one instance of the application, there might be more than one
 * <code>Session</code> present. Session handling is provided by the class
 * <code>SgEngine</code>.
 * </b>
 * @author jreese
 */
public class Session
{
    private String name;
    private boolean changed;
    private List<SessionElementDescriptor> descriptors;
    private File descriptorFile;
    private List<SessionListener> sessionListeners;
    private List<SessionElementListener> sessionElementListeners;
    private List<ObjectSelectionChangeListener> objectSelectionChangeListeners;
    private SessionElementDescriptor[] selectedObjects;
    private PropertyChangeSupport pcs;
    private Map<String,String> clientProperties;
    private Map<String,String> persistentClientProperties;

    /**
     * Constructs a new <code>Session</code>.
     * @param changed <code>true</code> if the session has changed, <code>false</code>
     *         otherwise.
     */
    public Session( File descriptorFile, boolean changed )
    {
        this.descriptorFile = descriptorFile;
        this.changed = changed;
        sessionListeners = Collections.synchronizedList( new ArrayList<SessionListener>() );
        sessionElementListeners = Collections.synchronizedList(
                new ArrayList<SessionElementListener>() );
        objectSelectionChangeListeners = Collections.synchronizedList(
                new ArrayList<ObjectSelectionChangeListener>() );
        descriptors = Collections.synchronizedList( new ArrayList<SessionElementDescriptor>() );
        selectedObjects = new SessionElementDescriptor[0];
        pcs = new PropertyChangeSupport( this );
        clientProperties = Collections.synchronizedMap( new HashMap<String,String>() );
        persistentClientProperties = Collections.synchronizedMap( new HashMap<String,String>() );
    }
    
    /**
     * Stores a non-persistent client property into this <code>Session</code>.
     * Non-persistent client properties are lost when this session is no longer
     * referenced.<br>
     * This method fires a <code>PropertyChangeEvent</code>, using the string
     * <code>clientProperty.</code> as prefix, directly followed by the specified
     * property name.
     * @param propertyName The property name.
     * @param value The <code>String</code>-encoded value. Shall <b>not</b> be
     *        <code>null</code>.
     */
    public void putClientProperty( String propertyName, String value )
    {
        String oldValue = (String) clientProperties.get( propertyName );
        clientProperties.put( propertyName, value );
        pcs.firePropertyChange( "clientProperty." + propertyName, oldValue, value );
    }
    
    /**
     * Gets a non-persistent client property from this <code>Session</code>.
     * @param propertyName The property name.
     * @return The value that has been previously stored, or <code>null</code>
     *         if no value is currently assigned to the given property name.
     */
    public String getClientProperty( String propertyName )
    {
        return (String) clientProperties.get( propertyName );
    }
    
    /**
     * Gets a <code>String</code> array from a client property.
     * @param propertyName The array property name.
     * @param delimeter The separator string that separates two array entries. 
     * @return An array of <code>String</code>, or <code>null</code> if no property
     *         with the given name is found.
     */
    public String[] getClientPropertyArray( String propertyName, String delimeter )
    {
        String s = getClientProperty( propertyName );
        if (s == null) { return null; }
        StringTokenizer st = new StringTokenizer( s, delimeter );
        String[] result = new String[st.countTokens()];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = st.nextToken();
        }
        return result;
    }
    
    /**
     * Sets a <code>String</code> array as a client property.
     * @param propertyName The array property name.
     * @param propertyArray An array of <code>String</code>.
     *        Must not be <code>null</code>, and the elements shall
     *        not contain the <code>delimeter</code>.
     * @param delimeter The separator string that separates two array entries. 
     */
    public void putClientPropertyArray(
        String propertyName, String[] propertyArray, String delimeter )
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < propertyArray.length; i++)
        {
            sb.append( propertyArray[i] );
            if (i + 1 < propertyArray.length)
            {
                sb.append( delimeter );
            }
        }
        putClientProperty( propertyName, sb.toString() );
    }
    
    /**
     * Removes a non-persistent client property from this <code>Session</code>.
     * This method fires a <code>PropertyChangeEvent</code> with it's <code>newValue</code>
     * property set to <code>null</code>, indicating the property removal.
     * @param propertyName The name of the property to be removed. Shall <b>not</b>
     *        be <code>null</code>.
     * @return <code>true</code> if the property with the given name has been
     *         removed, <code>false</code> otherwise (e.g., a property with that
     *         name did not exist).
     */
    public boolean removeClientProperty( String propertyName )
    {
        String oldVal = (String) clientProperties.remove( propertyName );
        if (oldVal != null)
        {
            pcs.firePropertyChange( "clientProperty." + propertyName, oldVal, null );
            return true;
        }
        return false;
    }
    
    /**
     * Stores a persistent client property into this <code>Session</code>.
     * Persistent client properties are stored when the <code>writeXml()</code>
     * method is called.<br>
     * This method fires a <code>PropertyChangeEvent</code>, using the string
     * <code>persistentClientProperty.</code> as prefix, directly followed by the
     * specified property name.
     * @param propertyName The property name.
     * @param value The <code>String</code>-encoded value. Shall <b>not</b> be
     *        <code>null</code>.
     */
    public void putPersistentClientProperty( String propertyName, String value )
    {
        String oldValue = (String) persistentClientProperties.get( propertyName );
        persistentClientProperties.put( propertyName, value );
        pcs.firePropertyChange( "persistentClientProperty." + propertyName, oldValue, value );

        if (value != oldValue &&
            ((value != null && !(value.equals( oldValue ))) ||
            ((oldValue != null && !(oldValue.equals( value ))))))
        {
            setChanged( true );
        }
    }
    
    /**
     * Gets a persistent client property from this <code>Session</code>.
     * @param propertyName The property name.
     * @return The value that has been previously stored, or <code>null</code>
     *         if no value is currently assigned to the given property name.
     */
    public String getPersistentClientProperty( String propertyName )
    {
        return (String) persistentClientProperties.get( propertyName );
    }
    
    /**
     * Gets a <code>String</code> array from a persistent client property.
     * @param propertyName The array property name.
     * @param delimeter The separator string that separates two array entries. 
     * @return An array of <code>String</code>, or <code>null</code> if no property
     *         with the given name is found.
     */
    public String[] getPersistentClientPropertyArray( String propertyName, String delimeter )
    {
        String s = getPersistentClientProperty( propertyName );
        if (s == null) { return null; }
        StringTokenizer st = new StringTokenizer( s, delimeter );
        String[] result = new String[st.countTokens()];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = st.nextToken();
        }
        return result;
    }

    /**
     * Sets a <code>String</code> array as a persistent client property.
     * @param propertyName The array property name.
     * @param propertyArray An array of <code>String</code>.
     *        Must not be <code>null</code>, and the elements shall
     *        not contain the <code>delimeter</code>.
     * @param delimeter The separator string that separates two array entries. 
     */
    public void putPersistentClientPropertyArray(
        String propertyName, String[] propertyArray, String delimeter )
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < propertyArray.length; i++)
        {
            sb.append( propertyArray[i] );
            if (i + 1 < propertyArray.length)
            {
                sb.append( delimeter );
            }
        }
        putPersistentClientProperty( propertyName, sb.toString() );
    }
    
    /**
     * Removes a persistent client property from this <code>Session</code>.
     * This method fires a <code>PropertyChangeEvent</code> with it's <code>newValue</code>
     * property set to <code>null</code>, indicating the property removal.
     * @param propertyName The name of the property to be removed. Shall <b>not</b>
     *        be <code>null</code>.
     * @return <code>true</code> if the property with the given name has been
     *         removed, <code>false</code> otherwise (e.g., a property with that
     *         name did not exist).
     */
    public boolean removePersistentClientProperty( String propertyName )
    {
        String oldVal = (String) persistentClientProperties.remove( propertyName );
        if (oldVal != null)
        {
            pcs.firePropertyChange( "persistentClientProperty." + propertyName, oldVal, null );
            setChanged( true );
            return true;
        }
        return false;
    }
    
    /**
     * Adds a <code>PropertyChangeListener</code> to this <code>Session</code>.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener( PropertyChangeListener l )
    {
        pcs.addPropertyChangeListener( l );
    }
    
    /**
     * Adds a <code>PropertyChangeListener</code> to this <code>Session</code>.
     * @param propertyName The property's name to be invoked of changes.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener( String propertyName, PropertyChangeListener l )
    {
        pcs.addPropertyChangeListener( propertyName, l );
    }
    
    /**
     * Removes a <code>PropertyChangeListener</code> from this <code>Session</code>.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener( PropertyChangeListener l )
    {
        pcs.removePropertyChangeListener( l );
    }
    
    /**
     * Removes a <code>PropertyChangeListener</code> from this <code>Session</code>.
     * @param propertyName The property's name to be no more invoked of changes.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener( String propertyName, PropertyChangeListener l )
    {
        pcs.removePropertyChangeListener( propertyName, l );
    }
    
    /**
     * Gets all <code>SessionListener</code>s that are currently registered to this
     * <code>Session</code>.
     * @return An array containing all registered <code>SessionListener</code> objects.
     */
    public SessionListener[] getSessionListeners() {
        SessionListener[] result = new SessionListener[sessionListeners.size()];
        sessionListeners.toArray( result );
        return result;
    }
    
    /**
     * Adds a <code>SessionListener</code> to this <code>Session</code>.
     * @param l The listener to be added.
     */
    public void addSessionListener( SessionListener l ) {
        sessionListeners.add( l );
    }
    
    /**
     * Removes a <code>SessionListener</code> from this <code>Session</code>.
     * @param l The listener that shall be no more invoked of session events.
     */
    public void removeSessionListener( SessionListener l ) {
        sessionListeners.remove( l );
    }
    
    /**
     * Gets all <code>SessionElementListener</code>s that are currently registered to this
     * <code>Session</code>.
     * @return An array containing all registered <code>SessionElementListener</code> objects.
     */
    public SessionElementListener[] getSessionElementListeners() {
        SessionElementListener[] result = new SessionElementListener[sessionListeners.size()];
        sessionElementListeners.toArray( result );
        return result;
    }
    
    /**
     * Adds a <code>SessionElementListener</code> to this <code>Session</code>.
     * @param l The listener to be added.
     */
    public void addSessionElementListener( SessionElementListener l ) {
        sessionElementListeners.add( l );
    }
    
    /**
     * Removes a <code>SessionElementListener</code> from this <code>Session</code>.
     * @param l The listener that shall be no more invoked of session events.
     */
    public void removeSessionElementListener( SessionElementListener l ) {
        sessionElementListeners.remove( l );
    }
    
    /**
     * Adds an <code>ObjectSelectionChangeListener</code> to this <code>Session</code>.
     * @param l The listener to be added.
     */
    public void addObjectSelectionChangeListener( ObjectSelectionChangeListener l ) {
        objectSelectionChangeListeners.add( l );
    }

    /**
     * Removes an <code>ObjectSelectionChangeListener</code> from this <code>Session</code>.
     * @param l The listener that shall be no more invoked of session events.
     */
    public void removeObjectSelectionChangeListener( ObjectSelectionChangeListener l ) {
        objectSelectionChangeListeners.remove( l );
    }
    
    /**
     * Gets the session name.
     * @return The session name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the session name.
     * @param name The session name.
     */
    public void setName( String name ) {
        String oldVal = this.name;
        this.name = name;
        pcs.firePropertyChange( "name", oldVal, this.name );
        setChanged( true );
    }
    
    /**
     * Indicates wether this <code>Session</code> has changed since
     * the last save operation. 
     * @return <code>true</code> if the session has changed, <code>false</code>
     *         otherwise.
     */
    public boolean hasChanged() {
        return changed;
    }
    
    /**
     * Sets the <code>changed</code> indication flag. 
     * @param changed <code>true</code> if the session has changed, <code>false</code>
     *         otherwise.
     */
    public void setChanged( boolean changed ) {
        if (this.changed == changed) { return; }
        this.changed = changed;
        //new Exception( "changed = " + changed ).printStackTrace();
        pcs.firePropertyChange( "changed", !changed, changed );
    }
    
    /**
     * Gets the session's descriptor file.
     * @return The session's descriptor file, or <code>null</code> if none present.
     */
    public File getDescriptorFile() {
        return descriptorFile;
    }
    
    /**
     * Sets the objects that are selected. Note that the object array passed to
     * this method is not checked. It might contain objects which do not belong
     * to this <code>Session</code>. It is up to the application's implementor that
     * only objects belonging to this <code>Session</code> are passed to this method.
     * @param selectedElements The selected objects.
     */
    public void setSelectedElements( SessionElementDescriptor[] selectedElements ) {
        if (isUnequal( selectedElements, this.selectedObjects )) {
            this.selectedObjects = selectedElements;
            fireObjectSelectionChangedEvent( new ObjectSelectionChangedEvent( this, selectedElements ) );
        }
    }

    /**
     * Sets the objects that are selected. Note that the object array passed to
     * this method is not checked. It might contain objects wich do not belong
     * to this <code>Session</code>. It is up to the application's implementor that
     * only objects belonging to this <code>Session</code> are passed to this method.
     * @param selectedElements The selected objects.
     * @param trigger An object may be passed here that triggered the selection change.
     *        This object can be requested from the event that is fired by calling this
     *        method. May be <code>null</code>.
     */
    public void setSelectedElements( SessionElementDescriptor[] selectedElements, Object trigger ) {
        if (isUnequal( selectedElements, this.selectedObjects )) {
            this.selectedObjects = selectedElements;
            fireObjectSelectionChangedEvent(
                    new ObjectSelectionChangedEvent( this, selectedElements, trigger ) );
        }
    }
    
    private boolean isUnequal( Object[] o0, Object[] o1 ) {
        if (o0 == null && o1 == null) { return false; }
        if (o0 == null || o1 == null) { return true; }
        if (o0.length != o1.length) { return true; }
        for (int i = 0; i < o0.length; i++)
        {
            if (o0[i] != o1[i]) { return true; }
        }
        return false;
    }

    /**
     * Gets the session elements that are selected. Note that the object returned by
     * this method is not checked before. It might contain objects wich do not belong
     * to this <code>Session</code>. It is up to the application's implementor that
     * only objects belonging to this <code>Session</code> are being selected.
     * @return The selected objects.
     */
    public SessionElementDescriptor[] getSelectedElements() {
        return selectedObjects;
    }
    
    /**
     * Fires the given event to the registered listener, using the
     * <code>SgEngine</code>s event queue.
     * @param e The event.
     */
    protected void fireObjectSelectionChangedEvent( ObjectSelectionChangedEvent e ) {
        EventQueueHandler eventQueue = SgEngine.getInstance().getEventQueue();
        for (int i = 0; i < objectSelectionChangeListeners.size(); i++) {
            ObjectSelectionChangeListener l = objectSelectionChangeListeners.get( i );
            AbstractEventRedirector rd = new AbstractEventRedirector( l ) {
                public void redirectEvent( EventObject e ) {
                    ((ObjectSelectionChangeListener) getListener()).objectSelectionChanged(
                        (ObjectSelectionChangedEvent) e );
                }
            };
            eventQueue.addQueueEntry( rd, e );
        }
        eventQueue.processEvents();
    }
    
    /**
     * Gets all MIDI files associated with this <code>Session</code>.
     * @return All MIDI files, as a newly created <code>File</code> array.
     */
    public MidiDescriptor[] getMidiElements() {
        int count = 0;
        for (int i = 0; i < descriptors.size(); i++) {
            if (descriptors.get( i ) instanceof MidiDescriptor) {
                count++;
            }
        }
        MidiDescriptor[] descs = new MidiDescriptor[count];
        count = 0;
        for (int i = 0; i < descriptors.size(); i++) {
            Object o = descriptors.get( i );
            if (o instanceof MidiDescriptor) {
                descs[count++] = (MidiDescriptor) o;
            }
        }
        return descs;
    }
    
    /**
     * Gets all audio files associated with this <code>Session</code>.
     * @return All audio files, as a newly created <code>File</code> array.
     */
    public AudioDescriptor[] getAudioElements() {
        int count = 0;
        for (int i = 0; i < descriptors.size(); i++) {
            if (descriptors.get( i ) instanceof AudioDescriptor) {
                count++;
            }
        }
        AudioDescriptor[] descs = new AudioDescriptor[count];
        count = 0;
        for (int i = 0; i < descriptors.size(); i++) {
            Object o = descriptors.get( i );
            if (o instanceof AudioDescriptor) {
                descs[count++] = (AudioDescriptor) o;
            }
        }
        return descs;
    }
    
    /**
     * Tries to find a <code>MidiDescriptor</code> by it's name.
     * @param name The name. If <code>null</code>, the return value will be <code>null</code>.
     * @return A <code>MidiDescriptor</code> if one with the given name was found
     * in this <code>Session</code>, <code>null</code> otherwise.
     */
    public MidiDescriptor getMidiElementByName( String name ) {
        if (name != null) {
            for (int i = 0; i < descriptors.size(); i++) {
                SessionElementDescriptor se = descriptors.get( i );
                if (se instanceof MidiDescriptor) {
                    if (name.equals( se.getName() )) {
                        return (MidiDescriptor) se;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Tries to find a <code>AudioDescriptor</code> by it's name.
     * @param name The name. If <code>null</code>, the return value will be <code>null</code>.
     * @return An <code>AudioDescriptor</code> if one with the given name was found
     * in this <code>Session</code>, <code>null</code> otherwise.
     */
    public AudioDescriptor getAudioElementByName( String name ) {
        if (name != null) {
            for (int i = 0; i < descriptors.size(); i++) {
                SessionElementDescriptor se = descriptors.get( i );
                if (se instanceof AudioDescriptor) {
                    if (name.equals( se.getName() )) {
                        return (AudioDescriptor) se;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Gets all session element descriptors associated with this
     * <code>Session</code>.
     * @return All descriptors, as a newly created array.
     */
    public SessionElementDescriptor[] getAllElements() {
        SessionElementDescriptor[] descs =
            new SessionElementDescriptor[descriptors.size()];
        for (int i = 0; i < descs.length; i++) {
            descs[i] = descriptors.get( i );
        }
        return descs;
    }
    
    /**
     * Fires the given event to the registered listener, using the
     * <code>SgEngine</code>s event queue.
     * @param e The event.
     */
    protected void fireElementAddedEvent( SessionElementEvent e )
    {
        EventQueueHandler eventQueue = SgEngine.getInstance().getEventQueue();
        for (int i = 0; i < sessionElementListeners.size(); i++) {
            SessionElementListener l = sessionElementListeners.get( i );
            AbstractEventRedirector rd = new AbstractEventRedirector( l ) {
                public void redirectEvent( EventObject e ) {
                    ((SessionElementListener) getListener()).elementAdded( (SessionElementEvent) e );
                }
            };
            eventQueue.addQueueEntry( rd, e );
        }
        eventQueue.processEvents();
    }
    
    /**
     * Fires the given event to the registered listener, using the
     * <code>SgEngine</code>s event queue.
     * @param e The event.
     */
    protected void fireElementRemovedEvent( SessionElementEvent e ) {
        EventQueueHandler eventQueue = SgEngine.getInstance().getEventQueue();
        for (int i = 0; i < sessionElementListeners.size(); i++) {
            SessionElementListener l = (SessionElementListener) sessionElementListeners.get( i );
            AbstractEventRedirector rd = new AbstractEventRedirector( l ) {
                public void redirectEvent( EventObject e ) {
                    ((SessionElementListener) getListener()).elementRemoved( (SessionElementEvent) e );
                }
            };
            eventQueue.addQueueEntry( rd, e );
        }
        eventQueue.processEvents();
    }
    
    /**
     * Adds an element to the list of session elements within
     * this session.
     * @param descriptor The descriptor to add.
     */
    public void addElement( SessionElementDescriptor descriptor ) {
        descriptors.add( descriptor );
        fireElementAddedEvent(
            new SessionElementEvent(
                this, descriptor,
                ((descriptor instanceof MidiDescriptor) ?
                    SessionElementEvent.TYPE_MIDI : SessionElementEvent.TYPE_UNKNOWN) ) );
        setChanged( true );
    }
    
    /**
     * Gets the <code>SessionElementDescriptor</code> at the specified
     * position.
     * @param index The index.
     * @return The <code>SessionElementDescriptor</code>.
     */
    public SessionElementDescriptor getElementAt( int index ) {
        return (SessionElementDescriptor) descriptors.get( index );
    }
    
    /**
     * Gets the number of elements within this <code>Session</code>.
     * @return The element count.
     */
    public int getElementCount() {
        return descriptors.size();
    }
    
    /**
     * Removes an element from the list of session elements within
     * this session.
     * @param descriptor The descriptor to remove. If it is not in the
     *        list, this method does nothing.
     */
    public void removeElement( SessionElementDescriptor descriptor ) {
        boolean b = descriptors.remove( descriptor );
        if (b) {
            fireElementRemovedEvent(
                new SessionElementEvent(
                    this, descriptor,
                    ((descriptor instanceof MidiDescriptor) ?
                        SessionElementEvent.TYPE_MIDI : SessionElementEvent.TYPE_UNKNOWN) ) );
            setChanged( true );
            descriptor.destroy();
        }
    }
    
    /**
     * Reads this <code>Session</code> from the given <code>InputStream</code>.
     * The input stream will not be closed by this method.
     * @param is The input stream to read from.
     * @throws IOException if the reading failed.
     * @throws ParserConfigurationException if SAXParser could not be configured.
     * @throws SAXException if the input stream does not provide a correct format.
     * @throws TransformerException if the parsing failed.
     */
    public void readXml( InputStream is )
        throws IOException, ParserConfigurationException, SAXException, TransformerException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = factory.newDocumentBuilder();
        Document doc = db.parse( is );
        Node root = XPathAPI.selectSingleNode( doc, "/sgSession" );
        Node name = XPathAPI.selectSingleNode( root, "name/text()" );
        setName( name.getNodeValue() );
        Node propsNode = XPathAPI.selectSingleNode( root, "sessionProperties" );
        if (propsNode != null) {
            NodeList properties = XPathAPI.selectNodeList( propsNode, "property" );
            for (int i = 0; properties != null && i < properties.getLength(); i++) {
                Node n = properties.item( i );
                if (n != null) {
                    String propertyName = XPathAPI.selectSingleNode( n, "@name" ).getNodeValue();
                    Node textNode = XPathAPI.selectSingleNode( n, "text()" );
                    String propertyValue = ((textNode == null) ? "" : textNode.getNodeValue());
                    persistentClientProperties.put( propertyName, propertyValue );
                }
            }
        }
        NodeList elems = XPathAPI.selectNodeList( root, "sessionElements/sessionElement" );
        for (int i = 0; elems != null && i < elems.getLength(); i++) {
            Node n = elems.item( i );
            if (n != null) {
                String type = XPathAPI.selectSingleNode( n, "@type" ).getNodeValue();
                try {
    				SessionElementDescriptor ed = (SessionElementDescriptor) Class.forName( type ).newInstance();
                    ed.readXml( n );
                    ed.setSession( this );
                    addElement( ed );
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
            }
        }
        setChanged( false );
    }
    
    /**
     * Saves this <code>Session</code> to the session's file.
     * @throws IOException if the save failed.
     */
    public void saveSessionToFile() throws IOException {
        FileOutputStream fout = new FileOutputStream( getDescriptorFile() );
        writeXml( fout );
        fout.close();
    }
    
    /**
     * Called when this session or the application is closed.
     */
    public void destroy() {
        SessionElementDescriptor[] elems = getAllElements();
        for (SessionElementDescriptor desc : elems) {
            desc.destroy();
        }
    }
    
    /**
     * Saves this <code>Session</code> to the given <code>OutputStream</code>.
     * The output stream will not be closed by this method.
     * @param out The <code>OutputStream</code> to save session to.
     * @throws IOException if the writing failed.
     */
    public void writeXml( OutputStream os ) throws IOException {
        // store header and general information
        System.out.println( "Writing XML session ..." );
        PrintWriter pw = new PrintWriter( os );
        pw.println( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" );
        pw.println( "<sgSession>" );
        pw.println( "    <name>" + getXmlEncoded( getName() ) + "</name>" );
        pw.println( "    <comment></comment>" );
        
        // store persistent properties
        Iterator<String> iter = persistentClientProperties.keySet().iterator();
        if (iter.hasNext()) {
            pw.println( "    <sessionProperties>" );
            while (iter.hasNext()) {
                String key = getXmlEncoded( iter.next() );
                String val = getXmlEncoded( persistentClientProperties.get( key ) );
                pw.println( "        <property name=\"" + key + "\">" + val + "</property>" );
            }
            pw.println( "    </sessionProperties>" );
        }
        
        // store session elements
        pw.println( "    <sessionElements>" );
        SessionElementDescriptor[] descs = getAllElements();
        for (int i = 0; i < descs.length; i++) {
            pw.println( "        <sessionElement type=\"" +
                getXmlEncoded( descs[i].getClass().getName() ) + "\">" );
            pw.flush();
            descs[i].writeXml( os, 12 );
            pw.println( "        </sessionElement>" );
        }
        pw.println( "    </sessionElements>" );
        
        pw.println( "</sgSession>" );
        pw.flush();
        setChanged( false );
    }
    
    /**
     * Encodes a string with XML special characters.
     * TODO: Implement this better!
     * @param s The string containing special characters
     * @return The encoded version.
     */
    public static String getXmlEncoded( String s ) {
        String newString = "";
        byte[] bArray = s.getBytes();

        for (int i = 0; i < s.length(); i++) {
            byte b  = bArray[i];

            int val = (b < 0) ? b + 256 : b;
    
            if (val > 8 && b < 32 || b > 126 || b == 92 || b == 38 || b == 34 || b == 35 || b == 37) {
                //System.out.println( s + " : " + val );
                newString+= "&#" + val + ";";
            } else {
                byte[] newByte = new byte[1];
                newByte[0] = b;
                newString+= new String( newByte );
            }
        }

        return newString;
    }
    
    /// invoked by SgEngine
    void sessionAdded( SessionEvent e ) {
        for (int i = 0; i < sessionListeners.size(); i++) {
            SessionListener sl = sessionListeners.get( i );
            AbstractEventRedirector r = new AbstractEventRedirector( sl ) {
                public void redirectEvent( EventObject e ) {
                    SessionEvent se = (SessionEvent) e;
                    SessionListener sl = (SessionListener) getListener();
                    sl.sessionAdded( se );
                }
            };
            SgEngine.getInstance().getEventQueue().addQueueEntry( r, e );
            SgEngine.getInstance().getEventQueue().processEvents();
        }
    }
    
    /// invoked by SgEngine
    void sessionRemoved( SessionEvent e ) {
        for (int i = 0; i < sessionListeners.size(); i++) {
            SessionListener sl = sessionListeners.get( i );
            AbstractEventRedirector r = new AbstractEventRedirector( sl ) {
                public void redirectEvent( EventObject e ) {
                    SessionEvent se = (SessionEvent) e;
                    SessionListener sl = (SessionListener) getListener();
                    sl.sessionRemoved( se );
                }
            };
            SgEngine.getInstance().getEventQueue().addQueueEntry( r, e );
            SgEngine.getInstance().getEventQueue().processEvents();
        }
    }
    
    /// invoked by SgEngine
    void sessionActivated( SessionEvent e ) {
        for (int i = 0; i < sessionListeners.size(); i++) {
            SessionListener sl = (SessionListener) sessionListeners.get( i );
            AbstractEventRedirector r = new AbstractEventRedirector( sl ) {
                public void redirectEvent( EventObject e ) {
                    SessionEvent se = (SessionEvent) e;
                    SessionListener sl = (SessionListener) getListener();
                    sl.sessionActivated( se );
                }
            };
            SgEngine.getInstance().getEventQueue().addQueueEntry( r, e );
            SgEngine.getInstance().getEventQueue().processEvents();
        }
    }

    /// invoked by SgEngine
    void sessionDeactivated( SessionEvent e ) {
        for (int i = 0; i < sessionListeners.size(); i++) {
            SessionListener sl = (SessionListener) sessionListeners.get( i );
            AbstractEventRedirector r = new AbstractEventRedirector( sl ) {
                public void redirectEvent( EventObject e ) {
                    SessionEvent se = (SessionEvent) e;
                    SessionListener sl = (SessionListener) getListener();
                    sl.sessionDeactivated( se );
                }
            };
            SgEngine.getInstance().getEventQueue().addQueueEntry( r, e );
            SgEngine.getInstance().getEventQueue().processEvents();
        }
    }
}
