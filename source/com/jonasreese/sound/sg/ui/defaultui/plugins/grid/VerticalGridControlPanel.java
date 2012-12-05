/*
 * Created on 07.10.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.grid;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * <p>
 * This class implements the vertical Grid view's control panel.
 * </p>
 * @author jreese
 */
public class VerticalGridControlPanel extends JPanel implements PropertyChangeListener
{
    private static final long serialVersionUID = 1;
    
    private GridController gridController;
    JSlider zoomSlider;
    
    /**
     * Constructs a new <code>HorizontalGridControlPanel</code>.
     * @param gridController The according <code>GridController</code>.
     */
    public VerticalGridControlPanel( GridController gridController )
    {
        super();
        //super( SgEngine.getInstance().getResourceBundle().getString(
        //        "plugin.gridView.toolBar" ) );
        
        //setRollover( true );
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        
        this.gridController = gridController;
        
        gridController.getGridComponent().addPropertyChangeListener( this );
        gridController.getGridComponent().addMouseWheelListener( new MouseWheelListener() {
            public void mouseWheelMoved( MouseWheelEvent e ) {
                if (e.isShiftDown()) {
                    if (e.getUnitsToScroll() < 0) {
                        zoomSlider.setValue( zoomSlider.getValue() + 1 );
                    } else {
                        zoomSlider.setValue( zoomSlider.getValue() - 1 );
                    }
                    e.consume();
                } else if (e.isControlDown()) {
                } else {
                    Container parent = e.getComponent().getParent();
                    if (parent != null) {
                        parent.dispatchEvent( e );
                    }
                }
            }
        } );


        // create control panel
        int val = gridController.getGridComponent().getRowHeight();
        if (val > 33) { val = 33; }
        if (val < 3) { val = 3; }
        zoomSlider = new JSlider( JSlider.VERTICAL, 3, 33, val );
        zoomSlider.setPaintLabels( false );
        //zoomSlider.setPaintTrack( false );
        zoomSlider.setSnapToTicks( false );
        zoomSlider.setMinorTickSpacing( 3 );
        zoomSlider.setMajorTickSpacing( 15 );
        zoomSlider.setPaintTicks( true );
        zoomSlider.addChangeListener( new ChangeListener()
        {
            public void stateChanged( ChangeEvent e )
            {
                JSlider slider = (JSlider) e.getSource();
                int val = slider.getValue();
                VerticalGridControlPanel.this.gridController.getGridComponent().setRowHeight( val );
            }
        } );
        //JLabel zoomLabel = new JLabel( rb.getString( "plugin.gridView.zoom" ) );
        //zoomLabel.setLabelFor( zoomSlider );
        //add( zoomLabel );
        add( zoomSlider );
    }

    void setHorizontalControlPanel( HorizontalGridControlPanel horizontalPanel )
    {
        Dimension d = new Dimension(
            zoomSlider.getPreferredSize().width, horizontalPanel.zoomSlider.getPreferredSize().width );
        zoomSlider.setPreferredSize( d );
        zoomSlider.setMaximumSize( d );
    }
    
    /*
     */
    public void propertyChange( PropertyChangeEvent e )
    {
        if ("rowHeight".equals( e.getPropertyName() ))
        {
            int rowHeight = ((Integer) e.getNewValue()).intValue();
            if (rowHeight < zoomSlider.getMinimum()) { rowHeight = zoomSlider.getMinimum(); }
            if (rowHeight > zoomSlider.getMaximum()) { rowHeight = zoomSlider.getMaximum(); }
            zoomSlider.setValue( rowHeight );
        }
    }
}