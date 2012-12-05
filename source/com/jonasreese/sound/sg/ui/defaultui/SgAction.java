/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 30.11.2003
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.jonasreese.util.Updatable;

/**
 * <b>
 * This is the base class for all SoundsGood actions that shall be
 * used with invoker delegation capabilities. This means, that a part
 * of the application (i.e., a plugin) can set itself to receive the
 * action.
 * </b>
 * @author jreese
 */
public class SgAction extends AbstractAction {
    
    private static final long serialVersionUID = 1;
    
    /// property name for tool tip text
    public static final String TOOL_TIP_TEXT = "toolTipText";
    
    protected Updatable actionReceiver;
    private boolean selected;
    
    /**
     * Constructs a new <code>SgAction</code>.
     */
    public SgAction() {
        super();
        initialize( getValue( NAME ) );
    }

    /**
     * Constructs a new <code>SgAction</code>.
     * @param name The name to set.
     */
    public SgAction( String name ) {
        super( name );
        initialize( getValue( NAME ) );
    }

    /**
     * Constructs a new <code>SgAction</code>.
     * @param name The name to set.
     * @param icon The action's small icon.
     */
    public SgAction( String name, Icon icon ) {
        super( name, icon );
        initialize( getValue( NAME ) );
    }

    /**
     * Constructs a new <code>SgAction</code>.
     * @param name The name to set.
     * @param icon The action's small icon.
     */
    public SgAction( String name, Icon icon, String toolTipText ) {
        super( name, icon );
        initialize( toolTipText );
    }
    
    /**
     * Sets the <code>Updatable</code> that shall receive this action.
     * The object passed to the <code>Updatable</code>s <code>update()</code>
     * method will be <code>this</code>.
     * @param actionReceiver The action receiver, or <code>null</code> if
     *        no <code>Updatable</code> shall be invoked.
     * @return <code>true</code> if the action receiver was successfully set,
     *         <code>false</code> otherise. This default implementation
     *         always returns <code>true</code>.
     */
    public boolean setActionReceiver( Updatable actionReceiver ) {
        this.actionReceiver = actionReceiver;
        
        // Set the enabled state using the following logic:
        // if the actionPerformed method has been overwritten,
        // do not touch the enabled state. Otherwise, set the
        // enabled state to false if actionReceiver is null.
        try {
            if (getClass().equals( SgAction.class )) {
                setEnabled( (actionReceiver != null) );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    /**
     * Gets the <code>Updatable</code> that will receive this action.
     * @return The current action receiver, or <code>null</code> if none is
     *         currently set. 
     */
    public Updatable getActionReceiver() {
        return actionReceiver;
    }
    
    /**
     * Delegates the action perform to the registered receiver.
     * @param e The <code>ActionEvent</code> that happened.
     */
    public void actionPerformed( ActionEvent e ) {
        if (actionReceiver != null) {
            actionReceiver.update( this );
        }
    }
    
    /**
     * Sets the <code>selected</code> state for this <code>SgAction</code>.
     * Fires a <code>PropertyChangeEvent</code>.
     * @param selected The selected state to set.
     */
    public void setSelected( boolean selected ) {
        if (this.selected == selected) { return; }
        PropertyChangeEvent e = new PropertyChangeEvent(
            this, "selected", new Boolean( this.selected ), new Boolean( selected ) );
        this.selected = selected;
        changeSupport.firePropertyChange( e );
    }
    
    
    // initialize everything...
    private void initialize( Object toolTipText ) {
        selected = false;
        setActionReceiver( null );
        putValue( TOOL_TIP_TEXT, toolTipText );
    }
}
