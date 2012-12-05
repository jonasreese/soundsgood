/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 25.09.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.session;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.jonasreese.sound.sg.ObjectSelectionChangeListener;
import com.jonasreese.sound.sg.ObjectSelectionChangedEvent;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.SgProperties;
import com.jonasreese.sound.sg.plugin.view.View;
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
public class InternalFrameSessionUi extends SessionUi {
    
    private JDesktopPane desktop;
    private HashMap<ViewInstance,Object[]> viMap;
    private ViewInstance activeVi;
    private ObjectSelectionChangeListener objectSelectionChangeListener;
    
    private static final String BOUNDS = "internalFrameBounds";

    /**
     * Constructs a new <code>InternalFrameSessionUi</code>.
     * @param panel
     * @param menuBar
     * @param toolBar A <code>SgToolBar</code> representing the tool bar.
     * @param actionPool
     */
    public InternalFrameSessionUi(
            JComponent panel, JMenuBar menuBar, SgToolBar toolBar, SessionActionPool actionPool ) {
        super( menuBar, toolBar, actionPool );
        desktop = new JDesktopPane();
        desktop.setAutoscrolls( true );
        panel.add( desktop );
        viMap = new HashMap<ViewInstance,Object[]>();
        activeVi = null;
        objectSelectionChangeListener = new ObjectSelectionChangeListener() {
            public void objectSelectionChanged( ObjectSelectionChangedEvent e ) {
                SessionElementDescriptor[] descs = e.getSelectedElements();
                synchronized (viMap) {
                    for (Iterator<Object[]> iter = viMap.values().iterator(); iter.hasNext(); ) {
                        Object[] o = iter.next();
                        ViewInternalFrame f = (ViewInternalFrame) o[0];
                        Object data = o[1];
                        boolean contained = false;
                        if (data == null) {
                            contained = true;
                        } else {
                            for (int i = 0; i < descs.length; i++) {
                                if (descs[i] == data) {
                                    contained = true;
                                }
                            }
                        }
                        if (contained && f.getView().isMultipleInstancePerSessionAllowed()) {
                            final Component c = f.getParent();
                            final ViewInternalFrame _f = f;
                            if (c instanceof JLayeredPane) {
                                SwingUtilities.invokeLater( new Runnable() {
                                    public void run() {
                                        ((JLayeredPane) c).setPosition( _f, 1 );
                                        ViewInstance activeVi = InternalFrameSessionUi.this.activeVi;
                                        if (activeVi != null) {
                                            Object[] o;
                                            synchronized (viMap) {
                                                o = viMap.get( activeVi );
                                            }
                                            if (o != null) {
                                                ViewInternalFrame vif = (ViewInternalFrame) ((Object[]) o)[0];
                                                vif.toFront();
                                            }
                                        }
                                    }
                                } );
                            }
                        }
                    }
                }
            }
        };
    }
    
    public void sessionUiAdded( Session session ) {
        System.out.println( "InternalFrameSessionUi.sessionUiAdded()" );
        getActionPool().getSession().addObjectSelectionChangeListener( objectSelectionChangeListener );
    }
    
    public void sessionUiRemoved( Session session ) {
        System.out.println( "InternalFrameSessionUi.sessionUiRemoved()" );
        getActionPool().getSession().removeObjectSelectionChangeListener( objectSelectionChangeListener );
    }
    
    /**
     * Gets this <code>InternalFrameSessionUi</code>s desktop.
     * @return The desktop.
     */
    public JDesktopPane getDesktop() {
        return desktop;
    }
    
    /**
     * Gets the currently active <code>ViewInstance</code>.
     * @return The current active <code>ViewInstance</code>,
     *         or <code>null</code> if none is currently active.
     */
    public ViewInstance getActiveViewInstance() {
        return activeVi;
    }

    /**
     * Creates an internal frame for the given <code>ViewAction</code>.
     * @param viewAction The <code>Action</code> that triggered the creation
     *        of the frame.
     * @param vi The <code>ViewInstance</code>.
     * @param data The vi data.
     * @return A newly created internal frame.
     */
    private ViewInternalFrame createInternalFrame(
            ViewAction viewAction, ViewInstance vi, Object data ) {
        
        ViewInternalFrame internalFrame = new ViewInternalFrame(
            this, viewAction.getView(), vi );
        internalFrame.setFrameIcon( (Icon) viewAction.getValue( Action.SMALL_ICON ) );
        internalFrame.addFocusListener( new ViewActionFocusListener( viewAction ) );
        internalFrame.addInternalFrameListener( new InternalFrameAdapter() {
            public void internalFrameClosed( InternalFrameEvent e ) {
                ViewInternalFrame f = (ViewInternalFrame) e.getSource();
                ((InternalFrameSessionUi) f.getSessionUi()).removeViewInstance(
                    f.getViewInstance() );
            }
            public void internalFrameActivated( InternalFrameEvent e ) {
                viewInstanceActivated(
                    ((ViewInternalFrame) e.getSource()).getViewInstance() );
            }
            public void internalFrameDeactivated( InternalFrameEvent e ) {
                viewInstanceDeactivated(
                    ((ViewInternalFrame) e.getSource()).getViewInstance() );
            }
        } );

        // add UI object to frame
        Object o = vi.getUiObject( internalFrame );
        if (!(o instanceof Component)) { return null; }
        Component c = (Component) o;
        internalFrame.getContentPane().add( c );

        // set default frame bounds
        Rectangle r = null;
        
        if (SgEngine.getInstance().getProperties().getRestoreViewsFromSession()) {
            // restore from session element
            if (vi.getView().isMultipleInstancePerSessionAllowed()) {
                if (data instanceof SessionElementDescriptor) {
                    String s = ((SessionElementDescriptor) data).getPersistentClientProperty(
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
                    } catch (Exception ignored) {}
                }
            }
        }
        
        if (r == null) {
            r =  SgEngine.getInstance().getProperties().getPluginProperty(
                viewAction.getView(), BOUNDS, (Rectangle) null );
        }
        if (r != null) {
            if (!vi.isSetBoundsAllowed()) {
                internalFrame.setLocation( r.x, r.y );
                internalFrame.pack();
            } else {
                internalFrame.setBounds( r );
                internalFrame.setPreferredSize( new Dimension( r.width, r.height ) );
            }
        } else {
            internalFrame.pack();
        }

        // add component listener to store frame bounds to
        // application properties and to session
        internalFrame.addComponentListener( new ComponentAdapter() {
            public void componentMoved( ComponentEvent e ) {
                componentResized( e );
            }
            public void componentResized( ComponentEvent e ) {
                ViewInternalFrame vif = (ViewInternalFrame) e.getSource();
                Rectangle r = e.getComponent().getBounds();
                SgProperties p = SgEngine.getInstance().getProperties();
                // save frame bounds to properties to keep default value
                p.setPluginProperty( vif.getView(), BOUNDS, r );
                // check if bounds shall also be saved to session or to
                // session element (view instance data)
                if (p.getRestoreViewsFromSession()) {
                    // store to session element
                    if (vif.getView().isMultipleInstancePerSessionAllowed()) {
                        Object[] o;
                        synchronized (viMap) {
                            o = viMap.get( vif.getViewInstance() );
                        }
                        Object data = o[1];
                        if (data instanceof SessionElementDescriptor) {
                            ((SessionElementDescriptor) data).putPersistentClientProperty(
                                vif.getView().getClass().getName() + "." + BOUNDS,
                                "" + r.x + "," + r.y + "," + r.width + "," + r.height );
                        }
                    } else { // store to session
                        Session session = getActionPool().getSession();
                        session.putPersistentClientProperty(
                            vif.getView().getClass().getName() + "." + BOUNDS,
                            "" + r.x + "," + r.y + "," + r.width + "," + r.height );
                    }
                }
            }
        } );

        return internalFrame;
    }
    
    /**
     * Invokes this <code>SessionUi</code> that a <code>ViewInstance</code>
     * has been activated.<br>
     * This method should be called from outside this
     * class only if consistency with this object state can be assured.
     * @param vi The <code>ViewInstance</code> that has been activated.
     */
    void viewInstanceActivated( ViewInstance vi ) {
        activeVi = vi;

        // activation notification
        try {
            vi.activate();
        } catch (Exception ex) { ex.printStackTrace(); }
                
        // deliver activation event to listeners
        ViewInstanceEvent event = new ViewInstanceEvent( this, vi );
        
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
    void viewInstanceDeactivated( ViewInstance vi ) {
        activeVi = null;

        // deactivation notification
        try {
            vi.deactivate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        // remove receivers from all actions
        SgAction[] actions = getActionPool().getActions();
        for (int i = 0; i < actions.length; i++) {
            actions[i].setActionReceiver( null );
        }
    }
    
    /**
     * Creates a <code>ViewInstance</code>.
     * @param viewAction The <code>ViewAction</code> that has triggered
     *        this creation (the caller).
     * @param session The <code>Session</code> a <code>ViewInstance</code>
     *        shall be created for.
     * @param d The data <code>SessionElementDescriptor</code> to be passed to the
     *        <code>View</code> in order to create a <code>ViewInstance</code>.
     * @return The created <code>ViewInstance</code>, or <code>null</code>
     *         if no <code>ViewInstance</code> could be created.
     * @throws ViewInstanceCreationFailedException if the <code>ViewInstance</code>
     *         could not be created because the corresponding <code>View</code>
     *         failed to create it.
     */
    public ViewInstance createViewInstance(
            ViewAction viewAction, Session session, SessionElementDescriptor d )
    throws ViewInstanceCreationFailedException {
        View view = viewAction.getView();
        if (!view.canHandle( d )) { return null; }
        ViewInstance vi = view.createViewInstance( session, d );
        ViewInternalFrame f = createInternalFrame( viewAction, vi, d );
        if (desktop == null) { return vi; }
        
        addViewInstance( vi, f, d );
        
        desktop.add( f );
        
        boolean contained = false;
        if (d == null) {
            contained = true;
        } else {
            SessionElementDescriptor[] descs = getActionPool().getSession().getSelectedElements();
            for (int i = 0; i < descs.length; i++) {
                if (descs[i] == d) {
                    contained = true;
                }
            }
        }
        //f.setVisible( contained );
        f.setVisible( true );
        if (!contained) {
            f.toBack();
        }
        vi.open();
        return vi;
    }


    private void addViewInstance(
            ViewInstance viewInstance,
            ViewInternalFrame f,
            SessionElementDescriptor sessionElementDescriptor ) {
        synchronized (viMap) {
            viMap.put( viewInstance, new Object[] { f, sessionElementDescriptor } );
        }

        viewInstanceCreated( viewInstance, sessionElementDescriptor );
        
        fireViewInstanceAddedEvent( new ViewInstanceEvent( this, viewInstance ) );
    }
    
    public void removeViewInstance( ViewInstance viewInstance ) {
        System.out.println( "removeViewInstance()" );
        ViewInternalFrame f;
        synchronized (viMap) {
            Object o = viMap.get( viewInstance );
            if (o == null) { return; }
            f = (ViewInternalFrame) ((Object[]) o)[0];
            viewInstance.close();
            viMap.remove( viewInstance );
        }
        f.dispose();
        f.setViewInstance( null );
        viewInstanceRemoved( viewInstance );
        fireViewInstanceRemovedEvent( new ViewInstanceEvent( this, viewInstance ) );
    }


    class ViewActionFocusListener implements FocusListener {
        ViewAction viewAction;
        
        ViewActionFocusListener( ViewAction viewAction ) {
            this.viewAction = viewAction;
        }
        
        public void focusGained( FocusEvent e ) {
            AbstractButton[] buttons = viewAction.getButtons();
            for (int i = 0; i < buttons.length; i++) {
                buttons[i].setSelected( true );
            }
        }
        public void focusLost( FocusEvent e ) {
            AbstractButton[] buttons = viewAction.getButtons();
            for (int i = 0; i < buttons.length; i++) {
                buttons[i].setSelected( false );
            }
        }
    }
}
