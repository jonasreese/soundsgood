/*
 * Created on 23.05.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

/**
 * <p>
 * A dummy <code>MidiDevice</code>
 * </p>
 * @author jonas.reese
 */
public class DummyDevice implements MidiDevice {
    private boolean open = false;
    public Info getDeviceInfo() {
        return null;
    }
    public void open() throws MidiUnavailableException {
        open = true;
    }
    public void close() {
        open = false;
    }
    public boolean isOpen() {
        return open;
    }
    public long getMicrosecondPosition() {
        return 0;
    }
    public int getMaxReceivers() {
        return 100000;
    }

    public int getMaxTransmitters() {
        return 100000;
    }

    public Receiver getReceiver() throws MidiUnavailableException {
        return new Receiver() {
            public void send(MidiMessage arg0, long arg1) {
            }
            public void close() {
            }
        };
    }

    public List<Receiver> getReceivers() {
        List<Receiver> l = new ArrayList<Receiver>();
        try {
            l.add( getReceiver() );
        } catch (MidiUnavailableException ex) {}
        return l;
    }

    public Transmitter getTransmitter() throws MidiUnavailableException {
        return new Transmitter() {
            Receiver rec = null;
            public void setReceiver(Receiver rec) {
                this.rec = rec;
            }
            public Receiver getReceiver() {
                return rec;
            }
            public void close() {
            }
        };
    }

    public List<Transmitter> getTransmitters() {
        List<Transmitter> l = new ArrayList<Transmitter>();
        try {
            l.add( getTransmitter() );
        } catch (MidiUnavailableException ex) {}
        return l;
    }
}