/*
 * Created on 01.07.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.notedisplay;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;

import javax.swing.JLabel;

import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;

/**
 * @author jonas.reese
 */
public class NoteDisplayComponent extends JLabel {

    private static final long serialVersionUID = 1;
    
    @SuppressWarnings("unchecked")
    public NoteDisplayComponent() {
        Font f = getFont();
        Map m = f.getAttributes();
        m.put( TextAttribute.SIZE, new Float( 20.0 ) );
        setFont( new Font( m ) );
    }
    
    public void addNotify() {
        super.addNotify();
        ViewContainer vic = (ViewContainer) UiToolkit.getViewContainer( this );
        vic.setHasFixedSize( true );
    }
}
