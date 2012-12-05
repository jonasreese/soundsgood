/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 08.12.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.grid;

import java.awt.GridLayout;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import com.jonasreese.sound.sg.SgEngine;

/**
 * <p>
 * A graphical panel that offers three options for edit focus selection:
 *   <li>Selected events
 *   <li>Selected track(s)
 *   <li>All tracks
 * </p>
 * @author jreese
 */
public class EditFocusPanel extends JPanel
{
    private static final long serialVersionUID = 1;
    
    private JRadioButton selectionRadioButton;
    private JRadioButton currentRadioButton;
    private JRadioButton allRadioButton;
    private Object clientData;
    
    /**
     * Constructs a new <code>EditFocusPanel</code> with the default title.
     * @param gc The <code>GridComponent</code>.
     */
    public EditFocusPanel( GridComponent gc )
    {
        this( gc, SgEngine.getInstance().getResourceBundle().getString(
            "plugin.gridView.editMidiEvents.focus" ) );
    }
    
    /**
     * Constructs a new <code>EditFocusPanel</code>.
     * @param gc The <code>GridComponent</code>.
     * @param title The title.
     */
    public EditFocusPanel( GridComponent gc, String title )
    {
        super( new GridLayout( 3, 1 ) );
        clientData = null;
        selectionRadioButton = new JRadioButton(
            SgEngine.getInstance().getResourceBundle().getString(
                "plugin.gridView.editMidiEvents.focus.selection" ) );
        currentRadioButton = new JRadioButton(
            SgEngine.getInstance().getResourceBundle().getString(
                "plugin.gridView.editMidiEvents.focus.current" ) );
        allRadioButton = new JRadioButton(
            SgEngine.getInstance().getResourceBundle().getString(
                "plugin.gridView.editMidiEvents.focus.all" ) );
        ButtonGroup bgr = new ButtonGroup();
        bgr.add( currentRadioButton );
        bgr.add( selectionRadioButton );
        bgr.add( allRadioButton );
        if (gc.isSelectionEmpty())
        {
            selectionRadioButton.setEnabled( false );
            currentRadioButton.setSelected( true );
        }
        else
        {
            selectionRadioButton.setSelected( true );
        }
        if (gc.getEditableTracks().length <= 1) { allRadioButton.setEnabled( false ); }
        add( selectionRadioButton );
        add( currentRadioButton );
        add( allRadioButton );
        setBorder( new TitledBorder( title ) );
    }
    
    /**
     * Gets the edit focus that has been set by the user.
     * @return <code>true</code>, if the according edit is applicable for all
     *         MIDI events on all tracks.
     */
    public boolean isApplicableForAllTracks() { return allRadioButton.isSelected(); }

    /**
     * Gets the edit focus that has been set by the user.
     * @return <code>true</code>, if the according edit is applicable for all
     *         MIDI events on current track.
     */
    public boolean isApplicableForCurrentTrack() { return currentRadioButton.isSelected(); }

    /**
     * Gets the edit focus that has been set by the user.
     * @return <code>true</code>, if the according edit is applicable for user selected
     *         MIDI events only.
     */
    public boolean isApplicableForSelection() { return selectionRadioButton.isSelected(); }
    
    void setClientData( Object clientData )
    {
        this.clientData = clientData;
    }
    
    Object getClientData() { return clientData; }
    
    AbstractButton getSelectionButton()
    {
        return selectionRadioButton;
    }
    
    AbstractButton getAllButton()
    {
        return allRadioButton;
    }
}
