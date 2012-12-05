/*
 * Copyright (c) 2005 Jonas Reese
 * Created on 11.04.2005
 */
package com.jonasreese.sound.sg.ui.defaultui.session;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import net.eleritec.docking.Dockable;
import net.eleritec.docking.DockableComponentWrapper;
import net.eleritec.docking.DockingManager;
import net.eleritec.docking.DockingPort;
import net.eleritec.docking.defaults.DefaultDockingPort;
import net.eleritec.docking.defaults.SubComponentProvider;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.plugin.Plugin;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.plugin.view.ViewInstanceCreationFailedException;
import com.jonasreese.sound.sg.ui.ViewInstanceEvent;
import com.jonasreese.sound.sg.ui.defaultui.SessionActionPool;
import com.jonasreese.sound.sg.ui.defaultui.SessionUi;
import com.jonasreese.sound.sg.ui.defaultui.SgToolBar;
import com.jonasreese.sound.sg.ui.defaultui.action.ViewAction;
import com.jonasreese.util.ParamRunnable;
import com.jonasreese.util.swing.DefaultPopupListener;

/**
 * <p>
 * A <code>SessionUi</code> implementation that uses the FlexDoc docking
 * framework.
 * </p>
 * @author jreese
 */
public class DockingSessionUi extends SessionUi implements Plugin, SubComponentProvider {

    private PropertyChangeListener focusPropertyListener;
    private VcImpl lastActivated;
    
    private Map<ViewInstance,VcImpl> viMap;
    private ViewInstance activeVi;
    private DefaultDockingPortImpl northPort;
    private DefaultDockingPortImpl southPort;
    private DefaultDockingPortImpl eastPort;
    private DefaultDockingPortImpl westPort;
    private DefaultDockingPortImpl centerPort;
    
    private JSplitPaneImpl northCenterPane;
    private JSplitPaneImpl southCenterPane;
    private JSplitPaneImpl eastCenterPane;
    private JSplitPaneImpl westCenterPane;
    
    private Map<String,DefaultDockingPort> portMap;
    

    private static final String BOUNDS = "dockingBounds";
    private static final String DIVIDERS = "dividerPositions";
    private static final String VIEW_REGION = "viewRegion";
    
    public static final String NORTH = BorderLayout.NORTH;
    public static final String SOUTH = BorderLayout.SOUTH;
    public static final String WEST = BorderLayout.WEST;
    public static final String EAST = BorderLayout.EAST;
    public static final String CENTER = BorderLayout.CENTER;

    
    private static VcImpl getVc( Object o ) {
        if (o instanceof Component) {
            Component c = (Component) o;
            while (c != null && !(c instanceof VcImpl)) {
                c = c.getParent();
            }
            return (VcImpl) c;
        }
        return null;
    }
    
    
    /**
     * @param menuBar
     * @param toolBar
     * @param actionPool
     */
    public DockingSessionUi(
            JComponent panel, JMenuBar menuBar, SgToolBar toolBar, SessionActionPool actionPool ) {
        super( menuBar, toolBar, actionPool );

        focusPropertyListener = new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent e ) {
                if ("permanentFocusOwner".equals( e.getPropertyName() ) ) {
                    VcImpl newVal = getVc( e.getNewValue() );
                    if (lastActivated == newVal) {
                        return;
                    }
                    if (newVal != null) {
                        if (newVal != lastActivated) {
                            if (lastActivated != null && lastActivated.vi != null) {
                                lastActivated.vi.deactivate();
                            }
                            if (newVal.vi != null) {
                                newVal.vi.activate();
                                lastActivated = newVal;
                            }
                       }
                    }
                }
            }
        };
        lastActivated = null;
        
        northPort = (DefaultDockingPortImpl) createChildPort( 0, BorderLayout.NORTH );
        southPort = (DefaultDockingPortImpl) createChildPort( 0, BorderLayout.SOUTH );
        eastPort = (DefaultDockingPortImpl) createChildPort( 0, BorderLayout.EAST );
        westPort = (DefaultDockingPortImpl) createChildPort( 0, BorderLayout.WEST );
        centerPort = (DefaultDockingPortImpl) createChildPort( 0, BorderLayout.CENTER );
        
        portMap = new HashMap<String,DefaultDockingPort>();
        portMap.put( NORTH, northPort );
        portMap.put( SOUTH, southPort );
        portMap.put( EAST, eastPort );
        portMap.put( WEST, westPort );
        portMap.put( CENTER, centerPort );

        northCenterPane = createSplitPane( northPort, centerPort, true );
        southCenterPane = createSplitPane( northCenterPane, southPort, true );
        southCenterPane.setResizeWeight( 1 );
        eastCenterPane = createSplitPane( southCenterPane, eastPort, false );
        eastCenterPane.setResizeWeight( 1 );
        westCenterPane = createSplitPane( westPort, eastCenterPane, false );
        panel.add( westCenterPane );

        String[] dividers = getActionPool().getSession().getPersistentClientPropertyArray( DIVIDERS, "," );
        if (dividers == null || dividers.length != 4) {
            String s = SgEngine.getInstance().getProperties().getPluginProperty( this, DIVIDERS, (String) null );
            if (s != null) {
                StringTokenizer st = new StringTokenizer( s, "," );
                try {
                    dividers = new String[] {
                            st.nextToken(),
                            st.nextToken(),
                            st.nextToken(),
                            st.nextToken()
                    };
                } catch (Exception ignored) {
                    dividers = null;
                }
            }
        }
        if (dividers != null && dividers.length == 4) {
            try {
                SwingUtilities.invokeAndWait( new ParamRunnable( dividers ) {
                    public void run() {
                        String[] dividers = (String[]) getParameter();
                        try {
                            northCenterPane.setDividerLocation( Integer.parseInt( dividers[0] ) );
                            eastCenterPane.setDividerLocation( Integer.parseInt( dividers[1] ) );
                            southCenterPane.setDividerLocation(
                                    Integer.parseInt( dividers[2] ) - southCenterPane.getDividerLocation() );
                            westCenterPane.setDividerLocation( Integer.parseInt( dividers[3] ) );
                        } catch (NumberFormatException ignored) {
                        }
                    }
                } );
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (InvocationTargetException e1) {
                e1.printStackTrace();
            }
        }
        
        viMap = new HashMap<ViewInstance,VcImpl>();
        activeVi = null;
    }

    public void sessionUiAdded( Session session ) {
        System.out.println( "DockingSessionUi.sessionUiAdded()" );
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
                focusPropertyListener );

        final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent e ) {
                System.out.println( "old value = " + e.getOldValue() + ", new value = " + e.getNewValue() );
                String[] dividers = new String[] {
                        Integer.toString( northCenterPane.getDividerLocation() ),
                        Integer.toString( eastCenterPane.getDividerLocation() ),
                        Integer.toString( southCenterPane.getDividerLocation() + southCenterPane.getDividerSize() ),
                        Integer.toString( westCenterPane.getDividerLocation() )
                };
                getActionPool().getSession().putPersistentClientPropertyArray(
                        DIVIDERS, dividers, "," );
                SgEngine.getInstance().getProperties().setPluginProperty(
                        DockingSessionUi.this, DIVIDERS,
                        dividers[0] + "," + dividers[1] + "," + dividers[2] + "," + dividers[3] );
           }
        };

        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                String pn = "dividerLocation";
                northCenterPane.addPropertyChangeListener( pn, propertyChangeListener );
                eastCenterPane.addPropertyChangeListener( pn, propertyChangeListener );
                southCenterPane.addPropertyChangeListener( pn, propertyChangeListener );
                westCenterPane.addPropertyChangeListener( pn, propertyChangeListener );
            }
        } );

//        getMenuBar().add( uiMenu );
    }
    
    public void sessionUiRemoved( Session session ) {
        System.out.println( "DockingSessionUi.sessionUiRemoved()" );
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(
                focusPropertyListener );
        if (lastActivated != null) {
            lastActivated.vi.close();
        }
//        getMenuBar().remove( uiMenu );
    }
    
    /* (non-Javadoc)
     */
    public ViewInstance getActiveViewInstance() {

        return activeVi;
    }

    public double getInitialDividerLocation() {
        return 0.5;
    }
    
    public JTabbedPane createTabbedPane() {
        JTabbedPane tp = new JTabbedPaneImpl();
        return tp;
    }
    
    public JSplitPane createSplitPane() {
        System.out.println( "createSplitPane()" );
        return createSplitPane( null, null, true );
    }
    
    private JSplitPaneImpl createSplitPane( JComponent left, JComponent right, boolean vertical ) {
        JSplitPaneImpl p = new JSplitPaneImpl(
                vertical ? JSplitPane.VERTICAL_SPLIT : JSplitPane.HORIZONTAL_SPLIT, left, right );
        p.setDividerLocation( 0.5 );
        p.setBorder( null );
        p.setDividerSize( 4 );
        return p;
    }

    /* (non-Javadoc)
     */
    public ViewInstance createViewInstance(
            ViewAction viewAction, Session session, SessionElementDescriptor d )
        throws ViewInstanceCreationFailedException {

        // check for existing VI if is single instance VI
        if (!viewAction.getView().isMultipleInstancePerSessionAllowed()) {
            for (Iterator<ViewInstance> iter = viMap.keySet().iterator(); iter.hasNext(); ) {
                ViewInstance vi = iter.next();
                if (vi.getView() == viewAction.getView()) {
                    throw new ViewInstanceCreationFailedException(
                            "Tried to create multiple instances of single instance View: " +
                            vi.getView().getName() );
                }
            }
        }
        
        View view = viewAction.getView();
        if (!view.canHandle( d )) { return null; }
        ViewInstance vi = view.createViewInstance( session, d );
        
        VcImpl vc = createViewContainer( viewAction, vi, d );
        
        addViewInstance( vi, vc, d );
        
        vi.open();
        
        return vi;
    }
    
    private DefaultDockingPort getPortFor( String region ) {
        int index = region.indexOf( "," );
        if (index >= 0) {
            region = region.substring( 0, index ).trim();
        }
        return portMap.get( region );
    }
    
    private String getRegionFor( DockingPort port, String portRegion ) {
        for (String key : portMap.keySet()) {
            Object val = portMap.get( key );
            if (val == port) {
                return key + "," + portRegion;
            }
        }
        if (port instanceof DefaultDockingPort) {
            Component parent = ((DefaultDockingPort) port).getParent();
            if (parent instanceof JSplitPane) {
                JSplitPane sp = (JSplitPane) parent;
                boolean horizontal = (sp.getOrientation() == JSplitPane.HORIZONTAL_SPLIT);
                String pr = null;
                if (port == sp.getLeftComponent()) {
                    if (horizontal) {
                        pr = DockingPort.WEST_REGION;
                    } else {
                        pr = DockingPort.NORTH_REGION;
                    }
                } else {
                    if (horizontal) {
                        pr = DockingPort.EAST_REGION;
                    } else {
                        pr = DockingPort.SOUTH_REGION;
                    }
                }
                if (parent.getParent() instanceof DockingPort) {
                    return getRegionFor( (DockingPort) parent.getParent(), pr );
                }
            }
        }
        return null;
    }
    
    private void docked( DefaultDockingPortImpl port, Component comp, String region ) {
        if (region == null || "null".equals( region )) {
            System.out.println( "null region, correcting to " + DockingPort.CENTER_REGION );
            region = DockingPort.CENTER_REGION;
        }
        VcImpl vcImpl = (VcImpl) comp;
        vcImpl.port = port;
        View view = vcImpl.vi.getView();
        String r = getRegionFor( port, region );
        System.out.println( "docked " + view.getName() + " to region: " + r + " on level " + port.level );
        String key = VIEW_REGION + "." + view.getClass();
        if (r != null) {
            getActionPool().getSession().putPersistentClientProperty( key, r );
            SgEngine.getInstance().getProperties().setPluginProperty( this, key, r );
        }
    }
        
    public DockingPort createChildPort() {
        return createChildPort( -1, null );
    }
    
    public DockingPort createChildPort( int level, String name ) {
        DefaultDockingPort port = new DefaultDockingPortImpl( level, name );
        port.setPreferredSize(new Dimension(100, 100));
        port.setLayoutResizable( true );
        port.setDockingAllowed( true );
        port.setComponentProvider( this );
        return port;
    }

    
    private DockingPort findPort( Component parent, Component child ) {
        if (parent instanceof DockingPort && ((DockingPort) parent).hasDockedChild( child )) {
            return (DockingPort) parent;
        }

        if (parent instanceof Container) {
            Component[] children = ((Container) parent).getComponents();
            for (int i = 0; i < children.length; i++) {
                DockingPort p = findPort( children[i], child );
                if (p != null) {
                    return p;
                }
            }
        }
        return null;
    }
    
    private DockingPort findPort( DockingPort parent, Component child ) {
        if (parent.hasDockedChild( child )) {
            return parent;
        }

        return findPort( (Component) parent.getDockedComponent(), child );
    }
    
    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.ui.ViewInstanceCreator#removeViewInstance(com.jonasreese.sound.sg.plugin.view.ViewInstance)
     */
    public void removeViewInstance( ViewInstance viewInstance ) {
        System.out.println( "removeViewInstance()" );
        VcImpl vc = viMap.remove( viewInstance );
        if (vc == null) { return; }
        
        DefaultDockingPortImpl port = (DefaultDockingPortImpl) findPort( (DockingPort) vc.port, vc );
        if (port != null) {
            port.undock( vc );
        }
        vc.vi = null;
        
        if (vc == lastActivated) {
            lastActivated = null;
        }

        viewInstance.close();
        
        viewInstanceRemoved( viewInstance );
        
        fireViewInstanceRemovedEvent( new ViewInstanceEvent( this, viewInstance ) );
    }
    
    private void addViewInstance(
            ViewInstance viewInstance, VcImpl vc, SessionElementDescriptor d )
    {
        viMap.put( viewInstance, vc );

        viewInstanceCreated( viewInstance, d );
        
        fireViewInstanceAddedEvent( new ViewInstanceEvent( this, viewInstance ) );
    }
    
    /**
     * Creates a dockable for the given <code>ViewAction</code>.
     * @param viewAction The <code>Action</code> that triggered the creation
     *        of the tab.
     * @param vi The <code>ViewInstance</code>.
     * @param d The SessionElementDescriptor.
     * @return A newly created <code>ViewContainer</code>.
     */
    private VcImpl createViewContainer(
        ViewAction viewAction, ViewInstance vi, SessionElementDescriptor d ) {

        String key = VIEW_REGION + "." + vi.getView().getClass();
        String region = getActionPool().getSession().getPersistentClientProperty( key );
        DefaultDockingPortImpl port = centerPort;
        String portRegion = DockingPort.CENTER_REGION;
        if (region == null) {
            region = SgEngine.getInstance().getProperties().getPluginProperty(
                    this, key, (String) null );
        }
        if (region != null) {
            DockingPort p = getPortFor( region );
            if (p != null) {
                port = (DefaultDockingPortImpl) p;
            }
            int index = region.indexOf( "," );
            if (index >= 0) {
                portRegion = region.substring( index + 1 );
            }
        }
        VcImpl vc = new VcImpl( vi, port );
        DockableComponentWrapper dockable = DockableComponentWrapper.create(
                vc, vi.getView().getClass().getName(), true );
        dockable.setDockedLayoutResizable( true );

        // create and register the Dockable panel
        DockingManager.registerDockable( vc, "", true );

        port.dock( dockable, portRegion );

        // add UI object to vc
        Object o = vi.getUiObject( vc );
        if (!(o instanceof Component)) { return null; }
        Component c = (Component) o;
        ((Container) vc).add( c );
        
        // set default frame bounds
        Rectangle r = null;
        
        if (SgEngine.getInstance().getProperties().getRestoreViewsFromSession()) {
            // restore from session element
            if (vi.getView().isMultipleInstancePerSessionAllowed()) {
                String s = d.getPersistentClientProperty(
                    vi.getView().getClass().getName() + "." + BOUNDS );
                if (s != null) {
                    try {
                        StringTokenizer st = new StringTokenizer( s, "," );
                        r = new Rectangle(
                            Integer.parseInt( st.nextToken() ),
                            Integer.parseInt( st.nextToken() ),
                            Integer.parseInt( st.nextToken() ),
                            Integer.parseInt( st.nextToken() ) );
                    } catch (Exception ignored) {}
                }
            } else { // restore from session
                String s = getActionPool().getSession().getPersistentClientProperty(
                    vi.getView().getClass().getName() + "." + BOUNDS );
                if (s != null) {
                    try {
                        StringTokenizer st = new StringTokenizer( s, "," );
                        r = new Rectangle(
                            Integer.parseInt( st.nextToken() ),
                            Integer.parseInt( st.nextToken() ),
                            Integer.parseInt( st.nextToken() ),
                            Integer.parseInt( st.nextToken() ) );
                    }
                    catch (Exception ignored) {}
                }
            }
        }
        
        if (r != null) {
            // do something
        }

        vc.updateUI();
        vc.invalidate();
        vc.validate();
        vc.repaint();

        northCenterPane.revalidate();
        northCenterPane.repaint();
        eastCenterPane.revalidate();
        eastCenterPane.repaint();
        westCenterPane.revalidate();
        westCenterPane.repaint();
        southCenterPane.revalidate();
        southCenterPane.repaint();

        return vc;
    }

    class VcImpl extends JPanel implements ViewContainer {
        
        private static final long serialVersionUID = 1;

        ViewInstance vi;
        DefaultDockingPortImpl port;
        String titleText;
        VcImpl(ViewInstance vi, DefaultDockingPortImpl port) {
            super( new BorderLayout( 2, 1 ) );
            JComponent c = new JComponent() {
                private static final long serialVersionUID = 1L;
                Dimension d = new Dimension( 1, 10 );
                public Dimension getPreferredSize() { return d; }
                public Dimension getMinimumSize() { return d; }
                public Dimension getMaximumSize() { return d; }
            };
            add( c, BorderLayout.NORTH );
            this.port = port;
            this.vi = vi;
        }
        public String getTitleText() {
            return titleText;
        }
        public void setTitleText(String titleText) {
            this.titleText = titleText;
            Component parent = getParent();
            if (parent instanceof JTabbedPaneImpl) {
                ((JTabbedPaneImpl) parent).titleUpdate( this );
            }
        }
        public void setHasFixedSize(boolean fixedSize) {
            port.setLayoutResizable( !fixedSize );
        }
        public void adjustToPreferredSize() {
        }
        public ViewInstance getViewInstance() {
            return vi;
        }
        public void addNotify() {
            super.addNotify();
            // component shall never be docked to a port directly,
            // there is always a TabbedPane inbetween
            if (getParent() instanceof DefaultDockingPortImpl) {
                DefaultDockingPortImpl port = (DefaultDockingPortImpl) getParent();
                JTabbedPane tp = createTabbedPane();
                port.remove( this );
                tp.addTab( null, this );
                port.add( tp );
            }
        }
    }

    public String getName() {
        return "Docking UI";
    }

    public String getShortDescription() {
        return "";
    }

    public String getDescription() {
        return "";
    }

    public String getPluginName() {
        return "DockingSessionUi";
    }

    public String getPluginVersion() {
        return "1.0";
    }

    public String getPluginVendor() {
        return "";
    }

    public void init() {
    }

    public void exit() {
    }
    
    
    class JTabbedPaneImpl extends JTabbedPane {
        private static final long serialVersionUID = 1L;
        JTabbedPaneImpl() {
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(
                    new AbstractAction(
                            SgEngine.getInstance().getResourceBundle().getString(
                                    "sessionui.docking.closeTab" )) {
                private static final long serialVersionUID = 1L;
                public void actionPerformed( ActionEvent e ) {
                    Component c = getSelectedComponent();
                    if (c instanceof VcImpl) {
                        removeViewInstance( ((VcImpl) c).vi );
                    } else {
                        System.out.println( "closeTab: " + c );
                    }
                }
            } );
            popupMenu.addSeparator();
            popupMenu.add(
                    new AbstractAction(
                            SgEngine.getInstance().getResourceBundle().getString(
                                    "sessionui.docking.closeAllTabs" )) {
                private static final long serialVersionUID = 1L;
                public void actionPerformed( ActionEvent e ) {
                    Component[] c = getComponents();
                    for (int i = 0; i < c.length; i++) {
                        if (c[i] instanceof VcImpl) {
                            removeViewInstance( ((VcImpl) c[i]).vi );
                        } else {
                            System.out.println( "closeTab: " + c );
                        }
                    }
                }
            } );
            addMouseListener( new DefaultPopupListener( popupMenu ) );
        }
        public void addTab( String s, Component c ) {
            Icon icon = null;
            if (c instanceof VcImpl) {
                VcImpl vcImpl = (VcImpl) c;
                if (vcImpl.vi != null) {
                    View view = vcImpl.vi.getView();
                    s = ((VcImpl) c).getTitleText();
                    if (view instanceof Icon) {
                        icon = (Icon) view;
                    }
                }
            }
            if ("null".equals( s )) {
                s = "*";
            }
            if (c instanceof JTabbedPaneImpl) {
                JTabbedPaneImpl tp = (JTabbedPaneImpl) c;
                for (int i = 0; i < tp.getTabCount(); i++) {
                    addTab( null, tp.getComponentAt( i ) );
                }
            } else {
                super.addTab( s, icon, c );
            }
        }
        public void addTab( String s, Icon icon, Component c, String arg3 ) {
            addTab( s, c );
        }
        public void addTab( String s, Icon icon, Component c ) {
            addTab( s, c );
        }
        void titleUpdate( VcImpl vc ) {
            for (int i = 0; i < getComponentCount(); i++) {
                if (getComponentAt( i ) == vc) {
                    setTitleAt( i, vc.getTitleText() );
                }
            }
        }
    }
    
    class DefaultDockingPortImpl extends DefaultDockingPort {
        private static final long serialVersionUID = 1L;
        
        int level;
        String name;
        Map<String,JTabbedPane> map;
        String region;
        String desc;
        boolean resizable;
        DefaultDockingPortImpl( int level, String name ) {
            this.level = level;
            this.name = name;
            map = new HashMap<String,JTabbedPane>();
            region = null;
            desc = null;
            resizable = false;
        }
        
        public void addNotify() {
            super.addNotify();
            
            if (level == 0) {
                return;
            }
            int count = 0;
            Component c = getParent();
            while (c != null) {
                if (c instanceof DefaultDockingPortImpl) {
                    count++;
                }
                c = c.getParent();
            }
            level = count;
        }
        
        DefaultDockingPortImpl getFirstLevel() {
            Component c = getParent();
            while (c != null) {
                if (c instanceof DefaultDockingPortImpl) {
                    if (((DefaultDockingPortImpl) c).level == 0) {
                        return (DefaultDockingPortImpl) c;
                    }
                }
                c = c.getParent();
            }
            return null;
        }
        
        public boolean dock( Component comp, String desc, String region, boolean resizable ) {
            docked( this, comp, region );
            JTabbedPane tp = map.get( region );
            boolean created = false;
            if (tp == null) {
                tp = createTabbedPane();
                created = true;
            }
            final Component _comp = comp;
            final JTabbedPane _tp = tp;
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    _tp.addTab( null, _comp );
                    _tp.setSelectedComponent( _comp );
                }
            } );
            
            comp = tp;
            boolean success = super.dock( comp, desc, region, resizable );
            
            if (created && success) {
                System.out.println(
                        "Mapping region " + region + " for port " + name + " to TabbedPane " + tp.hashCode() );
                map.put( region, tp );
            }
            
            return success;
        }

        public boolean dock( Dockable dockable, String region ) {
            return dock(
                    dockable.getDockable(),
                    dockable.getDockableDesc(),
                    region,
                    dockable.isDockedLayoutResizable() );
        }
        
        public boolean undock( Component c ) {
            if (!(c instanceof VcImpl)) {
                System.out.println( "undock called with " + c.getClass().getName() );
                return false;
            }
            final VcImpl vc = (VcImpl) c;
            JTabbedPaneImpl tp = null;
            if (vc.getParent() instanceof JTabbedPaneImpl) {
                tp = (JTabbedPaneImpl) vc.getParent();
            }
            
            boolean result;
            
            Component comp = null;
            if (tp != null) {
                if (tp.getTabCount() == 1) {
                    comp = tp.getParent();
                    result = super.undock( tp );
                    if (result) {
                        String reg = null;
                        for (Entry<String,JTabbedPane> entry : map.entrySet()) {
                            if (entry.getValue() == tp) {
                                reg = entry.getKey();
                                break;
                            }
                        }
                        if (reg != null) {
                            map.remove( reg );
                            System.out.println(
                                    "Unmapping region " + reg +
                                    " for port " + name + " from TabbedPane " + tp.hashCode() );
                        }
                    }
                } else {
                    if (!hasDockedChild( vc )) {
                        System.out.println( "Houston, we have a problem!" );
                    }
                    
                    comp = tp.getParent();
                    result = super.undock( vc );
                }
            } else {
                comp = vc.getParent();
                ((Container) comp).remove( vc );
                result = super.undock( vc );
                System.out.println( "Error: No TabbedPane found!" );
            }
            if (tp != null) {
                tp.remove( vc );
            }
            if (comp != null) {
                comp.repaint();
            }
            
            region = null;
            desc = null;

            System.out.println( "undock result is " + result );
            return result;
        }
    }
    
    class JSplitPaneImpl extends JSplitPane {
        private static final long serialVersionUID = 1L;

        public JSplitPaneImpl( int orientation, Component left, Component right ) {
            super( orientation, left, right );
        }
    }

    public PluginConfigurator getPluginConfigurator() {
        return null;
    }
}
