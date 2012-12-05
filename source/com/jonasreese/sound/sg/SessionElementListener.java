/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 23.09.2003
 */
package com.jonasreese.sound.sg;

import java.util.EventListener;

/**
 * <b>
 * This interface shall be implemented by classes that
 * wish to receive events from a <code>Session</code> when a
 * session element has been added to or removed from it.
 * </b>
 * @author jreese
 */
public interface SessionElementListener extends EventListener
{
    /**
     * Invoked when an element has been added to the source
     * <code>Session</code>.
     * @param e The <code>SessionElementEvent</code> pointing to
     *        the element that has been added, it's type and the source
     *        of the event.
     */
    public void elementAdded( SessionElementEvent e );

    /**
     * Invoked when an element has been removed from the source
     * <code>Session</code>.
     * @param e The <code>SessionElementEvent</code> pointing to
     *        the file that has been removed, it's type and the source
     *        of the event.
     */
    public void elementRemoved( SessionElementEvent e );
}
