/*
 * Created on 28.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import java.beans.PropertyChangeListener;
import java.util.Map;

/**
 * <p>
 * This interface shall be implemented by all classes that represent
 * a soundbus node of any type.
 * </p>
 * @author jonas.reese
 */
public interface SbNode {
    /// Convenience field to be returned if no inputs are available
    final static SbInput[] NO_INPUTS = new SbInput[0];
    /// Convenience field to be returned if no outputs are available
    final static SbOutput[] NO_OUTPUTS = new SbOutput[0];
    
    /**
     * Sets the human-readable name for this <code>SbNode</code>.
     * @param name The name to set.
     */
    public void setName( String name );

    /**
     * Gets the human-readable name for this <code>SbNode</code>.
     * @return The name.
     */
    public String getName();
    
    /**
     * Gets the <code>SbNode</code>'s unique type name.
     * @return The type name. Shall be unique for each type of <code>SbNode</code>.
     */
    public String getType();

    /**
     * Gets all inputs that are available on this <code>SbNode</code>.
     * @return An array of all inputs with no <code>null</code> elements, or
     * an empty array if no inputs are available on this <code>SbNode</code>.
     */
    public SbInput[] getInputs();
    
    /**
     * Gets all outputs that are available on this <code>SbNode</code>.
     * @return An array of all outputs with no <code>null</code> elements, or
     * an empty array if no outputs are available on this <code>SbNode</code>.
     */
    public SbOutput[] getOutputs();
    
    /**
     * Gets the parent soundbus this <code>SbNode</code> is associated with.
     * @return The soundbus. Must <b>not</b> be <code>null</code>.
     */
    public Soundbus getSoundbus();
    
    
    /**
     * Sets a generic client property on this <code>SbNode</code>.
     * Client properties will also be persisted when the parent <code>Soundbus</code>
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
     * Gets a generic client property from this <code>SbNode</code>.
     * @param property The property name.
     * @return The property value if found, or <code>null</code>.
     */
    public String getClientProperty( String property );
    
    /**
     * Gets all client properties as a typesafe <code>Map</code>.
     * @return All client properties for this <code>SbNode</code>.
     */
    public Map<String,String> getClientProperties();
    
    /**
     * Add a <code>PropertyChangeListener</code> to the this <code>SbNode</code>.
     * The listener is registered for all client properties. The same listener
     * object may be added more than once, and will be called as many times as it is added.
     * If <code>listener</code> is <code>null</code>, no exception is thrown and no action is taken.
     * @param listener The <code>propertyChangeListener</code> to be added.
     */
    public void addPropertyChangeListener( PropertyChangeListener listener );

    /**
     * Remove a <code>PropertyChangeListener</code> from this <code>SbNode</code>. This removes
     * a <code>PropertyChangeListener</code> that was registered for all client properties.
     * If <code>listener</code> was added more than once to the same event source,
     * it will be notified one less time after being removed. If listener is
     * <code>null</code>, or was never added, no exception is thrown and no action is taken.
     * @param listener The <code>PropertyChangeListener</code> to be removed.
     */
    public void removePropertyChangeListener( PropertyChangeListener listener );

    /**
     * Add a <code>PropertyChangeListener</code> to the this <code>SbNode</code>.
     * The listener is registered for the specified client property. The same listener
     * object may be added more than once, and will be called as many times as it is added.
     * If <code>listener</code> is <code>null</code>, no exception is thrown and no action is taken.
     * @param listener The <code>propertyChangeListener</code> to be added.
     * @param propertyName The name of the client property to listen to.
     * @param listener The <code>PropertyChangeListener</code> to be added.
     */
    public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener );
    
    /**
     * Remove a <code>PropertyChangeListener</code> from this <code>SbNode</code>. This removes
     * a <code>PropertyChangeListener</code> that was registered for the specified client property.
     * If <code>listener</code> was added more than once to the same event source,
     * it will be notified one less time after being removed. If <code>listener</code> is
     * <code>null</code>, or was never added, no exception is thrown and no action is taken.
     * @param listener The <code>PropertyChangeListener</code> to be removed.
     * @param propertyName The name of the client property that was listened on.
     * @param listener The <code>PropertyChangeListener</code> to be removed
     */
    public void removePropertyChangeListener( String propertyName, PropertyChangeListener listener );
    
    /**
     * Gets all registered <code>PropertyChangeListener</code>s.
     * @return An array containing all registered listeners.
     */
    public PropertyChangeListener[] getPropertyChangeListeners();
    
    /**
     * Gets a <code>Map</code> of parameters that are required for this
     * <code>SbNode</code> at creation time.
     * @return A <code>Map</code> of parameter name and value pairs, or
     * <code>null</code> if no parameters are required for this <code>SbNode</code>.
     */
    public Map<String,String> getParameters();
    
    /**
     * Sets a <code>Map</code> of parameters that are required for this
     * <code>SbNode</code> at creation time. This method is called (if it is called)
     * directly after a constructor has been called.
     * @param parameters A <code>Map</code> of parameter name and value pairs, or
     * an empty map if no parameters are required for this <code>SbNode</code>.
     */
    public void setParameters( Map<String,String> parameters );
}
