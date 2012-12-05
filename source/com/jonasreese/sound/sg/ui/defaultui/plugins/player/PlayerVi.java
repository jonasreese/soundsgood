/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 14.10.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.player;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.SimpleTimeZone;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jonasreese.sound.sg.ObjectSelectionChangeListener;
import com.jonasreese.sound.sg.ObjectSelectionChangedEvent;
import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.midi.MidiRecorder;
import com.jonasreese.sound.sg.midi.MidiUpdatable;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.sound.sg.ui.defaultui.action.player.ClickAction;
import com.jonasreese.sound.sg.ui.defaultui.action.player.ClickAndRecordAction;
import com.jonasreese.sound.sg.ui.defaultui.action.player.FastBackwardAction;
import com.jonasreese.sound.sg.ui.defaultui.action.player.FastForwardAction;
import com.jonasreese.sound.sg.ui.defaultui.action.player.JumpToLeftMarkerAction;
import com.jonasreese.sound.sg.ui.defaultui.action.player.JumpToRightMarkerAction;
import com.jonasreese.sound.sg.ui.defaultui.action.player.PermanentThruAction;
import com.jonasreese.sound.sg.ui.defaultui.action.player.PlayAction;
import com.jonasreese.sound.sg.ui.defaultui.action.player.PlayFromLeftToRightMarkerAction;
import com.jonasreese.sound.sg.ui.defaultui.action.player.RecordAction;
import com.jonasreese.sound.sg.ui.defaultui.action.player.RecordThruAction;
import com.jonasreese.sound.sg.ui.defaultui.action.player.StopAction;
import com.jonasreese.ui.swing.SegmentDisplayLabel;

/**
 * <b>
 * The <code>ViewInstance</code> implementation for the player plugin.
 * </b>
 * @author jreese
 */
public class PlayerVi
    implements ViewInstance, ObjectSelectionChangeListener, MidiUpdatable, PropertyChangeListener
{
    private View parent;
    private Session session;
    private JComponent uiObject;
    private JComponent display;
    private JLabel nameLabel;
    private ResourceBundle rb;
    private SimpleDateFormat df;
    private Action beginAction;
    private Action backAction;
    private Action forwardAction;
    private Action endAction;
    private Action stopAction;
    private Action playAction;
    private Action playFromLeftToRightMarkerAction;
    private Action loopFromLeftToRightMarkerAction;
    private Action recAction;
    private Action clickAndRecAction;
    private Action thruAction;
    private Action recThruAction;
    private Action clickAction;
    
    private AbstractButton backwardButton;
    private AbstractButton forwardButton;
    
    private AbstractButton thruButton;
    private AbstractButton recThruButton;
    
    private AbstractButton clickButton;
    
    private MidiDescriptor[] midiDescriptors;
    
    private static final int SEPARATOR_WIDTH = 5;
    
    private static JComponent createSeparator( int width ) {
        JComponent comp = new JComponent() {
            private static final long serialVersionUID = 1L;
        };
        Dimension size = new Dimension( width, 1 );
        comp.setPreferredSize( size );
        comp.setMinimumSize( size );
        comp.setMaximumSize( size );
        return comp;
    }
    
    protected void finalize() throws Throwable {
        System.out.println( "PlayerVi.finalize()" );
        super.finalize();
    }
    
    /**
     * Constructs a new <code>PlayerVi</code> object.
     * @param parent The parent <code>View</code>.
     * @param session The current session.
     */
    public PlayerVi( View parent, Session session ) {
        this.parent = parent;
        this.session = session;
        rb = SgEngine.getInstance().getResourceBundle();

        // a trick to (ab)use a SimpleDateFormat as
        // time display formatter
        df = new SimpleDateFormat( "HH:mm:ss.SSS" );
        df.setTimeZone( new SimpleTimeZone( 0, "" ) );

        ChangeListener forwardListener = new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                if (midiDescriptors != null) {
                    if (((AbstractButton) e.getSource()).getModel().isPressed()) {
                        for (int i = 0; i < midiDescriptors.length; i++) {
                            midiDescriptors[i].getMidiRecorder().startFastForward();
                        }
                    } else {
                        for (int i = 0; i < midiDescriptors.length; i++) {
                            midiDescriptors[i].getMidiRecorder().stopFastForward();
                        }
                    }
                }
            }
        };

        ChangeListener backwardListener = new ChangeListener() {
            public void stateChanged( ChangeEvent e ) {
                if (midiDescriptors != null) {
                    if (((AbstractButton) e.getSource()).getModel().isPressed()) {
                        for (int i = 0; i < midiDescriptors.length; i++) {
                            midiDescriptors[i].getMidiRecorder().startFastBackward();
                        }
                    } else {
                        for (int i = 0; i < midiDescriptors.length; i++) {
                            midiDescriptors[i].getMidiRecorder().stopFastBackward();
                        }
                    }
                }
            }
        };

        JPanel p = new JPanel( new BorderLayout() )
        {
            private static final long serialVersionUID = 1;
            public void addNotify()
            {
                super.addNotify();
                ViewContainer vic = (ViewContainer) UiToolkit.getViewContainer( this );
                vic.setHasFixedSize( true );
            }
        };

        beginAction = new JumpToLeftMarkerAction();
        backAction = new FastBackwardAction();
        forwardAction = new FastForwardAction();
        endAction = new JumpToRightMarkerAction();
        stopAction = new StopAction();
        playAction = new PlayAction();
        playFromLeftToRightMarkerAction = new PlayFromLeftToRightMarkerAction();
        loopFromLeftToRightMarkerAction = new PlayFromLeftToRightMarkerAction( true );
        recAction = new RecordAction();
        clickAndRecAction = new ClickAndRecordAction();
        recThruAction = new RecordThruAction();
        thruAction = new PermanentThruAction();
        clickAction = new ClickAction();

        JPanel controllerPanel = new JPanel( new BorderLayout() );
        JPanel firstRowPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
        JPanel secondRowPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 0, 0 ) );
        AbstractButton b = UiToolkit.createToolbarButton( beginAction );
        int buttonWidth = b.getPreferredSize().width;
        firstRowPanel.add( b );
        backwardButton = UiToolkit.createToolbarButton( backAction );
        backwardButton.addChangeListener( backwardListener );
        firstRowPanel.add( backwardButton );
        forwardButton = UiToolkit.createToolbarButton( forwardAction );
        forwardButton.addChangeListener( forwardListener );
        firstRowPanel.add( forwardButton );
        firstRowPanel.add( UiToolkit.createToolbarButton( endAction ) );
        firstRowPanel.add( createSeparator( SEPARATOR_WIDTH ) );
        firstRowPanel.add( UiToolkit.createToolbarButton( stopAction ) );
        firstRowPanel.add( UiToolkit.createToolbarButton( playAction ) );
        firstRowPanel.add( UiToolkit.createToolbarButton( playFromLeftToRightMarkerAction ) );
        firstRowPanel.add( UiToolkit.createToolbarButton( loopFromLeftToRightMarkerAction ) );
        thruButton = UiToolkit.createToolbarButton( thruAction, true );
        clickButton = UiToolkit.createToolbarButton( clickAction );
        recThruButton = UiToolkit.createToolbarButton( recThruAction, true );
        secondRowPanel.add( recThruButton );
        secondRowPanel.add( thruButton );
        secondRowPanel.add( clickButton );
        secondRowPanel.add( createSeparator( buttonWidth ) );
        secondRowPanel.add( createSeparator( SEPARATOR_WIDTH ) );
        secondRowPanel.add( createSeparator( buttonWidth ) );
        secondRowPanel.add( createSeparator( buttonWidth ) );
        secondRowPanel.add( UiToolkit.createToolbarButton( recAction ) );
        secondRowPanel.add( UiToolkit.createToolbarButton( clickAndRecAction ) );
        controllerPanel.add( BorderLayout.NORTH, firstRowPanel );
        controllerPanel.add( BorderLayout.SOUTH, secondRowPanel );
        nameLabel = new JLabel( "", SwingConstants.CENTER );
        setDefaultNameLabelText();
        createDisplay();
        JPanel displayPanel = new JPanel( new FlowLayout( FlowLayout.CENTER ) );
        displayPanel.add( display );
        uiObject = new JPanel();
        p.add( nameLabel, BorderLayout.NORTH );
        p.add( displayPanel );
        p.add( controllerPanel, BorderLayout.SOUTH );
        uiObject.add( p );
        
        objectSelected( session.getSelectedElements() );
    }
    
    private void createDisplay() {
        System.out.println( "Player.createDisplay()" );
        String s = "";
        if (this.display == null) {
            s = df.format( new Date( 0 ) );
        } else {
            if (this.display instanceof JLabel) {
                s = ((JLabel) display).getText();
            } else if (this.display instanceof SegmentDisplayLabel) {
                s = ((SegmentDisplayLabel) this.display).getDisplay();
            }
        }
        JComponent display;
        if (SgEngine.getInstance().getProperties().getPluginProperty( parent, "lcdDisplay", true )) {
            display = new SegmentDisplayLabel( s );
        } else {
            display = new JLabel( s );
            display.setFont( new Font( "Helvetica", Font.BOLD, 24 ) );
        }
        
        if (this.display != null) {
            Container parent = this.display.getParent();
            parent.remove( this.display );
            parent.add( display );

            Object o = UiToolkit.getViewContainer( uiObject );
            if (o instanceof ViewContainer) {
                ((ViewContainer) o).adjustToPreferredSize();
            }
        }
        this.display = display;
    }
    
    private void setDefaultNameLabelText() {
        nameLabel.setText( rb.getString( "plugin.playerView.noSongPlaying" ) );
    }
    
	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.ViewInstance#getUiObject()
	 */
	public Object getUiObject( ViewContainer parentUiObject ) {
		return uiObject;
	}
    
	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.ViewInstance#getView()
	 */
	public View getView() {
		return parent;
	}

    public void open() {
        session.addObjectSelectionChangeListener( this );
        SgEngine.getInstance().getProperties().addPropertyChangeListener( this );
    }
    
    public void close() {
        System.out.println( "PlayerVi.close()" );
        SgEngine.getInstance().getProperties().removePropertyChangeListener( this );
        session.removeObjectSelectionChangeListener( this );
        objectSelected( new Object[0] );
    }
    
    public void activate() {
    }

    public void deactivate() {
    }

    private void objectSelected( Object[] sel ) {
        if (midiDescriptors != null && midiDescriptors.length > 0) {
            midiDescriptors[0].getMidiRecorder().removeMidiUpdatable( this );
            setDefaultNameLabelText();
        }
        if (sel != null && sel.length > 0) {
            boolean b = true;
            for (int i = 0; i < sel.length; i++) {
                if (!(sel[i] instanceof MidiDescriptor)) {
                    b = false;
                }
            }
            if (b) {
                midiDescriptors = new MidiDescriptor[sel.length];
                for (int i = 0; i < midiDescriptors.length; i++) {
                    midiDescriptors[i] = (MidiDescriptor) sel[i];
                }
                midiDescriptors[0].getMidiRecorder().addMidiUpdatable( this );
                deviceUpdate( midiDescriptors[0].getMidiRecorder(), midiDescriptors[0], TICK );
                if (midiDescriptors.length > 1) {
                    nameLabel.setText( rb.getString( "plugin.playerView.multipleSelected" ) );
                } else {
                    nameLabel.setText( midiDescriptors[0].getName() );
                }
            }
        }
        
        if (midiDescriptors != null) {
            boolean b = false;
            boolean b2 = false;
            for (int i = 0; i < midiDescriptors.length; i++) {
                if (midiDescriptors[i].getMidiRecorder().isRecordLoopbackEnabled()) {
                    b = true;
                }
                if (midiDescriptors[i].getMidiRecorder().isLoopbackEnabled()) {
                    b2 = true;
                }
            }
            recThruButton.setSelected( b );
            thruButton.setSelected( b2 );
        }
        
        boolean one = (sel != null && sel.length == 1 && sel[0] instanceof MidiDescriptor);
        boolean oneOrMore = (sel != null && sel.length > 0 && sel[0] instanceof MidiDescriptor);
        beginAction.setEnabled( oneOrMore );
        backAction.setEnabled( one );
        forwardAction.setEnabled( one );
        endAction.setEnabled( one );
        stopAction.setEnabled( oneOrMore );
        playAction.setEnabled( oneOrMore );
        playFromLeftToRightMarkerAction.setEnabled( one );
        loopFromLeftToRightMarkerAction.setEnabled( one );
        recAction.setEnabled( one );
        clickAction.setEnabled( one );
        clickAndRecAction.setEnabled( one );
        thruAction.setEnabled( one );
        recThruAction.setEnabled( one );
    }

	/* (non-Javadoc)
	 */
	public void objectSelectionChanged( ObjectSelectionChangedEvent e ) {
		objectSelected( e.getSelectedElements() );
	}

    protected void play( boolean fromLeftToRight ) {
        if (midiDescriptors != null) {
            try {
                for (int i = 0; i < midiDescriptors.length; i++) {
                    if (fromLeftToRight) {
                        midiDescriptors[i].getMidiRecorder().playFromLeftToRightMarker();
                    } else {
                        midiDescriptors[i].getMidiRecorder().play();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                    uiObject,
                    rb.getString( "plugin.playerView.errorOnPlayText" ) + "\n" +
                    ex.getMessage(),
                    rb.getString( "plugin.playerView.errorOnPlay" ),
                    JOptionPane.ERROR_MESSAGE );
            }
        }
    }
    
    /* (non-Javadoc)
     */
    public void deviceUpdate( MidiRecorder recorder, MidiDescriptor midiDescriptor, int updateHint ) {
        String s = df.format( new Date( recorder.getMicrosecondPosition() / 1000 ) );
        
        if ((updateHint & MidiUpdatable.TICK) != 0) {
            if (display instanceof JLabel) {
                ((JLabel) display).setText( s );
            } else if (display instanceof SegmentDisplayLabel) {
                ((SegmentDisplayLabel) display).setDisplay( s );
            }
        }
        if ((updateHint & MidiUpdatable.LOOPBACK_STATE) != 0) {
            thruButton.setSelected( recorder.isLoopbackEnabled() );
        }
        if ((updateHint & MidiUpdatable.RECORD_LOOPBACK_STATE) != 0) {
            recThruButton.setSelected( recorder.isRecordLoopbackEnabled() );
        }
    }

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.plugin.view.ViewInstance#isSetBoundsAllowed()
	 */
	public boolean isSetBoundsAllowed() {
		return false;
	}

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange( PropertyChangeEvent e ) {
        String prefix = "plugin." + parent.getClass().getName() + ".";
        if (e.getPropertyName().startsWith( prefix ))
        {
            String name = e.getPropertyName().substring( prefix.length() );
            if ("lcdDisplay".equals( name ))
            {
                createDisplay();
            }
        }
    }
}
