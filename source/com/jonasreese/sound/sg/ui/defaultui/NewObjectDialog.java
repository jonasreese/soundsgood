/*
 * Created on 29.03.2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.SessionElementCreationHandler;
import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.ui.swing.JrDialog;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * @author jreese
 */
public class NewObjectDialog extends JrDialog implements ActionListener {
    
    private static final long serialVersionUID = 1;
    
    private static final int COLUMNS = 2;
    
    private ResourceBundle rb;

    private List<AbstractButton> buttons;
    private ButtonGroup buttonGroup;
    
    /**
     * Constructs a new <code>NewObjectDialog</code>.
     * @param parent The parent <code>Frame</code>.
     * @param title The dialog's title.
     */
    public NewObjectDialog( Frame parent, String title ) {
        super( parent, title, true );
        
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        
        rb = SgEngine.getInstance().getResourceBundle();
        
        createButtons();
        
        JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        JButton cancelButton = new JButton( rb.getString( "cancel" ) );
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                dispose();
            }
        } );
        
        JOptionPane textPane = new JOptionPane(
            rb.getString( "object.new.text" ), JOptionPane.INFORMATION_MESSAGE);
        textPane.setOptions(new Object[0]);
        getContentPane().add( textPane, BorderLayout.NORTH );

        int rows = buttons.size() / COLUMNS;
        if (buttons.size() % COLUMNS != 0) { rows++; }
        JPanel p = new JPanel( new GridLayout( rows, COLUMNS ) );
        for (int i = 0; i < buttons.size(); i++)
        {
            p.add( (JComponent) buttons.get( i ) );
        }
        JPanel tightPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        tightPanel.add( p );

        JScrollPane sp = new JScrollPane( tightPanel );
        sp.setPreferredSize( new Dimension( sp.getPreferredSize().width, 200 ) );

        getContentPane().add( sp );
        
        buttonPanel.add( cancelButton );
        getContentPane().add( buttonPanel, BorderLayout.SOUTH );
        
        pack();
        setLocation(
            parent.getX() + parent.getWidth() / 2 - getWidth() / 2,
            parent.getY() + parent.getHeight() / 2 - getHeight() / 2 );

        Action cancelKeyAction = new AbstractAction() {
            private static final long serialVersionUID = 1;
            public void actionPerformed(ActionEvent e) {
                ((AbstractButton)e.getSource()).doClick();
            }
        }; 
        KeyStroke cancelKeyStroke = KeyStroke.getKeyStroke( (char) KeyEvent.VK_ESCAPE );
        InputMap inputMap = cancelButton.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
        ActionMap actionMap = cancelButton.getActionMap();
        if (inputMap != null && actionMap != null) {
            inputMap.put(cancelKeyStroke, "cancel");
            actionMap.put("cancel", cancelKeyAction);
        }
    }
    
    private void createButtons() {
        buttons = new ArrayList<AbstractButton>();

        JButton button = new JButton(
            rb.getString( "object.new.session" ),
            new ResourceLoader( getClass(), "/resource/session_large.gif" ).getAsIcon() );
        button.addActionListener(
            UiToolkit.getActionPool().getAction( StaticActionPool.NEW_SESSION ) );
        button.addActionListener( this );
        button.setHorizontalAlignment( JButton.LEFT );

        buttons.add( button );

        SessionElementCreationHandler[] handlers =
            SgEngine.getInstance().getSessionElementCreationHandlers();
        
        for (int i = 0; i < handlers.length; i++) {
            final SessionElementCreationHandler handler = handlers[i];
            button = new JButton(
                handler.getType().getName(),
                new ImageIcon( handler.getType().getLargeIcon() ) );
            final Session session = SgEngine.getInstance().getActiveSession();
            button.setHorizontalAlignment( JButton.LEFT );
            if (UiToolkit.getSessionUi() != null && session != null) {
    	        button.addActionListener(
    	            new ActionListener() {
                        public void actionPerformed( ActionEvent e ) {
                            handler.createSessionElement( session );
                        }
                    } );
            } else {
            	button.setEnabled( false );
            }
            button.addActionListener( this );
            buttons.add( button );
        }

        buttonGroup = new ButtonGroup();
        for (int i = 0; i < buttons.size(); i++)
        {
            buttonGroup.add( (AbstractButton) buttons.get( i ) );
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed( ActionEvent e ) {
        dispose();
    }
}