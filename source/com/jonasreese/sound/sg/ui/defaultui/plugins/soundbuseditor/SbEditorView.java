/*
 * Created on 22.11.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.xml.sax.SAXException;

import com.jonasreese.sound.aucontainer.AUContainer;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.plugin.functionality.Functionality;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.plugin.view.ViewInstanceCreationFailedException;
import com.jonasreese.sound.sg.soundbus.IllegalSoundbusDescriptionException;
import com.jonasreese.sound.sg.soundbus.SoundbusAdapter;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.SoundbusEvent;
import com.jonasreese.sound.sg.soundbus.SoundbusListener;
import com.jonasreese.sound.sg.ui.defaultui.SessionActionPool;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.sound.sg.ui.defaultui.action.UndoAction;
import com.jonasreese.sound.vstcontainer.VstContainer;
import com.jonasreese.util.ParametrizedResourceBundle;
import com.jonasreese.util.resource.ResourceLoader;
import com.jonasreese.util.swing.DefaultPopupListener;

/**
 * @author jonas.reese
 */
public class SbEditorView implements View, Icon {

    private static Icon icon = new ResourceLoader(
            SbEditorView.class, "resource/editor.gif" ).getAsIcon();;
    private static Icon midiIcon = new ResourceLoader(
            SbEditorView.class, "resource/midi.gif" ).getAsIcon();
    private static Icon audioIcon = new ResourceLoader(
            SbEditorView.class, "resource/audio.gif" ).getAsIcon();
    private static Icon vstIcon = new ResourceLoader(
            SbEditorView.class, "resource/vst.gif" ).getAsIcon();
    private static Icon auIcon = new ResourceLoader(
            SbEditorView.class, "resource/au.gif" ).getAsIcon();
    private static Icon oscIcon = new ResourceLoader(
            SbEditorView.class, "resource/osc.gif" ).getAsIcon();
    
    public SbEditorView() {
    }
    
    public boolean isAutostartView() {
        return false;
    }

    public boolean isMultipleInstancePerSessionAllowed() {
        return true;
    }
    
    public boolean isMultipleInstancePerSessionElementAllowed() {
        return true;
    }

    public boolean canHandle( SessionElementDescriptor sessionElement ) {
        return (sessionElement instanceof SoundbusDescriptor);
    }

    public ViewInstance createViewInstance( Session session,
            SessionElementDescriptor sessionElementDescriptor )
            throws ViewInstanceCreationFailedException {
        if (!canHandle( sessionElementDescriptor )) {
            throw new IllegalArgumentException();
        }
        try {
            return new ViImpl( (SoundbusDescriptor) sessionElementDescriptor, this );
        } catch (Throwable t) {
            t.printStackTrace();
            throw new ViewInstanceCreationFailedException( t );
        }
    }

    public String getName() {
        return SgEngine.getInstance().getResourceBundle().getString(
                "plugin.sbEditor.name" );
    }

    public String getShortDescription() {
        return SgEngine.getInstance().getResourceBundle().getString(
                "plugin.sbEditor.shortDescription" );
    }

    public String getDescription() {
        return SgEngine.getInstance().getResourceBundle().getString(
                "plugin.sbEditor.description" );
    }

    public String getPluginName() {
        return "Soundbus Editor";
    }

    public String getPluginVersion() {
        return "0.1";
    }

    public String getPluginVendor() {
        return "Jonas Reese";
    }

    public void init() {
    }

    public void exit() {
    }

    public PluginConfigurator getPluginConfigurator() {
        return null;
    }

    static class ViImpl implements ViewInstance, PropertyChangeListener {
        private SbEditorComponent soundbusEditor;
        private SoundbusListener soundbusListener;
        private JComponent uiObject;
        private Action addTempoAction;
        private Action addMidiInputAction;
        private Action addMidiOutputAction;
        private Action addMidiJunctionAction;
        private Action addMidiBranchAction;
        private Action addMidiFilterAction;
        private Action addMidiSamplerAction;
        private Action addMidiNoteCounterAction;
        private Action addAudioInputAction;
        private Action addNetworkAudioInputAction;
        private Action addAudioOutputAction;
        private Action addNetworkAudioOutputAction;
        private Action addAudioSamplerAction;
        private Action addVstPluginAction;
        private Action addAudioUnitAction;
        private Action addOSCReceiverAction;
        private Action setLiveAction;
        private AbstractButton[] undoButtons;
        private AbstractButton[] redoButtons;
        private Action midiMenuAction;
        private Action audioMenuAction;
        private Action vstMenuAction;
        private Action auMenuAction;
        private Action oscMenuAction;
        private JToggleButton setLiveToggleButton;
        private JCheckBoxMenuItem setLiveMenuItems;
        
        private SessionActionPool sessionActionPool;
        private View view;
        
        //private SoundbusDescriptor soundbusDescriptor;
        
        ViImpl( SoundbusDescriptor soundbusDescriptor, View view ) {
            soundbusEditor = new SbEditorComponent( soundbusDescriptor );
            this.view = view;
            uiObject = new JPanel( new BorderLayout() );
            JScrollPane scrollPane = new JScrollPane( soundbusEditor );
            scrollPane.setAutoscrolls( true );
            scrollPane.getHorizontalScrollBar().setUnitIncrement( 30 );
            scrollPane.getVerticalScrollBar().setUnitIncrement( 30 );
            scrollPane.getHorizontalScrollBar().setBlockIncrement( 40 );
            scrollPane.getVerticalScrollBar().setBlockIncrement( 40 );
            uiObject.add( scrollPane );
            JToolBar toolbar = new JToolBar();
            toolbar.setRollover( true );
            uiObject.add( toolbar, BorderLayout.NORTH );

            ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();

            soundbusListener = new SoundbusAdapter() {
                public void soundbusClosed( SoundbusEvent e ) {
                    setLiveToggleButton.setSelected( false );
                    if (setLiveMenuItems != null) {
                        setLiveMenuItems.setSelected( false );
                    }
                }
                public void soundbusOpened( SoundbusEvent e ) {
                    setLiveToggleButton.setSelected( true );
                    if (setLiveMenuItems != null) {
                        setLiveMenuItems.setSelected( true );
                    }
                }
            };
            
            Functionality f = (Functionality) SgEngine.getInstance().getPlugin(
                    "com.jonasreese.sound.sg.ui.defaultui.plugins.soundbusmenu.SoundbusMenuFunctionality" );
            if (f != null) {
                addTempoAction = (Action) f.getProperty( "addTempoAction" );
                addMidiInputAction = (Action) f.getProperty( "addMidiInputAction" );
                addMidiOutputAction = (Action) f.getProperty( "addMidiOutputAction" );;
                addMidiJunctionAction = (Action) f.getProperty( "addMidiJunctionAction" );
                addMidiBranchAction = (Action) f.getProperty( "addMidiBranchAction" );
                addMidiFilterAction = (Action) f.getProperty( "addMidiFilterAction" );
                addMidiSamplerAction = (Action) f.getProperty( "addMidiSamplerAction" );
                addMidiNoteCounterAction = (Action) f.getProperty( "addMidiNoteCounterAction" );
                addAudioInputAction = (Action) f.getProperty( "addAudioInputAction" );
                addNetworkAudioInputAction = (Action) f.getProperty( "addNetworkAudioInputAction" );
                addAudioOutputAction = (Action) f.getProperty( "addAudioOutputAction" );
                addNetworkAudioOutputAction = (Action) f.getProperty( "addNetworkAudioOutputAction" );
                addAudioSamplerAction = (Action) f.getProperty( "addAudioSamplerAction" );
                addVstPluginAction = (Action) f.getProperty( "addVstPluginAction" );
                addAudioUnitAction = (Action) f.getProperty( "addAudioUnitAction" );
                addOSCReceiverAction = (Action) f.getProperty( "addOSCReceiverAction" );
                setLiveAction = (Action) f.getProperty( "setLiveAction" );
            }

            setLiveToggleButton = new JToggleButton( setLiveAction );
            setLiveToggleButton.setSelected( soundbusEditor.isSoundbusLive() );
            final JPopupMenu midiMenu = new JPopupMenu();
            midiMenuAction = new AbstractAction( null, midiIcon ) {
                private static final long serialVersionUID = 1L;
                public void actionPerformed( ActionEvent e ) {
                    if (e.getSource() instanceof Component) {
                        Component c = (Component) e.getSource();
                        midiMenu.show( c, 0, c.getHeight() );
                    }
                }
            };
            midiMenuAction.putValue( Action.SHORT_DESCRIPTION, rb.getString( "plugin.sbEditor.addMidi" ) );
            
            final JPopupMenu audioMenu = new JPopupMenu();
            audioMenuAction = new AbstractAction( null, audioIcon ) {
                private static final long serialVersionUID = 1L;
                public void actionPerformed( ActionEvent e ) {
                    if (e.getSource() instanceof Component) {
                        Component c = (Component) e.getSource();
                        audioMenu.show( c, 0, c.getHeight() );
                    }
                }
            };
            audioMenuAction.putValue( Action.SHORT_DESCRIPTION, rb.getString( "plugin.sbEditor.addAudio" ) );
            
            vstMenuAction = new AbstractAction( null, vstIcon ) {
                private static final long serialVersionUID = 1L;
                public void actionPerformed( ActionEvent e ) {
                    addVstPluginAction.actionPerformed( e );
                }
            };
            vstMenuAction.putValue( Action.SHORT_DESCRIPTION, rb.getString( "plugin.sbEditor.addVst" ) );
            
            auMenuAction = new AbstractAction( null, auIcon ) {
                private static final long serialVersionUID = 1L;
                public void actionPerformed( ActionEvent e ) {
                    addAudioUnitAction.actionPerformed( e );
                }
            };
            auMenuAction.putValue( Action.SHORT_DESCRIPTION, rb.getString( "plugin.sbEditor.addAu" ) );
            
            oscMenuAction = new AbstractAction( null, oscIcon ) {
                private static final long serialVersionUID = 1L;
                public void actionPerformed( ActionEvent e ) {
                    addOSCReceiverAction.actionPerformed( e );
                }
            };
            oscMenuAction.putValue( Action.SHORT_DESCRIPTION, rb.getString( "plugin.sbEditor.addOSC" ) );
            
            // create toolbar
            toolbar.add( setLiveToggleButton );
            toolbar.addSeparator();
            toolbar.add( midiMenuAction );
            toolbar.add( audioMenuAction );
            toolbar.add( vstMenuAction );
            toolbar.add( auMenuAction );
            toolbar.add( oscMenuAction );

            // create context menu
            JPopupMenu contextMenu = new JPopupMenu();
            
            JMenu addContextMenu = new JMenu( rb.getString( "plugin.sbEditor.node.add" ) );
            addContextMenu.setIcon( UiToolkit.SPACER );
            if (addTempoAction != null) {
                addContextMenu.add( addTempoAction );
                addContextMenu.addSeparator();
                midiMenu.add( addTempoAction );
                midiMenu.addSeparator();
            }
            if (addMidiInputAction != null) {
                addContextMenu.add( addMidiInputAction );
                midiMenu.add( addMidiInputAction );
            }
            if (addMidiOutputAction != null) {
                addContextMenu.add( addMidiOutputAction );
                midiMenu.add( addMidiOutputAction );
            }
            if (addMidiBranchAction != null) {
                addContextMenu.add( addMidiBranchAction );
                midiMenu.add( addMidiBranchAction );
            }
            if (addMidiJunctionAction != null) {
                addContextMenu.add( addMidiJunctionAction );
                midiMenu.add( addMidiJunctionAction );
            }
            if (addMidiFilterAction != null) {
                addContextMenu.add( addMidiFilterAction );
                midiMenu.add( addMidiFilterAction );
            }
            if (addMidiSamplerAction != null) {
                addContextMenu.add( addMidiSamplerAction );
                midiMenu.add( addMidiSamplerAction );
            }
            if (addMidiNoteCounterAction != null) {
                addContextMenu.add( addMidiNoteCounterAction );
                midiMenu.add( addMidiNoteCounterAction );
                addContextMenu.addSeparator();
            }
            if (addAudioInputAction != null) {
                addContextMenu.add( addAudioInputAction );
                audioMenu.add( addAudioInputAction );
            }
            if (addNetworkAudioInputAction != null) {
                addContextMenu.add( addNetworkAudioInputAction );
                audioMenu.add( addNetworkAudioInputAction );
            }
            if (addAudioOutputAction != null) {
                addContextMenu.add( addAudioOutputAction );
                audioMenu.add( addAudioOutputAction );
            }
            if (addNetworkAudioOutputAction != null) {
                addContextMenu.add( addNetworkAudioOutputAction );
                audioMenu.add( addNetworkAudioOutputAction );
            }
            if (addAudioSamplerAction != null) {
                addContextMenu.add( addAudioSamplerAction );
                audioMenu.add( addAudioSamplerAction );
            }
            boolean b = false;
            if (addVstPluginAction != null && VstContainer.getInstance().isVstContainerAvailable()) {
                b = true;
                addContextMenu.addSeparator();
                addContextMenu.add( addVstPluginAction );
            }
            if (addAudioUnitAction != null && AUContainer.getInstance().isAUContainerAvailable()) {
                if (!b) {
                    addContextMenu.addSeparator();
                }
                addContextMenu.add( addAudioUnitAction );
            }
            sessionActionPool =
                UiToolkit.getSessionUi( soundbusDescriptor.getSession() ).getActionPool();
            contextMenu.add( addContextMenu );
            contextMenu.addSeparator();
            if (setLiveAction != null) {
                setLiveMenuItems = new JCheckBoxMenuItem(
                        (String) setLiveAction.getValue( Action.NAME ),
                        (Icon) setLiveAction.getValue( Action.SMALL_ICON) );
                setLiveMenuItems.addActionListener( setLiveAction );
                setLiveMenuItems.setSelected( soundbusEditor.isSoundbusLive() );
                contextMenu.add( setLiveMenuItems );
                contextMenu.addSeparator();
            }
            undoButtons = new AbstractButton[1];
            redoButtons = new AbstractButton[1];
            undoButtons[0] = contextMenu.add( sessionActionPool.getAction( SessionActionPool.UNDO ) );
            redoButtons[0] = contextMenu.add( sessionActionPool.getAction( SessionActionPool.REDO ) );
            contextMenu.addSeparator();
            contextMenu.add( sessionActionPool.getAction( SessionActionPool.CUT ) );
            contextMenu.add( sessionActionPool.getAction( SessionActionPool.COPY ) );
            contextMenu.add( sessionActionPool.getAction( SessionActionPool.PASTE ) );
            contextMenu.addSeparator();
            contextMenu.add( sessionActionPool.getAction( SessionActionPool.DELETE ) );
            contextMenu.addSeparator();
            contextMenu.add( sessionActionPool.getAction( SessionActionPool.PROPERTIES ) );
            soundbusEditor.addMouseListener( new DefaultPopupListener( contextMenu ) );
        }
        public Object getUiObject( ViewContainer parentUiObject ) {
            return uiObject;
        }
        
//        protected void finalize() throws Throwable {
//            System.out.println( "SbEditorVi.finalize()" );
//            super.finalize();
//        }
        
        public View getView() {
            return view;
        }

        public void open() {
            // required for UNDO/REDO update
            UndoAction undo = (UndoAction) sessionActionPool.getAction( SessionActionPool.UNDO );
            UndoAction redo = (UndoAction) sessionActionPool.getAction( SessionActionPool.REDO );
            for (int i = 0; i < undoButtons.length; i++) {
                undo.addButton( undoButtons[i] );
            }
            for (int i = 0; i < redoButtons.length; i++) {
                redo.addButton( redoButtons[i] );
            }

            try {
                soundbusEditor.getSoundbusDescriptor().getSoundbus().addSoundbusListener( soundbusListener );
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IllegalSoundbusDescriptionException e) {
                e.printStackTrace();
            }
            
            soundbusEditor.getSoundbusDescriptor().addPropertyChangeListener( this );
            
            // delegate method call to soundbus editor component
            soundbusEditor.open();
        }
        
        public void activate() {
            soundbusEditor.activate();
        }

        public void deactivate() {
            soundbusEditor.deactivate();
        }

        public void close() {
            // required for UNDO/REDO update
            UndoAction undo = (UndoAction) sessionActionPool.getAction( SessionActionPool.UNDO );
            UndoAction redo = (UndoAction) sessionActionPool.getAction( SessionActionPool.REDO );
            for (int i = 0; i < undoButtons.length; i++) {
                undo.removeButton( undoButtons[i] );
            }
            for (int i = 0; i < redoButtons.length; i++) {
                redo.removeButton( redoButtons[i] );
            }
            
            soundbusEditor.getSoundbusDescriptor().removePropertyChangeListener( this );
            
            try {
                soundbusEditor.getSoundbusDescriptor().getSoundbus().removeSoundbusListener( soundbusListener );
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IllegalSoundbusDescriptionException e) {
                e.printStackTrace();
            }
            
            soundbusEditor.close();
        }
        public boolean isSetBoundsAllowed() {
            return true;
        }
        public void propertyChange( PropertyChangeEvent e ) {
            if ("changed".equals( e.getPropertyName() )) {
                soundbusEditor.updateTitle();
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
     */
    public void paintIcon( Component c, Graphics g, int x, int y ) {
        icon.paintIcon( c, g, x, y );
    }

    /* (non-Javadoc)
     * @see javax.swing.Icon#getIconWidth()
     */
    public int getIconWidth() {
        return icon.getIconWidth();
    }

    /* (non-Javadoc)
     * @see javax.swing.Icon#getIconHeight()
     */
    public int getIconHeight() {
        return icon.getIconHeight();
    }
}
