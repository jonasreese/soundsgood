/*
 * Created on 03.12.2009
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.jonasreese.sound.sg.soundbus.MidiNoteCounterElement;
import com.jonasreese.sound.sg.soundbus.MidiNoteCounterNode;

/**
 * @author Jonas Reese
 */
public class AdjustTempoMidiNoteCounterElement implements MidiNoteCounterElement {

    private MidiNoteCounterNode counterNode;
    private int fireAtCounter;
    private float targetTempoBpm;
    private boolean absolute;
    
    public void init( MidiNoteCounterNode counterNode ) {
        this.counterNode = counterNode;
    }

    public boolean notifyCounterChanged( int counter, int lastCounterFired ) {
        if (counterNode != null && ((absolute && fireAtCounter != counter) ||
                (!absolute && fireAtCounter != counter - lastCounterFired))) {
            return false;
        }
        counterNode.getSoundbus().setTempo( targetTempoBpm );
        
        return true;
    }

    public Map<String, String> getParameters() {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        
        map.put("fireAtCounter", Integer.toString( fireAtCounter ) );
        map.put("targetTempoBpm", Float.toString( targetTempoBpm ) );
        map.put("absolute", Boolean.toString( absolute ) );
        
        return map;
    }

    public void setParameters( Map<String, String> parameters ) {
        
        String val = parameters.get( "fireAtCounter" );
        if (val != null) {
            try {
                fireAtCounter = Integer.parseInt( val );
            } catch (NumberFormatException ignored) {
                ignored.printStackTrace();
            }
        }
        val = parameters.get( "targetTempoBpm" );
        if (val != null) {
            try {
                targetTempoBpm = Float.parseFloat( val );
            } catch (NumberFormatException ignored) {
                ignored.printStackTrace();
            }
        }
        val = parameters.get( "absolute" );
        if (val != null) {
            try {
                absolute = Boolean.parseBoolean( val );
            } catch (NumberFormatException ignored) {
                ignored.printStackTrace();
            }
        }
    }

}
