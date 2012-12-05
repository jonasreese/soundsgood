/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 07.09.2003
 */
package com.jonasreese.sound.sg;

import java.awt.Color;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import com.jonasreese.sound.sg.audio.AudioDeviceDescriptor;
import com.jonasreese.sound.sg.audio.AudioDeviceList;
import com.jonasreese.sound.sg.midi.MidiDeviceDescriptor;
import com.jonasreese.sound.sg.midi.MidiDeviceId;
import com.jonasreese.sound.sg.midi.MidiDeviceList;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.util.ParametrizedResourceBundle;

/**
 * <b>
 * The class encapsulating the SoundsGood application properties.
 * </b>
 * @author jreese
 */
public class SgProperties
{
    /// the timestamp containing the last application start
    public static long lastStart = -1;

    static
    {
        lastStart = System.currentTimeMillis();
    }


    private Properties p;
    
    private PropertyChangeSupport pcs;
    private ParametrizedResourceBundle resourceBundle;

    private static Locale[] availableResoureLocales = null;

    public static final int SHOW_TIPS_NEVER = 0;
    public static final int SHOW_TIPS_ONCE_A_DAY = 1;
    public static final int SHOW_TIPS_ON_EVERY_START = 2;
    
    public static final int VIEW_MODE_INTERNAL_FRAMES = 0;
    public static final int VIEW_MODE_TABBED = 1;
    public static final int VIEW_MODE_DOCKING = 2;
    public static final int VIEW_MODE_EXTENDED_DOCKING = 3;


    /** The file header */
    public static final String FILE_HEADER = "SoundsGood application configuration file - (c) 2003, 2004 Jonas Reese";

    /** The ResourceBundle prefix. */
    public static final String RESOURCE_BUNDLE =
        "com.jonasreese.sound.sg.Language";

    /** The resource where all available languages are listed. */
    public static final String AVAILABLE_RESOURCE_LOCALES_LIST =
        "com/jonasreese/sound/sg/Languages";

    public static final String SESSIONS_KEY = "sessions.list";
    
    public static final String SESSION_INDEX_KEY = "sessions.list.index";

    public static final String RESOURCE_LOCALE_KEY = "ui.language";
    public static final Locale DEFAULT_RESOURCE_LOCALE = getAvailableResourceLocales()[0];
    
    public static final String VIEW_MODE_KEY = "ui.viewmode";
    public static final int    DEFAULT_VIEW_MODE = VIEW_MODE_INTERNAL_FRAMES;

    public static final String LNF_CLASS_NAME_KEY = "lnf.class";
    public static final String DEFAULT_LNF_CLASS_NAME = null;

    public static final String SAVE_DIRECTORY_KEY = "save.directory";
    public static final String DEFAULT_SAVE_DIRECTORY = System.getProperty( "user.dir" );

    public static final String MIDI_UPDATE_TIME_KEY = "midi.update.time";
    public static final int    DEFAULT_MIDI_UPDATE_TIME = 50;

    public static final String MIDI_RESOLUTION_KEY = "midi.format.resolution";
    public static final int    DEFAULT_MIDI_RESOLUTION = 48;
    
    public static final String DEFAULT_MIDI_TEMPO_KEY = "midi.format.defaulttempo";
    public static final int    DEFAULT_DEFAULT_MIDI_TEMPO = 120;

    public static final String  OPEN_LAST_SESSIONS_ON_STARTUP_KEY = "program.startup.openlastsession";
    public static final boolean DEFAULT_OPEN_LAST_SESSIONS_ON_STARTUP = true;

    public static final String  AUTO_SAVE_SESSION_ON_CLOSE_KEY = "session.close.autosave";
    public static final boolean DEFAULT_AUTO_SAVE_SESSION_ON_CLOSE = true;
    
    public static final String  RESTORE_VIEWS_FROM_SESSION_KEY = "session.view.restore";
    public static final boolean DEFAULT_RESTORE_VIEWS_FROM_SESSION = true;

    public static final String FRAME_BOUNDS_KEY = "ui.bounds";
    public static final Rectangle DEFAULT_FRAME_BOUNDS = null;
    
    public static final String FRAME_MAXIMIZED_KEY = "ui.maximized";
    
    public static final String SESSION_DIRECTORY_KEY = "session.directory";
    public static final String DEFAULT_SESSION_DIRECTORY = System.getProperty( "user.dir" );

    public static final String UNDO_STEPS_KEY = "session.undo.steps";
    public static final int DEFAULT_UNDO_STEPS = 50;

    public static final String FILE_DIRECTORY_KEY = "file.directory";
    public static final String DEFAULT_FILE_DIRECTORY = System.getProperty( "user.dir" );
    
    public static final String AUDIO_FORMAT_SAMPLE_RATE_KEY = "audio.format.samplerate";
    public static final float  AUDIO_FORMAT_DEFAULT_SAMPLE_RATE = 44100f;
    public static final String AUDIO_FORMAT_SAMPLE_SIZE_KEY = "audio.format.samplesize";
    public static final int    AUDIO_FORMAT_DEFAULT_SAMPLE_SIZE = 16;
    public static final String AUDIO_FORMAT_MONO_KEY = "audio.format.mono";
    public static final boolean AUDIO_FORMAT_DEFAULT_MONO = false;
    public static final String AUDIO_FORMAT_BIG_ENDIAN_KEY = "audio.format.bigendian";
    public static final boolean AUDIO_FORMAT_DEFAULT_BIG_ENDIAN = false;
    public static final String AUDIO_FORMAT_SIGNED_KEY = "audio.format.signed";
    public static final boolean AUDIO_FORMAT_DEFAULT_SIGNED = true;

    public static final String AUDIO_INPUT_DEVICE_KEY = "audio.input.device";
    public static final String AUDIO_OUTPUT_DEVICE_KEY = "audio.output.device";
    
    public static final String MIDI_INPUT_DEVICE_KEY = "midi.input.device";
    public static final String MIDI_OUTPUT_DEVICE_KEY = "midi.output.device";
    public static final String MIDI_CLICK_DEVICE_KEY = "midi.click.device";
    public static final String MIDI_SEQUENCER_KEY = "midi.sequencer.device";
    
    public static final String ENABLE_RECORD_LOOPBACK_PER_DEFAULT_KEY = "midi.record.enable_loopback";
    public static final boolean DEFAULT_ENABLE_RECORD_LOOPBACK_PER_DEFAULT = true;
    
    public static final String MINIMUM_RECORD_SAFETY_SECONDS_KEY = "midi.record.minimum_safety_seconds";
    public static final int DEFAULT_MINIMUM_RECORD_SAFETY_SECONDS = 5;

    public static final String RECORD_INCREMENT_SECONDS_KEY = "midi.record.minimum_safety_seconds";
    public static final int DEFAULT_RECORD_INCREMENT_SECONDS = 10;

    public static final String SHOW_TIPS_KEY = "startup.show_tips";
    public static final int DEFAULT_SHOW_TIPS = SHOW_TIPS_ON_EVERY_START;

    public static final String TIPS_INDEX_KEY = "tips.index";
    public static final int DEFAULT_TIPS_INDEX = 0;

    public static final String LAST_START_KEY = "general.last_start";

    public static final String PLUGIN_KEY_PREFIX = "plugin.";

    public static final String NOTE_CLICK_KEY = "midi.click.note_channel";
    public static final short[] DEFAULT_NOTE_CLICK = { 0, 0, 127, 50 };

    public static final String NOTE_CLICK_ONE_KEY = "midi.click.note_channel_one";
    public static final short[] DEFAULT_NOTE_CLICK_ONE = { 0, 0, 127, 50 };
    
    public static final String CLICKS_PER_TACT_KEY = "midi.click.clicks_per_tact";
    public static final int DEFAULT_CLICKS_PER_TACT = 4;
    
    public static final String STRESS_ON_CLICK_ONE_KEY = "midi.click.stress_on_one";
    public static final boolean DEFAULT_STRESS_ON_CLICK_ONE = true;
    
    public static final String AUDIO_BUFFER_LENGTH_KEY = "audio.buffer.milliseconds";
    public static final int MIN_AUDIO_BUFFER_LENGTH = 0;
    public static final int DEFAULT_AUDIO_BUFFER_LENGTH = 10;
    public static final int MAX_AUDIO_BUFFER_LENGTH = 100;

    private boolean useMmjPatch = false;
    
    private ArrayList<Session> openSessions;
    private int activeSessionIndex;

    /**
     * Constructs a new <code>SgProperties</code>
     * object with the default properties set.
     */
    public SgProperties()
    {
        p = new Properties();
        pcs = new PropertyChangeSupport( this );
        resourceBundle = null;
        openSessions = new ArrayList<Session>();
    }
    
    /**
     * Constructs a new <code>SgProperties</code>
     * object with the default properties set that loads
     * all properties from the given file afterwards.
     * @param f The file to load the properties from.
     * @throws IOException if the properties could not be loaded.
     */
    public SgProperties( File f ) throws IOException
    {
        this();
        loadProperties( f );
    }

    /**
     * Loads the properties from the specified file.
     * @param f The file to load the properties from.
     * @throws IOException if the properties could not be loaded.
     */
    public void loadProperties( File f ) throws IOException
    {
        p.load( new FileInputStream( f ) );
    }

    /**
     * Add a PropertyChangeListener to the listener list.
     * The listener is registered for all properties.
     * @param listener The PropertyChangeListener to be added.
     */
    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        pcs.addPropertyChangeListener( listener );
    }
    
    /**
     * Add a PropertyChangeListener for a specific property.
     * The listener will be invoked only when a call on firePropertyChange
     * names that specific property.
     * @param propertyName The name of the property to listen on.
     * @param listener The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener( String propertyName, PropertyChangeListener listener ) {
        pcs.addPropertyChangeListener( propertyName, listener );
    }
    
    /**
     * Remove a PropertyChangeListener from the listener list.
     * This removes a PropertyChangeListener that was registered for
     * all properties.
     * @param listener The PropertyChangeListener to be removed.
     */
    public void removePropertyChangeListener( PropertyChangeListener listener ) {
        pcs.removePropertyChangeListener( listener );
    }
    
    /**
     * Remove a PropertyChangeListener for a specific property.
     * @param propertyName The name of the property that was listened on.
     * @param listener The PropertyChangeListener to be removed.
     */
    public void removePropertyChangeListener( String propertyName,
                                              PropertyChangeListener listener ) {
        pcs.removePropertyChangeListener( propertyName, listener );
    }
    
    /**
     * Returns all registered <code>PropertyChangeListener</code>s as an array.
     * @return An array with all registered listeners.
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }
    
    /**
     * Returns all <code>PropertyChangeListener</code>s registered for the given
     * property as an array.
     * @param propertyName The property name.
     * @return An array with all registered listeners.
     */
    public PropertyChangeListener[] getPropertyChangeListeners( String propertyName ) {
        return pcs.getPropertyChangeListeners( propertyName );
    }
    
    /**
     * Check if there are any listeners for a specific property.
     * @param propertyName The property name.
     * @return <code>true</code> if there are ore or more listeners
     *         for the given property.
     * 
     */
    public boolean hasListeners( String propertyName )
    {
        return pcs.hasListeners( propertyName );
    }
    
    /**
     * Indicates if the MMJ (MIDI for Java on Mac) workaround shall be used.
     * @return <code>true</code> if and only if MMJ patch is being used.
     */
    public boolean isUsingMmjPatch() {
        return useMmjPatch;
    }
    
    /**
     * Sets the MMJ usage.
     * @see #isUsingMmjPatch()
     * @param useMmjPatch Set to <code>true</code> to indicate MMJ patch usage.
     */
    public void setUseMmjPatch(boolean useMmjPatch) {
        this.useMmjPatch = useMmjPatch;
    }
    
    /**
     * Gets the current <code>ResourceBundle</code>.
     * @return The current <code>ResourceBundle</code>.
     */
    public ParametrizedResourceBundle getResourceBundle()
    {
        if (resourceBundle == null)
        {
            try
            {
                resourceBundle = new ParametrizedResourceBundle( ResourceBundle.getBundle(
                    RESOURCE_BUNDLE, getResourceLocale() ) );
            }
            catch (MissingResourceException mrex)
            {
                mrex.printStackTrace();
                resourceBundle = new ParametrizedResourceBundle( ResourceBundle.getBundle(
                    RESOURCE_BUNDLE, DEFAULT_RESOURCE_LOCALE ) );
            }
        }
        return resourceBundle;
    }

    /**
     * Gets the <code>Locale</code> that shall be used for
     * resources. This is especially used for the language
     * <code>ResourceBundle</code>.
     * @return The resource <code>Locale</code>.
     */
    public Locale getResourceLocale()
    {
        String s = p.getProperty( RESOURCE_LOCALE_KEY );
        if (s != null && !"".equals( s ))
        {
            return parseLocale( s );
        }
        return DEFAULT_RESOURCE_LOCALE;
    }

    /**
     * Sets the <code>Locale</code> that shall be used for
     * resources. This is especially used for the language
     * <code>ResourceBundle</code>.
     * @param locale The resource <code>Locale</code>.
     *        Must <b>not</b> be <code>null</code>.
     */
    public void setResourceLocale( Locale locale )
    {
        Locale l = getResourceLocale();
        if (!l.getCountry().equals( locale.getCountry() ) ||
            !l.getLanguage().equals( locale.getLanguage() ))
        {
            p.setProperty( RESOURCE_LOCALE_KEY, locale.toString() );
            pcs.firePropertyChange( "resourceLocale", l, locale );
        }
    }
    
    /**
     * Gets all available resource locales.
     * @return A non-empty array of <code>Locale</code> objects,
     *         representing an available resource locale each.
     */
    public static Locale[] getAvailableResourceLocales()
    {
        if (availableResoureLocales != null) {
            return availableResoureLocales; 
        }
        ArrayList<Locale> list = new ArrayList<Locale>();

        try {
            InputStream is =
                SgProperties.class.getClassLoader().getResourceAsStream(
                    AVAILABLE_RESOURCE_LOCALES_LIST );
            LineNumberReader lnr = new LineNumberReader(
                new InputStreamReader( is ) );
            String line = null;
            line = lnr.readLine();
            while (line != null) {
                Locale l = parseLocale( line );
                if (l != null) {
                    list.add( l );
                }
                line = lnr.readLine();
            }
        } catch (IOException ioex) { ioex.printStackTrace(); }

        Locale[] result = new Locale[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (Locale) list.get( i );
            System.out.println( result[i] );
        }
        
        availableResoureLocales = result;
        return result;
    }

    /**
     * Parses the given string and returns a Locale.
     * @param s The string to parse.
     * @return The according Locale, or <code>null</code>
     *         if the given string is not a valid one.
     */
    private static Locale parseLocale( String s )
    {
        if (s == null || "".equals( s )) { return null; }
        StringTokenizer st = new StringTokenizer(
            s, "_" );
        Locale l;
        if (st.countTokens() == 1)
        {
            l = new Locale( st.nextToken() );
        }
        else if (st.countTokens() == 2)
        {
            l = new Locale( st.nextToken(), st.nextToken() );
        }
        else
        {
            l = new Locale(
                st.nextToken(), st.nextToken(), st.nextToken() );
        }
        return l;
    }

    /**
     * Sets the MIDI device update time in milliseconds.
     * @param updateTime The update time in milliseconds.
     */
    public void setMidiUpdateTime( int updateTime )
    {
        int oldVal = getMidiUpdateTime();
        if (oldVal == updateTime) { return; }
        p.put( MIDI_UPDATE_TIME_KEY, "" + updateTime );
        pcs.firePropertyChange( "midiUpdateTime", oldVal, updateTime );
    }
    
    /**
     * Gets the MIDI device update time in milliseconds.
     * @return The update time in milliseconds.
     */
    public int getMidiUpdateTime()
    {
        String s = p.getProperty( MIDI_UPDATE_TIME_KEY );
        if (s == null) { return DEFAULT_MIDI_UPDATE_TIME; }
        try
        {
            return Integer.parseInt( s );
        }
        catch (Exception ignored)
        {
        }
        return DEFAULT_MIDI_UPDATE_TIME;
    }

    /**
     * Sets the MIDI resolution.
     * @param resolution The MIDI resolution to set.
     */
    public void setMidiResolution( int resolution )
    {
        int oldVal = getMidiResolution();
        if (oldVal == resolution) { return; }
        p.put( MIDI_RESOLUTION_KEY, "" + resolution );
        pcs.firePropertyChange( "midiResolution", oldVal, resolution );
    }
    
    /**
     * Gets the MIDI resolution.
     * @return The MIDI resoultion value.
     */
    public int getMidiResolution()
    {
        String s = p.getProperty( MIDI_RESOLUTION_KEY );
        if (s == null) { return DEFAULT_MIDI_RESOLUTION; }
        try
        {
            return Integer.parseInt( s );
        }
        catch (Exception ignored)
        {
        }
        return DEFAULT_MIDI_RESOLUTION;
    }

    /**
     * Sets the default MIDI tempo in BPM.
     * @param tempo The default MIDI tempo to set.
     */
    public void setDefaultMidiTempo( int tempo )
    {
        int oldVal = getDefaultMidiTempo();
        if (oldVal == tempo) { return; }
        p.put( DEFAULT_MIDI_TEMPO_KEY, "" + tempo );
        pcs.firePropertyChange( "defaultMidiTempo", oldVal, tempo );
    }
    
    /**
     * Gets the default MIDI temp in BPM.
     * @return The default MIDI tempo value.
     */
    public int getDefaultMidiTempo()
    {
        String s = p.getProperty( DEFAULT_MIDI_TEMPO_KEY );
        if (s == null) { return DEFAULT_DEFAULT_MIDI_TEMPO; }
        try
        {
            return Integer.parseInt( s );
        }
        catch (Exception ignored)
        {
        }
        return DEFAULT_DEFAULT_MIDI_TEMPO;
    }

    /**
     * Sets the undo steps number.
     * @param undoSteps The number of undo steps to set.
     */
    public void setUndoSteps( int undoSteps )
    {
        int oldVal = getUndoSteps();
        if (oldVal == undoSteps) { return; }
        p.put( UNDO_STEPS_KEY, "" + undoSteps );
        pcs.firePropertyChange( "undoSteps", oldVal, undoSteps );
    }
    
    /**
     * Gets the undo steps number.
     * @return The number of undo steps.
     */
    public int getUndoSteps()
    {
        String s = p.getProperty( UNDO_STEPS_KEY );
        if (s == null) { return DEFAULT_UNDO_STEPS; }
        try
        {
            return Integer.parseInt( s );
        }
        catch (Exception ignored)
        {
        }
        return DEFAULT_MIDI_UPDATE_TIME;
    }

    /**
     * Sets the "open last sessions on program startup" property.
     * @param b The property to set.
     */
    public void setOpenLastSessionsOnStartup( boolean b )
    {
        boolean oldVal = getOpenLastSessionsOnStartup();
        if (oldVal == b) { return; }
        p.put( OPEN_LAST_SESSIONS_ON_STARTUP_KEY, "" + b );
        pcs.firePropertyChange( "openLastSessionOnStartup", oldVal, b );
    }
    
    /**
     * Gets the "open last sessions on program startup" property.
     * @return The property set or the default property.
     */
    public boolean getOpenLastSessionsOnStartup()
    {
        String s = p.getProperty( OPEN_LAST_SESSIONS_ON_STARTUP_KEY );
        if ("true".equals( s ) || "false".equals( s ))
        {
            return "true".equals( s );
        }
        return DEFAULT_OPEN_LAST_SESSIONS_ON_STARTUP;
    }

    /**
     * Sets the "automatically save session when it is closed" property.
     * @param b The property to set.
     */
    public void setAutoSaveSessionOnClose( boolean b )
    {
        boolean oldVal = getAutoSaveSessionOnClose();
        if (oldVal == b) { return; }
        p.put( AUTO_SAVE_SESSION_ON_CLOSE_KEY, "" + b );
        pcs.firePropertyChange( "autoSaveSessionOnClose", oldVal, b );
    }
    
    /**
     * Gets the "automatically save session when it is closed" property.
     * @return The property set or the default property.
     */
    public boolean getAutoSaveSessionOnClose()
    {
        String s = p.getProperty( AUTO_SAVE_SESSION_ON_CLOSE_KEY );
        if ("true".equals( s ) || "false".equals( s ))
        {
            return "true".equals( s );
        }
        return DEFAULT_AUTO_SAVE_SESSION_ON_CLOSE;
    }

    /**
     * Gets the "restore session child views from session" property.
     * @param b The property to set.
     */
    public void setRestoreViewsFromSession( boolean b )
    {
        boolean oldVal = getRestoreViewsFromSession();
        if (oldVal == b) { return; }
        p.put( RESTORE_VIEWS_FROM_SESSION_KEY, "" + b );
        pcs.firePropertyChange( "restoreViewsFromSession", oldVal, b );
    }
    
    /**
     * Gets the "restore session child views from session" property.
     * @return The property set or the default property.
     */
    public boolean getRestoreViewsFromSession()
    {
        String s = p.getProperty( RESTORE_VIEWS_FROM_SESSION_KEY );
        if ("true".equals( s ) || "false".equals( s ))
        {
            return "true".equals( s );
        }
        return DEFAULT_RESTORE_VIEWS_FROM_SESSION;
    }

    /**
     * Gets the frame bounds.
     * @return A <code>Rectangle</code> object that contains the frame bounds
     *         May be <code>null</code>.
     */
    public Rectangle getFrameBounds() {
        String s = p.getProperty( FRAME_BOUNDS_KEY );
        if (s == null) { return DEFAULT_FRAME_BOUNDS; }
        try {
            StringTokenizer st = new StringTokenizer( s, "," );
            int x = Integer.parseInt( st.nextToken() );
            int y = Integer.parseInt( st.nextToken() );
            int width = Integer.parseInt( st.nextToken() );
            int height = Integer.parseInt( st.nextToken() );

            return new Rectangle( x, y, width, height );
        } catch (Exception ignored) {
            return DEFAULT_FRAME_BOUNDS;
        }
    }

    /**
     * Sets the frame bounds.
     * @param bounds The frame bounds to set.
     */
    public void setFrameBounds( Rectangle bounds )
    {
        String newVal;
        if (bounds == null)
        {
            newVal = "";
        }
        else
        {
            newVal = "" + bounds.x + "," + bounds.y + "," + bounds.width + "," + bounds.height;
        }
        Rectangle oldVal = getFrameBounds();
        if (oldVal != bounds &&
            ((bounds != null && !bounds.equals( oldVal )) ||
             (bounds == null && oldVal == null)))
        {
            p.put( FRAME_BOUNDS_KEY, newVal );
            pcs.firePropertyChange( "frameBounds", oldVal, bounds );
        }
    }
    
    /**
     * Sets the main frame's 'maximized' state to the given value.
     * @param maximized <code>true</code> if the main frame is maximized.
     */
    public void setFrameMaximized( boolean maximized ) {
        boolean oldVal = isFrameMaximized();
        if (oldVal == maximized) { return; }
        p.put( FRAME_MAXIMIZED_KEY, "" + maximized );
        pcs.firePropertyChange( "frameMaximized", oldVal, maximized );
    }
    
    /**
     * Gets the current 'main frame maximized' state.
     * @return <code>true</code> if main frame state is maximized.
     */
    public boolean isFrameMaximized() {
        return Boolean.TRUE.toString().equals( p.get( FRAME_MAXIMIZED_KEY ) );
    }

    /**
     * Sets the Look And Feel class name property.
     * @param name The class name.
     */
    public void setLNFClassName( String name )
    {
        String oldVal = getLNFClassName();
        if (oldVal != name && name != null && !name.equals( oldVal ))
        {
            p.put( LNF_CLASS_NAME_KEY, name );
            pcs.firePropertyChange( "lnfClassName", oldVal, name );
        }
    }

    /**
     * Gets the Look And Feel class name property.
     * @return The class name.
     */
    public String getLNFClassName()
    {
        String s = p.getProperty( LNF_CLASS_NAME_KEY );
        if (s == null) { return DEFAULT_LNF_CLASS_NAME; }
        return s;
    }

    /**
     * Sets the save directory.
     * @param saveDir The save directory.
     */
    public void setSaveDirectory( String saveDir )
    {
        String oldVal = getSaveDirectory();
        if (oldVal != saveDir && saveDir != null && !saveDir.equals( oldVal ))
        {
            p.put( SAVE_DIRECTORY_KEY, saveDir );
            pcs.firePropertyChange( "saveDirectory", oldVal, saveDir );
        }
    }

    /**
     * Gets the save directory.
     * @return The directory, represented as a string, that shall be opened
     *         per default by a save dialog..
     */
    public String getSaveDirectory()
    {
        String s = p.getProperty( SAVE_DIRECTORY_KEY );
        if (s == null) { return DEFAULT_SAVE_DIRECTORY; }
        return s;
    }

    /**
     * Sets the directory that shall be used for session storage by default.
     * @param directory The fully qualified directory.
     */
    public void setSessionDirectory( String directory )
    {
        String oldDir = getSessionDirectory();
        if (oldDir != directory && directory != null && !directory.equals( oldDir ))
        {
            p.put( SESSION_DIRECTORY_KEY, directory );
            pcs.firePropertyChange( "sessionDirectory", oldDir, directory );
        }
    }

    /**
     * Gets the directory that shall be used for session storage by default.
     * is set visible.
     * @return The fully qualified directory.
     */
    public String getSessionDirectory()
    {
        String s = p.getProperty( SESSION_DIRECTORY_KEY );
        if (s == null) { return DEFAULT_SESSION_DIRECTORY; }
        return s;
    }

    /**
     * Sets the directory that shall be used for file storage by default.
     * @param directory The fully qualified directory.
     */
    public void setFileDirectory( String directory )
    {
        String oldDir = getFileDirectory();
        if (oldDir != directory && directory != null && !directory.equals( oldDir ))
        {
            p.put( FILE_DIRECTORY_KEY, directory );
            pcs.firePropertyChange( "fileDirectory", oldDir, directory );
        }
    }

    /**
     * Gets the directory that shall be used for file storage by default.
     * is set visible.
     * @return The fully qualified directory.
     */
    public String getFileDirectory()
    {
        String s = p.getProperty( FILE_DIRECTORY_KEY );
        if (s == null) { return DEFAULT_FILE_DIRECTORY; }
        return s;
    }
    
    /**
     * Sets the current MIDI output device info.
     * @param deviceList The output device list.
     */
    public void setMidiOutputDeviceList( MidiDeviceList deviceList )
    {
        if (deviceList == null) {
            deviceList = new MidiDeviceList( new MidiDeviceDescriptor[0] );
        }
        MidiDeviceList oldList = getMidiOutputDeviceList();
        StringBuffer id = new StringBuffer();
        for (int i = 0; i < deviceList.getCount(); i++) {
            if (i > 0) {
                id.append( "\n" );
            }
            // create identifier string
            if (deviceList.getMidiDeviceDescriptor( i ).getDeviceInfo() != null)
            {
                MidiDevice.Info devInfo = deviceList.getMidiDeviceDescriptor( i ).getDeviceInfo();
                MidiDeviceId devId = new MidiDeviceId(devInfo);
                deviceList.getMidiDeviceDescriptor( i ).setId( devId );
                id.append( devId.getIdString() );
            } else {
                id.append( deviceList.getMidiDeviceDescriptor( i ).getId().getIdString() );
            }
        }
        String s = p.getProperty( MIDI_OUTPUT_DEVICE_KEY );
        if (!id.toString().equals( s )) {
            midiOutputDeviceList = deviceList;
            pcs.firePropertyChange( "midiOutputDeviceList", oldList, getMidiOutputDeviceList() );
            p.put( MIDI_OUTPUT_DEVICE_KEY, id.toString() );
        }
    }
    
    private MidiDeviceList midiOutputDeviceList = null;
    
    /**
     * Gets the current MIDI output device info.
     * @return The MIDI output device info, or an empty array if none is selected/available.
     */
    public MidiDeviceList getMidiOutputDeviceList() {
        if (midiOutputDeviceList != null) {
            return midiOutputDeviceList;
        }
        MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
        String s = p.getProperty( MIDI_OUTPUT_DEVICE_KEY );
        if (s == null) {
            midiOutputDeviceList = new MidiDeviceList( new MidiDeviceDescriptor[0] );
            return midiOutputDeviceList;
        }
        StringTokenizer st = new StringTokenizer( s, "\n" );
        ArrayList<MidiDeviceDescriptor> list = new ArrayList<MidiDeviceDescriptor>();
        while (st.hasMoreTokens()) {
            MidiDeviceId listItemId = new MidiDeviceId(st.nextToken());
            boolean b = false;
            for (int i = 0; i < info.length; i++) {
                MidiDeviceId id = new MidiDeviceId(info[i]);
                try {
                    MidiDevice dev = MidiSystem.getMidiDevice(info[i]);
                    // found!
                    if (dev.getMaxReceivers() != 0 && id.equals( listItemId )) {
                        list.add( new MidiDeviceDescriptor( info[i], id ) );
                        b = true;
                        break;
                    }
                } catch (MidiUnavailableException e) {
                    e.printStackTrace();
                }
            }
            if (!b) {
                list.add( new MidiDeviceDescriptor( null, listItemId ) );
            }
        }
        MidiDeviceDescriptor[] result = new MidiDeviceDescriptor[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get( i );
        }
        midiOutputDeviceList = new MidiDeviceList( result );
        
        return midiOutputDeviceList;
    }

    /**
     * Sets the current MIDI sequencer info.
     * @param devInfo The device's info.
     */
    public void setMidiSequencerInfo( MidiDevice.Info devInfo )
    {
        // create identifier string
        MidiDeviceId id = new MidiDeviceId(devInfo);
        
        MidiDevice.Info oldDevInfo = getMidiSequencerInfo();
        if (devInfo == oldDevInfo || oldDevInfo == null ||
            (oldDevInfo != null && !oldDevInfo.equals( devInfo )))
        {
            pcs.firePropertyChange( "midiSequencerInfo", oldDevInfo, devInfo );
            p.put( MIDI_SEQUENCER_KEY, id.getIdString() );
        }
    }
    
    /**
     * Gets the current MIDI sequencer info.
     * @return The MIDI sequencer info, or <code>null</code> if none is selected/available.
     */
    public MidiDevice.Info getMidiSequencerInfo()
    {
        MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
        String s = p.getProperty( MIDI_SEQUENCER_KEY );
        if (s == null) { return null; }
        MidiDeviceId devId = new MidiDeviceId(s);
        for (int i = 0; i < info.length; i++)
        {
            MidiDeviceId id = new MidiDeviceId(info[i]);
            // found!
            if (id.equals(devId))
            {
                return info[i];
            }
        }
        return null;
    }
    /**
     * Sets the current MIDI input device info.
     * @param deviceList The input device list.
     */
    public void setMidiInputDeviceList( MidiDeviceList deviceList ) {
        if (deviceList == null) {
            deviceList = new MidiDeviceList( new MidiDeviceDescriptor[0] );
        }
        MidiDeviceList oldList = getMidiInputDeviceList();
        StringBuffer id = new StringBuffer();
        for (int i = 0; i < deviceList.getCount(); i++) {
            if (i > 0) {
                id.append( "\n" );
            }
            // create identifier string
            if (deviceList.getMidiDeviceDescriptor( i ).getDeviceInfo() != null)
            {
                MidiDevice.Info devInfo = deviceList.getMidiDeviceDescriptor( i ).getDeviceInfo();
                MidiDeviceId deviceId = new MidiDeviceId(devInfo);
                deviceList.getMidiDeviceDescriptor( i ).setId( deviceId );
                id.append( deviceId.getIdString() );
            } else {
                id.append( deviceList.getMidiDeviceDescriptor( i ).getId().getIdString() );
            }
        }
        String s = p.getProperty( MIDI_INPUT_DEVICE_KEY );
        if (!id.toString().equals( s )) {
            midiInputDeviceList = deviceList;
            pcs.firePropertyChange( "midiInputDeviceList", oldList, getMidiInputDeviceList() );
            p.put( MIDI_INPUT_DEVICE_KEY, id.toString() );
        }
    }
    
    private MidiDeviceList midiInputDeviceList = null;
    
    /**
     * Gets the current MIDI input device info.
     * @return The MIDI input device info, or an empty array if none is selected/available.
     */
    public MidiDeviceList getMidiInputDeviceList() {
        if (midiInputDeviceList != null) {
            return midiInputDeviceList;
        }
        MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
        String s = p.getProperty( MIDI_INPUT_DEVICE_KEY );
        if (s == null) {
            midiInputDeviceList = new MidiDeviceList( new MidiDeviceDescriptor[0] );
            return midiInputDeviceList;
        }
        StringTokenizer st = new StringTokenizer( s, "\n" );
        ArrayList<MidiDeviceDescriptor> list = new ArrayList<MidiDeviceDescriptor>();
        while (st.hasMoreTokens()) {
            MidiDeviceId listItemId = new MidiDeviceId(st.nextToken());
            boolean b = false;
            for (int i = 0; i < info.length; i++) {
                MidiDeviceId id = new MidiDeviceId(info[i]);
                try {
                    MidiDevice dev = MidiSystem.getMidiDevice(info[i]);
                    // found!
                    if (dev.getMaxTransmitters() != 0 && id.equals( listItemId )) {
                        list.add( new MidiDeviceDescriptor( info[i], id ) );
                        b = true;
                        break;
                    }
                } catch (MidiUnavailableException e) {
                    e.printStackTrace();
                }
            }
            if (!b) {
                list.add( new MidiDeviceDescriptor( null, listItemId ) );
            }
        }
        MidiDeviceDescriptor[] result = new MidiDeviceDescriptor[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get( i );
        }
        midiInputDeviceList = new MidiDeviceList( result );
        
        return midiInputDeviceList;
    }

    /**
     * Sets the current MIDI click device info.
     * @param deviceList The click device list.
     */
    public void setMidiClickDeviceList( MidiDeviceList deviceList ) {
        if (deviceList == null) {
            deviceList = new MidiDeviceList( new MidiDeviceDescriptor[0] );
        }
        MidiDeviceList oldList = getMidiClickDeviceList();
        StringBuffer id = new StringBuffer();
        for (int i = 0; i < deviceList.getCount(); i++) {
            if (i > 0) {
                id.append( "\n" );
            }
            // create identifier string
            if (deviceList.getMidiDeviceDescriptor( i ).getDeviceInfo() != null)
            {
                MidiDevice.Info devInfo = deviceList.getMidiDeviceDescriptor( i ).getDeviceInfo();
                MidiDeviceId deviceId = new MidiDeviceId(devInfo);
                deviceList.getMidiDeviceDescriptor( i ).setId( deviceId );
                id.append( deviceId.getIdString() );
            } else {
                id.append( deviceList.getMidiDeviceDescriptor( i ).getId().getIdString() );
            }
        }
        String s = p.getProperty( MIDI_CLICK_DEVICE_KEY );
        if (!id.toString().equals( s )) {
            midiClickDeviceList = deviceList;
            pcs.firePropertyChange( "midiClickDeviceList", oldList, getMidiClickDeviceList() );
            p.put( MIDI_CLICK_DEVICE_KEY, id.toString() );
        }
    }
    
    private MidiDeviceList midiClickDeviceList = null;
    
    /**
     * Gets the current MIDI click device info.
     * @return The MIDI click device list, not <code>null</code>.
     */
    public MidiDeviceList getMidiClickDeviceList() {
        if (midiClickDeviceList != null) {
            return midiClickDeviceList;
        }
        MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
        String s = p.getProperty( MIDI_CLICK_DEVICE_KEY );
        if (s == null) {
            midiClickDeviceList = new MidiDeviceList( new MidiDeviceDescriptor[0] );
            return midiClickDeviceList;
        }
        StringTokenizer st = new StringTokenizer( s, "\n" );
        ArrayList<MidiDeviceDescriptor> list = new ArrayList<MidiDeviceDescriptor>();
        while (st.hasMoreTokens()) {
            MidiDeviceId listItemId = new MidiDeviceId(st.nextToken());
            boolean b = false;
            for (int i = 0; i < info.length; i++) {
                MidiDeviceId id = new MidiDeviceId(info[i]);
                try {
                    MidiDevice dev = MidiSystem.getMidiDevice(info[i]);
                    // found!
                    if (dev.getMaxReceivers() != 0 && id.equals( listItemId )) {
                        list.add( new MidiDeviceDescriptor( info[i], id ) );
                        b = true;
                    }
                } catch (MidiUnavailableException e) {
                    e.printStackTrace();
                }
            }
            if (!b) {
                list.add( new MidiDeviceDescriptor( null, listItemId ) );
            }
        }
        MidiDeviceDescriptor[] result = new MidiDeviceDescriptor[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get( i );
        }
        midiClickDeviceList = new MidiDeviceList( result );
        
        return midiClickDeviceList;
    }
    
    /**
     * Sets the audio input device list.
     * @param deviceList The audio device list to be the new device list for audio input.
     */
    public void setAudioInputDeviceList( AudioDeviceList deviceList ) {
        if (deviceList == null) {
            deviceList = new AudioDeviceList( new AudioDeviceDescriptor[0] );
        }
        AudioDeviceList oldList = getAudioInputDeviceList();
        StringBuffer id = new StringBuffer();
        for (int i = 0; i < deviceList.getCount(); i++) {
            if (i > 0) {
                id.append( "\n" );
            }
            // create identifier string
            if (deviceList.getAudioDeviceDescriptor( i ).getDeviceInfo() != null)
            {
                Mixer.Info devInfo = deviceList.getAudioDeviceDescriptor( i ).getDeviceInfo();
                String idString = devInfo.getVendor() + devInfo.getName() +
                    devInfo.getVersion() + devInfo.getDescription();
                deviceList.getAudioDeviceDescriptor( i ).setIdString( idString );
                id.append( idString );
            } else {
                id.append( deviceList.getAudioDeviceDescriptor( i ).getIdString() );
            }
        }
        String s = p.getProperty( AUDIO_INPUT_DEVICE_KEY );
        if (!id.toString().equals( s )) {
            audioInputDeviceList = deviceList;
            pcs.firePropertyChange( "audioInputDeviceList", oldList, getAudioInputDeviceList() );
            p.put( AUDIO_INPUT_DEVICE_KEY, id.toString() );
        }
    }
    
    private AudioDeviceList audioInputDeviceList;
    
    /**
     * Gets the current list of audio input devices.
     * @return An <code>AudioDeviceList</code>. Not <code>null</code>.
     */
    public AudioDeviceList getAudioInputDeviceList() {
        if (audioInputDeviceList != null) {
            return audioInputDeviceList;
        }
        Mixer.Info[] info = AudioSystem.getMixerInfo();
        String s = p.getProperty( AUDIO_INPUT_DEVICE_KEY );
        if (s == null) {
            audioInputDeviceList = new AudioDeviceList( new AudioDeviceDescriptor[0] );
            return audioInputDeviceList;
        }
        StringTokenizer st = new StringTokenizer( s, "\n" );
        ArrayList<AudioDeviceDescriptor> list = new ArrayList<AudioDeviceDescriptor>();
        while (st.hasMoreTokens()) {
            String listItemId = st.nextToken();
            boolean b = false;
            for (int i = 0; i < info.length; i++) {
                String id = info[i].getVendor() + info[i].getName() +
                       info[i].getVersion() + info[i].getDescription();
                // found!
                if (id.equals( listItemId )) {
                    list.add( new AudioDeviceDescriptor( info[i], id ) );
                    b = true;
                }
            }
            if (!b) {
                list.add( new AudioDeviceDescriptor( null, listItemId ) );
            }
        }
        AudioDeviceDescriptor[] result = new AudioDeviceDescriptor[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get( i );
        }
        audioInputDeviceList = new AudioDeviceList( result );
        return audioInputDeviceList;
    }

    /**
     * Sets the audio output device list.
     * @param deviceList The audio device list to be the new device list for audio output.
     */
    public void setAudioOutputDeviceList( AudioDeviceList deviceList ) {
        if (deviceList == null) {
            deviceList = new AudioDeviceList( new AudioDeviceDescriptor[0] );
        }
        AudioDeviceList oldList = getAudioOutputDeviceList();
        StringBuffer id = new StringBuffer();
        for (int i = 0; i < deviceList.getCount(); i++) {
            if (i > 0) {
                id.append( "\n" );
            }
            // create identifier string
            if (deviceList.getAudioDeviceDescriptor( i ).getDeviceInfo() != null)
            {
                Mixer.Info devInfo = deviceList.getAudioDeviceDescriptor( i ).getDeviceInfo();
                String idString = devInfo.getVendor() + devInfo.getName() +
                    devInfo.getVersion() + devInfo.getDescription();
                deviceList.getAudioDeviceDescriptor( i ).setIdString( idString );
                id.append( idString );
            } else {
                id.append( deviceList.getAudioDeviceDescriptor( i ).getIdString() );
            }
        }
        String s = p.getProperty( AUDIO_OUTPUT_DEVICE_KEY );
        if (!id.toString().equals( s )) {
            audioOutputDeviceList = deviceList;
            pcs.firePropertyChange( "audioOutputDeviceList", oldList, getAudioOutputDeviceList() );
            p.put( AUDIO_OUTPUT_DEVICE_KEY, id.toString() );
        }
    }
    
    private AudioDeviceList audioOutputDeviceList;
    
    /**
     * Gets the current list of audio output devices.
     * @return An <code>AudioDeviceList</code>. Not <code>null</code>.
     */
    public AudioDeviceList getAudioOutputDeviceList() {
        if (audioOutputDeviceList != null) {
            return audioOutputDeviceList;
        }
        Mixer.Info[] info = AudioSystem.getMixerInfo();
        String s = p.getProperty( AUDIO_OUTPUT_DEVICE_KEY );
        if (s == null) {
            audioOutputDeviceList = new AudioDeviceList( new AudioDeviceDescriptor[0] );
            return audioOutputDeviceList;
        }
        StringTokenizer st = new StringTokenizer( s, "\n" );
        ArrayList<AudioDeviceDescriptor> list = new ArrayList<AudioDeviceDescriptor>();
        while (st.hasMoreTokens()) {
            String listItemId = st.nextToken();
            boolean b = false;
            for (int i = 0; i < info.length; i++) {
                String id = info[i].getVendor() + info[i].getName() +
                       info[i].getVersion() + info[i].getDescription();
                // found!
                if (id.equals( listItemId )) {
                    list.add( new AudioDeviceDescriptor( info[i], id ) );
                    b = true;
                }
            }
            if (!b) {
                list.add( new AudioDeviceDescriptor( null, listItemId ) );
            }
        }
        AudioDeviceDescriptor[] result = new AudioDeviceDescriptor[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get( i );
        }
        audioOutputDeviceList = new AudioDeviceList( result );
        return audioOutputDeviceList;
    }

    /**
     * Sets the "enable record loopback per default" property.
     * @param b The property to set.
     */
    public void setDefaultRecordLoopbackEnabled( boolean b )
    {
        boolean oldVal = getDefaultRecordLoopbackEnabled();
        if (oldVal == b) { return; }
        p.put( ENABLE_RECORD_LOOPBACK_PER_DEFAULT_KEY, "" + b );
        pcs.firePropertyChange( "defaultRecordLoopbackEnabled", oldVal, b );
    }
    
    /**
     * Gets the "enable record loopback per default" property.
     * @return The property set or the default property.
     */
    public boolean getDefaultRecordLoopbackEnabled()
    {
        String s = p.getProperty( ENABLE_RECORD_LOOPBACK_PER_DEFAULT_KEY );
        if ("true".equals( s ) || "false".equals( s ))
        {
            return "true".equals( s );
        }
        return DEFAULT_ENABLE_RECORD_LOOPBACK_PER_DEFAULT;
    }


    /**
     * Sets the minimum safety time (in seconds) while recording.
     * @param seconds the number of seconds.
     */
    public void setMinimumRecordSafetyTime( int seconds )
    {
        int oldVal = getMinimumRecordSafetyTime();
        if (oldVal == seconds) { return; }
        p.put( MINIMUM_RECORD_SAFETY_SECONDS_KEY, "" + seconds );
        pcs.firePropertyChange( "minimumRecordSafetyTime", oldVal, seconds );
    }
    
    /**
     * Gets the minimum safety time (in seconds) while recording.
     * @return The time in seconds.
     */
    public int getMinimumRecordSafetyTime()
    {
        String s = p.getProperty( MINIMUM_RECORD_SAFETY_SECONDS_KEY );
        if (s == null) { return DEFAULT_MINIMUM_RECORD_SAFETY_SECONDS; }
        try
        {
            return Integer.parseInt( s );
        }
        catch (Exception ignored)
        {
        }
        return DEFAULT_MINIMUM_RECORD_SAFETY_SECONDS;
    }

    /**
     * Sets the incremental time (in seconds) while recording.
     * @param seconds the number of seconds.
     */
    public void setRecordIncrementalTime( int seconds )
    {
        int oldVal = getRecordIncrementalTime();
        if (oldVal == seconds) { return; }
        p.put( RECORD_INCREMENT_SECONDS_KEY, "" + seconds );
        pcs.firePropertyChange( "recordIncrementalTim", oldVal, seconds );
    }
    
    /**
     * Gets the incremental time (in seconds) while recording.
     * @return The time in seconds.
     */
    public int getRecordIncrementalTime()
    {
        String s = p.getProperty( RECORD_INCREMENT_SECONDS_KEY );
        if (s == null) { return DEFAULT_RECORD_INCREMENT_SECONDS; }
        try
        {
            return Integer.parseInt( s );
        }
        catch (Exception ignored)
        {
        }
        return DEFAULT_RECORD_INCREMENT_SECONDS;
    }

    /**
     * Gets the current show-tips mode.
     * @return One of SHOW_TIPS_NEVER, SHOW_TIPS_ONCE_A_DAY or SHOW_TIPS_ON_EVERY_START.
     */
    public int getShowTipsMode()
    {
        try { return Integer.parseInt( p.getProperty( SHOW_TIPS_KEY ) ); }
        catch (Exception ignored) {}
        return DEFAULT_SHOW_TIPS;
    }

    /**
     * Sets the show-tips mode.
     * @param mode One of SHOW_TIPS_NEVER, SHOW_TIPS_ONCE_A_DAY or SHOW_TIPS_ON_EVERY_START.
     */
    public void setShowTipsMode( int mode )
    {
        int old = getShowTipsMode();
        if (old != mode)
        {
            p.put( SHOW_TIPS_KEY, "" + mode );
            pcs.firePropertyChange( "showTipsMode", new Integer( old ), new Integer( mode ) );
        }
    }
    
    /**
     * Gets the current view mode.
     * @return One of VIEW_MODE_INTERNAL_FRAMES or VIEW_MODE_TABBED.
     */
    public int getViewMode()
    {
        try { return Integer.parseInt( p.getProperty( VIEW_MODE_KEY ) ); }
        catch (Exception ignored) {}
        return DEFAULT_VIEW_MODE;
    }

    /**
     * Sets the view mode.
     * @param mode One of VIEW_MODE_INTERNAL_FRAMES or VIEW_MODE_TABBED.
     */
    public void setViewMode( int mode )
    {
        int old = getViewMode();
        if (old != mode)
        {
            p.put( VIEW_MODE_KEY, "" + mode );
            pcs.firePropertyChange( "viewMode", new Integer( old ), new Integer( mode ) );
        }
    }
    
    /**
     * Applies the last application start time.
     * Call this method when the browser is about be be quit.
     * If you call the <code>saveProperties()</code> method,
     * you do not need to call this method explicitly.
     */
    public void applyLastApplicationStart()
    {
        p.setProperty( LAST_START_KEY, "" + lastStart );
    }

    /**
     * Gets the last time the browser has been started.
     * @return The the last time in millis since 1970, or -1 if never started.
     */
    public long getLastBrowserStart()
    {
        try { return Long.parseLong( p.getProperty( LAST_START_KEY ) ); }
        catch (Exception ignored) {}
        return -1;
    }

    /**
     * Gets the current tip index.
     * @return The index.
     */
    public int getTipIndex()
    {
        try { return Integer.parseInt( p.getProperty( TIPS_INDEX_KEY ) ); }
        catch (Exception ignored) {}
        return DEFAULT_TIPS_INDEX;
    }

    /**
     * Sets the tip index.
     * @param index The index.
     */
    public void setTipIndex( int index )
    {
        int old = getTipIndex();
        if (old != index)
        {
            p.put( TIPS_INDEX_KEY, "" + index );
            pcs.firePropertyChange( "tipIndex", new Integer( old ), new Integer( index ) );
        }
    }
    
    /**
     * Gets the note and channel and volume and duration values for the click.
     * @return An array, where the first element is the note and the second element is
     * the channel and the third is the volume and the fourth is the duration value.
     */
    public short[] getNoteClick() {
        try {
            String s = p.getProperty( NOTE_CLICK_KEY );
            if (s != null) {
                StringTokenizer st = new StringTokenizer( s, "," );
                short note = Short.parseShort( st.nextToken() );
                short channel = Short.parseShort( st.nextToken() );
                short volume = Short.parseShort( st.nextToken() );
                short duration = Short.parseShort( st.nextToken() );
                return new short[] { note, channel, volume, duration };
            }
        }
        catch (Exception ignored) {}
        return DEFAULT_NOTE_CLICK;
    }
    
    /**
     * Sets the note and channel and volume and duration values for the click.
     * @param note The note value.
     * @param channel The channel value.
     * @param volume The volume value (<code>0 &lt; volume &lt; 128</code>).
     * @param duration The duration (in milliseconds).
     */
    public void setNoteClick( short note, short channel, short volume, short duration ) {
        short[] oldVal = getNoteClick();
        if (oldVal[0] != note || oldVal[1] != channel || oldVal[2] != volume || oldVal[3] != duration) {
            p.put( NOTE_CLICK_KEY, "" + note + "," + channel + "," + volume + "," + duration );
            pcs.firePropertyChange( "noteClick", oldVal, new short[] { note, channel, volume, duration } );
        }
    }
    
    /**
     * Gets the note and channel and volume and duration values for the click on count 'one'.
     * @return An array, where the first element is the note and the second element is
     * the channel and the third is the volume and the fourth is the duration value.
     */
    public short[] getNoteClickOne() {
        try {
            String s = p.getProperty( NOTE_CLICK_ONE_KEY );
            if (s != null) {
                StringTokenizer st = new StringTokenizer( s, "," );
                short note = Short.parseShort( st.nextToken() );
                short channel = Short.parseShort( st.nextToken() );
                short volume = Short.parseShort( st.nextToken() );
                short duration = Short.parseShort( st.nextToken() );
                return new short[] { note, channel, volume, duration };
            }
        }
        catch (Exception ignored) {}
        return DEFAULT_NOTE_CLICK_ONE;
    }
    
    /**
     * Sets the note and channel and volume and duration values for the click on count 'one'.
     * @param note The note value.
     * @param channel The channel value.
     * @param volume The volume value (<code>0 &lt; volume &lt; 128</code>).
     * @param duration The duration (in milliseconds).
     */
    public void setNoteClickOne( short note, short channel, short volume, short duration ) {
        short[] oldVal = getNoteClickOne();
        if (oldVal[0] != note || oldVal[1] != channel || oldVal[2] != volume || oldVal[3] != duration) {
            p.put( NOTE_CLICK_ONE_KEY, "" + note + "," + channel + "," + volume + "," + duration );
            pcs.firePropertyChange( "noteClickOne", oldVal, new short[] { note, channel, volume, duration } );
        }
    }
    
    /**
     * Gets the click count per tact.
     * @return The click count.
     */
    public int getClicksPerTact() {
        try { return Integer.parseInt( p.getProperty( CLICKS_PER_TACT_KEY ) ); }
        catch (Exception ignored) {}
        return DEFAULT_CLICKS_PER_TACT;
    }

    /**
     * Sets the clicks per tact.
     * @param clicks The number of clicks per tact to set.
     */
    public void setClicksPerTact( int clicks ) {
        int old = getClicksPerTact();
        if (old != clicks)
        {
            p.put( CLICKS_PER_TACT_KEY, "" + clicks );
            pcs.firePropertyChange( "clicksPerTact", new Integer( old ), new Integer( clicks ) );
        }
    }
    
    /**
     * Gets the 'stress on 'one'' property for the click.
     * @return The property set or the default property.
     */
    public boolean isStressOnClickOne() {
        String s = p.getProperty( STRESS_ON_CLICK_ONE_KEY );
        if ("true".equals( s ) || "false".equals( s )) {
            return "true".equals( s );
        }
        return DEFAULT_STRESS_ON_CLICK_ONE;
    }
    
    /**
     * Sets the 'stress on 'one'' property for the click.
     * @param b The property to set.
     */
    public void setStressOnClickOne( boolean b ) {
        boolean oldVal = isStressOnClickOne();
        if (oldVal == b) { return; }
        p.put( STRESS_ON_CLICK_ONE_KEY, Boolean.toString( b ) );
        pcs.firePropertyChange( "stressOnClickOne", oldVal, b );
    }
    
    
    /**
     * Sets the default audio format sample rate in Hz.
     * @param samplerate The default audio format sample rate.
     */
    public void setDefaultAudioFormatSampleRate( float samplerate ) {
        float oldVal = getDefaultAudioFormatSampleRate();
        if (oldVal == samplerate) { return; }
        p.put( AUDIO_FORMAT_SAMPLE_RATE_KEY, "" + samplerate );
        pcs.firePropertyChange(
                "defaultAudioFormatSampleRate",
                new Float( oldVal ), new Float( samplerate ) );
    }
    
    /**
     * Gets the default audio format sample rate in Hz.
     * @return The default audio format sample rate.
     */
    public float getDefaultAudioFormatSampleRate() {
        String s = p.getProperty( AUDIO_FORMAT_SAMPLE_RATE_KEY );
        if (s == null) { return AUDIO_FORMAT_DEFAULT_SAMPLE_RATE; }
        try {
            return Float.parseFloat( s );
        } catch (Exception ignored) {
        }
        return AUDIO_FORMAT_DEFAULT_SAMPLE_RATE;
    }
    
    /**
     * Sets the default audio format sample size in bits (typically 16).
     * @param numOfBits The sample size in bits.
     */
    public void setDefaultAudioFormatSampleSize( int numOfBits ) {
        int oldVal = getDefaultAudioFormatSampleSize();
        if (oldVal == numOfBits) { return; }
        p.put( AUDIO_FORMAT_SAMPLE_SIZE_KEY, "" + numOfBits );
        pcs.firePropertyChange( "defaultAudioFormatSampleSize", oldVal, numOfBits );
    }
    
    /**
     * Gets the default audio format sample size in bits.
     * @return The default audio format sample size.
     */
    public int getDefaultAudioFormatSampleSize() {
        String s = p.getProperty( AUDIO_FORMAT_SAMPLE_SIZE_KEY );
        if (s == null) { return AUDIO_FORMAT_DEFAULT_SAMPLE_SIZE; }
        try {
            return Integer.parseInt( s );
        } catch (Exception ignored) {
        }
        return AUDIO_FORMAT_DEFAULT_SAMPLE_SIZE;
    }
    
    /**
     * Gets the 'mono' property for the default audio output format.
     * @return <code>true</code> if mono is used for default audio format.
     * Otherwise, stereo will be used.
     */
    public boolean isDefaultAudioFormatMono() {
        String s = p.getProperty( AUDIO_FORMAT_MONO_KEY );
        if ("true".equals( s ) || "false".equals( s )) {
            return "true".equals( s );
        }
        return AUDIO_FORMAT_DEFAULT_MONO;
    }
    
    /**
     * Sets the 'mono' property for the default audio output format.
     * @param b If <code>true</code>, mono is indicated for default audio format.
     * Otherwise, stereo will be used.
     */
    public void setDefaultAudioFormatMono( boolean b ) {
        boolean oldVal = isDefaultAudioFormatMono();
        if (oldVal == b) { return; }
        p.put( AUDIO_FORMAT_MONO_KEY, Boolean.toString( b ) );
        pcs.firePropertyChange( "defaultAudioFormatMono", oldVal, b );
    }

    /**
     * Gets the 'big endian' property for the default audio output format.
     * @return <code>true</code> if big endian is used for default audio format.
     * Otherwise, little endian will be used.
     */
    public boolean isDefaultAudioFormatBigEndian() {
        String s = p.getProperty( AUDIO_FORMAT_BIG_ENDIAN_KEY );
        if ("true".equals( s ) || "false".equals( s )) {
            return "true".equals( s );
        }
        return AUDIO_FORMAT_DEFAULT_BIG_ENDIAN;
    }
    
    /**
     * Sets the 'big endian' property for the default audio output format.
     * @param b If <code>true</code>, big endian is indicated for default audio format.
     * Otherwise, little endian will be used.
     */
    public void setDefaultAudioFormatBigEndian( boolean b ) {
        boolean oldVal = isDefaultAudioFormatBigEndian();
        if (oldVal == b) { return; }
        p.put( AUDIO_FORMAT_BIG_ENDIAN_KEY, Boolean.toString( b ) );
        pcs.firePropertyChange( "defaultAudioFormatBigEndian", oldVal, b );
    }

    /**
     * Gets the 'signed' property for the default audio output format.
     * @return <code>true</code> if signed is used for default audio format.
     * Otherwise, unsigned will be used.
     */
    public boolean isDefaultAudioFormatSigned() {
        String s = p.getProperty( AUDIO_FORMAT_SIGNED_KEY );
        if ("true".equals( s ) || "false".equals( s )) {
            return "true".equals( s );
        }
        return AUDIO_FORMAT_DEFAULT_SIGNED;
    }
    
    /**
     * Sets the 'signed' property for the default audio output format.
     * @param b If <code>true</code>, signed is indicated for default audio format.
     * Otherwise, unsigned will be used.
     */
    public void setDefaultAudioFormatSigned( boolean b ) {
        boolean oldVal = isDefaultAudioFormatSigned();
        if (oldVal == b) { return; }
        p.put( AUDIO_FORMAT_SIGNED_KEY, Boolean.toString( b ) );
        pcs.firePropertyChange( "defaultAudioFormatSigned", oldVal, b );
    }

    /**
     * Sets the audio bufer length.
     * @param bufferLength The audio buffer length in milliseconds.
     */
    public void setAudioBufferLength( int bufferLength ) {
        int oldVal = getAudioBufferLength();
        if (oldVal == bufferLength) { return; }
        p.put( AUDIO_BUFFER_LENGTH_KEY, "" + bufferLength );
        pcs.firePropertyChange( "audioBufferLength", oldVal, bufferLength );
    }
    
    /**
     * Gets the audio buffer length.
     * @return The audio buffer length in milliseconds.
     */
    public int getAudioBufferLength() {
        String s = p.getProperty( AUDIO_BUFFER_LENGTH_KEY );
        if (s == null) { return DEFAULT_AUDIO_BUFFER_LENGTH; }
        try {
            int val = Integer.parseInt( s );
            if (val >= MIN_AUDIO_BUFFER_LENGTH && val <= MAX_AUDIO_BUFFER_LENGTH) {
                return val;
            }
        } catch (Exception ignored) {
        }
        return DEFAULT_AUDIO_BUFFER_LENGTH;
    }

    
    /**
     * Gets the list of recently open session files.
     * @return The session description files as an array.
     */
    public File[] getRecentlyOpenSessionFiles()
    {
        String s = p.getProperty( SESSIONS_KEY );
        if (s == null) { return new File[0]; }
        StringTokenizer st = new StringTokenizer( s.trim(), "\n" );
        File[] f = new File[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens())
        {
            f[i++] = new File( st.nextToken() );
        }
        return f;
    }
    
    /**
     * Gets the recently active session index, or -1.
     * @return The recently active session index, or -1 if
     *         no session has been active recently.
     */
    public int getRecentlyActiveSessionIndex()
    {
        String s = p.getProperty( SESSION_INDEX_KEY );
        if (s == null) { return -1; }
        try
        {
            return Integer.parseInt( s );
        }
        catch (Exception ex)
        {
        }
        return -1;
    }
    
    /**
     * Adds a <code>Session</code> to be marked as <code>open</code>.
     * @param session The <code>Session</code> to be marked as <code>open</code>
     */
    public void addOpenSession( Session session )
    {
        if (!openSessions.contains( session ))
        {
            openSessions.add( session );
        }
    }
    
    /**
     * Adds a <code>Session</code> to be marked as <code>open</code>.
     * @param session The <code>Session</code> to be marked as <code>open</code>
     * @throws ClassCastException if the <code>Collection</code> contains non-
     * 			<code>Session</code> elements.
     */
    public void addOpenSessions( Collection<Session> sessions ) {
        for (Session session : sessions) {
            addOpenSession( session );
        }
    }
    
    /**
     * Removes a <code>Session</code> from those who are marked as <code>open</code>.
     * @param session The <code>Session</code> to be no more marked as <code>open</code>.
     */
    public void removeOpenSession( Session session ) {
        openSessions.remove( session );
    }
    
    public void setActiveSessionIndex( int activeSessionIndex ) {
        this.activeSessionIndex = activeSessionIndex;
    }

    /**
     * Stores all sessions currently marked as <code>open</code> and the active session index.
     */
    public void storeOpenSessions() {
        StringBuffer sb = new StringBuffer();
        for (Session session : openSessions) {
            sb.append( session.getDescriptorFile().getPath() + "\n" );
        }
        p.put( SESSIONS_KEY, sb.toString() );
        p.put( SESSION_INDEX_KEY, "" + activeSessionIndex );
    }
    
	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.ui.SgUiProperties#saveProperties(java.io.File)
	 */
	public void saveProperties( File f ) throws IOException {
        applyLastApplicationStart();
        FileOutputStream fout = new FileOutputStream( f );
        p.store( fout, FILE_HEADER );
        fout.close();
    }



    /**
     * Gets a plugin-defined property from the given key that contains a <code>boolean</code>.
     * @param plugin The <code>Plugin</code> that requests the property.
     * @param key The key that uniquely describes the desired property.
     * @param defaultValue The default value to return if the property could not be found
     *        or contains invalid data or data of another type.
     * @return The desired property, or <code>defaultValue</code> if not available.
     */
    public boolean getPluginProperty( Plugin plugin, String key, boolean defaultValue )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        String s = p.getProperty( k );
        if (s == null || (!s.equals( "true" ) && !s.equals( "false" )))
        {
            return defaultValue;
        }
        return s.equals( "true" );
    }

    /**
     * Sets a plugin-defined property from the given key to a given <code>boolean</code> value.
     * @param plugin The <code>Plugin</code> that requests the setting of the property.
     * @param key The key that uniquely describes the property to set.
     * @param value The value to set.
     */
    public void setPluginProperty( Plugin plugin, String key, boolean value )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        String s = p.getProperty( k );
        Boolean oldVal = null;
        if (s != null && (s.equals( "true" ) || s.equals( "false" )))
        {
            oldVal = new Boolean( s.equals( "true" ) );
        }
        if (oldVal == null || oldVal.booleanValue() != value)
        {
            p.setProperty( k, "" + value );
            pcs.firePropertyChange( k, oldVal, new Boolean( value ) );
        }
    }

    /**
     * Gets a plugin-defined property from the given key that contains an <code>int</code>.
     * @param plugin The <code>Plugin</code> that requests the property.
     * @param key The key that uniquely describes the desired property.
     * @param defaultValue The default value to return if the property could not be found
     *        or contains invalid data or data of another type.
     * @return The desired property, or <code>defaultValue</code> if not available.
     */
    public int getPluginProperty( Plugin plugin, String key, int defaultValue )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        String s = p.getProperty( k );
        try
        {
            return Integer.parseInt( s );
        }
        catch (Exception ignored) {}
        return defaultValue;
    }

    /**
     * Sets a plugin-defined property from the given key to a given <code>int</code> value.
     * @param plugin The <code>Plugin</code> that requests the setting of the property.
     * @param key The key that uniquely describes the property to set.
     * @param value The value to set.
     */
    public void setPluginProperty( Plugin plugin, String key, int value )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        String s = p.getProperty( k );
        Integer oldVal = null;
        try
        {
            oldVal = new Integer( Integer.parseInt( s ) );
        }
        catch (NumberFormatException ignored) {}
        if (oldVal == null || oldVal.intValue() != value)
        {
            p.setProperty( k, "" + value );
            pcs.firePropertyChange( k, oldVal, new Integer( value ) );
        }
    }

    /**
     * Gets a plugin-defined property from the given key that contains an <code>int</code>.
     * @param plugin The <code>Plugin</code> that requests the property.
     * @param key The key that uniquely describes the desired property.
     * @param defaultValue The default value to return if the property could not be found
     *        or contains invalid data or data of another type.
     * @return The desired property, or <code>defaultValue</code> if not available.
     */
    public short getPluginProperty( Plugin plugin, String key, short defaultValue )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        String s = p.getProperty( k );
        try
        {
            return Short.parseShort( s );
        }
        catch (Exception ignored) {}
        return defaultValue;
    }

    /**
     * Sets a plugin-defined property from the given key to a given <code>short</code> value.
     * @param plugin The <code>Plugin</code> that requests the setting of the property.
     * @param key The key that uniquely describes the property to set.
     * @param value The value to set.
     */
    public void setPluginProperty( Plugin plugin, String key, short value )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        String s = p.getProperty( k );
        Short oldVal = null;
        try
        {
            oldVal = new Short( Short.parseShort( s ) );
        }
        catch (NumberFormatException ignored) {}
        if (oldVal == null || oldVal.shortValue() != value)
        {
            p.setProperty( k, "" + value );
            pcs.firePropertyChange( k, oldVal, new Short( value ) );
        }
    }

    /**
     * Gets a plugin-defined property from the given key that contains a <code>long</code>.
     * @param plugin The <code>Plugin</code> that requests the property.
     * @param key The key that uniquely describes the desired property.
     * @param defaultValue The default value to return if the property could not be found
     *        or contains invalid data or data of another type.
     * @return The desired property, or <code>defaultValue</code> if not available.
     */
    public long getPluginProperty( Plugin plugin, String key, long defaultValue )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        String s = p.getProperty( k );
        try
        {
            return Long.parseLong( s );
        }
        catch (Exception ignored) {}
        return defaultValue;
    }

    /**
     * Sets a plugin-defined property from the given key to a given <code>long</code> value.
     * @param plugin The <code>Plugin</code> that requests the setting of the property.
     * @param key The key that uniquely describes the property to set.
     * @param value The value to set.
     */
    public void setPluginProperty( Plugin plugin, String key, long value )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        String s = p.getProperty( k );
        Long oldVal = null;
        try
        {
            oldVal = new Long( Long.parseLong( s ) );
        }
        catch (NumberFormatException ignored) {}
        if (oldVal == null || oldVal.longValue() != value)
        {
            p.setProperty( k, "" + value );
            pcs.firePropertyChange( k, oldVal, new Long( value ) );
        }
    }

    /**
     * Gets a plugin-defined property from the given key that contains a <code>String</code>.
     * @param plugin The <code>Plugin</code> that requests the property.
     * @param key The key that uniquely describes the desired property.
     * @param defaultValue The default value to return if the property could not be found
     *        or contains invalid data or data of another type.
     * @return The desired property, or <code>defaultValue</code> if not available.<br>
     *         Note: <code>null</code> can be returned if and only if  <code>defaultValue</code>
     *         is <code>null</code> <b>and</b> a value with is not available for the given key.
     */
    public String getPluginProperty( Plugin plugin, String key, String defaultValue )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        String s = p.getProperty( k );
        if (s == null)
        {
            return defaultValue;
        }
        return s;
    }

    /**
     * Sets a plugin-defined property from the given key to a given <code>long</code> value.
     * @param plugin The <code>Plugin</code> that requests the setting of the property.
     * @param key The key that uniquely describes the property to set.
     * @param value The value to set.
     */
    public void setPluginProperty( Plugin plugin, String key, String value )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        String oldVal = getPluginProperty( plugin, key, (String) null );
        if (oldVal == null || !oldVal.equals( value ))
        {
            p.setProperty( k, "" + value );
            pcs.firePropertyChange( k, oldVal, value );
        }
    }

    /**
     * Gets a plugin-defined property from the given key that contains a <code>double</code>.
     * @param plugin The <code>Plugin</code> that requests the property.
     * @param key The key that uniquely describes the desired property.
     * @param defaultValue The default value to return if the property could not be found
     *        or contains invalid data or data of another type.
     * @return The desired property, or <code>defaultValue</code> if not available.
     */
    public double getPluginProperty( Plugin plugin, String key, double defaultValue )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        String s = p.getProperty( k );
        try
        {
            return Double.parseDouble( s );
        }
        catch (Exception ignored) {}
        return defaultValue;
    }

    /**
     * Sets a plugin-defined property from the given key to a given <code>double</code> value.
     * @param plugin The <code>Plugin</code> that requests the setting of the property.
     * @param key The key that uniquely describes the property to set.
     * @param value The value to set.
     */
    public void setPluginProperty( Plugin plugin, String key, double value )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        String s = p.getProperty( k );
        Double oldVal = null;
        try
        {
            oldVal = new Double( Double.parseDouble( s ) );
        }
        catch (NumberFormatException ignored) {}
        if (oldVal == null || oldVal.doubleValue() != value)
        {
            p.setProperty( k, "" + value );
            pcs.firePropertyChange( k, oldVal, new Double( value ) );
        }
    }

    /**
     * Gets a plugin-defined property from the given key that contains a <code>Rectangle</code>.
     * @param plugin The <code>Plugin</code> that requests the property.
     * @param key The key that uniquely describes the desired property.
     * @param defaultValue The default value to return if the property could not be found
     *        or contains invalid data or data of another type.
     * @return The desired property, or <code>defaultValue</code> if not available.
     */
    public Rectangle getPluginProperty( Plugin plugin, String key, Rectangle defaultValue )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        String s = p.getProperty( k );
        Rectangle r = new Rectangle();
        try
        {
            StringTokenizer st = new StringTokenizer( s, "," );
            r.x = Integer.parseInt( st.nextToken() );
            r.y = Integer.parseInt( st.nextToken() );
            r.width = Integer.parseInt( st.nextToken() );
            r.height = Integer.parseInt( st.nextToken() );
            return r;
        }
        catch (Exception ignored) {}
        return defaultValue;
    }
    
    /**
     * Returns true if one and another are not the same and not equal
     * to each other.
     * @param one
     * @param another
     * @return
     */
    private boolean cmpValues( Object one, Object another )
    {
        if (one == another) { return false; }
        if (one != null && one.equals( another )) { return false; }
        if (another != null && another.equals( one )) { return false; }
        return true;
    }

    /**
     * Sets a plugin-defined property from the given key to a given <code>Rectangle</code> value.
     * @param plugin The <code>Plugin</code> that requests the setting of the property.
     * @param key The key that uniquely describes the property to set.
     * @param value The value to set.
     */
    public void setPluginProperty( Plugin plugin, String key, Rectangle value )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        Rectangle oldVal = null;
        try
        {
            oldVal = getPluginProperty( plugin, key, (Rectangle) null );
        }
        catch (NumberFormatException ignored) {}
        if (cmpValues( oldVal, value ))
        {
            p.setProperty(
                k, "" + value.x + "," + value.y + "," + value.width + "," + value.height );
            pcs.firePropertyChange( k, oldVal, value );
        }
    }

    /**
     * Gets a plugin-defined property from the given key that contains a <code>Color</code>.
     * @param plugin The <code>Plugin</code> that requests the property.
     * @param key The key that uniquely describes the desired property.
     * @param defaultValue The default value to return if the property could not be found
     *        or contains invalid data or data of another type.
     * @return The desired property, or <code>defaultValue</code> if not available.
     */
    public Color getPluginProperty( Plugin plugin, String key, Color defaultValue ) {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        String s = p.getProperty( k );
        try {
            StringTokenizer st = new StringTokenizer( s, "," );
            int r = Integer.parseInt( st.nextToken() );
            int g = Integer.parseInt( st.nextToken() );
            int b = Integer.parseInt( st.nextToken() );
            int a = Integer.parseInt( st.nextToken() );
            return new Color( r, g, b, a );
        }
        catch (Exception ignored) {}
        return defaultValue;
    }

    /**
     * Sets a plugin-defined property from the given key to a given <code>Color</code> value.
     * @param plugin The <code>Plugin</code> that requests the setting of the property.
     * @param key The key that uniquely describes the property to set.
     * @param value The value to set.
     */
    public void setPluginProperty( Plugin plugin, String key, Color value )
    {
        Class<? extends Plugin> c = plugin.getClass();
        String k = PLUGIN_KEY_PREFIX + c.getName() + "." + key;
        Rectangle oldVal = null;
        try {
            oldVal = getPluginProperty( plugin, key, (Rectangle) null );
        }
        catch (NumberFormatException ignored) {}
        if (cmpValues( oldVal, value )) {
            p.setProperty(
                k, "" + value.getRed() + "," + value.getGreen() +
                "," + value.getBlue() + "," + value.getAlpha() );
            pcs.firePropertyChange( k, oldVal, value );
        }
    }
}
