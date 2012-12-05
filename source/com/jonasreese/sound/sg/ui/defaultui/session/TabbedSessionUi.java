/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 23.10.2004
 */
package com.jonasreese.sound.sg.ui.defaultui.session;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SessionEvent;
import com.jonasreese.sound.sg.SessionListener;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.SgProperties;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.plugin.view.ViewInstanceCreationFailedException;
import com.jonasreese.sound.sg.ui.ViewInstanceEvent;
import com.jonasreese.sound.sg.ui.defaultui.SessionActionPool;
import com.jonasreese.sound.sg.ui.defaultui.SessionUi;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.sound.sg.ui.defaultui.SgToolBar;
import com.jonasreese.sound.sg.ui.defaultui.action.ViewAction;

/**
 * <b>
 * This class keeps UI-specific information related to a
 * given <code>Session</code>. 
 * </b>
 * @author jreese
 */
public class TabbedSessionUi extends SessionUi implements SessionListener
{
    private HashMap<ViewInstance,Object[]> viMap;
    private ViewInstance activeVi;
    private JTabbedPane tabbedPane;
    private int selectedTab;
    
    private static final String BOUNDS = "frameBounds";
    
    /**
     * Constructs a new <code>TabbedSessionUi</code>.
     * @param panel
     * @param menuBar
     * @param toolBar A <code>SgToolBar</code> representing the tool bar.
     * @param actionPool
     */
    public TabbedSessionUi(
        JComponent panel, JMenuBar menuBar, SgToolBar toolBar, SessionActionPool actionPool )
    {
        super( menuBar, toolBar, actionPool );
        tabbedPane = new JTabbedPane();
        panel.add( tabbedPane );
        viMap = new HashMap<ViewInstance,Object[]>();
        activeVi = null;
        selectedTab = -1;
        getSession().addSessionListener( this );
        tabbedPane.addChangeListener( new ChangeListener()
        {
            public void stateChanged( ChangeEvent e )
            {
                if (selectedTab != tabbedPane.getSelectedIndex())
                {
                    if (selectedTab >= 0)
                    {
                        System.out.println( "deactivating viewInstance at index " + selectedTab );
                        viewInstanceDeactivated(
                                ((ViewTab) tabbedPane.getComponentAt( selectedTab )).getViewInstance() );
                    }
                    if (tabbedPane.getSelectedIndex() >= 0)
                    {
                        System.out.println( "activating viewInstance at index " + tabbedPane.getSelectedIndex() );
                        viewInstanceActivated(
                                ((ViewTab) tabbedPane.getSelectedComponent()).getViewInstance() );
                    }
                    selectedTab = tabbedPane.getSelectedIndex();
                }
            }
        } );
    }
    
    /**
     * @return Returns the tabbedPane.
     */
    public JTabbedPane getTabbedPane()
    {
        return tabbedPane;
    }
    
    /**
     * Gets the currently active <code>ViewInstance</code>.
     * @return The current active <code>ViewInstance</code>,
     *         or <code>null</code> if none is currently active.
     */
    public ViewInstance getActiveViewInstance()
    {
        return activeVi;
    }

    /**
     * Creates a tab for the given <code>ViewAction</code>.
     * @param viewAction The <code>Action</code> that triggered the creation
     *        of the tab.
     * @param vi The <code>ViewInstance</code>.
     * @param data The vi data.
     * @return A newly created <code>ViewTab</code>.
     */
    private ViewContainer createViewContainer(
        ViewAction viewAction, ViewInstance vi, Object data )
    {
        ViewContainer vc;
        
        if (vi.getView().isMultipleInstancePerSessionAllowed())
        {
            ViewTab tab = new ViewTab(
                this, viewAction.getView(), vi );
            vc = tab;
            tab.addFocusListener( new ViewActionFocusListener( viewAction ) );
        }
        else
        {
            ViewFrame frame = new ViewFrame( this, viewAction.getView(), vi );
            vc = frame;
            frame.addWindowListener( new WindowListener()
            {
                public void windowOpened( WindowEvent e )
                {
                    //ViewFrame f = (ViewFrame) e.getSource();
                    //f.getSessionUi().addViewInstance( f.getView(), f );
                }
                public void windowClosed( WindowEvent e )
                {
                    ViewFrame f = (ViewFrame) e.getSource();
                    ((TabbedSessionUi) f.getSessionUi()).removeViewInstance(
                        f.getViewInstance() );
                }
                public void windowClosing( WindowEvent e ) {}
                public void windowIconified( WindowEvent e ) {}
                public void windowDeiconified( WindowEvent e ) {}
                public void windowActivated( WindowEvent e )
                {
                    viewInstanceActivated(
                        ((ViewContainer) e.getSource()).getViewInstance() );
                }
                public void windowDeactivated( WindowEvent e )
                {
                    viewInstanceDeactivated(
                        ((ViewContainer) e.getSource()).getViewInstance() );
                }
            } );
        }

        // add UI object to vc
        Object o = vi.getUiObject( vc );
        if (!(o instanceof Component)) { return null; }
        Component c = (Component) o;
        ((Container) vc).add( c );

        // set default frame bounds
        Rectangle r = null;
        
        if (SgEngine.getInstance().getProperties().getRestoreViewsFromSession())
        {
            // restore from session element
            if (vi.getView().isMultipleInstancePerSessionAllowed())
            {
                if (data instanceof SessionElementDescriptor)
                {
                    String s = ((SessionElementDescriptor) data).getPersistentClientProperty(
                        vi.getView().getClass().getName() + "." + BOUNDS );
                    if (s != null)
                    {
                        try
                        {
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
            // restore from session
            else
            {
                String s = getActionPool().getSession().getPersistentClientProperty(
                    vi.getView().getClass().getName() + "." + BOUNDS );
                if (s != null)
                {
                    try
                    {
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
        
        if (vc instanceof ViewFrame)
        {
            ViewFrame frame = (ViewFrame) vc;
            if (r == null)
            {
                r =  SgEngine.getInstance().getProperties().getPluginProperty(
                    viewAction.getView(), BOUNDS, (Rectangle) null );
            }
            if (r != null)
            {
                if (!vi.isSetBoundsAllowed())
                {
                    frame.setLocation( r.x, r.y );
                    frame.pack();
                }
                else
                {
                    frame.setBounds( r );
                    frame.setPreferredSize( new Dimension( r.width, r.height ) );
                }
            }
            else
            {
                frame.pack();
            }
    
            // add component listener to store frame bounds to
            // application properties and to session
            ((Component) vc).addComponentListener( new ComponentAdapter()
            {
                public void componentMoved( ComponentEvent e )
                {
                    componentResized( e );
                }
                public void componentResized( ComponentEvent e )
                {
                    ViewContainer vc = (ViewContainer) e.getSource();
                    Rectangle r = e.getComponent().getBounds();
                    SgProperties p = SgEngine.getInstance().getProperties();
                    // save frame bounds to properties to keep default value
                    p.setPluginProperty( vc.getViewInstance().getView(), BOUNDS, r );
                    // check if bounds shall also be saved to session or to
                    // session element (view instance data)
                    if (p.getRestoreViewsFromSession())
                    {
                        // store to session element
                        if (vc.getViewInstance().getView().isMultipleInstancePerSessionAllowed())
                        {
                            Object[] o = viMap.get( vc.getViewInstance() );
                            Object data = o[1];
                            if (data instanceof SessionElementDescriptor)
                            {
                                ((SessionElementDescriptor) data).putPersistentClientProperty(
                                    vc.getViewInstance().getView().getClass().getName() + "." + BOUNDS,
                                    "" + r.x + "," + r.y + "," + r.width + "," + r.height );
                            }
                        }
                        // store to session
                        else
                        {
                            Session session = getActionPool().getSession();
                            session.putPersistentClientProperty(
                                vc.getViewInstance().getView().getClass().getName() + "." + BOUNDS,
                                "" + r.x + "," + r.y + "," + r.width + "," + r.height );
                        }
                    }
                }
            } );
        }

        return vc;
    }
    
    /**
     * Invokes this <code>SessionUi</code> that a <code>ViewInstance</code>
     * has been activated.<br>
     * This method should be called from outside this
     * class only if consistency with this object state can be assured.
     * @param vi The <code>ViewInstance</code> that has been activated.
     */
    void viewInstanceActivated( ViewInstance vi )
    {
        activeVi = vi;

        // activation notification
        try
        {
            vi.activate();
        }
        catch (Exception ex) { ex.printStackTrace(); }
                
        // deliver activation event to listeners
        ViewInstanceEvent event = new ViewInstanceEvent(
            this, vi );
        
        fireViewInstanceActivatedEvent( event );
        
        SgEngine.getInstance().getEventQueue().processEvents();
    }

    /**
     * Invokes this <code>SessionUi</code> that a <code>ViewInstance</code>
     * has been deactivated. <br>
     * This method should be called from outside this
     * class only if consistency with this object state can be assured.
     * @param vi The <code>ViewInstance</code> that has been dedactivated.
     */
    void viewInstanceDeactivated( ViewInstance vi )
    {
        activeVi = null;

        // deactivation notification
        try
        {
            vi.deactivate();
        }
        catch (Exception ex) { ex.printStackTrace(); }
        
        // remove receivers from all actions
        SgAction[] actions = getActionPool().getActions();
        for (int i = 0; i < actions.length; i++)
        {
            actions[i].setActionReceiver( null );
        }
    }
    
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
        ViewAction viewAction, Session session, SessionElementDescriptor d ) throws ViewInstanceCreationFailedException
    {
        View view = viewAction.getView();
        if (!view.canHandle( d )) { return null; }
        ViewInstance vi = view.createViewInstance( session, d );
        ViewContainer vc = createViewContainer( viewAction, vi, d );
        
        addViewInstance( vi, vc, d );
        
        if (vc instanceof ViewFrame)
        {
            ViewFrame frame = (ViewFrame) vc;
            if (session != null && session == SgEngine.getInstance().getActiveSession()) {
                frame.setVisible( true );
            }
        }
        else if (vc instanceof JComponent)
        {
            tabbedPane.addTab(
                    (String) viewAction.getValue( Action.NAME ),
                    (Icon) viewAction.getValue( Action.SMALL_ICON ),
                    (JComponent) vc );
        }
        vi.open();
        return vi;
    }


    private void addViewInstance(
        ViewInstance viewInstance, ViewContainer vc, SessionElementDescriptor descriptor )
    {
        viMap.put( viewInstance, new Object[] { vc, descriptor } );
        
        viewInstanceCreated( viewInstance, descriptor );
        
        fireViewInstanceAddedEvent( new ViewInstanceEvent( this, viewInstance ) );
    }
    
    public void removeViewInstance( ViewInstance viewInstance )
    {
        System.out.println( "removeViewInstance()" );
        Object o = viMap.get( viewInstance );
        if (o == null) { return; }
        Object uiObj = ((Object[]) o)[0];
        if (uiObj instanceof ViewTab)
        {
            ViewTab vt = (ViewTab) uiObj;
            tabbedPane.removeTabAt( vt.getTabIndex() );
        }
        else if (uiObj instanceof ViewFrame)
        {
            ViewFrame vf = (ViewFrame) uiObj;
            vf.dispose();
        }
        viewInstance.close();
        viMap.remove( viewInstance );

        viewInstanceRemoved( viewInstance );
        
        fireViewInstanceRemovedEvent( new ViewInstanceEvent( this, viewInstance ) );
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.SessionListener#sessionAdded(com.jonasreese.sound.sg.SessionEvent)
     */
    public void sessionAdded( SessionEvent e )
    {
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.SessionListener#sessionRemoved(com.jonasreese.sound.sg.SessionEvent)
     */
    public void sessionRemoved( SessionEvent e )
    {
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.SessionListener#sessionActivated(com.jonasreese.sound.sg.SessionEvent)
     */
    public void sessionActivated( SessionEvent e )
    {
        System.out.println( "Session " + e.getSession().getName() + " activated" );
        setExternalFramesVisible( true );
    }
    
    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.SessionListener#sessionDeactivated(com.jonasreese.sound.sg.SessionEvent)
     */
    public void sessionDeactivated( SessionEvent e  )
    {
        System.out.println( "Session " + e.getSession().getName() + " deactivated" );
        setExternalFramesVisible( false );
    }

    private void setExternalFramesVisible( boolean b )
    {
        for (Object o : viMap.values())
        {
            if (o != null)
            {
                Object uiObj = ((Object[]) o)[0];
                if (uiObj instanceof ViewFrame)
                {
                    ViewFrame frame = (ViewFrame) uiObj;
                    frame.setVisible( b );
                }
            }
        }
    }

    class ViewActionFocusListener implements FocusListener
    {
        ViewAction viewAction;
        
        ViewActionFocusListener( ViewAction viewAction )
        {
            this.viewAction = viewAction;
        }
        
        public void focusGained( FocusEvent e )
        {
            AbstractButton[] buttons = viewAction.getButtons();
            for (int i = 0; i < buttons.length; i++)
            {
                buttons[i].setSelected( true );
            }
        }
        public void focusLost( FocusEvent e )
        {
            AbstractButton[] buttons = viewAction.getButtons();
            for (int i = 0; i < buttons.length; i++)
            {
                buttons[i].setSelected( false );
            }
        }
    }
}
