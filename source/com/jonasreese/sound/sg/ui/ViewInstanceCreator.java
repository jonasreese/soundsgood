/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 25.09.2003
 */
package com.jonasreese.sound.sg.ui;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.plugin.view.ViewInstanceCreationFailedException;
import com.jonasreese.sound.sg.ui.defaultui.action.ViewAction;

/**
 * <b>
 * This interface shall be implemented by classes that can instantiate
 * and visually display a <code>View</code>.
 * </b>
 * @author jreese
 */
public interface ViewInstanceCreator {

    /**
     * Creates a <code>ViewInstance</code>.
     * @param viewAction The <code>ViewAction</code> that has triggered
     *        this creation (the caller).
     * @param session The <code>Session</code> a <code>ViewInstance</code>
     *        shall be created for.
     * @param d The <code>SessionElementDescriptor</code> to be passed to the
     *        <code>View</code> in order to create a <code>ViewInstance</code>.
     * @return The created <code>ViewInstance</code>, or <code>null</code>
     *         if no <code>ViewInstance</code> could be created.
     * @throws ViewInstanceCreationFailedException if the <code>ViewInstance</code>
     *         could not be created because the corresponding <code>View</code>
     *         failed to create it.
     */
    public ViewInstance createViewInstance(
        ViewAction viewAction, Session session, SessionElementDescriptor d ) throws ViewInstanceCreationFailedException;

    /**
     * Removes a <code>ViewInstance</code> that has been previously created using one
     * of the <code>createViewInstance()</code> methods.
     * @param viewInstance The <code>ViewInstance</code> to be removed.
     */
    public void removeViewInstance( ViewInstance viewInstance );
}
