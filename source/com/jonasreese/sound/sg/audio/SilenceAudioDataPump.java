/*
 * Created on 23.06.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.audio;

import javax.sound.sampled.AudioFormat;

/**
 * <p>
 * This class can asynchronously pump silence audio data frames to a receiver
 * class. It can be used if no input device is available or desired as a data pump.
 * </p>
 * @author jonas.reese
 */
public class SilenceAudioDataPump implements AudioDataPump {
    private AudioFormat format;
    private AudioDataReceiver audioDataReceiver;
    private SilenceAudioDataPumpThread thread;
    
    /**
     * Constructs a new <code>SilenceAudioDataPump</code> for the given
     * <code>AudioFormat</code>.
     * @param format The audio format. Must not be <code>null</code>.
     * @param audioDataReceiver The <code>AudioDataReceiver</code>.
     */
    public SilenceAudioDataPump( AudioFormat format, AudioDataReceiver audioDataReceiver ) {
        this.format = format;
        this.audioDataReceiver = audioDataReceiver;
        thread = null;
    }
    
    /**
     * Gets the <code>AudioFormat</code> this <code>SilenceAudioDataPump</code>
     * generates as output.
     * @return The audio format.
     */
    public AudioFormat getAudioFormat() {
        return format;
    }

    public void setAudioDataReceiver(AudioDataReceiver audioDataReceiver) {
        this.audioDataReceiver = audioDataReceiver;
    }

    public AudioDataReceiver getAudioDataReceiver() {
        return audioDataReceiver;
    }
    
    /**
     * Starts the data flow. If it has already been started, this method
     * has no effect.
     */
    public synchronized void start() {
        if (!isRunning()) {
            thread = new SilenceAudioDataPumpThread();
            thread.start();
        }
    }
    
    /**
     * Stops the data flow. If it has already been stopped or not yet started,
     * this method has no effect.
     */
    public synchronized void stop() {
        if (thread != null && thread.running && thread.isAlive()) {
            thread.running = false;
            thread.interrupt();
            try {
                wait();
            } catch (InterruptedException e) {
            }
            thread = null;
        }
    }
    
    /**
     * Retrieves the current <code>running</code> status.
     * @return <code>true</code> if this <code>SilenceAudioDataPump</code>
     * is currently running, <code>false</code> otherwise.
     */
    public boolean isRunning() {
        SilenceAudioDataPumpThread thread = this.thread;
        return (thread != null && thread.running);
    }
    
    class SilenceAudioDataPumpThread extends Thread {
        private boolean running;
        
        SilenceAudioDataPumpThread() {
            super( "Silence Audio Data Pump Thread" );
            setPriority( MAX_PRIORITY );
            running = false;
        }
        
        public void run() {
            running = true;
            
            byte[] buffer = new byte[AudioToolkit.getBufferSize( format )];
            long bufferLengthNanos = AudioToolkit.getAudioBufferLengthInNanos(
                    format.getSampleRate(), format.getFrameSize(), buffer.length );
            System.out.println( "buffers size is " + buffer.length );
            long nextTime = System.nanoTime() + bufferLengthNanos;
            while (running) {
                audioDataReceiver.receive( buffer, 0, buffer.length, SilenceAudioDataPump.this );
                try {
                    waitUntil( nextTime - 1000000 ); // subtract a millisecond to avoid buffer underflow
                } catch (InterruptedException iex) {
                }
                nextTime += bufferLengthNanos;
            }
            synchronized (SilenceAudioDataPump.this) {
                SilenceAudioDataPump.this.notifyAll();
            }
        }
        private void waitUntil( long nanoTime ) throws InterruptedException {
            long timeDiff = nanoTime - System.nanoTime();
            if (timeDiff <= 0) {
                return;
            }
            int nanos = (int) (timeDiff % 1000000);
            sleep( timeDiff / 1000000, nanos );
        }
    }
}
