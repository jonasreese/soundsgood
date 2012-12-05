/*
 * Created on 01.10.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jonasreese.sound.sg.edit.UndoableEditUpdateEvent;
import com.jonasreese.sound.sg.edit.UndoableEditUpdateListener;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.util.AbstractEventRedirector;
import com.jonasreese.util.EventQueueHandler;
import com.jonasreese.util.Updatable;

/**
 * <p>
 * This class describes an element of a <code>Session</code>.
 * A certain subtype of a session element (e.g., a MIDI element)
 * shall be be implemented as a subclass.
 * </p>
 * @author jreese
 */
public abstract class SessionElementDescriptor {
    public static final int STATUS_UNMODIFIED = 0;
    public static final int STATUS_IN_MODIFICATION = 1;
    public static final int STATUS_DONE = 2;
    public static final int STATUS_UNKNOWN = 3;
    
    private File file;
    private Session session;
    private boolean changed;
    protected HashMap<String,String> clientProperties;
    protected HashMap<String,String> persistentClientProperties;
    private int status;
    private String descriptionText;
    private ArrayList<ViewInstance> registeredViews;
    private ArrayList<Updatable> propertyHooks;

    // undo handling
    private ArrayList<UndoableEditUpdateListener> undoListeners;
    private UndoManager undoManager;
    
    /// for use in subclasses' getter and setter methods 
    protected PropertyChangeSupport propertyChangeSupport;

    /**
     * Constructs a new <code>SessionElementDescriptor</code>.
     */
    public SessionElementDescriptor() {
        file = null;
        session = null;
        changed = false;
        status = STATUS_UNMODIFIED;
        propertyChangeSupport = new PropertyChangeSupport( this );
        clientProperties = new HashMap<String,String>();
        persistentClientProperties = new HashMap<String,String>();
        registeredViews = new ArrayList<ViewInstance>();
        propertyHooks = new ArrayList<Updatable>();

        undoListeners = new ArrayList<UndoableEditUpdateListener>();
        undoManager = new UndoManager() {
            private static final long serialVersionUID = 1;
            public void undoableEditHappened( UndoableEditEvent e ) {
                super.undoableEditHappened( e );
                fireUndoableEditUpdateEvent( new UndoableEditUpdateEvent( undoManager ) );
            }
            public boolean addEdit( UndoableEdit anEdit ) {
                boolean b = super.addEdit( anEdit );
                if (b) {
                    fireUndoableEditUpdateEvent( new UndoableEditUpdateEvent( undoManager ) );
                }
                return b;
            }
        };
        undoManager.setLimit( SgEngine.getInstance().getProperties().getUndoSteps() );
    }
    
    /**
     * Gets the type of this <code>SessionElementDescriptor</code>, encapsulated
     * by a <code>SessionElementType</code> object.
     * @return The type. Must not be <code>null</code>.
     */
    public abstract SessionElementType getType();
    
    /**
     * Adds an <code>UndoableEditUpdateListener</code> to this <code>SgEngine</code>.
     * This listener will be invoked when the <code>UndoManager</code> changes it's
     * state in that way, that undo/redo options toggle or new <code>UndoableEdit</code>s
     * have been added to it.
     * @param l The listener to add.
     */
    public void addUndoableEditUpdateListener( UndoableEditUpdateListener l ) {
        synchronized (undoListeners) {
            undoListeners.add( l );
        }
    }
    
    /**
     * Removes an <code>UndoableEditUpdateListener</code> to this <code>SgEngine</code>.
     * @param l The listener to remove.
     */
    public void removeUndoableEditUpdateListener( UndoableEditUpdateListener l ) {
        synchronized (undoListeners) {
            undoListeners.remove( l );
        }
    }
    
    /**
     * Gets all registered <code>UndoableEditUpdateListener</code>s.
     * @return An array of all regitered listeners.
     */
    public UndoableEditUpdateListener[] getUndoableEditUpdateListeners() {
        synchronized (undoListeners) {
            UndoableEditUpdateListener[] result = new UndoableEditUpdateListener[undoListeners.size()];
            return undoListeners.toArray( result );
        }
    }
    
    /**
     * Fires an <code>UndoableEditUpdateEvent</code> to all registered listeners.
     * @param e The event to be fired.
     */
    protected void fireUndoableEditUpdateEvent( UndoableEditUpdateEvent e ) {
        // fire events
        EventQueueHandler eventQueue = SgEngine.getInstance().getEventQueue();
        synchronized (undoListeners) {
            for (int i = 0; i < undoListeners.size(); i++) {
                UndoableEditUpdateListener l =
                    (UndoableEditUpdateListener) undoListeners.get( i );
                AbstractEventRedirector r = new AbstractEventRedirector( l ) {
                    public void redirectEvent( EventObject e ) {
                        ((UndoableEditUpdateListener) getListener()).undoableEditUpdate(
                            (UndoableEditUpdateEvent) e );
                    }
                };
                eventQueue.addQueueEntry( r, e );
            }
        }
        eventQueue.processEvents();
    }
    
    /**
     * Gets the <code>UndoManager</code> for this SoudsGood application engine.
     * @return The <code>UndoManager</code>.
     */
    public UndoManager getUndoManager() { return undoManager; }
    
    /**
     * Stores a non-persistent client property into this <code>SessionElementDescriptor</code>.
     * Non-persistent client properties are lost when the parent session is no longer
     * referenced.<br>
     * This method fires a <code>PropertyChangeEvent</code>, using the string
     * <code>clientProperty.</code> as prefix, directly followed by the specified
     * property name.
     * @param propertyName The property name.
     * @param value The <code>String</code>-encoded value. Shall <b>not</b> be
     *        <code>null</code>.
     */
    public void putClientProperty( String propertyName, String value ) {
        String oldValue;
        synchronized (clientProperties) {
            oldValue = (String) clientProperties.get( propertyName );
        }
        clientProperties.put( propertyName, value );
        propertyChangeSupport.firePropertyChange(
            "clientProperty." + propertyName, oldValue, value );
    }
    
    /**
     * Gets a non-persistent client property from this <code>SessionElementDescriptor</code>.
     * @param propertyName The property name.
     * @return The value that has been previously stored, or <code>null</code>
     *         if no value is currently assigned to the given property name.
     */
    public String getClientProperty( String propertyName ) {
        synchronized (clientProperties) {
            return (String) clientProperties.get( propertyName );
        }
    }

    /**
     * Gets a <code>String</code> array from a client property.
     * @param propertyName The array property name.
     * @param delimeter The separator string that separates two array entries. 
     * @return An array of <code>String</code>, or <code>null</code> if no property
     *         with the given name is found.
     */
    public String[] getClientPropertyArray( String propertyName, String delimeter ) {
        String s = getClientProperty( propertyName );
        if (s == null) { return null; }
        StringTokenizer st = new StringTokenizer( s, delimeter );
        String[] result = new String[st.countTokens()];
        for (int i = 0; i < result.length; i++) {
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
        String propertyName, String[] propertyArray, String delimeter ) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < propertyArray.length; i++) {
            sb.append( propertyArray[i] );
            if (i + 1 < propertyArray.length) {
                sb.append( delimeter );
            }
        }
        putClientProperty( propertyName, sb.toString() );
    }
    
    /**
     * Removes a non-persistent client property from this <code>SessionElementDescriptor</code>.
     * This method fires a <code>PropertyChangeEvent</code> with it's <code>newValue</code>
     * property set to <code>null</code>, indicating the property removal.
     * @param propertyName The name of the property to be removed. Shall <b>not</b>
     *        be <code>null</code>.
     * @return <code>true</code> if the property with the given name has been
     *         removed, <code>false</code> otherwise (e.g., a property with that
     *         name did not exist).
     */
    public boolean removeClientProperty( String propertyName ) {
        String oldVal;
        synchronized (clientProperties) {
            oldVal = (String) clientProperties.remove( propertyName );
        }
        if (oldVal != null) {
            propertyChangeSupport.firePropertyChange(
                "clientProperty." + propertyName, oldVal, null );
            return true;
        }
        return false;
    }
    
    /**
     * Stores a persistent client property into this <code>SessionElementDescriptor</code>.
     * Persistent client properties are stored when the <code>writeXml()</code>
     * method is called.<br>
     * This method fires a <code>PropertyChangeEvent</code>, using the string
     * <code>persistentClientProperty.</code> as prefix, directly followed by the
     * specified property name.
     * @param propertyName The property name.
     * @param value The <code>String</code>-encoded value. Shall <b>not</b> be
     *        <code>null</code>.
     */
    public void putPersistentClientProperty( String propertyName, String value ) {
        String oldValue;
        synchronized (persistentClientProperties) {
            oldValue = (String) persistentClientProperties.get( propertyName );
            persistentClientProperties.put( propertyName, value );
        }
        propertyChangeSupport.firePropertyChange(
            "persistentClientProperty." + propertyName, oldValue, value );

        if (value != oldValue &&
            ((value != null && !(value.equals( oldValue ))) ||
            ((oldValue != null && !(oldValue.equals( value )))))) {
            getSession().setChanged( true );
        }
    }
    
    /**
     * Gets a persistent client property from this <code>SessionElementDescriptor</code>.
     * @param propertyName The property name.
     * @return The value that has been previously stored, or <code>null</code>
     *         if no value is currently assigned to the given property name.
     */
    public String getPersistentClientProperty( String propertyName ) {
        synchronized (persistentClientProperties) {
            return (String) persistentClientProperties.get( propertyName );
        }
    }
    
    /**
     * Gets a persistent client property of type <code>long</code>.
     * @param defaultValue The default value if property does not exist or not a numeric type.
     * @param propertyName The property name.
     * @return The according property value as a <code>long</code>, or <code>defaultValue</code>.
     */
    public long getPersistentClientProperty( long defaultValue, String propertyName ) {
        String s = getPersistentClientProperty( propertyName );
        if (s == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong( s );
        } catch (NumberFormatException nfex) {
            return defaultValue;
        }
    }
    
    /**
     * Gets a <code>String</code> array from a persistent client property.
     * @param propertyName The array property name.
     * @param delimeter The separator string that separates two array entries. 
     * @return An array of <code>String</code>, or <code>null</code> if no property
     *         with the given name is found.
     */
    public String[] getPersistentClientPropertyArray( String propertyName, String delimeter ) {
        String s = getPersistentClientProperty( propertyName );
        if (s == null) { return null; }
        StringTokenizer st = new StringTokenizer( s, delimeter );
        String[] result = new String[st.countTokens()];
        for (int i = 0; i < result.length; i++) {
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
        String propertyName, String[] propertyArray, String delimeter ) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < propertyArray.length; i++) {
            sb.append( propertyArray[i] );
            if (i + 1 < propertyArray.length) {
                sb.append( delimeter );
            }
        }
        putPersistentClientProperty( propertyName, sb.toString() );
    }
    
    /**
     * Removes a persistent client property from this <code>SessionElementDescriptor</code>.
     * This method fires a <code>PropertyChangeEvent</code> with it's <code>newValue</code>
     * property set to <code>null</code>, indicating the property removal.
     * @param propertyName The name of the property to be removed. Shall <b>not</b>
     *        be <code>null</code>.
     * @return <code>true</code> if the property with the given name has been
     *         removed, <code>false</code> otherwise (e.g., a property with that
     *         name did not exist).
     */
    public boolean removePersistentClientProperty( String propertyName ) {
        String oldVal = (String) persistentClientProperties.remove( propertyName );
        if (oldVal != null) {
            propertyChangeSupport.firePropertyChange(
                "persistentClientProperty." + propertyName, oldVal, null );
            getSession().setChanged( true );
            return true;
        }
        return false;
    }

    /**
     * Adds a <code>PropertyChangeListener</code> to this
     * <code>SessionElementDescriptor</code>.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        propertyChangeSupport.addPropertyChangeListener( l );
    }

    /**
     * Adds a <code>PropertyChangeListener</code> to this
     * <code>SessionElementDescriptor</code>.
     * @param propertyName The property name to listen to.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener( String propertyName, PropertyChangeListener l ) {
        propertyChangeSupport.addPropertyChangeListener( propertyName, l );
    }
    
    /**
     * Removes a <code>PropertyChangeListener</code> from this
     * <code>SessionElementDescriptor</code>.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        propertyChangeSupport.removePropertyChangeListener( l );
    }

    /**
     * Removes a <code>PropertyChangeListener</code> from this
     * <code>SessionElementDescriptor</code>.
     * @param propertyName The property name to no more listen to.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener( String propertyName, PropertyChangeListener l ) {
        propertyChangeSupport.removePropertyChangeListener( propertyName, l );
    }
    
    /**
     * Returns all registered <code>PropertyChangeListener</code>s as an array.
     * @return An array with all registered listeners.
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return propertyChangeSupport.getPropertyChangeListeners();
    }
    
    /**
     * Returns all <code>PropertyChangeListener</code>s registered for the given
     * property as an array.
     * @param propertyName The property name.
     * @return An array with all registered listeners.
     */
    public PropertyChangeListener[] getPropertyChangeListeners( String propertyName ) {
        return propertyChangeSupport.getPropertyChangeListeners( propertyName );
    }
    
    /**
     * Sets the <code>Session</code> that is associated with (parent to)
     * this <code>SessionElementDescriptor</code>.<br>
     * Since this method is a basic one and shall be called directly
     * after the constructor and only once, this method will not fire a
     * property change event.
     * @param session The that is associated with (parent to)
     *         this <code>SessionElementDescriptor</code>. Since each
     *         <code>SessionElementDescriptor</code> is associated with a
     *         <code>Session</code>, this parameter shall not be <code>null</code>.
     */
    public void setSession( Session session ) {
        this.session = session;
    }

    /**
     * Gets the <code>Session</code> that is associated with (parent to)
     * this <code>SessionElementDescriptor</code>.
     * @return The <code>Session</code>. Since each
     *         <code>SessionElementDescriptor</code> is associated with a
     *         <code>Session</code>, this method shall not return <code>null</code>.
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the <code>changed</code> state for this <code>SessionElementDescriptor</code>.
     * This method fires a property change event using the property named <code>changed</code>.
     * @return <code>true</code> to indicate that the session element described by
     *         this has been changed (and should be saved), <code>false</code> otherwise.
     */
    public void setChanged( boolean changed ) {
        if (this.changed == changed) { return; }
        PropertyChangeEvent e = new PropertyChangeEvent(
            this, "changed", new Boolean( this.changed ), new Boolean( changed ) );
        this.changed = changed;
        propertyChangeSupport.firePropertyChange( e );
    }

    /**
     * Resets any data cached in mememory by this <code>SessionElementDescriptor</code>.
     * This method can be called when data held by this <code>SessionElementDescriptor</code>
     * shall be re-loaded from the file system. This method should not be called while
     * there are <code>ViewInstance</code>s registered, or this
     * <code>SessionElementDescriptor</code> has changed but not yet been saved.
     * However, this method will not check any preconditions and anyway reset it's memory
     * data. So be careful when calling this method.<br>
     */
    public abstract void resetData();

    /**
     * Gets the <code>changed</code> state for this <code>SessionElementDescriptor</code>.
     * @return <code>true</code> to indicate that the session element described by
     *         this has been changed (and should be saved), <code>false</code> otherwise.
     */
    public boolean isChanged() {
        return changed;
    }
    
    /**
     * Gets the status of this <code>SessionElementDescriptor</code>. Keeping
     * a status shall help the user to know what to do with this session element.
     * @return One of <code>STATUS_UNMODIFIED</code>, <code>STATUS_IN_MODIFICATION</code>,
     *         <code>STATUS_DONE</code> or <code>STATUS_UNKNOWN</code>.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets the status of this <code>SessionElementDescriptor</code>. Keeping
     * a status shall help the user to know what to do with this session element.
     * @param status One of <code>STATUS_UNMODIFIED</code>, <code>STATUS_IN_MODIFICATION</code>,
     *         <code>STATUS_DONE</code> or <code>STATUS_UNKNOWN</code>.
     */
    public void setStatus( int status ) {
        if (this.status == status) { return; }
        PropertyChangeEvent e = new PropertyChangeEvent(
            this, "status", new Integer( this.status ), new Integer( status ) );
        this.status = status;
        propertyChangeSupport.firePropertyChange( e );
        session.setChanged( true );
    }

    /**
     * Gets a human-readable description text for this <code>SessionElementDescriptor</code>.
     * @return A human-readable plain text <code>String</code>, or <code>null</code>.
     */
    public String getDescriptionText() {
        return descriptionText;
    }

    /**
     * Sets a human-readable description text for this <code>SessionElementDescriptor</code>.
     * @param descriptionText A human-readable plain text <code>String</code>, or <code>null</code>.
     */
    public void setDescriptionText( String descriptionText ) {
        if ((descriptionText == this.descriptionText)) { return; }
        PropertyChangeEvent e = new PropertyChangeEvent(
            this, "descriptionText", this.descriptionText, descriptionText );
        this.descriptionText = descriptionText;
        propertyChangeSupport.firePropertyChange( e );
        session.setChanged( true );
    }

    /**
     * Gets a human-readable display name for this <code>SessionElementDescriptor</code>.
     * @return A name.
     */
    public abstract String getName();

    /**
     * Adds a <code>propertyHook</code> to this <code>SessionElementDescriptor</code>.
     * A property hook is an object that receives a callback when this
     * <code>SessionElementDescriptor</code> is about to be saved. By using a property
     * hook, you can lazily save all pending properties just once and directly before
     * the properties are saved. This avoids expensive property marshalling every time
     * a property has changed.<br><br>
     * A property hook will not be added if already registered.
     * @param propertyHook An <code>Updatable</code>. Must not be <code>null</code>.
     * The <code>update(Object)</code> method is called before this
     * <code>SessionElementDescriptor</code> is persisted. The object passed to the
     * update method is this <code>SessionElementDescriptor</code>.
     */
    public void addPropertyHook( Updatable propertyHook ) {
        synchronized (propertyHooks) {
            if (!propertyHooks.contains( propertyHook )) {
                propertyHooks.add( propertyHook );
            }
        }
    }
    
    /**
     * Unregisters a <code>ViewInstance</code> from this <code>SessionElementDescriptor</code>.
     * @param viewInstance The <code>ViewInstance</code> that will no more change this
     *        <code>SessionElementDescriptor</code> after this method has been called.
     */
    public void removePropertyHook( Updatable propertyHook ) {
        synchronized (propertyHooks) {
            propertyHooks.remove( propertyHook );
        }
    }
    
    /**
     * Gets all registered property hooks.
     * @return An array of <code>Updatable</code> objects.
     */
    public Updatable[] getPropertyHooks() {
        synchronized (propertyHooks) {
            Updatable[] result = new Updatable[propertyHooks.size()];
            propertyHooks.toArray( result );
            return result;
        }
    }

    /**
     * Registers a <code>ViewInstance</code> to this <code>SessionElementDescriptor</code>.
     * This method shall be called by the part of the UI subsystem that creates a
     * <code>ViewInstance</code> for this <code>SessionElementDescriptor</code>.
     * @param viewInstance The <code>ViewInstance</code> to be registered. The <code>View</code>
     *        passed to this method may change this <code>SessionElementDescriptor</code>,
     *        so that it is safe to do so until <code>unregisterViewInstance()</code> is called. 
     */
    public void registerViewInstance( ViewInstance viewInstance ) {
        synchronized (registeredViews) {
            if (!registeredViews.contains( viewInstance )) {
                registeredViews.add( viewInstance );
            }
        }
    }

    /**
     * Unregisters a <code>ViewInstance</code> from this <code>SessionElementDescriptor</code>.
     * @param viewInstance The <code>ViewInstance</code> that will no more change this
     *        <code>SessionElementDescriptor</code> after this method has been called.
     * @return <code>true</code> if the given <code>ViewInstance</code> has been successfully
     * unregistered, <code>false</code> otherwise.
     */
    public boolean unregisterViewInstance( ViewInstance viewInstance ) {
        synchronized (registeredViews) {
            return registeredViews.remove( viewInstance );
        }
    }
    
    /**
     * Returns <code>true</code> if any view instance is registered within this
     * <code>SessionElementDescriptor</code>.
     * @return <code>true</code> if a <code>ViewInstance</code> is registered, <code>false</code>
     *         otherwise.
     */
    public boolean isViewInstanceRegistered() {
        synchronized (registeredViews) {
            return !registeredViews.isEmpty();
        }
    }

    /**
     * Returns <code>true</code> if the given view instance is registered within this
     * <code>SessionElementDescriptor</code>.
     * @param viewInstance The view instance to check.
     * @return <code>true</code> if the <code>ViewInstance</code> is registered, <code>false</code>
     *         otherwise.
     */
    public boolean isViewInstanceRegistered( ViewInstance viewInstance ) {
        synchronized (registeredViews) {
            return registeredViews.contains( viewInstance );
        }
    }

    /**
     * Gets all <code>ViewInstance</code>s that are registered within (and thus, might change)
     * this <code>SessionElementDescriptor</code>.
     * @return A copied array of all registered view instances. If none is registered, an empty
     *         array is returned.
     */
    public ViewInstance[] getRegisteredViewInstances() {
        synchronized (registeredViews) {
            ViewInstance[] vis = new ViewInstance[registeredViews.size()];
            for (int i = 0; i < registeredViews.size(); i++) {
                vis[i] = (ViewInstance) registeredViews.get( i );
            }
            return vis;
        }
    }

    /**
     * Convenience method. A call to this method will set this
     * <code>SessionElementDescriptor</code> as the only element in
     * it's <code>Session</code>'s selection. The following code has
     * the same effect as calling this method (assuming that the
     * <code>SessionElementDescriptor</code> is <code>sed</code>:
     * <pre>
     * sed.getSession().setSelectedObjects( new Object[]{ sed }, sed );
     * </pre>
     */
    public void select() {
        getSession().setSelectedElements( new SessionElementDescriptor[]{ this }, this );
    }

	/**
     * Gets the file that is associated with the session element
     * described by this <code>SessionElementDescriptor</code>.
	 * @return The file, or <code>null</code> if no file is yet
     *         associated (e.g., if the session element has not
     *         yet been saved to a file.
	 */
	public File getFile() {
		return file;
	}

	/**
     * Sets the file for this <code>SessionElementDescriptor</code>.
     * For now, this method does not fire a property change event.
	 * @param file The file to set.
	 */
	public void setFile( File file ) {
		this.file = file;
	}
    
    /**
     * Saves this <code>SessionElementDescriptor</code>s contents
     * (<b>not</b> the descriptor itself).
     * @throws IOException if the save operation failed.
     */
    public abstract void save() throws IOException;

    /**
     * Saves a copy to the given <code>File</code>.
     * @param copy The <code>File</code> denoting the copy destination.
     * @throws IOException if the save operation failed.
     */
    public abstract void saveCopy( File copy ) throws IOException;
    
    /**
     * Notified when this <code>SessionElementDescriptor</code> is permanently removed.
     * This happens if it is removed from it's parent session, the parent session is closed
     * or removed, or the application is closed.
     */
    public abstract void destroy();
    
    /**
     * Reads from an XML <code>Node</code> and initialized this
     * <code>SessionElementDescriptor</code> with the properties read
     * from the node.
     * @param node The parent node.
     * @throws IOException if the reading failed.
     * @throws ParserConfigurationException if SAXParser could not be configured.
     * @throws SAXException if the input stream does not provide a correct format.
     * @throws TransformerException if the parsing failed.
     */
    public void readXml( Node node )
        throws ParserConfigurationException, IOException, TransformerException {

        Node fileNode = XPathAPI.selectSingleNode( node, "file/text()" );
        if (fileNode != null && fileNode.getNodeValue() != null) {
            setFile( new File( fileNode.getNodeValue() ).getAbsoluteFile() );
        }
        Node statusNode = XPathAPI.selectSingleNode( node, "status" );
        if (statusNode != null) {
            Node statusTextNode = XPathAPI.selectSingleNode( statusNode, "text()" );
            if (statusTextNode != null) {
                String s = statusTextNode.getNodeValue();
                if ("unmodified".equalsIgnoreCase( s )) {
                    status = STATUS_UNMODIFIED;
                } else if ("inModification".equalsIgnoreCase( s )) {
                    status = STATUS_IN_MODIFICATION;
                } else if ("done".equalsIgnoreCase( s )) {
                    status = STATUS_DONE;
                } else {
                    status = STATUS_UNKNOWN;
                }
            } else {
                status = STATUS_UNKNOWN;
            }
        } else {
            status = STATUS_UNKNOWN;
        }
        Node descNode = XPathAPI.selectSingleNode( node, "description" );
        if (descNode != null) {
            Node descTextNode = XPathAPI.selectSingleNode( descNode, "text()" );
            if (descTextNode != null) {
                String s = descTextNode.getNodeValue();
                descriptionText = s;
            }
        }
        Node propsNode = XPathAPI.selectSingleNode( node, "sessionElementProperties" );
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
    }

    /**
     * Writes this <code>SessionElementDescriptor</code> in an XML format
     * to the given output stream. The output stream shall not be closed
     * afterwards.
     * @param os The <code>OutputStream</code> to write to.
     * @throws IOException if the writing failed.
     */
    public void writeXml( OutputStream os, int indent ) throws IOException {
        
        // before saving, execute property hooks
        synchronized (propertyHooks) {
            for (int i = 0; i < propertyHooks.size(); i++) {
                Updatable u = (Updatable) propertyHooks.get( i );
                u.update( this );
            }
        }
        
        PrintWriter pw = new PrintWriter( os );
        String indentString = getIndentString( indent );
        if (getFile() != null) {
            pw.println( indentString + "<file>" +
                Session.getXmlEncoded( getFile().getAbsolutePath() ) + "</file>" );
    
            if (getStatus() != STATUS_UNKNOWN) {
                if (getStatus() == STATUS_UNMODIFIED) {
                    pw.println( indentString + "<status>unmodified</status>" );
                } else if (getStatus() == STATUS_IN_MODIFICATION) {
                    pw.println( indentString + "<status>inModification</status>" );
                } else if (getStatus() == STATUS_DONE) {
                    pw.println( indentString + "<status>done</status>" );
                }
            }
            
            String s = getDescriptionText();
            if (s != null && !"".equals( s.trim() )) {
                pw.println( indentString + "<description>" +
                    Session.getXmlEncoded( s ) + "</description>" );
            }
    
            synchronized (persistentClientProperties) {
                // store persistent properties
                Iterator<String> iter = persistentClientProperties.keySet().iterator();
                if (iter.hasNext()) {
                    pw.println( indentString + "<sessionElementProperties>" );
                    while (iter.hasNext()) {
                        String key = Session.getXmlEncoded( iter.next() );
                        String val = Session.getXmlEncoded( persistentClientProperties.get( key ) );
                        pw.println( indentString + "    <property name=\"" + key + "\">" + val + "</property>" );
                    }
                    pw.println( indentString + "</sessionElementProperties>" );
                }
            }
        }

        pw.flush();
    }
    
    /**
     * Utility method to get the XML creation indent string.
     * @param indent The number of spaces of indentation.
     * @return The according prefix string.
     */
    protected static String getIndentString( int indent ) {
        char[] c = new char[indent];
        for (int i = 0; i < c.length; i++) { c[i] = ' '; }
        return new String( c );
    }
    
    /**
     * Returns the String representation for this <code>SessionElementDescriptor</code>.
     * Per default, this is the name.
     * @return The String representation.
     */
    public String toString() { return getName(); }
}