/*
 * Copyright (c) 2004 Jonas Reese
 * Created on 24.01.2004
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.grid;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <p>
 * A panel that allows the user to increase or decrease the volume of
 * a number of MIDI notes.
 * </p>
 * @author jreese
 */
public class XcreaseVolumePanel extends JPanel
{
    
    private static final long serialVersionUID = 1;
    
    private JSpinner factorSpinner;
    private JSpinner volumeSpinner;
    private JLabel functionLabel;
    private String functionPrefix;
    private boolean increase;
    
    /**
     * Constructs a new <code>XcreaseVolumePanel</code>.
     * @param increase If set to <code>true</code>, an increase dialog
     *        is created, otherwise a decrease dialog.
     * @param gridComponent The parent <code>GridComponent</code>.
     * @param linear If set to <code>true</code>, indicates that
     *        a linear function shall be used.
     * @param constant If set to <code>true</code>, indicates that
     *        a constant value shall be used.
     * @param defaultFactor The default factor for a linear function.
     * @param defaultValue The default (initial) volume value.
     * @param title The title.
     * @param factorCaption The label for the linear function factor spinner.
     * @param constantCaption The label for the constant volume spinner.
     */
    public XcreaseVolumePanel(
        boolean increase,
        GridComponent gridComponent,
        boolean linear,
        boolean constant,
        float defaultFactor,
        int defaultValue,
        String title,
        String factorCaption,
        String constantCaption,
        String functionPrefix )
    {
        super( new BorderLayout() );
        this.increase = increase;
        this.functionPrefix = functionPrefix;

        ChangeListener cl = new ChangeListener()
        {
            public void stateChanged( ChangeEvent e )
            {
                updateFunction();
            }
        };

        factorSpinner = new JSpinner(
            new SpinnerNumberModel(
                new Float( defaultFactor ),
                new Float( (increase ? 1 : 0) ),
                new Float( (increase ? 127 : 1) ), // 7-bit value
                new Float( 0.1 ) ) );
        factorSpinner.addChangeListener( cl );

        volumeSpinner = new JSpinner(
            new SpinnerNumberModel(
                new Integer( (defaultValue & 127) ),
                new Integer( 0 ),
                new Integer( 127 ), // 7-bit value
                new Integer( 1 ) ) );
        volumeSpinner.addChangeListener( cl );
        factorSpinner.setPreferredSize( volumeSpinner.getPreferredSize() );
        
        JLabel factorLabel = new JLabel( factorCaption );
        JPanel p0 = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        p0.add( factorLabel );
        p0.add( factorSpinner );

        JLabel volumeLabel = new JLabel( constantCaption );
        JPanel p1 = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        p1.add( volumeLabel );
        p1.add( volumeSpinner );

        functionLabel = new JLabel( "?", JLabel.RIGHT );
        add( p0, BorderLayout.NORTH );
        add( p1 );
        add( functionLabel, BorderLayout.SOUTH );

        setBorder( new TitledBorder( title ) );
        updateFunction();
    }
    
    private void updateFunction()
    {
        float fac = ((Float) factorSpinner.getValue()).floatValue();
        int con = ((Integer) volumeSpinner.getValue()).intValue();
        NumberFormat df = DecimalFormat.getInstance();
        String facStr = df.format( fac );
        String conStr = df.format( con );
        String addSymbol = (increase ? "+" : "-");
        functionLabel.setText(
            functionPrefix + ": " +
            "f(x) = " + ((fac != 1.0) ? facStr : "") +
            "x " + ((con != 0) ? addSymbol + " " + conStr : "") + "      " );
    }
    
    public boolean hasChanged() { return true; }
    
    /**
     * Gets linear function's constant set by the user.
     * @return The constant. A value between 0 and 127.
     */
    public int getConstant()
    {
        return ((Integer) volumeSpinner.getValue()).intValue();
    }
    
    /**
     * Gets linear function's factor set by the user.
     * @return The factor. A value between 0.0 and 1.0 if <code>increase</code>
     *         is <code>false</code>, a value between 1.0 and 127.0 otherwise.
     */
    public float getFactor()
    {
        return ((Float) factorSpinner.getValue()).floatValue();
    }
}
