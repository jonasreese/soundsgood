/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 15.12.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.grid;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.EventDescriptor;
import com.jonasreese.sound.sg.midi.EventMap;
import com.jonasreese.sound.sg.midi.NoteDescriptor;
import com.jonasreese.sound.sg.midi.TrackProxy;
import com.jonasreese.sound.sg.midi.edit.MoveEventMapEdit;
import com.jonasreese.sound.sg.midi.edit.RemoveEventMapEdit;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.util.ParametrizedResourceBundle;

/**
 * <p>
 * This class is the component that is used as a ruler component at
 * the right side of a <code>JScrollPane</code> that contains a
 * <code>GridComponent</code>. It has some user input and MIDI event mapping
 * functionalities. The relationship between
 * <code>EventMappingComponent</code> and <code>GridComponent</code>
 * shall always be 1:1.
 * </p>
 * @author jreese
 */
class EventMappingComponent extends JComponent {
    private static final long serialVersionUID = 1;
    
    private GridComponent gridComponent;
    private GridController gridController;
    private Font labelFont;
    private EMLabel highlighted;
    private int componentWidth;
    private int rowHeight;
    private JComponent blackLine;
    
    /**
     * Constructs a new <code>EventMappingComponent</code>.
     * @param gridComponent The <code>GridComponent</code> to be controlled.
     */
    public EventMappingComponent( GridComponent gridComponent, GridController gridController ) {
        this.gridComponent = gridComponent;
        this.gridController = gridController;
        MouseMotionAdapter mma = new MouseMotionAdapter() {
            public void mouseMoved( MouseEvent e ) {
                int y = e.getY();
                if (e.getSource() != EventMappingComponent.this.gridComponent) {
                    y += ((Component) e.getSource()).getY();
                }
                highlight( y );
            }
            public void mouseDragged( MouseEvent e ) {
                mouseMoved( e );
            }
        };
        gridComponent.addMouseMotionListener( mma );
        gridComponent.addMouseListener( new MouseAdapter() {
            public void mouseExited( MouseEvent e  ) {
                highlight( - 1 );
            }
        } );
        highlighted = null;
        componentWidth = 50;
        rowHeight = -1;
        
        blackLine = new JComponent() {
            private static final long serialVersionUID = 1L;
            public void paintComponent( Graphics g ) {
                g.setColor( Color.BLACK );
                Rectangle r = g.getClipBounds();
                g.fillRect( r.x, r.y, r.width, r.height );
            }
        };
        addMouseListener( new MouseAdapter() {
            public void mouseReleased( MouseEvent e ) {
                if (e.isPopupTrigger()) {
                    createPopupMenu( null ).show( e.getComponent(), e.getX(), e.getY() );
                }
            }
        } );
    }
    
    private JPopupMenu createPopupMenu( final EMLabel label ) {
        JPopupMenu menu = new JPopupMenu();
        ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        if (label != null) {
            AbstractAction action = new AbstractAction(
                    rb.getString(
                            "plugin.gridView.eventmap.removeEvent", "\"" +
                            label.ed.getDescription() + "\"" ) ) {
                private static final long serialVersionUID = 1L;
                public void actionPerformed( ActionEvent e ) {
                    RemoveEventMapEdit edit = new RemoveEventMapEdit(
                            label.ed.getEventMap(), label.ed.getEventMap().getIndexFor( label.ed ) );
                    edit.perform();
                    gridComponent.getMidiDescriptor().getUndoManager().addEdit( edit );
                }
            };
            action.putValue( SgAction.TOOL_TIP_TEXT,
                rb.getString( "plugin.gridView.eventmap.removeEvent.description" ) );
            menu.add( action );
        }
        menu.add( gridController.getEditNoteMappingAction() );
        menu.addSeparator();
        menu.add( gridController.getExtractNoteMappingAction() );
        menu.add( gridController.getImportNoteMappingAction() );
        menu.add( gridController.getExportNoteMappingAction() );
        menu.add( gridController.getDefaultNoteMappingAction() );
        return menu;
    }
    
    private void highlight( int y ) {
        if (y < 0) {
            EMLabel highlighted = this.highlighted;
            if (highlighted != null) {
                highlighted.setHighlighted( false );
            }
        } else {
            Component c = getComponentAt( getSize().width / 2, y );
            if (c instanceof EMLabel) {
                ((EMLabel) c).setHighlighted( true );
                highlighted = (EMLabel) c;
            }
        }
    }
    
    /**
     * Updates the layout of the existing labels.
     */
    void updateLayout() {
        int width = getComponentWidth();
        int rowHeight = gridComponent.getRowHeight();
        boolean setFont = false;
        if (this.rowHeight != rowHeight || labelFont != null) {
            labelFont = calculateBestFont();
            setFont = true;
        }
        int height = 0;
        for (int i = 0; i < getComponentCount(); i++) {
            Component c = getComponent( i );
            if (c instanceof EMLabel) {
                EMLabel label = (EMLabel) c;
                int y = gridComponent.translateEventY( label.ed );
                if (setFont) {
                    label.setFont( labelFont );
                }
                label.setBounds( 0, y, width, rowHeight );
                if (y + rowHeight > height) {
                    height = y + rowHeight;
                }
            }
        }
        this.rowHeight = rowHeight;
        setPreferredSize( new Dimension( getComponentWidth(), height ) );
        setSize( getPreferredSize() );
    }
    
    /**
     * Updates the labelling.
     */
    void updateLabels() {
        //System.out.println( "EventMappingComponent.updateLabels()" );
        int width = getComponentWidth();
        int rowHeight = gridComponent.getRowHeight();
        removeAll();
        if (this.rowHeight != rowHeight || labelFont == null) {
            labelFont = calculateBestFont();
        }
        EventMap em = gridComponent.getEventMap();
        int max = em.getSize();
        int height = 0;
        for (int i = 0; i < max; i++) {
            EventDescriptor ed = em.getEventAt( i );
            EMLabel label = new EMLabel( ed );
            int y = gridComponent.translateEventY( ed );
            label.setBounds( 0, y, width, rowHeight );
            if (y + rowHeight > height) {
                height = y + rowHeight;
            }
            add( label );
        }
        setPreferredSize( new Dimension( getComponentWidth(), height ) );
        setSize( getPreferredSize() );
        this.rowHeight = rowHeight;
    }
    
    private Font calculateBestFont() {
        Font font = getFont();
        if (font == null) { return null; }
        boolean working = true;
        int rowHeight = gridComponent.getRowHeight();

        while (working) {
            FontMetrics fm = getFontMetrics( font );
            if (fm == null) {
                System.out.println( "fontMetrics == null" );
                return font;
            }
            if (fm.getHeight() > rowHeight) {
                int fs = font.getSize() - 1;
                if (fs <= 1) { working = false; }
                else
                {
                    font = new Font( font.getName(), font.getStyle(), fs );
                }
            } else {
                working = false;
            }
        }
        
        return font;
    }
    
    public int getComponentWidth() {
        return componentWidth;
    }
    
    public void setComponentWidth( int componentWidth ) {
        this.componentWidth = componentWidth;
    }
    
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
    
    
    class EMLabel extends JTextField implements ActionListener {
        private static final long serialVersionUID = 1;
        
        EventDescriptor ed;
        boolean hl;
        boolean dragged;
        boolean paintText;
        EMLabel( EventDescriptor ed ) {
            super( (labelFont != null ? ed.getDescription() : "") );
            this.ed = ed;
            setDisabledTextColor( getForeground() );
            setEditMode( false );
            setBorder( null );
            paintText = (labelFont != null);
            if (paintText) {
                setFont( labelFont );
            }
            hl = false;
            dragged = false;

            addActionListener( this );
        }
        
        public void setEditMode( boolean b ) {
            if (!b) {
                setSelectionStart( 0 );
                setSelectionEnd( 0 );
                setCaretPosition( 0 );
            }
            setEnabled( b );
            setEditable( b );
            if (b) {
                setBackground( Color.WHITE );
            } else {
                setBackground( null );
            }
        }
        protected void processKeyEvent( KeyEvent e ) {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                setText( ed.getDescription() );
                setEditMode( false );
            } else {
                super.processKeyEvent( e );
            }
        }
        protected void processMouseEvent( MouseEvent e ) {
            if (!paintText) { return; }
            if (e.getID() == MouseEvent.MOUSE_CLICKED) {
                if (e.getClickCount() == 2) {
                    setEditMode( true );
                    selectAll();
                    requestFocus();
                }
                if (e.getClickCount() == 1) {
                    if (e.isControlDown()) {
                        gridComponent.selectRow( ed.getEventMap().getIndexFor( ed ) );
                    } else if (e.isShiftDown()) {
                        gridComponent.deselectRow( ed.getEventMap().getIndexFor( ed ) );
                    }
                }
            } else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                if (e.getClickCount() == 1 && !isEditable()) {
                    if (ed instanceof NoteDescriptor) {
                        try {
                            int channel = 0;
                            TrackProxy track = gridComponent.getTrack();
                            MidiEvent event = track.getLastShortMessageNoteOnEvent();
                            if (event != null && event.getMessage() instanceof ShortMessage) {
                                channel = ((ShortMessage) event.getMessage()).getChannel();
                            }
                            NoteDescriptor nd = (NoteDescriptor) ed;
                            ShortMessage sm = new ShortMessage();
                            sm.setMessage( ShortMessage.NOTE_ON, channel, nd.getNote(), 127 );
                            MidiEvent on = new MidiEvent( sm, 0 );
                            sm = new ShortMessage();
                            sm.setMessage( ShortMessage.NOTE_ON, channel, nd.getNote(), 0 );
                            double d = (double) track.getParent().getTickLength() /
                                (double) track.getParent().getMicrosecondLength();
                            MidiEvent off = new MidiEvent( sm, (long) (d * 250000.0) );
                            gridComponent.getMidiDescriptor().getMidiRecorder().playSingleNote( on, off );
                        } catch (Exception any) {
                            any.printStackTrace();
                        }
                    }
                }
            } else if (e.getID() == MouseEvent.MOUSE_ENTERED) {
                setHighlighted( true );
            } else if (e.getID() == MouseEvent.MOUSE_EXITED) {
                setHighlighted( false );
            } else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                if (e.isPopupTrigger()) {
                    createPopupMenu( this ).show( this, e.getX(), e.getY() );
                    if (dragged) {
                        dragged = false;
                        EventMappingComponent.this.remove( blackLine );
                        EventMappingComponent.this.repaint( blackLine.getBounds() );
                    }
                } else {
                    if (dragged) {
                        dragged = false;
                        EventMappingComponent.this.remove( blackLine );
                        EventMappingComponent.this.repaint( blackLine.getBounds() );
                        EventDescriptor newEvent = gridComponent.translateYPos( e.getY() + getY() );
                        int oldIndex = ed.getEventMap().getIndexFor( ed );
                        int newIndex = newEvent.getEventMap().getIndexFor( newEvent );
                        MoveEventMapEdit edit = new MoveEventMapEdit( ed.getEventMap(), oldIndex, newIndex );
                        edit.perform();
                        gridComponent.getMidiDescriptor().getUndoManager().addEdit( edit );
                    }
                }
            }
            super.processMouseEvent( e );
        }
        protected void processMouseMotionEvent( MouseEvent e ) {
            if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
                if (isEditable()) {
                    super.processMouseMotionEvent( e );
                } else {
                    EventDescriptor newEvent = gridComponent.translateYPos( e.getY() + getY() );
                    int newIndex = newEvent.getEventMap().getIndexFor( newEvent );
                    if (!dragged) {
                        EventMappingComponent.this.add( blackLine, 0 );
                        dragged = true;
                    }
                    if (e.getY() - rowHeight / 2 > 0) {
                        newIndex++;
                    }
                    blackLine.setBounds( 0, newIndex * rowHeight - 1, EventMappingComponent.this.getWidth(), 3 );
                    blackLine.repaint();
                }
            } else {
                super.processMouseMotionEvent( e );
            }
        }
        public void actionPerformed( ActionEvent e ) {
            setEditMode( false );
            String text = getText();
            if (text != null && !text.equals( ed.getDescription() )) {
                ed.setDescription( text );
            }
        }
        protected void processFocusEvent( FocusEvent e ) {
            if (!paintText) { return; }
            if (e.getID() == FocusEvent.FOCUS_LOST) {
                setText( ed.getDescription() );
                setEditMode( false );
            }
            super.processFocusEvent( e );
        }
        private void setHighlighted( boolean b ) {
            if (b && this.hl) { return; }
            this.hl = b;
            if (highlighted != null) {
                highlighted.hl = false;
                if (highlighted.isEditable()) {
                    highlighted.setBackground( Color.WHITE );
                } else {
                    highlighted.setBackground( null );
                }
            }
            if (b) {
                if (!isEditable()) {
                    setBackground( Color.YELLOW );
                }
                highlighted = this;
            } else {
                highlighted = null;
            }
        }
    }
}