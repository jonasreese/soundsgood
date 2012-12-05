/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 07.09.2003
 */
package com.jonasreese.sound.sg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.StringTokenizer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.jonasreese.sound.sg.midi.*;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.util.AbstractEventRedirector;
import com.jonasreese.util.EventQueueHandler;
import com.jonasreese.util.ParametrizedResourceBundle;
import com.jonasreese.util.ProgressMonitoringInputStream;
import com.jonasreese.util.Updatable;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <b>
 * The SoundsGood application's engine class.
 * </b>
 * @author jreese
 */
public class SgEngine {

    private EventQueueHandler eventQueue;
    private static SgEngine instance = null;
    
    private List<Object[]> pluginList;
    private SgProperties properties;
    private List<Session> sessionList;
    private List<SessionListener> sessionListeners;
    private List<FileHandler> fileHandlers;
    private List<SessionElementCreationHandler> sessionElementCreationHandlers;
    private int activeSessionIndex;
    private Updatable loadingUpdatable;
    
    /**
     * Constructs a new <code>SgEngine</code>.
     */
    private SgEngine() {
        properties = new SgProperties();
        sessionList = new ArrayList<Session>();
        sessionListeners = new ArrayList<SessionListener>();
        fileHandlers = new ArrayList<FileHandler>();
        sessionElementCreationHandlers = new ArrayList<SessionElementCreationHandler>();
        activeSessionIndex = -1;
        eventQueue = new EventQueueHandler();
        loadingUpdatable = null;
    }
    
    /**
     * Gets the singleton instance of the <code>SgEngine</code>.
     * @return The non-<code>null</code> <code>SgEngine</code> instance.
     */
    public static SgEngine getInstance() {
        if (instance == null) {
            instance = new SgEngine();
        }
        return instance;
    }
    
    /**
     * Loads the plugin property file and loads and initializes the plugins.
     * This method shall be called before the <code>getPlugins()</code> method
     * is called.
     * @throws IOException If the property file could not be read.
     */
    public void loadPlugins() throws IOException {
        pluginList = new ArrayList<Object[]>();
        // load plugin properties
        BufferedReader r = new BufferedReader(
            new InputStreamReader(
                new ResourceLoader(
                    getClass(), "plugins.properties" ).getResourceAsStream() ) );
        String s;
        while ((s = r.readLine()) != null) {
            s = s.trim();
            if (!"".equals( s ) && !s.startsWith( "#" )) {
                StringTokenizer st = new StringTokenizer( s, "," );
                if (st.hasMoreTokens()) {
                    try {
                        Plugin plugin = (Plugin) Class.forName( st.nextToken() ).newInstance();
                        plugin.init();
                        Boolean isDefault = Boolean.FALSE;
                        String descriptorType = null;
                        if (st.hasMoreTokens() && st.nextToken().trim().equalsIgnoreCase( "DEFAULT" )) {
                            isDefault = Boolean.TRUE;
                            if (st.hasMoreTokens()) {
                                descriptorType = st.nextToken().trim();
                            }
                        }
                        pluginList.add( new Object[] { plugin, isDefault, descriptorType } );
                    }
                    // can't do anything about this...
                    catch (Throwable t) { t.printStackTrace(); }
                }
            }
        }
    }
    
    /**
     * Gets an array of <code>Plugin</code> objects describing the pluggable
     * pieces of software contained by the <code>SoundsGood</code> application.
     * The <code>loadPlugins()</code> method should be called before this method.
     * @return A non-<code>null</code> array if <code>loadPlugins()</code> has
     *         been called, or <code>null</code> otherwise.
     */
    public Plugin[] getPlugins() {
        if (pluginList == null) { return null; }
        Plugin[] plugins = new Plugin[pluginList.size()];
        for (int i = 0; i < plugins.length; i++) {
            plugins[i] = (Plugin) ((Object[]) pluginList.get( i ))[0];
        }
        return plugins;
    }

    /**
     * Gets a loaded <code>Plugin</code> by it's implementation class.
     * @param clazz The <code>Class</code> identifying the plugin Please note that
     *        an exact match is required, not an <code>instanceof</code>.
     * @return An instance of <code>Plugin</code> if one with the given
     *         class is loaded, or <code>null</code> otherwise.
     */
    public Plugin getPlugin( Class<? extends Plugin> clazz ) {
        if (pluginList == null) { return null; }
        for (int i = 0; i < pluginList.size(); i++) {
            Plugin plugin = (Plugin) ((Object[]) pluginList.get( i ))[0];
            if (plugin.getClass().equals( clazz )) {
                return plugin;
            }
        }
        return null;
    }
    
    /**
     * Gets a loaded <code>Plugin</code> by it's class name.
     * @param className The fully qualified class name.
     * @return The <code>Plugin</code>, or <code>null</code> if not found/loaded.
     */
    public Plugin getPlugin( String className ) {
        if (pluginList == null) { return null; }
        for (int i = 0; i < pluginList.size(); i++) {
            Plugin plugin = (Plugin) ((Object[]) pluginList.get( i ))[0];
            if (plugin.getClass().getName().equals( className )) {
                return plugin;
            }
        }
        return null;
    }
    
    /**
     * Gets a loaded default <code>Plugin</code> by it's implementation class.
     * @param clazz The <code>Class</code> identifying the plugin. Here, an
     *        <code>instanceof</code> check is performed.
     * @param descriptor The <code>SessionElementDescriptor</code>. If <code>null</code>,
     *         a non-<code>null</code> return value is only possible if a plugin is configured
     *         as default for all <code>SessionElementDescriptor</code> types.
     * @return An instance of <code>Plugin</code> if one default plugin with the given
     *         class is loaded, or <code>null</code> otherwise.
     */
    public Plugin getDefaultPlugin( Class<? extends Plugin> clazz, SessionElementDescriptor descriptor ) {
        if (pluginList == null) { return null; }
        for (int i = 0; i < pluginList.size(); i++) {
            Object[] o = (Object[]) pluginList.get( i );
            Plugin plugin = (Plugin) o[0];
            if (((Boolean) o[1]).booleanValue() &&
                clazz.isAssignableFrom( plugin.getClass() )) {
                // default plugin for all descriptor types found
                if (descriptor == null && o[2] == null) {
                    return plugin;
                }
                if (o[2] != null && descriptor != null) {
                    try {
                        Class<?> descClass = getClass().getClassLoader().loadClass( o[2].toString() );
                        if (descClass.isAssignableFrom( descriptor.getClass() )) {
                            return plugin;
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Gets the <code>EventQueueHandler</code> that is used for the
     * SG application. 
     * @return The <code>EventQueueHandler</code>.
     */
    public EventQueueHandler getEventQueue() {
        return eventQueue;
    }

    /**
     * Gets all <code>SessionListener</code>s that are currently registered to this
     * <code>SgEngine</code>.
     * @return An array containing all registered <code>SessionListener</code> objects.
     */
    public SessionListener[] getSessionListeners() {
        SessionListener[] result = new SessionListener[sessionListeners.size()];
        for (int i = 0; i < sessionListeners.size(); i++)
        {
            result[i] = sessionListeners.get( i );
        }
        return result;
    }
    
    /**
     * Adds a <code>SessionListener</code> to this <code>SgEngine</code>.
     * @param index The index where to add the listener. An index of 0 for
     *        example means that the given listener is invoked first.
     * @param l The listener to add.
     */
    public void addSessionListener( int index, SessionListener l ) {
        sessionListeners.add( index, l );
    }
    
    /**
     * Adds a <code>SessionListener</code> to this <code>SgEngine</code>.
     * @param l The listener to add.
     */
    public void addSessionListener( SessionListener l ) {
        sessionListeners.add( l );
    }
    
    /**
     * Removes a <code>SessionListener</code> from this <code>SgEngine</code>.
     * @param l The listener to add.
     */
    public void removeSessionListener( SessionListener l ) {
        sessionListeners.remove( l );
    }
    
    /**
     * Sets the properties.
     * @param properties The properties to set.
     */
    public void setProperties( SgProperties properties ) {
        this.properties = properties;
    }
    
    /**
     * Gets the properties.
     * @return The properties.
     */
    public SgProperties getProperties() {
        return properties;
    }
    
    /**
     * Convenience method to get the resource bundle.
     * @return Same as <code>getProperties().getResourceBundle()</code>.
     */
    public ParametrizedResourceBundle getResourceBundle() {
        return properties.getResourceBundle();
    }
    
    /**
     * Adds the given session to this <code>SgEngine</code>.
     * @param session The session to be registered.
     * @param newSession Shall be <code>true</code> if a newly created
     *        session is added.
     */
    protected void addSession( Session session, boolean newSession ) {
        int index = sessionList.size();
        sessionList.add( session );
        // dispatch events
        SessionEvent event = new SessionEvent( this, session, index, newSession );
        for (int i = 0; i < sessionListeners.size(); i++) {
            SessionListener l = (SessionListener) sessionListeners.get( i );
            AbstractEventRedirector r = new AbstractEventRedirector( l ) {
    			public void redirectEvent( EventObject e ) {
                    SessionEvent se = (SessionEvent) e;
                    ((SessionListener) getListener()).sessionAdded( se );
                }
    		};
            eventQueue.addQueueEntry( r, event );
        }
        session.sessionAdded( event );
        eventQueue.processEvents();
    }
    
    /**
     * Gets the total number of sessions currently present in the <code>SgEngine</code>.
     * @return The session count.
     */
    public int getSessionCount() {
        return sessionList.size();
    }
    
    /**
     * Gets the <code>Session</code> at the specified logical index.
     * @param index The logical index.
     * @return The session.
     * @throws ArrayIndexOutOfBoundsException if the logical index is out of bounds.
     */
    public Session getSessionAt( int index ) {
        return (Session) sessionList.get( index );
    }
    
    /**
     * Removes the given session.
     * @param session The session to be removed.
     */
    public void removeSession( Session session ) {
        int i = sessionList.indexOf( session );
        removeSession( i );
    }
    
    /**
     * Removes the session at the specified index.
     * @param index The index of the session that is to be removed.
     */
    public void removeSession( int index ) {
        if (index >= 0) {
            Session session = (Session) sessionList.get( index );
            sessionList.remove( index );
            SessionEvent event = new SessionEvent( this, session, index, false );
            for (int i = 0; i < sessionListeners.size(); i++) {
                SessionListener l = (SessionListener) sessionListeners.get( i );
                AbstractEventRedirector r = new AbstractEventRedirector( l ) {
                    public void redirectEvent( EventObject e ) {
                        SessionEvent se = (SessionEvent) e;
                        ((SessionListener) getListener()).sessionRemoved( se );
                    }
                };
                eventQueue.addQueueEntry( r, event );
            }
            eventQueue.processEvents();
            session.sessionRemoved( event );
            if (activeSessionIndex >= index) {
                int newActiveSessionIndex = index;
                activeSessionIndex = -1;
                if (newActiveSessionIndex >= sessionList.size()) {
                    newActiveSessionIndex = sessionList.size() - 1;
                }
                setActiveSession( newActiveSessionIndex );
            }
            session.destroy();
        }
    }
    
    /**
     * Sets the given <code>Session</code> object to the currently
     * active session. If the given <code>Session</code> object is not
     * registered as open session to this <code>SgEngine</code>, this method
     * does nothing and will not change the current active session.
     * @param session The <code>Session</code> to set to active state, or
     *        <code>null</code> if no session shall be active after method
     *        call.
     */
    public void setActiveSession( Session session ) {
        if (session == null) {
            setActiveSession( -1 );
        } else {
            int index = sessionList.indexOf( session );
            if (index >= 0) {
                setActiveSession( index );
            }
        }
    }
    
    /**
     * Sets the currently active session by the session object's
     * array index. Use <code>getSessions()</code> method to obtain the
     * according array.
     * @param sessionIndex The session's index. If the index is out of
     *        bounds, this method will set all sessions to be inactive.
     */
    public void setActiveSession( int sessionIndex ) {
        Session session = getActiveSession();
        int oldActiveSessionIndex = activeSessionIndex;
        if (sessionIndex < 0 || sessionIndex >= sessionList.size()) {
            activeSessionIndex = -1;
        } else {
            activeSessionIndex = sessionIndex;
        }
        // dispatch events
        if (oldActiveSessionIndex != activeSessionIndex) {
            if (session != null) {
                SessionEvent event = new SessionEvent( this, session, activeSessionIndex, false );
                for (int i = 0; i < sessionListeners.size(); i++) {
                    SessionListener l = (SessionListener) sessionListeners.get( i );
                    AbstractEventRedirector r = new AbstractEventRedirector( l ) {
                        public void redirectEvent( EventObject e ) {
                            SessionEvent se = (SessionEvent) e;
                            SessionListener sl = (SessionListener) getListener();
                            sl.sessionDeactivated( se );
                        }
                    };
                    eventQueue.addQueueEntry( r, event );
                }
                eventQueue.processEvents();
                session.sessionDeactivated( event );
            }

            // dispatch events
            session = ((activeSessionIndex < 0) ?
                null : (Session) sessionList.get( activeSessionIndex ));
            SessionEvent event = new SessionEvent( this, session, activeSessionIndex, false );
            for (int i = 0; i < sessionListeners.size(); i++) {
                SessionListener l = (SessionListener) sessionListeners.get( i );
                AbstractEventRedirector r = new AbstractEventRedirector( l ) {
                    public void redirectEvent( EventObject e ) {
                        SessionEvent se = (SessionEvent) e;
                        SessionListener sl = (SessionListener) getListener();
                        sl.sessionActivated( se );
                    }
                };
                eventQueue.addQueueEntry( r, event );
            }
            eventQueue.processEvents();
            if (session != null) {
                session.sessionActivated( event );
            }
        }
    }
    
    /**
     * Gets the currently active session, or <code>null</code>
     * if no session is currently active.
     * @return The active session, or <code>null</code>.
     */
    public Session getActiveSession() {
        if (activeSessionIndex < 0 || activeSessionIndex >= sessionList.size()) {
            return null;
        }
        return (Session) sessionList.get( activeSessionIndex );
    }
    
    /**
     * Gets the currently active session's index, or <code>-1</code>
     * if no session is currently active.
     * @return The active session index, or <code>-1</code>.
     */
    public int getActiveSessionIndex() {
        if (activeSessionIndex < 0 || activeSessionIndex >= sessionList.size()) {
            return -1;
        }
        return activeSessionIndex;
    }
    
    /**
     * Creates a session using the given session descriptor file
     * and registeres it with this <code>SgEngine</code> as an
     * open session <b>and</b> as the currently active session.
     * @param sessionDescriptorFile The <code>File</code> that
     *        contains all required session information.
     * @return A newly created and registered <code>Session</code>
     *         object.
     * @throws IOException if the session could not be saved to
     *         the given descriptor file.
     */
    public Session createSession( File sessionDescriptorFile ) throws IOException {
        Session session = new Session( sessionDescriptorFile, false );
        session.setName( sessionDescriptorFile.getName() );
        int activeSessionIndex = sessionList.size();
        addSession( session, true );
        setActiveSession( activeSessionIndex );
        return session;
    }
    
    /**
     * Loads a session from the given session descriptor file
     * and registeres it with this <code>SgEngine</code> as an
     * open session <b>and</b> as the currently active session.
     * @param sessionDescriptorFile The <code>File</code> that
     *        contains all required session information.
     * @return A newly created and registered <code>Session</code>
     *         object.
     * @throws IOException if the session could not be loaded from
     *         the given descriptor file.
     */
    public Session loadSession( File sessionDescriptorFile )
        throws IOException, ParserConfigurationException, SAXException, TransformerException {

        FileInputStream fin = new FileInputStream( sessionDescriptorFile );
        Session session = new Session( sessionDescriptorFile, false );
        session.setName( sessionDescriptorFile.getName() );
        session.readXml( fin );
        fin.close();
        int activeSessionIndex = sessionList.size();
        addSession( session, false );
        setActiveSession( activeSessionIndex );
        return session;
    }
    
    /**
     * Gets the currently open sessions.
     * @return A newly constructed array of <code>Session</code> objects,
     *         one for each session that is currently open.
     */
    public Session[] getSessions() {
        Session[] sessions = new Session[sessionList.size()];
        for (int i = 0; i < sessions.length; i++) {
            sessions[i] = (Session) sessionList.get( i );
        }
        return sessions;
    }
    
    /**
     * Sets the <code>Updatable</code> that keeps track of
     * loading progress operations.
     * @param loadingUpdatable The loading <code>Updatable</code>.
     *        The argument for the <code>update(Object)</code> method
     *        will be a <code>ProgressMonitoringFileInputStream</code>.
     */
    public void setLoadingUpdatable( Updatable loadingUpdatable ) {
        this.loadingUpdatable = loadingUpdatable;
    }
    
    /**
     * Gets the <code>Updatable</code> that keeps track of
     * loading progress operations.
     * @return The loading <code>Updatable</code>.
     */
    public Updatable getLoadingUpdatable() {
        return loadingUpdatable;
    }
    
    /**
     * Adds a file handler. A <code>FileHandler</code> can perform the required
     * action when the user has chosen to open a certain type of file.
     * @param fileHandler The <code>FileHandler</code> to be added.
     */
    public void addFileHandler( FileHandler fileHandler ) {
        synchronized (fileHandlers) {
            fileHandlers.add( fileHandler );
        }
    }
    
    /**
     * Gets all registered <code>FileHandler</code>s.
     * @return An array of all registered <code>FileHandler</code> objects.
     */
    public FileHandler[] getFileHandlers() {
        synchronized (fileHandlers) {
            FileHandler[] result = new FileHandler[fileHandlers.size()];
            fileHandlers.toArray( result );
            return result;
        }
    }
    
    /**
     * Adds a SessionElement creation handler. A <code>SessionElementCreationHandler</code>
     * can perform the required action when a user has chosen to create a new
     * session element of a certain type.
     * @param handler The <code>SessionElementCreationHandler</code> to be added.
     */
    public void addSessionElementCreationHandler( SessionElementCreationHandler handler ) {
        synchronized (sessionElementCreationHandlers) {
            sessionElementCreationHandlers.add( handler );
        }
    }
    
    /**
     * Gets all registered <code>SessionElementCreationHandler</code>s.
     * @return An array of all registered <code>SessionElementCreationHandler</code> objects.
     */
    public SessionElementCreationHandler[] getSessionElementCreationHandlers() {
        synchronized (sessionElementCreationHandlers) {
            SessionElementCreationHandler[] result =
                new SessionElementCreationHandler[sessionElementCreationHandlers.size()];
            sessionElementCreationHandlers.toArray( result );
            return result;
        }
    }
    
    /**
     * Loads a <code>Sequence</code> from a <code>MidiDescriptor</code>
     * and returns it. Before returning the sequence, it is also set
     * in the given <code>MidiDescriptor</code> using the 
     * <code>setSequence()</code> method. If the given <code>MidiDescriptor</code>
     * does not (yet) point to a file, a newly created <code>SgMidiSequence</code>
     * is returned by this method.
     * @param descriptor The descriptor pointing to the file to load
     *        a <code>Sequence</code> from.
     * @return The sequence, either loaded from the given descriptor's
     *         file, or, if non-<code>null</code>, from the descriptor's
     *         <code>getSequence()</code> method.
     */
    public SgMidiSequence loadSequence( MidiDescriptor descriptor )
        throws InvalidMidiDataException, IOException {

        if (descriptor.getFile() == null) {
            // create a new empty MIDI sequence
            SgMidiSequence seq = new SgMidiSequence(
                Sequence.PPQ, SgEngine.getInstance().getProperties().getMidiResolution(), descriptor );
            seq.createTrackProxy( this );
            descriptor.setSequence( seq );
            return seq;
        } else {
            MyProgressMonitoringInputStream is =
                new MyProgressMonitoringInputStream(
                    new FileInputStream( descriptor.getFile() ),
                    descriptor.getFile(), 200, loadingUpdatable );
            Sequence seq = null;
            try {
                seq = MidiSystem.getSequence( is );
                // since we have problems with a Sun implementation
                // of ShortMessage, we change to the original ShortMessage
                // therefor, we copy the whole sequence
                int c = 0;
                Track[] tracks = seq.getTracks();
                SgMidiSequence copiedSeq =
                    new SgMidiSequence( seq.getDivisionType(), seq.getResolution(), tracks.length, descriptor );
                Track[] copiedTracks = copiedSeq.getTracks();
                for (int i = 0; i < tracks.length; i++) {
                    for (int j = 0; j < tracks[i].size(); j++) {
                        MidiEvent event = tracks[i].get( j );
                        MidiMessage msg = event.getMessage();
                        if (msg instanceof ShortMessage) {
                            ShortMessage message = (ShortMessage) msg;
                            ShortMessage message2 = new ShortMessage();
                            message2.setMessage(
                                message.getStatus(),
                                message.getData1(),
                                message.getData2() );
                            MidiEvent event2 = new MidiEvent( message2, event.getTick() );
    
                            copiedTracks[i].add( event2 );
                            c++;
                        } else {
                            copiedTracks[i].add( event );
                        }
                    }
                }
                //System.out.println( "replaced " + c + " midi events" );
                
                // restore track properties from midi descriptor persisten client properties
                descriptor.setSequence( copiedSeq );
                TrackProxy[] trackProxies = copiedSeq.getTrackProxies();
                for (int i = 0; i < trackProxies.length; i++) {
                    trackProxies[i].restoreProperties();
                }
                
                // avoid sequences that are too long, compared to their last MIDI event
                long maxlen = copiedSeq.getActualLength() * 3;
                if (copiedSeq.getLength() > maxlen) {
                    TrackProxy[] tp = copiedSeq.getTrackProxies();
                    for (int i = 0; i < tp.length; i++) {
                        tp[i].setLength( Math.min( tp[i].getLength(), maxlen ), null );
                    }
                }
                return copiedSeq;
            } catch (Throwable t) {
                t.printStackTrace();
                if (t instanceof IOException) { throw (IOException) t; }
                if (t instanceof InvalidMidiDataException) {
                    throw (InvalidMidiDataException) t;
                }
            }
        }
        return null;
    }
    
    /**
     * Stops the engine and performs a System.exit().
     */
    public void stopEngine() {
        try {
            for (int i = 0; i < pluginList.size(); i++) {
                ((Plugin) ((Object[]) pluginList.get( i ))[0]).exit();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        Session[] sessions = getSessions();
        for (Session session : sessions) {
            session.destroy();
        }
        System.exit( 0 );
    }
    
    
    
    
    /// an own implementation to do some hacking with it
    class MyProgressMonitoringInputStream extends ProgressMonitoringInputStream {
        Updatable updatable;
        boolean done = false;
        boolean firstCall = true;
        public MyProgressMonitoringInputStream(
            InputStream arg0, Object arg1, int arg2, Updatable updatable) throws IOException {
            super(arg0, arg1, arg2);
            setUpdatable( updatable );
            this.updatable = updatable;
        }
    }
}