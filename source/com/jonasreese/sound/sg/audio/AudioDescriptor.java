/*
 * Created on 18.12.2007
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SessionElementType;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.audio.impl.AudioRecorderImpl;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * This class describes an audio session element.
 * 
 * @author Jonas Reese
 */
public class AudioDescriptor extends SessionElementDescriptor {
    
    public static final SessionElementType TYPE = new SessionElementType() {
        public String getName() {
            return SgEngine.getInstance().getResourceBundle().getString(
                    "descriptor.type.audio" );
        }
        public String getDescription() {
            return SgEngine.getInstance().getResourceBundle().getString(
                "descriptor.type.audio.description" );
        }

        public Image getSmallIcon() {
            return new ResourceLoader( getClass(), "/resource/audio.gif" ).getAsImage();
        }

        public Image getLargeIcon() {
            return new ResourceLoader( getClass(), "/resource/audio.gif" ).getAsImage();
        }
    };
    
    
    private String name;
    private AudioRecorder recorder;
    
    /**
     * Default constructor.
     */
    public AudioDescriptor() {
        recorder = new AudioRecorderImpl( this );
    }
    

    @Override
    public void destroy() {
    }

    /**
     * Sets this <code>AudioDescriptor</code>s name.
     * @param name The name to set. Can be <code>null</code> if the
     *        name shall be set to it's default value.
     */
    public void setName( String name ) {
        this.name = name;
    }
    

    @Override
    public String getName() {
        if (name != null) {
            return name;
        }
        File f = getFile();
        if (f != null) {
            return f.getName();
        }
        return SgEngine.getInstance().getResourceBundle().getString(
            "audio.new.defaultName" );
    }

    @Override
    public SessionElementType getType() {
        return TYPE;
    }

    @Override
    public void resetData() {
    }

    @Override
    public void save() throws IOException {
        saveCopy( getFile() );
    }

    @Override
    public void saveCopy( File copy ) throws IOException {
    }
    
    public AudioRecorder getAudioRecorder() {
        return recorder;
    }
}
