/*
 * Created on 19.07.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.looks;

import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.jonasreese.sound.sg.plugin.PluginConfigurator;
import com.jonasreese.sound.sg.plugin.functionality.Functionality;

/**
 * Loads the JGoodies LNFs.
 * @author jonas.reese
 */
public class LooksInitializer implements Functionality {

    // no properties
    public Object getProperty( String name ) {
        return null;
    }

    public void exit() {
    }

    public String getDescription() {
        return "Loads additional Look & Feels on application startup";
    }

    public String getName() {
        return "LooksInitializer";
    }

    // no plugin configurator
    public PluginConfigurator getPluginConfigurator() {
        return null;
    }

    public String getPluginName() {
        return getName();
    }

    public String getPluginVendor() {
        return "Jonas Reese";
    }

    public String getPluginVersion() {
        return "1.0.0";
    }

    public String getShortDescription() {
        return getDescription();
    }
    
    protected boolean installLnf( String className ) {
        try {
            Object lnf = Class.forName( className ).newInstance();
            UIManager.installLookAndFeel(
                    ((LookAndFeel) lnf).getName(), lnf.getClass().getName() );
            return true;
        } catch (ClassNotFoundException cnfex) {
            System.err.println( "Extended L&F " + className + " not found" );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void init() {
        // initialize UI defaults
        UIManager.put( "OptionPane.buttonOrientation", SwingConstants.RIGHT );

        installLnf( "com.jgoodies.looks.plastic.PlasticLookAndFeel" );
        installLnf( "com.jgoodies.looks.plastic.Plastic3DLookAndFeel" );
        installLnf( "com.jgoodies.looks.plastic.PlasticXPLookAndFeel" );
        installLnf( "com.jgoodies.looks.windows.WindowsLookAndFeel" );
        if (installLnf( "org.jvnet.substance.SubstanceLookAndFeel" )) {
            UIManager.put( "lafwidgets.textEditContextMenu", Boolean.TRUE );
        }
    }

}
