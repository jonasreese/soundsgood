/*
 * Created on 24.12.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;

import com.jonasreese.sound.sg.midi.EventDescriptor;
import com.jonasreese.sound.sg.midi.EventMap;
import com.jonasreese.sound.sg.midi.TrackProxy;
import com.jonasreese.ui.swing.SubListSelectionPanel;

/**
 * <p>
 * This class implements a UI that allows the user to edit an event map.
 * </p>
 * @author jonas.reese
 */
public class EditEventMapPanel extends JPanel {

    private static final long serialVersionUID = 1;
    
    private SubListSelectionPanel eventTypeList;
    

    /**
     * Creates a new <code>EditEventMapPanel</code> object.
     * @param eventMap The <code>EventMap</code> that shall be edited.
     * @param map The 'map' string (button label).
     * @param unmap The 'unmap' string (button label).
     * @param mapAll The 'map all' string (button label).
     * @param mapNone The 'map none' string (button label).
     */
    public EditEventMapPanel(
            EventMap eventMap, String map, String unmap, String mapAll, String mapNone ) {
        super( new BorderLayout() );
        
        JPanel innerPanel = new JPanel( new BorderLayout() );
        
        Action toLeftAction = new AbstractAction( unmap ) {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e ) {
            }
        };
        Action toRightAction = new AbstractAction( map ) {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e ) {
            }
        };
        Action allToLeftAction = new AbstractAction( mapNone ) {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e ) {
            }
        };
        Action allToRightAction = new AbstractAction( mapAll ) {
            private static final long serialVersionUID = 1;
            public void actionPerformed( ActionEvent e ) {
            }
        };
        eventTypeList = new SubListSelectionPanel( new Object[0], new Object[0] , toLeftAction, toRightAction );
        eventTypeList.setAllToLeftAction( allToLeftAction );
        eventTypeList.setAllToRightAction( allToRightAction );
        eventTypeList.getSplitPane().setBorder( null );
        eventTypeList.setKeepLeftOrder( true );
        innerPanel.add( eventTypeList );


        EventMap defaultMap = TrackProxy.createDefaultEventMap();
        EventDescriptor[] eds = defaultMap.getEventDescriptors();
        ArrayList<EventDescWrapper> leftList = new ArrayList<EventDescWrapper>();
        ArrayList<EventDescWrapper> rightList = new ArrayList<EventDescWrapper>();
        for (int i = 0; i < eds.length; i++) {
            int index = eventMap.getIndexFor( eds[i] );
            if (index < 0) {
                leftList.add( new EventDescWrapper( eds[i] ) );
            }
        }
        eventTypeList.setLeftObjects( leftList );
        eds = eventMap.getEventDescriptors();
        for (int i = 0; i < eds.length; i++) {
            rightList.add( new EventDescWrapper( eds[i] ) );
        }
        eventTypeList.setRightObjects( rightList );
        add( innerPanel );
    }
    
    public SubListSelectionPanel getEventTypeList() {
        return eventTypeList;
    }

    public static class EventDescWrapper {
        EventDescriptor ed;
        EventDescWrapper( EventDescriptor ed ) {
            this.ed = ed;
        }
        
        public EventDescriptor getEventDescriptor() {
            return ed;
        }
        
        public String toString() {
            return ed.getDescription();
                //+ (ed instanceof NoteDescriptor ? " (" + ((NoteDescriptor) ed).getNote() + ")" : "");
        }
    }
}
