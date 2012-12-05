/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 26.11.2003
 */
package com.jonasreese.sound.sg.ui.defaultui;

/**
 * <b>
 * A <code>StaticActionPool</code> is a set of <code>Action</code>
 * objects whose executive actions are common to all parts of the
 * application. As a consequence to this fact, there should be only
 * one instance of <code>StaticActionPool</code> existing within the
 * whole application, which can be requested in the <code>UiToolkit</code>
 * class.
 * </b>
 * @author jreese
 */
public interface StaticActionPool
{
    public static final String NEW_OBJECT = "newObject";
    public static final String NEW_SESSION = "newSession";
    public static final String OPEN_SESSION = "openSession";
    public static final String INSERT_FILE_INTO_SESSION = "insertFileIntoSession";
    public static final String CLOSE_SESSION = "closeSession";
    public static final String CLOSE_ALL_SESSIONS = "closeAllSession";
    public static final String EXIT_APPLICATION = "exitApplication";

    public static final String PROGRAM_SETTINGS = "programSettings";
    public static final String GARBAGE_COLLECTION = "garbageCollection";
    
    public static final String FULLSCREEN = "fullscreen";
    
    
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
}
