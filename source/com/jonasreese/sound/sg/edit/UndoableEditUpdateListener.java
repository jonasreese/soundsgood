/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 25.11.2003
 */
package com.jonasreese.sound.sg.edit;

import java.util.EventListener;

/**
 * <b>
 * </b>
 * @author jreese
 */
public interface UndoableEditUpdateListener extends EventListener
{
    public void undoableEditUpdate( UndoableEditUpdateEvent e );
}
