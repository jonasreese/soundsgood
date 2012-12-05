/*
 * Created on 06.09.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.session;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.defaults.DockableComponentWrapper;
import org.flexdock.docking.drag.effects.EffectsManager;
import org.flexdock.docking.drag.preview.GhostPreview;
import org.flexdock.docking.state.DockingState;
import org.flexdock.docking.state.PersistenceException;
import org.flexdock.view.Viewport;

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
import com.jonasreese.util.swing.DefaultPopupListener;

/**
 * <p>
 * A <code>SessionUi</code> implementation that uses the FlexDoc docking
 * framework.
 * </p>
 * @author jreese
 */
public class FlexDockSessionUi extends SessionUi implements Plugin {
    
    private static final String ROOT_PORT_PERSISTENCE_ID = "rootPort";

    private PropertyChangeListener focusPropertyListener;
    private VcImpl lastActivated;
    
    private HashMap<ViewInstance,VcImpl> viMap;
    private HashMap<String,DockingState> dockingPersistenceMap; 
    private ViewInstance activeVi;
    private Viewport rootPort;
    
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
    public FlexDockSessionUi(
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
        
        rootPort = new Viewport( ROOT_PORT_PERSISTENCE_ID );
        panel.add( rootPort );
        DockingManager.registerDockable( rootPort );

        DockingManager.setFloatingEnabled( true );
        EffectsManager.setPreview( new GhostPreview() );
        
        try {
            System.out.println( "restoreLayout() : " + DockingManager.restoreLayout( true ) );
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (PersistenceException e1) {
            e1.printStackTrace();
        }
        DockingManager.setSingleTabsAllowed( true );
        
        viMap = new HashMap<ViewInstance,VcImpl>();
        dockingPersistenceMap = new HashMap<String,DockingState>();
        activeVi = null;
    }

    public void sessionUiAdded( Session session ) {
        System.out.println( "FexDockSessionUi.sessionUiAdded()" );
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(
                focusPropertyListener );
    }
    
    public void sessionUiRemoved( Session session ) {
        System.out.println( "FexDockSessionUi.sessionUiRemoved()" );
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener(
                focusPropertyListener );
        if (lastActivated != null) {
            lastActivated.vi.close();
        }
    }
    
    /* (non-Javadoc)
     */
    public ViewInstance getActiveViewInstance() {

        return activeVi;
    }
    
    public double getInitialDividerLocation() {
        return 0.5;
    }
    

    /* (non-Javadoc)
     */
    public ViewInstance createViewInstance(
            ViewAction viewAction, Session session, SessionElementDescriptor d )
        throws ViewInstanceCreationFailedException {

        System.out.println( "createViewInstance: " + viewAction.getView().getName() );
        
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
    
    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.ui.ViewInstanceCreator#removeViewInstance(com.jonasreese.sound.sg.plugin.view.ViewInstance)
     */
    public void removeViewInstance( ViewInstance viewInstance ) {
        System.out.println( "removeViewInstance()" );
        VcImpl vc = viMap.remove( viewInstance );
        if (vc == null) { return; }

        DockingManager.undock( vc );
        DockingManager.unregisterDockable( vc );
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

        VcImpl vc = new VcImpl( vi );

        // check for persistent docking information
        String persistenceKey = vi.getView().getClass().getName() + (d != null ? "|" + d.getName() : "");
        DockingState whereToDock = dockingPersistenceMap.get( persistenceKey );

        // create and register the Dockable panel
        DockableComponentWrapper dockable = DockableComponentWrapper.create(
                vc, persistenceKey, vi.getView().getName() );
        DockingManager.registerDockable( dockable );
        if (whereToDock != null) {
            String region = (whereToDock.getRegion() != null ?
                    whereToDock.getRegion() : DockingConstants.CENTER_REGION);
            float splitRatio = (whereToDock.getSplitRatio() >= 0 ? whereToDock.getSplitRatio() : 0.5f);
            if (whereToDock.getRelativeParent() != null) {
                DockingManager.dock( dockable, whereToDock.getRelativeParent(), region, splitRatio );
            } else {
                DockingManager.dock( dockable, rootPort, region );
            }
            
        } else {
            String region = DockingConstants.CENTER_REGION;
            DockingPort port = rootPort;
            Dockable parent = null;
            if (vi.getView().getClass().getName().endsWith( "PlayerView" )) {
                region = DockingConstants.WEST_REGION;
                parent = port.getDockable( region );
            } else if (!vi.getView().isMultipleInstancePerSessionAllowed()) {
                region = DockingConstants.WEST_REGION;
                parent = port.getDockable( region );
            }
            if (parent != null) {
                DockingManager.dock( dockable, parent, region, 0.5f );
            } else {
                DockingManager.dock( dockable, rootPort, region );
            }
        }
        
        
        
        // add UI object to vc
        Object o = vi.getUiObject( vc );
        if (!(o instanceof Component)) { return null; }
        Component c = (Component) o;
        ((Container) vc).add( c );
        
        vc.updateUI();
        vc.invalidate();
        vc.validate();
        vc.repaint();
        
        return vc;
    }

    class VcImpl extends JPanel implements ViewContainer {
        
        private static final long serialVersionUID = 1;

        ViewInstance vi;
        String titleText;
        VcImpl( ViewInstance vi ) {
            super( new BorderLayout( 2, 1 ) );
            JComponent c = new JComponent() {
                private static final long serialVersionUID = 1L;
                Dimension d = new Dimension( 1, 10 );
                public Dimension getPreferredSize() { return d; }
                public Dimension getMinimumSize() { return d; }
                public Dimension getMaximumSize() { return d; }
            };
            add( c, BorderLayout.NORTH );
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
            // TODO: set port non-resizable
        }
        public void adjustToPreferredSize() {
        }
        public ViewInstance getViewInstance() {
            return vi;
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
                View view = ((VcImpl) c).vi.getView();
                s = ((VcImpl) c).getTitleText();
                if (view instanceof Icon) {
                    icon = (Icon) view;
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
