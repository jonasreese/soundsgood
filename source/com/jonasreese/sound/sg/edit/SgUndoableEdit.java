/*
 * Created on 07.12.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.edit;

import javax.swing.undo.AbstractUndoableEdit;

/**
 * <p>
 * This class is the base class for all <code>UndoableEdit</code>
 * implementing classes in the SoundsGood application.
 * </p>
 * @author jreese
 */
public abstract class SgUndoableEdit extends AbstractUndoableEdit
{
    private static final long serialVersionUID = 1L;

    /**
     * This object shall be used for global critical code
     * sections within undo or redo operations.
     */
    protected static final Object SYNCHRONIZER = new Object();

    /**
     * Causes this edit operation to be performed. This method
     * shall be called before the <code>undo()</code> method
     * can be called.
     */
    public abstract void perform();
}