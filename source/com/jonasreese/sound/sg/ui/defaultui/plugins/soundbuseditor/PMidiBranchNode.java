/*
 * Created on 19.12.2005
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.Color;

import com.jonasreese.sound.sg.soundbus.MidiBranchNode;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author jonas.reese
 */
public class PMidiBranchNode extends PSbNode {

    private static final long serialVersionUID = 1L;
    
    public PMidiBranchNode(
            SbEditorComponent editor, MidiBranchNode node, SoundbusDescriptor soundbusDescriptor ) {
        super( editor, node, soundbusDescriptor );
        backgroundPaint = new Color( 55, 255, 0, 100 );
    }

    public void editNode() {
        try {
            if (getSoundbusDescriptor().getSoundbus().isOpen()) {
                return;
            }
        } catch (Exception e) {
            return;
        }
        System.out.println( "PMidiBranchNode.editNode()" );
    }
    
    protected void paintInOutNode( InOutNode n, PPaintContext paintContext ) {
        super.paintInOutNode( n, paintContext );
        PBounds b = n.getBoundsReference();
        paintContext.getGraphics().drawImage(
                n.isInputNode() ? PMidiOutputNode.MIDI_IN_ICON :
                    PMidiInputNode.MIDI_OUT_ICON, (int) b.getX(), (int) b.getY(), null );
    }
}