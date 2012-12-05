/*
 * Created on 23.03.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.util.AbstractEventRedirector;
import com.jonasreese.util.Base64;
import com.jonasreese.util.Bitdata;
import com.jonasreese.util.EventQueueHandler;
import com.jonasreese.util.Updatable;

/**
 * <p>
 * This class has mainly the same functionalities as a <code>javax.sound.midi.Track</code>,
 * but implements a notification architecture that allows other objects to be notified
 * of changes within a MIDI track. Additionally, advanced MIDI track manipulation
 * functionality is provided by this class. 
 * </p>
 * <p>
 * Thus, a <code>TrackProxy</code> shall be used rather than a <code>Track</code> if
 * changes are made on a MIDI track.
 * </p>
 * @author jreese
 */
public class TrackProxy {
    private static final String EVENTMAP_PROPERTY_NAME = "eventmap";

    private Track track;
    private SgMidiSequence parent;
    private boolean asyncMode;
    private long length;
    private int size;
    private List<MidiEvent> cachedEvents;
    private List<EventWrapper> addedEvents;
    private List<EventWrapper> removedEvents;
    private Set<MidiEvent> selectedEvents;
    private List<MidiEvent> selectedEventList;
    private MidiEvent eot;
    private int eventId;
    private EventMapImpl eventMap;
    private boolean eventMapChanged;

    private List<MidiEventSelectionListener> midiEventSelectionListeners;
    private Updatable propertyHook;
    
    /**
     * Constructs a new <code>TrackProxy</code>.
     * @param track The MIDI <code>Track</code> to be wrapped by this <code>TrackProxy</code>.
     * @param parent The parent MIDI sequence.
     */
    TrackProxy( Track track, SgMidiSequence parent ) {
        this.track = track;
        this.parent = parent;
        asyncMode = false;
        selectedEvents = new HashSet<MidiEvent>() {
            private static final long serialVersionUID = 1;
            public boolean add( MidiEvent o ) {
                if (isMetaEndOfTrack( o )) {
                    return false;
                }
                return super.add( o );
            }
        };
        selectedEventList = new ArrayList<MidiEvent>();
        if (track.ticks() == 0) {
            try {
                setLength( 1, null );
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }
        midiEventSelectionListeners = new ArrayList<MidiEventSelectionListener>();
        
        eventMapChanged = false;
        propertyHook = new Updatable() {
            public void update( Object o ) {
                if (eventMapChanged) {
                    System.out.println( "TrackProxy property hook: persisting event map" );
                    MidiDescriptor midiDescriptor = (MidiDescriptor) o;
                    midiDescriptor.putPersistentClientProperty(
                            TrackProxy.this, EVENTMAP_PROPERTY_NAME, Base64.encode( getEventMap().toBinary() ) );
                    eventMapChanged = false;
                }
            }
        };
    }

    /**
     * Loads the default properties for this <code>TrackProxy</code> from the
     * parent <code>MidiDescriptor</code>'s persistent client properties.
     */
    public void restoreProperties() {
        long time = System.currentTimeMillis();
        String s = (parent.getMidiDescriptor() != null ?
                parent.getMidiDescriptor().getPersistentClientProperty( this, EVENTMAP_PROPERTY_NAME ) : null);
        if (s != null) {
            byte[] b = Base64.decode( s );
            try {
                getEventMap();
                eventMap.loadFromBinaryImpl( b );
            } catch (IOException e) {
            }
        }
        System.out.println( "restoreProperties() took " + (System.currentTimeMillis() - time) );
    }
    
    /**
     * Gets the size (in MidiEvents) of the underlying <code>Track</code>.
     * @return The size.
     */
    public int size() {
        return (asyncMode ? size : track.size());
    }
    
    /**
     * Gets the size (in ticks) of the underlying <code>Track</code>.
     * @return The size in ticks.
     */
    public long ticks() {
        return (asyncMode ? length : track.ticks());
    }
    
    /**
     * Gets the length (in ticks) of the track. This method returns the same value as
     * the <code>ticks()</code> method.
     * @return
     */
    public long getLength() {
        return ticks();
    }
    
    /**
     * Sets the length (in ticks) of this <code>TrackProxy</code>.
     * @param ticks The length in MIDI ticks.
     * @param who The object that caused the change.
     * @throws InvalidMidiDataException if there are MIDI events at
     * a position after <code>ticks</code>. Then, the track length cannot
     * be set to the given value.
     */
    public void setLength( long ticks, Object who ) throws InvalidMidiDataException {
        if (ticks == ticks()) { return; }
        // check if length can be set to the given value
        if (track.size() > 1) {
            MidiEvent lastEvent = track.get( track.size() - 2 );
            if (lastEvent.getTick() == 0 && ticks == 0) {
                ticks = 1;
            }
            if (ticks <= lastEvent.getTick()) {
                throw new InvalidMidiDataException(
                        "cannot set length to " + ticks + " " + lastEvent.getMessage() +
                        " event at tick " + lastEvent.getTick() + ", track size is " + track.size() );
            }
        }
        if (ticks < ticks()) {
            // remove EOT event to reduce length
            track.remove( track.get( track.size() - 1 ) );
        }
        MidiEvent eot = new MidiEvent( new EOT(), ticks );
        track.add( eot );
        this.eot = eot;
        this.length = ticks;
        parent.fireTrackLengthChanged( this, who );
    }

    private boolean addInCorrectOrder( MidiEvent event ) {
        int i = cachedEvents.size();
        for ( ; i > 0; i--) {
            if (event.getTick() >= ((MidiEvent) cachedEvents.get( i - 1 )).getTick()) {
                break;
            }
        }
//        if (i < cachedEvents.size()) {
//            System.out.println( "inserting" );
//        } else {
//            System.out.println( "adding" );
//        }
        cachedEvents.add( i, event );
        addedEvents.add( new EventWrapper( event ) );
        return true;
    }
    
    /**
     * Adds a <code>MidiEvent</code> to this <code>TrackProxy</code> (and to it's
     * underlying <code>Track</code> object, of course).<br>
     * A call to this method causes all registered <code>MidiChangeMonitor</code>
     * objects to be invoked.
     * @param event The event to be added.
     * @param who The object that caused the change.
     * @return <code>true</code> if the event has been added, <code>false</code> otherwise.
     */
    public boolean add( MidiEvent event, Object who ) {
        return add( event, who, true );
    }
    
    /**
     * Adds a <code>MidiEvent</code> to this <code>TrackProxy</code> (and to it's
     * underlying <code>Track</code> object, of course).
     * @param event The event to be added.
     * @param who The object that caused the change.
     * @return <code>true</code> if the event has been added, <code>false</code> otherwise.
     */
    public boolean add( MidiEvent event, Object who, boolean invokeMonitors ) {
        long ticks = ticks();
        boolean b;
        if (asyncMode) {
            b = addInCorrectOrder( event );
        } else {
            b = track.add( event );
        }
        if (b) {
            size++;
            if (event.getTick() > length) {
                length = event.getTick();
            }
            if (invokeMonitors) {
                parent.fireEventAdded( this, event, who );
                if (ticks != ticks()) {
                    parent.fireTrackLengthChanged( this, who );
                }
            }
        }
        return b;
    }
    
    /**
     * Adds all given <code>MidiEvent</code>s to this <code>TrackProxy</code>
     * (and to it's underlying <code>Track</code> object, of course).<br>
     * A call to this method causes all registered <code>MidiChangeMonitor</code>
     * objects to be invoked.
     * @param events The events to be added. Shall not be <code>null</code>, and no
     *        array element shall be <code>null</code>.
     * @param who The object that caused the change.
     */
    public void addAll( MidiEvent[] events, Object who ) {
        addAll( events, who, true );
    }
    
    /**
     * Adds all given <code>MidiEvent</code>s to this <code>TrackProxy</code>
     * (and to it's underlying <code>Track</code> object, of course).
     * @param events The events to be added. Shall not be <code>null</code>, and no
     *        array element shall be <code>null</code>.
     * @param who The object that caused the change.
     * @param invokeMonitors If set to <code>true</code>, a call to this method
     *        causes all registered <code>MidiChangeMonitor</code> objects to be invoked.
     */
    public void addAll( MidiEvent[] events, Object who, boolean invokeMonitors ) {
        if (events == null || events.length == 0) {
            return;
        }
        long ticks = ticks();
        for (int i = 0; i < events.length; i++) {
            boolean b;
            if (asyncMode) {
                b = addInCorrectOrder( events[i] );
            } else {
                b = track.add( events[i] );
            }
            if (b) {
                if (events[i].getTick() > length) {
                    length = events[i].getTick();
                }
                size++;
            }
        }
        if (invokeMonitors) {
            parent.fireEventsAdded( this, events, who );
            if (ticks != ticks()) {
                parent.fireTrackLengthChanged( this, who );
            }
        }
    }
    
    /**
     * Removes the given <code>MidiEvent</code> from this <code>TrackProxy</code>
     * (and from it's underlying <code>Track</code> object, of course).<br>
     * A call to this method causes all registered <code>MidiChangeMonitor</code>
     * objects to be invoked.
     * @param event The MIDI event to be removed.
     * @param who The object that caused the change.
     * @return <code>true</code> if the event has been removed, <code>false</code> otherwise.
     */
    public boolean remove( MidiEvent event, Object who ) {
        return remove( event, who, true );
    }

    /**
     * Removes the given <code>MidiEvent</code> from this <code>TrackProxy</code>
     * (and from it's underlying <code>Track</code> object, of course).
     * @param event The MIDI event to be removed.
     * @param who The object that caused the change.
     * @param invokeMonitors If set to <code>true</code>, a call to this method
     *        causes all registered <code>MidiChangeMonitor</code> objects to be invoked.
     * @return <code>true</code> if the event has been removed, <code>false</code> otherwise.
     */
    public boolean remove( MidiEvent event, Object who, boolean invokeMonitors ) {
        long ticks = ticks();
        boolean b;
        if (asyncMode) {
            b = removedEvents.add( new EventWrapper( event ) );
        } else {
            b = track.remove( event );
        }
        deselect( event, who );
        if (b) {
            size--;
            if (invokeMonitors) {
                parent.fireEventRemoved( this, event, who );
                if (ticks != ticks()) {
                    parent.fireTrackLengthChanged( this, who );
                }
            }
        }
        return b;
    }

    /**
     * Removes all given <code>MidiEvent</code>s from this <code>TrackProxy</code>
     * (and from it's underlying <code>Track</code> object, of course).
     * @param events The MIDI events to be removed. Shall not be <code>null</code>,
     *        and no array element shall be <code>null</code>.
     * @param who The object that caused the change.
     */
    public void removeAll( MidiEvent[] events, Object who ) {
        if (events == null || events.length == 0) {
            return;
        }
        long ticks = ticks();
        for (int i = 0; i < events.length; i++) {
            boolean b;
            if (asyncMode) {
                b = removedEvents.add( new EventWrapper( events[i] ) );
            } else {
                b = track.remove( events[i] );
            }
            if (b) {
                size--;
            }
        }
        deselectAll( events, who );
        parent.fireEventsRemoved( this, events, who );
        if (ticks != ticks()) {
            parent.fireTrackLengthChanged( this, who );
        }
    }
    
    /**
     * Adds a listener that will receive updates when the user changed the midi event
     * selection.
     * @param l The listener to add.
     */
    public void addMidiEventSelectionListener( MidiEventSelectionListener l ) {
        synchronized (midiEventSelectionListeners) {
            midiEventSelectionListeners.add( l );
        }
    }
    
    /**
     * Fires an event to all registered listeners.
     * @param e The event.
     */
    protected void fireMidiEventSelectionEvent( MidiEventSelectionEvent e ) {
        EventQueueHandler eq = SgEngine.getInstance().getEventQueue();
        for (int i = 0; i < midiEventSelectionListeners.size(); i++) {
            eq.addQueueEntry(
                new AbstractEventRedirector(
                    (MidiEventSelectionListener) midiEventSelectionListeners.get( i ) )
            {
                public void redirectEvent( EventObject e )
                {
                    ((MidiEventSelectionListener) getListener()).midiEventSelectionUpdate(
                        (MidiEventSelectionEvent) e );
                }
            }, e );
        }
        eq.processEvents();
    }

    /**
     * Removes a listener that will no longer receive updates when the user changed
     * the midi event selection.
     * @param l The listener to be removed.
     */
    public void removeMidiEventSelectionListener( MidiEventSelectionListener l ) {
        synchronized (midiEventSelectionListeners) {
            midiEventSelectionListeners.remove( l );
        }
    }
    
    /**
     * Adds the given <code>MidiEvent</code> to the selection.
     * @param event The event to be selected. Must not be <code>null</code>.
     * @param who The object that caused the selection change.
     */
    public void select( MidiEvent event, Object who ) {
        boolean b;
        synchronized (selectedEvents) {
            b = selectedEvents.add( event );
            if (b) {
                selectedEventList.add( event );
            }
        }
        if (b) {
            fireMidiEventSelectionEvent( new MidiEventSelectionEvent(
                    who, this, MidiEventSelectionEvent.UPDATE_HINT_SELECTION_ADDED, new MidiEvent[] { event } ) );
        }
    }

    /**
     * Adds the given <code>MidiEvent</code>s to the selection.
     * @param events The events to be selected. Must not be
     * <code>null</code> or contain a <code>null</code> value.
     * @param who The object that caused the selection change.
     */
    public void selectAll( MidiEvent[] events, Object who ) {
        boolean b = false;
        synchronized (selectedEvents) {
            for (int i = 0; i < events.length; i++) {
                if (selectedEvents.add( events[i] )) {
                    selectedEventList.add( events[i] );
                    b = true;
                }
            }
        }
        if (b) {
            fireMidiEventSelectionEvent( new MidiEventSelectionEvent(
                    who, this, MidiEventSelectionEvent.UPDATE_HINT_SELECTION_ADDED, events ));
        }
    }
    
    /**
     * Gets the count of currently selected events.
     * @return The current selected events count.
     */
    public int getSelectedEventCount() {
        synchronized (selectedEvents) {
            return selectedEvents.size();
        }
    }
    
    /**
     * Gets the selected event at the specified index.
     * @param index The index.
     * @return The selected <code>MidiEvent</code> at the given index.
     * @throws ArrayIndexOutOfBoundsException if <code>index</code> is
     * out of bounds (index &lt; 0 or index &gt;= <code>getSelectedEventCount()</code>.
     */
    public MidiEvent getSelectedEventAt( int index ) {
        synchronized (selectedEvents) {
            return (MidiEvent) selectedEventList.get( index );
        }
    }
    
    /**
     * Returns the <code>selected</code> state for the given <code>MidiEvent</code>.
     * @param event The event to get the <code>selected</code> state for.
     * @return <code>true</code> if the given <code>MidiEvent</code> is currently selected,
     * <code>false</code> otherwise.
     */
    public boolean isSelected( MidiEvent event ) {
        synchronized (selectedEvents) {
            return selectedEvents.contains( event );
        }
    }

    /**
     * Gets the selected events as a <code>MidiEvent</code> array.
     * @return A copy of the selected <code>MidiEvent</code> objects.
     */
    public MidiEvent[] getSelectedEvents() {
        synchronized (selectedEvents) {
            MidiEvent[] events = new MidiEvent[selectedEvents.size()];
            int i = 0;
            for (MidiEvent event : selectedEventList) {
                events[i++] = event;
            }
            return events;
        }
    }
    
    /**
     * Gets all NOTE_ON MIDI events that are currently selected.
     * @return A copy of the selected <code>MidiEvent</code> objects.
     */
    public MidiEvent[] getSelectedNoteOnEvents() {
        synchronized (selectedEvents) {
            int count = 0;
            for (MidiEvent e : selectedEventList) {
                if (e.getMessage() instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) e.getMessage();
                    if (sm.getCommand() == ShortMessage.NOTE_ON &&
                            sm.getData2() > 0) {
                        count++;
                    }
                }
            }
            MidiEvent[] events = new MidiEvent[count];
            int i = 0;
            for (MidiEvent e : selectedEventList) {
                if (e.getMessage() instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) e.getMessage();
                    if (sm.getCommand() == ShortMessage.NOTE_ON &&
                            sm.getData2() > 0) {
                        events[i++] = e;
                    }
                }
            }
            return events;
        }
    }
    
    /**
     * Adds the given <code>MidiEvent</code> to the selection.
     * @param event The event to be selected. Must not be <code>null</code>.
     * @param who The object that caused the selection change.
     */
    public void deselect( MidiEvent event, Object who ) {
        boolean b;
        synchronized (selectedEvents) {
            b = selectedEvents.remove( event );
            if (b) {
                selectedEventList.remove( event );
            }
        }
        if (b) {
            fireMidiEventSelectionEvent(
                    new MidiEventSelectionEvent(
                    who,
                    this,
                    MidiEventSelectionEvent.UPDATE_HINT_SELECTION_REMOVED |
                    (isSelectionEmpty() ? MidiEventSelectionEvent.UPDATE_HINT_SELECTION_EMPTY : 0),
                    new MidiEvent[] { event } ));
        }
    }

    /**
     * Adds the given <code>MidiEvent</code>s to the selection.
     * @param events The events to be selected. Must not be
     * <code>null</code> or contain a <code>null</code> value.
     * @param who The object that caused the selection change.
     */
    public void deselectAll( MidiEvent[] events, Object who ) {
        boolean b = false;
        synchronized (selectedEvents) {
            for (int i = 0; i < events.length; i++) {
                if (selectedEvents.remove( events[i] )) {
                    selectedEventList.remove( events[i] );
                    b = true;
                }
            }
        }
        if (b) {
            fireMidiEventSelectionEvent(
                    new MidiEventSelectionEvent(
                    who,
                    this,
                    MidiEventSelectionEvent.UPDATE_HINT_SELECTION_REMOVED |
                    (isSelectionEmpty() ? MidiEventSelectionEvent.UPDATE_HINT_SELECTION_EMPTY : 0),
                    events ));
        }
    }
    
    /**
     * Removes all <code>MidiEvent</code>s from the current selection.
     * @param who The object that caused the selection change.
     */
    public void clearSelection( Object who ) {
        if (selectedEvents.isEmpty()) {
            return;
        }
        synchronized (selectedEvents) {
            selectedEvents.clear();
            selectedEventList.clear();
        }
        fireMidiEventSelectionEvent( new MidiEventSelectionEvent(
                who, this,
                MidiEventSelectionEvent.UPDATE_HINT_SELECTION_REMOVED |
                MidiEventSelectionEvent.UPDATE_HINT_SELECTION_EMPTY, null ) );
    }
    
    /**
     * Selects all <code>MidiEvent</code>s.
     * @param who The object that caused the selection change.
     */
    public void selectAll( Object who ) {
        boolean b = false;
        synchronized (selectedEvents) {
            for (int i = 0; i < size(); i++) {
                MidiEvent e = get( i );
                if (!MidiToolkit.isTrackNameEvent( e ) &&
                        !MidiToolkit.isTempoEvent( e )) {
                    if (selectedEvents.add( e )) {
                        b = true;
                        selectedEventList.add( e );
                    }
                }
            }
        }
        if (b) {
            fireMidiEventSelectionEvent( new MidiEventSelectionEvent(
                    who, this,
                    MidiEventSelectionEvent.UPDATE_HINT_SELECTION_ADDED |
                    MidiEventSelectionEvent.UPDATE_HINT_SELECTION_FULL, null ) );
        }
    }
    
    /**
     * Returns <code>true</code> if and only if the selection is empty.
     * @return <code>true</code> if and only if the selection is empty.
     */
    public boolean isSelectionEmpty() {
        return selectedEvents.isEmpty();
    }
    
    /**
     * Returns <code>true</code> if all events on this track are selected.
     * @return <code>true</code> if all events are selected.
     */
    public boolean isAllSelected() {
        return selectedEvents.size() + 1 >= size();
    }
    
    /**
     * Invokes all registered <code>MidiChangeMonitor</code> objects that the
     * given <code>MidiEvent</code>s have changed. Since the change cannot be
     * detected automatically, you need to call this method manually when you
     * want to avoid removing events and then re-adding them.
     * @param events The <code>MidiEvent</code>s that have changed. Please be
     *        honest and pass only those events that have really changed!
     * @param who The object that caused the change.
     */
    public void fireEventsChanged( MidiEvent[] events, Object who ) {
        parent.fireEventsChanged( this, events, who );
    }
    
    /**
     * Gets the <code>MidiEvent</code> at the specified index.
     * @param index The index.
     * @return The according <code>MidiEvent</code>.
     */
    public MidiEvent get( int index ) {
        if (asyncMode) {
            if (index == cachedEvents.size()) {
                return eot;
            } else {
                return  (MidiEvent) cachedEvents.get( index );
            }
        } else {
            return track.get( index );
        }
    }
    
    /**
     * Gets a copy of all <code>MidiEvent</code>s on this <code>TrackProxy</code>, as
     * a <code>MidiEvent</code> array.
     * @return A copied array.
     */
    public MidiEvent[] getAllMidiEvents() {
        MidiEvent[] events;
        if (asyncMode) {
            events = new MidiEvent[size];
            for (int i = 0; i < events.length; i++) {
                events[i] = (MidiEvent) cachedEvents.get( i );
            }
            events[size - 1] = eot;
        } else {
            events = new MidiEvent[track.size()];
            for (int i = 0; i < events.length; i++) {
                events[i] = track.get( i );
            }
        }
        return events;
    }
    
    /**
     * Gets the <code>Track</code> that is wrapped by this <code>TrackProxy</code>.
     * @return The underlying <code>Track</code>. Please be careful with modifying
     *         this track (or just don't do it - use this <code>TrackProxy</code> instead)!
     */
    public Track getTrack() {
        return track;
    }
    
    /**
     * Sets the <code>Track</code> that is wrapped by this <code>TrackProxy</code>.
     * This method should usually <b>not</b> be called, since a call to this method
     * may lead to severe inconsistency. Some track-related edit actually do have
     * to call this method because of the rather unsatisfyingly desinged
     * javax.sound.midi API.
     * @param track The <code>Track</code> to set.
     */
    void setTrack( Track track ) {
        this.track = track;
    }
    
    /**
     * This method is called when this <code>TrackProxy</code> has been added to
     * it's parent.
     */
    void addedToParent() {
        if (this.parent != null) {
            this.parent.getMidiDescriptor().addPropertyHook( propertyHook );
        }
    }
    
    /**
     * This method is called when this <code>TrackProxy</code> has been removed
     * from it's parent.
     */
    void removedFromParent() {
        if (this.parent != null) {
            this.parent.getMidiDescriptor().removePropertyHook( propertyHook );
        }
    }
    
    /**
     * Gets the parent <code>SgMidiSequence</code>.
     * @return The non-<code>null</code> parent <code>SgMidiSequence</code>.
     */
    public SgMidiSequence getParent() {
        return parent;
    }
    
    /**
     * Sets the parent <code>MidiSequence</code>.
     * @param parent The new parent to set.
     */
    void setParent( SgMidiSequence parent ) {
        this.parent = parent;
    }
    
    /**
     * Gets the last ShortMessage MIDI event on this track.
     * @return The last <code>MidiEvent</code> containing a
     * <code>ShortMessage</code>, or <code>null</code> if none found.
     */
    public MidiEvent getLastShortMessageNoteOnEvent() {
        int index = track.size() - 1;
        boolean secondOne = false;
        for (; index > 0; index--) {
            MidiEvent event = track.get( index );
            if (event.getMessage() instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage) event.getMessage();
                if (sm.getCommand() == ShortMessage.NOTE_ON) {
                    if (sm.getData2() > 0 || secondOne) {
                        return event;
                    } else if (sm.getData2() == 0) {
                        secondOne = true;
                    }
                } else {
                    secondOne = false;
                }
            }
        }
        return null;
    }
    
    /**
     * Gets the next <code>MidiEvent</code> either before or after the given tick.
     * @param tick The tick where to start searching.
     * @param before If set to <code>true</code>, indicates that the search shall start
     * at <code>tick</code> and then go backwards in time. Otherwise, it starts at
     * <code>tick</code> and goes forward in time.
     * @return The <code>MidiEvent</code> next to the given tick. If there is an
     * appropriate event at exactly the given tick, that event is returned. If no event
     * is found, <code>null</code> is returned.
     */
    public MidiEvent getMidiEventNextTo( long tick, boolean before ) {
        int index = getMidiEventIndexNextTo( tick, before );
        if (index < 0) { return null; }
        return track.get( index );
    }
        
    /**
     * Gets the next <code>MidiEvent</code>'s index either before or after the given tick.
     * @param tick The tick where to start searching.
     * @param before If set to <code>true</code>, indicates that the search shall start
     * at <code>tick</code> and then go backwards in time. Otherwise, it starts at
     * <code>tick</code> and goes forward in time.
     * @return The <code>MidiEvent</code> index next to the given tick. If there is an
     * appropriate event at exactly the given tick, that event's track index is returned.
     * If no event is found, <code>-1</code> is returned.
     */
    public int getMidiEventIndexNextTo( long tick, boolean before ) {
        
        // we perform a logarithmical search for low time complexity
        int lowerIndex = 0;
        int nearest = -1;
        int upperIndex = track.size();
        int lastIndex = -1;
        int index = -2;
        while (lastIndex != index) {
            lastIndex = index;
            index = (upperIndex - lowerIndex) / 2 + lowerIndex;
            MidiEvent e = track.get( index );
            if (e.getTick() == tick) {
                return index; // we can directly return it!
            } else if (e.getTick() < tick) {
                if (before) {
                    nearest = index;
                }
                lowerIndex = index;
            } else if (e.getTick() > tick) {
                if (!before) {
                    nearest = index;
                }
                upperIndex = index;
            }
        }
        
        return nearest;
    }
    
    /**
     * Gets the next <code>ShortMessage</code> <code>NOTE_ON</code> event either
     * before or after the given tick.
     * @param tick The tick where to start searching.
     * @param before If set to <code>true</code>, indicates that the search shall start
     * at <code>tick</code> and then go backwards in time. Otherwise, it starts at
     * <code>tick</code> and goes forward in time.
     * @return The <code>ShortMessage</code> <code>NOTE_ON</code> event next to the
     * given tick. If there is an appropriate event at exactly the given tick, that event
     * is returned. If no event is found, <code>null</code> is returned.
     */
    public MidiEvent getShortMessageNoteOnEventNextTo( long tick, boolean before ) {
        
        boolean b = true;
        MidiEvent e = null;
        boolean next = false;
        MidiEvent lastE = null;
        while (b) {
            e = getMidiEventNextTo( tick, before );
            if (e != null) {
                if (e == lastE) {
                    b = false;
                }
                if (e.getMessage() instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) e.getMessage();
                    if (sm.getCommand() == ShortMessage.NOTE_ON) {
                        if (sm.getData2() > 0 || next) {
                            b = false;
                        } else {
                            next = true;
                        }
                    }
                }
            } else {
                b = false;
            }
            lastE = e;
        }
        return e;
    }
    
    /**
     * Gets the first <code>MidiEvent</code> that provides tempo information
     * from this MIDI track.
     * @return The <code>MidiEvent</code> containing a <code>MetaMessage</code> that
     *         describes a set-tempo event, or <code>null</code> if this track
     *         does not contain such event.
     */
    public MidiEvent getTempoEvent() {
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get( i );
            MidiMessage message = event.getMessage();
            if (message instanceof MetaMessage) {
                MetaMessage metaMessage = (MetaMessage) message;
                if (metaMessage.getType() == 0x51) { // set tempo
                    return event;
                }
            }
        }
        return null;
    }

    /**
     * Gets the tempo from this MIDI track.
     * @return The tempo in microseconds per quarternote, or -1 if this track
     *         does not provide tempo information.
     */
    public float getTempoInMPQ() {
        float tempo = -1;

        MidiEvent event = getTempoEvent();
        if (event != null) {
            MetaMessage metaMessage = (MetaMessage) event.getMessage();
            tempo = MidiToolkit.getTempoInMPQ( metaMessage );
        }
        return tempo;
    }
    
    
    /**
     * Tries to obtain the track's human-readable name from this
     * <code>TrackProxy</code> object.
     * @return The name, or <code>null</code> if the name could not be determined.
     */
    public String getTrackName()
    {
        if (track == null) { return null; }
        for (int i = 0; i < track.size(); i++) {
            MidiMessage msg = track.get( i ).getMessage();
            if (msg instanceof MetaMessage) {
                byte[] data = msg.getMessage();
                if (data != null &&
                    data.length > 2 &&
                    toInt( data[0] ) == 0xff &&
                    toInt( data[1] ) == 0x03) {
                    int len = toInt( data[2] );
                    return new String( data, 3, Math.min( len, data.length - 3 ) );
                }
            }
        }
        return null;
    }

    // private helper method
    private void setTrackNameMetaMessage( MetaMessage msg, String name ) {
        byte[] data = name.getBytes();
        try {
            msg.setMessage( 0x03, data, data.length );
        } catch (InvalidMidiDataException imdex) {
            System.err.println( "ERROR: Unexpected exception:" );
            imdex.printStackTrace();
        }
    }
    
    /**
     * Tries to obtain the track's human-readable name from this
     * <code>TrackProxy</code> object.
     * @param track The <code>TrackProxy</code> to try to obtain the name from.
     * @param name The name, or <code>null</code> if no track-name indicating MIDI
     *        event shall be contained by the MIDI track.
     * @param changeObj The source <code>Object</code> for the track name change.
     */
    public void setTrackName( String name, Object changeObj ) {
        if (track == null) { return; }
        if (name != null && name.length() > 127) {
            name = name.substring( 0, 127 );
        }
        boolean found = false;
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get( i );
            MidiMessage msg = event.getMessage();
            if (msg instanceof MetaMessage) {
                byte[] data = msg.getMessage();
                if (data != null &&
                    data.length > 2 &&
                    toInt( data[0] ) == 0xff &&
                    toInt( data[1] ) == 0x03) {
                    found = true;
                    System.out.println( "tick = " + event.getTick() );
                    // if name is null, remove this event from track
                    if (name == null) {
                        remove( event, changeObj );
                    }
                    if (name != null) {
                        setTrackNameMetaMessage( (MetaMessage) msg, name );
                        fireEventsChanged( new MidiEvent[]{ event }, changeObj );
                    }
                }
            }
        }
        // add track name descriptor
        if (!found && (name != null)) {
            MetaMessage msg = new MetaMessage();
            setTrackNameMetaMessage( msg, name );
            MidiEvent event = new MidiEvent( msg, 0 );
            add( event, changeObj );
        }
    }
    
    /**
     * Gets the <code>EventMap</code> that is currently associated with this
     * <code>TrackProxy</code>.
     * @return The <code>EventMap</code>, or <code>null</code> if none is associated.
     */
    public EventMap getEventMap() {
        if (eventMap == null) {
            eventMap = createDefaultEventMap( new EventMapImpl( this ), false );
        }
        return eventMap;
    }
    
    /**
     * Starts the async edit mode.
     */
    public void startAsynchronousEditMode() {
        if (asyncMode) {
            return;
        }
        if (addedEvents == null) {
            addedEvents = new ArrayList<EventWrapper>();
        }
        if (removedEvents == null) {
            removedEvents = new ArrayList<EventWrapper>();
        }
        if (cachedEvents == null) {
            cachedEvents = new ArrayList<MidiEvent>();
        }
        size = track.size();
        for (int i = 0; i < size; i++) {
            MidiEvent event = (MidiEvent) track.get( i );
            if (!isMetaEndOfTrack( event )) {
                cachedEvents.add( event );
            } else {
                eot = event;
            }
        }
        asyncMode = true;
    }
    
    /**
     * Stops the async edit mode.
     */
    public void stopAsynchronousEditMode() {
        if (!asyncMode) {
            return;
        }
        asyncMode = false;
        synchronizeWithUnderlyingTrack();
    }
    
    public boolean isInAsynchronousEditMode() {
        return asyncMode;
    }
    
    /**
     * This method shall be called when an asynchronous change of track 
     * is finished. All changes made to this <code>TrackProxy</code> are
     * then synchronized with the underlying MIDI <code>Track</code> (SUN Sound API).
     */
    public void synchronizeWithUnderlyingTrack() {
        System.out.println( "TrackProxy.synchronizeWithUnderlyingTrack()" );
        int addIndex = 0;
        int removeIndex = 0;
        int addSize = (addedEvents == null ? 0 : addedEvents.size());
        int removeSize = (removedEvents == null ? 0 : removedEvents.size());
        while (addIndex < addSize || removeIndex < removeSize) {
            if (addIndex == addSize) {
                remove( ((EventWrapper) removedEvents.get( removeIndex++ )).event, null, false );
            } else if (removeIndex == removeSize) {
                add( ((EventWrapper) addedEvents.get( addIndex++ )).event, null, false );
            } else {
                EventWrapper addedEvent = (EventWrapper) addedEvents.get( addIndex );
                EventWrapper removedEvent = (EventWrapper) removedEvents.get( removeIndex );
                if (addedEvent.eventId < removedEvent.eventId) {
                    add( addedEvent.event, null, false );
                    addIndex++;
                } else {
                    remove( removedEvent.event, null, false );
                    removeIndex++;
                }
            }
        }
        addedEvents.clear();
        removedEvents.clear();
        cachedEvents.clear();
    }
    
    private boolean isMetaEndOfTrack( MidiEvent e ) {
        MidiMessage m = e.getMessage();
        if (m instanceof MetaMessage) {
            if (((MetaMessage) m).getType() == 47) {
                return true;
            }
        }
        return false;
    }
    
    public String toString() {
        String name = getTrackName();
        if (name != null) {
            return name;
        }
        return SgEngine.getInstance().getResourceBundle().getString( "track" )
            + " " + (parent.getIndexOf( this ) + 1);
    }

    private static int toInt( byte val ) {
        return ((val < 0) ? 256 + val : val);
    }
    
//    private static byte toByte( int val ) {
//        byte result = 0;
//        // check if return value must be negative
//        if (val > Byte.MAX_VALUE) {
//            result = (byte) (val - 256);
//        } else {
//            result = (byte) val;
//        }
//
//        return result;
//    }

    private static class EOT extends MetaMessage {
        private EOT() {
            super(new byte[3]);
            data[0] = (byte) META;
            data[1] = 47;
            data[2] = 0;
        }
    }
    
    private class EventWrapper {
        MidiEvent event;
        int eventId;
        EventWrapper( MidiEvent event ) {
            this.event = event;
            eventId = TrackProxy.this.eventId++;
        }
    }
    
    /**
     * Creates a new default <code>EventMap</code>.
     * @return An <code>EventMap</code> implementation.
     */
    public static EventMap createDefaultEventMap() {
        return createDefaultEventMap( new EventMapImpl( null ), false );
    }
    
    /**
     * Creates a new default <code>EventMap</code> that only includes MIDI
     * note messages.
     * @return An <code>EventMap</code> implementation.
     */
    public static EventMap createDefaultNoteEventMap() {
        return createDefaultEventMap( new EventMapImpl( null ), true );
    }
    
    /**
     * Creates a new empty <code>EventMap</code>.
     * @return An <code>EventMap</code> implementation that is initially empty.
     */
    public static EventMap createEmptyEventMap() {
        return new EventMapImpl( null );
    }
    
    
    /**
     * Gets a the default <code>EventMap</code> that can be used for
     * note instruments.
     * @return A new default <code>EventMap</code>.
     */
    private static EventMapImpl createDefaultEventMap( EventMapImpl em, boolean notesOnly ) {

        em.setName(
            SgEngine.getInstance().getResourceBundle().getString(
                "eventMap.defaultName" ) );
        em.setDescription(
            SgEngine.getInstance().getResourceBundle().getString(
                "eventMap.defaultDescription" ) );
        String scaleString =
            SgEngine.getInstance().getResourceBundle().getString(
                "eventMap.defaultScale" );
        StringTokenizer st = new StringTokenizer( scaleString, "|" );
        String[] scale = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            scale[i] = st.nextToken();
        }
        
        if (!notesOnly) {
            // add ShortMessage events
            for (String key : EventMapImpl.typeMap.keySet()) {
                Integer status = EventMapImpl.typeMap.get( key );
                String description = EventMap.EVENT_NAME_MAP.get( status.toString() );
                if (description == null) {
                    description = key;
                }
                EventDescriptor desc = new ShortMessageEventDescriptor( description, em, status );
                em.appendEventImpl( desc );
            }
        }
        
        int num = 8;
        int scaleIndex = 127 % scale.length;
        for (int i = 0; i < 128; i++) {
            EventDescriptor desc = new NoteDescriptor( 127 - i, em, scale[scaleIndex--] + num/* + " - " + i*/ );
            if (scaleIndex < 0) {
                scaleIndex = scale.length - 1;
                num--;
            }
            em.appendEventImpl( desc );
        }
        
        return em;
    }
    
    /// an EventMap implementation
    static class EventMapImpl extends EventMap {
        TrackProxy track;
        URL url;
        int size = 0;
        
        // we use a fast hashing algorithm
        // (256 elements are ok, since we have best performance using an array)
        // each array element consists of a MidiDescriptor
        EventDescriptor[] map = new EventDescriptor[258];
        short[] indices = new short[map.length];

        EventMapImpl( TrackProxy track ) {
            this.track = track;
            removeAllEventsImpl();
        }

        public URL getURL() { return url; }
        
        void eventMapChanged() {
            if (track != null) {
                track.eventMapChanged = true;
                track.parent.fireTrackEventMapChanged( track, this );
                track.parent.getMidiDescriptor().getSession().setChanged( true );
            }
        }


        // calculates the hash value for the given MIDI event
        int hv( MidiMessage msg ) {
            if (msg instanceof SysexMessage) {
                return 256;
            } else if (msg instanceof MetaMessage) {
                return 257;
            } else { // must be a ShortMessage
                ShortMessage sm = (ShortMessage) msg;
                if (sm.getCommand() == ShortMessage.NOTE_ON ||
                    sm.getCommand() == ShortMessage.NOTE_OFF)
                {
                    return sm.getData1();
                }
                if (MidiToolkit.isChannelMessageStatusByte( sm.getStatus() )) {
                    return sm.getCommand();
                }
                return sm.getStatus();
            }
        }
        
        // calculates the hash value for the given EventDescriptor
        int hv( EventDescriptor ed ) {
            if (ed instanceof ShortMessageEventDescriptor) {
                if (ed instanceof NoteDescriptor) {
                    return ((NoteDescriptor) ed).getNote();
                } else {
                    return ((ShortMessageEventDescriptor) ed).getStatus();
                }
            } else if (ed instanceof SysexDescriptor) {
                return 256;
            }
            return 257; // must be a MetaMessageDescriptor
        }
        
        private void removeAllEventsImpl() {
            for (int i = 0; i < indices.length; i++) { indices[i] = (short) -1; }
            size = 0;
        }

        public int getIndexFor( EventDescriptor event ) {
            return indices[hv( event )];
        }

        public int getIndexFor( MidiEvent event ) {
            return indices[hv( event.getMessage() )];
        }
        
        public int getIndexFor( MidiMessage message ) {
            return indices[hv( message )];
        }

        public int insertEventAt( int index, EventDescriptor eventDescriptor ) {
            int result = insertEventAtImpl( index, eventDescriptor );
            
            if (result >= 0) {
                eventMapChanged();
            }
            return result;
        }

        private int insertEventAtImpl( int index, EventDescriptor eventDescriptor ) {
            if (index >= map.length || getIndexFor( eventDescriptor ) >= 0) {
                return -1;
            }
            if (index < 0) {
                throw new IllegalArgumentException(
                    "EventMap.insertEventAt(index, eventDescriptor): index must not be < 0" );
            }
            if (index >= size) { return appendEvent( eventDescriptor ); }
            
            for (int i = 0; i < indices.length; i++) {
                if (indices[i] >= index) {
                    indices[i]++;
                }
            }
            
            for (int i = size; i > index; i--) {
                map[i] = map[i - 1];
            }
            size++;
            map[index] = eventDescriptor;
            indices[hv( eventDescriptor )] = (short) index;
            return index;
        }

        public int appendEvent( EventDescriptor eventDescriptor ) {
            int index = appendEventImpl( eventDescriptor );
            eventMapChanged();
            return index;
        }

        private int appendEventImpl( EventDescriptor eventDescriptor ) {
            if (size >= map.length) {
                throw new ArrayIndexOutOfBoundsException( "Cannot append event: event map full" );
            }
            map[size] = eventDescriptor;
            indices[hv( eventDescriptor )] = (short) size;
            
            return size++;
        }

        public int getSize() {
            return size;
        }

        private void checkIndex( int index ) {
            if (index >= size) {
                throw new ArrayIndexOutOfBoundsException( "index out of bounds: " + index + " >= " + size );
            } else if (index < 0) {
                throw new ArrayIndexOutOfBoundsException( "index out of bounds: " + index + " < 0" );
            }
        }

        public EventDescriptor getEventAt( int index ) {
            checkIndex( index );
            return map[index];
        }
        
        public void setEventDescriptors( EventDescriptor[] eventDescriptors ) {
            removeAllEventsImpl();
            for (int i = 0; i < eventDescriptors.length; i++) {
                appendEventImpl( eventDescriptors[i] );
            }
            eventMapChanged();
        }
        
        public boolean contains( MidiEvent event ) {
            return (indices[hv( event.getMessage() )] >= 0);
        }

        public boolean removeEvent( MidiEvent event ) {
            boolean b = removeEventImpl( getEventDescriptorFor( event ) );
            if (b) {
                eventMapChanged();
            }
            return b;
        }
        
        private boolean removeEventImpl( EventDescriptor event ) {
            int index = getIndexFor( event );
            if (index < 0) { return false; }
            
            indices[hv( event )] = (short) -1;
            for (int i = 0; i < indices.length; i++) {
                if (indices[i] > index) {
                    indices[i]--;
                }
            }
            
            for (int i = index + 1; i < size; i++) {
                map[i - 1] = map[i];
            }
            
            map[--size] = null;
            
            return true;
        }
        
        public void removeEventAt( int index ) {
            checkIndex( index );
            removeEventImpl( getEventAt( index ) );
            eventMapChanged();
        }
        
        public void moveEvent( int fromIndex, int toIndex ) {
            if (fromIndex == toIndex) {
                return;
            }
            checkIndex( fromIndex );
            checkIndex( toIndex );
            EventDescriptor ed = getEventAt( fromIndex );
            removeEventImpl( ed );
            insertEventAtImpl( toIndex, ed );
            eventMapChanged();
        }

        /* (non-Javadoc)
         * @see com.jonasreese.sound.sg.midi.EventMap#toBinary()
         */
        public byte[] toBinary() {
            Bitdata bitdata = new Bitdata();
            
            bitdata.add( getName().length(), 32 );
            bitdata.addBytes( getName().getBytes() );
            bitdata.add( getDescription().length(), 32 );
            bitdata.addBytes( getDescription().getBytes() );
            for (int i = 0; i < getSize(); i++) {
                EventDescriptor descriptor = getEventAt( i );
                int command = 0;
                int noteValue = 0;
                String description = descriptor.getDescription();
                if (descriptor instanceof NoteDescriptor) {
                    NoteDescriptor nd = (NoteDescriptor) descriptor;
                    command = nd.getStatus();
                    noteValue = nd.getNote();
                } else if (descriptor instanceof ShortMessageEventDescriptor) {
                    ShortMessageEventDescriptor smed = (ShortMessageEventDescriptor) descriptor;
                    command = smed.getStatus();
                }
                bitdata.add( command, 8 );
                bitdata.add( noteValue, 8 );
                bitdata.add( description.length(), 32 );
                bitdata.addBytes( description.getBytes() );
            }
            
            return bitdata.getDataBytes();
        }

        /* (non-Javadoc)
         * @see com.jonasreese.sound.sg.midi.EventMap#toXml()
         */
        public String toXml() {
            StringBuffer xml = new StringBuffer( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n\n<eventmap>\n" );
            xml.append( "    <name>" + getName() + "</name>\n" );
            xml.append( "    <description>\n        " + getDescription() + "\n    </description>\n" );
            xml.append( "    <mapping>\n" );
            for (int i = 0; i < getSize(); i++) {
                EventDescriptor descriptor = getEventAt( i );
                if (descriptor instanceof NoteDescriptor) {
                    NoteDescriptor nd = (NoteDescriptor) descriptor;
                    xml.append( "        <note value=\"" + nd.getNote() + "\">" + nd.getDescription() + "</event>\n" );
                } else if (descriptor instanceof ShortMessageEventDescriptor) {
                    ShortMessageEventDescriptor smed = (ShortMessageEventDescriptor) descriptor;
                    String typeName = "";
                    for (Object o : typeMap.keySet()) {
                        Integer val = (Integer) typeMap.get( o );
                        if (val.intValue() == smed.getStatus()) {
                            typeName = o.toString();
                            break;
                        }
                    }
                    xml.append( "        <event type=\"" + typeName + "\">" + smed.getDescription() + "</event>\n" );
                }
            }
            xml.append( "    </mapping>\n</eventmap>" );
            return xml.toString();
        }

        public void loadFromXmlDescription( InputStream is )
            throws ParserConfigurationException, SAXException, IOException, TransformerException {
            loadFromXmlDescriptionImpl( is );
            
            eventMapChanged();
        }

        private void loadFromXmlDescriptionImpl( InputStream is )
            throws ParserConfigurationException, SAXException, IOException, TransformerException {

            removeAllEventsImpl();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            Document doc = db.parse( is );
            Node root = XPathAPI.selectSingleNode( doc, "/eventmap" );
            Node nameNode = XPathAPI.selectSingleNode( root, "name/text()" );
            Node desc = XPathAPI.selectSingleNode( root, "description/text()" );
            name = nameNode.getNodeValue();
            description = desc.getNodeValue();
            
            NodeList elems = XPathAPI.selectNodeList( root, "mapping/*" );
            for (int i = 0; elems != null && i < elems.getLength(); i++) {
                Node n = elems.item( i );
                if (n != null) {
                    //System.out.println( n.getNodeName() );
                    if ("note".equals( n.getNodeName() )) {
                        String val = XPathAPI.selectSingleNode( n, "@value" ).getNodeValue();
                        String description = XPathAPI.selectSingleNode( n, "text()" ).getNodeValue();
                        try {
                            appendEventImpl( new NoteDescriptor( Integer.parseInt( val ), this, description ) );
                        } catch (Exception e) {
                            throw new SAXException( e );
                        }
                    } else if ("event".equals( n.getNodeName() )) {
                        String type = XPathAPI.selectSingleNode( n, "@type" ).getNodeValue();
                        try {
                            int command = getMidiCommandFor( type );
                            String description = XPathAPI.selectSingleNode( n, "text()" ).getNodeValue();
                            appendEventImpl( new ShortMessageEventDescriptor( description, this, command ) );
                        } catch (Exception e) {
                            throw new SAXException( e );
                        }
                    }
                }
            }
        }
        
        public void loadFromBinary( byte[] data ) throws IOException, IllegalArgumentException {
            loadFromBinaryImpl( data );

            eventMapChanged();
        }

        private void loadFromBinaryImpl( byte[] data ) throws IOException, IllegalArgumentException {

            removeAllEventsImpl();
            Bitdata bitdata = new Bitdata( data );
            int strLen = (int) bitdata.getValue( 32, 0 );
            if (strLen > data.length) {
                throw new IllegalArgumentException();
            }
            name = new String( bitdata.getDataBytes( 4, strLen ) );
            int offset = 4 + strLen;
            strLen = (int) bitdata.getValue( 32, offset * 8 );
            offset += 4;
            description = new String( bitdata.getDataBytes( offset, strLen ) );
            offset += strLen;
            while (offset < bitdata.getSize()) {
                int command = (int) bitdata.getValue( 8, offset * 8 );
                offset++;
                int noteVal = (int) bitdata.getValue( 8, offset * 8 );
                offset++;
                strLen = (int) bitdata.getValue( 32, offset * 8 );
                offset += 4;
                String description = new String( bitdata.getDataBytes( offset, strLen ) );
                offset += strLen;
                if (command == ShortMessage.NOTE_ON ||
                        command == ShortMessage.NOTE_OFF) {
                    appendEventImpl( new NoteDescriptor( noteVal, this, description ) );
                } else {
                    appendEventImpl( new ShortMessageEventDescriptor( description, this, command ) );
                }
            }
        }
        
        public void resetToDefault() {
            resetToDefaultImpl();
            eventMapChanged();
        }
        
        private void resetToDefaultImpl() {
            if (size > 0) {
                removeAllEventsImpl();
            }
            createDefaultEventMap( this, false );
        }
    }
}