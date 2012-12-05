/*
 * Created on 04.02.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.soundbus;

import com.jonasreese.sound.sg.audio.AudioDataReceiver;


/**
 * <p>
 * This interface shall be implemented by entities that represent
 * an audio output plug of a soundbus. It can receive audio data from
 * an <code>SbAudioOutput</code>.
 * </p>
 * @author jonas.reese
 */
public interface SbAudioInput extends SbInput, AudioDataReceiver {
}
