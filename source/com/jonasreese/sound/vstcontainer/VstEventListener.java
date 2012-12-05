/*
 * Created on 27.07.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.vstcontainer;

/**
 * <p>
 * A <code>VstEventListener</code> shall be implemented by classes that wish to receive
 * <code>VstEvent</code> object from one or more <code>VstPlugin</code>s.
 * </p>
 * <p>
 * Please note that this is not a typical event listener, since it does not inherit the
 * <code>java.util.EventListener</code> interface and does not receive event objects
 * inheriting <code>java.util.EventObject</code>. <code>VstEvent</code>s are retrieved
 * directly as an array, and the source <code>VstPlugin</code> is provided to the method
 * in order to avoid additional event object being created.
 * </p>
 * <p>
 * The <code>process()</code> method is not called from an event dispatcher thread, but
 * from the thread that created those events.
 * </p>
 * @author jonas.reese
 */
public interface VstEventListener {

    /**
     * Called when one or more <code>VstEvent</code>s arrived at the VST host.
     * @param events The events that can be processed.
     * @param plugin The <code>VstPlugin</code> that sent those events.
     */
    public void process( VstEvent[] events, VstPlugin plugin );
}
