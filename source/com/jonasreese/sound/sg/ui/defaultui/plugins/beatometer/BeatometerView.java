/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 20.09.2003
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.beatometer;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementDescriptor;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.midi.MidiDescriptor;
import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.ui.swing.SegmentDisplayLabel;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <b>
 * The beat-o-meter <code>View</code>.
 * </b>
 * @author jreese
 */
public class BeatometerView implements View, Icon
{
    private ResourceBundle rb;
    private Icon icon;

    /**
     * Default constructor (invoked by plugin subsystem).
     */
    public BeatometerView() {
        rb = SgEngine.getInstance().getResourceBundle();
        icon = new ResourceLoader(
            getClass(), "resource/beatometer.gif" ).getAsIcon();
    }

	/* (non-Javadoc)
	 */
	public boolean canHandle( SessionElementDescriptor d ) {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.Plugin#getName()
	 */
	public String getName() {
		return rb.getString( "plugin.beatometerView.name" );
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.Plugin#getShortDescription()
	 */
	public String getShortDescription() {
		return rb.getString( "plugin.beatometerView.shortDescription" );
	}

	/* (non-Javadoc)
	 * @see com.jonasreese.sound.sg.Plugin#getDescription()
	 */
	public String getDescription() {
		return rb.getString( "plugin.beatometerView.description" );
	}

    public String getPluginName() {
        return "SoundsGood (c) Beat-O-Meter Plugin";
    }
    
    public String getPluginVersion() {
        return "1.0";
    }

    public String getPluginVendor() {
        return "Jonas Reese";
    }

    public void init() {}
    public void exit() {}

    /* (non-Javadoc)
     */
    public ViewInstance createViewInstance( Session session, SessionElementDescriptor d ) {
        return new ViewInstanceImpl();
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

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.View#isAutostartView()
     */
    public boolean isAutostartView() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.View#isMultipleInstanceView()
     */
    public boolean isMultipleInstancePerSessionAllowed() {
        return false;
    }

    public boolean isMultipleInstancePerSessionElementAllowed() {
        return false;
    }
    
    class ViewInstanceImpl extends JPanel implements ViewInstance {
        private static final long serialVersionUID = 1L;
        
        private SegmentDisplayLabel averageDisplay;
        private SegmentDisplayLabel lastValueDisplay;
        private SegmentDisplayLabel firstTenValuesDisplay;
        private SegmentDisplayLabel lastTenValuesDisplay;
        private List<Long> measures;
        private NumberFormat nf;
        
        private final JPanel createPanelFor( Component c1, Component c2 ) {
            JPanel p = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
            p.add( c1 );
            p.add( c2 );
            return p;
        }

        MidiDescriptor descriptor;
        ViewInstanceImpl() {
            super( new BorderLayout() );
            
            
            averageDisplay = new SegmentDisplayLabel( "000.00" );
            lastValueDisplay = new SegmentDisplayLabel( "000.00" );
            firstTenValuesDisplay = new SegmentDisplayLabel( "000.00" );
            lastTenValuesDisplay = new SegmentDisplayLabel( "000.00" );

            JPanel displayPanel = new JPanel( new GridLayout( 2, 2 ) );
            
            displayPanel.add( createPanelFor(
                    new JLabel( rb.getString( "plugin.beatometerView.firstTen" ) ), firstTenValuesDisplay ) );
            displayPanel.add( createPanelFor(
                    new JLabel( rb.getString( "plugin.beatometerView.lastTen" ) ), lastTenValuesDisplay ) );
            ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
            displayPanel.add( createPanelFor(
                    new JLabel( rb.getString( "plugin.beatometerView.average" ) ), averageDisplay ) );
            displayPanel.add( createPanelFor(
                    new JLabel( rb.getString( "plugin.beatometerView.lastMeasure" ) ), lastValueDisplay ) );
            
            JPanel displayWrapPanel = new JPanel();
            displayWrapPanel.add( displayPanel );
            
            add( displayWrapPanel );
            
            JPanel buttonPanel = new JPanel();
            JButton resetButton = new JButton( rb.getString(
                    "plugin.beatometerView.resetAndStart" ) );
            resetButton.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    requestFocus();
                    measures = new ArrayList<Long>();
                    updateDisplay();
                }
            } );
            buttonPanel.add( resetButton );
            add( buttonPanel, BorderLayout.SOUTH );
            nf = new DecimalFormat( "000.00", new DecimalFormatSymbols( Locale.US ) );
            setFocusable( true );
            
            enableEvents( AWTEvent.KEY_EVENT_MASK );
        }
        
        private void updateDisplay() {
            long lastTime = 0;
            double lastVal = 0;
            double acc = 0;
            double ftAcc = 0;
            double ltAcc = 0;
            for (int i = 0; i < measures.size(); i++) {
                long l = measures.get( i );
                if (i == 0) {
                    lastTime = l;
                } else {
                    long time = l - lastTime;
                    lastVal = ((double) 60000 / (double) time);
                    acc += lastVal;
                    if (measures.size() - i <= 10) {
                        ltAcc += lastVal;
                    }
                    if (i < 10) {
                        ftAcc += lastVal;
                    }
                    lastTime = l;
                }
            }
            double average = 0.0;
            double ftAverage = 0.0;
            double ltAverage = 0.0;
            if (measures.size() > 0) {
                average = acc / measures.size();
                ftAverage = ftAcc / Math.min( measures.size(), 10 );
                ltAverage = ltAcc / Math.min( measures.size(), 10 );
            }
            averageDisplay.setDisplay( nf.format( average ) );
            lastValueDisplay.setDisplay( nf.format( lastVal ) );
            firstTenValuesDisplay.setDisplay( nf.format( ftAverage ) );
            lastTenValuesDisplay.setDisplay( nf.format( ltAverage ) );
        }
        
        protected void processKeyEvent( KeyEvent e ) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                measures.add( System.currentTimeMillis() );
                updateDisplay();
            }
        }
        
        public Object getUiObject( ViewContainer parentUiObject ) {
            return this;
        }

		public void close() {
		}

		public View getView() {
			return BeatometerView.this;
		}

        public void open() {
            ViewContainer vc = (ViewContainer) UiToolkit.getViewContainer( this );
            if (vc != null) {
                vc.setTitleText( BeatometerView.this.getName() );
            }
        }

        public void activate() {
        }
        
        public void deactivate() {
        }

		public boolean isSetBoundsAllowed() {
			return false;
		}
    }

    /* (non-Javadoc)
     * @see com.jonasreese.sound.sg.View#createViewConfigurator()
     */
    public PluginConfigurator getPluginConfigurator() {
        return null;
    }
}
