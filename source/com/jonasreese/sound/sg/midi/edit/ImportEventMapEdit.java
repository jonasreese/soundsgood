/*
 * Created on 25.12.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.midi.edit;

import java.io.File;
import java.net.URL;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.edit.SgUndoableEdit;
import com.jonasreese.sound.sg.midi.EventDescriptor;
import com.jonasreese.sound.sg.midi.EventMap;

/**
 * <p>
 * An <code>UndoableEdit</code> implementation that allows to undo
 * an "import event map" action. Subclass this class, override
 * the <code>perform()</code> method and implement the import only.
 * It is not required that any other method is implemented.
 * </p>
 * @author jreese
 */
public abstract class ImportEventMapEdit extends SgUndoableEdit {
    private static final long serialVersionUID = 1L;

    protected EventMap eventMap;
    private EventDescriptor[] eventDescriptors;
    protected URL xmlDescriptionUrl;
    protected File binaryFile;

    /**
     * Constructs a new <code>ImportEventMapEdit</code> that imports from an
     * XML description.
     * @param eventMap The <code>EventMap</code> to be edited.
     * @param xmlDescriptionUrl The URL where to find the XML description.
     */
    public ImportEventMapEdit( EventMap eventMap, URL xmlDescriptionUrl ) {
        this.eventMap = eventMap;
        this.xmlDescriptionUrl = xmlDescriptionUrl;
        this.binaryFile = null;
        this.eventDescriptors = eventMap.getEventDescriptors();
    }

    /**
     * Constructs a new <code>ImportEventMapEdit</code> that imports from an
     * binary data file.
     * @param eventMap The <code>EventMap</code> to be edited.
     * @param binaryFile A <code>File</code> that contains the binary data.
     */
    public ImportEventMapEdit( EventMap eventMap, File binaryFile ) {
        this.eventMap = eventMap;
        this.binaryFile = binaryFile;
        this.xmlDescriptionUrl = null;
        this.eventDescriptors = eventMap.getEventDescriptors();
    }

    
    @Override
    public String getRedoPresentationName() {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.redo" ) + " " + getPresentationName();
    }
    
    @Override
    public String getUndoPresentationName() {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.undo" ) + " " + getPresentationName();
    }
    
    @Override
    public String getPresentationName() {
        return SgEngine.getInstance().getResourceBundle().getString(
            "edit.importEventMapEdit" );
    }
    
    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        EventDescriptor[] eventDescriptors = eventMap.getEventDescriptors();
        eventMap.setEventDescriptors( this.eventDescriptors );
        this.eventDescriptors = eventDescriptors;
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        EventDescriptor[] eventDescriptors = eventMap.getEventDescriptors();
        eventMap.setEventDescriptors( this.eventDescriptors );
        this.eventDescriptors = eventDescriptors;
    }
}
