/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 07.09.2003
 */
package com.jonasreese.sound;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;

/**
 * <b>
 * </b>
 * @author jreese
 */
public class SoundInfo
{
	public static void main( String[] args )
	{
        System.out.println( "Name\tVendor\tVersion\tDescription" );
        
        MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < info.length; i++)
        {
            System.out.println( info[i].getName() + "\t" +
                info[i].getVendor() + "\t" +
                info[i].getVersion() + "\t" +
                info[i].getDescription() );
        }
	}
}
