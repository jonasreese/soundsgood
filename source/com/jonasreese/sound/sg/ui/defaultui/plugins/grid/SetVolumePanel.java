/*
 * Copyright (c) 2004 Jonas Reese
 * Created on 02.01.2004
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.grid;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

/**
 * <p>
 * A panel that allows the user to set a volume for
 * a number of MIDI notes.
 * </p>
 * @author jreese
 */
public class SetVolumePanel extends JPanel
{
    private static final long serialVersionUID = 1;
    
    private JSpinner volumeSpinner;
    
    /**
     * Constructs a new <code>SetVolumePanel</code>.
     * @param gridComponent The parent <code>GridComponent</code>.
     * @param defaultValue The default (initial) volume value.
     * @param title The title.
     * @param volumeCaption The label for the volume spinner.
     */
    public SetVolumePanel(
        GridComponent gridComponent,
        int defaultValue,
        String title,
        String volumeCaption )
    {
        super( new FlowLayout( FlowLayout.RIGHT ) );

        volumeSpinner = new JSpinner(
            new SpinnerNumberModel(
                new Integer( (defaultValue & 127) ),
                new Integer( 0 ),
                new Integer( 127 ), // 7-bit value
                new Integer( 1 ) ) );
        JLabel volumeLabel = new JLabel( volumeCaption );
        add( volumeLabel );
        add( volumeSpinner );

        setBorder( new TitledBorder( title ) );
        
    }
    
    public boolean hasChanged() { return true; }
    
    /**
     * Gets the volume that has been adjusted by the user.
     * @return The volume. A value between 0 and 127
     */
    public int getVolume()
    {
        return ((Integer) volumeSpinner.getValue()).intValue();
    }
}
