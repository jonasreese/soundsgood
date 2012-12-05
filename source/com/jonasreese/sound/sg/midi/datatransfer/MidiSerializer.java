/*
 * Copyright (c) 2004 Jonas Reese
 * Created on 11.02.2004
 */
package com.jonasreese.sound.sg.midi.datatransfer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 * <p>
 * This class is capable of serializing and deserializing a set of
 * <code>MidiEvent</code> objects.
 * </p>
 * @author jreese
 */
public class MidiSerializer implements Serializable {
    private static final long serialVersionUID = 1;
    
    private transient MidiEvent[] midiEvents;
    private transient int resolution;
    
    /**
     * Constructs a new <code>MidiSerializer</code>.
     * @param midiEvents The MIDI events.
     * @param resolution The MIDI resolution. The MIDI resolution is required
     *        when tick values of MIDI events shall be translated for sequences
     *        with different MIDI resolutions.
     */
    public MidiSerializer( MidiEvent[] midiEvents, int resolution )
    {
        this.midiEvents = midiEvents;
        this.resolution = resolution;
    }
    
    /**
     * Gets the midi events.
     * @return The <code>MidiEvent</code> array passed to the constructor.
     */
    public MidiEvent[] getMidiEvents() { return midiEvents; }

    /**
     * Gets the MIDI resolution.
     * @return The MIDI resolution value.
     */
    public int getResolution() { return resolution; }

    private void writeObject( ObjectOutputStream out ) throws IOException
    {
        //System.out.println( "writeObject() called!" );
        out.writeInt( resolution );
        for (int i = 0; i < midiEvents.length; i++)
        {
            MidiMessage msg = midiEvents[i].getMessage();
            if (msg instanceof ShortMessage)
            {
                out.writeLong( midiEvents[i].getTick() );
                out.writeInt( msg.getLength() );
                out.write( msg.getMessage(), 0, msg.getLength() );
            }
        }
    }
    private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException
    {
        //System.out.println( "readObject() called!" );
        resolution = in.readInt();
        ArrayList<MidiEvent> events = new ArrayList<MidiEvent>();
        while (in.available() > 0)
        {
            long tick = in.readLong();
            int len = in.readInt();
            ShortMessage msg = new ShortMessage();
            try
            {
                if (len == 1)
                {
                    msg.setMessage( in.readUnsignedByte() );
                }
                else
                {
                    msg.setMessage(
                        in.readUnsignedByte(),
                        in.readUnsignedByte(),
                        ((len > 2) ? in.readUnsignedByte() : 0) );
                }
                MidiEvent event = new MidiEvent( msg, tick );
                events.add( event );
            }
            catch (InvalidMidiDataException imdex)
            {
                throw new IOException( imdex.getMessage() );
            }
        }
        midiEvents = new MidiEvent[events.size()];
        events.toArray( midiEvents );
    }
}
