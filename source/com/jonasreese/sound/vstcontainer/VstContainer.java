/*
 * Created on 19.10.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.vstcontainer;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <p>
 * Please note that this class is <b>not</b> thread safe! External synchronization
 * or single-threaded access required.
 * </p>
 * @author jonas.reese
 */
public class VstContainer {
    private static final String NATIVE_LIBRARY_NAME = "vstcontainer";

    private static VstContainer instance = null;

    private VstPlugin23Configuration configuration;
    private boolean available = false;
    private boolean lazy;
    private List<File> pathList;
    private VstNodeImpl root;
    private String userDefinedNativeLibPath;
    private String initFailedMessage;

    private ArrayList<VstPluginDescriptor> plugins;
    private HashMap<Long,WeakReference<VstPlugin>> pluginsMap;
    
    /**
     * Gets the singleton <code>VSTContainer</code> instance.
     * @return The singleton instance of <code>VSTContainer</code>.
     */
    public static VstContainer getInstance() {
        if (instance == null) {
            instance = new VstContainer();
        }
        return instance;
    }
    
    /**
     * Sets the <code>VstPlugin23Configuration</code> for all plugins contained
     * within this <code>VstContainer</code>.
     * @param configuration The configuration to set. If <code>null</code>, the
     * default configuration will be used.
     */
    public void setConfiguration( VstPlugin23Configuration configuration ) {
        if (this.configuration == configuration) {
            return;
        }
        this.configuration = configuration;
        VstPlugin.setConfiguration( getConfiguration() );
    }
    
    /**
     * Sets the tempo in BPM.
     * @param bpm The tempo.
     */
    public void setTempo( float bpm ) {
        if (isVstContainerAvailable()) {
            VstPlugin.setTempo( bpm );
        }
    }
    
    /**
     * Gets the current tempo in BPM.
     * @return The current tempo.
     */
    public float getTempo() {
        if (isVstContainerAvailable()) {
            return VstPlugin.getTempo();
        }
        return 120;
    }
    
    /**
     * Gets the current <code>VstPlugin23Configuraiton</code> for all plugins
     * contained within this <code>VstContainer</code>.
     * @return The current configuration.
     */
    public VstPlugin23Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new VstPlugin23Configuration() {
                public int canHostDo( String feature ) {
                    return 0;
                }
                public VstUiHandler getUiHandler() {
                    return null;
                }
                public int getBlockSize() {
                    return 2;
                }
                public String getLogBasePath() {
                    return null;
                }
                public String getLogFileName() {
                    return null;
                }
                public int getMasterVersion() {
                    return 2300;
                }
                public String getProductName() {
                    return "JR VST Host";
                }
                public int getProductVersion() {
                    return 1;
                }
                public float getSampleRate() {
                    return 44100;
                }
                public String getVendor() {
                    return "Jonas Reese";
                }
                public void setParameterAutomated( int index, float value ) {
                }
            };
            setConfiguration( configuration );
        }
        
        return configuration;
    }
    
    private VstContainer() {
        plugins = null;
        pluginsMap = new HashMap<Long,WeakReference<VstPlugin>>();
        pathList = new ArrayList<File>();
        userDefinedNativeLibPath = null; // default
        lazy = true;
        root = new VstNodeImpl( null, null, null, null, null );
        initFailedMessage = null;
    }

    private void init() {
        if (available) {
            return;
        }
        try {
            if (userDefinedNativeLibPath != null) {
                System.load(
                        new File(
                                userDefinedNativeLibPath,
                                System.mapLibraryName( NATIVE_LIBRARY_NAME ) ).getAbsolutePath() );
                //System.out.println( "user defined VST library: " + new File(
                //        userDefinedNativeLibPath,
                //        System.mapLibraryName( NATIVE_LIBRARY_NAME ) ).getAbsolutePath() );
            } else {
                System.loadLibrary( NATIVE_LIBRARY_NAME );
            }
            available = true;
        } catch (Throwable t) {
            if (t.getMessage() != null) {
                initFailedMessage = t.getMessage();
            }
            available = false;
        }
    }
    
    /**
     * Sets the VST search path(s). Multiple paths are separated by <code>File.pathSeparator</code>.
     * <br><br>Please note that this method affects the
     * VST tree, so you need to call <code>getVstRoot</code> after calling this method.
     * @param paths The path string.
     * @throws IOException if an I/O error ocurred.
     * @throws VstPluginNotAvailableException If a VST plugin is not available.
     */
    public void setVstPaths( String paths ) throws IOException, VstPluginNotAvailableException {
        List<File> newPathList = new ArrayList<File>();
        StringTokenizer st = new StringTokenizer( paths, File.pathSeparator );
        while (st.hasMoreTokens()) {
            File f = new File( st.nextToken() );
            if (!pathList.contains( f )) {
                addVstPath( f );
            }
            newPathList.add( f );
        }
        for (Iterator<File> iter = pathList.iterator(); iter.hasNext(); ) {
            File f = (File) iter.next();
            if (!newPathList.contains( f )) {
                removeVstPath( f );
            }
        }
    }

    /**
     * This method can be used to define a custom location for the native library that is
     * required for VST operation. This method shall be called (if it is called) before
     * the method <code>getVstRoot()</code> is called the first time.
     * @param userDefinedNativeLibPath The user defined path where to find the native library.
     * If <code>null</code>, the system default paths will be used.
     */
    public void setUserDefinedNativeLibraryPath( String userDefinedNativeLibPath ) {
        this.userDefinedNativeLibPath = userDefinedNativeLibPath;
    }

    /**
     * Returns the currently set user defined native lib search path.
     * @return The user defined path where to find the native library, or <code>null</code>
     * if the system default paths are used.
     */
    public String getUserDefinedNativeLibraryPath() {
        return userDefinedNativeLibPath;
    }

    /**
     * Enables/disables the 'lazy' mode for VST plugins.
     * @param lazy If <code>true</code>, indicates that plugins shall be loaded and initialized
     * on first method call. Otherwise, they will be loaded and initialized when this
     * <code>VstContainer</code> is started (by calling the <code>getVstRoot()</code> method.
     */
    public void setLazy( boolean lazy ) {
        this.lazy = lazy;
    }
    
    /**
     * Gets the currently set 'lazy' flag.
     * @return <code>true</code> if lazy mode is enabled for VST plugins, <code>false</code>
     * otherwise.
     */
    public boolean isLazy() {
        return lazy;
    }
    
    /**
     * Gets all VST paths that are currently mounted.
     * @return All mounted VST paths.
     */
    public File[] getVstPaths() {
        File[] result = new File[pathList.size()];
        pathList.toArray( result );
        return result;
    }
    
    /**
     * Mounts a VST plugin search path.
     * <br><br>Please note that this method affects the
     * VST tree, so you need to call <code>getVstRoot</code> after calling this method.
     * @param path The path to be added.
     * @throws IOException If an I/O error ocurred.
     */
    public void addVstPath( File path ) throws IOException, VstPluginNotAvailableException {
        path = path.getAbsoluteFile();
        //System.out.println( "addVstPath( " + path + " )" );
        
        addToNode( path, path, root );
    }
    
    private void addToNode( File rootPath, File path, VstNodeImpl node )
            throws IOException, VstPluginNotAvailableException {
        plugins = null; // indicate that new flattened list has to be created next time

        File[] children = path.listFiles();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                if (children[i].isFile()) { // possibly a plugin
                    if (isVstFile( children[i] )) {
                        node.addPluginDescriptor( new VstPluginDescriptor( children[i], node, lazy ) );
                    }
                } else { // subdirectory
                    VstNodeImpl newNode = new VstNodeImpl( null, node, rootPath, children[i], null );
                    node.addChild( newNode );
                    addToNode( rootPath, children[i], newNode );
                }
            }
        }
    }
    
    private boolean isVstFile( File file ) {
        // TODO: implement this correctly
        return file.getName().endsWith( ".dll" );
    }
    
    /**
     * Unmounts a VST plugin search path.
     * <br><br>Please note that this method affects the
     * VST tree, so you need to call <code>getVstRoot</code> after calling this method.
     * @param path The path to be removed.
     */
    public void removeVstPath( File path ) {
        path = path.getAbsoluteFile();
        //System.out.println( "removeVstPath( " + path + " )" );
    }
    
    /**
     * Gets the availability state of the <code>VstContainer</code>.
     * @return <code>true</code> if the VST container is available, <code>false</code> otherwise.
     */
    public boolean isVstContainerAvailable() {
        init();
        return available;
    }
    
    /**
     * Retrieves a potential error message that occurred during VST initialization.
     * @return The error message, or <code>null</code> if none is available.
     */
    public String getInitFailedMessage() {
        return initFailedMessage;
    }
    
    /**
     * Gets the VST tree root.
     * @return
     */
    public VstNode getVstRoot() {
        init();
        return root;
    }
    
    // helper method for method getAllVstPlugins: recursive call
    private void addToList( ArrayList<VstPluginDescriptor> plugins, VstNode node ) {
        if (node.containsPlugins()) {
            VstPluginDescriptor[] p = node.getPluginDescriptors();
            for (int i = 0; i < p.length; i++) {
                plugins.add( p[i] );
            }
            VstNode[] children = node.getChildren();
            for (int i = 0; i < children.length; i++) {
                addToList( plugins, children[i] );
            }
        }
    }
    
    // helper method that creates a flattened list of all plugins
    private void createFlattenedList() {
        if (plugins == null) {
            plugins = new ArrayList<VstPluginDescriptor>();
            addToList( plugins, getVstRoot() );
        }
    }
    
    // this method shall be called by a VstPlugin when it has been initialized
    void pluginInitialized( VstPlugin plugin ) {
        pluginsMap.put( plugin.getId(), new WeakReference<VstPlugin>( plugin ) );
        // clean up pluginsMap (remove garbage collected values)
        for (Long key : pluginsMap.keySet()) {
            WeakReference<VstPlugin> ref = pluginsMap.get( key );
            if (ref == null || ref.get() == null) {
                pluginsMap.remove( key );
            }
        }
    }
    
    /**
     * Gets a flattened array of all available <code>VstPluginDescriptor</code> objects.
     * @return An array. Can be empty, but not <code>null</code>.
     */
    public VstPluginDescriptor[] getAllVstPluginDescriptors() {
        createFlattenedList();
        VstPluginDescriptor[] result = new VstPluginDescriptor[plugins.size()];
        plugins.toArray( result );
        return result;
    }
    
    /**
     * Retrieves a <code>VstPluginDescriptor</code> by it's name.
     * @param name The name. Shall not be <code>null</code>.
     * @return A <code>VstPlugin</code>, or <code>null</code> if none with the
     * given name was found. If more than one plugin with the given name exists,
     * the first one found on the search path is returned.
     */
    public VstPluginDescriptor getVstPluginDescriptorByName( String name ) {
        if (name == null) {
            return null;
        }
        VstPluginDescriptor[] plugs = getAllVstPluginDescriptors();
        for (int i = 0; i < plugs.length; i++) {
            if (name.equals( plugs[i].getName() )) {
                return plugs[i];
            }
        }
        return null;
    }
    
    // called from the native side (or from anywhere else) when VstEvents
    // shall be processed
    static int processEventsCallback( long pluginId, VstEvent[] events ) {
        VstContainer vc = getInstance();
        VstPlugin plugin = vc.pluginsMap.get( pluginId ).get();
        if (plugin != null) {
            plugin.processEventsCallback( events );
        }
        return 0;
    }

    static class VstNodeImpl implements VstNode {
        
        private List<VstNode> children;
        private VstNode parent;
        private File rootPath;
        private File path;
        private List<VstPluginDescriptor> plugins;
        
        VstNodeImpl(
                VstNode[] children,
                VstNode parent,
                File rootPath,
                File path,
                VstPluginDescriptor[] plugins ) {
            this.children = new ArrayList<VstNode>();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    this.children.add( children[i] );
                }
            }
            this.parent = parent;
            this.rootPath = rootPath;
            this.path = path;
            this.plugins = new ArrayList<VstPluginDescriptor>();
            if (plugins != null) {
                for (int i = 0; i < plugins.length; i++) {
                    this.plugins.add( plugins[i] );
                }
            }
        }
        
        void addChild( VstNode child ) {
            children.add( child );
        }
        
        void removeChild( VstNode child ) {
            children.remove( child );
        }
        
        void addPluginDescriptor( VstPluginDescriptor plugin ) {
            plugins.add( plugin );
        }
        
        void removePlugin( VstPlugin plugin ) {
            plugins.remove( plugin );
        }
        
        public VstNode[] getChildren() {
            VstNode[] children = new VstNode[this.children.size()];
            this.children.toArray( children );
            return children;
        }

        public VstNode getParent() {
            return parent;
        }

        public File getRootPath() {
            return rootPath;
        }

        public File getPath() {
            return path;
        }

        public String getName() {
            if (path == null) {
                return null;
            }
            return path.getName();
        }

        public VstPluginDescriptor[] getPluginDescriptors() {
            VstPluginDescriptor[] plugins = new VstPluginDescriptor[this.plugins.size()];
            this.plugins.toArray( plugins );
            return plugins;
        }
        
        public boolean equals( VstNode another ) {
            return (another != null && another.getRootPath().equals( rootPath ));
        }

        public boolean containsPlugins() {
            if (!plugins.isEmpty()) {
                return true;
            }
            for (Iterator<VstNode> iter = children.iterator(); iter.hasNext(); ) {
                VstNode child = iter.next();
                if (child.containsPlugins()) {
                    return true;
                }
            }
            return false;
        }
    }
}
