/*
 * Created on 09.03.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.ParseException;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiToolkit;

/**
 * <p>
 * A <code>JPanel</code> implementation for setting the MIDI tempo
 * both in MPQ (Microseconds Per Quarternote) and BPM (Beats Per Minute).
 * </p>
 * @author jreese
 */
public class SetTempoPanel extends JPanel {
    
    private static final long serialVersionUID = 1;
    
    private JRadioButton mpqRadioButton;
    
    private JSpinner mpqField;
    private JSpinner bpmField;
    
    private float bpm;
    private boolean changed;
    
    /**
     * Constructs a new <code>SetTempoPanel</code>.
     * @param mpq The mpq value.
     */
    public SetTempoPanel( float mpq )     {
        super( new BorderLayout() );
        
        changed = false;
        bpm = MidiToolkit.mpqToBPM( mpq );
        
        ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        mpqRadioButton = new JRadioButton(
            rb.getString( "midi.tempo.title.mpq" ), false );
        JRadioButton bpmRadioButton = new JRadioButton(
            rb.getString( "midi.tempo.title.bpm" ), true );
        
        ButtonGroup bgr = new ButtonGroup();
        int width = Math.max( mpqRadioButton.getPreferredSize().width,
            bpmRadioButton.getPreferredSize().width );
        mpqRadioButton.setPreferredSize(
            new Dimension( width, mpqRadioButton.getPreferredSize().height ) );
        bpmRadioButton.setPreferredSize(
            new Dimension( width, bpmRadioButton.getPreferredSize().height ) );
        bgr.add( mpqRadioButton );
        bgr.add( bpmRadioButton );

        mpqField = new JSpinner( new SpinnerNumberModel(
            new Float( mpq ),
            new Float( 1 ),
            new Float( 9999999 ),
            new Float( 10 ) ) );
        mpqField.addChangeListener( new ChangeListener() {
			public void stateChanged( ChangeEvent e ) {
                changed = true;
                bpmField.setValue(
                    new Float(
                        MidiToolkit.mpqToBPM(
                            ((Float) mpqField.getValue()).floatValue() ) ) );
			}
        } );
        JPanel mpqPanel = new JPanel();
        mpqPanel.add( mpqRadioButton );
        mpqPanel.add( mpqField );
        if (bpm < 20) { bpm = 20; }
        if (bpm > 280) { bpm = 280; }
        bpmField = new JSpinner( new SpinnerNumberModel(
            new Float( bpm ),
            new Float( 20 ),
            new Float( 280 ),
            new Float( 1 ) ) );

        ((DefaultEditor) bpmField.getEditor()).getTextField().addKeyListener( new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        bpmField.commitEdit();
                    } catch (ParseException ignore) {
                    }
                }
            }
            public void keyReleased(KeyEvent e) {
            }
            public void keyTyped(KeyEvent e) {
            }
        } );

        ((DefaultEditor) mpqField.getEditor()).getTextField().addKeyListener( new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        mpqField.commitEdit();
                    } catch (ParseException ignore) {
                    }
                }
            }
            public void keyReleased(KeyEvent e) {
            }
            public void keyTyped(KeyEvent e) {
            }
        } );
        
        bpmField.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                changed = true;
                mpqField.setValue(
                    new Float(
                        MidiToolkit.bpmToMPQ(
                            ((Float) bpmField.getValue()).floatValue() ) ) );
            }
        } );
        width = Math.max(
            mpqField.getPreferredSize().width, bpmField.getPreferredSize().width );
        mpqField.setPreferredSize( new Dimension( width, mpqField.getPreferredSize().height ) );
        bpmField.setPreferredSize( new Dimension( width, bpmField.getPreferredSize().height ) );
        JPanel bpmPanel = new JPanel();
        bpmPanel.add( bpmRadioButton );
        bpmPanel.add( bpmField );
        
        add( mpqPanel, BorderLayout.SOUTH );
        add( bpmPanel, BorderLayout.NORTH );


        ActionListener al = new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                boolean b = mpqRadioButton.isSelected();
                mpqField.setEnabled( b );
                bpmField.setEnabled( !b );
                if (b) {
                    ((DefaultEditor) mpqField.getEditor()).getTextField().requestFocus();
                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            ((DefaultEditor) mpqField.getEditor()).getTextField().selectAll();
                        }
                    } );
                } else {
                    ((DefaultEditor) bpmField.getEditor()).getTextField().requestFocus();
                    SwingUtilities.invokeLater( new Runnable() {
                        public void run() {
                            ((DefaultEditor) bpmField.getEditor()).getTextField().selectAll();
                        }
                    } );
                }
            }
        };
        al.actionPerformed( null );

        mpqRadioButton.addActionListener( al );
        bpmRadioButton.addActionListener( al );
        bpmField.requestFocusInWindow();
        
        FocusListener fl = new FocusListener() {
            public void focusGained(final FocusEvent e) {
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        ((JTextField) e.getComponent()).selectAll();
                    }
                } );
            }
            public void focusLost(FocusEvent e) {
            }
        };
        ((DefaultEditor) bpmField.getEditor()).getTextField().addFocusListener( fl );
        ((DefaultEditor) mpqField.getEditor()).getTextField().addFocusListener( fl );
    }
    
    public void requestFocus() {
        final JTextField tf = ((DefaultEditor) bpmField.getEditor()).getTextField();
        tf.requestFocus();
    }
    
    /**
     * Returns true if the user entered in BPM.
     * @return see above.
     */
    public boolean isInBPM() { return !mpqRadioButton.isSelected(); }
    
    /**
     * Returns true if the user entered in MPQ.
     * @return see above.
     */
    public boolean isInMPQ() { return mpqRadioButton.isSelected(); }
    
    /**
     * Gets the MPQ value.
     * @return The MPQ entered by the user.
     */
    public float getMPQValue() { return ((Float) mpqField.getValue()).floatValue(); }

    /**
     * Gets the BPM value.
     * @return The BPM entered by the user.
     */
    public float getBPMValue() { return ((Float) bpmField.getValue()).floatValue(); }
    
    /**
     * Requests if the user changed one of the temp values. 
     * @return <code>true</code> if the user changed something, <code>false</code> otherwise.
     */
    public boolean hasChanged() {
        return changed;
    }
}
