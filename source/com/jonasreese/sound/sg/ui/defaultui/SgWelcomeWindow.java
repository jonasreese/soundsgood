/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 01.12.2003
 */
package com.jonasreese.sound.sg.ui.defaultui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;

import com.jonasreese.util.resource.ResourceLoader;

/**
 * <b>
 * </b>
 * @author jreese
 */
public class SgWelcomeWindow extends JWindow
{
    private static final long serialVersionUID = 1;
    
    
    private static final int PROGRESS_BAR_HEIGHT = 15;
    
    public SgWelcomeWindow( JProgressBar progressBar )
    {
        getContentPane().setLayout( null );
        Icon image = new ResourceLoader( getClass(), "resource/splash.jpg" ).getAsIcon();
        JLabel l = new JLabel( image );
        getContentPane().add( l );
        setSize( image.getIconWidth(), image.getIconHeight() + PROGRESS_BAR_HEIGHT );
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation( d.width / 2 - getWidth() / 2, d.height / 2 - getHeight() / 2 );
        l.setSize( getWidth(), getHeight() - PROGRESS_BAR_HEIGHT );
        
        progressBar.setBounds( 0, l.getHeight(), l.getWidth(), PROGRESS_BAR_HEIGHT );
        getContentPane().add( progressBar );
    }
}