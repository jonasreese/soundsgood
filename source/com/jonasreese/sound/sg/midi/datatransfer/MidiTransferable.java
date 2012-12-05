/*
 * Copyright (c) 2004 Jonas Reese
 * Created on 11.02.2004
 */
package com.jonasreese.sound.sg.midi.datatransfer;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * <p>
 * This class implements the <code>java.awt.datatransfer.Transferable</code>
 * interface for MIDI event data exchange.
 * </p>
 * @author jreese
 */
public class MidiTransferable implements Transferable, ClipboardOwner
{
    private DataFlavor[] dataFlavors;
    private MidiSerializer midiSerializer;
    
    /**
     * Constructs a new <code>MidiTransferable</code>.
     */
    public MidiTransferable( MidiSerializer midiSerializer )
    {
        dataFlavors = new DataFlavor[]
        {
            new DataFlavor( MidiSerializer.class, "MIDI events" ),
        };
        this.midiSerializer = midiSerializer;
    }
    
    /* (non-Javadoc)
     * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
     */
    public DataFlavor[] getTransferDataFlavors()
    {
        //System.out.println( "MidiTransferable.getTransferDataFlavors()" );
        return dataFlavors;
    }
    /* (non-Javadoc)
     * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
     */
    public boolean isDataFlavorSupported( DataFlavor dataFlavor )
    {
        //System.out.println( "MidiTransferable.isDataFlavorSupported()" );
        return (MidiSerializer.class.equals( dataFlavor.getDefaultRepresentationClass() ) ||
            dataFlavor.isMimeTypeSerializedObject());
    }
    /* (non-Javadoc)
     * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
     */
    public Object getTransferData( DataFlavor dataFlavor )
        throws UnsupportedFlavorException, IOException
    {
        //System.out.println( "MidiTransferable.getTransferData(" + dataFlavor + ")" );
        if (!isDataFlavorSupported( dataFlavor ))
        {
            throw new UnsupportedFlavorException( dataFlavor );
        }
        return midiSerializer;
    }

    /* (non-Javadoc)
     * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
     */
    public void lostOwnership( Clipboard arg0, Transferable arg1 )
    {
        // TODO Auto-generated method stub
        
    }
}
