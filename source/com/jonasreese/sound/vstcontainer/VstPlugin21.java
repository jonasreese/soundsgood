/*
 * Created on 20.10.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.vstcontainer;

/**
 * @author jonas.reese
 */
public interface VstPlugin21 extends VstPlugin20 {

    public static final String CANDO_PLUG_MIDI_PROGRAM_NAMES = "midiProgramNames";
    public static final String CANDO_PLUG_CONFORMS_TO_WINDOW_RULES = "conformsToWindowRules";
    
    /**
    
    List of methods to be provided by a VST host version 2.1 (Steinberg VST Plug-Ins SDK) 
   
   [2.1]
   getMidiProgramName
   getCurrentMidiProgram
   getMidiProgramCategory
   hasMidiProgramsChanged
   getMidiKeyName
   beginSetProgram
   endSetProgram 

    */
    
    public int getMidiProgramName( int channel, MidiProgramName midiProgramName );
    public int getCurrentMidiProgram( int channel, MidiProgramName currentProgram );
    public int getMidiProgramCategory( int channel, MidiProgramCategory category );
    public boolean hasMidiProgramsChanged( int channel );
    public boolean getMidiKeyName( int channel, MidiKeyName keyName );
    public boolean beginSetProgram();
    public boolean endSetProgram();
}
