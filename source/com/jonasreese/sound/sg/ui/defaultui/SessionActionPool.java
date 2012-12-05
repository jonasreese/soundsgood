/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 26.11.2003
 */
package com.jonasreese.sound.sg.ui.defaultui;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.ui.defaultui.action.ViewAction;

/**
 * <b>
 * An <code>ActionPool</code> is a set of <code>Action</code>
 * objects that are created at the time a new <code>Session</code>
 * is created and which can be used by several different modules,
 * as plugins, views, ...<br>
 * The <code>SessionActionPool</code> for a <code>Session</code>
 * can be requested from the according <code>SessionUi</code> object.
 * </b>
 * @author jreese
 */
public interface SessionActionPool
{
    public static final String SAVE_AS = "saveAs";
    public static final String SAVE_COPY_AS = "saveCopyAs";
    public static final String SAVE = "save";
    public static final String SAVE_ALL = "saveAll";
    public static final String REVERT = "revert";
    public static final String SAVE_SESSION = "saveSession";
    public static final String PROPERTIES = "properties";

    public static final String UNDO = "undo";
    public static final String REDO = "redo";
    public static final String CUT = "cut";
    public static final String COPY = "copy";
    public static final String PASTE = "paste";
    public static final String DELETE = "delete";
    public static final String SELECT_ALL = "selectAll";
    public static final String SELECT_NONE = "selectNone";
    public static final String INVERT_SELECTION = "invertSelection";
    
    /**
     * Gets the <code>SgAction</code> described by <code>id</code>.
     * @param id The action identifier (see constants definition).
     * @return The according action, or <code>null</code> if an action
     *         for the given ID is not available.
     */
    public SgAction getAction( String id );
    
    /**
     * Gets all actions.
     * @return An array with all actions contained by this <code>ActionPool</code>.
     */
    public SgAction[] getActions();
    
    /**
     * Gets the <code>ViewAction</code> objects that are used to trigger
     * <code>View</code> plugins to be executed.
     * @return The <code>ViewAction</code> array.
     */
    public ViewAction[] getViewActions();
    
    /**
     * Gets the <code>Session</code> to which this <code>SessionActionPool</code>
     * belongs.
     * @return A non-<code>null</code> <code>Session</code>.
     */
    public Session getSession();
}
