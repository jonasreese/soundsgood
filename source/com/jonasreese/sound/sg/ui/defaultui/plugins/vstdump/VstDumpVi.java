/*
 * Created on 27.07.2006
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package com.jonasreese.sound.sg.ui.defaultui.plugins.vstdump;

import static com.jonasreese.sound.vstcontainer.VstPlugin.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.jonasreese.sound.sg.Session;
import com.jonasreese.sound.sg.plugin.view.View;
import com.jonasreese.sound.sg.plugin.view.ViewContainer;
import com.jonasreese.sound.sg.plugin.view.ViewInstance;
import com.jonasreese.sound.vstcontainer.VstContainer;
import com.jonasreese.sound.vstcontainer.VstPlugin;
import com.jonasreese.sound.vstcontainer.VstPluginDescriptor;
import com.jonasreese.sound.vstcontainer.VstPluginNotAvailableException;

/**
 * @author jonas.reese
 */
public class VstDumpVi implements ViewInstance {

    private VstDumpView view;
    private JPanel panel;
    private JTextArea textArea;
    
    public VstDumpVi( Session session, VstDumpView view ) {
        this.view = view;
        
        panel = new JPanel( new BorderLayout() );
        textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane( textArea );
        panel.add( scrollPane );
        JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        JButton doButton = new JButton( "Dump" );
        doButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                try {
                    dump();
                } catch (VstPluginNotAvailableException e1) {
                    e1.printStackTrace();
                }
            }
        } );
        buttonPanel.add( doButton );
        panel.add( buttonPanel, BorderLayout.SOUTH );
    }
    
    public void activate() {
    }

    public void close() {
    }

    public void deactivate() {
    }

    public Object getUiObject( ViewContainer parentUiObject ) {
        return panel;
    }

    public View getView() {
        return view;
    }

    public boolean isSetBoundsAllowed() {
        return true;
    }

    public void open() {
    }
    
    
    private void addLine() {
        addLine( 0, "" );
    }
    
    private void addLine( int indent, String text ) {
        for (int i = 0; i < indent; i++) {
            textArea.append( "  " );
        }
        textArea.append( text + "\n" );
    }
    
    // dumps text info to text field
    private void dump() throws VstPluginNotAvailableException {
        VstPluginDescriptor[] plugins = VstContainer.getInstance().getAllVstPluginDescriptors();
        for (int i = 0; i < plugins.length; i++) {
            VstPlugin p = plugins[i].createPlugin();
            addLine( 0, "Plugin: " + p.getName() );
            addLine( 1, "General" );
            addLine( 2, "Effect name: " + p.getEffectName() );
            addLine( 2, "Effect VST version: " + p.getVstVersion() );
            addLine( 2, "Vendor: " + p.getVendorString() );
            addLine( 2, "Version: " + p.getVendorVersion() );
            addLine( 2, "Category: " + getCategoryString( p.getPlugCategory() ) );
            addLine( 2, "File: " + p.getPluginLibrary() );
            addLine( 2, "Inputs/outputs: " + p.getNumInputs() + "/" + p.getNumOutputs() );
            addLine();
            addLine( 1, "Capabilities" );
            addLine( 2, "Can processReplacing: " + p.canProcessReplacing() );
            addLine( 2, "Has editor: " + p.hasEditor() );
            addLine( 2, "Can mono: " + p.canMono() );
            addLine( 2, "Can offline: " + p.canOffline() );
            addLine( 2, "Can realtime: " + p.canRealtime() );
            addLine( 2, "Can receive MIDI events: " +  getCandoString( p, CANDO_PLUG_RECEIVE_VST_MIDI_EVENT ) );
            addLine( 2, "Can send MIDI events: " +  getCandoString( p, CANDO_PLUG_SEND_VST_MIDI_EVENT ) );
            addLine( 2, "Can be plugged as channel insert: " +  getCandoString( p, CANDO_PLUG_PLUG_AS_CHANNEL_INSERT ) );
            addLine( 2, "Can be plugged as send: " +  getCandoString( p, CANDO_PLUG_PLUG_AS_SEND ) );
            addLine( 2, "Can receive VST time info: " +  getCandoString( p, CANDO_PLUG_RECEIVE_VST_TIME_INFO) );
            addLine( 2, "Can send VST time info: " +  getCandoString( p, CANDO_PLUG_SEND_VST_TIME_INFO) );
            addLine();
            addLine();
        }
    }
    
    private String getCandoString( VstPlugin p, String feature ) {
        int r = p.canDo( feature );
        if (r < 0) {
            return "No";
        } else if (r > 0) {
            return "Yes";
        } else {
            return "Don't know";
        }
    }
    
    private String getCategoryString( int category ) {
        switch (category) {
        case (VstPlugin.PLUGIN_CATEGORY_EFFECT):
            return "effect";
        case (VstPlugin.PLUGIN_CATEGORY_SYNTH):
            return "synthesizer";
        case (VstPlugin.PLUGIN_CATEGORY_ANALYSIS):
            return "analysis";
        case (VstPlugin.PLUGIN_CATEGORY_MASTERING):
            return "mastering";
        case (VstPlugin.PLUGIN_CATEGORY_SPACIALIZER):
            return "spacializer";
        case (VstPlugin.PLUGIN_CATEGORY_ROOM_FX):
            return "room FX";
        case (VstPlugin.PLUGIN_CATEGORY_SURROUND_FX):
            return "surround FX";
        case (VstPlugin.PLUGIN_CATEGORY_RESTORATION):
            return "restoration";
        case (VstPlugin.PLUGIN_CATEGORY_OFFLINE_PROCESS):
            return "offline processing";
        case (VstPlugin.PLUGIN_CATEGORY_SHELL):
            return "shell";
        case (VstPlugin.PLUGIN_CATEGORY_GENERATOR):
            return "generator";
        }
        return "unknown";
    }
}
