/*
 * Created on 20.10.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.vstcontainer;

import javax.sound.sampled.AudioFormat;

/**
 * @author jonas.reese
 */
public interface VstPlugin10 {

    /**

    List of methods to be provided by a VST host version 1.0 (Steinberg VST Plug-Ins SDK) 

    setParameter *
    getParameter *
    process *
    processReplacing *
    dispatcher
    open
    close
    getProgram *
    setProgram *
    setProgramName *
    getProgramName *
    getParameterLabel *
    getParameterDisplay *
    getParameterName *
    getVu
    getChunk
    setChunk
    setSampleRate
    setBlockSize
    suspend
    resume 

     */

    public int getNumInputs();
    public int getNumOutputs();
    public void setParameter( int index, float value );
    public float getParameter( int index );
    public void process( float[][] inputs, float[][] outputs, int sampleFrames );
    public void processReplacing( float[][] inputs, float[][] outputs, int sampleFrames );
    /**
     * This method does the same processing as the other process() method, but uses byte
     * arrays instead to be compliant with the Java Sound API.
     * @param inputData The binary input data.
     * @param outputData The output data buffer.
     * @param numInputs The number of inputs to be processed.
     * @param numOutputs The number of outputs to be processed.
     * @param frameSize The frame size (in bytes).
     * @param bigEndian If <code>true</code>, indicates big-endian data representation, otherwise
     * little-endian.
     * @param encoding The encoding.
     */
    public void process(
            byte[] inputData,
            byte[] outputData,
            int numInputs,
            int numOutputs,
            int frameSize,
            boolean bigEndian,
            AudioFormat.Encoding encoding );

    /**
     * This method does the same processing as the other processReplacing() method, but uses byte
     * arrays instead to be compliant with the Java Sound API.
     * @param inputData The binary input data.
     * @param outputData The output data buffer.
     * @param numInputs The number of inputs to be processed.
     * @param numOutputs The number of outputs to be processed.
     * @param frameSize The frame size (in bytes).
     * @param bigEndian If <code>true</code>, indicates big-endian data representation, otherwise
     * little-endian.
     * @param encoding The encoding.
     */
    public void processReplacing(
            byte[] inputData,
            byte[] outputData,
            int numInputs,
            int numOutputs,
            int frameSize,
            boolean bigEndian,
            AudioFormat.Encoding encoding );
    public void open();
    public void close();
    public int getProgram();
    public void setProgram( int program );
    public void setProgramName( String name );
    public String getProgramName();
    public String getParameterLabel( int index );
    public String getParameterDisplay( int index );
    public String getParameterName( int index );
    public float getVu();
//    public int getChunk( float[][] data, boolean isPreset );
//    public int setChunk( float[] data, int byteSize, boolean isPreset );
    public void setSampleRate( float sampleRate );
    public void setBlockSize( int blockSize );
    public void suspend();
    public void resume();
    public boolean canMono();
    public boolean canRealtime();
    public boolean canOffline();
    public boolean canProcessReplacing();
    public boolean hasEditor();
    public int getUniqueId();
    public boolean isInputConnected( int input );
    public boolean isOutputConnected( int output );
    public int getNumParams();
    public int getNumPrograms();
    
    // editor methods
    public void openEditWindow();
}
