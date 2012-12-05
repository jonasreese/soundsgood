/*
 * Created on 03.09.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jonasreese.sound.sg.SgEngine;

/**
 * <p>
 * A <code>JPanel</code> implementation for setting the parameters for
 * repeating MIDI notes.
 * </p>
 * @author jonas.reese
 */
public class RepeatNotePanel extends JPanel {
    
    private static final long serialVersionUID = 1;
    
    private JSpinner countPerTactField;
    private JSpinner tactCountField;
    
    /**
     * Constructs a new <code>RepeatNotePanel</code>.
     * @param countPerTact The default number of repeatings per tact.
     * @param tactCount The default number of tacts for repeating.
     */
    public RepeatNotePanel( int countPerTact, int tactCount ) {
        super( new BorderLayout() );
        
        ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        JLabel cptLabel = new JLabel( rb.getString( "midi.note.repeat.perTact" ) );
        
        countPerTactField = new JSpinner( new SpinnerNumberModel(
            new Integer( countPerTact ),
            new Integer( 1 ),
            new Integer( 32 ),
            new Integer( 1 ) ) );
        cptLabel.setLabelFor( countPerTactField );
        JPanel cptPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        cptPanel.add( cptLabel );
        cptPanel.add( countPerTactField );
        tactCountField = new JSpinner( new SpinnerNumberModel(
            new Integer( tactCount ),
            new Integer( 1 ),
            new Integer( 800 ),
            new Integer( 1 ) ) );

        JPanel tcPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );

        countPerTactField.setPreferredSize( tactCountField.getPreferredSize() );
        
        JLabel tcLabel = new JLabel( rb.getString( "midi.note.repeat.tacts" ) );
        tcLabel.setLabelFor( tactCountField );
        tcPanel.add( tcLabel );
        tcPanel.add( tactCountField );
        
        add( cptPanel, BorderLayout.SOUTH );
        add( tcPanel, BorderLayout.NORTH );
    }
    
    /**
     * Gets the tact count value.
     * @return The tact count entered by the user.
     */
    public int getTactCountValue() { return ((Number) tactCountField.getValue()).intValue(); }

    /**
     * Gets the count per tact value.
     * @return The count per tact entered by the user.
     */
    public int getCountPerTactValue() { return ((Number) countPerTactField.getValue()).intValue(); }
}
