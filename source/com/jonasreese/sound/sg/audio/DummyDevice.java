/*
 * Created on 22.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Control.Type;

/**
 * @author jonas.reese
 */
public class DummyDevice implements Mixer {

    public Info getMixerInfo() {
        return null;
    }

    public Line.Info[] getSourceLineInfo() {
        return new Line.Info[0];
    }

    public Line.Info[] getTargetLineInfo() {
        return new Line.Info[0];
    }

    public Line.Info[] getSourceLineInfo( Line.Info info ) {
        return new Line.Info[0];
    }

    public Line.Info[] getTargetLineInfo( Line.Info info ) {
        return new Line.Info[0];
    }

    public boolean isLineSupported( Line.Info info ) {
        return (info != null &&
                (info.getLineClass().equals( SourceDataLine.class ) ||
                        info.getLineClass().equals( TargetDataLine.class )));
    }

    public Line getLine( Line.Info info ) throws LineUnavailableException {
        if (info == null) {
            return null;
        }
        if (info.getLineClass().equals( SourceDataLine.class )) {
            final Line.Info _info = info;
            return new SourceDataLine() {
                boolean running = false;
                boolean open = false;
                long timeStarted = 0;
                public void open(AudioFormat arg0, int arg1) throws LineUnavailableException {
                    open = true;
                }
                public void open(AudioFormat arg0) throws LineUnavailableException {
                    open = true;
                }
                public int write(byte[] arg0, int off, int len) {
                    return len;
                }
                public void drain() {
                }
                public void flush() {
                }
                public void start() {
                    running = true;
                    timeStarted = System.currentTimeMillis();
                }
                public void stop() {
                    running = false;
                }
                public boolean isRunning() {
                    return running;
                }
                public boolean isActive() {
                    return false;
                }
                public AudioFormat getFormat() {
                    return null;
                }
                public int getBufferSize() {
                    // TODO Auto-generated method stub
                    return 0;
                }
                public int available() {
                    return 0;
                }
                public int getFramePosition() {
                    return 0;
                }
                public long getLongFramePosition() {
                    return 0;
                }

                public long getMicrosecondPosition() {
                    return (System.currentTimeMillis() - timeStarted) * 1000;
                }

                public float getLevel() {
                    return 0;
                }
                public Line.Info getLineInfo() {
                    return _info;
                }
                public void open() throws LineUnavailableException {
                    open = true;
                }

                public void close() {
                    open = false;
                }
                public boolean isOpen() {
                    return open;
                }
                public Control[] getControls() {
                    return new Control[0];
                }
                public boolean isControlSupported( Type arg0 ) {
                    return false;
                }
                public Control getControl( Type arg0 ) {
                    return null;
                }
                public void addLineListener( LineListener arg0 ) {
                }
                public void removeLineListener( LineListener arg0 ) {
                }
            };
        } else if (info.getLineClass().equals( TargetDataLine.class )) {
            final Line.Info _info = info;
            return new TargetDataLine() {
                boolean running = false;
                boolean open = false;
                long timeStarted = 0;
                public void open(AudioFormat arg0, int arg1) throws LineUnavailableException {
                    open = true;
                }
                public void open(AudioFormat arg0) throws LineUnavailableException {
                    open = true;
                }
                public int read(byte[] arg0, int off, int len) {
                    return len;
                }
                public void drain() {
                }
                public void flush() {
                }
                public void start() {
                    running = true;
                    timeStarted = System.currentTimeMillis();
                }
                public void stop() {
                    running = false;
                }
                public boolean isRunning() {
                    return running;
                }
                public boolean isActive() {
                    return false;
                }
                public AudioFormat getFormat() {
                    return null;
                }
                public int getBufferSize() {
                    // TODO Auto-generated method stub
                    return 0;
                }
                public int available() {
                    return 0;
                }
                public int getFramePosition() {
                    return 0;
                }
                public long getLongFramePosition() {
                    return 0;
                }

                public long getMicrosecondPosition() {
                    return (System.currentTimeMillis() - timeStarted) * 1000;
                }

                public float getLevel() {
                    return 0;
                }
                public Line.Info getLineInfo() {
                    return _info;
                }
                public void open() throws LineUnavailableException {
                    open = true;
                }

                public void close() {
                    open = false;
                }
                public boolean isOpen() {
                    return open;
                }
                public Control[] getControls() {
                    return new Control[0];
                }
                public boolean isControlSupported( Type arg0 ) {
                    return false;
                }
                public Control getControl( Type arg0 ) {
                    return null;
                }
                public void addLineListener( LineListener arg0 ) {
                }
                public void removeLineListener( LineListener arg0 ) {
                }
            };
        }

        return null;
    }

    public int getMaxLines( Line.Info arg0 ) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Line[] getSourceLines() {
        // TODO Auto-generated method stub
        return null;
    }

    public Line[] getTargetLines() {
        // TODO Auto-generated method stub
        return null;
    }

    public void synchronize(Line[] arg0, boolean arg1) {
        // TODO Auto-generated method stub
        
    }

    public void unsynchronize(Line[] arg0) {
        // TODO Auto-generated method stub
        
    }

    public boolean isSynchronizationSupported(Line[] arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    public Line.Info getLineInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    public void open() throws LineUnavailableException {
        // TODO Auto-generated method stub
        
    }

    public void close() {
        // TODO Auto-generated method stub
        
    }

    public boolean isOpen() {
        // TODO Auto-generated method stub
        return false;
    }

    public Control[] getControls() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isControlSupported(Type arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public Control getControl(Type arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void addLineListener(LineListener arg0) {
        // TODO Auto-generated method stub
        
    }

    public void removeLineListener(LineListener arg0) {
        // TODO Auto-generated method stub
        
    }

}
