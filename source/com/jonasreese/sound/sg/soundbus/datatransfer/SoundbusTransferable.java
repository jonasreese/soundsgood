/*
 * Copyright (c) 2004 Jonas Reese
 * Created on 11.02.2004
 */
package com.jonasreese.sound.sg.soundbus.datatransfer;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * <p>
 * This class implements the <code>java.awt.datatransfer.Transferable</code>
 * interface for Soundbus data exchange.
 * </p>
 * @author jreese
 */
public class SoundbusTransferable implements Transferable, ClipboardOwner
{
    private DataFlavor[] dataFlavors;
    private SoundbusSerializer soundbusSerializer;
    
    /**
     * Constructs a new <code>SoundbusTransferable</code>.
     * @param soundbusSerializer The <code>SoundbusSerializer</code>
     */
    public SoundbusTransferable( SoundbusSerializer soundbusSerializer ) {
        dataFlavors = new DataFlavor[] {
            new DataFlavor( SoundbusSerializer.class, "Soundbus nodes" ),
        };
        this.soundbusSerializer = soundbusSerializer;
    }
    
    /* (non-Javadoc)
     * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
     */
    public DataFlavor[] getTransferDataFlavors() {
        return dataFlavors;
    }
    /* (non-Javadoc)
     * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
     */
    public boolean isDataFlavorSupported( DataFlavor dataFlavor ) {
        return (SoundbusSerializer.class.equals( dataFlavor.getDefaultRepresentationClass() ) ||
            dataFlavor.isMimeTypeSerializedObject());
    }
    /* (non-Javadoc)
     * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
     */
    public Object getTransferData( DataFlavor dataFlavor )
        throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported( dataFlavor )) {
            throw new UnsupportedFlavorException( dataFlavor );
        }
        return soundbusSerializer;
    }

    /* (non-Javadoc)
     * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
     */
    public void lostOwnership( Clipboard arg0, Transferable arg1 ) {
    }
}
