package com.jonasreese.sound.sg.ui.defaultui.plugins.soundbuseditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.jonasreese.sound.sg.SgEngine;
import com.jonasreese.sound.sg.soundbus.OSCNode;
import com.jonasreese.sound.sg.soundbus.SoundbusDescriptor;
import com.jonasreese.sound.sg.soundbus.edit.SbNodeStateChangeEdit;
import com.jonasreese.sound.sg.ui.defaultui.UiToolkit;
import com.jonasreese.util.ParametrizedResourceBundle;
import com.jonasreese.util.resource.ResourceLoader;

import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author jonas.reese
 */
public class POSCNode extends PSbNode implements PropertyChangeListener {
    private static final long serialVersionUID = 1L;
    
    static final Image MIDI_OUT_ICON = new ResourceLoader(
        PMidiInputNode.class, "/resource/midi_out.gif" ).getAsImage();

    public POSCNode(
            SbEditorComponent editor, OSCNode node, SoundbusDescriptor soundbusDescriptor ) {
        super( editor, node, soundbusDescriptor );
        backgroundPaint = new Color( 0, 0, 140, 100 );
        setTitleTextSecondLine();
    }
    
    @Override
    public void nodeAdded() {
        getSbNode().addPropertyChangeListener( this );
    }
    
    @Override
    public void nodeRemoved() {
        getSbNode().removePropertyChangeListener( this );
    }
    
    public void propertyChange( PropertyChangeEvent evt ) {
        setTitleTextSecondLine();
    }

    public void setTitleTextSecondLine() {
        OSCNode n = (OSCNode) node;
        ParametrizedResourceBundle rb = SgEngine.getInstance().getResourceBundle();
        String s1 = rb.getString("plugin.sbEditor.node.osc.generalInfoLine2", n.getReceivePort());

        String active = rb.getString("plugin.sbEditor.node.osc." + (n.isSendEnabled() ? "active" : "inactive"));
        String s2 = rb.getString("plugin.sbEditor.node.osc.generalInfoLine1", active);
        if (n.isSendEnabled()) {
            s2 += rb.getString("plugin.sbEditor.node.osc.sendDetails", n.getSendToHost(), n.getSendToPort());
        }
        setTitleTextSecondLine(s1 + "\n" + s2);
    }

    public void editNode() {

        EditNodePanel panel = new EditNodePanel( (OSCNode) node );
        Object[] message = new Object[] { panel };
        int option = JOptionPane.showConfirmDialog(
            UiToolkit.getMainFrame(), message,
            SgEngine.getInstance().getResourceBundle().getString(
                "plugin.sbEditor.node.osc.edit.title" ),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null );

        if (option == JOptionPane.OK_OPTION && panel.hasChanged()) {
            ChangeNodeEdit edit = new ChangeNodeEdit(getSoundbusDescriptor(), (OSCNode) node, panel);
            getSoundbusDescriptor().getUndoManager().addEdit(edit);
            edit.perform();
        }
    }
    
    static class EditNodePanel extends JPanel {
        private static final long serialVersionUID = 1L;

        JSpinner receivePortSpinner;
        JCheckBox sendEnabledCheckBox;
        JSpinner sendToPortSpinner;
        JTextField sendToHostTextField;
        JTextField tempoMessageAddressTextField;
        JTextField clickOnOffMessageAddressTextField;
        JCheckBox sendRegularUpdatesCheckBox;
        
        int receivePort;
        boolean sendEnabled;
        String sendToHost;
        int sendToPort;
        String tempoMessageAddress;
        String clickOnOffMessageAddress;
        boolean sendRegularUpdates;
        
        int addRow(JComponent comp, JPanel destinationPanel, String name, List<JComponent> l) {
            int w = 0;
            ResourceBundle rb = SgEngine.getInstance().getResourceBundle();
            JPanel compPanel = new JPanel(new BorderLayout());
            if (name != null) {
                JLabel label = new JLabel(rb.getString( "plugin.sbEditor.node.osc.edit." + name ) + " ");
                w = label.getPreferredSize().width;
                l.add(label);
                label.setLabelFor(comp);
                compPanel.add(BorderLayout.WEST, label);
            }
            compPanel.add(comp);
            destinationPanel.add(compPanel);

            return w;
        }
        
        void updateEnabledStates() {
            boolean b = sendEnabledCheckBox.isSelected();
            sendToPortSpinner.setEnabled(b);
            sendToHostTextField.setEnabled(b);
            tempoMessageAddressTextField.setEnabled(b);
            clickOnOffMessageAddressTextField.setEnabled(b);;
        }
        
        EditNodePanel( OSCNode node ) {
            super( new BorderLayout() );
            
            receivePort = node.getReceivePort();
            sendEnabled = node.isSendEnabled();
            sendToHost = node.getSendToHost();
            sendToPort = node.getSendToPort();
            clickOnOffMessageAddress = node.getClickOnOffMessageAddress();
            tempoMessageAddress = node.getTempoMessageAddress();
            sendRegularUpdates = node.isSendRegularUpdatesEnabled();

            ResourceBundle rb = SgEngine.getInstance().getResourceBundle();

            receivePortSpinner = new JSpinner(new SpinnerNumberModel(receivePort, 1, 0xffff, 1));
            sendEnabledCheckBox = new JCheckBox(rb.getString("plugin.sbEditor.node.osc.edit.sendEnabled"), sendEnabled);
            sendEnabledCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateEnabledStates();
                }
            });
            sendToPortSpinner = new JSpinner(new SpinnerNumberModel(sendToPort, 1, 0xffff, 1));
            sendToHostTextField = new JTextField(sendToHost, 30);
            clickOnOffMessageAddressTextField = new JTextField(clickOnOffMessageAddress, 30);
            tempoMessageAddressTextField = new JTextField(tempoMessageAddress, 30);
            sendRegularUpdatesCheckBox = new JCheckBox(rb.getString("plugin.sbEditor.node.osc.edit.sendRegularUpdates"), sendRegularUpdates);

            JPanel destinationPanel = new JPanel(new GridLayout(7, 1));
            int w = 0;
            List<JComponent> l = new ArrayList<JComponent>();
            w = Math.max(addRow(receivePortSpinner, destinationPanel, "receivePort", l), w);
            w = Math.max(addRow(sendEnabledCheckBox, destinationPanel, null, l), w);
            w = Math.max(addRow(sendToPortSpinner, destinationPanel, "sendToPort", l), w);
            w = Math.max(addRow(sendToHostTextField, destinationPanel, "sendToHost", l), w);
            w = Math.max(addRow(clickOnOffMessageAddressTextField, destinationPanel, "clickOnOffMessage", l), w);
            w = Math.max(addRow(tempoMessageAddressTextField, destinationPanel, "tempoMessage", l), w);
            w = Math.max(addRow(sendRegularUpdatesCheckBox, destinationPanel, null, l), w);
            for (JComponent comp : l) {
                Dimension d = comp.getPreferredSize();
                d.width = w;
                comp.setPreferredSize(d);
            }
            updateEnabledStates();
            
            add(destinationPanel);
        }
        int getNewPort(JSpinner sp) {
            return ((Number) ((SpinnerNumberModel) sp.getModel()).getValue()).intValue();
        }
        boolean hasChanged() {
            return (getNewPort(receivePortSpinner) != receivePort) ||
                (getNewPort(sendToPortSpinner) != sendToPort) ||
                (sendEnabledCheckBox.isSelected() != sendEnabled) ||
                !sendToHostTextField.getText().trim().equals(sendToHost) ||
                !tempoMessageAddressTextField.getText().trim().equals(tempoMessageAddress) ||
                !clickOnOffMessageAddressTextField.getText().trim().equals(clickOnOffMessageAddress) ||
                sendRegularUpdatesCheckBox.isSelected() != sendRegularUpdates;
        }
    }

    protected void paintInOutNode( InOutNode n, PPaintContext paintContext ) {
        super.paintInOutNode( n, paintContext );
        PBounds b = n.getBoundsReference();
        paintContext.getGraphics().drawImage( MIDI_OUT_ICON, (int) b.getX(), (int) b.getY(), null );
    }
    
    static class ChangeNodeEdit extends SbNodeStateChangeEdit {

        private static final long serialVersionUID = 1L;

        int receivePort;
        boolean sendEnabled;
        String sendToHost;
        int sendToPort;
        String tempoMessageAddress;
        String clickOnOffMessageAddress;
        boolean sendRegularUpdates;
        
        ChangeNodeEdit(SoundbusDescriptor soundbusDescriptor, OSCNode node, EditNodePanel editPanel) {
            super(soundbusDescriptor, node, SgEngine.getInstance().getResourceBundle().getString( "edit.changeOscNode" ), true);
            receivePort = editPanel.getNewPort(editPanel.receivePortSpinner);
            sendEnabled = editPanel.sendEnabledCheckBox.isSelected();
            sendToHost = editPanel.sendToHostTextField.getText().trim();
            sendToPort = editPanel.getNewPort(editPanel.sendToPortSpinner);
            tempoMessageAddress = editPanel.tempoMessageAddressTextField.getText().trim();
            clickOnOffMessageAddress = editPanel.clickOnOffMessageAddressTextField.getText().trim();
            sendRegularUpdates = editPanel.sendRegularUpdatesCheckBox.isSelected();
        }
        
        
        @Override
        protected void redoImpl() {
            performImpl();
        }

        @Override
        protected void undoImpl() {
            performImpl();
        }

        @Override
        public void performImpl() {
            OSCNode node = (OSCNode) getNode();

            int receivePort = node.getReceivePort();
            node.setReceivePort(this.receivePort);
            this.receivePort = receivePort;

            boolean sendEnabled = node.isSendEnabled();
            node.setSendEnabled(this.sendEnabled);
            this.sendEnabled = sendEnabled;
            
            String sendToHost = node.getSendToHost();
            node.setSendToHost(this.sendToHost);
            this.sendToHost = sendToHost;
            
            int sendToPort = node.getSendToPort();
            node.setSendToPort(this.sendToPort);
            this.sendToPort = sendToPort;
            
            String tempoMessageAddress = node.getTempoMessageAddress();
            node.setTempoMessageAddress(this.tempoMessageAddress);
            this.tempoMessageAddress = tempoMessageAddress;

            String clickOnOffMessageAddress = node.getClickOnOffMessageAddress();
            node.setClickOnOffMessageAddress(this.clickOnOffMessageAddress);
            this.clickOnOffMessageAddress = clickOnOffMessageAddress;

            boolean sendRegularUpdates = node.isSendRegularUpdatesEnabled();
            node.setSendRegularUpdatesEnables(this.sendRegularUpdates);
            this.sendRegularUpdates = sendRegularUpdates;
        }
    }
}
