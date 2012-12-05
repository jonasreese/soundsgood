/*
 * Copyright (c) 2003 Jonas Reese
 * Created on 07.09.2003
 */
package com.jonasreese.sound.sg;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import com.jonasreese.sound.sg.ui.defaultui.SgFrame;
import com.jonasreese.sound.sg.ui.defaultui.SgWelcomeWindow;

/**
 * <b>
 * </b>
 * @author jreese
 */
public class SoundsGood
{
    /** The default configuration file name */
    public static final String DEFAULT_CONFIG_FILE_NAME = "sg.ini";

    /** The application's title */
    public static final String APPLICATION_TITLE = "SoundsGood";

    private static SgFrame f;
    private static SgProperties p;
    private static File configFile;

    /**
        Displays the command-line help message and terminates the application.
    */
    private static void displayHelpMessage()
    {
        System.out.println( "usage: java com.jonasreese.sound.sg.SoundsGood [-?|<configutation_file>]" );
        System.out.println( "\t-?\t\t\tdisplays this message" );
        System.out.println( "\tconfiguration_file\tspecifies alternative configuration file." );
        System.out.println();
        System.exit( 0 );
    }


    /**
     * The application's <code>main(...)</code> method...
     * @param args The command-line arguments:<br>
     *             <blockquote>
     *             <code>
     *             -?             Display help
     *             &lt;config_file&gt;  Specify alternative configuration file.
     *             </code>
     *             </blockquote>
     */
    public static void main( String[] args ) throws Exception
    {
        
        try
        {
            // show welcome window
            JProgressBar progressBar = new JProgressBar();
            progressBar.setStringPainted( true );

            SgWelcomeWindow welcomeWindow = new SgWelcomeWindow( progressBar );
            welcomeWindow.setVisible( true );
            welcomeWindow.toFront();
    
            configFile = new File( System.getProperty( "user.dir" ), DEFAULT_CONFIG_FILE_NAME );
            if (args.length > 1) {
                displayHelpMessage();
            }
            if (args.length > 0 && args[0].equals( "-?" )) {
                displayHelpMessage();
            }
            if (args.length > 0 && args[0].equals( "debug=true" )) {
                SgFrame.setDebug( true );
            } else if (args.length > 0) {
                configFile = new File( args[0] );
            }

            progressBar.setString( "Loading configuration..." );

            // load properties from configuration file
            p = new SgProperties();
            try {
                System.out.print( "loading configuration..." );
                p.loadProperties( configFile );
                System.out.println( "done." );
            } catch (IOException ioex) {
                System.out.println( " - error! Using default configuration." );
            }
            progressBar.setValue( 20 );
            SgEngine.getInstance().setProperties( p );
            progressBar.setString( "Loading language package..." );
            ResourceBundle rb = p.getResourceBundle();
            progressBar.setValue( 45 );

            progressBar.setString( rb.getString( "startup.setLnF" ) );

            String s = SgEngine.getInstance().getProperties().getLNFClassName();
            if (s != null && !UIManager.getLookAndFeel().getClass().getName().equals( s )) {
                try {
                    UIManager.setLookAndFeel( s );
                } catch (Exception ex) {
                    System.err.println( "Error: LNF " + s + " not found, using default" );
                }
            }
            
            progressBar.setValue( 55 );

            progressBar.setString( rb.getString( "startup.creatingMainWindow" ) );

            f = new SgFrame( APPLICATION_TITLE );

            progressBar.setValue( 75 );

            progressBar.setString( rb.getString( "startup.loadingPlugins" ) );

            // try to load plugins
            try
            {
                SgEngine.getInstance().loadPlugins();
            }
            catch (IOException e) { e.printStackTrace(); } // no plugin file - no plugins!
        
            Runtime.getRuntime().addShutdownHook( new Thread()
            {
                public void run()
                {
                    try
                    {
                        System.out.print( "saving configuration..." );
                        p.storeOpenSessions();
                        p.saveProperties( configFile );
                        System.out.println( "done." );
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            } );
            
            progressBar.setValue( 100 );

            progressBar.setString( rb.getString( "startup.applicationStartupDone" ) );

            try
            {
                Thread.sleep( 350 );
            }
            catch (Exception ignored) {}

            f.adjustBounds();

            f.setVisible( true );
            System.out.println( "setVisible( true ) done." );
            welcomeWindow.dispose();
            welcomeWindow = null;

            // open recent sessions
            if (SgEngine.getInstance().getProperties().getOpenLastSessionsOnStartup())
            {
                File[] files =
                    SgEngine.getInstance().getProperties().getRecentlyOpenSessionFiles();
                int index = SgEngine.getInstance().getProperties().getRecentlyActiveSessionIndex();
                f.openSessions( files, index );
            }

            int tm = SgEngine.getInstance().getProperties().getShowTipsMode();
            boolean b = true;
            long lbs = SgEngine.getInstance().getProperties().getLastBrowserStart();
            if (tm == SgProperties.SHOW_TIPS_ONCE_A_DAY && lbs >= 0)
            {
                // compare last start date with actual date
                Calendar gc = Calendar.getInstance();
                gc.setTime( new Date( lbs ) );
                int doy = gc.get( Calendar.DAY_OF_YEAR );
                int y = gc.get( Calendar.YEAR );
                gc.setTime( new Date() );
                b = ((doy != gc.get( Calendar.DAY_OF_YEAR )) ||
                     (y != gc.get( Calendar.YEAR )));
            }
            if (((tm == SgProperties.SHOW_TIPS_ON_EVERY_START) ||
                (tm == SgProperties.SHOW_TIPS_ONCE_A_DAY) && b))
            {
                f.showTipsDialog();
            }
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            JOptionPane.showMessageDialog(
                (Component) null,
                ("Fatal error: " + t + " - " + t.getMessage()),
                "Error on SoundsGood startup", JOptionPane.ERROR_MESSAGE );
            System.exit( -1 );
        }
        // done.
    }
}
