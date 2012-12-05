/*
 * Created on 09.03.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi.edit;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.MidiToolkit;
import com.jonasreese.sound.sg.midi.TrackProxy;

/**
 * <p>
 * An <code>UndoableEdit</code> implementation that allows to undo
 * a change to a <code>MetaMessage</code> on a track that describes
 * the speed to use for playback (in microseconds per quarternote).
 * @see com.jonasreese.sound.sg.midi.MidiToolkit#getTempoEvent(Track)
 * @see com.jonasreese.sound.sg.midi.MidiToolkit#getTempoInMPQ(Track)
 * </p>
 * @author jreese
 */
public class ChangeTempoEdit extends SgUndoableEdit {

    private static final long serialVersionUID = 1;
    
    private boolean changed;
    private MidiDescriptor midiDescriptor;

    private TrackProxy track;
    private MidiEvent tempoMidiEvent;
    /// the new MPQ value (passed to the constructor)
    protected float newMPQ;
    /// the old MPQ value (extracted from the track)
    protected float oldMPQ;

    protected Object changeObj;
    
    /**
     * Constructs a new <code>ChangeTempoEdit</code>.
     * @param midiDescriptor The <code>MidiDescriptor</code>.
     *        The <code>setTempoInMpq(float)</code> method will be called on this midi descriptor,
     *        so that the tempo change can be notified by change listeners on the midi descriptor.
     *        May be <code>null</code>, but then the correct tempo is <b>not</b> set to the
     *        midi descriptor, but only on the track. 
     * @param track The MIDI track that contains the tempo <code>MidiEvent</code>. Shall
     *        not be <code>null</code>.
     * @param tempoMidiEvent The tempo midi event. Shall not be <code>null</code>.
     * @param newMPQ The new tempo in microseconds per quarternote.
     * @param changeObj The <code>Object</code> that adds the events.
     *        Used to identify the source to a change on a MIDI track.
     * @throws IllegalArgumentException if the given <code>MidiEvent</code> is not a
     *         tempo MIDI event (means, it does not contain a <code>MetaMessage</code>
     *         of type <code>0x51</code>.
     */
    public ChangeTempoEdit(
        MidiDescriptor midiDescriptor,
        TrackProxy track,
        MidiEvent tempoMidiEvent,
        float newMPQ,
        Object changeObj )
    {
        this.midiDescriptor = midiDescriptor;
        this.track = track;
        this.tempoMidiEvent = tempoMidiEvent;
        this.newMPQ = newMPQ;
        this.changeObj = changeObj;
        
        if (!(tempoMidiEvent.getMessage() instanceof MetaMessage))
        {
            throw new IllegalArgumentException(
                "Tempo information can only be extracted from a meta message" );
        }
        this.oldMPQ = MidiToolkit.getTempoInMPQ( ((MetaMessage) tempoMidiEvent.getMessage()) );
        changed = (midiDescriptor != null ? midiDescriptor.isChanged() : true);
    }

    private void updateRunningSequencer( float mpqVal )
    {
        // if a sequencer is currently running, change it's tempo, too!
        if (midiDescriptor.getMidiRecorder().isPlaying())
        {
            midiDescriptor.getMidiRecorder().setTempoInMPQ( mpqVal );
        }
    }

    private void setMessageTempo( float mpq )
    {
        byte[] tempoBytes = MidiToolkit.createTempoBytes( mpq );
        
        MetaMessage mm = (MetaMessage) tempoMidiEvent.getMessage();
        byte[] data = mm.getData();
        data[0] = tempoBytes[0];
        data[1] = tempoBytes[1];
        data[2] = tempoBytes[2];
        try
        {
            mm.setMessage( mm.getType(), data, 3 );
            if (midiDescriptor != null)
            {
                midiDescriptor.setTempoInMpq( mpq );
            }
        }
        catch (InvalidMidiDataException imdex)
        {
            System.err.println( "Unexpected exception:" );
            imdex.printStackTrace();
        }
    }

    public static byte toByte( int value )
    {
        return (value > 127) ? (byte) (value - 256) : (byte) value;
    }

    public String getRedoPresentationName()
    {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.redo" ) + " " + getPresentationName();
    }

    public String getUndoPresentationName()
    {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.undo" ) + " " + getPresentationName();
    }
    
    public String getPresentationName()
    {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.changeTempoEdit" );
    }

    public void die()
    {
        super.die();
        track = null;
        tempoMidiEvent = null;
    }

    public void undo()
    {
        super.undo();
        //track.remove( tempoMidiEvent, changeObj );
        setMessageTempo( oldMPQ );
        //track.add( tempoMidiEvent, changeObj );
        track.fireEventsChanged( new MidiEvent[]{ tempoMidiEvent }, changeObj );
        if (midiDescriptor != null)
        {
            changed = changed || !midiDescriptor.isChanged();
            midiDescriptor.setChanged( changed );
        }
        updateRunningSequencer( oldMPQ );
    }
    
    public void redo()
    {
        super.redo();
        perform();
    }

    /**
     * Performs the tempo change.
     */
	public void perform()
	{
        //track.remove( tempoMidiEvent, changeObj );
        setMessageTempo( newMPQ );
        //track.add( tempoMidiEvent, changeObj );
        track.fireEventsChanged( new MidiEvent[]{ tempoMidiEvent }, changeObj );
        if (midiDescriptor != null)
        {
            midiDescriptor.setChanged( true );
        }
        updateRunningSequencer( newMPQ );
	}
}