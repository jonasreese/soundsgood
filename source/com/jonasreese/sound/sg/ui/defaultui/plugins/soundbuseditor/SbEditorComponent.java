/*
 * Created on 23.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.soundbus.AudioInputNode;
import com.jonasreese.sound.sg.soundbus.AudioOutputNode;
import com.jonasreese.sound.sg.soundbus.AudioSamplerNode;
import com.jonasreese.sound.sg.soundbus.AudioUnitNode;
import com.jonasreese.sound.sg.soundbus.MidiBranchNode;
import com.jonasreese.sound.sg.soundbus.MidiFilterNode;
import com.jonasreese.sound.sg.soundbus.MidiInputNode;
import com.jonasreese.sound.sg.soundbus.MidiJunctionNode;
import com.jonasreese.sound.sg.soundbus.MidiNoteCounterNode;
import com.jonasreese.sound.sg.soundbus.MidiOutputNode;
import com.jonasreese.sound.sg.soundbus.MidiSamplerNode;
import com.jonasreese.sound.sg.soundbus.NetworkAudioInputNode;
import com.jonasreese.sound.sg.soundbus.NetworkAudioOutputNode;
import com.jonasreese.sound.sg.soundbus.OSCNode;
import com.jonasreese.sound.sg.soundbus.SbInput;
import com.jonasreese.sound.sg.soundbus.SbNode;
import com.jonasreese.sound.sg.soundbus.SbOutput;
import com.jonasreese.sound.sg.soundbus.Soundbus;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.SoundbusEvent;
import com.jonasreese.sound.sg.soundbus.SoundbusException;
import com.jonasreese.sound.sg.soundbus.SoundbusListener;
import com.jonasreese.sound.sg.soundbus.SoundbusNodesConnectionEvent;
import com.jonasreese.sound.sg.soundbus.SoundbusToolkit;
import com.jonasreese.sound.sg.soundbus.TempoNode;
import com.jonasreese.sound.sg.soundbus.VstNode;
import com.jonasreese.sound.sg.soundbus.datatransfer.SoundbusSerializer;
import com.jonasreese.sound.sg.soundbus.datatransfer.SoundbusTransferable;
import com.jonasreese.sound.sg.soundbus.edit.DeleteSbNodesEdit;
import com.jonasreese.sound.sg.soundbus.edit.DisconnectSbNodesEdit;
import com.jonasreese.sound.sg.soundbus.edit.PasteSoundbusElementsEdit;
import com.jonasreese.sound.sg.soundbus.edit.SbNodeStateChangeEdit;
import com.jonasreese.sound.sg.ui.defaultui.SessionActionPool;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor.PSbNode.InOutNode;
import com.jonasreese.util.ParametrizedResourceBundle;
import com.jonasreese.util.Updatable;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.PRoot;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.event.PNotification;
import edu.umd.cs.piccolox.event.PNotificationCenter;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;
import edu.umd.cs.piccolox.handles.PBoundsHandle;
import edu.umd.cs.piccolox.util.PBoundsLocator;

/**
 * @author jonas.reese
 */
public class SbEditorComponent extends PCanvas implements SoundbusListener {
    private static final long serialVersionUID = 1L;
    
    public static final String ZOOM_PROPERTY_NAME = "zoom";
    
    private PSelectionEventHandler selectionEventHandler;
    
    private SessionActionPool sessionActionPool;
    private SoundbusDescriptor soundbusDescriptor;
    private Updatable deleteUpdatable;
    private Map<SbNode,PSbNode> nodeMap;
    private PropertyChangeListener pRootPropertyChangeListener;
    private PropertyChangeListener nodePropertyChangeListener;
    
    /**
     * Constructs a new <code>SbEditorComponent</code>.
     * @param soundbusDescriptor The <code>SoundbusDescriptor</code> wrapping the
     * soundbus that shall be edited by this <code>SbEditorComponent</code>.
     */
    public SbEditorComponent( SoundbusDescriptor soundbusDescriptor ) {
        
        this.soundbusDescriptor = soundbusDescriptor;
        
        sessionActionPool =
            UiToolkit.getSessionUi( soundbusDescriptor.getSession() ).getActionPool();
        
        pRootPropertyChangeListener = new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent e ) {
                adjustBounds();
            }
        };

        // basic component configuration
        setLayout( new BorderLayout() );
        setFocusable( true );
        setAutoscrolls( true );
        
        setZoomEventHandler( null );
        setPanEventHandler( null );
        
        nodePropertyChangeListener = new PropertyChangeListener() {
            public void propertyChange( PropertyChangeEvent e ) {
                if ("bounds".equals( e.getPropertyName() )) {
                    if (e.getSource() instanceof SbNode) {
                        SbNode sbNode = (SbNode) e.getSource();
                        PSbNode pnode = nodeMap.get( sbNode );
                        if (pnode != null) { 
                            pnode.setBounds( PSbNode.getBoundsProperty( pnode.getSbNode() ) );
                        }
                    }
                } else if ("offset".equals( e.getPropertyName() )) {
                    if (e.getSource() instanceof SbNode) {
                        SbNode sbNode = (SbNode) e.getSource();
                        PSbNode pnode = nodeMap.get( sbNode );
                        if (pnode != null) { 
                            pnode.setOffset( PSbNode.getOffsetProperty( pnode.getSbNode() ) );
                        }
                    }
                }
            }
        };
        
        MouseWheelListener[] mwls = getMouseWheelListeners();
        if (mwls != null) {
            for (int i = 0; i < mwls.length; i++) {
                removeMouseWheelListener( mwls[i] );
            }
        }

        enableEvents( AWTEvent.MOUSE_WHEEL_EVENT_MASK );
        
        nodeMap = new HashMap<SbNode,PSbNode>();
        
        // create updatables for enabling standard SoundsGood actions
        deleteUpdatable = new Updatable() {
            public void update( Object o ) {
                Collection<?> collection = selectionEventHandler.getSelection();
                
                if (collection.size() == 1 && collection.iterator().next() instanceof PArrow) {
                    PArrow parrow = (PArrow) collection.iterator().next();
                    if (parrow.getInput() != null && parrow.getOutput() != null) {
                        DisconnectSbNodesEdit disconnectEdit = new DisconnectSbNodesEdit(
                                SbEditorComponent.this.soundbusDescriptor,
                                parrow.getInput(), parrow.getOutput() );
                        disconnectEdit.perform();
                        SbEditorComponent.this.soundbusDescriptor.getUndoManager().addEdit(
                                disconnectEdit );
                    }
                } else {
                    int count = 0;
                    for (Object obj : collection) {
                        if (obj instanceof PSbNode) {
                            count++;
                        }
                    }
                    SbNode[] nodes = new SbNode[count];
                    count = 0;
                    for (Object obj : collection) {
                        if (obj instanceof PSbNode) {
                            nodes[count++] = ((PSbNode) obj).getSbNode();
                        }
                    }
                    DeleteSbNodesEdit deleteEdit = new DeleteSbNodesEdit(
                            SbEditorComponent.this.soundbusDescriptor, nodes );
                    deleteEdit.perform();
                    SbEditorComponent.this.soundbusDescriptor.getUndoManager().addEdit( deleteEdit );

                }
            }
        };
        
        selectionEventHandler =
            new PSelectionEventHandler( getLayer(), getLayer() ) {
            public void keyPressed( PInputEvent e ) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    deleteUpdatable.update( null );
                }
            }
            public void decorateSelectedNode( PNode node ) {
                if (node instanceof PArrow) {
                    PArrow arrow = (PArrow) node;
                    boolean slash = false;
                    if (arrow.start.y >= arrow.end.y) {
                        slash = (arrow.start.x <= arrow.end.x);
                    } else {
                        slash = (arrow.start.x > arrow.end.x);
                    }
                    //System.out.println( "slash = " + slash );
                    if (slash) {
                        node.addChild( new PBoundsHandle( PBoundsLocator.createSouthWestLocator( node ) ) );
                        node.addChild( new PBoundsHandle( PBoundsLocator.createNorthEastLocator( node ) ) ); 
                    } else {
                        node.addChild( new PBoundsHandle( PBoundsLocator.createNorthWestLocator( node ) ) ); 
                        node.addChild( new PBoundsHandle( PBoundsLocator.createSouthEastLocator( node ) ) );
                    }
                } else {
                    super.decorateSelectedNode( node );
                }
            }
        };
        addInputEventListener( selectionEventHandler );
        getRoot().getDefaultInputManager().setKeyboardFocus( selectionEventHandler );
        
        try {
            Soundbus s = SbEditorComponent.this.soundbusDescriptor.getSoundbus();
            String zoomStr = s.getClientProperty( ZOOM_PROPERTY_NAME );
            if (zoomStr != null) {
                double d = Double.parseDouble( zoomStr );
                if (d > 0 && d <= 1.0) {
                    getCamera().setViewScale( d );
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
//    protected void finalize() throws Throwable {
//        System.out.println( "SbEditorComponent.finalize()" );
//        super.finalize();
//    }
    
    public boolean isSoundbusLive() {
        Soundbus soundbus = null;
        try {
            soundbus = soundbusDescriptor.getSoundbus();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (soundbus != null && soundbus.isOpen());
    }

    private void adjustBounds() {
        double currScale = getCamera().getViewScale();
        setPreferredSize( new Dimension(
                (int) (getRoot().getFullBoundsReference().width * currScale),
                (int) (getRoot().getFullBoundsReference().height  * currScale) ) );
        revalidate();
    }
    
    protected void processMouseWheelEvent( MouseWheelEvent e ) {
        if (e.isControlDown()) {
            double currentScale = getCamera().getViewScale();
            if (e.getWheelRotation() < 0) {
                if (currentScale < 1) {
                    getCamera().setViewScale( currentScale + 0.1 );
                    adjustBounds();
                }
            } else {
                if (currentScale > 0.2) {
                    getCamera().setViewScale( currentScale - 0.1 );
                    adjustBounds();
                }
            }
            try {
                Soundbus s = SbEditorComponent.this.soundbusDescriptor.getSoundbus();
                s.putClientProperty( ZOOM_PROPERTY_NAME, Double.toString( getCamera().getViewScale() ) );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            Component parent = getParent();
            while (parent != null && !(parent instanceof JScrollPane)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                parent.dispatchEvent( e );
            }
        }
        super.processMouseWheelEvent( e );
    }
    
    
    /**
     * Adds all graphical elements required to display the soundbus currently set in the
     * <code>SoundbusDescriptor</code>...
     * @param soundbus The soundbus. Must not be null
     */
    private void displaySoundbus( Soundbus soundbus ) {
        SbNode[] nodes = soundbus.getNodes();
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].addPropertyChangeListener( nodePropertyChangeListener );
            nodeAdded( nodes[i] );
        }
        for (int i = 0; i < nodes.length; i++) {
            SbInput[] inputs = nodes[i].getInputs();
            for (int j = 0; j < inputs.length; j++) {
                if (inputs[j].getConnectedOutput() != null) {
                    nodesConnected( inputs[j], inputs[j].getConnectedOutput() );
                }
            }
        }
    }
    
    public boolean isSelectionEmpty() {
        return selectionEventHandler.getSelectionReference() == null ||
            selectionEventHandler.getSelectionReference().isEmpty();
    }
    
    public SoundbusDescriptor getSoundbusDescriptor() {
        return soundbusDescriptor;
    }
    
    // invoked when the selection has changed
    public void selectionChanged( PNotification notfication ) {
        boolean notEmpty = !isSelectionEmpty();
        sessionActionPool.getAction( SessionActionPool.DELETE ).setEnabled( notEmpty );
        sessionActionPool.getAction( SessionActionPool.CUT ).setEnabled( notEmpty );
        sessionActionPool.getAction( SessionActionPool.COPY ).setEnabled( notEmpty );
        sessionActionPool.getAction( SessionActionPool.SELECT_NONE ).setEnabled( notEmpty );
        sessionActionPool.getAction( SessionActionPool.PROPERTIES ).setEnabled( notEmpty );
    }

    // method call delegated by ViewInstance implementation
    public void open() {
        ToolTipManager.sharedInstance().registerComponent( this );
        PNotificationCenter.defaultCenter().addListener(
                this, "selectionChanged",
                PSelectionEventHandler.SELECTION_CHANGED_NOTIFICATION,
                selectionEventHandler );
        getRoot().addPropertyChangeListener( PRoot.PROPERTY_FULL_BOUNDS, pRootPropertyChangeListener );
        
        Soundbus sb = null;
        try {
            sb = soundbusDescriptor.getSoundbus();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sb != null) {
            displaySoundbus( sb );
            sb.addSoundbusListener( this );
        }
        updateTitle();
    }
    
    void updateTitle() {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                ViewContainer vc = (ViewContainer) UiToolkit.getViewContainer( SbEditorComponent.this );
                if (vc != null) {
                    vc.setTitleText(
                            getSoundbusDescriptor().getName() + (getSoundbusDescriptor().isChanged() ? "*" : "") );
                }
            }
        } );
    }
    
    private void copySelectedEvents() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Collection<?> collection = selectionEventHandler.getSelection();
        int count = 0;
        for (Object obj : collection) {
            if (obj instanceof PSbNode) {
                count++;
            }
        }
        SbNode[] selectedNodes = new SbNode[count];
        count = 0;
        for (Object obj : collection) {
            if (obj instanceof PSbNode) {
                selectedNodes[count++] = ((PSbNode) obj).getSbNode();
            }
        }
        Element root = new Element( "soundbus" );
        try {
            SoundbusToolkit.serializeSoundbusNodes( selectedNodes, root );
            XMLOutputter xmlOutputter = new XMLOutputter();
            org.jdom.Document doc = new org.jdom.Document( root );
            xmlOutputter.setFormat( Format.getCompactFormat() );
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            xmlOutputter.output( doc, out );
            SoundbusTransferable transferable = new SoundbusTransferable(
                new SoundbusSerializer( new String( out.toByteArray() ) ) );
            clipboard.setContents( transferable, transferable );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Pastes the events from the clipboard to the <code>GridComponent</code>.
     */
    private void pasteFromClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable t = clipboard.getContents( this );
        selectionEventHandler.unselectAll();
        DataFlavor df = new DataFlavor( SoundbusSerializer.class, null );
        if (t.isDataFlavorSupported( df )) {
            try {
                PasteSoundbusElementsEdit edit = new PasteSoundbusElementsEdit(
                        soundbusDescriptor, (SoundbusSerializer) t.getTransferData( df ) );
                soundbusDescriptor.getUndoManager().addEdit( edit );
                edit.perform();
            } catch (UnsupportedFlavorException ufex) {
                ufex.printStackTrace();
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
            
        } else {
            System.out.println( "*** CLIPBOARD DATA NOT SUPPORTED: " + df.getDefaultRepresentationClass() );
        }
    }

    // method call delegated by ViewInstance implementation
    public void activate() {
        soundbusDescriptor.select();
        
        // set action receivers
        sessionActionPool.getAction( SessionActionPool.CUT ).setActionReceiver( new Updatable() {
            public void update( Object o ) {
                copySelectedEvents();
                deleteUpdatable.update( null );
            }
        } );
        sessionActionPool.getAction( SessionActionPool.COPY ).setActionReceiver( new Updatable() {
            public void update( Object o ) {
                copySelectedEvents();
            }
        } );
        sessionActionPool.getAction( SessionActionPool.PASTE ).setActionReceiver( new Updatable() {
            public void update( Object o ) {
                pasteFromClipboard();
            }
        } );
        sessionActionPool.getAction( SessionActionPool.DELETE ).setActionReceiver( deleteUpdatable );
        sessionActionPool.getAction( SessionActionPool.SELECT_ALL ).setActionReceiver( new Updatable() {
            public void update( Object o ) {
                Collection<PSbNode> nodes = nodeMap.values();
                for (PSbNode node : nodes) {
                    selectionEventHandler.select( node );
                    SbInput[] inputs = node.getSbNode().getInputs();
                    for (int i = 0; i < inputs.length; i++) {
                        InOutNode ion = node.getInOutNodeFor( inputs[i] );
                        if (ion.arrow != null) {
                            selectionEventHandler.select( ion.arrow );
                        }
                    }
                }
            }
        } );
        sessionActionPool.getAction( SessionActionPool.SELECT_NONE ).setActionReceiver( new Updatable() {
            public void update( Object o ) {
                selectionEventHandler.unselectAll();
            }
        } );
        sessionActionPool.getAction( SessionActionPool.INVERT_SELECTION ).setActionReceiver( new Updatable() {
            public void update( Object o ) {
                Collection<PSbNode> nodes = nodeMap.values();
                for (PSbNode node : nodes) {
                    if (selectionEventHandler.isSelected( node )) {
                        selectionEventHandler.unselect( node );
                    } else {
                        selectionEventHandler.select( node );
                    }
                    SbInput[] inputs = node.getSbNode().getInputs();
                    for (int i = 0; i < inputs.length; i++) {
                        InOutNode ion = node.getInOutNodeFor( inputs[i] );
                        if (ion.arrow != null) {
                            if (selectionEventHandler.isSelected( ion.arrow )) {
                                selectionEventHandler.unselect( ion.arrow );
                            } else {
                                selectionEventHandler.select( ion.arrow );
                            }
                        }
                    }
                }
            }
        } );
        sessionActionPool.getAction( SessionActionPool.PROPERTIES ).setActionReceiver( new Updatable() {
            public void update( Object o ) {
                // TODO: implement this
            }
        } );
        // update selection-dependant action enabled states
        selectionChanged( null );
    }
    // method call delegated by ViewInstance implementation
    public void deactivate() {
    }
    // method call delegated by ViewInstance implementation
    public void close() {
        ToolTipManager.sharedInstance().unregisterComponent( this );
        PNotificationCenter.defaultCenter().removeListener( this,
                PSelectionEventHandler.SELECTION_CHANGED_NOTIFICATION,
                selectionEventHandler );
        getRoot().removePropertyChangeListener( pRootPropertyChangeListener );
        
        Soundbus sb = null;
        try {
            sb = soundbusDescriptor.getSoundbus();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sb != null) {
            // remove client property listener from nodes
            SbNode[] nodes = sb.getNodes();
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].removePropertyChangeListener( nodePropertyChangeListener );
            }
            
            try {
                sb.close();
            } catch (SoundbusException e) {
                e.printStackTrace();
            }
            sb.removeSoundbusListener( this );
        }
        
        // call close() method on all UI nodes
        synchronized (nodeMap) {
            for (PSbNode n : nodeMap.values()) {
                n.nodeRemoved();
            }
        }
        
        sessionActionPool.getAction( SessionActionPool.CUT ).setActionReceiver( null );
        sessionActionPool.getAction( SessionActionPool.COPY ).setActionReceiver( null );
        sessionActionPool.getAction( SessionActionPool.PASTE ).setActionReceiver( null );
        sessionActionPool.getAction( SessionActionPool.DELETE ).setActionReceiver( null );
        sessionActionPool.getAction( SessionActionPool.SELECT_ALL ).setActionReceiver( null );
        sessionActionPool.getAction( SessionActionPool.SELECT_NONE ).setActionReceiver( null );
        sessionActionPool.getAction( SessionActionPool.INVERT_SELECTION ).setActionReceiver( null );
        sessionActionPool.getAction( SessionActionPool.PROPERTIES ).setActionReceiver( null );
    }
    
    public void nodeAdded( SoundbusEvent e ) {
        e.getNode().addPropertyChangeListener( nodePropertyChangeListener );
        nodeAdded( e.getNode() );
    }
    
    private void nodeAdded( SbNode sbn ) {
        final ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        PSbNode pnode = null;
        if (sbn instanceof TempoNode) {
            TempoNode node = (TempoNode) sbn;
            node.setName( rb.getString( "soundbus.node.tempo" ) );
            pnode = new PTempoNode( this, node, soundbusDescriptor );
        } else if (sbn instanceof MidiInputNode) {
            MidiInputNode node = (MidiInputNode) sbn;
            node.setName( rb.getString( "soundbus.node.midiInput" ) );
            pnode = new PMidiInputNode( this, node, soundbusDescriptor );
        } else  if (sbn instanceof MidiOutputNode) {
            MidiOutputNode node = (MidiOutputNode) sbn;
            node.setName( rb.getString( "soundbus.node.midiOutput" ) );
            pnode = new PMidiOutputNode( this, node, soundbusDescriptor );
        } else  if (sbn instanceof MidiBranchNode) {
            MidiBranchNode node = (MidiBranchNode) sbn;
            node.setName( rb.getString( "soundbus.node.midiBranch" ) );
            pnode = new PMidiBranchNode( this, node, soundbusDescriptor );
        } else if (sbn instanceof MidiJunctionNode) {
            MidiJunctionNode node = (MidiJunctionNode) sbn;
            node.setName( rb.getString( "soundbus.node.midiJunction" ) );
            pnode = new PMidiJunctionNode( this, node, soundbusDescriptor );
        } else if (sbn instanceof MidiFilterNode) {
            MidiFilterNode node = (MidiFilterNode) sbn;
            node.setName( rb.getString( "soundbus.node.midiFilter" ) );
            pnode = new PMidiFilterNode( this, node, soundbusDescriptor );
        } else if (sbn instanceof MidiSamplerNode) {
            MidiSamplerNode node = (MidiSamplerNode) sbn;
            node.setName( rb.getString( "soundbus.node.midiSampler" ) );
            pnode = new PMidiSamplerNode( this, node, soundbusDescriptor );
        } else if (sbn instanceof MidiNoteCounterNode) {
            MidiNoteCounterNode node = (MidiNoteCounterNode) sbn;
            node.setName( rb.getString( "soundbus.node.midiNoteCounter" ) );
            pnode = new PMidiNoteCounterNode( this, node, soundbusDescriptor );
        } else if (sbn instanceof VstNode) {
            VstNode node = (VstNode) sbn;
            node.setName( rb.getString( "soundbus.node.vstPlugin" ) );
            pnode = new PVstNode( this, node, soundbusDescriptor );
        } else if (sbn instanceof AudioUnitNode) {
            AudioUnitNode node = (AudioUnitNode) sbn;
            node.setName( rb.getString( "soundbus.node.audioUnit" ) );
            pnode = new PAudioUnitNode( this, node, soundbusDescriptor );
        } else if (sbn instanceof AudioInputNode) {
            AudioInputNode node = (AudioInputNode) sbn;
            node.setName( rb.getString( "soundbus.node.audioInput" ) );
            pnode = new PAudioInputNode( this, node, soundbusDescriptor );
        } else if (sbn instanceof NetworkAudioInputNode) {
            NetworkAudioInputNode node = (NetworkAudioInputNode) sbn;
            node.setName( rb.getString( "soundbus.node.networkAudioInput" ) );
            pnode = new PNetworkAudioInputNode( this, node, soundbusDescriptor );
        } else if (sbn instanceof AudioOutputNode) {
            AudioOutputNode node = (AudioOutputNode) sbn;
            node.setName( rb.getString( "soundbus.node.audioOutput" ) );
            pnode = new PAudioOutputNode( this, node, soundbusDescriptor );
        } else if (sbn instanceof NetworkAudioOutputNode) {
            NetworkAudioOutputNode node = (NetworkAudioOutputNode) sbn;
            node.setName( rb.getString( "soundbus.node.networkAudioOutput" ) );
            pnode = new PNetworkAudioOutputNode( this, node, soundbusDescriptor );
        } else if (sbn instanceof AudioSamplerNode) {
            AudioSamplerNode node = (AudioSamplerNode) sbn;
            node.setName( rb.getString( "soundbus.node.audioSampler" ) );
            pnode = new PAudioSamplerNode( this, node, soundbusDescriptor );
        } else if (sbn instanceof OSCNode) {
            OSCNode node = (OSCNode) sbn;
            node.setName( rb.getString( "soundbus.node.osc" ) );
            pnode = new POSCNode( this, node, soundbusDescriptor );
        } else {
            System.out.println( "SbEditorComponent: added unknown node type: " + sbn.getClass() );
        }
        
        
        if (pnode != null) {
            // add move listener
            final PSbNode _pnode = pnode;
            pnode.addInputEventListener( new PBasicInputEventHandler() {
                double moveOffsetX;
                double moveOffsetY;
                public void mousePressed( PInputEvent e ) {
                    moveOffsetX = _pnode.getXOffset();
                    moveOffsetY = _pnode.getYOffset();
                    PNode n = e.getPickedNode();
                    n.moveToFront();
                }
                public void mouseReleased( PInputEvent e ) {
                    // node moved, create undo action
                    final double moveOffsetX = this.moveOffsetX - _pnode.getXOffset();
                    final double moveOffsetY = this.moveOffsetY - _pnode.getYOffset();
                    if (moveOffsetX != 0 || moveOffsetY != 0) {
                        List<SbNode> nodes = new ArrayList<SbNode>();
                        for (Object o : selectionEventHandler.getSelection()) {
                            PNode n = (PNode) o;
                            if (n instanceof PSbNode) {
                                PSbNode psbnode = (PSbNode) n;
                                nodes.add( psbnode.getSbNode() );
                            }
                        }
                        SbNodeStateChangeEdit moveEdit = new MoveEdit(
                                soundbusDescriptor,
                                nodes,
                                rb.getString( "edit.moveSbNodes" ),
                                moveOffsetX, moveOffsetY );
                        moveEdit.perform();
                        soundbusDescriptor.getUndoManager().addEdit( moveEdit );
                    }
                }
                public void mouseClicked( PInputEvent e ) {
                    if (e.getClickCount() == 2) {
                        PNode n = e.getPickedNode();
                        if (n instanceof PSbNode) {
                            ((PSbNode) n).editNode();
                        }
                    }
                }
            } );

            
            // add new node
            getLayer().addChild( pnode );

            // animate new node
            Rectangle2D.Double bounds = PSbNode.getBoundsProperty( pnode.getSbNode() );
            Point2D.Double offset = PSbNode.getOffsetProperty( pnode.getSbNode() );
            if (bounds == null && offset == null) {
                double x = getVisibleRect().width / 2 - pnode.getWidth() / 2 + getVisibleRect().x;
                double y = getVisibleRect().height / 2 - pnode.getWidth() / 2 + getVisibleRect().y;
                pnode.animateToPositionScaleRotation( x, y,
                        getLayer().getScale(), 0, 700 );
                PSbNode.setBoundsProperty( pnode.getSbNode(), pnode.getBounds() );
                PSbNode.setOffsetProperty( pnode.getSbNode(), x, y );
            } else {
                pnode.setBounds( bounds );
                pnode.setOffset( offset.getX(), offset.getY() );
                selectionEventHandler.select( pnode );
            }

            nodeMap.put( sbn, pnode );
            pnode.nodeAdded();
        }
    }

    public void nodeRemoved( SoundbusEvent e ) {
        //System.out.println( "SbEditorComponent.nodeRemoved(): " + e );
        selectionEventHandler.unselectAll();
        SbNode node = e.getNode();
        if (node != null) {
            PSbNode pnode = nodeMap.get( node );
            if (pnode != null) {
                getLayer().removeChild( pnode );
                pnode.nodeRemoved();
            }
            nodeMap.remove( node );
        }
    }

    public void nodesConnected( SoundbusNodesConnectionEvent e ) {
        nodesConnected( e.getInput(), e.getOutput() );
    }
    
    private void nodesConnected( SbInput input, SbOutput output ) {
        //System.out.println( "connected input " + input.getName() + " to " + output.getName() );
        PSbNode pnode = nodeMap.get( input.getSbNode() );
        PSbNode destNode = nodeMap.get( output.getSbNode() );
        if (pnode != null && destNode != null) {
            pnode.nodesConnected( destNode, input, output );
        }
    }

    public void nodesDisconnected( SoundbusNodesConnectionEvent e ) {
        //System.out.println( "disconnected input " + e.getInput().getName() + " from " + e.getOutput().getName() );
        PSbNode pnode = nodeMap.get( e.getInput().getSbNode() );
        PSbNode destNode = nodeMap.get( e.getOutput().getSbNode() );
        if (pnode != null) {
            pnode.nodesDisconnected( destNode, e.getInput(), e.getOutput() );
        }
    }

    public void soundbusOpened( SoundbusEvent e ) {
        repaint();
    }

    public void soundbusClosed( SoundbusEvent e ) {
        repaint();
    }

    public void muteStatusChanged(SoundbusEvent e) {
    }

    public void tempoChanged(SoundbusEvent e) {
    }
    
    private static class MoveEdit extends SbNodeStateChangeEdit {
        private double moveOffsetX;
        private double moveOffsetY;
        
        public MoveEdit(
                SoundbusDescriptor soundbusDescriptor,
                List<SbNode> nodes,
                String presentationName,
                double moveOffsetX, double moveOffsetY ) {
            super( soundbusDescriptor, nodes, presentationName );
            this.moveOffsetX = moveOffsetX;
            this.moveOffsetY = moveOffsetY;
        }
        private static final long serialVersionUID = 1L;
        public void undoImpl() {
            for (Iterator<SbNode> iter = getNodes().iterator(); iter.hasNext(); ) {
                SbNode node = iter.next();
                Point2D.Double offset = PSbNode.getOffsetProperty( node );
                PSbNode.setOffsetProperty( node, offset.x + moveOffsetX, offset.y + moveOffsetY );
            }
        }
        public void redoImpl() {
            performImpl();
        }
        @Override
        public void performImpl() {
            for (Iterator<SbNode> iter = getNodes().iterator(); iter.hasNext(); ) {
                SbNode node = iter.next();
                Point2D.Double offset = PSbNode.getOffsetProperty( node );
                PSbNode.setOffsetProperty( node, offset.x - moveOffsetX, offset.y - moveOffsetY );
            }

        }
    }
    
}