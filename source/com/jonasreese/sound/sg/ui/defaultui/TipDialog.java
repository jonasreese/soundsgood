package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.SgProperties;
import com.jonasreese.ui.swing.JrDialog;
import com.jonasreese.util.resource.ResourceLoader;


/**
 * This class implements the DVBBrowser application's TIP-dialog.<br>
 * <p>
 * <b>Copyright:</b>    Copyright (c) 2003<br>
 * <b>Company:</b>      SCIP AG<br>
 * </p>
 * @author Jonas Reese
 * @version 1.0
 */
public class TipDialog extends JrDialog
{
    
    private static final long serialVersionUID = 1;
    
    private int index;
    private JOptionPane op;
    private List<String> tips;
    private JEditorPane htmlPane;
    private JPanel showTipsPanel, bottomPanel;
    private JDialog dlg;
    private ResourceBundle rb;
    
    /**
     * Constructs a new <code>TipDialog</code>.
     * @param title The dialog's title.
     * @param dvbBrowserFrame The parent <code>DVBBrowserFrame</code>.
     */
    public TipDialog( String title, JFrame parent )
    {
        super( parent, title, true );
        
        rb = SgEngine.getInstance().getResourceBundle();
        
        tips = new ArrayList<String>();
        loadTips();

        index = SgEngine.getInstance().getProperties().getTipIndex();
        op = new JOptionPane(
            "<html><b>" + rb.getString( "tips.didYouKnow" ) + "</b></html>",
            JOptionPane.PLAIN_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            new ResourceLoader( getClass(), "resource/tip.gif" ).getAsIcon(),
            new Object[0] );

        getContentPane().add( op, BorderLayout.NORTH );
        
        htmlPane = new JEditorPane();
        htmlPane.setContentType( "text/html" );
        htmlPane.setEditable( false );
        JScrollPane sp = new JScrollPane( htmlPane );
        JPanel p = new JPanel( new BorderLayout() );
        p.add( sp );
        p.add( BorderLayout.WEST, new FillComponent( 10, 10 ) );
        p.add( BorderLayout.EAST, new FillComponent( 10, 10 ) );
        p.add( BorderLayout.SOUTH, new FillComponent( 10, 10 ) );
        
        getContentPane().add( p );

        showTipsPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 10, 5 ) );
        JRadioButton everyStartRadioButton = new JRadioButton(
            rb.getString( "tips.onEveryStart" ) );
        everyStartRadioButton.setSelected(
            (SgEngine.getInstance().getProperties().getShowTipsMode() ==
                SgProperties.SHOW_TIPS_ON_EVERY_START) );
        everyStartRadioButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                SgEngine.getInstance().getProperties().setShowTipsMode(
                    SgProperties.SHOW_TIPS_ON_EVERY_START );
            }
        } );
        JRadioButton onceRadioButton = new JRadioButton(
            rb.getString( "tips.onceADay" ) );
        onceRadioButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                SgEngine.getInstance().getProperties().setShowTipsMode(
                    SgProperties.SHOW_TIPS_ONCE_A_DAY );
            }
        } );
        onceRadioButton.setSelected(
            (SgEngine.getInstance().getProperties().getShowTipsMode()
                == SgProperties.SHOW_TIPS_ONCE_A_DAY) );
        JRadioButton neverRadioButton = new JRadioButton(
            rb.getString( "tips.never" ) );
        neverRadioButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                SgEngine.getInstance().getProperties().setShowTipsMode(
                    SgProperties.SHOW_TIPS_NEVER );
            }
        } );
        neverRadioButton.setSelected(
            (SgEngine.getInstance().getProperties().getShowTipsMode()
                == SgProperties.SHOW_TIPS_NEVER) );
        
        ButtonGroup bg = new ButtonGroup();
        bg.add( everyStartRadioButton );
        bg.add( onceRadioButton );
        bg.add( neverRadioButton );
        
        showTipsPanel.add( everyStartRadioButton );
        showTipsPanel.add( onceRadioButton );
        showTipsPanel.add( neverRadioButton );
        showTipsPanel.setBorder(
            new TitledBorder( rb.getString( "tips.showTips" ) ) );
        
        JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 1, 10 ) );
        JButton prevTipButton = new JButton(
            rb.getString( "tips.previousTip" ) );
        prevTipButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                previousTip();
            }
        } );
        JButton nextTipButton = new JButton(
            rb.getString( "tips.nextTip" ) );
        nextTipButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                nextTip();
            }
        } );
        JButton closeButton = new JButton(
            rb.getString( "close" ) );
        closeButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                dispose();
            }
        } );
        int width = prevTipButton.getPreferredSize().width;
        int height = prevTipButton.getPreferredSize().height;
        int w = nextTipButton.getPreferredSize().width;
        width = (w > width ? w : width);
        w = closeButton.getPreferredSize().width;
        width = (w > width ? w : width);
        Dimension d = new Dimension( width, height );
        prevTipButton.setPreferredSize( d );
        nextTipButton.setPreferredSize( d );
        closeButton.setPreferredSize( d );
        getRootPane().setDefaultButton( closeButton );
        
        buttonPanel.add( prevTipButton );
        buttonPanel.add( nextTipButton );
        buttonPanel.add( new FillComponent( 10, 1 ) );
        buttonPanel.add( closeButton );
        buttonPanel.add( new FillComponent( 10, 1 ) );

        p = new JPanel( new BorderLayout() );
        p.add( BorderLayout.EAST, new FillComponent( 10, 1 ) );
        p.add( BorderLayout.WEST, new FillComponent( 10, 1 ) );
        p.add( showTipsPanel );
        bottomPanel = new JPanel( new BorderLayout() );
        bottomPanel.add( p, BorderLayout.NORTH );
        bottomPanel.add( buttonPanel, BorderLayout.SOUTH );
        
        getContentPane().add( bottomPanel, BorderLayout.SOUTH );
        
        Action cancelKeyAction = new AbstractAction() {
            private static final long serialVersionUID = 1;
            public void actionPerformed(ActionEvent e) {
                ((AbstractButton)e.getSource()).doClick();
            }
        }; 
        KeyStroke cancelKeyStroke = KeyStroke.getKeyStroke( (char) KeyEvent.VK_ESCAPE );
        InputMap inputMap = closeButton.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
        ActionMap actionMap = closeButton.getActionMap();
        if (inputMap != null && actionMap != null) {
            inputMap.put(cancelKeyStroke, "cancel");
            actionMap.put("cancel", cancelKeyAction);
        }
    }
    
    private void setText( String text )
    {
        htmlPane.setText( text );
        SwingUtilities.invokeLater(
            new Runnable()
            {
                public void run()
                {
                    htmlPane.scrollRectToVisible( new Rectangle( 0, 0, 10, 10 ) );
                }
            } );
    }
    
    public void setVisible( boolean b )
    {
        if (b) { b = nextTip(); }
        super.setVisible( b );
    }
    
    private void loadTips()
    {
        try
        {
            Locale l = SgEngine.getInstance().getProperties().getResourceLocale();
            InputStream in = new ResourceLoader( getClass(), "resource/tips_" +
                l.getLanguage() + "_" + l.getCountry() + ".dat" ).getResourceAsStream();
            String s;
            {
                int read = 0;
                byte[] b = new byte[in.available()];
                while (read < in.available())
                {
                    int readNow = in.read( b, read, b.length - read );
                    read += readNow;
                }
                s = new String( b );
            }
            in.close();
            
            StringTokenizer st = new StringTokenizer( s, "{" );
            while (st.hasMoreTokens())
            {
                String t = st.nextToken();
                //System.out.println( t );
                tips.add( t );
            }
        } catch (Exception ex) {
            tips.add(
                rb.getString( "tips.noTipsAvailable" ) +
                " <i>" + ex.getMessage() + "</i>" );
        }
    }
    
    private String getTip()
    {
        String tip;
        
        if (tips.size() <= index) {
            tip = (String) tips.get( tips.size() - 1 );
        } else {
            tip = (String) tips.get( index );
        }
        
        return tip;
    }
    
    public boolean nextTip()
    {
        setText( getTip() );
        index++;
        int ti = SgEngine.getInstance().getProperties().getTipIndex();
        if (index >= tips.size())
        {
            dlg = new JDialog( this,
                rb.getString( "tips.congratulations" ), true );
            dlg.getContentPane().add( new JOptionPane(
                rb.getString( "tips.congratulationsText" ),
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                new ResourceLoader( getClass(), "resource/tip_small.gif" ).getAsIcon(),
                new Object[0] ) );
            JCheckBox cb = new JCheckBox(
                rb.getString( "tips.switchOffTips" ), true );
            JButton okb = new JButton( rb.getString( "ok" ) );
            dlg.getRootPane().setDefaultButton( okb );
            JPanel p = new JPanel();
            p.add( cb );
            p.add( okb );
            okb.addActionListener( new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    dlg.setVisible( false );
                }
            } );
            dlg.getContentPane().add( p, BorderLayout.SOUTH );
            dlg.pack();
            dlg.setLocation(
                getX() + getWidth()/2 - dlg.getWidth()/2,
                getY() + getHeight()/2 - dlg.getHeight()/2 );

            dlg.setVisible( true );
            
            dlg = null;
            
            index = 0;
            SgEngine.getInstance().getProperties().setTipIndex( 0 );
            if (cb.isSelected())
            {
                SgEngine.getInstance().getProperties().setShowTipsMode( SgProperties.SHOW_TIPS_NEVER );
                dispose();
                return false;
            }
            else
            {
                nextTip();
            }
        }
        else if (index > ti)
        {
            SgEngine.getInstance().getProperties().setTipIndex( ti + 1 );
        }
        return true;
    }
    
    public void previousTip()
    {
        index -= 2;
        if (index < 0)
        {
            index += 2;
            return;
        };
        setText( getTip() );
        index++;
    }
    
    class FillComponent extends JComponent {
        private static final long serialVersionUID = 1L;

        public FillComponent( int width, int height ) {
            Dimension d = new Dimension( width, height );
            setPreferredSize( d );
            setMinimumSize( d );
            setMaximumSize( d );
        }
    }
}
