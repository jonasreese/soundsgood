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
public class VstVariableIo {

    private float[][] inputs;     
    private float[][] outputs;    
    private int numSamplesInputProcessed;     
    private int numSamplesOutputProcessed;
    
    
    public VstVariableIo() {
        this( null, null, 0, 0 );
    }
    
    public VstVariableIo(
            float[][] inputs,
            float[][] outputs,
            int numSamplesInputProcessed,
            int numSamplesOutputProcessed ) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.numSamplesInputProcessed = numSamplesInputProcessed;
        this.numSamplesOutputProcessed = numSamplesOutputProcessed;
    }

    public float[][] getInputs() {
        return inputs;
    }

    public void setInputs( float[][] inputs ) {
        this.inputs = inputs;
    }

    public int getNumSamplesInput() {
        if (inputs.length == 0) {
            return 0;
        }
        return inputs[0].length;
    }

    public int getNumSamplesInputProcessed() {
        return numSamplesInputProcessed;
    }

    public void setNumSamplesInputProcessed( int numSamplesInputProcessed ) {
        this.numSamplesInputProcessed = numSamplesInputProcessed;
    }

    public int getNumSamplesOutput() {
        if (outputs.length == 0) {
            return 0;
        }
        return outputs[0].length;
    }

    public int getNumSamplesOutputProcessed() {
        return numSamplesOutputProcessed;
    }

    public void setNumSamplesOutputProcessed(int numSamplesOutputProcessed) {
        this.numSamplesOutputProcessed = numSamplesOutputProcessed;
    }

    public float[][] getOutputs() {
        return outputs;
    }

    public void setOutputs( float[][] outputs ) {
        this.outputs = outputs;
    }
    
    
}
