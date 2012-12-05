/*
 * Created on 03.10.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.plugin.view;

/**
 * <p>
 * This interface shall be implemented by any kind of class that
 * represents the container of a visible <code>ViewInstance</code>.
 * </p>
 * <p>
 * In a Swing implementation, for instance, it might be a
 * <code>JFrame</code> or a <code>JInternalFrame</code> that
 * implements this interface if visible instances of views are
 * displayed within a frame or internal frame.
 * </p>
 * @author jreese
 */
public interface ViewContainer
{
    /**
     * Gets the title text.
     * @return The title text.
     */
    public String getTitleText();

    /**
     * Sets the title text.
     * @param titleText The title text to set.
     */
    public void setTitleText( String titleText );
    
    /**
     * Sets the fixed size flag.
     * @param fixedSize If set to <code>true</code>, the
     *        <code>ViewContainer shall not be resized</code>.
     */
    public void setHasFixedSize( boolean fixedSize );
    
    /**
     * Adjusts the <code>ViewContainer</code> to the preferred size of
     * contained UI object, if possible.
     */
    public void adjustToPreferredSize();
    
    /**
     * Gets the <code>ViewInstance</code> that is associated with this
     * <code>ViewContainer</code>.
     * @return The <code>ViewInstance</code>.
     */
    public ViewInstance getViewInstance();
}