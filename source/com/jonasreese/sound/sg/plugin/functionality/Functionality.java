/*
 * Copyright (c) 2004 Jonas Reese
 * Created on 14.10.2004
 */
package com.jonasreese.sound.sg.plugin.functionality;

import com.jonasreese.sound.sg.plugin.Plugin;

/**
 * This interface shall be implemented by classes that provide extra-functionality
 * to the SoundsGood application. This functionality is started when the application
 * is started and stopped when the application is stopped. Thus, a <code>Functionality</code>
 * implementation provides functionality in a static manner, availabe from the start of the
 * application until the end. Since this interface defines no methods, any functionality
 * implemented shall be applied when the <code>init()</code> method is called.
 * @author jonas.reese
 */
public interface Functionality extends Plugin {
    
    /**
     * Gets the property with the specified name. The namespace and the type of
     * the returned objects is defined by the <code>Functionality</code> implementation.
     * This method allows other plugins to use parts this <code>Functionality</code>
     * without giving up the weak interconnection. 
     * @param name The parameter name
     * @return An object, or <code>null</code> if property is not defined/specified.
     */
    public Object getProperty( String name );
}
