/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 30.09.2003
 */
package com.jonasreese.sound.sg;

import java.util.EventListener;

/**
 * <b>
 * </b>
 * @author jreese
 */
public interface ObjectSelectionChangeListener extends EventListener
{
    public void objectSelectionChanged( ObjectSelectionChangedEvent e );
}
