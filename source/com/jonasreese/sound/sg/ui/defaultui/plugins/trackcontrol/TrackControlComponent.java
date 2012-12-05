/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 17.09.2004
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.trackcontrol;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.border.EtchedBorder;

import com.jonasreese.sound.sg.ObjectSelectionChangeListener;
import com.jonasreese.sound.sg.ObjectSelectionChangedEvent;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiChangeMonitor;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.MidiRecorder;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.midi.MidiUpdatable;
import com.jonasreese.sound.sg.midi.SgMidiSequence;
import com.jonasreese.sound.sg.midi.TrackProxy;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.functionality.Functionality;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.util.resource.ResourceLoader;
import com.jonasreese.util.swing.DefaultPopupListener;

/**
 * This class implements the graphical panel for the <i>TrackControl</i> plugin.
 * The TrackControl plugin allows the user to manage the tracks within a MIDI element,
 * such as muting tracks or selecting them for record or solo play.
 * @author jonas.reese
 */
public class TrackControlComponent extends JPanel
    implements PropertyChangeListener, MidiUpdatable, MidiChangeMonitor, ObjectSelectionChangeListener {
    
    private static final long serialVersionUID = 1;
    
    private static final Icon[] RSM_ICON = {
        new ResourceLoader(TrackControlComponent.class, "resource/r.gif").getAsIcon(),
        new ResourceLoader(TrackControlComponent.class, "resource/s.gif").getAsIcon(),
        new ResourceLoader(TrackControlComponent.class, "resource/m.gif").getAsIcon(),
        new ResourceLoader(TrackControlComponent.class, "resource/r_active.gif").getAsIcon(),
        new ResourceLoader(TrackControlComponent.class, "resource/s_active.gif").getAsIcon(),
        new ResourceLoader(TrackControlComponent.class, "resource/m_active.gif").getAsIcon()
    };
    
    private MidiDescriptor midiDescriptor;
    private JScrollPane scrollPane;
    private TrackList trackList;
    private DefaultPopupListener popupListener;
    private Session session;

    /**
     * Constructs a new <code>TrackControlComponent</code>.
     * @param session The <code>Session</code>.
     * @param midiDescriptor The <code>MidiDescriptor</code> being edited/displayed by
     *        this <code>TrackControlComponent</code>. The code in this class expects
     *        the <code>SgSequence</code> within the given <code>MidiDescriptor</code>
     *        to be loaded an thus to be not <code>null</code>.
     */
    public TrackControlComponent(Session session, MidiDescriptor midiDescriptor) {
        super( new BorderLayout() );
        this.session = session;
        this.midiDescriptor = midiDescriptor;

        setFocusable( true );
        
        // add MIDI track menu functionality to context menu
        Plugin p = SgEngine.getInstance().getPlugin(
                "com.jonasreese.sound.sg.ui.defaultui.plugins.midimenu.MidiMenuFunctionality" );

        if (p instanceof Functionality) {
            Functionality f = (Functionality) p;
            // create popup menu
            try {
                JPopupMenu popupMenu = new JPopupMenu();
                popupMenu.add( (Action) f.getProperty( "addTrackAction" ) );
                popupMenu.add( (Action) f.getProperty( "renameTrackAction" ) );
                popupMenu.add( (Action) f.getProperty( "deleteTrackAction" ) );
                popupMenu.addSeparator();
                popupMenu.add( (Action) f.getProperty( "assignOutputDevicesAction" ) );
                popupMenu.add( (Action) f.getProperty( "assignInputDevicesAction" ) );
                popupListener = new DefaultPopupListener( popupMenu );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        trackList = new TrackList();
        scrollPane = new JScrollPane( trackList );
        scrollPane.getVerticalScrollBar().setBlockIncrement( 150 );
        scrollPane.getHorizontalScrollBar().setBlockIncrement( 150 );
        scrollPane.getVerticalScrollBar().setUnitIncrement( 20 );
        scrollPane.getHorizontalScrollBar().setUnitIncrement( 20 );
        add( scrollPane );
    }
    
    public boolean isFocusable() {
        return true;
    }
    
    /**
     * Called by the ViewInstance implementation.
     */
    void open() {
        session.addObjectSelectionChangeListener( this );
    }
    
    /**
     * Called by the ViewInstance implementation.
     */
    void close() {
        session.removeObjectSelectionChangeListener( this );
    }
    
    public void objectSelectionChanged( ObjectSelectionChangedEvent e ) {
        SessionElementDescriptor[] descs = e.getSelectedElements();
        if (descs == null || descs.length != 1 || !(descs[0] instanceof MidiDescriptor)) {
            setMidiDescriptor( null );
        } else {
            setMidiDescriptor( (MidiDescriptor) descs[0] );
        }
    }

    private void setMidiDescriptor( MidiDescriptor midiDescriptor ) {
        
        if (this.midiDescriptor == midiDescriptor) {
            return;
        }
        
        if (this.midiDescriptor != null) {
            this.midiDescriptor.removePropertyChangeListener( this );
            this.midiDescriptor.getMidiRecorder().removeMidiUpdatable( this );
            try {
                this.midiDescriptor.getSequence().removeMidiChangeMonitor( this );
            } catch (InvalidMidiDataException e) {
            } catch (IOException e) {
            }
        }

        this.midiDescriptor = midiDescriptor;
        
        updateTitle();
        if (this.midiDescriptor != null) {
            this.midiDescriptor.addPropertyChangeListener(this);
            this.midiDescriptor.getMidiRecorder().addMidiUpdatable(this);
            try {
                this.midiDescriptor.getSequence().addMidiChangeMonitor(this);
            } catch (InvalidMidiDataException e) {
            } catch (IOException e) {
            }
        }
        trackList.update();
        trackList.updateMuteStates();
        trackList.updateRecordStates();
        trackList.updateSoloStates();
    }
    
    
    /**
     * Called by the ViewInstance implementation.
     */
    void activate() {
        // add own menues/menu items to menu bar
        try {
            if (midiDescriptor != null) {
                midiDescriptor.getSequence().setSelectedTrackProxy( trackList.getSelectedTrack(), this );
            }
        } catch (InvalidMidiDataException e) {
        } catch (IOException e) {
        }
    }

    /**
     * Called by the ViewInstance implementation.
     */
    void deactivate() {
    }

    /**
     * Shall be invoked when the window title has to be updated.
     */
    void updateTitle() {
        ViewContainer vc = (ViewContainer) UiToolkit.getViewContainer(this);
        if (vc != null) {
            vc.setTitleText(
                SgEngine.getInstance().getResourceBundle().getString("plugin.trackControl.name")
                + (midiDescriptor == null ? "" :
                    " - " + midiDescriptor.getName() + (midiDescriptor.isChanged() ? "*" : "")));
        }
    }
    
    /**
     * Returns the currently selected track.
     * @return The current track.
     */
    public TrackProxy getSelectedTrack() {
        return trackList.getSelectedTrack();
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent e) {
        if ("changed".equals(e.getPropertyName())) {
            updateTitle();
        }
    }

    /* (non-Javadoc)
     */
    public void deviceUpdate(MidiRecorder recorder, MidiDescriptor midiDescriptor, int updateHint) {
        if ((updateHint & RECORD_ENABLE_STATE) != 0) {
            //System.out.println("record state changed");
            trackList.updateRecordStates();
        }
        if ((updateHint & SOLO_STATE) != 0) {
            //System.out.println("solo state changed");
            trackList.updateSoloStates();
        }
        if ((updateHint & MUTE_STATE) != 0) {
            //System.out.println("mute state changed");
            trackList.updateMuteStates();
        }
    }

    /* (non-Javadoc)
     */
    public void deviceClosed(MidiDevice device, MidiDescriptor midiDescriptor) {
        // what shall be done here?
    }

    /* (non-Javadoc)
     */
    public void midiEventsAdded(SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj) {
        if (events.length == 1 &&
                MidiToolkit.isTrackNameEvent(events[0])) {
            trackList.update();
            trackList.updateMuteStates();
            trackList.updateSoloStates();
            trackList.updateRecordStates();
        }
    }

    /* (non-Javadoc)
     */
    public void midiEventsRemoved(SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj) {
        if (events.length == 1 &&
                MidiToolkit.isTrackNameEvent(events[0])) {
            trackList.update();
            trackList.updateMuteStates();
            trackList.updateSoloStates();
            trackList.updateRecordStates();
        }
    }

    /* (non-Javadoc)
     */
    public void midiEventsChanged(SgMidiSequence sequence, TrackProxy track, MidiEvent[] events, Object changeObj) {
        if (events.length == 1 &&
                MidiToolkit.isTrackNameEvent(events[0])) {
            trackList.update();
            trackList.updateMuteStates();
            trackList.updateSoloStates();
            trackList.updateRecordStates();
        }
    }

    /* (non-Javadoc)
     */
    public void midiTrackAdded(SgMidiSequence sequence, TrackProxy track, Object changeObj) {
        trackList.insertTrack(track, sequence.getIndexOf(track));
        trackList.updateMuteStates();
        trackList.updateRecordStates();
        trackList.updateSoloStates();
    }

    /* (non-Javadoc)
     */
    public void midiTrackRemoved(SgMidiSequence sequence, TrackProxy track, Object changeObj) {
        trackList.removeTrack(track);
        trackList.updateMuteStates();
        trackList.updateRecordStates();
        trackList.updateSoloStates();
    }

    /* (non-Javadoc)
     */
    public void midiTrackLengthChanged(SgMidiSequence sequence, TrackProxy track, Object changeObj) {
        // nothing to do...
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.midi.MidiChangeMonitor#midiTrackEventMapChanged(com.jonasreese.sound.sg.midi.SgMidiSequence, com.jonasreese.sound.sg.midi.TrackProxy, java.lang.Object)
     */
    public void midiTrackEventMapChanged( SgMidiSequence sequence, TrackProxy track, Object changeObj ) {
    }

    class RsmButton extends JToggleButton implements ActionListener {
        private static final long serialVersionUID = 1;
        
        int rsm;
        ListEntry listEntry;
        TrackProxy track;
        RsmButton(int rsm, ListEntry listEntry, TrackProxy track) {
            this.rsm = rsm;
            this.listEntry = listEntry;
            this.track = track;
            setFocusPainted(false);
            setBorderPainted(false);
            setMargin(new Insets(2, 2, 2, 2));
            setFocusable(false);
            addActionListener(this);
            updateIcon();
        }
        void updateIcon() {
            Icon icon = RSM_ICON[rsm + (isSelected() ? 3 : 0)];
            if (icon == getIcon()) {
                return;
            }
            setIcon(icon);
            setMaximumSize(new Dimension(getIcon().getIconWidth(), getIcon().getIconHeight()));
        }
        public void setSelected(boolean selected) {
            super.setSelected(selected);
            updateIcon();
        }
        public void actionPerformed(ActionEvent e) {
            updateIcon();
            if (rsm == 0) {
                boolean b = isSelected();
                midiDescriptor.getMidiRecorder().setRecordEnabled(listEntry.track, b);
                if (b && midiDescriptor.getMidiRecorder().getMidiInputMap(listEntry.track).isEmpty()) {
                    UiToolkit.showAssignMidiInputDevicesDialog( midiDescriptor, listEntry.track );
                }
            } else if (rsm == 1) {
                midiDescriptor.getMidiRecorder().setTrackSolo(track, isSelected());
            } else if (rsm == 2) {
                midiDescriptor.getMidiRecorder().setTrackMuted(track, isSelected());
            }
        }
    }
    
    class ListEntry extends JPanel implements MouseListener {
        private static final long serialVersionUID = 1;
        
        TrackProxy track;
        int index;
        boolean highlighted;
        boolean selected;
        RsmButton rButton;
        RsmButton sButton;
        RsmButton mButton;
        JLabel label;
        Color selectionForeground;
        Color selectionBackground;
        Color foreground;
        Color background;
        ListEntry(TrackProxy track, int index) {
            super(new FlowLayout(FlowLayout.LEFT));
            this.track = track;
            this.index = index;
            rButton = new RsmButton(0, this, track);
            rButton.addMouseListener(this);
            add(rButton);
            sButton = new RsmButton(1, this, track);
            sButton.addMouseListener(this);
            add(sButton);
            mButton = new RsmButton(2, this, track);
            mButton.addMouseListener(this);
            add(mButton);
            label = new JLabel(getCaption());
            label.addMouseListener(this);
            add(label);
            highlighted = true;
            setHighlighted(false);
            selected = false;
            addMouseListener(this);
            JList jlist = new JList();
            selectionForeground = jlist.getSelectionForeground();
            selectionBackground = jlist.getSelectionBackground();
            foreground = getForeground();
            background = getBackground();
            this.setFocusable( true );
            addMouseListener( popupListener );
            label.addMouseListener( popupListener );
        }
        void setHighlighted(boolean highlighted) {
            if (this.highlighted == highlighted) {
                return;
            }
            this.highlighted = highlighted;
            setBorder(new EtchedBorder((highlighted ? EtchedBorder.RAISED : EtchedBorder.LOWERED)));
        }
        void setSelected(boolean selected) {
            if (this.selected == selected) {
                return;
            }
            this.selected = selected;
            
            setBackground((selected ? selectionBackground : background));
            setForeground((selected ? selectionForeground : foreground));
            label.setBackground((selected ? selectionBackground : background));
            label.setForeground((selected ? selectionForeground : foreground));
        }
        public Dimension getMaximumSize() {
            return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
        }
        String getCaption() {
            String name = track.getTrackName();
            if (name == null) {
                name =
                    SgEngine.getInstance().getResourceBundle().getString("track")
                    + " " + (index + 1);
            }
            return name;
        }
        public void mouseClicked(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {
            if (!(e.getSource() instanceof RsmButton)) {
                TrackControlComponent.this.trackList.setSelectedIndex(index);
            }
        }
        public void mouseReleased(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {
            setHighlighted(true);
        }
        public void mouseExited(MouseEvent e) {
            setHighlighted(false);
        }
    }
    
    class TrackList extends JPanel {
        private static final long serialVersionUID = 1;
        
        int selectedIndex;
        ArrayList<ListEntry> listEntries;
        TrackList() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            selectedIndex = -1;
            setFocusable( true );
            enableEvents(MouseEvent.MOUSE_EVENT_MASK);
            enableEvents(KeyEvent.KEY_EVENT_MASK);
            listEntries = new ArrayList<ListEntry>();
            update();
        }
        
        void update() {
            removeAll();
            TrackProxy[] tracks = null;
            if (midiDescriptor != null) {
                try {
                    tracks = midiDescriptor.getSequence().getTrackProxies();
                } catch (InvalidMidiDataException e) {
                } catch (IOException e) {
                }
            }
            if (tracks != null) {
                listEntries.clear();
                // create all list entries and add them to layout
                // later, only changes are performed...
                for (int i = 0; i < tracks.length; i++) {
                    ListEntry entry = new ListEntry(tracks[i], i);
                    listEntries.add(entry);
                    add(entry);
                }
            } else {
                listEntries.clear();
            }
            revalidate();
            repaint();
        }
        protected void processKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (selectedIndex > 0) {
                        setSelectedIndex( selectedIndex - 1 );
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (selectedIndex + 1 < listEntries.size()) {
                        setSelectedIndex( selectedIndex + 1 );
                    }
                }
            }
        }
        protected void processMouseEvent(MouseEvent e) {
            if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                setSelectedIndex( -1 );
            }
        }
        TrackProxy getSelectedTrack() {
            if (selectedIndex < 0 || selectedIndex >= listEntries.size()) {
                return null;
            }
            return ((ListEntry) listEntries.get(selectedIndex)).track;
        }
        void setSelectedIndex(int selectedIndex) {
            if (midiDescriptor == null) {
                return;
            }
            if (this.selectedIndex != selectedIndex) {
                if (this.selectedIndex >= 0 && this.selectedIndex < listEntries.size()) {
                    ((ListEntry) listEntries.get(this.selectedIndex)).setSelected(false);
                }
            }
            this.selectedIndex = selectedIndex;
            if (selectedIndex >= 0 && selectedIndex < listEntries.size()) {
                ((ListEntry) listEntries.get(selectedIndex)).setSelected(true);
            } else {
                this.selectedIndex = -1;
            }
            try {
                midiDescriptor.getSequence().setSelectedTrackProxy( getSelectedTrack(), TrackControlComponent.this );
            } catch (InvalidMidiDataException e) {
            } catch (IOException e) {
            }
        }
        void insertTrack(TrackProxy track, int index) {
            if (index >= listEntries.size()) {
                ListEntry entry = new ListEntry(track, listEntries.size());
                listEntries.add(entry);
                add(entry);
            } else {
                ListEntry entry = new ListEntry(track, index);
                for (int i = index; i < listEntries.size(); i++) {
                    remove((ListEntry) listEntries.get(i));
                }
                listEntries.add(index, entry);
                for (int i = index; i < listEntries.size(); i++) {
                    ListEntry le = (ListEntry) listEntries.get(i);
                    le.index = i;
                    le.label.setText(le.getCaption());
                    add(le);
                }
            }
            int selectedIndex = this.selectedIndex;
            if (index <= selectedIndex) {
                selectedIndex++;
            }
            setSelectedIndex(-1);
            setSelectedIndex(selectedIndex);
            revalidate();
        }
        void removeTrack(TrackProxy track) {
            int index = -1;
            for (int i = 0; index < 0 && i < listEntries.size(); i++) {
                ListEntry entry = (ListEntry) listEntries.get(i);
                if (entry.track == track) {
                    index = i;
                    remove(entry);
                }
            }
            revalidate();
            if (index < 0) { return; }
            if (selectedIndex == index) {
                setSelectedIndex(-1);
            }
            listEntries.remove(index);
            for (int i = index; i < listEntries.size(); i++) {
                ListEntry entry = (ListEntry) listEntries.get(i);
                entry.index = i;
                entry.label.setText(entry.getCaption());
            }
            setSelectedIndex(selectedIndex);
        }
        void updateRecordStates() {
            for (int i = 0; i < listEntries.size(); i++) {
                ListEntry entry = (ListEntry) listEntries.get(i);
                boolean b = midiDescriptor.getMidiRecorder().isRecordEnabled(entry.track);
                entry.rButton.setSelected(b);
                if (b) {
                    //System.out.println("record enabled for track " + (i + 1));
                }
            }
        }
        void updateSoloStates() {
            for (int i = 0; i < listEntries.size(); i++) {
                ListEntry entry = (ListEntry) listEntries.get(i);
                entry.sButton.setSelected(midiDescriptor.getMidiRecorder().isTrackSolo(entry.track));
            }
        }
        void updateMuteStates() {
            for (int i = 0; i < listEntries.size(); i++) {
                ListEntry entry = (ListEntry) listEntries.get(i);
                entry.mButton.setSelected(midiDescriptor.getMidiRecorder().isTrackMuted(entry.track));
            }
        }
    }
}