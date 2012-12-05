/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 08.10.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.grid;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;

/**
 * <b>
 * This class implements the <code>ViewInstance</code> for the
 * <code>GridView</code> class.
 * </b>
 * @author jreese
 */
public class GridVi implements ViewInstance
{
    private View parent;
    private JComponent uiObject;
    private GridController gridController;
    
    /**
     * Constructs a new <code>GridVi</code> object.
     * @param parent The parent <code>View</code>.
     * @param midiDescriptor The <code>MidiDescriptor</code> keeping the information
     *        required to gain the MIDI sequence.
     */
    public GridVi( View parent, MidiDescriptor midiDescriptor )
    throws InvalidMidiDataException, IOException {
        this.parent = parent;
        gridController = new GridController( midiDescriptor, parent );

        // create UI object
        uiObject = new JPanel( new BorderLayout() );
        HorizontalGridControlPanel horizontalControlPanel = new HorizontalGridControlPanel( gridController );
        VerticalGridControlPanel verticalControlPanel = new VerticalGridControlPanel( gridController );
        horizontalControlPanel.setVerticalControlPanel( verticalControlPanel );
        verticalControlPanel.setHorizontalControlPanel( horizontalControlPanel );
        uiObject.add( horizontalControlPanel, BorderLayout.NORTH );
        uiObject.add( verticalControlPanel, BorderLayout.WEST );
        uiObject.add( gridController.getGridScrollPane() );
    }
    
	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.ViewInstance#getUiObject()
	 */
	public Object getUiObject( ViewContainer parentUiObject ) {
		return uiObject;
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.ViewInstance#getView()
	 */
	public View getView() {
		return parent;
	}

    public void activate() {
        // select the current MidiDescriptor
        gridController.getMidiDescriptor().select();
        
        // activate the grid component
        gridController.activate();
    }
    
    public void deactivate() {
        gridController.deactivate();
    }

    public void open() {
        gridController.open();
    }

	public void close() {
        gridController.close();
	}

	public boolean isSetBoundsAllowed() {
		return true;
	}
    
//    public void finalize() throws Throwable {
//        System.out.println( "finalizing GridVi" );
//        super.finalize();
//    }
}
