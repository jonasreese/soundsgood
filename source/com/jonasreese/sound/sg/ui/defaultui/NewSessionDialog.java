/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 17.09.2003
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.ui.swing.JrDialog;
import com.jonasreese.util.ExtensionFileFilter;

/**
 * <b>
 * The dialog that is being shown when the user wants to create a new
 * session.
 * </b>
 * @author jreese
 */
public class NewSessionDialog extends JrDialog
{
    private static final long serialVersionUID = 1;
    
    private ResourceBundle rb;
    private JTextField pathTextField;
    
    /**
     * Constructs a new <code>NewSessionDialog</code>.
     * @param parent The parent <code>Frame</code>.
     * @param title The dialog's title.
     */
    public NewSessionDialog( Frame parent, String title )
    {
        super( parent, title, true );
        
        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        
        rb = SgEngine.getInstance().getResourceBundle();
        
        JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        JButton okButton = new JButton( rb.getString( "ok" ) );
        Dimension d = okButton.getPreferredSize();
        okButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                try
                {
                    ok();
                }
                catch (Exception ex)
                {
                    JOptionPane.showMessageDialog(
                        NewSessionDialog.this,
                        rb.getString( "session.new.errorOnCreateFile" ) + "\n" + ex.getMessage(),
                        rb.getString( "session.new.errorOnCreateFile" ),
                        JOptionPane.ERROR_MESSAGE );
                }
                dispose();
            }
        } );
        JButton cancelButton = new JButton( rb.getString( "cancel" ) );
        d = new Dimension( Math.max( cancelButton.getPreferredSize().width, d.width ), d.height );
        okButton.setPreferredSize( d );
        cancelButton.setPreferredSize( d );
        cancelButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                dispose();
            }
        } );
        
        JOptionPane textPane = new JOptionPane(
            rb.getString( "session.new.text" ), JOptionPane.INFORMATION_MESSAGE);
        textPane.setOptions(new Object[0]);
        getContentPane().add( textPane, BorderLayout.NORTH );
        pathTextField = new JTextField();
        JPanel pathTextFieldPanel = new JPanel();
        pathTextField.setText( searchFreeSessionPath() );
        JButton selectPathButton = new JButton( rb.getString( "session.new.selectPath" ) );
        selectPathButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                File f = new File( pathTextField.getText() );
                File parent = null;
                if (f != null && f.getParent() != null)
                {
                    parent = f.getParentFile();
                }
                JFileChooser chooser = new JFileChooser( parent );
                chooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
                chooser.setFileFilter( new ExtensionFileFilter( "sgs", rb.getString( "file.soundsGoodSession" ) ) );
                int option = chooser.showOpenDialog( NewSessionDialog.this );
                if (option == JFileChooser.APPROVE_OPTION)
                {
                    f = chooser.getSelectedFile();
                    if (f != null)
                    {
                        String s = chooser.getSelectedFile().getAbsolutePath().trim();
                        if (s.indexOf( "." ) < 0)
                        {
                            s += ".sgs";
                        }
                        else if (s.endsWith( "." ))
                        {
                            s = s.substring( 0, s.length() - 1 );
                        }
                        pathTextField.setText( s );
                    }
                }
            }
        } );
        int prefWidth =
            Math.max( cancelButton.getPreferredSize().width, selectPathButton.getPreferredSize().width );
        selectPathButton.setPreferredSize(
            new Dimension( prefWidth, selectPathButton.getPreferredSize().height ) );
        cancelButton.setPreferredSize(
            new Dimension( prefWidth, cancelButton.getPreferredSize().height ) );
        Dimension ps = pathTextField.getPreferredSize();
        ps.width = textPane.getPreferredSize().width - selectPathButton.getPreferredSize().width;
        pathTextField.setPreferredSize( ps );
        pathTextFieldPanel.add( pathTextField );
        pathTextFieldPanel.add( selectPathButton );
        getContentPane().add( pathTextFieldPanel );
        
        getRootPane().setDefaultButton( okButton );
        buttonPanel.add( okButton );
        buttonPanel.add( cancelButton );
        getContentPane().add( buttonPanel, BorderLayout.SOUTH );
        
        pack();
        setLocation(
            parent.getX() + parent.getWidth() / 2 - getWidth() / 2,
            parent.getY() + parent.getHeight() / 2 - getHeight() / 2 );

        getRootPane().setDefaultButton( okButton );
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
    
    private String searchFreeSessionPath()
    {
        int i = 1;
        File f = null;
        while ((f = new File( "Session_" + i + ".sgs" ).getAbsoluteFile()).exists()) { i++; }
        return f.getAbsolutePath();
    }
    
    /**
     * performs operation when user pressed the OK button
     *
     */
    private void ok() throws IOException
    {
        File f = new File( pathTextField.getText() ).getAbsoluteFile();
        if (f.exists())
        {
            // TODO: Show warning and confirm dialog
        }
        
        SgEngine.getInstance().createSession( f );
    }
}
