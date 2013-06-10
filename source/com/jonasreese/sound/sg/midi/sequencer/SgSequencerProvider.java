/*
 * Created on 24.11.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi.sequencer;

import  javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import  javax.sound.midi.spi.MidiDeviceProvider;


public class SgSequencerProvider extends MidiDeviceProvider
{
    private static MidiDevice.Info info;

    public SgSequencerProvider()
    {
        synchronized (SgSequencerProvider.class)
        {
            if (info == null)
            {
                info = new SgSequencer.Info(
                    "SoundsGood Sequencer",
                    "(c) Jonas Reese",
                    "",
                    "1.0");
            }
        }
    }

    public MidiDevice.Info[] getDeviceInfo()
    {
        return new MidiDevice.Info[]{ info };
    }

    public MidiDevice getDevice( MidiDevice.Info forInfo )
    {
        MidiDevice  device = null;
        if (info != null && info.equals( forInfo ))
        {
            try {
                device = new SgSequencer( info );
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
        else
        {
            throw new IllegalArgumentException( "No device found" );
        }
        return device;
    }
}
