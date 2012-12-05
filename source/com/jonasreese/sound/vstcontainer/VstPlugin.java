/*
 * Created on 19.10.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.vstcontainer;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.swing.JDialog;

import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;

/**
 * Java side of a VST plugin.
 * <p>
 * This class is <b>not</b> thread-safe and thus access
 * to methods (especially those defining the lifecycle) has to be serialized externally!
 * @author jonas.reese
 */
public class VstPlugin implements VstPlugin23 {
    
    private long vstPluginPeer;
    private boolean initialized;
    
    
    private Rectangle editBounds;
    private boolean editWindowOpen;
    private Window editWindow;
    private EditorUpdaterThread editorUpdaterThread;
    private VstPluginDescriptor descriptor;
    
    private Set<VstEventListener> vstEventListeners;
    
    VstPlugin( VstPluginDescriptor descriptor )
            throws VstPluginNotAvailableException {
        this.descriptor = descriptor;
        initialized = false;
        vstPluginPeer = 0;
        if (!descriptor.isLazy()) {
            init();
        }
        
        // plugin sematics-specific initializations
        editBounds = new Rectangle();
        editWindowOpen = false;
        vstEventListeners = new HashSet<VstEventListener>();
    }
    
    public VstPluginDescriptor getDescriptor() {
        return descriptor;
    }
    
    long getId() {
        return vstPluginPeer;
    }
    
    public VstNode getParent() {
        return descriptor.getParent();
    }

    public String getName() {
        String s = descriptor.getPluginLibrary().getName();
        int index = s.lastIndexOf( "." );
        if (index >= 0) {
            s = s.substring( 0, index );
        }
        return s;
    }
    
    public File getPluginLibrary() {
        return descriptor.getPluginLibrary();
    }
    
    private void checkPluginPeer() {
        if (!initialized) {
            try {
                init();
            } catch (VstPluginNotAvailableException e) {
            }
        }
        if (vstPluginPeer == 0) {
            throw new IllegalStateException(
                    "VstPlugin: Native peer missing for " + descriptor.getPluginLibrary().getAbsolutePath() );
        }
    }
    
    private native void exit( long vstPluginPeer );

    static native void setConfiguration( VstPlugin23Configuration configuration );

    private native long init( String pluginLibrary, VstPlugin23Configuration configuration );
    synchronized void init() throws VstPluginNotAvailableException {
        System.out.println( "VST: " + getName() + ".init()" );
        initialized = true;
        vstPluginPeer = init( descriptor.getPluginLibrary().getAbsolutePath(),
                VstContainer.getInstance().getConfiguration() );
        if (vstPluginPeer == 0) {
            throw new VstPluginNotAvailableException();
        }
        VstContainer.getInstance().pluginInitialized( this );
    }
    
    // gets the tempo in BPM
    static native float getTempo();
    static native void setTempo( float bpm );
    
    private native int getNumInputs( long vstPluginPeer );
    public int getNumInputs() {
        checkPluginPeer();
        return getNumInputs( vstPluginPeer );
    }

    private native int getNumOutputs( long vstPluginPeer );
    public int getNumOutputs() {
        checkPluginPeer();
        return getNumOutputs( vstPluginPeer );
    }

    private native int setTotalSampleToProcess( long vstPluginPeer, int value );
    public int setTotalSampleToProcess( int value ) {
        checkPluginPeer();
        return setTotalSampleToProcess( vstPluginPeer, value );
    }

    private native void getNextShellPlugin( long vstPluginPeer, PluginNameAndId nameAndId );
    public PluginNameAndId getNextShellPlugin() {
        checkPluginPeer();
        PluginNameAndId nameAndId = new PluginNameAndId( null, 0 );
        getNextShellPlugin( vstPluginPeer, nameAndId );
        return nameAndId;
    }

    private native int startProcess( long vstPluginPeer );
    public int startProcess() {
        checkPluginPeer();
        return startProcess( vstPluginPeer );
    }

    private native int stopProcess( long vstPluginPeer );
    public int stopProcess() {
        checkPluginPeer();
        return stopProcess( vstPluginPeer );
    }

    private native int getMidiProgramName( long vstPluginPeer, int channel, MidiProgramName midiProgramName );
    public int getMidiProgramName( int channel, MidiProgramName midiProgramName ) {
        checkPluginPeer();
        return getMidiProgramName( vstPluginPeer, channel, midiProgramName );
    }

    private native int getCurrentMidiProgram( long vstPluginPeer, int channel, MidiProgramName currentProgram );
    public int getCurrentMidiProgram( int channel, MidiProgramName currentProgram ) {
        checkPluginPeer();
        return getCurrentMidiProgram( vstPluginPeer, channel, currentProgram );
    }

    private native int getMidiProgramCategory( long vstPluginPeer, int channel, MidiProgramCategory category );
    public int getMidiProgramCategory( int channel, MidiProgramCategory category ) {
        checkPluginPeer();
        return getMidiProgramCategory( vstPluginPeer, channel, category );
    }

    private native boolean hasMidiProgramsChanged( long vstPluginPeer, int channel );
    public boolean hasMidiProgramsChanged( int channel ) {
        checkPluginPeer();
        return hasMidiProgramsChanged( vstPluginPeer, channel );
    }

    private native boolean getMidiKeyName( long vstPluginPeer, int channel, MidiKeyName keyName );
    public boolean getMidiKeyName( int channel, MidiKeyName keyName ) {
        checkPluginPeer();
        return getMidiKeyName( vstPluginPeer, channel, keyName );
    }

    private native boolean beginSetProgram( long vstPluginPeer );
    public boolean beginSetProgram() {
        checkPluginPeer();
        return beginSetProgram( vstPluginPeer );
    }

    private native boolean endSetProgram( long vstPluginPeer );
    public boolean endSetProgram() {
        checkPluginPeer();
        return endSetProgram( vstPluginPeer );
    }

    private native int canDo( long vstPluginPeer, String feature );
    public int canDo( String feature ) {
        checkPluginPeer();
        return canDo( vstPluginPeer, feature );
    }

    private native boolean canParameterBeAutomated( long vstPluginPeer, int index );
    public boolean canParameterBeAutomated( int index ) {
        checkPluginPeer();
        return canParameterBeAutomated( vstPluginPeer, index );
    }

    private native boolean copyProgram( long vstPluginPeer, int destination );
    public boolean copyProgram( int destination ) {
        checkPluginPeer();
        return copyProgram( vstPluginPeer, destination );
    }

    private native int fxIdle( long vstPluginPeer );
    public int fxIdle() {
        checkPluginPeer();
        return fxIdle( vstPluginPeer );
    }

    private native void editIdle( long vstPluginPeer );
    public void editIdle() {
        checkPluginPeer();
        editIdle( vstPluginPeer );
    }
    
    private native float getChannelParameter( long vstPluginPeer, int channel, int index );
    public float getChannelParameter( int channel, int index ) {
        checkPluginPeer();
        return getChannelParameter( vstPluginPeer, channel, index );
    }

    private native int getNumCategories( long vstPluginPeer );
    public int getNumCategories() {
        checkPluginPeer();
        return getNumCategories( vstPluginPeer );
    }

    private native String getProgramNameIndexed( long vstPluginPeer, int category, int index );
    public String getProgramNameIndexed( int category, int index ) {
        checkPluginPeer();
        return getProgramNameIndexed( vstPluginPeer, category, index );
    }

    private native void getInputProperties( long vstPluginPeer, int index, VstPinProperties inputProperties );
    public VstPinProperties getInputProperties( int index ) {
        checkPluginPeer();
        VstPinProperties inputProperties = new VstPinProperties();
        getInputProperties( vstPluginPeer, index, inputProperties );
        return inputProperties;
    }

    private native void getOutputProperties( long vstPluginPeer, int index, VstPinProperties outputProperties );
    public VstPinProperties getOutputProperties( int index ) {
        checkPluginPeer();
        VstPinProperties outputProperties = new VstPinProperties();
        getOutputProperties( vstPluginPeer, index, outputProperties );
        return outputProperties;
    }

    public byte[] getIcon() {
        // TODO: Not Yet Implemented
        return null;
    }

    private native String getEffectName( long vstPluginPeer );
    public String getEffectName() {
        checkPluginPeer();
        return getEffectName( vstPluginPeer );
    }

    private native String getErrorText( long vstPluginPeer );
    public String getErrorText() {
        checkPluginPeer();
        return getErrorText( vstPluginPeer );
    }

    private native int getTailSize( long vstPluginPeer );
    public int getTailSize() {
        checkPluginPeer();
        return getTailSize( vstPluginPeer );
    }

    private native void getParameterProperties( long vstPluginPeer, int index, VstParameterProperties props );
    public VstParameterProperties getParameterProperties( int index ) {
        checkPluginPeer();
        VstParameterProperties props = new VstParameterProperties();
        getParameterProperties( vstPluginPeer, index, props );
        return props;
    }

    private native int getPlugCategory( long vstPluginPeer );
    public int getPlugCategory() {
        checkPluginPeer();
        return getPlugCategory( vstPluginPeer );
    }

    private native String getProductString( long vstPluginPeer );
    public String getProductString() {
        checkPluginPeer();
        return getProductString( vstPluginPeer );
    }

    private native String getVendorString( long vstPluginPeer );
    public String getVendorString() {
        checkPluginPeer();
        return getVendorString( vstPluginPeer );
    }
    
    private native int getVendorVersion( long vstPluginPeer );
    public int getVendorVersion() {
        checkPluginPeer();
        return getVendorVersion( vstPluginPeer );
    }

    private native int getVstVersion( long vstPluginPeer );
    public int getVstVersion() {
        checkPluginPeer();
        return getVstVersion( vstPluginPeer );
    }

    private native void inputConnected( long vstPluginPeer, int index, boolean state );
    public void inputConnected( int index, boolean state ) {
        checkPluginPeer();
        inputConnected( vstPluginPeer, index, state );
    }

    private native void outputConnected( long vstPluginPeer, int index, boolean state );
    public void outputConnected( int index, boolean state ) {
        checkPluginPeer();
        outputConnected( vstPluginPeer, index, state );
    }

    private native boolean keysRequired( long vstPluginPeer );
    public boolean keysRequired() {
        checkPluginPeer();
        return keysRequired( vstPluginPeer );
    }

    private native int processEvents( long vstPluginPeer, VstEvent[] events );
    public int processEvents( VstEvent[] events ) {
        checkPluginPeer();
        return processEvents( vstPluginPeer, events );
    }

    private native long processVariableIo(
            long vstPluginPeer, float[][] inputs, float[][] outputs );
    public boolean processVariableIo( VstVariableIo varIo ) {
        checkPluginPeer();
        long result = processVariableIo( vstPluginPeer, varIo.getInputs(), varIo.getOutputs() );
        int inputProcessed = (int) (result & 0x7fffffff); // 31 bit value
        result >>= 31;
        int outputProcessed = (int) (result & 0x7fffffff); // 31 bit value
        varIo.setNumSamplesInputProcessed( inputProcessed );
        varIo.setNumSamplesOutputProcessed( outputProcessed );
        result >>= 31;
        boolean success = ((result & 1) != 0);
        System.out.println( "inputProcessed = " + inputProcessed );
        System.out.println( "outputProcessed = " + outputProcessed );
        System.out.println( "success = " + success );
        return success;
    }

    private native int reportCurrentPosition( long vstPluginPeer );
    public int getCurrentPosition() {
        checkPluginPeer();
        return reportCurrentPosition( vstPluginPeer );
    }

    private native float[] reportDestinationBuffer( long vstPluginPeer );
    public float[] getDestinationBuffer() {
        checkPluginPeer();
        return reportDestinationBuffer( vstPluginPeer );
    }

    private native void setBlockSizeAndSampleRate( long vstPluginPeer, int blockSize, float sampleRate );
    public void setBlockSizeAndSampleRate( int blockSize, float sampleRate ) {
        checkPluginPeer();
        setBlockSizeAndSampleRate( vstPluginPeer, blockSize, sampleRate );
    }

    private native boolean setBypass( long vstPluginPeer, boolean bypass );
    public boolean setBypass( boolean bypass ) {
        checkPluginPeer();
        return setBypass( vstPluginPeer, bypass );
    }

    private native boolean setSpeakerArrangement(
            long vstPluginPeer, VstSpeakerArrangement pluginInput, VstSpeakerArrangement pluginOutput );
    public boolean setSpeakerArrangement(
            VstSpeakerArrangement pluginInput, VstSpeakerArrangement pluginOutput ) {
        checkPluginPeer();
        return setSpeakerArrangement( vstPluginPeer, pluginInput, pluginOutput );
    }

    private native boolean setViewPosition( long vstPluginPeer, int x, int y );
    public boolean setViewPosition( int x, int y ) {
        checkPluginPeer();
        return setViewPosition( vstPluginPeer, x, y );
    }

    private native boolean stringToParameter( long vstPluginPeer, int index, String value );
    public boolean stringToParameter( int index, String value ) {
        checkPluginPeer();
        return stringToParameter( vstPluginPeer, index, value );
    }

    private native void setParameter( long vstPluginPeer, int index, float value );
    public void setParameter( int index, float value ) {
        checkPluginPeer();
        setParameter( vstPluginPeer, index, value );
    }

    private native float getParameter( long vstPluginPeer, int index );
    public float getParameter( int index ) {
        checkPluginPeer();
        return getParameter( vstPluginPeer, index );
    }

    private int getEncodingFor( AudioFormat.Encoding encoding ) {
        if (encoding == AudioFormat.Encoding.PCM_SIGNED) {
            return 1;
        } else if (encoding == AudioFormat.Encoding.PCM_UNSIGNED) {
            return 2;
        } else if (encoding == AudioFormat.Encoding.ALAW) {
            return 3;
        } else if (encoding == AudioFormat.Encoding.ULAW) {
            return 4;
        }
        throw new IllegalArgumentException( "Unknown encoding" );
    }

    @Deprecated
    public void process( float[][] inputs, float[][] outputs, int sampleFrames ) {
        checkPluginPeer();
    }

    @Deprecated
    public void process(
            byte[] inputData,
            byte[] outputData,
            int numInputs,
            int numOutputs,
            int frameSize,
            boolean bigEndian,
            AudioFormat.Encoding encoding ) {
        checkPluginPeer();
    }

    private native void processReplacingByteArray(
            long vstPluginPeer,
            byte[] inputData,
            byte[] outputData,
            int numInputs,
            int numOutputs,
            int frameSize,
            boolean bigEndian,
            int encoding );
    public void processReplacing(
            byte[] inputData,
            byte[] outputData,
            int numInputs,
            int numOutputs,
            int frameSize,
            boolean bigEndian,
            AudioFormat.Encoding encoding ) {
        checkPluginPeer();
        processReplacingByteArray(
                vstPluginPeer,
                inputData,
                outputData,
                numInputs,
                numOutputs,
                frameSize,
                bigEndian,
                getEncodingFor( encoding ) );
    }

    private native void processReplacing(
            long vstPluginPeer, float[][] inputs, float[][] outputs, int sampleFrames );
    public void processReplacing( float[][] inputs, float[][] outputs, int sampleFrames ) {
        checkPluginPeer();
        processReplacing( vstPluginPeer, inputs, outputs, sampleFrames );
    }

    private native void open( long vstPluginPeer );
    public void open() {
        System.out.println( "VST: " + getName() + ".open()" );
        checkPluginPeer();
        open( vstPluginPeer );
    }

    private native void close( long vstPluginPeer );
    public synchronized void close() {
        if (editWindowOpen) {
            closeEditWindow();
        }
        System.out.println( "VST: " + getName() + ".close()" );
        checkPluginPeer();
        close( vstPluginPeer );
        exit( vstPluginPeer );
        initialized = false;
        vstPluginPeer = 0;
    }

    private native int getProgram( long vstPluginPeer );
    public int getProgram() {
        checkPluginPeer();
        return getProgram( vstPluginPeer );
    }

    private native void setProgram( long vstPluginPeer, int program );
    public void setProgram( int program ) {
        checkPluginPeer();
        setProgram( vstPluginPeer, program );
    }

    private native void setProgramName( long vstPluginPeer, String name );
    public void setProgramName( String name ) {
        checkPluginPeer();
        setProgramName( vstPluginPeer, name );
    }

    private native String getProgramName( long vstPluginPeer );
    public String getProgramName() {
        checkPluginPeer();
        return getProgramName( vstPluginPeer );
    }

    private native String getParameterLabel( long vstPluginPeer, int index );
    public String getParameterLabel( int index ) {
        checkPluginPeer();
        return getParameterLabel( vstPluginPeer, index );
    }

    private native String getParameterDisplay( long vstPluginPeer, int index );
    public String getParameterDisplay( int index ) {
        checkPluginPeer();
        return getParameterDisplay( vstPluginPeer, index );
    }

    private native String getParameterName( long vstPluginPeer, int index );
    public String getParameterName( int index ) {
        checkPluginPeer();
        return getParameterName( vstPluginPeer, index );
    }

    private native float getVu( long vstPluginPeer );
    public float getVu() {
        checkPluginPeer();
        return getVu( vstPluginPeer );
    }

//    private native int getChunk( long vstPluginPeer, float[][] data, boolean isPreset );
//    public int getChunk( float[][] data, boolean isPreset ) {
//        checkPluginPeer();
//        return getChunk( vstPluginPeer, data, isPreset );
//    }

//    private native int setChunk( long vstPlugin, float[] data, int byteSize, boolean isPreset );
//    public int setChunk( float[] data, int byteSize, boolean isPreset ) {
//        checkPluginPeer();
//        return setChunk( vstPluginPeer, data, byteSize, isPreset );
//    }

    private native void setSampleRate( long vstPluginPeer, float sampleRate );
    public void setSampleRate( float sampleRate ) {
        checkPluginPeer();
        setSampleRate( vstPluginPeer, sampleRate );
    }

    private native void setBlockSize( long vstPluginPeer, int blockSize );
    public void setBlockSize( int blockSize ) {
        checkPluginPeer();
        setBlockSize( vstPluginPeer, blockSize );
    }

    private native void suspend( long vstPluginPeer );
    public void suspend() {
        checkPluginPeer();
        suspend( vstPluginPeer );
    }

    private native void resume( long vstPluginPeer );
    public void resume() {
        checkPluginPeer();
        resume( vstPluginPeer );
    }
    
    
    // inquiry methods (1.0)
    private native boolean canMono( long vstPluginPeer );
    public boolean canMono() {
        checkPluginPeer();
        return canMono( vstPluginPeer );
    }
    
    private native boolean hasEditor( long vstPluginPeer );
    public boolean hasEditor() {
        checkPluginPeer();
        return hasEditor( vstPluginPeer );
    }

    private native boolean canRealtime( long vstPluginPeer );
    public boolean canRealtime() {
        checkPluginPeer();
        return canRealtime( vstPluginPeer );
    }

    private native boolean canOffline( long vstPluginPeer );
    public boolean canOffline() {
        checkPluginPeer();
        return canOffline( vstPluginPeer );
    }

    private native boolean canProcessReplacing( long vstPluginPeer );
    public boolean canProcessReplacing() {
        checkPluginPeer();
        return canProcessReplacing( vstPluginPeer );
    }

    private native int getUniqueId( long vstPluginPeer );
    public int getUniqueId() {
        checkPluginPeer();
        return getUniqueId( vstPluginPeer );
    }

    public boolean isInputConnected( int input ) {
        VstPinProperties properties = getInputProperties( input );
        if (properties != null) {
            return properties.isActive();
        }
        return false;
    }

    public boolean isOutputConnected( int output ) {
        VstPinProperties properties = getOutputProperties( output );
        if (properties != null) {
            return properties.isActive();
        }
        return false;
    }

    private native int getNumParams( long vstPluginPeer );
    public int getNumParams() {
        checkPluginPeer();
        return getNumParams( vstPluginPeer );
    }

    private native int getNumPrograms( long vstPluginPeer );
    public int getNumPrograms() {
        checkPluginPeer();
        return getNumPrograms( vstPluginPeer );
    }
    
    private native void openEditWindow( long vstPluginPeer, Component component );
    public synchronized void openEditWindow() {
        checkPluginPeer();
        if (editWindowOpen) {
            return;
        }
        System.out.println( "VST: " + getName() + ".openEditWindow()" );
        JDialog d = new JDialog( UiToolkit.getMainFrame(), getName(), false );
        d.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
        d.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                closeEditWindow();
            }
        } );
        editWindow = d;
        //System.out.println( "getEditBounds() = " + getEditBounds() );
        d.setVisible( true );
        openEditWindow( vstPluginPeer, d );
        editorUpdaterThread = new EditorUpdaterThread( d );
        editorUpdaterThread.start();
        editWindowOpen = true;
    }
    
    private native void closeEditWindow( long vstPluginPeer );
    public synchronized void closeEditWindow() {
        if (!editWindowOpen) {
            return;
        }
        editWindowOpen = false;
        if (editorUpdaterThread != null && editorUpdaterThread.isAlive()) {
            editorUpdaterThread.interrupt();
        }
        System.out.println( "VST: " + getName() + ".closeEditWindow()" );
        checkPluginPeer();
        editWindow.dispose();
        closeEditWindow( vstPluginPeer );
    }

    private native void getEditBounds( long vstPluginPeer, Rectangle rect );
    public Rectangle getEditBounds() {
        getEditBounds( vstPluginPeer, editBounds );
        System.out.println( "VST: " + getName() + ".getEditBounds(): " + editBounds );
        return editBounds;
    }
    
    /**
     * Adds a <code>VstEventListener</code> to this <code>VstPlugin</code>.
     * @param listener The listener to be added. If it has already been added,
     * this method does nothing.
     */
    public void addVstEventListener( VstEventListener listener ) {
        vstEventListeners.add( listener );
    }
    
    /**
     * Removes a <code>VstEventListener</code> from this <code>VstPlugin</code>.
     * @param listener The listener to be removed. If it has already been removed
     * or not added, this method does nothing.
     */
    public void removeVstEventListener( VstEventListener listener ) {
        vstEventListeners.remove( listener );
    }
    
    void processEventsCallback( VstEvent[] events ) {
        for (VstEventListener l : vstEventListeners) {
            l.process( events, this );
        }
    }
    
    public void finalize() throws Throwable {
        super.finalize();
        if (initialized) {
            close();
        }
    }
    
    
    class IdleThread extends Thread {
        boolean running;
        
        public IdleThread() {
            super( "VST Idle thread " + vstPluginPeer );
        }
        
        void end() {
            running = false;
            interrupt();
        }
        
        public void run() {
            running = true;
            while (running) {
                fxIdle();
                if (editWindowOpen) {
                    editIdle();
                }
                try {
                    Thread.sleep( 200 );
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
    
    class EditorUpdaterThread extends Thread {
        Window w;
        Insets ins;
        int width;
        int height;
        EditorUpdaterThread( Window w ) {
            this.w = w;
            ins = w.getParent().getInsets();
            width = -1;
            height = -1;
        }
        public void run() {
            synchronized (VstPlugin.this) {
                try {
                    for (int i = 0; i < 5 && w != null && editWindowOpen; i++) {
                        Rectangle r = getEditBounds();
                        if (r.width != width || r.height != height) {
                            height = r.height;
                            width = r.width;
                            System.out.println( "adjusting bounds to " + r );
                            int width = r.width + ins.left + ins.right;
                            int height = r.height + ins.top + ins.bottom; 
                            int x = w.getParent().getX();
                            int y = w.getParent().getY();
                            w.setBounds( x - width / 2 + w.getParent().getWidth() / 2,
                                    y - height / 2 + w.getParent().getHeight() / 2, width, height );
                        }
                        VstPlugin.this.wait( 1000 );
                    }
                } catch (InterruptedException e1) {
                }
                editorUpdaterThread = null;
            }
        }
    }
}
