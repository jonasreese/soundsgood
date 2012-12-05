/*
 * Created on 02.02.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;



/**
 * Instances of <code>MidiFilter</code> can time-efficiently filter
 * a MIDI input stream for criteria like MIDI command, MIDI channel,
 * DATA1 and DATA2 fields.
 * 
 * @author jonas.reese
 */
public class MidiFilter {
    
    private boolean optimistic;
    private boolean blockNonShortMessageEvents;
    private MidiFilterElement[] filterElements;


    /**
     * Constructs a <code>MidiFilterElement</code> from it's string representation.
     * @param stringRepresentation The string representation of the <code>MidiFilterElement</code>.
     * @throws IllegalArgumentException if the string representation is incorrect.
     */
    public MidiFilter( String stringRepresentation ) {
        StringTokenizer st = new StringTokenizer( stringRepresentation, "|" );
        if (st.countTokens() < 2) {
            throw new IllegalArgumentException( "Invalid MidiFilter string representation" );
        }
        optimistic = (Boolean.TRUE.toString().equalsIgnoreCase( st.nextToken() ));
        blockNonShortMessageEvents = (Boolean.TRUE.toString().equalsIgnoreCase( st.nextToken() ));
        ArrayList<MidiFilterElement> filterElements = new ArrayList<MidiFilterElement>();
        while (st.hasMoreTokens()) {
            MidiFilterElement midiFilterElement = new MidiFilterElement( st.nextToken() );
            filterElements.add( midiFilterElement );
        }
        this.filterElements = new MidiFilterElement[filterElements.size()];
        filterElements.toArray( this.filterElements );
    }

    /**
     * Constructs a new <code>MidiFilter</code>.
     * @param optimistic If <code>true</code>, indicates that all MIDI
     * events pass the filter unless blocking filtering rules are defined.
     * Otherwise, all MIDI events will be blocked unless passing filtering
     * rules are defined.
     * @param blockNonShortMessageEvents If <code>true</code>, all non short message
     * MIDI events (such as sysex) will be blocked by this filter.
     * @param filterElements An array of <code>MidiFilterElement</code> objects.
     * This array will not be copied, so it should not be modified after this
     * constructor's invocation.
     */
    public MidiFilter(
            boolean optimistic, boolean blockNonShortMessageEvents, MidiFilterElement[] filterElements ) {
        this.optimistic = optimistic;
        this.blockNonShortMessageEvents = blockNonShortMessageEvents;
        this.filterElements = filterElements;
    }
    
    /**
     * Constructs a new <code>MidiFilter</code>.
     * @param optimistic If <code>true</code>, indicates that all MIDI
     * events pass the filter unless blocking filtering rules are defined.
     * Otherwise, all MIDI event will be blocked unless passing filtering
     * rules are defined.
     * @param blockNonShortMessageEvents If <code>true</code>, all non short message
     * MIDI events (such as sysex) will be blocked by this filter.
     * @param filterElements An <code>List</code> of <code>MidiFilterElement</code>
     * objects. Can be <code>null</code>.
     */
    public MidiFilter(
            boolean optimistic, boolean blockNonShortMessageEvents, List<MidiFilterElement> filterElements ) {
        this.optimistic = optimistic;
        this.blockNonShortMessageEvents = blockNonShortMessageEvents;
        this.filterElements = new MidiFilterElement[filterElements == null ? 0 : filterElements.size()];
        filterElements.toArray( this.filterElements );
    }
    
    /**
     * Gets the <code>optimistic</code> flag.
     * @return <code>true</code> to indicate that all MIDI
     * events pass the filter unless blocking filtering rules are defined.
     * <code>false</code> to indicate that all MIDI events will be blocked
     * unless passing filtering rules are defined.
     */
    public boolean isOptimistic() {
        return optimistic;
    }
    
    /**
     * Gets the <code>blockNonShortMessageEvents</code> flag.
     * @return <code>true</code> if non-shortmessage MIDI events are blocked.
     */
    public boolean isBlockingNonShortMessageEvents() {
        return blockNonShortMessageEvents;
    }
    
    /**
     * Gets all contained filter elements.
     * @return An array of all <code>FilterElement</code>s.
     */
    public MidiFilterElement[] getFilterElements() {
        return filterElements;
    }
    
    /**
     * Gets the <code>MidiFilterElement</code> for the given command.
     * @param command The MIDI command.
     * @return A <code>MidiFilterElement</code> if one defined for the given
     * command, or <code>null</code>.
     */
    public MidiFilterElement getFilterElement( short command ) {
        if (filterElements != null) {
            for (int i = 0; i < filterElements.length; i++) {
                if (filterElements[i] != null && filterElements[i].getCommand() == command) {
                    return filterElements[i];
                }
            }
        }
        return null;
    }
    
    /**
     * Filters the given <code>MidiMessage</code> and returns <code>true</code>
     * if it passes the filter.
     * @param m The MIDI message.
     * @return <code>true</code> if and only if the filter criteria is matched.
     */
    public boolean filter( MidiMessage m ) {
        if (m instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) m;
            if (filterElements != null) {
                int command;
                int channel;
                if (MidiToolkit.isChannelMessageStatusByte( sm.getStatus() )) {
                    command = sm.getCommand();
                    if (command == ShortMessage.NOTE_OFF) {
                        command = ShortMessage.NOTE_ON;
                    }
                    channel = sm.getChannel();
                } else {
                    command = sm.getStatus();
                    if (command == ShortMessage.STOP || command == ShortMessage.CONTINUE) {
                        command = ShortMessage.START;
                    }
                    channel = -1;
                }
                for (int i = 0; i < filterElements.length; i++) {
                    if (filterElements[i] != null && filterElements[i].getCommand() == command) {
                        return filterElements[i].filter( channel, sm.getData1(), sm.getData2() );
                    }
                }
            }
            return optimistic;
        } else {
            return !blockNonShortMessageEvents;
        }
    }
    
    /**
     * Returns a string representation of this <code>MidiFilterElement</code>.
     * Another <code>MidiFilterElement</code> object can be constructed from this string
     * representation by simply passing it to the constructor.
     * @return A (valid) string representation.
     */
    public String getStringRepresentation() {
        StringBuffer sb = new StringBuffer();
        sb.append( optimistic );
        sb.append( "|" );
        sb.append( blockNonShortMessageEvents );
        if (filterElements != null) {
            for (MidiFilterElement filterElement : filterElements) {
                if (filterElements != null) {
                    sb.append( "|" );
                    sb.append( filterElement.getStringRepresentation() );
                }
            }
        }
        return sb.toString();
    }
    
    public boolean equals( Object another ) {
        if (!(another instanceof MidiFilter)) {
            return false;
        }
        MidiFilter filter = (MidiFilter) another;
        if (optimistic != filter.optimistic || blockNonShortMessageEvents != filter.blockNonShortMessageEvents) {
            return false;
        }
        if (filter.filterElements == filterElements) {
            return true;
        }
        if (filter.filterElements == null && filter.filterElements != null ||
                filter.filterElements != null && filter.filterElements == null) {
            return false;
        }
        if (filter.filterElements.length != filterElements.length) {
            return false;
        }
        for (int i = 0; i < filterElements.length; i++) {
            if (filterElements[i] != null) {
                MidiFilterElement fe =filter.getFilterElement( filterElements[i].getCommand() );
                if (fe == null || !fe.equals( filterElements[i] )) {
                    return false;
                }
            }
        }
        return true;
    }
}
