/*
 * Created on 05.12.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.grid;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.ui.swing.SegmentDisplayLabel;
import com.jonasreese.util.Updatable;

/**
 * <p>
 * This class implements the horizontal Grid view's control panel.
 * </p>
 * @author jreese
 */
public class HorizontalGridControlPanel extends JPanel implements PropertyChangeListener, Updatable
{
    
    private static final long serialVersionUID = 1;
    
    private GridController gridController;
    JSlider zoomSlider;
    private JSpinner gridTickSpacingSpinner;
    private JComboBox tracksComboBox;
    private SegmentDisplayLabel tempoLabel;
    private JLabel tempoMeasureLabel;
    private boolean bpmMeasure;
    private JComponent spacerComponent;
    
    /**
     * Constructs a new <code>HorizontalGridControlPanel</code>.
     * @param gridController The according <code>GridController</code>.
     */
    public HorizontalGridControlPanel( GridController gridController )
    {
        super();
        //super( SgEngine.getInstance().getResourceBundle().getString(
        //        "plugin.gridView.toolBar" ) );
        
        //setRollover( true );
        setLayout( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
        
        spacerComponent = new Spacer();
        add( spacerComponent );
        
        ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        this.gridController = gridController;
        
        gridController.getGridComponent().addPropertyChangeListener( this );
        gridController.getGridComponent().addMouseWheelListener( new MouseWheelListener() {
            public void mouseWheelMoved( MouseWheelEvent e ) {
                if (e.isControlDown()) {
                    if (e.getUnitsToScroll() < 0) {
                        zoomSlider.setValue( zoomSlider.getValue() + 1 );
                    } else {
                        zoomSlider.setValue( zoomSlider.getValue() - 1 );
                    }
                    e.consume();
                }
            }
        } );

        // create control panel
        zoomSlider = new JSlider( JSlider.HORIZONTAL, -9, 9, 0 );
        adjustZoom( gridController.getGridComponent().getZoom() );
        zoomSlider.setPaintLabels( false );
        //zoomSlider.setPaintTrack( false );
        zoomSlider.setSnapToTicks( true );
        zoomSlider.setMinorTickSpacing( 1 );
        zoomSlider.setMajorTickSpacing( 9 );
        zoomSlider.setPaintTicks( true );
        zoomSlider.addChangeListener( new ChangeListener()
        {
            public void stateChanged( ChangeEvent e )
            {
                JSlider slider = (JSlider) e.getSource();
                int val = slider.getValue();
                double zoom = 1.0;
                if (val < 0) { zoom = zoom + (val * 0.1); }
                else if (val > 0) { zoom = zoom + val; }
                HorizontalGridControlPanel.this.gridController.getGridComponent().setZoom( zoom );
            }
        } );
        //JLabel zoomLabel = new JLabel( rb.getString( "plugin.gridView.zoom" ) );
        //zoomLabel.setLabelFor( zoomSlider );
        //add( zoomLabel );
        add( zoomSlider );
        addSeparator();
        //JPanel gcControlPanel = new JPanel( new GridLayout( 1, 3 ) );
        JLabel l1 = new JLabel( rb.getString( "plugin.gridView.selectTrackToEdit" ) );
        tracksComboBox = gridController.getTracksComboBox();
        tracksComboBox.setMaximumSize( tracksComboBox.getPreferredSize() );
        l1.setLabelFor( tracksComboBox );
        add( l1 );
        add( tracksComboBox );
        addSeparator();
        AbstractButton showGridCheckBox = gridController.getGridToggleButton();
        showGridCheckBox.setFocusPainted( false );
        add( showGridCheckBox );
            
        gridTickSpacingSpinner = new JSpinner(
            new SpinnerNumberModel(
                gridController.getGridComponent().getGridDivisor(), 1, 64, 1 )
        {
            private static final long serialVersionUID = 1;
            public Object getNextValue()
            {
                setStepSize( (Number) getValue() );
                return super.getNextValue();
            }
            public Object getPreviousValue()
            {
                int val = ((Integer) getValue()).intValue();
                if (val > 1) { val = val / 2; }
                setStepSize( new Integer( val ) );
                return super.getPreviousValue();
            }
        } );
        gridTickSpacingSpinner.addChangeListener( new ChangeListener()
        {
            public void stateChanged( ChangeEvent e )
            {
                JSpinner spinner = (JSpinner) e.getSource();
                HorizontalGridControlPanel.this.gridController.getGridComponent().setGridDivisor(
                    ((Integer) spinner.getValue()).intValue() );
            }
        } );
        gridTickSpacingSpinner.setMaximumSize(
            gridTickSpacingSpinner.getPreferredSize() );
        add( gridTickSpacingSpinner );
        
        bpmMeasure = true;
        
        MouseAdapter ma = new MouseAdapter()
        {
            public void mousePressed( MouseEvent e )
            {
                bpmMeasure = !bpmMeasure;
                update( new float[] {
                    HorizontalGridControlPanel.this.gridController.getMidiDescriptor().getTempoInBpm(),
                    HorizontalGridControlPanel.this.gridController.getMidiDescriptor().getTempoInMpq() } );
            }
        };
        
        tempoLabel = new SegmentDisplayLabel( "     . " );
        tempoLabel.addMouseListener( ma );
        tempoMeasureLabel = new JLabel();
        tempoMeasureLabel.addMouseListener( ma );
        addSeparator();
        update( new float[] {
            gridController.getMidiDescriptor().getTempoInBpm(),
            gridController.getMidiDescriptor().getTempoInMpq() } );
        gridController.setTempoUpdatable( this );
        add( tempoLabel );
        add( tempoMeasureLabel );
        add( UiToolkit.createToolbarButton( gridController.getSetTempoAction() ) );
    }
    
    public void addSeparator()
    {
        add( new Spacer() );
    }

    public void update( Object o )
    {
        float bpm = ((float[]) o)[0];
        float mpq = ((float[]) o)[1];
        String display;
        if (bpmMeasure)
        {
            tempoMeasureLabel.setText( "BPM" );
            int t = (int) (bpm * 10);
            if (t < 0)
            {
                display = "none . ";
            }
            else
            {
                String s = Integer.toString( t );
                s = s.substring( 0, s.length() - 1 ) + "." + s.substring( s.length() - 1 );
                display = s;
            }
        }
        else
        {
            tempoMeasureLabel.setText( "MPQ" );
            int t = (int) (bpm * 10);
            if (t < 0)
            {
                display = "none . ";
            }
            else
            {
                display = "" + ((int) mpq);
            }
        }
        
        while (display.length() < 7)
        {
            display = " " + display;
        }
        tempoLabel.setDisplay( display );
    }
    
    void setVerticalControlPanel( VerticalGridControlPanel verticalPanel )
    {
        Dimension d = new Dimension( verticalPanel.getPreferredSize().width, 1 );
        spacerComponent.setPreferredSize( d );
        spacerComponent.setMinimumSize( d );
        spacerComponent.setMaximumSize( d );
    }
    
    void adjustZoom( double zoom )
    {
        if (zoom >= 1.0)
        {
            zoomSlider.setValue( (int) Math.round( zoom - 1 ) );
        }
        else
        {
            zoomSlider.setValue( -10 + (int) Math.round( zoom * 10.0 ) );
        }
    }
    
    /*
     */
    public void propertyChange( PropertyChangeEvent e )
    {
        if ("zoom".equals( e.getPropertyName() ))
        {
            double zoom = ((Double) e.getNewValue()).doubleValue();
            adjustZoom( zoom );
        }
        else if ("gridDivisor".equals( e.getPropertyName() ))
        {
            gridTickSpacingSpinner.setValue( e.getNewValue() );
        }
//        else if ("track".equals( e.getPropertyName() ))
//        {
//            Track track = (Track) e.getNewValue();
//            Track[] tracks = gridController.getGridComponent().getMidiDescriptor().getSequence().getTracks();
//            for (int i = 0; i < tracks.length; i++)
//            {
//                if (tracks[i] == track)
//                {
//                    tracksComboBox.setSelectedIndex( i );
//                    break;
//                }
//            }
//        }
    }
    
    private static class Spacer extends JComponent
    {
        private static final long serialVersionUID = 1;
        public Spacer()
        {
            Dimension d = new Dimension( 10, 1 );
            setPreferredSize( d );
            setMaximumSize( d );
            setMinimumSize( d );
        }
    }
}