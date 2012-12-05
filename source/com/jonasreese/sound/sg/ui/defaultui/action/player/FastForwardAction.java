/*
 * Created on 18.04.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.action.player;

import javax.swing.Icon;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.ui.defaultui.SgAction;
import com.jonasreese.util.resource.ResourceLoader;

/**
 * <p>
 * This class implements the player action for 'fast forward'.
 * </p>
 * @author jonas.reese
 */
public class FastForwardAction extends SgAction {

    private static final long serialVersionUID = 1;
    
    private static final Icon FORWARD_ICON =
        new ResourceLoader( FastForwardAction.class, "resource/forward.gif" ).getAsIcon();

    /**
     * Constructs a new <code>FastForwardAction</code>.
     */
    public FastForwardAction() {
        super( SgEngine.getInstance().getResourceBundle().getString(
                "player.control.fastForward" ), FORWARD_ICON );
    }
}
